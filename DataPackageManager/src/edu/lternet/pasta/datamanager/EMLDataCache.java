/**
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
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

package edu.lternet.pasta.datamanager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;


/**
 * EMLDataCache manages and tracks all entities. 
 * It stores information about each entity in the
 * cache in a Data Cache Registry.
 *  
 *   pacakge_id         the package identifier
 *   entity_name        the entity name
 *   entity_id          the entity identifier
 *   date_created       creation date of the entity
 *   update_date        update date of the entity
 *   
 * @author dcosta
 * @created 18-Nov-2010 4:30:02 PM
 *
 */
public class EMLDataCache {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EMLDataCache.class);
  
  // Name of the database table where entities in the data cache are registered
  private final String DATA_CACHE_REGISTRY = "datapackagemanager.DATA_CACHE_REGISTRY";
  private String dbDriver;           // database driver
  private String dbURL;              // database URL
  private String dbUser;             // database user name
  private String dbPassword;         // database user password
  
  
  /*
   * Constructors
   */
  
  /**
   * Constructs a new EMLDataCache object.
   * 
   * @param   dbDriver      the database driver
   * @param   dbURL         the database URL
   * @param   dbUser        the database user name
   * @param   dbPassword    the database user password
   * @return  an EMLDataCache object
   */
  public EMLDataCache(String dbDriver, String dbURL, String dbUser,
                      String dbPassword) 
          throws ClassNotFoundException, SQLException {
    this.dbDriver = dbDriver;
    this.dbURL = dbURL;
    this.dbUser = dbUser;
    this.dbPassword = dbPassword;
  }
  

  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  

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
   * Adds a new table entry for a given entity. By default, the creation
   * date and update date are set to the current date and time.
   * 
   * @param   emlPackageId   the EmlPackageId object for this entity
   * @param   emlEntity      the entity object
   * @return  true if successfully added to the table, else false
   */
  public boolean addDataEntity(EmlPackageId emlPackageId, EMLEntity emlEntity)
          throws ClassNotFoundException, SQLException {
    boolean success = false;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    java.util.Date now = new java.util.Date();
    java.sql.Date sqlDate = new java.sql.Date(now.getTime());
    
    if (emlPackageIdFormat != null) {
      String entityId = emlEntity.getEntityId();
      String entityName = emlEntity.getEntityName();
      String dataFormat = emlEntity.getDataFormat();
      String packageId = emlPackageIdFormat.format(emlPackageId);
        
      if (isEntityRegistered(packageId, entityId)) {
        /* This needs to throw an exception instead of just
         * issue a warning.
         */
        logger.warn("The specified entity is already registered:\n" +
                    "    packageId: " + packageId + "\n" +
                    "    entityId:  " + entityId + "\n");
      }
      else {
        String scope = emlPackageId.getScope();
        Integer identifier = emlPackageId.getIdentifier();
        Integer revision = emlPackageId.getRevision();
        
        /*
         * Insert a new entry for this entity into the data cache registry.
         */
        StringBuffer insertSQL = 
        	      new StringBuffer("INSERT INTO " + DATA_CACHE_REGISTRY + "(");
        insertSQL.append("PACKAGE_ID, SCOPE, IDENTIFIER, REVISION, ENTITY_ID, ENTITY_NAME, DATA_FORMAT, DATE_CREATED, UPDATE_DATE) " + 
        	             "VALUES(?,?,?,?,?,?,?,?,?)");      
        String insertString = insertSQL.toString();
        logger.debug("insertString: " + insertString);

        Connection connection = null;
        try {
          connection = getConnection();
          PreparedStatement pstmt = connection.prepareStatement(insertString);
	      pstmt.setString(1, packageId);
	      pstmt.setString(2, scope);
	      pstmt.setInt(3, identifier);
	      pstmt.setInt(4, revision);
	      pstmt.setString(5, entityId);
	      pstmt.setString(6, entityName);
	      pstmt.setString(7, dataFormat);
	      pstmt.setDate(8, sqlDate);
	      pstmt.setDate(9, sqlDate);
	      pstmt.executeUpdate();
	      if (pstmt != null) {
	        pstmt.close();
	      }
	      success = true;
        }
        catch (SQLException e) {
          logger.error(
            "Error inserting record for " + entityId + 
            " into the data cache registry (" + DATA_CACHE_REGISTRY + ")");      
          logger.error("SQLException: " + e.getMessage());
          throw(e);
        }
        finally {
          returnConnection(connection);
        }
      }
    }
      
    return success;
  }
  
  	
	/**
	 * Deletes all data entity entries from the Data Cache Registry for the
	 * specified package id.
	 * 
   * @param emlPackageId   The packageId whose entities are to be deleted.
   * @return success, true if deleted successfully, else false
	 */
	public boolean deleteDataEntities(EmlPackageId emlPackageId) 
	        throws ClassNotFoundException, SQLException {
    Connection connection = null;
    boolean success = false;
    String deleteString;
    int rowCount = -1;
    Statement stmt = null;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    
    if (emlPackageIdFormat != null) {
      String packageId = emlPackageIdFormat.format(emlPackageId);
    
      deleteString = "DELETE FROM " + DATA_CACHE_REGISTRY + 
                     "  WHERE package_id='" + packageId + "'";
      
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        rowCount = stmt.executeUpdate(deleteString);
        success = (rowCount > 0);
        logger.debug("deleteString: " + deleteString);
        logger.debug("Number of rows deleted: " + rowCount);
        if (stmt != null) stmt.close();
      }
      catch(ClassNotFoundException e) {
        e.printStackTrace();
        throw(e);
      }
      catch(SQLException e) {
        e.printStackTrace();
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
    
      return success;
  }
  
  
  /**
   * Deletes all data entity entries from the Data Cache Registry for the
   * specified scope and identifier
   * 
   * @param scope        the scope value whose entities are to be deleted
   * @param identifier   the identifier value whose entities are to be deleted
   * @return rowCount, the number of data entities deleted
   */
  public int deleteDataEntities(String scope, String identifier) 
          throws ClassNotFoundException, SQLException {
    Connection connection = null;
    String deleteString;
    int rowCount = -1;
    Statement stmt = null;
    
    if (scope != null) {
      deleteString = "DELETE FROM " + DATA_CACHE_REGISTRY + 
                     "  WHERE scope='" + scope + 
                     "' AND identifier='" + identifier + "'";
      
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        rowCount = stmt.executeUpdate(deleteString);
        logger.debug("deleteString: " + deleteString);
        logger.debug("Number of rows deleted: " + rowCount);
        if (stmt != null) stmt.close();
      }
      catch(ClassNotFoundException e) {
        e.printStackTrace();
        throw(e);
      }
      catch(SQLException e) {
        e.printStackTrace();
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
    
    return rowCount;
  }
  
  
  /**
   * Deletes all data entity entries from the Data Cache Registry for the
   * specified scope, identifier, and revision
   * 
   * @param scope        the scope value whose entities are to be deleted
   * @param identifier   the identifier value whose entities are to be deleted
   * @param revision     the revision value whose entities are to be deleted
   * @return rowCount, the number of data entities deleted
   */
  public int deleteDataEntities(String scope, String identifier, String revision) 
          throws ClassNotFoundException, SQLException {
    Connection connection = null;
    String deleteString;
    int rowCount = -1;
    Statement stmt = null;
    
    if (scope != null) {
      deleteString = "DELETE FROM " + DATA_CACHE_REGISTRY + 
                     "  WHERE scope='" + scope + 
                     "' AND identifier='" + identifier +
                     "' AND revision='" + revision + "'" ;
      
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        rowCount = stmt.executeUpdate(deleteString);
        logger.debug("deleteString: " + deleteString);
        logger.debug("Number of rows deleted: " + rowCount);
        if (stmt != null) stmt.close();
      }
      catch(ClassNotFoundException e) {
        e.printStackTrace();
        throw(e);
      }
      catch(SQLException e) {
        e.printStackTrace();
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
    
    return rowCount;
  }
  
  
  /**
   * Boolean to determine whether a data entity is already registered.
   * 
   * @param    packageId        the packageId string
   * @param    entityId         the entityId string
   * @return   true if registered, else false
   */
  public boolean isEntityRegistered(String packageId, String entityId) 
          throws ClassNotFoundException, SQLException {
    boolean isRegistered = false;
    Connection connection = null;
    String selectString = 
      "SELECT count(*) FROM " + DATA_CACHE_REGISTRY +
      "  WHERE package_id='" + packageId + "' AND " +
      "        entity_id='" + entityId + "'";
  
    Statement stmt = null;
  
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(selectString);
    
      while (rs.next()) {
        int count = rs.getInt("count");
        isRegistered = (count > 0);
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
     
    return isRegistered;
  }
  
  
  /**
   * Gets the data format of a given entity in the data cache.
   * Returns the contents of the 'data_format' field in the
   * data cache.
   * 
   * @param   packageId the package id of the data package
   * @param   entityId  the id of the entity whose data format is returned
   * @return  data format, a String
   */
  public String getDataFormat(String packageId, String entityId) 
          throws ClassNotFoundException, SQLException {
    Connection connection = null;
    String dataFormat = null;
    String selectString = 
      "SELECT data_format FROM " + DATA_CACHE_REGISTRY +
      " WHERE package_id ='" + packageId + "' AND entity_id='" + entityId + "'";
    Statement stmt = null;
    
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(selectString);
      
      while (rs.next()) {
        dataFormat = rs.getString("data_format");    
      }
    }
    catch(ClassNotFoundException e) {
      System.err.println("ClassNotFoundException: " + e.getMessage());
      throw(e);
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      returnConnection(connection);
    }
    
    return dataFormat;
  }
  
  
  /**
   * Gets the newest revision value for the current scope and identifier.
   * 
   * @param scope        the metadata document scope
   * @param identifier   the metadata document identifier
   * @return  newest, an Integer value representing the newest revision
   *          value for the specified scope and identifier
   */
  public Integer getNewestRevision(String scope, String identifier) 
          throws ClassNotFoundException, IllegalArgumentException, SQLException {
    Integer newest = null;
    
    try {
      Integer identifierInt = new Integer(identifier);
   
      Connection connection = null;
      String selectString = 
        "SELECT max(revision) FROM " + DATA_CACHE_REGISTRY +
        "  WHERE scope='" + scope + "' AND identifier='" + identifierInt + "'";
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

        if (stmt != null) stmt.close();
      }
      catch(ClassNotFoundException e) {
        logger.error("ClassNotFoundException: " + e.getMessage());
        e.printStackTrace();
        throw(e);
      }
      catch(SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        e.printStackTrace();
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
    catch (NumberFormatException e) {
      logger.error("Non-numeric identifier value: " + identifier);
      e.printStackTrace();
      throw(new IllegalArgumentException(e.getMessage()));
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
  public Integer getOldestRevision(String scope, String identifier)
          throws ClassNotFoundException, IllegalArgumentException, SQLException {

    Integer oldest = null;
    
    try {
      Integer identifierInt = new Integer(identifier);
   
      Connection connection = null;
      String selectString = 
        "SELECT min(revision) FROM " + DATA_CACHE_REGISTRY +
        "  WHERE scope='" + scope + "' AND identifier='" + identifierInt + "'";
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

        if (stmt != null) stmt.close();
      }
      catch(SQLException e) {
        logger.error("SQLException: " + e.getMessage());
        e.printStackTrace();
        throw(e);
      }
      finally {
        returnConnection(connection);
      }
    }
    catch (NumberFormatException e) {
      logger.error("Non-numeric identifier value: " + identifier);
      e.printStackTrace();
      throw(new IllegalArgumentException(e.getMessage()));
    }
    
    return oldest;
  }
  
  
}

