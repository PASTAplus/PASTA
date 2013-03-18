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

package edu.lternet.pasta.auditmanager;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import org.apache.log4j.Logger;


/**
* @author dcosta
* @version 1.0
* @created 16-Aug-2011 1:40:03 PM
* 
* The DataPackageRegistry class maintains the current state of data package
* resources in PASTA by reading from and writing to a database.
*/
public class AuditManager {

  /*
   * Class fields
   */
 
  private static Logger logger = Logger.getLogger(AuditManager.class);
 
 
  /*
   * Instance fields
   */
 
  // Name of the database table where audits are logged
  private final String AUDIT_MANAGER_SCHEMA = "auditmanager";
  private final String AUDIT_MANAGER_TABLE = "EVENTLOG";
  private final String AUDIT_MANAGER_TABLE_QUALIFIED = AUDIT_MANAGER_SCHEMA + "." + AUDIT_MANAGER_TABLE;

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
  public AuditManager(Properties p) 
         throws ClassNotFoundException, SQLException {  
    this.dbDriver = ConfigurationListener.getProperty(p, "dbDriver");
    this.dbURL = ConfigurationListener.getProperty(p, "dbURL");
    this.dbUser = ConfigurationListener.getProperty(p, "dbUser");
    this.dbPassword = ConfigurationListener.getProperty(p, "dbPassword");
   
    /*
     * Check for existence of Data Package Registry table. 
     * Create it if it does not already exist.
     */
    if (!isAuditTablePresent()) {
      String message = "The audit table was not found in the PASTA database.";
      throw new SQLException(message);
    }
  }
 

  /*
   * Class methods
   */
 
  public static String dateToTimestamp(String s, boolean isToTime) {
    if (s == null || s.isEmpty()) throw new IllegalStateException();
    
    /*
     *  Add a timestamp if it is missing from the date string.
     *  If isToTime is true, set the time to the last second of the day.
     */
    if (s.indexOf('T') == -1) {
      if (isToTime) {
        s += "T23:59:59";
      }
      else {
        s += "T00:00:00";
      }
    }
    
    return s;
  }

  
  public static Date strToDate(String s, boolean isToTime) {
    if (s == null || s.isEmpty()) throw new IllegalStateException();
    
    /*
     *  Add a timestamp if it is missing from the date string.
     *  If isToTime is true, set the time to the last second of the day.
     */
    if (s.indexOf('T') == -1) {
      if (isToTime) {
        s += "T23:59:59";
      }
      else {
        s += "T00:00:00";
      }
    }
    
    Date returnDate = DatatypeConverter.parseDateTime(s).getTime();
    logger.debug("returnDate: " + returnDate.toString());
    return returnDate;
  }

  
  /*
   * Instance methods
   */
 
