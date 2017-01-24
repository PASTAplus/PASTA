/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.datapackagemanager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.ucsb.nceas.utilities.Options;


/**
 * @author Duane Costa
 * 
 * The ReservationManager class maintains the current state of data package
 * that have previously been reserved for use by end users.
 */
public class ReservationManager {

  /*
   * Class variables
   */
  
  private static Logger logger = Logger.getLogger(ReservationManager.class);
    
  
  /*
   * Instance variables
   */
  
  // Name of the database table where data packages are registered
  private final String DATA_PACKAGE_MANAGER_SCHEMA = "datapackagemanager";
  private final String RESERVATION_TABLE = "RESERVATION";
  private final String RESERVATION = "datapackagemanager.RESERVATION";
 
  private String dbDriver;           // database driver
  private String dbURL;              // database URL
  private String dbUser;             // database user name
  private String dbPassword;         // database user password
  
  
  /*
   * Constructors
   */
  
	/**
	 * Constructs a new ReservationManager object.
	 * 
	 * @param dbDriver
	 *            the database driver
	 * @param dbURL
	 *            the database URL
	 * @paramm dbUser 
	 *            the database user name
	 * @param dbPassword
	 *            the database user password
	 * @return a Reservation object
	 */
	public ReservationManager(String dbDriver, String dbURL, String dbUser, String dbPassword)
			throws ClassNotFoundException, SQLException {

		this.dbDriver = dbDriver;
		this.dbURL = dbURL;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;

		/*
		 * Check for existence of the WORKING_ON table.
		 */
		if (!isReservationTablePresent()) {
			String message = String.format("The %s table was not found in the PASTA database.", RESERVATION);
			throw new SQLException(message);
		}

	}

	
  /*
   * Class methods
   */
  
