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
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.lternet.pasta.datapackagemanager.checksum.ChecksumException;
import edu.lternet.pasta.doi.DOIException;
import edu.lternet.pasta.doi.Resource;
import edu.lternet.pasta.common.DataPackageUpload;
import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.security.token.BasicAuthToken;
import edu.ucsb.nceas.utilities.Options;


/**
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 * 
 * The DataPackageRegistry class maintains the current state of data package
 * resources in PASTA by reading from and writing to a database.
 */
public class DataPackageRegistry {

  /*
   * Class variables
   */
  
  private static Logger logger = Logger.getLogger(DataPackageRegistry.class);
  
  private static final String PUBLIC = "public";
  private static final String EML_VERSION = "2.1.0";
  
  
  /*
   * Instance variables
   */
  
  // Name of the database table where data packages are registered
  private final String ACCESS_MATRIX_TABLE = "ACCESS_MATRIX";
  private final String ACCESS_MATRIX = "datapackagemanager.ACCESS_MATRIX";
  private final String DATA_PACKAGE_MANAGER_SCHEMA = "datapackagemanager";
  private final String PROVENANCE_TABLE = "PROVENANCE";
  private final String PROVENANCE = "datapackagemanager.PROVENANCE";
  private final String RESOURCE_REGISTRY = "datapackagemanager.RESOURCE_REGISTRY";
  private final String RESOURCE_REGISTRY_TABLE = "RESOURCE_REGISTRY";
 
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
  public DataPackageRegistry(String dbDriver, String dbURL, String dbUser,
                      String dbPassword) 
          throws ClassNotFoundException, SQLException {
  	
    this.dbDriver = dbDriver;
    this.dbURL = dbURL;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
    
    /*
     * Check for existence of Data Package Registry table. 
     * Create it if it does not already exist.
     */
    if (!isDataPackageRegistryPresent()) {
      String message = "The data package registry table was not found in the PASTA database.";
      throw new SQLException(message);
    }
    
  }
  

  /*
   * Class methods
   */
  
  /**
   * @param args
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

    DataPackageRegistry dpr = null;
    String dbDriver = "org.postgresql.Driver";
    String dbURL = "jdbc:postgresql://localhost:5432/pasta";
    String dbUser = "pasta";
    String dbPassword = "p@st@";
    
    // String resourceId = "http://localhost:8000/package/report/eml/knb-lter-nin/1/1";
    String resourceId = "http://localhost:8000/package/report/eml/knb-lter-atz/1/1";

    try {
      dpr = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
      if (dpr.getDoi(resourceId) == null) {
        logger.info("It's NULL");
      } else {
        logger.info("It's not NULL");
      }
    } catch (Exception e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

  }


  /*
   * Instance methods
   */
  
