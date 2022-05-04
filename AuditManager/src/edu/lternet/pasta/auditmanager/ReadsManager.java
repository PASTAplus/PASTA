/**
*
* $Date$
* $Author: dcosta $
* $Revision$
*
* Copyright 2011-2018 the University of New Mexico.
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
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
* @author dcosta
* @version 1.0
* @created 16-Aug-2011 1:40:03 PM
* 
* The DataPackageRegistry class maintains the current state of data package
* resources in PASTA by reading from and writing to a database.
*/
public class ReadsManager {

    public enum ResourceType {
        archive, data, dataPackage, metadata, report
    }
    
  /*
   * Class fields
   */
 
  public static final String XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"; 
  public static final String READS_OPENING_TAG = "<resourceReads>\n"; 
  public static final String READS_CLOSING_TAG = "</resourceReads>\n";
  private static Logger logger = Logger.getLogger(ReadsManager.class);
 
  
  /*
   * Class methods
readDataEntity' OR " +
              "       servicemethod='readDataPackage' OR " +
              "       servicemethod='readDataPackageArchive' OR " +
              "       servicemethod='readDataPackageReport' OR " +
              "       servicemethod='readMetadata') " +   */
  
  public static void main(String[] args) 
          throws ClassNotFoundException, SQLException {
       ConfigurationListener.loadPropertiesFile("auditmanager.properties");
       Properties properties = ConfigurationListener.getProperties();
       ReadsManager readsManager = new ReadsManager(properties);
       
       // First, drop all rows in the table
       readsManager.deleteAllRows();
       readsManager.initializeResourceReads();
   }

   
  /*
   * Instance fields
   */
 
  // Name of the database table where audits are logged
  private final String READS_MANAGER_SCHEMA = "auditmanager";
  private final String READS_MANAGER_TABLE = "RESOURCE_READS";
  private final String READS_MANAGER_TABLE_QUALIFIED = READS_MANAGER_SCHEMA + "." + READS_MANAGER_TABLE;

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
  public ReadsManager(Properties p) 
         throws ClassNotFoundException, SQLException {  
    this.dbDriver = ConfigurationListener.getProperty(p, "dbDriver");
    this.dbURL = ConfigurationListener.getProperty(p, "dbURL");
    this.dbUser = ConfigurationListener.getProperty(p, "dbUser");
    this.dbPassword = ConfigurationListener.getProperty(p, "dbPassword");
    
    /*
     * Check for existence of RESOURCE_READS table. 
     */
    if (!isResourceReadsTablePresent()) {
      String message = "The resource_reads table was not found in the Audit Manager database.";
      throw new SQLException(message);
    }
  }
 

  /*
   * Class methods
   */
 
  /*
   * Instance methods
   */
  
