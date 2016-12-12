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
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.ucsb.nceas.utilities.Options;


/**
 * @author Duane Costa
 * 
 * The WorkingOn class maintains the current state of data packages
 * that are currently being worked on in PASTA.
 */
public class WorkingOn {

  /*
   * Class variables
   */
  
  private static Logger logger = Logger.getLogger(WorkingOn.class);
    
  
  /*
   * Instance variables
   */
  
  // Name of the database table where data packages are registered
  private final String DATA_PACKAGE_MANAGER_SCHEMA = "datapackagemanager";
  private final String WORKING_ON_TABLE = "WORKING_ON";
  private final String WORKING_ON = "datapackagemanager.WORKING_ON";
 
  private String dbDriver;           // database driver
  private String dbURL;              // database URL
  private String dbUser;             // database user name
  private String dbPassword;         // database user password
  
  
  /*
   * Constructors
   */
  
	/**
	 * Constructs a new WorkingOn object.
	 * 
	 * @param dbDriver
	 *            the database driver
	 * @param dbURL
	 *            the database URL
	 * @paramm dbUser 
	 *            the database user name
	 * @param dbPassword
	 *            the database user password
	 * @return a WorkingOn object
	 */
	public WorkingOn(String dbDriver, String dbURL, String dbUser, String dbPassword)
			throws ClassNotFoundException, SQLException {

		this.dbDriver = dbDriver;
		this.dbURL = dbURL;
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;

		/*
		 * Check for existence of the WORKING_ON table.
		 */
		if (!isWorkingOnTablePresent()) {
			String message = String.format("The %s table was not found in the PASTA database.", WORKING_ON);
			throw new SQLException(message);
		}

	}

	
  /*
   * Class methods
   */
  
