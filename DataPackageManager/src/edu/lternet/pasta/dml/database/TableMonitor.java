/**
 *    '$RCSfile: TableMonitor.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2008-01-03 23:32:10 $'
 *   '$Revision: 1.19 $'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package edu.lternet.pasta.dml.database;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import edu.lternet.pasta.common.SqlEscape;
import edu.lternet.pasta.datapackagemanager.WorkingOn;
import edu.lternet.pasta.dml.DataManager;
import edu.lternet.pasta.dml.parser.Entity;
import org.apache.log4j.Logger;

/**
 * TableMonitor monitors all data tables in the database. It stores information
 * about each table in a data table registry:
 *  
 *   table name                    the database table name
 *   entity identifier             the entity identifier (its URL)
 *   entity name                   the entity name
 *   creation date                 creation date of the database table
 *   last usage date               last usage date of the database table
 *   priority (expiration policy)  controls whether the table can be expired 
 *                                 from the cache
 *   
 * It also sets the maximum amount of space that the database can use, and
 * attempts to free up space by dropping old tables when necessary.
 * 
 */
public class TableMonitor {
  
  private static Logger logger = Logger.getLogger(TableMonitor.class);
  
  /*
   * Class fields
   */


  /*
   * Instance fields
   */

  private DatabaseAdapter    dbAdapter = null;   // the DatabaseAdapter name
  private final String DATA_TABLE_REGISTRY = "DATA_TABLE_REGISTRY";
                                             // name of the database table where
                                             // data tables are registered
  private final int DEFAULT_DB_SIZE = 100;   // default maximum DB size (in Mb)
  private int dbSize = DEFAULT_DB_SIZE;      // maximum DB size (in Mb)
  
  
  /*
   * Constructors
   */

  /**
   * Constructs a new TableMonitor object.
   * 
   * @param   dbConnection  the database Connection object
   * @param   dbAdapterName the DatabaseAdapter name
   * @return  a TableMonitor object
   */
  public TableMonitor(DatabaseAdapter dbAdapter)
        throws SQLException {
    this.dbAdapter = dbAdapter;

    /*
     * Check for existence of dataTableRegistry table. Create it if it does not
     * already exist.
     */
    if (!isTableInDB(DATA_TABLE_REGISTRY)) {
      createDataTableRegistry();
    }
    
  }
  
  
  /*
   * Class methods
   */
	

  /*
   * Instance methods
   */
	