    /*
     * Parses a resourceId value to derive its scope, identifier, and revision values.
     * The derived values are returned as a list of three strings, or null if the
     * resourceId value did not match any of the expected patterns.
     */
    private ArrayList<String> derivePackageIdParts(String resourceId) {
        ArrayList<String> packageIdParts = null;

        if (resourceId != null && resourceId.contains("/eml/")) {
            // Try to match resources other than data resources
            final String patternString = "^.*/eml/(\\S+)/(\\d+)/(\\d+)";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(resourceId);
            if (matcher.matches()) {
                packageIdParts = new ArrayList<String>();
                String scope = matcher.group(1);
                String identifier = matcher.group(2);
                String revision = matcher.group(3);
                packageIdParts.add(scope);
                packageIdParts.add(identifier);
                packageIdParts.add(revision);
            }
            else {
                // Try to match data resources
                final String dataPatternString = "^.*/eml/(\\S+)/(\\d+)/(\\d+)/.+";
                Pattern dataResourcePattern = Pattern.compile(dataPatternString);
                Matcher dataResourceMatcher = dataResourcePattern.matcher(resourceId);
                if (dataResourceMatcher.matches()) {
                    packageIdParts = new ArrayList<String>();
                    String scope = dataResourceMatcher.group(1);
                    String identifier = dataResourceMatcher.group(2);
                    String revision = dataResourceMatcher.group(3);
                    packageIdParts.add(scope);
                    packageIdParts.add(identifier);
                    packageIdParts.add(revision);
                }
            }
        }

        return packageIdParts;
    }
    
   
  /*
   * Drop all records from the resource_reads table prior to re-initializing it.
   */
  private void deleteAllRows() 
          throws ClassNotFoundException, SQLException {
      Connection connection = null;
      final String UPDATE_STRING = String.format(
          "DELETE FROM %s",
          READS_MANAGER_TABLE_QUALIFIED);
      
      Statement stmt = null;
      
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        int updateCount = stmt.executeUpdate(UPDATE_STRING);
        logger.info(String.format("%d records removed from resource_reads table.", updateCount));
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
  
  
  public ResourceType resourceTypeFromServiceMethod(String serviceMethod) {
      ResourceType resourceType = null;

      switch (serviceMethod) {
      case "readDataEntity":
            resourceType = ResourceType.data;
            break;
      case "readDataPackage":
            resourceType = ResourceType.dataPackage;
            break;
      case "readDataPackageArchive":
            resourceType = ResourceType.archive;
            break;
      case "readDataPackageReport":
            resourceType = ResourceType.report;
            break;
      case "readMetadata":
            resourceType = ResourceType.metadata;
            break;
      }
      
      return resourceType;
  }

  
    /**
     * Register a resource read. It will involve either incrementing an existing resource record
     * or adding a new one.
     * 
     * @param    resourceId  the resource identifier
     * @param    resourceType  the resource type
     * @param    isNonRobotRead  true if this read was not from a robot, else false
     */
    public void registerResourceRead(String resourceId, ResourceType resourceType, boolean isNonRobotRead)
            throws ClassNotFoundException, SQLException {
        ArrayList<String> packageIdParts = derivePackageIdParts(resourceId);

        if (packageIdParts != null) {
            String scope = packageIdParts.get(0);
            Integer identifier = Integer.parseInt(packageIdParts.get(1));
            Integer revision = Integer.parseInt(packageIdParts.get(2));

            if (hasResource(resourceId)) {
                incrementResourceRecord(resourceId, isNonRobotRead);
            } 
            else {
                addResourceRecord(resourceId, resourceType, scope, identifier, revision, isNonRobotRead);
            }
        }
    }
  
    
  /**
   * Initialize (or re-initialize) the contents of the resource_reads table.
   * 
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public void initializeResourceReads() 
          throws ClassNotFoundException, SQLException {
      Connection connection = null;
      ArrayList<String> resourceIds = new ArrayList<String>();
           
      final String SQL_STRING =
          "SELECT DISTINCT resourceId " +
              "FROM auditmanager.eventlog " +
              "WHERE (servicemethod='readDataEntity' " +
              "OR servicemethod='readDataPackage' " +
              "OR servicemethod='readDataPackageArchive' " +
              "OR servicemethod='readDataPackageReport' " +
              "OR servicemethod='readMetadata') " +
              "AND statuscode=200 " +
              "AND resourceId like 'http%%' " +
              "ORDER BY resourceId ASC";
      Statement stmt = null;
      
      try {
          connection = getConnection();
          stmt = connection.createStatement();
          ResultSet rs = stmt.executeQuery(SQL_STRING);
         
          while (rs.next()) {
            String resourceId = rs.getString("resourceId");
            resourceIds.add(resourceId);
          }
          
          System.err.printf("Processing %d resourceIds%n", resourceIds.size());
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
      
      try {
          connection = getConnection();
          int i = 0;
          
          for (String resourceId : resourceIds) {
              i++;
              System.err.printf("(%d) Processing resourceId: %s%n", i, resourceId);
          
              String SQL_STRING_RESOURCE = String.format(
                  "SELECT servicemethod, userid " +
                      "FROM auditmanager.eventlog " +
                      "WHERE resourceid='%s' " +
                      "AND (servicemethod='readDataEntity' " +
                      "OR servicemethod='readDataPackage' " +
                      "OR servicemethod='readDataPackageArchive' " +
                      "OR servicemethod='readDataPackageReport' " +
                      "OR servicemethod='readMetadata') AND statuscode=200 ",
              edu.lternet.pasta.common.SqlEscape.str(resourceId));

              try {
                  stmt = connection.createStatement();
                  ResultSet rs = stmt.executeQuery(SQL_STRING_RESOURCE);
       
                  while (rs.next()) {
                      String serviceMethod = rs.getString("servicemethod");
                      String userId = rs.getString("userid");
                      ReadsManager.ResourceType resourceType = resourceTypeFromServiceMethod(serviceMethod);
                      boolean isNonRobotRead = isNonRobotRead(userId);
                      registerResourceRead(resourceId, resourceType, isNonRobotRead);
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
              }
          }
      }
      finally {
          returnConnection(connection);
      }
  }
  
  
  public boolean isNonRobotRead(String userId) {
      boolean isNonRobotRead = true;
      
      if (userId != null && userId.startsWith("robot:")) {
          isNonRobotRead = false;
      }
      
      return isNonRobotRead;
  }
  
  
  /**
   * Adds a new resource_id entry to the resource_reads table
   * 
   * @param oid   
   * @param service 
   * @param category 
   * @param serviceMethod     
   * @param entryText  
   * @param resourceId  
   * @param statusCode   
   * @param userId 
   * @param groups 
   * @param authSystem 
   */
  public void addResourceRecord (
     String resourceId,
     ReadsManager.ResourceType resourceType,
     String scope,
     Integer identifier,
     Integer revision,
     boolean isNonRobotRead
     )
          throws ClassNotFoundException, SQLException {
    int totalReads = 1;
    int nonRobotReads = isNonRobotRead ? 1 : 0;
    Connection connection = null;

    String queryStr =
        String.format("INSERT INTO %s (resource_id, resource_type, scope, identifier, " +
            "revision, total_reads, non_robot_reads) " +
            "VALUES(?,?,?,?,?,?,?)", READS_MANAGER_TABLE_QUALIFIED);

    logger.debug("queryStr: " + queryStr);

    try {
      connection = getConnection();
      PreparedStatement pstmt =
          connection.prepareStatement(queryStr, Statement.RETURN_GENERATED_KEYS);
      pstmt.setString(1, resourceId);
      pstmt.setObject(2, resourceType, java.sql.Types.OTHER);
      pstmt.setString(3, scope);
      pstmt.setInt(4, identifier);
      pstmt.setInt(5, revision);
      pstmt.setInt(6, totalReads);
      pstmt.setInt(7, nonRobotReads);
      pstmt.executeUpdate();
      pstmt.close();
    }
    catch (SQLException e) {
        logger.error("Error inserting record for resource " + resourceId
            + " into the Audit Manager (" + READS_MANAGER_TABLE_QUALIFIED + ")");
        logger.error("SQLException: " + e.getMessage());
        throw (e);
      }
    finally {
        returnConnection(connection);
    }
    
  }
  
  
  /**
   * Increments the total_reads (and possibly the non_robot_reads) value(s) for the specified resource_id.
   * 
   * @param resourceId              The resource whose value is to be incremented.
   * @param isNonRobotRead          If true, the non_robot_reads field is also incremented.
   * @throws ClassNotFoundException
   * @throws SQLException
   */
  public void incrementResourceRecord(String resourceId, boolean isNonRobotRead)
      throws ClassNotFoundException, SQLException
  {
    String sqlQuery;
    Statement stmt = null;

    if (isNonRobotRead) {
      sqlQuery = String.format(
          "UPDATE %s SET total_reads = total_reads + 1, " +
              "non_robot_reads = non_robot_reads + 1 " +
              "WHERE resource_id=?",
          READS_MANAGER_TABLE_QUALIFIED);
    }
    else {
      sqlQuery = String.format(
          "UPDATE %s SET total_reads = total_reads + 1 WHERE resource_id=?",
          READS_MANAGER_TABLE_QUALIFIED);
    }

    Connection conn = null;

//    org.postgresql.core.Utils.escapeLiteral

    try {
      conn = getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sqlQuery);
      pstmt.setObject(1, resourceId);
      pstmt.executeQuery();
    } catch (ClassNotFoundException e) {
      logger.error("ClassNotFoundException: " + e.getMessage());
      throw (e);
    } catch (SQLException e) {
      logger.error("SQLException: " + e.getMessage());
      throw (e);
    } finally {
      returnConnection(conn);
    }
  }
       
       
  /*
   * Formats a resource record from the resource_reads table as XML
   */
  private String toXML(String resourceId, 
                       String resourceType, 
                       String scope, 
                       Integer identifier, 
                       Integer revision, 
                       int totalReads, 
                       int nonRobotReads) {
      String xmlString = null;
      
      StringBuffer sb = new StringBuffer("    <resource>\n");
      sb.append(String.format("        <resourceId>%s</resourceId>\n", resourceId));
      sb.append(String.format("        <resourceType>%s</resourceType>\n", resourceType));
      sb.append(String.format("        <scope>%s</scope>\n", scope));
      sb.append(String.format("        <identifier>%d</identifier>\n", identifier));
      sb.append(String.format("        <revision>%d</revision>\n", revision));
      sb.append(String.format("        <totalReads>%d</totalReads>\n", totalReads));
      sb.append(String.format("        <nonRobotReads>%d</nonRobotReads>\n", nonRobotReads));
      sb.append("    </resource>\n");
      xmlString = sb.toString();
      
      return xmlString;
  }

  
  /**
   * Gets a list of resource read records from the resource_reads table
   * matching the provided criteria, and return them in XML format.
   * 
   * @param scope  the scope value to match
   * @param identifier the identifier value to match
   * @return  an XML-formatted list of matching records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public String getDocIdReads(String scope, Integer identifier)
           throws ClassNotFoundException, SQLException, IllegalArgumentException {
      StringBuilder stringBuffer = new StringBuilder(XML_DECLARATION);
      stringBuffer.append(READS_OPENING_TAG);
      String xmlString = null;
      Connection connection = null;
     
      String queryStr = String.format(
          "SELECT * FROM %s " +
                "WHERE scope='%s' AND identifier=%d " +
                "ORDER BY scope, identifier, revision ASC",
                      READS_MANAGER_TABLE_QUALIFIED, SqlEscape.str(scope), identifier);

      logger.debug("queryStr: " + queryStr);

      Statement stmt = null;
     
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
       
        while (rs.next()) {
          String resourceId = rs.getString("resource_id");
          String resourceType = rs.getString("resource_type");
          Integer revision = rs.getInt("revision");
          int totalReads = rs.getInt("total_reads");
          int nonRobotReads = rs.getInt("non_robot_reads");
          String resourceXML = toXML(resourceId, resourceType, scope, identifier, revision, totalReads, nonRobotReads);
          stringBuffer.append(resourceXML);
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
        stringBuffer.append(READS_CLOSING_TAG);
        xmlString = stringBuffer.toString();
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
   
    return xmlString;
  }
 

  /**
   * Gets a list of resource read records from the resource_reads table
   * matching the provided criteria, and return them in XML format.
   * 
   * @param scope  the scope value to match
   * @param identifer the identifier value to match
   * @param revision  the revision value to match
   * @return  an XML-formatted list of matching records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public String getPackageIdReads(String scope, Integer identifier, Integer revision)
           throws ClassNotFoundException, SQLException, IllegalArgumentException {
      StringBuffer stringBuffer = new StringBuffer(XML_DECLARATION);
      stringBuffer.append(READS_OPENING_TAG);
      String xmlString = null;
      Connection connection = null;
     
      String queryStr =
        String.format("SELECT * FROM %s " +
                "WHERE scope='%s' AND identifier=%d AND revision=%d " +
                "ORDER BY scope, identifier, revision ASC",
                      READS_MANAGER_TABLE_QUALIFIED, SqlEscape.str(scope), identifier, revision);

      logger.debug("queryStr: " + queryStr);

      Statement stmt = null;
     
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
       
        while (rs.next()) {
          String resourceId = rs.getString("resource_id");
          String resourceType = rs.getString("resource_type");
          int totalReads = rs.getInt("total_reads");
          int nonRobotReads = rs.getInt("non_robot_reads");
          String resourceXML = toXML(resourceId, resourceType, scope, identifier, revision, totalReads, nonRobotReads);
          stringBuffer.append(resourceXML);
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
        stringBuffer.append(READS_CLOSING_TAG);
        xmlString = stringBuffer.toString();
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
   
    return xmlString;
  }
 

  /**
   * Gets a list of resource read records from the resource_reads table
   * matching the provided criteria, and return them in XML format.
   * 
   * @param resourceId the resource identifier to match
   * @return  an XML-formatted list of matching records
   * @throws ClassNotFoundException
   * @throws SQLException
   * @throws IllegalArgumentException
   */
  public String getResourceIdReads(String resourceId)
           throws ClassNotFoundException, SQLException, IllegalArgumentException {
      StringBuffer stringBuffer = new StringBuffer(XML_DECLARATION);
      stringBuffer.append(READS_OPENING_TAG);
      String xmlString = null;
      Connection connection = null;
     
      String queryStr =
        String.format("SELECT * FROM %s " +
                "WHERE resource_id='%s' " +
                "ORDER BY scope, identifier, revision ASC",
                      READS_MANAGER_TABLE_QUALIFIED, SqlEscape.str(resourceId));

      logger.debug("queryStr: " + queryStr);

      Statement stmt = null;
     
      try {
        connection = getConnection();
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
       
        while (rs.next()) {
            String resourceType = rs.getString("resource_type");
            String scope = rs.getString("scope");
            Integer identifier= rs.getInt("identifier");
            Integer revision = rs.getInt("revision");
            int totalReads = rs.getInt("total_reads");
            int nonRobotReads = rs.getInt("non_robot_reads");
            String resourceXML = toXML(resourceId, resourceType, scope, identifier, revision, totalReads, nonRobotReads);
            stringBuffer.append(resourceXML);
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
        stringBuffer.append(READS_CLOSING_TAG);
        xmlString = stringBuffer.toString();
        if (stmt != null) stmt.close();
        returnConnection(connection);
      }
   
    return xmlString;
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
   * Boolean to determine whether the specified audit record is in the
   * audit database based on a specified audit id value
   * 
   * @param auditEntryId   the audit entry identifier
   * @return  true if the audit record is present, else false
   */
  public boolean hasResource(String resourceId)
          throws ClassNotFoundException, SQLException {
    boolean hasResource = false;
    Connection connection = null;
    String queryStr =
        String.format("SELECT count(*) FROM %s WHERE resource_id='%s'",
            READS_MANAGER_TABLE_QUALIFIED, SqlEscape.str(resourceId));

    logger.debug("queryStr: " + queryStr);

    Statement stmt = null;
  
    try {
      connection = getConnection();
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
    
      while (rs.next()) {
        int count = rs.getInt("count");
        hasResource = (count > 0);
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
    
    return hasResource;
  }
  

  /**
   * Boolean to determine whether the audit table already exists. 
   * If it isn't present, it will need to be created.
   * 
   * @return  isPresent, true if the audit table is present, else false
   */
  private boolean isResourceReadsTablePresent() 
            throws ClassNotFoundException, SQLException {          
    boolean isPresent = false;
    String catalog = null;          // A catalog name (may be null)
    Connection connection = null;
    DatabaseMetaData databaseMetaData = null; // For getting db metadata
    ResultSet rs;
    String schemaPattern = READS_MANAGER_SCHEMA; // A schema name pattern
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

          if (TABLE_NAME.equalsIgnoreCase(READS_MANAGER_TABLE)) {
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