  /**
   * Main method that can be used for testing the Reservation class. It takes
   * no arguments.
   */
	public static void main(String[] args) {

		Options options = null;
		options = ConfigurationListener.getOptions();

		String dirPath = "WebRoot/WEB-INF/conf";

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		ReservationManager reservationManager = null;
		String dbDriver = options.getOption("dbDriver");
		String dbURL = options.getOption("dbURL");
		String dbUser = options.getOption("dbUser");
		String dbPassword = options.getOption("dbPassword");
		
		final String scope = "edi";
		final Integer identifier = new Integer(99);
		final String principal = "uid=LNO,o=LTER,dc=ecoinformatics,dc=org";

		try {
			reservationManager = new ReservationManager(dbDriver, dbURL, dbUser, dbPassword);
			reservationManager.addDataPackageReservation(scope, identifier, principal);

			String reservationsXML = reservationManager.listActiveReservations();
			System.out.println(reservationsXML);
			boolean isActive = reservationManager.isActiveReservation(scope, identifier);
			String word = isActive ? "" : "NOT ";
			System.out.println(String.format("%s.%d IS %sACTIVE.", scope, identifier, word));
			
			String identifiers = reservationManager.listReservationIdentifiers(scope);
			System.out.println(String.format("\nIdentifiers reserved for scope %s:", scope));
			System.out.println(identifiers);
			
			reservationManager.setDateUploaded(scope, identifier);
		    reservationsXML = reservationManager.listActiveReservations();
			System.out.println(reservationsXML);
			isActive = reservationManager.isActiveReservation(scope, identifier);
			word = isActive ? "" : "NOT ";
			System.out.println(String.format("%s.%d IS %sACTIVE.", scope, identifier, word));
			
			reservationManager.deleteDataPackageReservation(scope, identifier);

			identifiers = reservationManager.listReservationIdentifiers(scope);
			System.out.println(String.format("\nIdentifiers reserved for scope %s:", scope));
			System.out.println(identifiers);
			
		    reservationsXML = reservationManager.listActiveReservations();
			System.out.println(reservationsXML);
			isActive = reservationManager.isActiveReservation(scope, identifier);
			word = isActive ? "" : "NOT ";
			System.out.println(String.format("%s.%d IS %sACTIVE.", scope, identifier, word));
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	
  /*
   * Instance methods
   */
  
	/**
	 * Adds a data package identifier reservation to the reservation table. 
	 * This is called when an end user client application reserves a data 
	 * package identifier.
	 * 
	 * @param scope
	 *            The scope value
	 * @param identifier
	 *            The identifier integer value
	 * @param principal
	 *            The user who is making the reservation
	 */
	public void addDataPackageReservation(String scope, Integer identifier, String principal)
			throws ClassNotFoundException, SQLException {
		Connection connection = null;
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		String packageId = String.format("%s.%d", scope, identifier);

		StringBuilder insertSQL = new StringBuilder("INSERT INTO " + RESERVATION + "(");
		insertSQL.append("scope, identifier, principal, date_reserved) " + "VALUES(?,?,?,?)");

		String insertString = insertSQL.toString();
		logger.debug("insertString: " + insertString);

		try {
			connection = getConnection();
			PreparedStatement pstmt = connection.prepareStatement(insertString);
			pstmt.setString(1, scope);
			pstmt.setInt(2, identifier);
			pstmt.setString(3, principal);
			pstmt.setTimestamp(4, ts);

			pstmt.executeUpdate();
			if (pstmt != null) {
				pstmt.close();
			}
			logger.info(String.format("Data package identifier %s has been reserved.", packageId));
		} 
		catch (SQLException e) {
			logger.error("Error reserving data package identifier " + packageId + " into table " + RESERVATION);
			logger.error("SQLException: " + e.getMessage());
			throw (e);
		} 
		finally {
			returnConnection(connection);
		}
	}

	
	/**
	 * Deletes a data package identifier reservation from the reservation table. 
	 * This is provided as a convenience to JUnit test so they can clean-up 
	 * after themselves.
	 * 
	 * @param scope
	 *            The scope value
	 * @param identifier
	 *            The identifier integer value
	 */
	public void deleteDataPackageReservation(String scope, Integer identifier)
			throws ClassNotFoundException, SQLException {
		Connection connection = null;
		String packageId = String.format("%s.%d", scope, identifier);

		StringBuilder deleteSQL = new StringBuilder("DELETE FROM " + RESERVATION + 
				" WHERE scope=? AND identifier=?");

		String deleteString = deleteSQL.toString();

		try {
			connection = getConnection();
			PreparedStatement pstmt = connection.prepareStatement(deleteString);
			pstmt.setString(1, scope);
			pstmt.setInt(2, identifier);
			pstmt.executeUpdate();
			if (pstmt != null) {
				pstmt.close();
			}
			logger.info(String.format("Data package identifier %s has been deleted from the %s table", 
					                  packageId, RESERVATION));
		} 
		catch (SQLException e) {
			logger.error(String.format("Error deleting data package identifier %s from table %s",
					                   packageId, RESERVATION));
			logger.error("SQLException: " + e.getMessage());
			throw (e);
		} 
		finally {
			returnConnection(connection);
		}
	}

	
	/**
	 * Sets the date_uploaded value of the data package, marking its
	 * identifier value as no longer actively reserved.
	 * 
	 * @param scope
	 *            The scope value
	 * @param identifier
	 *            The identifier integer value
	 * @throws SQLException
	 */
	public void setDateUploaded(String scope, Integer identifier) 
			throws SQLException {
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		String packageId = String.format("%s.%d", scope, identifier);

		Connection conn = null;
		try {
			conn = this.getConnection();
		} 
		catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String updateSQL = "UPDATE datapackagemanager.RESERVATION " + 
		"SET date_uploaded=? WHERE scope=? AND identifier=?";

		Integer rowCount = null;

		try {
			PreparedStatement pstmt = conn.prepareStatement(updateSQL);
	        pstmt.setTimestamp(1, ts);          // The upload date-time
	        pstmt.setString(2, scope);          // Set WHERE scope value
	        pstmt.setInt(3, identifier);        // Set WHERE identifier value
	        rowCount = pstmt.executeUpdate();
	        pstmt.close();
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		finally {
			returnConnection(conn);
		}

		if (rowCount == null || rowCount < 1) {
			String msg = String.format("Reservation.setDateUploaded(): Failed to set date_uploaded for data package %s", packageId); 
			throw new SQLException(msg);
		}
		else {
			logger.info(String.format("Data package %s has been uploaded. Its identifier is no longer reserved.", packageId));
		}
	}

	
  /**
   * Returns a connection to the database.
   * 
   * @return  conn  the database Connection object
   */
  public Connection getConnection() 
          throws ClassNotFoundException {
    Connection conn = null;
    SQLWarning warn;
    
    // Load the jdbc driver
    try {
      Class.forName(dbDriver);
    }
    catch (ClassNotFoundException e) {
      logger.error("Can't load driver " + e.getMessage());
      throw(e);
    } 

    // Make the database connection
    try {
      conn = DriverManager.getConnection(dbURL, dbUser, dbPassword);

      // If a SQLWarning object is available, print its warning(s).
      // There may be multiple warnings chained.
      warn = conn.getWarnings();
      
      if (warn != null) {
        while (warn != null) {
          logger.warn("SQLState: " + warn.getSQLState());
          logger.warn("Message:  " + warn.getMessage());
          logger.warn("Vendor: " + warn.getErrorCode());
          warn = warn.getNextWarning();
        }
      }
    }
    catch (SQLException e) {
      logger.error("Database access failed " + e);
    }
    
    return conn;
  }
  
  
	/**
	 * Gets a list of active reservations.
	 * 
	 * @return    an XML string listing the active reservations
	 */
	public String listActiveReservations() throws Exception {
		Connection conn = null;
		String activeReservationsXML = null;
		StringBuilder queryBuilder = new StringBuilder();
		StringBuilder xmlBuilder = new StringBuilder("<reservations>\n");

		queryBuilder.append("SELECT scope, identifier, date_reserved, principal FROM ");
		queryBuilder.append(RESERVATION);
		queryBuilder.append(" WHERE date_uploaded IS NULL");
		queryBuilder.append(" ORDER BY date_reserved ASC;");
		String sqlQuery = queryBuilder.toString();

		try {
			conn = getConnection();

			if (conn != null) {
				Statement stmnt = conn.createStatement();
				ResultSet rs = stmnt.executeQuery(sqlQuery);

				while (rs.next()) {
					xmlBuilder.append("  <reservation>\n");
					String scope = rs.getString(1);
					Integer identifier = rs.getInt(2);
					java.sql.Timestamp date_reserved = rs.getTimestamp(3);
					String dateReserved = date_reserved.toString();
					String principal = rs.getString(4);
					String docId = String.format("%s.%d", scope, identifier);
					xmlBuilder.append(String.format("    <docid>%s</docid>\n", docId));
					xmlBuilder.append(String.format("    <principal>%s</principal>\n", principal));
					xmlBuilder.append(String.format("    <dateReserved>%s</dateReserved>\n", dateReserved));
					xmlBuilder.append("  </reservation>\n");
				}
			}
		}
		finally {
			xmlBuilder.append("</reservations>\n");
			returnConnection(conn);
		}
		
		activeReservationsXML = xmlBuilder.toString();
		return activeReservationsXML;
	}
  
  
	/**
	 * Gets a simple flat list of active reservation identifiers for the 
	 * specified scope.
	 * 
	 * @return    a newline-separated list of numeric identifier values
	 */
	public String listReservationIdentifiers(String scope) throws Exception {
		Connection conn = null;
		StringBuilder queryBuilder = new StringBuilder();
		StringBuilder identifiers = new StringBuilder("");

		queryBuilder.append("SELECT identifier FROM ");
		queryBuilder.append(RESERVATION);
		queryBuilder.append(" WHERE date_uploaded IS NULL AND");
		queryBuilder.append(String.format(" scope='%s'", scope));
		queryBuilder.append(" ORDER BY identifier ASC;");
		String sqlQuery = queryBuilder.toString();

		try {
			conn = getConnection();

			if (conn != null) {
				Statement stmnt = conn.createStatement();
				ResultSet rs = stmnt.executeQuery(sqlQuery);

				while (rs.next()) {
					Integer identifier = rs.getInt(1);
					identifiers.append(String.format("%d\n", identifier));
				}
			}
		}
		finally {
			returnConnection(conn);
		}
		
		String identifiersList = identifiers.toString().trim();
		return identifiersList;
	}
  
  
	/**
	 * Boolean to determine whether the specified data package identifier is 
	 * actively reserved in PASTA, based on a specified scope and identifier.
	 * A data package is reserved if it is present in the reservation table and
	 * its upload date is NULL.
	 * 
	 * @param scope
	 *            the scope value, e.g. "knb-lter-nin"
	 * @param identifier
	 *            the identifier value, e.g. 1
	 */
	public boolean isActiveReservation(String scope, Integer identifier)
			throws ClassNotFoundException, SQLException {
		boolean isActiveIdentifier = false;
		Connection connection = null;
		String selectString = 
				"SELECT count(*) FROM " + RESERVATION + 
				"  WHERE scope='" + scope + 
				"' AND identifier='" + identifier + 
				"' AND date_uploaded IS NULL;";

		Statement stmt = null;

		try {
			connection = getConnection();
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(selectString);

			while (rs.next()) {
				int count = rs.getInt("count");
				isActiveIdentifier = (count > 0);
			}

			if (stmt != null)
				stmt.close();
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException: " + e.getMessage());
			throw (e);
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw (e);
		} finally {
			returnConnection(connection);
		}

		return isActiveIdentifier;
	}  

	
	/**
	 * For a given identifier, determine which user id, if any, has actively
	 * reserved that identifier. May return null.
	 * 
	 * @param scope
	 *            the scope value, e.g. "knb-lter-nin"
	 * @param identifier
	 *            the identifier value, e.g. 1
	 * @return the user id, e.g. "uid=LNO,o=LTER,dc=ecoinformatics,dc=org"
	 */
	public String getReservationUserId(String scope, Integer identifier)
			throws ClassNotFoundException, SQLException {
		String userId = null;
		Connection connection = null;
		String selectString = 
				"SELECT principal FROM " + RESERVATION + 
				"  WHERE scope='" + scope + 
				"' AND identifier='" + identifier + 
				"' AND date_uploaded IS NULL;";

		Statement stmt = null;

		try {
			connection = getConnection();
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(selectString);

			while (rs.next()) {
				userId = rs.getString(1);
			}

			if (stmt != null)
				stmt.close();
		} catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException: " + e.getMessage());
			throw (e);
		} catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			throw (e);
		} finally {
			returnConnection(connection);
		}

		return userId;
	}  

	
   /**
   * Boolean to determine whether the data package identifier Reservation
   * table already exists. If it isn't present, it will need to
   * be created.
   * 
   * @return  isPresent, true if the data cache table is present, else false
   */
	private boolean isReservationTablePresent() throws ClassNotFoundException, SQLException {
		boolean isPresent = false;
		String catalog = null; // A catalog name (may be null)
		Connection connection = null;
		DatabaseMetaData databaseMetaData = null; // For getting db metadata
		ResultSet rs;
		String schemaPattern = DATA_PACKAGE_MANAGER_SCHEMA; // A schema name
															// pattern
		String tableNamePattern = "%"; // Matches all table names in the db
		String[] types = { "TABLE" }; // A list of table types to include

		try {
			connection = getConnection();

			if (connection != null) {
				databaseMetaData = connection.getMetaData();
				rs = databaseMetaData.getTables(catalog, schemaPattern, tableNamePattern, types);
				while (rs.next()) {
					String tableName = rs.getString("TABLE_NAME");

					if (tableName.equalsIgnoreCase(RESERVATION_TABLE)) {
						isPresent = true;
					}
				}

				if (rs != null)
					rs.close();
			} else {
				SQLException e = new SQLException("Unable to connect to database.");
				throw (e);
			}
		} catch (ClassNotFoundException e) {
			throw (e);
		} catch (SQLException e) {
			throw (e);
		} finally {
			returnConnection(connection);
		}

		return isPresent;
	}
  
	
	/**
	 * Closes the connection to the database.
	 */
	public void returnConnection(Connection conn) {
		try {
			// Close the database connection
			logger.debug("Closing the database connection");

			if (conn != null) {
				conn.close();
			} else {
				logger.warn("Unable to close connection, 'conn' object is null");
			}
		}
		// We won't worry about throwing an exception if the connection return
		// fails. Just issue a log error.
		catch (SQLException e) {
			logger.error("Failed to close connection. Database access failed " + 
		                 e.getMessage());
		}
	}

}