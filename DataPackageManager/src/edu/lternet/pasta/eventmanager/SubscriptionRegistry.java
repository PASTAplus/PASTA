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

package edu.lternet.pasta.eventmanager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import edu.lternet.pasta.common.*;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;

/**
* @author dcosta
* @version 1.0
* @created 18-Jun-2013 1:40:03 PM
* 
* The DataPackageRegistry class maintains the current state of data package
* resources in PASTA by reading from and writing to a database.
*/
public class SubscriptionRegistry {

 /*
  * Class variables
  */
 
 private static Logger logger = Logger.getLogger(SubscriptionRegistry.class);
 
 
 /*
  * Instance variables
  */
 
 // Name of the database table where data packages are registered
 private final String EML_SUBSCRIPTION_TABLE = "EMLSUBSCRIPTION";
 private final String EML_SUBSCRIPTION = "datapackagemanager.EMLSUBSCRIPTION";
 private final String EVENT_MANAGER_SCHEMA = "datapackagemanager";

 private String dbDriver;           // database driver
 private String dbURL;              // database URL
 private String dbUser;             // database user name
 private String dbPassword;         // database user password
 
 
 /*
  * Constructors
  */
 
 /**
  * Constructs a new DataPackageRegistry object.
  * 
  * @param   dbDriver      the database driver
  * @param   dbURL         the database URL
  * @paramm  dbUser        the database user name
  * @param   dbPassword    the database user password
  * @return  an EMLDataCache object
  */
	public SubscriptionRegistry() throws ClassNotFoundException, SQLException {

		Options options = ConfigurationListener.getOptions();
		dbDriver = options.getOption("dbDriver");
		dbURL = options.getOption("dbURL");
		dbUser = options.getOption("dbUser");
		dbPassword = options.getOption("dbPassword");

		/*
		 * Check for existence of Subscription Registry table.
		 */
		if (!isSubscriptionRegistryPresent()) {
			String message = "The subscription registry table was not found in the PASTA database.";
			throw new SQLException(message);
		}

	}
 

 /*
  * Class methods
  */
 
 /*
  * Instance methods
  */
 
	/**
	 * Adds a new subscription to the subscription registry.
	 * 
	 * @param creator      the creator name
	 * @param scope        The scope value
	 * @param identifier   The identifier integer value
	 * @param revision     The revision integer value
	 * @param url          The URL value
	 * @param mayOverwrite if true, an existing subscription may be overwritten with the new values
	 * @return the subscription id of the added subscription
	 */
	public int addSubscription(String creator, String scope, Integer identifier, Integer revision, String url) 
					throws ClassNotFoundException, SQLException {
	    int subscriptionId = 0;
		Connection connection = null;
		java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
		int existingSubscriptionId = hasSubscription(creator, scope,
				identifier, revision, url);

		if (existingSubscriptionId < 0) {
			String queryStr = String.format(
					"INSERT INTO %s (active,date_created,creator,scope,identifier,revision,url) " +
					"VALUES(?,?,?,?,?,?,?)", EML_SUBSCRIPTION);

			logger.debug("queryStr: " + queryStr);
			
			try {
				connection = getConnection();
		        PreparedStatement pstmt = connection.prepareStatement(queryStr, Statement.RETURN_GENERATED_KEYS);
				pstmt.setBoolean(1, true);
				pstmt.setTimestamp(2, ts);
				pstmt.setString(3, creator);

				if (scope == null) { 
					pstmt.setNull(4, java.sql.Types.VARCHAR); 
			    } 
				else { 
					pstmt.setString(4, scope); 
			    }

				if (identifier == null) {
					pstmt.setNull(5, java.sql.Types.INTEGER);
				}
				else {
					pstmt.setInt(5, identifier);
				}
				
				if (revision == null) {
					pstmt.setNull(6, java.sql.Types.INTEGER);
				}
				else {
					pstmt.setInt(6, revision);
				}

				pstmt.setString(7, url);
				pstmt.executeUpdate();
		        ResultSet rs = pstmt.getGeneratedKeys();
		        while (rs.next()) {
		          subscriptionId = rs.getInt(1);
		        }
				pstmt.close();
			}
			catch (SQLException e) {
				logger.error(String.format("Error inserting record into the subscription registry (%s): %s",
								EML_SUBSCRIPTION, e.getMessage()));
				throw (e);
			}
			finally {
				returnConnection(connection);
			}
		}
		else {
			throw new ResourceExistsException(
					String.format("Subscription already exists with subscription ID = %d", existingSubscriptionId));
		}
		
		return subscriptionId;
	}
		