  /**
   * Adds a new table entry for a given Entity object. By default, the creation
   * date and last used date are set to the current date and time. By default,
   * the expiration policy is set to 1 (may be expired).
   * 
   * @param   entity        the Entity object for which a table entry is added
   * @return  the name of the table that was added, or null if not successful
   */
  public String addTableEntry(Entity entity) throws SQLException {
    String entityIdentifier = entity.getEntityIdentifier();
    String entityName = entity.getName();
    String packageId = entity.getPackageId();
    String queryStr;
    Date now = new Date();
    String priority = "1";
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Statement stmt = null;
    
    // Assign a table name for this entity
    String tableName = assignTableName(entityIdentifier, entityName);
    
    boolean inUse = isDBTableNameInUse(tableName);

    /*
     * If we already have an entry for this entity in the data table registry,
     * simply update its last usage date to the current date.
     */
    if (inUse) {
      setLastUsageDate(tableName, now);
    } 
    /*
     * Otherwise, insert a new entry for this entity into the data
     * table registry.
     */
    else {
      queryStr =
          String.format("INSERT INTO %s values(%s, %s, %s, %s, %s, %s, %s)",
              SqlEscape.name(DATA_TABLE_REGISTRY),
              SqlEscape.str(tableName),
              SqlEscape.str(packageId),
              SqlEscape.str(entityIdentifier),
              SqlEscape.str(entityName),
              SqlEscape.str(simpleDateFormat.format(now)),
              SqlEscape.str(simpleDateFormat.format(now)),
              SqlEscape.str(priority));

      logger.debug("queryStr: " + queryStr);
      
      Connection connection = DataManager.getConnection();

      try {
        stmt = connection.createStatement();
        stmt.executeUpdate(queryStr);
      } 
      catch (SQLException e) {
        System.err.println("Error inserting record for " + tableName
            + " into the data table registry (" + DATA_TABLE_REGISTRY + ")");
        System.err.println("SQLException: " + e.getMessage());
        tableName = null;
      } 
      finally {
        if (stmt != null) stmt.close();
        DataManager.returnConnection(connection);
      }
    }
    
    entity.setDBTableName(tableName);
    return tableName;
  }
  
  
  /**
   * Assigns a table name to a particular entity name.
   * 
   * If the table name has previously been assigned to this entity, simply
   * return the previously assigned table name.
   * 
   * Otherwise, choose a table name to assign. If the table name is already in
   * use by a different entity, mangle the name, and check again. If still in
   * use, mangle again, check again, etc.
   * 
   * The table name assigned to the entity must be unique, and once we have
   * assigned a name for this entity we should not need to assign a new one.
   * 
   * @param entityIdentifier    the id of the Entity object
   * @param entityName  the name of the Entity object
   * @return tableName  the name of the database table assigned to this Entity
   *         object
   */
  String assignTableName(String entityIdentifier, String entityName) 
          throws SQLException {
    Connection connection = DataManager.getConnection();
    String tableName = null;
    String queryStr = String.format(
        "SELECT TABLE_NAME, ENTITY_IDENTIFIER, ENTITY_NAME " +
            "FROM %s " +
            "WHERE ENTITY_IDENTIFIER=%s AND ENTITY_NAME=%s",
        SqlEscape.name(DATA_TABLE_REGISTRY),
        SqlEscape.str(entityIdentifier),
        SqlEscape.str(entityName)
    );
    
    logger.debug("queryStr: " + queryStr);
    
    Statement stmt = null;

    /*
     * First, determine whether this entity has already been assigned a table
     * name. If it has, just return the previously assigned table name.
     */
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        tableName = rs.getString("table_name");
      }    
    }
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }

    /*
     * If tableName is still null, it means that we have not already assigned
     * a table name to this entity. Now we have to go through a loop,
     * trying different candidate table names until we finally
     * find a name that is not already in use.
     */
    if (tableName == null) {
      String tableNameCandidate = 
                                DatabaseAdapter.getLegalDBTableName(entityName);
      
      while (tableName == null) {
        if (isDBTableNameInUse(tableNameCandidate)) {
          tableNameCandidate = mangleName(tableNameCandidate);
        }
        else {
          tableName = tableNameCandidate;
        }
      }
    }
    
    return tableName;
  }
  

  /**
   * Counts the number of rows in a table.
   * 
   * @param tableName         the table name
   * @return                  an integer indicating the row count, or -1 if
   *                          the table is not in the database 
   * @throws SQLException
   */
  public int countRows(String tableName) throws SQLException {
    int rowCount = -1;
    
    if (isTableInDB(tableName)) {
      String queryStr = dbAdapter.getCountingRowNumberSQL(tableName);
      Statement stmt = null;
      
      Connection connection = DataManager.getConnection();

      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(queryStr);
        
        while (rs.next()) {
          rowCount = rs.getInt("count");
        }    
        if (rs != null)rs.close();
      }
      catch (SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {	
        if (stmt != null) stmt.close();
        DataManager.returnConnection(connection);
      }
    }
    
    return rowCount;
  }
  
  
  /**
   * Creates the DATA_TABLE_REGISTRY table. This is the table that the
   * TableMonitor uses to keep track of data table information such as
   * data table name, when the data table was created, when it was last
   * used, and its priority (expiration policy) setting.
   *
   */
  private void createDataTableRegistry() throws SQLException {
    Connection connection = DataManager.getConnection();
    // database table name
    // package id
    // entity identifier
    // entity name
    // creation date
    // last usage date
    // expiration policy
    String createString = String.format(
        "create table %s (TABLE_NAME varchar(64), PACKAGE_ID varchar(64), ENTITY_IDENTIFIER varchar(256), ENTITY_NAME varchar(256), CREATION_DATE date, LAST_USAGE_DATE date, PRIORITY int)",
        DATA_TABLE_REGISTRY);

    Statement stmt = null;

    try {
      stmt = connection.createStatement();             
      stmt.executeUpdate(createString);
    } 
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
  }
  
  
  /**
   * Drops a table entry for a given table name.
   * 
   * @param   tableName   the name of the table to be dropped from the database
   * @return  the row count returned by executing the SQL update
   */
  public boolean dropTableEntry(String tableName) throws SQLException {
    Connection connection = DataManager.getConnection();
    boolean success = false;
    String queryStr;
    int rowCount = -1;
    Statement stmt = null;

    queryStr =
        String.format("DELETE FROM %s WHERE TABLE_NAME=%s",
            SqlEscape.name(DATA_TABLE_REGISTRY),
            SqlEscape.str(tableName)
        );
    
    logger.debug("queryStr: " + queryStr);
    
    try {
      stmt = connection.createStatement();
      rowCount = stmt.executeUpdate( queryStr);
      success = (rowCount == 1);
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return success;
  }
  

  /**
   * Frees up table space by dropping one or more old tables.
   * @param  databaseHandler  the databaseHandler which will free space
   * @return  the size of table which has been freed
   */
  public int freeTableSpace(DatabaseHandler databaseHandler)
          throws SQLException {
    int freedSpace = 0;
    //String oldestTable = getOldestTable();
    
    return freedSpace;
  }
  
 
  /**
   * Gets the creation date of a given table in the database.
   * 
   * @param   tableName   the name of the table whose creation date is returned
   * @return  the creation date, a Date object
   */
  public Date getCreationDate(String tableName) throws SQLException {
    Connection connection = DataManager.getConnection();
    Date creationDate = null;
    String queryStr =
        String.format("SELECT creation_date FROM %s WHERE table_name=%s",
            SqlEscape.name(DATA_TABLE_REGISTRY),
            SqlEscape.str(tableName)
        );
    
    logger.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    try {
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        creationDate = rs.getDate("creation_date");    
      }
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return creationDate;
  }
  

  /**
   * Gets a list of database field names for the specified packageID and entity
   * name.
   * 
   * @param packageID    the packageID for this entity
   * @param entityName   the entity name
   * @return  a String array holding the field names for this entity, or null
   *          if there was no match for this packageID and entity name in the
   *          database.
   * @throws SQLException
   */
  public String[] getDBFieldNames(String packageID, String entityName) 
        throws SQLException{
    String catalog = null;          // A catalog name (may be null)
    String columnNamePattern = "%"; // Matches all column names in the table
    DatabaseMetaData databaseMetaData = null; // For getting db metadata
    String[] fieldNames = null;
    ResultSet rs;
    String schemaPattern = null;    // A schema name pattern (may be null)
    String tableName = getDBTableName(packageID, entityName);
    
    if (tableName != null ) {
      Vector vector = new Vector();
      Connection connection = DataManager.getConnection();
      String tableNamePattern = tableName.toUpperCase();
      databaseMetaData = connection.getMetaData();
      rs = databaseMetaData.getColumns(catalog, schemaPattern, 
                                       tableNamePattern, columnNamePattern);
      while (rs.next()) {
        String fieldName = rs.getString("COLUMN_NAME");
        vector.add(fieldName);
      }

      /* 
       * Deal with case sensitivity issues in table names. If the uppercase
       * version of the table name pattern didn't return any columns, try
       * matching against the lowercase version.
       */
      if (vector.size() == 0) {
        if (rs != null) rs.close();
        tableNamePattern = tableName.toLowerCase();
        rs = databaseMetaData.getColumns(catalog, schemaPattern,
                                         tableNamePattern, columnNamePattern);
        while (rs.next()) {
          String fieldName = rs.getString("COLUMN_NAME");
          vector.add(fieldName);
        }
      }
      
      fieldNames = new String[vector.size()];

      for (int i = 0; i < fieldNames.length; i++) {
        fieldNames[i] = (String) vector.elementAt(i);
      }
      
      if (rs != null) rs.close();
      DataManager.returnConnection(connection);
    
    
      /*
       * Ensure that the field names are surrounded by quotes.
       * (See Bug #2737: 
       *   http://bugzilla.ecoinformatics.org/show_bug.cgi?id=2737
       * )
       */
      final String QUOTE = DatabaseAdapter.DOUBLEQUOTE;
      for (int i = 0; i < fieldNames.length; i++) {
        String fieldName = fieldNames[i];
        if (fieldName != null) {
          if (!fieldName.startsWith(QUOTE)) {
            fieldName = QUOTE + fieldName;
          }
          if (!fieldName.endsWith(QUOTE)) {
            fieldName = fieldName + QUOTE;
          }
          fieldNames[i] = fieldName;
        }
      }
    }
    
    return fieldNames;
  }


  /**
   * Gets the database table name for a specified packageID and entity name.
   * 
   * @param packageID    the packageID for this entity
   * @param entityName   the entity name
   * @return tableName   the database table name for this entity, or null if
   *                     no match to the packageID and entity name is found
   * @throws SQLException
   */
  public String getDBTableName(String packageID, String entityName) 
          throws SQLException {
    String tableName = null;
    
    Connection connection = DataManager.getConnection();
    String queryStr = String.format(
        "SELECT table_name FROM %s WHERE package_id=%s AND entity_name=%s",
        SqlEscape.name(DATA_TABLE_REGISTRY),
        SqlEscape.str(packageID),
        SqlEscape.str(entityName)
    );

    logger.debug("queryStr: " + queryStr);

    Statement stmt = null;
      
    try {
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      if (rs.next()) {
        tableName = rs.getString("table_name");
      }
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
          
    return tableName;
  }
  
 
  /**
   * Gets a list of the database table names for all data tables
   * associated with a specified packageID.
   * 
   * @param packageID    the packageID
   * @return tableNames  a list of database table names associated with
   *           this packageId, or null if no matches to the packageID 
   *           were found
   * @throws SQLException
   */
  public ArrayList<String> getDBTableNames(String packageID) 
          throws SQLException {
    ArrayList<String> tableNames = null;
    
    if (packageID != null) {
      tableNames = new ArrayList<String>();
      Connection connection = DataManager.getConnection();
      String queryStr =
          String.format("SELECT table_name FROM %s WHERE package_id =%s",
              SqlEscape.name(DATA_TABLE_REGISTRY),
              SqlEscape.str(packageID)
          );

      logger.debug("queryStr: " + queryStr);

      Statement stmt = null;
      
      try {
        stmt = connection.createStatement();             
        ResultSet rs = stmt.executeQuery(queryStr);
      
        while (rs.next()) {
          String tableName = rs.getString("table_name");
          tableNames.add(tableName);
        }
      }
      catch(SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        throw(e);
      }
      finally {
        if (stmt != null) stmt.close();
        DataManager.returnConnection(connection);
      }
    }
          
    return tableNames;
  }
  
 
  /**
   * Returns the name of the data table registry table. Used primarily for 
   * unit testing of this class by the TableMonitorTest class.
   * 
   * @return   The private constant, DATA_TABLE_REGISTRY.
   */
  public String getDataTableRegistryName () {
    return DATA_TABLE_REGISTRY;
  }
  

  /**
   * Gets the last usage date for a given table name in the database.
   * 
   * @param  tableName  the name of the the table whose last usage date
   *                    is returned
   * @return  the last usage date, a Date object
   */
  public Date getLastUsageDate(String tableName) throws SQLException {
    Connection connection = DataManager.getConnection();
    Date lastUsageDate = null;
    String queryStr =
        String.format("SELECT last_usage_date FROM %s WHERE table_name=%s",
            SqlEscape.name(DATA_TABLE_REGISTRY),
            SqlEscape.str(tableName)
        );

    logger.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    try {
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        lastUsageDate = rs.getDate("last_usage_date");    
      }
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return lastUsageDate;
  }
  

  /**
   * Find the oldest table in the data table registry (the table whose 
   * last_usage_date is the oldest) and return its table name.
   * 
   * @return the name of the oldest table in the data table registry
   * @throws SQLException
   */
  String getOldestTable() throws SQLException {
    Connection connection = DataManager.getConnection();
    Date oldestDate = new Date();
    String oldestTable = null;
    String queryStr = String.format("SELECT table_name, last_usage_date FROM %s",
        DATA_TABLE_REGISTRY);

    logger.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    try {
      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        String tableName = rs.getString("table_name");
        Date lastUsageDate = rs.getDate("last_usage_date");
        
        if (lastUsageDate.before(oldestDate)) {
          oldestDate = lastUsageDate;
          oldestTable = tableName;
        }
      }    
    }
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return oldestTable;
  }


  /**
   * Gets a list of all the table names currently in the database.
   * 
   * @return  a String array of all tables names currently in the database
   */
  public String[] getTableList() throws SQLException {
    Connection connection = DataManager.getConnection();
    String queryStr = String.format("SELECT table_name FROM %s", DATA_TABLE_REGISTRY);
    logger.debug("queryStr: " + queryStr);
    Statement stmt = null;
    Vector vector = new Vector();
    
    try {
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        String tableName = rs.getString("table_name");
        vector.add(tableName);
      }
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    String[] tableList = new String[vector.size()];
    for (int i = 0; i < vector.size(); i++) {
      tableList[i] = (String) vector.get(i);
    }

    return tableList;
  }
  
  
  /**
   * Given an identifier string, return its corresponding table name. 
   * 
   * @param   identifier   the identifier string for the entity
   * @return  the corresponding table name, or null if there is no entry for
   *          this identifier
   */
  String identifierToTableName(String identifier) 
          throws SQLException {
    Connection connection = DataManager.getConnection();
    String tableName = null;

    String queryStr = String.format(
        "SELECT table_name FROM %s WHERE entity_identifier=%s",
        SqlEscape.name(DATA_TABLE_REGISTRY),
        SqlEscape.str(identifier)
    );

    logger.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    try {
      stmt = connection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        tableName = rs.getString("table_name");    
      }
      if(rs != null)rs.close();
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return tableName;
  }
  

  /**
   * Checks the data table registry to determine whether a given table name is
   * already in use. 
   * 
   * @param tableName        the table name to be checked 
   * @return                 true if the table name is in use, else false
   * @throws SQLException
   */
  boolean isDBTableNameInUse(String tableName) throws SQLException {
    boolean inUse = false;
    String[] tableNames = getTableList();
    
    for (int i = 0; i < tableNames.length; i++) {
      if (tableNames[i].equalsIgnoreCase(tableName)) {
        inUse = true;
        break;
      }
    }
    
    return inUse;
  }
  

  /**
   * Boolean to determine whether a given table name is currently in the
   * database.
   * 
   * @param tableName   the name of the table that is being checked
   * @return  true if the table is currently in the database, else false
   */
  public boolean isTableInDB(String tableName) throws SQLException {
    String catalog = null;          // A catalog name (may be null)
    Connection connection = DataManager.getConnection();
    DatabaseMetaData databaseMetaData = null; // For getting db metadata
    boolean isPresent = false;  
    ResultSet rs;
    String schemaPattern = null;    // A schema name pattern (may be null)
    String tableNamePattern = "%";  // Matches all table names in the db
    String[] types = {"TABLE"};     // A list of table types to include
    
    databaseMetaData = connection.getMetaData();
    rs = databaseMetaData.getTables(catalog, schemaPattern, 
                                    tableNamePattern, types);

    while (rs.next()) {
      String TABLE_NAME = rs.getString("TABLE_NAME");
 
      if (TABLE_NAME.equalsIgnoreCase(tableName)) {
        isPresent = true;
      }
    }
    
    if (rs != null) rs.close();
    DataManager.returnConnection(connection);
    
    return isPresent;
  }
  

  /**
   * Given a table name, return a mangled name. This is done by tagging on a
   * string pattern followed by an integer. If the table name always contains
   * the string pattern (i.e. it has already been mangled one or more times
   * already), mangle it further by incrementing the integer. Eventually, the
   * integer will be incremented to a value that has not been used before for
   * this name.
   * 
   * Examples:
   * 
   * mangleName("aTable")  -->  "aTable_XYZXY_1"
   * mangleName("aTable_XYZXY_1")  -->  "aTable_XYZXY_2"
   * mangleName("aTable_XYZXY_344")  -->  "aTable_XYZXY_345"
   * 
   * @param  tableName  the table name to be mangled
   * @return the mangled name
   */
  String mangleName(String tableName) {
    int index;
    int tailInt = 0;
    String mangledName;
    String patternMatch = "_XYZYX_";  // Used to flag this as a mangled name
    String rootName = tableName;
    
    index = tableName.indexOf(patternMatch);

    /*
     * Check whether the table name has been previously mangled one or more
     * times already. If it has (i.e. it contains the pattern)
     */
    if (index > -1) {
      String tail = tableName.substring(index + patternMatch.length());
      Integer tailInteger = Integer.valueOf(tail);
      tailInt = tailInteger.intValue();
      rootName = tableName.substring(0, index);
    }
    
    tailInt++;
    mangledName = rootName + patternMatch + tailInt;
    
    return mangledName;
  }
	

  /**
   * Sets the maximum database size to the given value (in Megabytes).
   * (Note: How do we persist this value -- in a table?)
   * 
   * @param size   the maximum size (in Megabytes) of the database
   */
  public void setDBSize(int size) {
    int minSize = 1;      // Don't allow dbSize to be set below a minimum value

    if (size < minSize) {
      dbSize = minSize;
    }
    else {
      dbSize = size;
    }
  }
  

  /**
   * Sets the last usage date for a given table in the database. This method
   * should be called by the DatabaseHandler whenever the table is accessed
   * in any way.
   * 
   * @param tableName  the name of the table whose usage date we are setting.
   * @param date       the date to set
   * @return  true if the last usage date is successfully set; else false
   */
  public boolean setLastUsageDate(String tableName, Date date)
        throws SQLException {
    Connection connection = DataManager.getConnection();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = simpleDateFormat.format(date);
    int rowCount = 0;
    Statement stmt = null;
		boolean success = false;

    String queryStr = String.format(
        "UPDATE %s SET last_usage_date=%s WHERE table_name=%s",
        SqlEscape.name(DATA_TABLE_REGISTRY),
        SqlEscape.str(dateString),
        SqlEscape.str(tableName)
    );

    logger.debug("queryStr: " + queryStr);

    // Set the last usage date
    try {
      stmt = connection.createStatement();
      rowCount = stmt.executeUpdate(queryStr);
      success = (rowCount == 1);
    } 
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw (e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return success;
	}


  /**
   * Sets the expiration policy for a given table in the database.
   * We'll start by keeping it very simple. Non-zero means that the table is
   * allowed to expire (i.e. can be removed from the database when it gets old),
   * while zero means that it should never expired (always stays in the
   * database).
   * 
   * @param tableName    the name of the table whose expiration policy is 
   *                     being set
   * @param priority     the expiration policy: non-zero means that table
   *                     can be deleted; zero means that it should never be
   *                     expired
   * @return  true if the expiration policy was successfully set, else false
   */
  public boolean setTableExpirationPolicy(String tableName, int priority) 
        throws SQLException {
    Connection connection = DataManager.getConnection();
    int rowCount = 0;
    Statement stmt = null;
    boolean success = false;

    String queryStr =
        String.format("UPDATE %s SET priority=%s WHERE table_name=%s",
            SqlEscape.name(DATA_TABLE_REGISTRY),
            SqlEscape.integer(priority),
            SqlEscape.str(tableName)
        );

    logger.debug("queryStr: " + queryStr);

    // Set the last usage date
    try {
      stmt = connection.createStatement();
      rowCount = stmt.executeUpdate( queryStr);
      success = (rowCount == 1);
    } 
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw (e);
    }
    finally {
      if (stmt != null) stmt.close();
      DataManager.returnConnection(connection);
    }
    
    return success;
  }
	
}