  /**
   * Adds an access control rule to the access_matrix table.
   * @param resourceId   the resource identifier value
   * @param principal    the user that the rule applies to  
   * @param accessType   the access type, e.g. 'allow', 'deny'
   * @param accessOrder  the access order,e.g. 'allowFirst', 'denyFirst'
   * @param permission   the permission, e.g. 'read'
   * @param mayOverwrite boolean to determine whether this rule may
   *                     overwrite an existing rule (used for evaluate mode)
   */
  public void addAccessControlRule(String resourceId, 
                                   String principal, 
                                   Authorizer.AccessType accessType,
                                   Authorizer.AccessOrder accessOrder,
                                   Rule.Permission permission,
                                   boolean mayOverwrite)
          throws ClassNotFoundException, SQLException {
    Connection connection = null;
    
    /*
     * If this resource may be overwritten, and it is already in the 
     * resource registry, then delete any existing access control rules 
     * for the specified resourceId and principal.
     */
    if (mayOverwrite && hasResource(resourceId)) {
      String deleteSQL = "DELETE FROM "  + ACCESS_MATRIX +
                         "  WHERE resource_id=? AND principal=?";
      String deleteString = deleteSQL.toString();
      logger.debug("deleteString: " + deleteString);

      try {
        connection = getConnection();
        PreparedStatement pstmt = connection.prepareStatement(deleteString);
        pstmt.setString(1, resourceId);
        pstmt.setString(2, principal);
        pstmt.executeUpdate();
        if (pstmt != null) { pstmt.close(); }
      }
      catch (SQLException e) {
        logger.error(
          "Error deleting record for resource " + resourceId +
          " and princiapl " + principal +
          " from the access matrix table (" + ACCESS_MATRIX + ")");
        logger.error("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
 
    StringBuffer insertSQL = 
      new StringBuffer("INSERT INTO " + ACCESS_MATRIX + "(");
    insertSQL.append("resource_id, principal, access_type, access_order, permission) " + 
                     "VALUES(?,?,?,?,?)");      
    String insertString = insertSQL.toString();
    logger.debug("insertString: " + insertString);

    try {
      connection = getConnection();
      PreparedStatement pstmt = connection.prepareStatement(insertString);
      pstmt.setString(1, resourceId);
      pstmt.setString(2, principal);
      pstmt.setObject(3, accessType, java.sql.Types.OTHER);
      pstmt.setObject(4, accessOrder, java.sql.Types.OTHER);
      pstmt.setObject(5, permission, java.sql.Types.OTHER);
      pstmt.executeUpdate();
      if (pstmt != null) {
        pstmt.close();
      }
    }
    catch (SQLException e) {
      logger.error("Error inserting access control record for resource " + resourceId +
                   " and principal " + principal +
                   " into the access matrix table (" + ACCESS_MATRIX_TABLE + ")");
      logger.error("SQLException: " + e.getMessage());
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
  }

  
  /**
   * Adds an access control rule to the access_matrix table.
   * @param resourceId   the resource identifier value
   * @param ruleList     a list of Rule objects
   * @param mayOverwrite boolean to determine whether this rule may
   *                     overwrite an existing rule (used for evaluate mode)
   */
  public void addAccessControlRules(String resourceId, 
                                    ArrayList<Rule> ruleList,
                                    boolean mayOverwrite)
          throws ClassNotFoundException, SQLException {
    Connection connection = null;
    
    /*
     * If this resource may be overwritten, and it is already in the 
     * resource registry, then delete any existing access control rules 
     * for the specified resourceId.
     */
    if (mayOverwrite && hasResource(resourceId)) {
      String deleteSQL = "DELETE FROM "  + ACCESS_MATRIX +
                         "  WHERE resource_id=?";
      String deleteString = deleteSQL.toString();
      logger.debug("deleteString: " + deleteString);

      try {
        connection = getConnection();
        PreparedStatement pstmt = connection.prepareStatement(deleteString);
        pstmt.setString(1, resourceId);
        pstmt.executeUpdate();
        if (pstmt != null) { pstmt.close(); }
      }
      catch (SQLException e) {
        logger.error(
          "Error deleting record for resource " + resourceId +
          " from the access matrix table (" + ACCESS_MATRIX + ")");
        logger.error("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
    
    if (ruleList != null) {
      for (Rule rule : ruleList) {

        String principal = rule.getPrincipal();
        String accessType = rule.getAccessType();
        String accessOrder = rule.getOrder();
        Rule.Permission permission = rule.getPermission();

        StringBuffer insertSQL = new StringBuffer("INSERT INTO "
            + ACCESS_MATRIX + "(");
        insertSQL
            .append("resource_id, principal, access_type, access_order, permission) "
                + "VALUES(?,?,?,?,?)");
        String insertString = insertSQL.toString();
        logger.debug("insertString: " + insertString);

        try {
          connection = getConnection();
          PreparedStatement pstmt = connection.prepareStatement(insertString);
          pstmt.setString(1, resourceId);
          pstmt.setString(2, principal);
          pstmt.setObject(3, accessType, java.sql.Types.OTHER);
          pstmt.setObject(4, accessOrder, java.sql.Types.OTHER);
          pstmt.setObject(5, permission, java.sql.Types.OTHER);
          pstmt.executeUpdate();
          if (pstmt != null) {
            pstmt.close();
          }
        }
        catch (SQLException e) {
          logger.error("Error inserting access control record for resource "
              + resourceId + " and principal " + principal
              + " into the access matrix table (" + ACCESS_MATRIX_TABLE + ")");
          logger.error("SQLException: " + e.getMessage());
          throw (e);
        }
        finally {
          returnConnection(connection);
        }
      }
    }
  }

  
	/**
	 * Adds a new resource to the data package resource registry.
	 * 
	 * @param resourceId   The full resource identifier value
	 * @param resourceType The resource type, one of a fixed set of values
   * @param resourceLocation The resource location, may be null
	 * @param packageId    The packageId value
	 * @param scope        The scope value
	 * @param identifier   The identifier integer value
	 * @param revision     The revision value
	 * @param entityId     The entityId value (may be null if this is not a data entity resource)
	 * @param entityName   The entityName value (may be null if this is not a data entity resource)
	 * @param principalOwner The user (principal) who owns the the resource 
	 * @param formatType   The format type, currently used for metadata resources only,
	 *                       may be null, e.g. "eml://ecoinformatics.org/eml-2.1.1"
	 * @param mayOverwrite If true, an existing resource with the same resourceId may
	 *                     be overwritten by the new resource by updating its creation date.
	 *                     This would be typically be set true only for evaluation resources
	 *                     such as evaluation reports. If false, an error is generated.
	 */
 	public void addDataPackageResource(
 	   String resourceId, 
 	   DataPackageManager.ResourceType resourceType,
 	   String resourceLocation,
 	   String packageId, String scope, Integer identifier, Integer revision,
 	   String entityId, String entityName, String principalOwner, String formatType,
 	   boolean mayOverwrite)
          throws ClassNotFoundException, SQLException {
    Connection connection = null;
	  java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
	  
	  /*
	   * If this resource may be overwritten, and it is already in the resource
	   * registry, then update its creation date instead of inserting a new
	   * entry.
	   */
	  if (mayOverwrite && hasResource(resourceId)) {
	    StringBuffer updateSQL = new StringBuffer("UPDATE " + RESOURCE_REGISTRY + 
	                                              " SET date_created=? " +
	                                              " WHERE resource_id=?");
	    String updateString = updateSQL.toString();
	    logger.debug("updateString: " + updateString);

	    try {
	      connection = getConnection();
	      PreparedStatement pstmt = connection.prepareStatement(updateString);
	      pstmt.setTimestamp(1, ts);
	      pstmt.setString(2, resourceId);
	      pstmt.executeUpdate();
	      if (pstmt != null) { pstmt.close(); }
	    }
	    catch (SQLException e) {
	      logger.error(
	        "Error updating record for resource " + resourceId + 
	        " in the resource registry (" + RESOURCE_REGISTRY + ")");
	      logger.error("SQLException: " + e.getMessage());
	      throw(e);
	    }
	    finally {
	      returnConnection(connection);
	    }
	  }
	  else {
      StringBuffer insertSQL = new StringBuffer("INSERT INTO " + 
                                                RESOURCE_REGISTRY + 
                                                "(");
      if (resourceType == ResourceType.data) {
        insertSQL.append("resource_id, resource_type, package_id, scope, identifier, " + 
                         "revision, resource_location, entity_id, entity_name, principal_owner, date_created) " + 
                         "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
      }
      else {
        insertSQL.append("resource_id, resource_type, package_id, scope, identifier, " + 
                         "revision, principal_owner, date_created, format_type) " + 
                         "VALUES(?,?,?,?,?,?,?,?,?)");
      }
      String insertString = insertSQL.toString();
      logger.debug("insertString: " + insertString);

      try {
        connection = getConnection();
        PreparedStatement pstmt = connection.prepareStatement(insertString);
        pstmt.setString(1, resourceId);
        pstmt.setObject(2, resourceType, java.sql.Types.OTHER);
        pstmt.setString(3, packageId);
        pstmt.setString(4, scope);
        pstmt.setInt(5, identifier);
        pstmt.setInt(6, revision);
        if (resourceType == ResourceType.data) {
          pstmt.setString(7, resourceLocation);
          pstmt.setString(8, entityId);
          pstmt.setString(9, entityName);
          pstmt.setString(10, principalOwner);
          pstmt.setTimestamp(11, ts);
        }
        else {
          pstmt.setString(7, principalOwner);
          pstmt.setTimestamp(8, ts);
          pstmt.setString(9, formatType);
        }
        pstmt.executeUpdate();
        if (pstmt != null) {
          pstmt.close();
        }
      }
      catch (SQLException e) {
        logger.error("Error inserting record for resource " + resourceId
            + " into the resource registry (" + RESOURCE_REGISTRY + ")");
        logger.error("SQLException: " + e.getMessage());
        throw (e);
      }
      finally {
        returnConnection(connection);
      }
	  }
  }

	/**
	 * Add the DOI of resource to the resource registry..
	 * 
	 * @param resourceId
	 *          The resource identifier of the resource to be updated
	 * @param doi
	 *          The DOI of the resource
	 * @throws DOIException
	 * @throws SQLException
	 */
	public void addResourceDoi(String resourceId, String doi)
	    throws DOIException, SQLException {

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "UPDATE datapackagemanager.resource_registry"
		    + " SET doi='" + doi + "' WHERE resource_id='" + resourceId + "';";

		Statement stat = null;
		Integer rowCount = null;

		try {
			stat = conn.createStatement();
			rowCount = stat.executeUpdate(queryString);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		if (rowCount != 1) {
			String gripe = "updateRegistryDoi: failed to update DOI in resource registry.";
			throw new DOIException(gripe);
		}

	}


	/**
	 * Delete all resources associated with a data package based on
	 * the specified scope and identifier values.
	 * 
	 * @param scope
	 * @param identifier
	 * @return  true if successfully deleted, else false
	 */
	public boolean deleteDataPackage(String scope, Integer identifier) 
	        throws ClassNotFoundException, SQLException, Exception {
    boolean deleted = false;
    
    Calendar calendar = Calendar.getInstance();
    Timestamp timestamp = new java.sql.Timestamp(calendar.getTimeInMillis());
    String updateSQL = "UPDATE " + RESOURCE_REGISTRY + 
                       " SET date_deactivated=? " +
                       " WHERE scope=? AND identifier=?";
  
    Connection conn = getConnection();
  
    if (conn != null) {
      logger.debug("updateSQL: " + updateSQL);
      
      try {
        PreparedStatement pstmt = conn.prepareStatement(updateSQL);
        pstmt.setTimestamp(1, timestamp);
        pstmt.setString(2, scope);             // Set WHERE scope value
        pstmt.setInt(3, identifier);           // Set WHERE identifier value
        int nRecords = pstmt.executeUpdate();
        pstmt.close();
        
        // Should update one or more records in the resource registry
        if (nRecords >= 1) { 
          logger.debug("deleteDataPackage() updated " + nRecords + " records.");
          deleted = true; 
        }   
      }
      catch(SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        returnConnection(conn);
      }
    }
    else {
      String message = "deleteDataPackage() failed due to connection error.";
      Exception e = new Exception(message);
      throw(e);
    }
    
    return deleted;
	}

	
	/**
	 * 
	 * @param scope
	 * @param identifier
	 * @param revision
	 */
	public boolean deleteDataPackage(String scope, String identifier, String revision){
		return false;   // Not yet supported
	}
	
	
	/**
	 * Delete provenance records for a data package.
	 * 
	 * @param  packageId  the data package identifier, e.g. "knb-lter-nin.1.1"
	 * @return true if successfully deleted, else false
	 */
	public boolean deleteProvenanceRecords(String packageId) 
			throws ClassNotFoundException, SQLException {
		boolean deleted = false;

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("DELETE FROM " + PROVENANCE + " WHERE derived_id=?");
		String updateSQL = sqlBuilder.toString();

		Connection conn = getConnection();

		if (conn != null) {
			try {
				PreparedStatement pstmt = conn.prepareStatement(updateSQL);
				pstmt.setString(1, packageId); // Set packageId value
				int nRecords = pstmt.executeUpdate();
				pstmt.close();

				// Should update one or more records in the resource registry
				if (nRecords >= 1) {
					logger.debug("deleteDataPackage() updated " + nRecords
							+ " records.");
					deleted = true;
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
			String message = "deleteDataPackage() failed due to connection error.";
			SQLException e = new SQLException(message);
			throw (e);
		}

		return deleted;
	}

	
	/**
	 * Delete the DOI field of the Data Package Manager resource registry
	 * for the resource identified by the DOI.
	 * 
	 * @param doi
	 *          The DOI of the resource
	 * @throws Exception
	 */
	public void deleteResourceDoi(String doi)
	    throws DOIException {

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "UPDATE datapackagemanager.resource_registry"
		    + " SET doi=NULL WHERE doi='" + doi + "';";

		Statement stat = null;
		Integer rowCount = null;

		try {
			stat = conn.createStatement();
			rowCount = stat.executeUpdate(queryString);
		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		if (rowCount != 1) {
			String gripe = "obsoleteRegistryDoi: failed to obsolete DOI in resource registry.";
			throw new DOIException(gripe);
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
   * Gets a list of access control records from the access_matrix table
   * matching a given resource identifier and returns them as a list of 
   * Rule objects.
   * 
   * @param resourceId     the resource identifier value
   * @return               a list of matching Rule objects suitable for
   *                       constructing an AccessMatrix object
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public ArrayList<Rule> getAccessControlRules(String resourceId)
          throws ClassNotFoundException, SQLException, IllegalArgumentException {
    ArrayList<Rule> ruleList = new ArrayList<Rule>();
    
    if (resourceId != null && !resourceId.equals("")) {
      Connection connection = null;
      
      String selectString = 
        "SELECT principal, access_type, access_order, permission FROM " + ACCESS_MATRIX +
        "  WHERE resource_id='" + resourceId + "'";
      
      Statement stmt = null;
      
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectString);
        
        while (rs.next()) {
          String principal = rs.getString(1);
          String access_type = rs.getString(2);
          String access_order = rs.getString(3);
          String permissionStr = rs.getString(4);
          Rule.Permission permission = Rule.Permission.valueOf(permissionStr);
          Rule rule = new Rule();
          rule.setPrincipal(principal);
          rule.setAccessType(access_type);
          rule.setOrder(access_order);
          rule.setPermission(permission);
          ruleList.add(rule);
        }
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
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
    }
    
    return ruleList;
  }
  

	/**
	 * Returns a list of data package resource identifiers for the specified
	 * data package. 
	 * 
	 * @param scope      the scope, e.g. "knb-lter-lno"
	 * @param identifier the identifier integer value, e.g. 2
	 * @param revision   the revision value, e.g. 1
	 */
	public ArrayList<String> getDataPackageResources(String scope, Integer identifier, Integer revision)
          throws ClassNotFoundException, SQLException, IllegalArgumentException {
    ArrayList<String> resources = new ArrayList<String>();
    
    if (scope != null && identifier != null && revision != null) {
      Connection connection = null;
      String selectString = 
        "SELECT resource_id FROM " + RESOURCE_REGISTRY +
        "  WHERE scope='" + scope + 
        "' AND identifier='" + identifier + 
        "' AND revision='" + revision + "'" +
        "  ORDER BY date_created";
    
      Statement stmt = null;
    
      try {
        connection = getConnection();
        stmt = connection.createStatement();             
        ResultSet rs = stmt.executeQuery(selectString);
      
        while (rs.next()) {
          String resourceId = rs.getString("resource_id");
          if (!isEvaluateResource(resourceId)) {
            resources.add(resourceId);
          }
        }
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
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
    }
    else {
      String message = "One or more of the scope, identifier, or revision values is null";
      throw new IllegalArgumentException(message);
    }
    
    return resources;
  }
	
	
	/*
	 * Boolean to determine if this is an evaluate resource based on its
	 * resourceId value.
	 */
	private boolean isEvaluateResource(String resourceId) {
	  boolean isEvaluateResource = false;
	  
	  if (resourceId != null && resourceId.contains("/evaluate/")) {
	    isEvaluateResource = true;
	  }
	  
	  return isEvaluateResource;
	}

	
  /**
   * Gets the newest revision value for the current scope and identifier.
   * 
   * @param scope        the metadata document scope
   * @param identifier   the metadata document identifier
   * @return  newest, an Integer value representing the newest revision
   *          value for the specified scope and identifier
   */
  public Integer getNewestRevision(String scope, Integer identifier) 
          throws ClassNotFoundException, IllegalArgumentException, SQLException {
    Integer newest = null;
    
    if (hasDataPackage(scope, identifier)) {

      try {
        Connection connection = null;
        String selectString = "SELECT max(revision) FROM " + RESOURCE_REGISTRY
            + "  WHERE scope='" + scope + "' AND identifier='" + identifier
            + "'";
        logger.debug("selectString: " + selectString);

        Statement stmt = null;

        try {
          connection = getConnection();
          stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(selectString);

          while (rs.next()) {
            int maxRevision = rs.getInt(1);
            newest = new Integer(maxRevision);
          }

          if (stmt != null)
            stmt.close();
        }
        catch (ClassNotFoundException e) {
          logger.error("ClassNotFoundException: " + e.getMessage());
          e.printStackTrace();
          throw (e);
        }
        catch (SQLException e) {
          logger.error("SQLException: " + e.getMessage());
          e.printStackTrace();
          throw (e);
        }
        finally {
          returnConnection(connection);
        }
      }
      catch (NumberFormatException e) {
        logger.error("Non-numeric identifier value: " + identifier);
        e.printStackTrace();
        throw (new IllegalArgumentException(e.getMessage()));
      }
    }
    
    return newest;
  }
  
  
  /**
   * Gets the oldest revision value for the current scope and identifier.
   * 
   * @param scope        the metadata document scope
   * @param identifier   the metadata document identifier
   * @return  oldest, an Integer value representing the oldest revision
   *          for the specified scope and identifier
   */
  public Integer getOldestRevision(String scope, Integer identifier)
          throws ClassNotFoundException, IllegalArgumentException, SQLException {

    Integer oldest = null;
    
    if (hasDataPackage(scope, identifier)) {

      try {
        Integer identifierInt = new Integer(identifier);

        Connection connection = null;
        String selectString = "SELECT min(revision) FROM " + RESOURCE_REGISTRY
            + "  WHERE scope='" + scope + "' AND identifier='" + identifierInt
            + "'";
        logger.debug("selectString: " + selectString);

        Statement stmt = null;

        try {
          connection = getConnection();
          stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(selectString);

          while (rs.next()) {
            int minRevision = rs.getInt(1);
            oldest = new Integer(minRevision);
          }

          if (stmt != null)
            stmt.close();
        }
        catch (SQLException e) {
          logger.error("SQLException: " + e.getMessage());
          e.printStackTrace();
          throw (e);
        }
        finally {
          returnConnection(connection);
        }
      }
      catch (NumberFormatException e) {
        logger.error("Non-numeric identifier value: " + identifier);
        e.printStackTrace();
        throw (new IllegalArgumentException(e.getMessage()));
      }
    }
    
    return oldest;
  }
  
  /**
   * Gets the doi value for a given resourceId
   * 
   * @param resourceId   the resource identifier
   * @return  the value of the 'doi' field matching
   *          the specified resourceId ('resource_id') value
   */
  public String getDoi(String resourceId) 
          throws ClassNotFoundException, SQLException {
    
  	String doi = null;
    
    Connection connection = null;
    String selectString = 
            "SELECT doi FROM " + RESOURCE_REGISTRY +
            "  WHERE resource_id='" + resourceId + "'";
    logger.debug("selectString: " + selectString);

    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        doi = rs.getString(1);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    return doi;
    
  }


	/**
	 * Gets the format type value for a given resourceId.
	 * 
	 * @param resourceId
	 *            the resource identifier
	 * @return the value of the 'format_type' field matching the specified
	 *         resourceId ('resource_id') value
	 */
	public String getFormatType(String resourceId)
			throws ClassNotFoundException, SQLException {

		String formatType = null;

		Connection connection = null;
		String selectString = "SELECT format_type FROM " + RESOURCE_REGISTRY
				+ "  WHERE resource_id='" + resourceId + "'";

		Statement stmt = null;

		try {
			connection = getConnection();
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(selectString);

			while (rs.next()) {
				formatType = rs.getString(1);
				if (formatType != null) { formatType = formatType.trim(); }
			}

			if (stmt != null)
				stmt.close();
		}
		catch (ClassNotFoundException e) {
			logger.error("ClassNotFoundException: " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
		catch (SQLException e) {
			logger.error("SQLException: " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
		finally {
			returnConnection(connection);
		}

		return formatType;
	}


  /**
   * Gets the entityName value for a given data entity resource identifier
   * 
   * @param resourceId   the resource identifier
   * @return  the value of the database table's 'entity_name' field matching
   *          the specified resourceId ('resource_id') value
   */
  public String getDataEntityName(String resourceId) 
          throws ClassNotFoundException, SQLException {
    
    String entityName = null;
    
    Connection connection = null;
    String selectString = 
            "SELECT entity_name FROM " + RESOURCE_REGISTRY +
            "  WHERE resource_id='" + resourceId + "'";
    logger.debug("selectString: " + selectString);

    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        entityName = rs.getString(1);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    return entityName;
    
  }


  /**
   * Gets the principalOwner value for a given resourceId
   * 
   * @param resourceId   the resource identifier
   * @return  the value of the 'principal_owner' field matching
   *          the specified resourceId ('resource_id') value
   */
  public String getPrincipalOwner(String resourceId) 
          throws ClassNotFoundException, SQLException {
    String principalOwner = null;
    
    Connection connection = null;
    String selectString = 
            "SELECT principal_owner FROM " + RESOURCE_REGISTRY +
            "  WHERE resource_id='" + resourceId + "'";
    logger.debug("selectString: " + selectString);

    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        principalOwner = rs.getString(1);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    return principalOwner;
  }
  
  
  /**
   * Composes an access control list (ACL) XML string for a given resourceId
   * 
   * @param   resourceId   the resource identifier
   * @return  An XML string representing the 'access_matrix' table entries
   *          matching the specified resourceId. An additional 'allow'
   *          entry is included for the resource's principal owner
   *          as stored in the 'resource_registry' table.
   */
  public String getResourceAcl(String resourceId) 
          throws ClassNotFoundException, SQLException {
	boolean isOwner = true;
	String principalOwner = null;
    String principal = null;
    String access_type = null;
    String access_order = null;
    String permission = null;
    boolean allowFirst = true;
      StringBuffer accessXmlBuffer =
          new StringBuffer("<access:access " +
                               "xmlns:access=\"eml://ecoinformatics" +
                               ".org/access-" + EML_VERSION + "\" " +
                               "authSystem=\"https://pasta.lternet" +
                               ".edu/authentication\" " +
                               "order=\"allowFirst\" " +
                               "system=\"https://pasta.lternet.edu\">\n"
          );

    /* First compose an 'allow' entry for the resource owner/submitter */
    Connection connection = null;
    Statement stmt = null;
    String selectString = 
            "SELECT principal_owner FROM " + RESOURCE_REGISTRY +
            "  WHERE resource_id='" + resourceId + "'";

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
    	  principalOwner = rs.getString(1);
    	  String element = composeAllowOrDenyElement(principalOwner, "allow", "changePermission", isOwner);
    	  accessXmlBuffer.append(element);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    /* Then compose 'allow' and/or 'deny' entries from the access_matrix table */
    isOwner = false;
    selectString = 
            "SELECT principal, access_type, access_order, permission FROM " + ACCESS_MATRIX +
            "  WHERE resource_id='" + resourceId + "'";

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
    	  principal = rs.getString(1);
    	  access_type = rs.getString(2);
    	  access_order = rs.getString(3);
    	  permission = rs.getString(4);
    	  if (access_order.equals("denyFirst")) { allowFirst = false; }
    	  String element = composeAllowOrDenyElement(principal, access_type, permission, isOwner);
    	  accessXmlBuffer.append(element);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    accessXmlBuffer.append("</access:access>");
    String accessXML = accessXmlBuffer.toString();
    if (!allowFirst) accessXML.replace("allowFirst", "denyFirst");
    return accessXML;
  }
  
  
  /*
   * Composes an 'allow' or 'deny' XML element.
   */
  private String composeAllowOrDenyElement(String principal, String accessType, String permission, boolean isOwner) {
	  String element = null;
	  StringBuffer elementBuffer = new StringBuffer("");
	  elementBuffer.append(String.format("  <%s>\n", accessType));
	  elementBuffer.append(String.format("    <principal>%s</principal>\n", principal));
	  elementBuffer.append(String.format("    <permission>%s</permission>\n", permission));
	  elementBuffer.append(String.format("  </%s>\n", accessType));
	  element = elementBuffer.toString();	  
	  return element;
  }
  
  
  /**
   * Gets the SHA checksum value for a given resourceId
   * 
   * @param   resourceId   the resource identifier
   * @return  the value of the 'sha1_checksum' field matching
   *          the specified resourceId ('resource_id') value
   */
  public String getResourceShaChecksum(String resourceId) 
          throws ClassNotFoundException, SQLException {
    String checksum = null;
    
    Connection connection = null;
    String selectString = 
            "SELECT sha1_checksum FROM " + RESOURCE_REGISTRY +
            "  WHERE resource_id='" + resourceId + "'";
    logger.debug("selectString: " + selectString);

    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        checksum = rs.getString(1);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    return checksum;
  }
  
  
	/**
	 * Gets a list of recent uploads, either inserts or updates.
	 * 
	 * @param serviceMethod  one of "createDataPackage" or "updateDataPackage"
	 * @param fromTime       the cut-off date for how far back we want to query, e.g. '2015-01-01'
	 * @param limit          a limit on the number of uploads to return, e.g. 2
	 */
	public ArrayList<DataPackageUpload> getUploads(String serviceMethod, String fromTime, Integer limit) 
			throws Exception {
		Connection conn = null;
		ArrayList<DataPackageUpload> uploadsList = new ArrayList<DataPackageUpload>();
		StringBuilder sb = new StringBuilder();
		TreeSet<String> docids = new TreeSet<String>();
		
		sb.append("SELECT scope, identifier, revision, date_created FROM ");
		sb.append(RESOURCE_REGISTRY);
		sb.append(" WHERE resource_type='dataPackage' AND ");
		sb.append("   date_deactivated IS NULL AND ");
		sb.append("   date_created > '" + fromTime + "'\n");
		sb.append("ORDER BY date_created DESC;");
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
					java.sql.Date dateCreated = rs.getDate(4);
					String uploadDate = dateCreated.toString();
					
					ArrayList<String> revisions = listDataPackageRevisions(scope, identifier);
					if (revisions != null) {
						if ((revisions.size() == 1) && (serviceMethod.equals("createDataPackage"))) {
							DataPackageUpload upload = 
								new DataPackageUpload(uploadDate, serviceMethod, scope, identifier, revision);
							uploadsList.add(upload);
						}
						else if ((revisions.size() > 1) && (serviceMethod.equals("updateDataPackage"))) {
							String docid = String.format("%s.%d", scope, identifier);
							if (!docids.contains(docid)) {
								DataPackageUpload upload = 
									new DataPackageUpload(uploadDate, serviceMethod, scope, identifier, revision);
								uploadsList.add(upload);
								docids.add(docid);
							}
						}
					}
					if (uploadsList.size() >= limit) {
						break;
					}
				}
			}
		}
		finally {
			returnConnection(conn);
		}

		return uploadsList;
	}

  
  
  /**
   * Gets the resourceLocation value for a given resourceId
   * 
   * @param resourceId   the resource identifier
   * @return  the value of the 'resource_location' field matching
   *          the specified resourceId ('resource_id') value
   */
  public String getResourceLocation(String resourceId) 
          throws ClassNotFoundException, SQLException {
    String resourceLocation = null;
    
    Connection connection = null;
    String selectString = 
            "SELECT resource_location FROM " + RESOURCE_REGISTRY +
            "  WHERE resource_id='" + resourceId + "'";
    logger.debug("selectString: " + selectString);

    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        resourceLocation = rs.getString(1);
      }

      if (stmt != null) stmt.close();
    }
    catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      e.printStackTrace();
      throw (e);
    }
    finally {
      returnConnection(connection);
    }
    
    return resourceLocation;
  }
  
  
  /**
   * Boolean to determine whether the specified data package is present in the
   * Data Package Registry based on a specified scope and identifier.
   * 
   * @param scope        the scope value, e.g. "knb-lter-lno"
   * @param identifier   the identifier value, e.g. "1"
   */
  public boolean hasDataPackage(String scope, Integer identifier)
          throws ClassNotFoundException, SQLException {
    boolean hasDataPackage = false;
    Connection connection = null;
    String selectString = 
      "SELECT count(*) FROM " + RESOURCE_REGISTRY +
      "  WHERE scope='" + scope + "' AND " +
      "        identifier='" + identifier + "' AND " +
      "        resource_type != 'report'";
  
    Statement stmt = null;
  
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(selectString);
    
      while (rs.next()) {
        int count = rs.getInt("count");
        hasDataPackage = (count > 0);
      }

      if (stmt != null) stmt.close();
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
    
    return hasDataPackage;
  }
  

	/**
	 * Boolean to determine whether the specified data package is present in the
	 * Data Package Registry  based on a specified scope, identifier, and
	 * revision.
	 * 
	 * @param scope        the scope value, e.g. "knb-lter-lno"
	 * @param identifier   the identifier value, e.g. "1"
	 * @param revision     the revision value, e.g. "2"
	 */
	public boolean hasDataPackage(String scope, Integer identifier, String revision)
          throws ClassNotFoundException, SQLException {
    boolean hasDataPackage = false;
    Connection connection = null;
    String selectString = 
      "SELECT count(*) FROM " + RESOURCE_REGISTRY +
      "  WHERE scope='" + scope + "' AND " +
      "        identifier='" + identifier + "' AND " +
      "        revision='" + revision + "'";
  
    Statement stmt = null;
  
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(selectString);
    
      while (rs.next()) {
        int count = rs.getInt("count");
        hasDataPackage = (count > 0);
      }

      if (stmt != null) stmt.close();
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
    
    return hasDataPackage;
  }
  

  /**
   * Boolean to determine whether the specified resource is in the
   * Data Package Registry based on a specified resourceId value
   * 
   * @param resourceId   the resource identifier
   * @return  true if the resource is in the registry, else false
   */
  public boolean hasResource(String resourceId)
          throws ClassNotFoundException, SQLException {
    boolean hasReource = false;
    Connection connection = null;
    String selectString = 
      "SELECT count(*) FROM " + RESOURCE_REGISTRY +
      "  WHERE resource_id='" + resourceId + "'";
  
    Statement stmt = null;
  
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(selectString);
    
      while (rs.next()) {
        int count = rs.getInt("count");
        hasReource = (count > 0);
      }

      if (stmt != null) stmt.close();
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
    
    return hasReource;
  }
  
  
    /**
     * Inserts a provenance record into the provenance table of the
     * data package registry.
     * 
     * @param derivedId
     * @param sourceId
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws ProvenanceException
     */
	public void insertProvenance(String derivedId, String sourceId) 
			throws ClassNotFoundException, SQLException, ProvenanceException {
		Connection connection = null;
		
		if (derivedId == null) {
			throw new ProvenanceException("Provenance error: null package id for derived data package");
		}
		else if (sourceId == null) {
			throw new ProvenanceException("Provenance error: null package id for source data package");
		}
		
		/*
		 * Check whether the documented source data package exists in
		 * PASTA. Flag an error if it doesn't.
		 */
		EmlPackageIdFormat epif = new EmlPackageIdFormat();
		EmlPackageId epi = epif.parse(sourceId);
		String sourceResourceId = 
				DataPackageManager.composeResourceId(ResourceType.dataPackage, 
						                             epi.getScope(),
						                             epi.getIdentifier(), 
						                             epi.getRevision(), 
						                             null);
		if (!hasResource(sourceResourceId)) {
			throw new ProvenanceException(
					String.format("The derived data package %s documents a dependency "
							    + "on a non-existent source data package %s",
							      derivedId, sourceId));
		}

		/*
		 * Since the combination of derivedId and sourceId is a primary key 
		 * and must be unique, delete any existing provenance records for this 
		 * pair of values.
		 */
		String deleteString = "DELETE FROM "  + PROVENANCE + "  WHERE derived_id=? AND source_id=?";
		logger.debug("deleteString: " + deleteString);

		try {
			connection = getConnection();
			PreparedStatement pstmt = connection.prepareStatement(deleteString);
			pstmt.setString(1, derivedId);
			pstmt.setString(2, sourceId);
			pstmt.executeUpdate();
			if (pstmt != null) { pstmt.close(); }
		}
		catch (SQLException e) {
			logger.error(
					String.format("Error deleting provenance record for derived id %s " +
			                      "and source id %s", derivedId, sourceId));
			logger.error("SQLException: " + e.getMessage());
			throw(e);
		}
		finally {
			returnConnection(connection);
		}

		StringBuffer insertSQL = new StringBuffer("INSERT INTO " + PROVENANCE + "(");
		insertSQL.append("derived_id, source_id) VALUES(?,?)");      
		String insertString = insertSQL.toString();
		logger.debug("insertString: " + insertString);

		try {
			connection = getConnection();
			PreparedStatement pstmt = connection.prepareStatement(insertString);
			pstmt.setString(1, derivedId);
			pstmt.setString(2, sourceId);
			pstmt.executeUpdate();
			if (pstmt != null) {
				pstmt.close();
			}
		}
		catch (SQLException e) {
			logger.error(
					String.format("Error inserting provenance record for derived id %s " +
			                      "and source id %s", derivedId, sourceId));
			logger.error("SQLException: " + e.getMessage());
			throw(e);
		}
		finally {
			returnConnection(connection);
		}
	}


  /**
   * Boolean to determine whether the Data Package Registry
   * table already exists. If it isn't present, it will need to
   * be created.
   * 
   * @return  isPresent, true if the data cache table is present, else false
   */
  private boolean isDataPackageRegistryPresent() 
          throws ClassNotFoundException, SQLException {          
    boolean isPresent = false;
    String catalog = null;          // A catalog name (may be null)
    Connection connection = null;
    DatabaseMetaData databaseMetaData = null; // For getting db metadata
    ResultSet rs;
    String schemaPattern = DATA_PACKAGE_MANAGER_SCHEMA; // A schema name pattern
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
 
          if (TABLE_NAME.equalsIgnoreCase(RESOURCE_REGISTRY_TABLE)) {
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
   * Boolean to determine whether the specified data package is present in the
   * Data Package Registry but was previously de-activated.
   * 
   * @param scope        the scope value, e.g. "knb-lter-lno"
   * @param identifier   the identifier value, e.g. "1"
   */
  public boolean isDeactivatedDataPackage(String scope, Integer identifier)
          throws ClassNotFoundException, SQLException {
    boolean isDeactivated = false;
    Connection connection = null;
    String selectString = 
      "SELECT count(*) FROM " + RESOURCE_REGISTRY +
      "  WHERE scope='" + scope + "' AND " +
      "        identifier='" + identifier + "' AND " +
      "        resource_type='" + ResourceType.dataPackage + "' AND " +
      "        date_deactivated IS NOT NULL";
  
    Statement stmt = null;
  
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(selectString);
    
      while (rs.next()) {
        int count = rs.getInt("count");
        isDeactivated = (count > 0);
      }

      if (stmt != null) stmt.close();
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
    
    return isDeactivated;
  }
  
	/**
	 * Determines whether the given resource is publicly accessible.
	 * 
	 * @param resourceId
	 * @return Is publicly accessible
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public Boolean isPublicAccessible(String resourceId) throws SQLException {

		Boolean publicAccessible = false;

		ArrayList<Rule> ruleList = new ArrayList<Rule>();

		Connection conn = null;

		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, principal, access_type, "
		    + "access_order, permission FROM datapackagemanager.access_matrix WHERE"
		    + " resource_id='" + resourceId + "';";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			while (result.next()) {

				Rule rule = new Rule();

				rule.setPrincipal(result.getString("principal"));
				rule.setAccessType(result.getString("access_type"));
				rule.setOrder(result.getString("access_order"));
				rule.setPermission((Rule.Permission) Enum.valueOf(
				    Rule.Permission.class, result.getString("permission")));

				ruleList.add(rule);

			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		String tokenString = BasicAuthToken.makeTokenString(PUBLIC, PUBLIC);
		AuthToken authToken = AuthTokenFactory
		    .makeAuthTokenWithPassword(tokenString);

		AccessMatrix accessMatrix = new AccessMatrix(ruleList);
		Rule.Permission permission = (Rule.Permission) Enum.valueOf(
		    Rule.Permission.class, Rule.READ);
		publicAccessible = accessMatrix.isAuthorized(authToken, null, permission);

		return publicAccessible;

	}

  
	  /**
	   * Lists all active (undeleted) data packages.
	   * 
	   * @return A list of document id strings corresponding to the list of
	   *         active (undeleted) data packages, where a document id is the packageId
	   *         minus the revision value.
	   * @throws ClassNotFoundException
	   * @throws SQLException
	   * @throws IllegalArgumentException
	   */
	  public ArrayList<String> listActiveDataPackages()
	      throws ClassNotFoundException, SQLException, IllegalArgumentException {
	    ArrayList<String> docidList = new ArrayList<String>();

	    Connection connection = null;
	    String selectString = "SELECT DISTINCT scope, identifier FROM " + RESOURCE_REGISTRY +
	        "  WHERE resource_type='dataPackage'" +
	        "  AND date_deactivated IS NULL" +
	        "  ORDER BY scope, identifier";
	    Statement stmt = null;

	    try {
	      connection = getConnection();
	      stmt = connection.createStatement();
	      ResultSet rs = stmt.executeQuery(selectString);

	      while (rs.next()) {
	        String scope = rs.getString("scope");
	        int identifier = rs.getInt("identifier");
	        String docid = scope + "." + identifier;
	        docidList.add(docid);
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
	      if (stmt != null) stmt.close();
	      returnConnection(connection);
	    }

	    return docidList;
	  }

	  
	  /**
	   * Lists all data package revisions known to PASTA, including active and deleted.
	   * 
	   * @return A list of packageId strings corresponding to the list of
	   *         data packages.
	   *        
	   * @throws ClassNotFoundException
	   * @throws SQLException
	   * @throws IllegalArgumentException
	   */
	  public ArrayList<String> listAllDataPackageRevisions()
	      throws ClassNotFoundException, SQLException, IllegalArgumentException {
	    ArrayList<String> packageIdList = new ArrayList<String>();

	    Connection connection = null;
	    String selectString = 
	    	"SELECT DISTINCT scope, identifier, revision" +
	        "  FROM " + RESOURCE_REGISTRY +
	        "  WHERE resource_type='dataPackage'" +
	        "  ORDER BY scope, identifier, revision";
	    Statement stmt = null;

	    try {
	      connection = getConnection();
	      stmt = connection.createStatement();
	      ResultSet rs = stmt.executeQuery(selectString);

	      while (rs.next()) {
	        String scope = rs.getString("scope");
	        int identifier = rs.getInt("identifier");
	        int revision = rs.getInt("revision");
	        String packageId = scope + "." + identifier + "." + revision;
	        packageIdList.add(packageId);
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
	      if (stmt != null) stmt.close();
	      returnConnection(connection);
	    }

	    return packageIdList;
	  }

	  
	  /**
	   * Lists all data entities for a given data package.
	   * 
	   * @param scope        the data package scope value
	   * @param identifier   the data package identifier value
	   * @param revison      the data package revision value
	   * @return A list of entity id strings corresponding to the list of
	   *         data entities for the specified data package.
	   * @throws ClassNotFoundException
	   * @throws SQLException
	   * @throws IllegalArgumentException
	   */
  public ArrayList<String> listDataEntities(String scope, Integer identifier, Integer revision)
    throws ClassNotFoundException, SQLException, IllegalArgumentException {
      ArrayList<String> entityList = new ArrayList<String>();
      
      if (scope != null && identifier != null && revision != null) {
        Connection connection = null;
        String selectString = 
          "SELECT entity_id FROM " + RESOURCE_REGISTRY +
          "  WHERE scope='" + scope + 
          "' AND identifier='" + identifier + 
          "' AND revision='" + revision +
          "' AND entity_id IS NOT NULL" +
          "  ORDER BY date_created";
      
        Statement stmt = null;
      
        try {
          connection = getConnection();
          stmt = connection.createStatement();             
          ResultSet rs = stmt.executeQuery(selectString);
        
          while (rs.next()) {
            String entityId = rs.getString("entity_id");
            entityList.add(entityId);
          }
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
          if (stmt != null) stmt.close();
          returnConnection(connection);
        }
      }
      else {
        String message = "One or more of the scope, identifier, or revision values is null";
        throw new IllegalArgumentException(message);
      }
      
      return entityList;
  }

  
	/**
	 * 
	 * @param scope
	 */
	public ArrayList<String> listDataPackageIdentifiers(String scope)
        throws ClassNotFoundException, SQLException, IllegalArgumentException {
      ArrayList<String> identifierList = new ArrayList<String>();
      
      if (scope != null) {
        Connection connection = null;
        String selectString = 
          "SELECT DISTINCT identifier FROM " + RESOURCE_REGISTRY +
          "  WHERE resource_type='dataPackage'" +
          "  AND scope='" + scope + "'" +
          "  AND date_deactivated IS NULL" +
          "  ORDER BY identifier";
        Statement stmt = null;
      
        try {
          connection = getConnection();
          stmt = connection.createStatement();             
          ResultSet rs = stmt.executeQuery(selectString);
        
          while (rs.next()) {
            String identifier = rs.getString("identifier");
            identifierList.add(identifier);
          }
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
          if (stmt != null) stmt.close();
          returnConnection(connection);
        }
      }
      else {
        String message = "Scope value is null";
        throw new IllegalArgumentException(message);
      }
      
      return identifierList;
    }

	
	/**
	 * 
	 * @param scope
	 * @param identifier
	 */
	public ArrayList<String> listDataPackageRevisions(String scope, Integer identifier)
          throws ClassNotFoundException, SQLException, IllegalArgumentException {
    ArrayList<String> revisionList = new ArrayList<String>();
    
    if (scope != null && identifier != null) {
      Connection connection = null;
      String selectString = 
        "SELECT revision FROM " + RESOURCE_REGISTRY +
        "  WHERE resource_type='dataPackage'" +
        "  AND scope='" + scope + 
        "' AND identifier='" + identifier + "'" +
        "  AND date_deactivated IS NULL" +
        "  ORDER BY revision";
      Statement stmt = null;
    
      try {
        connection = getConnection();
        stmt = connection.createStatement();             
        ResultSet rs = stmt.executeQuery(selectString);
      
        while (rs.next()) {
          String revision = rs.getString("revision");
          revisionList.add(revision);
        }
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
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
    }
    else {
      String message = "One or more of the scope or identifier values is null";
      throw new IllegalArgumentException(message);
    }
    
    return revisionList;
  }

	
	/**
	 * Lists all prior revisions for a data package.
	 *  
	 * @param emlPackageId   the EML package identifier object
	 * @return  an array list of integer values in ascending order representing
	 *          all known prior revisions to the current data package revision
	 */
	public ArrayList<Integer> listPriorRevisions(EmlPackageId emlPackageId)
          throws ClassNotFoundException, SQLException, IllegalArgumentException {
    ArrayList<Integer> priorRevisionList = new ArrayList<Integer>();
    String scope = emlPackageId.getScope();
    Integer identifier = emlPackageId.getIdentifier();
    Integer revision = emlPackageId.getRevision();
    boolean foundThisRevision = false;
    
    if (scope != null && 
    	identifier != null && 
    	revision != null
       ) {
      Connection connection = null;
      String selectString = 
        "SELECT revision FROM " + RESOURCE_REGISTRY +
        "  WHERE resource_type='dataPackage'" +
        "  AND scope='" + scope + 
        "' AND identifier='" + identifier + "'" +
        "  ORDER BY revision";
      Statement stmt = null;
    
      try {
        connection = getConnection();
        stmt = connection.createStatement();             
        ResultSet rs = stmt.executeQuery(selectString);
      
        while (rs.next()) {
          int rev = rs.getInt("revision");
          // test for prior revision
          if (rev < revision) {
          	priorRevisionList.add(new Integer(rev));
          }
          else if (rev == revision) {
        	  // We found this revision, so it's okay to return the prior revisions
        	  foundThisRevision = true;
          }
        }
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
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
    }
    else {
      String message = "One or more of the scope or identifier values is null";
      throw new IllegalArgumentException(message);
    }
    
    ArrayList<Integer> revisionList = null;
    // Unless we found this revision, we'll return null
    if (foundThisRevision) {
    	revisionList = priorRevisionList;
    }
    
    return revisionList;
  }

	
  public ArrayList<String> listDataPackageScopes()
      throws ClassNotFoundException, SQLException, IllegalArgumentException {
    ArrayList<String> scopeList = new ArrayList<String>();

    Connection connection = null;
    String selectString = "SELECT DISTINCT scope FROM " + RESOURCE_REGISTRY +
        "  WHERE resource_type='dataPackage'" +
        "    AND date_deactivated IS NULL" +
        "  ORDER BY scope";
    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        String scope = rs.getString("scope");
        scopeList.add(scope);
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
      if (stmt != null) stmt.close();
      returnConnection(connection);
    }

    return scopeList;
  }


  /**
   * Lists all data packages that have been deleted from the resource
   * registry.
   * 
   * @return A list of document id strings corresponding to the list of
   *         deleted data packages, where a document id is the packageId
   *         minus the revision value
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public ArrayList<String> listDeletedDataPackages()
      throws ClassNotFoundException, SQLException, IllegalArgumentException {
    ArrayList<String> docidList = new ArrayList<String>();

    Connection connection = null;
    String selectString = "SELECT DISTINCT scope, identifier FROM " + RESOURCE_REGISTRY +
        "  WHERE resource_type='dataPackage'" +
        "  AND date_deactivated IS NOT NULL" +
        "  ORDER BY scope, identifier";
    Statement stmt = null;

    try {
      connection = getConnection();
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(selectString);

      while (rs.next()) {
        String scope = rs.getString("scope");
        int identifier = rs.getInt("identifier");
        String docid = scope + "." + identifier;
        docidList.add(docid);
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
      if (stmt != null) stmt.close();
      returnConnection(connection);
    }

    return docidList;
  }

  
  
	/**
	 * Returns an array list of resources that can be assigned a checksum.
	 * 
	 * @return Array list of resources
	 * @throws SQLException
	 */
	public ArrayList<Resource> listChecksumableResources() throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, resource_type, resource_location, scope, identifier, revision, entity_id, sha1_checksum"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " resource_type != 'dataPackage' AND date_deactivated IS NULL;";

		Statement statement = null;
		try {
			statement = conn.createStatement();
			ResultSet result = statement.executeQuery(queryString);

			while (result.next()) {
				Resource resource = new Resource();
				String resourceId = result.getString("resource_id");
				String resourceType = result.getString("resource_type");
				String resourceLocation = result.getString("resource_location");
				String scope = result.getString("scope");
				Integer identifier = new Integer(result.getInt("identifier"));
				Integer revision = new Integer(result.getInt("revision"));
				String packageId = scope + "." + identifier + "." + revision;
				String entityId = result.getString("entity_id");
				String sha1Checksum = result.getString("sha1_checksum");
				resource.setResourceId(resourceId);
				resource.setResourceType(resourceType);
				resource.setResourceLocation(resourceLocation);
				resource.setScope(scope);
				resource.setIdentifier(identifier);
				resource.setRevision(revision);
				resource.setEntityId(entityId);
				resource.setPackageId(packageId);
				resource.setSha1Checksum(sha1Checksum);
				resourceList.add(resource);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return resourceList;

	}

	
	/**
	 * Returns an array list of resources that are lacking checksums in
	 * the resource registry.
	 * 
	 * @return Array list of resources
	 * @throws SQLException
	 */
	public ArrayList<Resource> listChecksumlessResources() throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, resource_type, scope, identifier, revision, entity_id"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " resource_type != 'dataPackage' AND date_deactivated IS NULL AND sha1_checksum IS NULL;";

		Statement stat = null;
		try {
			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			while (result.next()) {
				Resource resource = new Resource();
				String resourceId = result.getString("resource_id");
				String resourceType = result.getString("resource_type");
				String scope = result.getString("scope");
				Integer identifier = new Integer(result.getInt("identifier"));
				Integer revision = new Integer(result.getInt("revision"));
				String packageId = scope + "." + identifier + "." + revision;
				String entityId = result.getString("entity_id");
				resource.setResourceId(resourceId);
				resource.setResourceType(resourceType);
				resource.setScope(scope);
				resource.setIdentifier(identifier);
				resource.setRevision(revision);
				resource.setPackageId(packageId);
				resource.setEntityId(entityId);
				resourceList.add(resource);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return resourceList;

	}

	
	/**
	 * Returns an array list of resources that are lacking format_type
	 * values in the resource registry.
	 * 
	 * @return Array list of resources
	 * @throws SQLException
	 */
	public ArrayList<Resource> listFormatlessResources() throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, resource_type, scope, identifier, revision, entity_id"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " resource_type != 'dataPackage' AND format_type IS NULL;";

		Statement stat = null;
		try {
			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			while (result.next()) {
				Resource resource = new Resource();
				String resourceId = result.getString("resource_id");
				String resourceType = result.getString("resource_type");
				String scope = result.getString("scope");
				Integer identifier = new Integer(result.getInt("identifier"));
				Integer revision = new Integer(result.getInt("revision"));
				String packageId = scope + "." + identifier + "." + revision;
				String entityId = result.getString("entity_id");
				resource.setResourceId(resourceId);
				resource.setResourceType(resourceType);
				resource.setScope(scope);
				resource.setIdentifier(identifier);
				resource.setRevision(revision);
				resource.setPackageId(packageId);
				resource.setEntityId(entityId);
				resourceList.add(resource);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return resourceList;

	}

	
	/**
	 * Returns an array list of resources that are both publicly accessible and
	 * lacking DOIs.
	 * 
	 * @return Array list of resources
	 * @throws SQLException
	 */
	public ArrayList<Resource> listDoilessResources() throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT resource_id, resource_type, scope, identifier, revision, date_created"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " resource_type='dataPackage' AND doi IS NULL AND date_deactivated IS NULL;";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);
			String resourceId = null;

			while (result.next()) {

				Resource resource = new Resource();

				// Test here for resource public accessibility before adding to list

				resourceId = result.getString("resource_id");

				if (this.isPublicAccessible(resourceId)) {

					String packageId = result.getString("scope") + "."
							+ result.getInt("identifier") + "."
							+ result.getInt("revision");
					
					resource.setResourceId(resourceId);
					resource.setResourceType(result.getString("resource_type"));
					resource.setDateCreate(result.getString("date_created"));
					resource.setPackageId(packageId);

					resourceList.add(resource);

				}

			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return resourceList;

	}

	
	/**
	 * Returns an array list of resources that are accessible for a 
	 * particular data package. We expect the list to contain zero or one Resource
	 * objects for a given package id.
	 * 
	 * @param  packageId the data package identifier
	 * @param  publicOnly  include only publicly accessible resources
	 * @return Array list of resources
	 * @throws SQLException
	 */
	public ArrayList<Resource> listDataPackageResources(String packageId, boolean publicOnly) 
				throws SQLException {

		ArrayList<Resource> resourceList = new ArrayList<Resource>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = 
			"SELECT resource_id, resource_type, scope, identifier, revision, " +
		    " date_created, doi, resource_location, entity_id, sha1_checksum " + 
		    "FROM datapackagemanager.resource_registry " +
		    "WHERE package_id='" + packageId + "'";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);
			String resourceId = null;

			while (result.next()) {

				Resource resource = new Resource();

				// Test here for resource public accessibility before adding to list

				resourceId = result.getString("resource_id");

				if (!publicOnly || this.isPublicAccessible(resourceId)) {
					String resourceType = result.getString("resource_type");
					resource.setResourceId(resourceId);
					resource.setResourceType(resourceType);
					resource.setDateCreate(result.getString("date_created"));
					resource.setPackageId(packageId);
					resource.setDoi(result.getString("doi"));
					
					if (resourceType != null && resourceType.equals("data")) {
						resource.setResourceLocation(result.getString("resource_location"));
						resource.setEntityId(result.getString("entity_id"));
						resource.setSha1Checksum(result.getString("sha1_checksum"));
					}

					resourceList.add(resource);

				}

			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return resourceList;

	}

	
	/**
	 * Returns an array list of DOIs that are obsolete.
	 * 
	 * @return Array list of resources
	 * @throws SQLException
	 */
	public ArrayList<String> listObsoleteDois() throws SQLException {

		ArrayList<String> doiList = new ArrayList<String>();

		Connection conn = null;
		try {
			conn = this.getConnection();
		} catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		String queryString = "SELECT doi"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " doi IS NOT NULL and date_deactivated IS NOT NULL;";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			while (result.next()) {
				String doi = result.getString("doi");
				doiList.add(doi);
			}

		} catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			conn.close();
		}

		return doiList;

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

  
  /**
   * Update the format_type field of a resource in the resource registry.
   * 
   * @param resourceId
   *          The resource identifier of the resource to be updated
   * @param formatType
   *          The value to be stored in the format_type field.
   * @throws ClassNotFoundException, IllegalArgumentException, SQLException
   */
	public void updateFormatType(String resourceId, String formatType)
			throws ClassNotFoundException, SQLException {
		Connection conn = null;

		if (formatType == null) {
			throw new IllegalArgumentException("Format type is null");
		}

		try {
			conn = this.getConnection();
		}
		catch (ClassNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		}

		String queryString = String.format(
			"UPDATE datapackagemanager.resource_registry SET format_type='%s' WHERE resource_id='%s'",
			formatType, resourceId);

		try {
			Statement statement = conn.createStatement();
			int rowCount = statement.executeUpdate(queryString);
			if (rowCount != 1) {
				String msg = String.format(
						"When updating format_type, expected 1 row updated, instead %d rows were updated.",
						rowCount);
				throw new SQLException(msg);
			}
		}
		catch (SQLException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw (e);
		}
		finally {
			if (conn != null)
				conn.close();
		}

  }

	
  /**
   * Update the SHA-1 checksum of a resource to the resource registry.
   * 
   * @param resourceId
   *          The resource identifier of the resource to be updated
   * @param sha1Checksum
   *          The SHA-1 checksum of the resource
   * @throws SQLException
   */
  public void updateShaChecksum(String resourceId, String sha1Checksum)
          throws ChecksumException, ClassNotFoundException, SQLException {
    Connection conn = null;
    
    if ((sha1Checksum == null) || (sha1Checksum.length() != 40)) {
      throw new ChecksumException("SHA-1 checksum must be 40 characters in length");
    }
    
    try {
      conn = this.getConnection();
    } 
    catch (ClassNotFoundException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      throw(e);
    }

    String queryString = String.format("UPDATE datapackagemanager.resource_registry "
        + "SET sha1_checksum='%s' WHERE resource_id='%s'", sha1Checksum, resourceId);

    try {
      Statement statement = conn.createStatement();
      int rowCount = statement.executeUpdate(queryString);
      if (rowCount != 1) {
        String msg = String.format("When updating SHA-1 checksum, expected 1 row updated, instead %d row(s) were updated.", rowCount);
        throw new ChecksumException(msg);
      }
    } 
    catch (SQLException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      throw(e);
    } 
    finally {
      conn.close();
    }

  }

}