	/**
	 * Deletes test subscriptions. Needed for JUnit testing.
	 */
	public void deleteTestSubscriptions()
			throws ClassNotFoundException, SQLException, Exception {
		Connection conn = getConnection();

		String queryStr = String.format("DELETE FROM %s where creator = 'junit'",
				                         EML_SUBSCRIPTION);

		logger.debug("queryStr: " + queryStr);

		conn = getConnection();

		if (conn != null) {
			try {
				Statement stmt = conn.createStatement();
				int nRecords = stmt.executeUpdate(queryStr);
				stmt.close();
				logger.info(String.format("Deleted %d JUnit subscriptions", nRecords));
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				returnConnection(conn);
			}
		}
		else {
			String message = "deleteTestSubscriptions() failed due to connection error.";
			Exception e = new Exception(message);
			throw (e);
		}
	}

	
	/**
	 * Deletes a subscription based on its subscription ID. The user who is deleting
	 * the subscription must be the same as the user who created the subscription.
	 * 
	 * @param subscriptionId    the subscription ID value
	 * @param uid  the user id
	 * @return true if successfully deleted, else false
	 */
	public Integer deleteSubscription(Integer subscriptionId, String uid)
			throws ClassNotFoundException, SQLException, Exception {
		Integer deletedId = new Integer("-1");
		
		if (wasDeleted(subscriptionId)) {
			String msg = String.format("Subscription with ID = %d had previously been deleted.", subscriptionId);
			throw new ResourceDeletedException(msg);
		}

		if (!hasSubscription(subscriptionId)) {
			String msg = String.format("Subscription with ID = %d was not found..", subscriptionId);
			throw new ResourceNotFoundException(msg);
		}

		Connection conn = getConnection();

		if (conn != null) {
			try {
				String querySQL = String.format("SELECT creator FROM %s WHERE subscription_id=?", 
		                EML_SUBSCRIPTION);
				PreparedStatement pstmt = conn.prepareStatement(querySQL);
				pstmt.setInt(1, subscriptionId);
			    ResultSet rs = pstmt.executeQuery();
			    String creator = null;
			   
			    while (rs.next()) {
			       creator = rs.getString(1);
			    }

				pstmt.close();
			    
			    if (creator == null || !creator.equals(uid)) {
			    	String msg = String.format(
								"User '%s' is not authorized to delete subscription with ID = %d",
			    			uid, subscriptionId);
			    	throw new UnauthorizedException(msg);
			    }
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				returnConnection(conn);
			}
		}
		else {
			String message = "deleteSubscription() failed due to connection error.";
			Exception e = new Exception(message);
			throw (e);
		}
		
		
		String queryStr = String.format(
				"UPDATE %s SET active=false WHERE subscription_id=?",
				                         EML_SUBSCRIPTION);

		logger.debug("queryStr: " + queryStr);

		conn = getConnection();

		if (conn != null) {
			try {
				PreparedStatement pstmt = conn.prepareStatement(queryStr);
				pstmt.setInt(1, subscriptionId);
				int nRecords = pstmt.executeUpdate();
				pstmt.close();

				// Should update one or more records in the subscription registry
				if (nRecords >= 1) {
					deletedId = subscriptionId;
				}
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				returnConnection(conn);
			}
		}
		else {
			String message = "deleteSubscription() failed due to connection error.";
			Exception e = new Exception(message);
			throw (e);
		}

		return deletedId;
	}

	
	/**
	 * Returns a connection to the database.
	 * 
	 * @return conn the database Connection object
	 */
	private Connection getConnection() throws ClassNotFoundException {
		Connection conn = null;
		SQLWarning warn;

		// Load the jdbc driver
		try {
			Class.forName(dbDriver);
		}
		catch (ClassNotFoundException e) {
			logger.error("Can't load driver " + e.getMessage());
			throw (e);
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
 
 
 /*
  * Boolean to determine whether the specified subscription is in the
  * Subscription Registry based on a specified values
  * 
  * @return  a positive value if the resource is in the registry, else -1
  */
 private int hasSubscription(String creator, String scope, Integer identifier, Integer revision, String url)
         throws ClassNotFoundException, SQLException {
   int subscriptionId = -1;
   String scopeClause = (scope == null) ? "scope IS NULL" : String.format("scope=%s", SqlEscape.str(scope));
   String identifierClause = (identifier == null) ? "identifier IS NULL" : String.format("identifier=%s", SqlEscape.integer(identifier));
   String revisionClause = (revision == null) ? "revision IS NULL" : String.format("revision=%s", SqlEscape.integer(revision));
   Connection connection = null;

	 String queryStr =
     String.format(
				 "SELECT subscription_id " +
						 "FROM %s " +
						 "WHERE creator=%s AND %s AND %s AND %s AND url=%s AND active=true",
				 SqlEscape.name(EML_SUBSCRIPTION),
				 SqlEscape.str(creator),
				 scopeClause,
				 identifierClause,
				 revisionClause,
				 SqlEscape.str(url)
		 );

	 logger.debug("queryStr: " + queryStr);

   Statement stmt = null;
 
   try {
     connection = getConnection();
     stmt = connection.createStatement();             
     ResultSet rs = stmt.executeQuery(queryStr);
   
     while (rs.next()) {
       subscriptionId = rs.getInt(1);
     }

		 stmt.close();
   }
   catch(ClassNotFoundException e) {
     logger.error("ClassNotFoundException: " + e.getMessage());
     throw(e);
   }
   catch(SQLException e) {
     logger.error("SQLException: " + e.getMessage());
     throw(e);
   }
   finally {
     returnConnection(connection);
   }
   
   return subscriptionId;
 }
 

 /*
  * Boolean to determine whether the specified subscription is in the
  * Subscription Registry based on a subscription ID value
  * 
  * @return  true if a subscription matches the id, else false
  */
 private boolean hasSubscription(Integer subscriptionId)
         throws ClassNotFoundException, SQLException {
   boolean hasSubscription = false;
   Connection connection = null;
   String queryStr =
     String.format(
				 "SELECT * FROM %s WHERE subscription_id=%d AND active=true",
    		 EML_SUBSCRIPTION, subscriptionId
		 );

	 logger.debug("queryStr: " + queryStr);

   Statement stmt = null;
 
   try {
     connection = getConnection();
     stmt = connection.createStatement();             
     ResultSet rs = stmt.executeQuery(queryStr);
   
     while (rs.next()) {
       hasSubscription = true;
     }

		 stmt.close();
   }
   catch(ClassNotFoundException e) {
     logger.error("ClassNotFoundException: " + e.getMessage());
     throw(e);
   }
   catch(SQLException e) {
     logger.error("SQLException: " + e.getMessage());
     throw(e);
   }
   finally {
     returnConnection(connection);
   }
   
   return hasSubscription;
 }
 

 /**
  * Boolean to determine whether the Data Package Registry
  * table already exists. If it isn't present, it will need to
  * be created.
  * 
  * @return  isPresent, true if the data cache table is present, else false
  */
 private boolean isSubscriptionRegistryPresent() 
         throws ClassNotFoundException, SQLException {          
   boolean isPresent = false;
   String catalog = null;          // A catalog name (may be null)
   Connection connection = null;
   DatabaseMetaData databaseMetaData = null; // For getting db metadata
   ResultSet rs;
   String schemaPattern = EVENT_MANAGER_SCHEMA; // A schema name pattern
   String tableNamePattern = "%";  // Matches all table names in the db
   String[] types = {"TABLE"};     // A list of table types to include
   
   try {
     connection = getConnection();
     
     if (connection != null) {
       databaseMetaData = connection.getMetaData();
       rs = databaseMetaData.getTables(catalog, schemaPattern, 
                                   tableNamePattern, types);
       while (rs.next()) {
         String TABLE_NAME = rs.getString("TABLE_NAME");

         if (TABLE_NAME.equalsIgnoreCase(EML_SUBSCRIPTION_TABLE)) {
           isPresent = true;
         }
       }
     
       if (rs != null) rs.close();
     }
     else {
       SQLException e = new SQLException("Unable to connect to database.");
       throw(e);
     }
   }
   catch (ClassNotFoundException e) {
     throw(e);
   }
   catch (SQLException e) {
     throw(e);
   }
   finally {
     returnConnection(connection);
   }
   
   return isPresent;
 }
 
 
	/**
	 * Retrieves a subscription based on its subscription ID. The user who is reading
	 * the subscription must be the same as the user who created the subscription.
	 * 
	 * @param subscriptionId    the subscription ID value
	 * @param uid  the user id
	 * @return the subscription XML string, if successful
	 */
	public EmlSubscription getSubscription(Integer subscriptionId, String uid)
			throws ClassNotFoundException, SQLException, Exception {
		EmlSubscription emlSubscription = null;
		String creator = null;

		if (wasDeleted(subscriptionId)) {
			String msg = String.format(
					"Subscription with ID = %d had previously been deleted.", subscriptionId);
			throw new ResourceDeletedException(msg);
		}

		if (!hasSubscription(subscriptionId)) {
			String msg = String.format("Subscription with ID = %d was not found or may have been deleted.",
							           subscriptionId);
			throw new ResourceNotFoundException(msg);
		}

		Connection conn = getConnection();

		if (conn != null) {

			String queryStr = String.format(
					"SELECT date_created, creator, scope, identifier, revision, url " +
							"FROM %s " +
							"WHERE subscription_id=%d and active='true'",
					EML_SUBSCRIPTION, subscriptionId);

			logger.debug("queryStr: " + queryStr);

			Statement stmt = null;

			try {
				stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(queryStr);

				while (rs.next()) {
					//java.sql.Timestamp sqlTimestamp = rs.getTimestamp(1);
					creator = rs.getString(2);
					String scope = rs.getString(3);
					Object identifierObj = rs.getObject(4);
					Integer identifier = identifierObj == null ? null : (Integer) identifierObj;
					Object revisionObj = rs.getObject(5);
					Integer revision = revisionObj == null ? null : (Integer) revisionObj;
					String url = rs.getString(6);
					emlSubscription = new EmlSubscription();
					emlSubscription.setSubscriptionId(subscriptionId);
					emlSubscription.setCreator(creator);
					emlSubscription.setScope(scope);
					emlSubscription.setIdentifier(identifier);
					emlSubscription.setRevision(revision);
					emlSubscription.setUrl(url);
				}
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				if (stmt != null)
					stmt.close();
				returnConnection(conn);
			}
		}

	    if (creator == null || !creator.equals(uid)) {
	    	String msg = String.format("User '%s' is not authorized to read subscription with ID = %d", 
	    			                   uid, subscriptionId);
	    	throw new UnauthorizedException(msg);
	    }

	    return emlSubscription;
	}

	
	/**
	 * Gets a list of EML subscriptions from the 'emlsubscription' table
	 * matching the provided criteria.
	 * 
	 * @param userId the user id
	 * @param queryParams
	 *            a map of query parameters and the values they should be
	 *            matched to
	 * @return a list of matching EmlSubscription objects
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 */
	public List<EmlSubscription> getSubscriptions(String userId, Map<String, List<String>> queryParams)
			throws ClassNotFoundException, SQLException,
			IllegalArgumentException {
		List<EmlSubscription> emlSubscriptions = new ArrayList<EmlSubscription>();

		if (queryParams != null) {
			Connection connection = null;

			String whereClause = composeWhereClause(userId, queryParams);

			String queryStr = String
					.format(
							"SELECT subscription_id, date_created, creator, scope, identifier, revision, url " +
									"FROM %s %s",
							EML_SUBSCRIPTION, whereClause);

			logger.debug("queryStr: " + queryStr);

			Statement stmt = null;

			try {
				connection = getConnection();
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(queryStr);

				while (rs.next()) {
					int subscriptionId = rs.getInt(1);
					//java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
					String creator = rs.getString(3);
					String scope = rs.getString(4);
					Object identifierObj = rs.getObject(5);
					Integer identifier = identifierObj == null ? null : (Integer) identifierObj;
					Object revisionObj = rs.getObject(6);
					Integer revision = revisionObj == null ? null : (Integer) revisionObj;
					String url = rs.getString(7);
					EmlSubscription emlSubscription = new EmlSubscription();
                    emlSubscription.setSubscriptionId(subscriptionId);
					//java.util.Date entryTime = new
					//java.util.Date(sqlTimestamp.getTime());
					//emlSubscription.setEntryTime(entryTime);
					emlSubscription.setCreator(creator);
					emlSubscription.setScope(scope);
					emlSubscription.setIdentifier(identifier);
					emlSubscription.setRevision(revision);
					emlSubscription.setUrl(url);
					emlSubscriptions.add(emlSubscription);			 
				}
			}
			catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException: " + e.getMessage());
				throw (e);
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				if (stmt != null)
					stmt.close();
				returnConnection(connection);
			}
		}

		return emlSubscriptions;
	}


	/**
	 * Gets a list of EML subscriptions from the 'emlsubscription' table
	 * matching the specified package id.
	 * 
	 * @param queryParams
	 *            a map of query parameters and the values they should be
	 *            matched to
	 * @return a list of matching EmlSubscription objects
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws IllegalArgumentException
	 */
	public List<EmlSubscription> getSubscriptions(EmlPackageId emlPackageId)
			throws ClassNotFoundException, SQLException,
			IllegalArgumentException {
		List<EmlSubscription> emlSubscriptions = new ArrayList<EmlSubscription>();

		if (emlPackageId != null) {
			Connection connection = null;

			String whereClause = composeWhereClause(emlPackageId);
			String queryStr = String
					.format(
							"SELECT subscription_id, date_created, creator, scope, identifier, revision, url " +
									"FROM %s %s",
							EML_SUBSCRIPTION, whereClause);

			logger.debug("queryStr: " + queryStr);

			Statement stmt = null;

			try {
				connection = getConnection();
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(queryStr);

				while (rs.next()) {
					int subscriptionId = rs.getInt(1);
					//java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
					String creator = rs.getString(3);
					String scope = rs.getString(4);
					Object identifierObj = rs.getObject(5);
					Integer identifier = identifierObj == null ? null : (Integer) identifierObj;
					Object revisionObj = rs.getObject(6);
					Integer revision = revisionObj == null ? null : (Integer) revisionObj;
					String url = rs.getString(7);
					EmlSubscription emlSubscription = new EmlSubscription();
                    emlSubscription.setSubscriptionId(subscriptionId);
					//java.util.Date entryTime = new
					//java.util.Date(sqlTimestamp.getTime());
					//emlSubscription.setEntryTime(entryTime);
					emlSubscription.setCreator(creator);
					emlSubscription.setScope(scope);
					emlSubscription.setIdentifier(identifier);
					emlSubscription.setRevision(revision);
					emlSubscription.setUrl(url);
					emlSubscriptions.add(emlSubscription);			 
				}
			}
			catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException: " + e.getMessage());
				throw (e);
			}
			catch (SQLException e) {
				logger.error("SQLException: " + e.getMessage());
				throw (e);
			}
			finally {
				if (stmt != null)
					stmt.close();
				returnConnection(connection);
			}
		}

		return emlSubscriptions;
	}