  /**
   * Main method that can be used for testing the WorkingOn class. It takes
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

		WorkingOn workingOn = null;
		String dbDriver = options.getOption("dbDriver");
		String dbURL = options.getOption("dbURL");
		String dbUser = options.getOption("dbUser");
		String dbPassword = options.getOption("dbPassword");

		try {
			workingOn = new WorkingOn(dbDriver, dbURL, dbUser, dbPassword);
			Map<String, String> active = workingOn.listActiveDataPackages();
			System.out.println("Package ID       Start Date");
			for (String key : active.keySet()) {
				System.out.println(String.format("  %s  %s", key, active.get(key)));
			}
			
			boolean isActive = workingOn.isActive("knb-lter-xyz", new Integer(1), new Integer(1));
			String word = isActive ? "" : "not ";
			System.out.println("knb-lter-xyz IS " + word + "ACTIVE");
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		/*
		 * Uncomment this block to test the detectInterrupted() method, but
		 * be aware that detectInterrupted() has the side effect of setting
		 * the "interrupted" field to true.
		 * 
		try {
			workingOn.detectInterrupted();
		}
		catch (SQLException e) {
			;
		}
		*/

	}

	
  /*
   * Instance methods
   */
  
	/**
	 * Adds a data package record to the working_on table. This is called when
	 * PASTA begins working on the data package.
	 * 
	 * @param scope
	 *            The scope value
	 * @param identifier
	 *            The identifier integer value
	 * @param revision
	 *            The revision value
	 */
	public void addDataPackage(String scope, Integer identifier, Integer revision)
			throws ClassNotFoundException, SQLException {
		Connection connection = null;
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		String packageId = String.format("%s.%d.%d", scope, identifier, revision);

		StringBuilder insertSQL = new StringBuilder("INSERT INTO " + WORKING_ON + "(");
		insertSQL.append("scope, identifier, revision, start_date) " + "VALUES(?,?,?,?)");

		String insertString = insertSQL.toString();
		logger.debug("insertString: " + insertString);

		try {
			connection = getConnection();
			PreparedStatement pstmt = connection.prepareStatement(insertString);
			pstmt.setString(1, scope);
			pstmt.setInt(2, identifier);
			pstmt.setInt(3, revision);
			pstmt.setTimestamp(4, ts);

			pstmt.executeUpdate();
			if (pstmt != null) {
				pstmt.close();
			}
			logger.info(String.format("Work on data package %s has been started.", packageId));
		} 
		catch (SQLException e) {
			logger.error("Error inserting data package " + packageId + " into table " + WORKING_ON);
			logger.error("SQLException: " + e.getMessage());
			throw (e);
		} 
		finally {
			returnConnection(connection);
		}
	}

	
	/**
	 * Sets the end_date value of the data package, marking it as no longer
	 * actively being worked on. (Note: this is not the same as saying that the
	 * data package was successfully uploaded to PASTA.)
	 * 
	 * @param scope
	 *            The scope value
	 * @param identifier
	 *            The identifier integer value
	 * @param revision
	 *            The revision value
	 * @throws SQLException
	 */
	public void updateEndDate(String scope, Integer identifier, Integer revision) 
			throws SQLException {
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		String packageId = String.format("%s.%d.%d", scope, identifier, revision);

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String updateSQL = "UPDATE datapackagemanager.WORKING_ON " + 
		"SET end_date=? WHERE scope=? AND identifier=? AND revision=? AND interrupted=?";

		Integer rowCount = null;

		try {
			PreparedStatement pstmt = conn.prepareStatement(updateSQL);
	        pstmt.setTimestamp(1, ts);          // The field to be updated
	        pstmt.setString(2, scope);          // Set WHERE scope value
	        pstmt.setInt(3, identifier);        // Set WHERE identifier value
	        pstmt.setInt(4, revision);          // Set WHERE revision value
	        pstmt.setBoolean(5, false);         // Set WHERE interrupted value
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
			String msg = String.format("WorkingOn.updateEndDate(): Failed to set end_date for data package %s", packageId); 
			throw new SQLException(msg);
		}
		else {
			logger.info(String.format("Work on data package %s has been completed.", packageId));
		}
	}

	
	/**
	 * Detects data packages that were interrupted before they could be
	 * completed in PASTA and sets the "interrupted" field in the WORKING_ON
	 * table to true. 
	 * 
	 * This method should be called when the server initializes. A servlet
	 * dedicated to performing this task at initialization is the
	 * WorkingOnServlet.
	 * 
	 * @throws SQLException
	 */
	public void detectInterrupted() 
			throws SQLException {
		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String updateSQL = "UPDATE datapackagemanager.WORKING_ON " + 
		"SET interrupted=? WHERE end_date IS NULL AND interrupted=?";

		Integer rowCount = null;

		try {
			PreparedStatement pstmt = conn.prepareStatement(updateSQL);
	        pstmt.setBoolean(1, true);          // The field to be updated
	        pstmt.setBoolean(2, false);         // Set WHERE interrupted value
	        rowCount = pstmt.executeUpdate();
	        
			if (rowCount > 0) {
				logger.warn(
						String.format(
								"%d data packages were interrupted with the last shutdown.", 
								rowCount));
			}
			
	        pstmt.close();
		} 
		catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} 
		finally {
			returnConnection(conn);
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
	 * Gets a list of recent uploads, either inserts or updates.
	 * 
	 * @return    a Map of active data packages in PASTA where the key is the 
	 *            package ID and the value is the timestamp as of when
	 *            activity on the data package started.
	 */
	public Map<String, String> listActiveDataPackages() throws Exception {
		Connection conn = null;
		Map<String, String> activeDataPackages = new HashMap<String, String>();
		StringBuilder sb = new StringBuilder();
		TreeSet<String> docids = new TreeSet<String>();

		sb.append("SELECT scope, identifier, revision, start_date FROM ");
		sb.append(WORKING_ON);
		sb.append(" WHERE start_date IS NOT NULL  AND ");
		sb.append("   end_date IS NULL AND ");
		sb.append("   interrupted=false ");
		sb.append("ORDER BY start_date ASC;");
		String sqlQuery = sb.toString();

		try {
			conn = getConnection();

			if (conn != null) {
				Statement stmnt = conn.createStatement();
				ResultSet rs = stmnt.executeQuery(sqlQuery);

				while (rs.next()) {
					String scope = rs.getString(1);
					Integer identifier = rs.getInt(2);
					Integer revision = rs.getInt(3);
					java.sql.Timestamp startDate = rs.getTimestamp(4);
					String uploadDate = startDate.toString();
					String packageId = String.format("%s.%d.%d", scope, identifier, revision);
					activeDataPackages.put(packageId, uploadDate);
				}
			}
		}
		finally {
			returnConnection(conn);
		}

		return activeDataPackages;
	}
  
  
	/**
	 * Boolean to determine whether the specified data package is actively being
	 * processed in PASTA, based on a specified scope, identifier, and revision.
	 * A data package is active if its end_date is NULL and there is no record
	 * of it being interrupted by a previous shutdown.
	 * 
	 * @param scope
	 *            the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *            the identifier value, e.g. 1
	 * @param revision
	 *            the revision value, e.g. 2
	 */
	public boolean isActive(String scope, Integer identifier, Integer revision)
			throws ClassNotFoundException, SQLException {
		boolean hasDataPackage = false;
		Connection connection = null;
		String selectString = 
				"SELECT count(*) FROM " + WORKING_ON + 
				"  WHERE scope='" + scope + 
				"' AND identifier='" + identifier + 
				"' AND revision='" + revision + 
				"' AND end_date IS NULL AND interrupted='false';";

		Statement stmt = null;

		try {
			connection = getConnection();
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(selectString);

			while (rs.next()) {
				int count = rs.getInt("count");
				hasDataPackage = (count > 0);
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

		return hasDataPackage;
	}  

	
   /**
   * Boolean to determine whether the Data Package Registry
   * table already exists. If it isn't present, it will need to
   * be created.
   * 
   * @return  isPresent, true if the data cache table is present, else false
   */
	private boolean isWorkingOnTablePresent() throws ClassNotFoundException, SQLException {
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
					String TABLE_NAME = rs.getString("TABLE_NAME");

					if (TABLE_NAME.equalsIgnoreCase(WORKING_ON_TABLE)) {
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