  /**
   * Adds a new audit log record to the audit table. (TO BE IMPLEMENTED)
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
   * @param mayOverwrite If true, an existing resource with the same resourceId may
   *                     be overwritten by the new resource by updating its creation date.
   *                     This would be typically be set true only for evaluation resources
   *                     such as evaluation reports. If false, an error is generated.
   */
  public void addAuditLog(String resourceId, 
                          String resourceLocation,
                          String packageId, 
                          String scope, Integer identifier, Integer revision,
                          String entityId, 
                          String entityName, 
                          String principalOwner, 
                          boolean mayOverwrite)
         throws ClassNotFoundException, SQLException {
    Connection connection = null;
    java.sql.Timestamp ts = new java.sql.Timestamp(System.currentTimeMillis());
   
    StringBuffer insertSQL = new StringBuffer("INSERT INTO " +  AUDIT_MANAGER_TABLE_QUALIFIED + 
                                               "(");
    insertSQL.append("resource_id, resource_type, package_id, scope, identifier, " + 
                        "revision, resource_location, entity_id, entity_name, principal_owner, date_created) " + 
                        "VALUES(?,?,?,?,?,?,?,?,?,?,?)");
    String insertString = insertSQL.toString();
    logger.debug("insertString: " + insertString);

    try {
      connection = getConnection();
      PreparedStatement pstmt = connection.prepareStatement(insertString);
      pstmt.setString(1, resourceId);
      pstmt.setObject(2, resourceId);
      pstmt.setString(3, packageId);
      pstmt.setString(4, scope);
      pstmt.setInt(5, identifier);
      pstmt.setInt(6, revision);
      pstmt.setString(7, resourceLocation);
      pstmt.setString(8, entityId);
      pstmt.setString(9, entityName);
      pstmt.setString(10, principalOwner);
      pstmt.setTimestamp(11, ts);
      pstmt.executeUpdate();
      if (pstmt != null) {
        pstmt.close();
      }
    }
    catch (SQLException e) {
      logger.error("Error inserting record for resource " + resourceId
         + " into the resource registry (" + AUDIT_MANAGER_TABLE_QUALIFIED + ")");
      logger.error("SQLException: " + e.getMessage());
      throw (e);
    }
    finally {
      returnConnection(connection);
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
  
  
  private String getFieldName(String key) {
    String fieldName = key.toLowerCase();
    
    if (key.equalsIgnoreCase("user")) {
      fieldName = "userid";
    }
    else if (key.equalsIgnoreCase("responseStatus") ||
             key.equalsIgnoreCase("status")
            ) {
      fieldName = "statuscode";
    }
    
    return fieldName;
  }
  
  
  private String composeORClause(String key, List<String> values) {
    StringBuffer stringBuffer = new StringBuffer("( ");
    boolean firstValue = true;
    
    if (values.size() == 0) return "";
    
    for (String value : values) {
      if (!firstValue) { stringBuffer.append(" OR "); }
      String fieldName = getFieldName(key);
      
      if (fieldName.equals("resourceid")) {
      	stringBuffer.append(String.format("%s like '%s'", fieldName, value));
      } else {
      	stringBuffer.append(String.format("%s='%s'", fieldName, value));
      }
      
      firstValue = false;
    }
    
    stringBuffer.append(" )");
    String orClause = stringBuffer.toString();

    return orClause;
  }
  
  
  private String composeWhereClause(Map<String, List<String>> queryParams) {
    String whereClause = null;
    
    StringBuffer stringBuffer = new StringBuffer(" WHERE category IS NOT NULL");
    
    for (String key : queryParams.keySet()) {
      stringBuffer.append(" AND ");
      List<String> values = queryParams.get(key);
      
      if (key.equalsIgnoreCase("fromtime")) {
        String value = values.get(0);
        String timestamp = dateToTimestamp(value, false);
        stringBuffer.append(String.format(" entrytime >= '%s'", timestamp));
      }
      else if (key.equalsIgnoreCase("totime")) {
        String value = values.get(0);
        String timestamp = dateToTimestamp(value, true);
        stringBuffer.append(String.format(" entrytime <= '%s'", timestamp));
      }
      else if (key.equalsIgnoreCase("group")) {
        String orClause = composeORClause("groups", values);
        stringBuffer.append(orClause);
      }
      else {
        String orClause = composeORClause(key, values);
        stringBuffer.append(orClause);
      }
    }
    
    whereClause = stringBuffer.toString();
    
    return whereClause;
  }
 
 
  /**
   * Gets a list of audit log records from the audit table (named "eventlog")
   * matching the provided criteria.
   * 
   * @param queryParams    a map of query parameters and the values they should be matched to
   * @return               a list of matching LogItem objects
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public List<AuditRecord> getAuditRecords(Map<String, List<String>> queryParams)
           throws ClassNotFoundException, SQLException, IllegalArgumentException {
    List<AuditRecord> auditRecords = new ArrayList<AuditRecord>();
   
    if (queryParams != null) { 
      Connection connection = null;
     
      String selectString = 
        "SELECT oid, entrytime, service, category, servicemethod," +
        " entrytext, resourceid, statuscode, userid, groups, authsystem " +
        "FROM " + AUDIT_MANAGER_TABLE_QUALIFIED + 
        composeWhereClause(queryParams);
      
      logger.warn(selectString);
      
      Statement stmt = null;
     
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(selectString);
       
        while (rs.next()) {
          int oid = rs.getInt(1);
          java.sql.Timestamp sqlTimestamp = rs.getTimestamp(2);
          String service = rs.getString(3);
          String category = rs.getString(4);
          String serviceMethod = rs.getString(5);
          String entryText = rs.getString(6);
          String resourceId = rs.getString(7);
          int statusCode = rs.getInt(8);
          String userId = rs.getString(9);
          String groups = rs.getString(10);
          String authSystem = rs.getString(11);
          AuditRecord auditRecord = new AuditRecord();
          auditRecord.setOid(oid);
          java.util.Date entryTime = new java.util.Date(sqlTimestamp.getTime());
          auditRecord.setEntryTime(entryTime);
          auditRecord.setService(service);
          auditRecord.setCategory(category);
          auditRecord.setServiceMethod(serviceMethod);
          auditRecord.setEntryText(entryText);
          auditRecord.setResourceId(resourceId);
          auditRecord.setStatusCode(new Integer(statusCode));
          auditRecord.setUserId(userId);
          auditRecord.setGroups(groups);
          auditRecord.setAuthSystem(authSystem);
          auditRecords.add(auditRecord);
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
   
    return auditRecords;
  }
 

  /**
   * Boolean to determine whether the Data Package Registry
   * table already exists. If it isn't present, it will need to
   * be created.
   * 
   * @return  isPresent, true if the data cache table is present, else false
   */
  private boolean isAuditTablePresent() 
            throws ClassNotFoundException, SQLException {          
    boolean isPresent = false;
    String catalog = null;          // A catalog name (may be null)
    Connection connection = null;
    DatabaseMetaData databaseMetaData = null; // For getting db metadata
    ResultSet rs;
    String schemaPattern = AUDIT_MANAGER_SCHEMA; // A schema name pattern
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

          if (TABLE_NAME.equalsIgnoreCase(AUDIT_MANAGER_TABLE)) {
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

}