	private String composeWhereClause(String userId, Map<String, List<String>> queryParams) {
		String whereClause = null;

		StringBuffer stringBuffer = null;
		try {
			stringBuffer = new StringBuffer(String.format(" WHERE active='true' AND creator=%s", SqlEscape.str(userId)));
		} catch (SQLException e) {
			e.printStackTrace();
		}

		for (String key : queryParams.keySet()) {
			stringBuffer.append(" AND ");
			List<String> values = queryParams.get(key);
			String orClause = composeORClause(key, values);
			stringBuffer.append(orClause);
		}

		whereClause = stringBuffer.toString();
		return whereClause;
	}


	private String composeWhereClause(EmlPackageId emlPackageId) {
		String whereClause = null;

		StringBuffer stringBuffer = new StringBuffer(" WHERE active='true'");
		
		String scope = emlPackageId.getScope();
		String scopeClause = null;
		try {
			scopeClause = (scope == null) ? "scope IS NULL" : String.format("(scope=%s OR scope IS NULL)", SqlEscape.str(scope));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		Integer identifier = emlPackageId.getIdentifier();
		String identifierClause = (identifier == null) ? "identifier IS NULL" : String.format("(identifier=%d OR identifier IS NULL)", identifier);
		Integer revision = emlPackageId.getRevision();
		String revisionClause = (revision == null) ? "revision IS NULL" : String.format("(revision=%d OR revision IS NULL)", revision);
		stringBuffer.append(String.format(" AND %s AND %s AND %s", scopeClause, identifierClause, revisionClause));
		
		whereClause = stringBuffer.toString();
		return whereClause;
	}


	private String composeORClause(String key, List<String> values) {
		StringBuffer stringBuffer = new StringBuffer("( ");
		boolean firstValue = true;

		if (values.size() == 0)
			return "";

		for (String value : values) {
			if (!firstValue) {
				stringBuffer.append(" OR ");
			}
			
			// Fortunately, there's a direct one-to-one mapping between query keys and database field names
			String fieldName = key;

			try {
				if (fieldName.equalsIgnoreCase("identifier") ||
						fieldName.equalsIgnoreCase("revision")) {
					stringBuffer.append(String.format("%s=%s",
							SqlEscape.name(fieldName),
							SqlEscape.integer(value)));
				}
				else {
					stringBuffer.append(String.format(
							"%s=%s",
							SqlEscape.name(fieldName),
							SqlEscape.str(value)
						)
					);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			firstValue = false;
		}

		stringBuffer.append(" )");
		String orClause = stringBuffer.toString();
		return orClause;
	}


 /**
  * Closes the connection to the database.
  */
 private void returnConnection(Connection conn) {
   try {
     // Close the database connection
     logger.debug("Closing the database connection");
     
     if (conn != null) {
       conn.close();
     }
     else {
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

 
 /*
  * Boolean to determine whether the specified subscription is in the
  * Subscription Registry based on a subscription ID value but has
  * been deleted (inactivated)
  * 
  * @return  true if a subscription matches the id, else false
  */
 private boolean wasDeleted(Integer subscriptionId)
         throws ClassNotFoundException, SQLException {
   boolean wasDeleted = false;
   Connection connection = null;
   String queryStr = String.format(
				 "SELECT * FROM %s WHERE subscription_id = %d AND active=false",
    		 EML_SUBSCRIPTION, subscriptionId
		 );

	 logger.debug("queryStr: " + queryStr);

   Statement stmt = null;
 
   try {
     connection = getConnection();
     stmt = connection.createStatement();             
     ResultSet rs = stmt.executeQuery(queryStr);
   
     while (rs.next()) {
       wasDeleted = true;
     }

		 stmt.close();
   }
   catch(ClassNotFoundException e) {
     logger.error("ClassNotFoundException: " + e.getMessage());
     throw(e);
   }
   catch(SQLException e) {
     logger.error("SQLException: " + e.getMessage());
     throw(e);
   }
   finally {
     returnConnection(connection);
   }
   
   return wasDeleted;
 }
 

}