package edu.lternet.pasta.dml.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import edu.lternet.pasta.common.SqlEscape;
import edu.lternet.pasta.datapackagemanager.WorkingOn;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import edu.lternet.pasta.dml.database.DatabaseAdapter;
import edu.lternet.pasta.dml.DataManager;
import edu.lternet.pasta.dml.parser.Entity;
import org.apache.log4j.Logger;

public class TableMonitorTest extends TestCase {

  /*
   * Instance fields
   */
    
  private TableMonitor tableMonitor;  // An instance of the object being tested
  private Connection dbConnection     = null;         // the database connection
  private final String packageId      = "test.1.1";
  private Entity entity               = null;
  private final String id             = "001";
  private final String entityName     = "testEntity";
  private String dbAdapterName        = null;
  private DatabaseConnectionPoolInterfaceTest connectionPool = null;
  
  // For testGetOldestTable(). Use an entity whose last usage date is
  // set to a very old date (near the start of the Unix epoch), and a second
  // entity whose last usage date is now.
  private Entity entityAncient           = null;
  private final String idAncient         = "002";
  private final String entityNameAncient = "ancientEntity";
  private Entity entityCurrent           = null;
  private final String idCurrent         = "003";
  private final String entityNameCurrent = "currentEntity";
  
  private final String description    = "testEntity";
  private final Boolean caseSensitive = new Boolean(false);
  private final String  orientation   = "column";
  private final int     numRecords    = 200;
  private final String TEST_TABLE = entityName;
  private DatabaseAdapter databaseAdapter = null;
  private DataManager dataManager = null;
    

  private static Logger log = Logger.getLogger(TableMonitorTest.class);

  /*
   * Constructors
   */

  /**
   * Because DataManagerTest is a subclass of TestCase, it must provide a
   * constructor with a String parameter.
   * 
   * @param name   the name of a test method to run
   */
  public TableMonitorTest(String name) {
    super(name);
  }
    
    
  /*
   * Class methods
   */
  
  /**
   * Create a suite of tests to be run together.
   */
  public static Test suite() {
    TestSuite testSuite = new TestSuite();
    
    testSuite.addTest(new TableMonitorTest("initialize"));
    testSuite.addTest(new TableMonitorTest("testAddTableEntry"));
    testSuite.addTest(new TableMonitorTest("testAssignTableName"));
    testSuite.addTest(new TableMonitorTest("testCountRows"));
    testSuite.addTest(new TableMonitorTest("testDropTableEntry"));
    testSuite.addTest(new TableMonitorTest("testGetCreationDate"));
    testSuite.addTest(new TableMonitorTest("testGetLastUsageDate"));
    testSuite.addTest(new TableMonitorTest("testGetOldestTable"));
    testSuite.addTest(new TableMonitorTest("testGetTableList"));
    testSuite.addTest(new TableMonitorTest("testIdentifierToTableName"));
    testSuite.addTest(new TableMonitorTest("testIsDBTableNameInUse"));
    testSuite.addTest(new TableMonitorTest("testIsTableInDB"));
    testSuite.addTest(new TableMonitorTest("testMangleName"));
    testSuite.addTest(new TableMonitorTest("testSetLastUsageDate"));
    testSuite.addTest(new TableMonitorTest("testSetTableExpirationPolicy"));
    
    return testSuite;
  }
    
    
  /*
   * Instance methods
   */
    
  /**
   * Run an initial test that always passes to check that the test harness
   * is working.
   */
  public void initialize() {
    assertTrue(1 == 1);
  }
    

  /**
   * Establish a testing framework by initializing appropriate objects.
   */
  public void setUp() throws Exception {
    super.setUp();
    connectionPool = new DatabaseConnectionPoolInterfaceTest();
    dbConnection = connectionPool.getConnection();
    dbAdapterName = connectionPool.getDBAdapterName();
    dataManager = DataManager.getInstance(connectionPool, dbAdapterName);

    entity = new Entity(packageId, id, entityName, description,
                        caseSensitive, orientation, numRecords);

    entityAncient = new Entity(packageId, idAncient, entityNameAncient, description,
        caseSensitive, orientation, numRecords);

    entityCurrent = new Entity(packageId, idCurrent, entityNameCurrent, description,
        caseSensitive, orientation, numRecords);
    
    if (dbAdapterName.equals(DatabaseAdapter.POSTGRES_ADAPTER)) {
	      databaseAdapter = new PostgresAdapter();
	    }
	    else if (dbAdapterName.equals(DatabaseAdapter.HSQL_ADAPTER)) {
	      databaseAdapter = new HSQLAdapter();
	    }
	    else if (dbAdapterName.equals(DatabaseAdapter.ORACLE_ADAPTER)) {
	      databaseAdapter = new OracleAdapter();
	      
	    }

    tableMonitor = new TableMonitor(databaseAdapter);
  }
    
    
  /**
   * Release any objects and closes database connections after tests 
   * are complete.
   */
  public void tearDown() throws Exception {
    connectionPool.returnConnection(dbConnection);
    connectionPool = null;
    dbConnection = null;
    dataManager = null;
    entity = null;
    entityAncient = null;
    entityCurrent = null;
    tableMonitor = null;
    super.tearDown();
  }
  

  /**
   * Tests the TableMonitor.addTableEntry() method. Does so by adding a table
   * entry for a test table and asserting that it has been entered,
   * and only one record for it exists in the data table registry.
   * 
   * @throws SQLException
   */
  public void testAddTableEntry() throws SQLException {
    boolean isPresent = false;
    String registry = tableMonitor.getDataTableRegistryName();
    ResultSet rs;
    String tableName;

    String queryStr = String.format(
        "SELECT * FROM %s WHERE TABLE_NAME=%s",
        SqlEscape.name(registry),
        SqlEscape.str(DatabaseAdapter.getLegalDBTableName(TEST_TABLE))
    );

    log.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    String cleanString =
        String.format("DELETE FROM %s WHERE TABLE_NAME=%s",
            SqlEscape.name(registry),
            SqlEscape.str(DatabaseAdapter.getLegalDBTableName(TEST_TABLE))
        );

    // First, clean-up any existing entry for the test table
    try {
      stmt = dbConnection.createStatement();             
      stmt.executeUpdate(cleanString);
    } 
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
    }

    // Next, tell TableMonitor to add the table entry for the test table
    tableName = tableMonitor.addTableEntry(entity);
    
    // Assert that the operation succeeded
    assertNotNull("Failed to add table entry", tableName);

    // Query the table registry. The table entry should be present, and only
    // one record of it should exist.
    try {
      stmt = dbConnection.createStatement();             
      rs = stmt.executeQuery(queryStr);
      
      int rowCount = 0;
      while (rs.next()) {
        rowCount++;
        String TABLE_NAME = rs.getString("TABLE_NAME");
        
        if (TABLE_NAME.equalsIgnoreCase(DatabaseAdapter.getLegalDBTableName(TEST_TABLE))) {
          isPresent = true;
        }
      }
      
      assertTrue("Table entry not present", isPresent);
      assertEquals("Multiple table entries found for "+DatabaseAdapter.getLegalDBTableName(TEST_TABLE), rowCount, 1);
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
    }
    finally {
      if (stmt != null) stmt.close();
    }
    
    // Clean-up any existing entries for the test table
    try {
      stmt = dbConnection.createStatement();             
      stmt.executeUpdate(cleanString);
    } 
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
    }

  }

  
  /**
   * Tests the TableMonitor.assignTableName() method. First uses a regular
   * entity name (no spaces or dashes) and then uses an irregular entity name
   * (containing spaces and dashes).
   * 
   * @throws SQLException
   */
  public void testAssignTableName() throws SQLException {
    String entityIdentifier = this.id;
    String entityName = this.entityName;
    String assignedTableName = null;
    String irregularEntityName = "entity With Spaces-And-Dashes";
    String irregularTableName = "entity With Spaces-And-Dashes";

    /* Get the assigned table name from the TableMonitor for a simple
     * entity name (no spaces or dashes). In this case, the assigned name
     * should be equal to the entityName.
     */
    assignedTableName = tableMonitor.assignTableName(entityIdentifier, entityName);
    assertEquals("Error assigning table name: ", DatabaseAdapter.getLegalDBTableName(entityName), assignedTableName);
    
    /* Get the assigned table name from the TableMonitor for an entity name
     * that contains spaces and dashes. In this case, the assigned name
     * should be equal to irregularName.
     */
    assignedTableName = tableMonitor.assignTableName(entityIdentifier, irregularEntityName);
    String expectedTableName = DatabaseAdapter.getLegalDBTableName(irregularTableName);
    assertEquals("Error assigning table name: ", expectedTableName, assignedTableName);
    
  }
  

  /**
   * Tests the TableMonitor.countRows() method. Create a table containing a
   * known number of rows. Run countRows() on the table and save the returned
   * value. Drop the table. Then assert that the returned value equals the known
   * number of rows. (The table should be dropped before the assert, in case the
   * assert fails.)
   * 
   * @throws SQLException
   */
  public void testCountRows() throws SQLException {
    String testTable = DatabaseAdapter.getLegalDBTableName("TEST_COUNT_ROWS");
    String createString =
        String.format("create table %s (ROW_NUMBER int)", SqlEscape.name(testTable));
    String dropString = String.format("drop table %s", SqlEscape.name(testTable));
    String queryStr = String.format("insert into %s values (0)", SqlEscape.name(testTable));
    log.debug("queryStr: " + queryStr);

    int nRows = 3;
    int rowCount;
    Statement stmt = null;

    /* First, create a test table */
    try {
      stmt = dbConnection.createStatement();
      stmt.executeUpdate(createString);
    } 
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw (e);
    } 
    finally {
      if (stmt != null)
        stmt.close();
    }

    /* Next, insert a known number of rows into the test table */
    for (int i = 0; i < nRows; i++) {
      try {
        stmt = dbConnection.createStatement();
        stmt.executeUpdate(queryStr);
      } 
      catch (SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        throw (e);
      } 
      finally {
        if (stmt != null)
          stmt.close();
      }
    }
 
    /* Get the row count from tableMonitor.countRows() */
    rowCount = tableMonitor.countRows(testTable);

    try {
      stmt = dbConnection.createStatement();
      stmt.executeUpdate(dropString);
    } 
    catch (SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw (e);
    } 
    finally {
      if (stmt != null)
        stmt.close();
    }
    
    assertEquals("countRows() returned unexpected row count: ", 
                 nRows, rowCount);
  }
  
 
  /**
   * Tests the TableMonitor.dropTableEntry() method. Does so by adding a table
   * entry for a test table and asserting that it has been entered,
   * and only one record for it exists in the data table registry.
   * 
   * @throws SQLException
   */
  public void testDropTableEntry() throws SQLException {
    Date now = new Date();
    String registry = tableMonitor.getDataTableRegistryName();
    boolean success;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Statement stmt = null;

    String queryStr =
        String.format("INSERT INTO %s values(%s, %s, %s, %s, %s, %s, 1)",
            SqlEscape.name(registry),
            SqlEscape.str(DatabaseAdapter.getLegalDBTableName(TEST_TABLE)),
            SqlEscape.str(packageId),
            SqlEscape.str(id),
            SqlEscape.str(entityName),
            SqlEscape.str(simpleDateFormat.format(now)),
            SqlEscape.str(simpleDateFormat.format(now))
        );

    log.debug("queryStr: " + queryStr);

    // First, insert an entry for the test table
    try {
      stmt = dbConnection.createStatement();             
      stmt.executeUpdate(queryStr);
    } 
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
    }

    // Next, tell TableMonitor to drop the table entry for the test table
    success = tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
    
    // Assert that one row was successfully deleted
    assertTrue("Failed to drop table entry", success);
  }
  

  /**
   * Tests the TableMonitor.getCreationDate() method. Adds a data table entry,
   * retrieves the entry's creation date, and compares it to today's date
   * (they should be equal). Then cleans up by dropping the data table entry.
   */
  public void testGetCreationDate() throws SQLException {
    Date creationDate;
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String nowDateString = simpleDateFormat.format(now);

    // Add the test table entry. By default, creation date is today's date.
    tableMonitor.addTableEntry(entity);
    
    // Retrieve the creation date from the data table registry
    creationDate = tableMonitor.getCreationDate(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
    String creationDateString = simpleDateFormat.format(creationDate);

    // Assert that creation date should be equal to today's date
    assertTrue("Creation date should equal today's date: " + 
               creationDateString + " " + nowDateString, 
               creationDateString.equals(nowDateString));
    
    // Clean-up the test table entry
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
  }
    
 
  /**
   * Tests the TableMonitor.getLastUsageDate() method. Adds a data table entry,
   * retrieves the entry's last usage date, and compares it to today's date
   * (they should be equal). Then cleans up by dropping the data table entry.
   */
  public void testGetLastUsageDate() throws SQLException {
    Date lastUsageDate;
    Date now = new Date();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String nowDateString = simpleDateFormat.format(now);

    // Add the test table entry. By default, creation date is today's date.
    tableMonitor.addTableEntry(entity);
    
    // Retrieve the creation date from the data table registry
    lastUsageDate = tableMonitor.getCreationDate(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
    String lastUsageDateString = simpleDateFormat.format(lastUsageDate);

    // Assert that last usage date should be equal to today's date
    assertTrue("Last usage date should equal today's date: " + 
               lastUsageDateString + " " + nowDateString, 
               lastUsageDateString.equals(nowDateString));
    
    // Clean-up the test table entry
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
  }
  

  /**
   * Tests the TableMonitor.getOldestTable() method. Add two table entries to 
   * the data table registry. Set the last usage date for the first one 
   * to a very old date, the other to the current date. Test that 
   * getOldestTable() returns the name of the table with the very old last
   * usage date.
   * 
   * @throws SQLException
   */
  public void testGetOldestTable() throws SQLException {
    long unixEpoch = 0;
    Date ancientDate = new Date(unixEpoch);
    Date now = new Date();
    
    tableMonitor.addTableEntry(entityAncient);
    tableMonitor.setLastUsageDate(DatabaseAdapter.getLegalDBTableName(entityNameAncient), ancientDate);

    tableMonitor.addTableEntry(entityCurrent);
    tableMonitor.setLastUsageDate(DatabaseAdapter.getLegalDBTableName(entityNameCurrent), now);

    String oldestTable = tableMonitor.getOldestTable();  
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(entityNameAncient));
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(entityNameCurrent));
    
    assertEquals("Did not find the oldest table", oldestTable,
            DatabaseAdapter.getLegalDBTableName(entityNameAncient));
  }
  

  /**
   * Tests the TableMonitor.getTableList() method. Adds a table entry for the
   * test table, then gets the table list and asserts that the test table has
   * been found. Then drops the table entry, gets the table list a second time,
   * and asserts that the test table was not found in the table list.
   * 
   * @throws SQLException
   */
  public void testGetTableList() throws SQLException {
    boolean found = false;
    
    tableMonitor.addTableEntry(entity);
    String[] tableList = tableMonitor.getTableList();
    
    for (int i = 0; i < tableList.length; i++) {
      found = found || (tableList[i].equalsIgnoreCase(DatabaseAdapter.getLegalDBTableName(TEST_TABLE)));
    }
    
    assertTrue("Did not find " + DatabaseAdapter.getLegalDBTableName(TEST_TABLE) + " in table list", found);

    found = false;
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
    tableList = tableMonitor.getTableList();
    
    for (int i = 0; i < tableList.length; i++) {
      found = found || (tableList[i].equalsIgnoreCase(DatabaseAdapter.getLegalDBTableName(TEST_TABLE)));
    }
    
    assertFalse("Found " + DatabaseAdapter.getLegalDBTableName(TEST_TABLE) +
                " in table list, but it should have been dropped" , 
                found);
  }
  
 
  /**
   * Tests the TableMonitor.identifierToTableName() method. First add a table
   * entry for an entity, returning a table name. Then check that the entity
   * identify can be used to retrieve the same table name.
   *
   */
  public void testIdentifierToTableName() throws SQLException {
    String identifier = entity.getEntityIdentifier();
    String returnedTableName = null;
    String addedTableName = null;
    
    // First, tell TableMonitor to add the table entry for the test table
    addedTableName = tableMonitor.addTableEntry(entity);
    
    // Assert that the addTableEntry() operation succeeded
    assertNotNull("Failed to add table entry", addedTableName);

    // Next, get the table name corresponding to this identifier
    returnedTableName = tableMonitor.identifierToTableName(identifier);

    // Assert that the table name that was added equals the table name
    // associated with this identifier.
    //
    assertEquals("Error getting correct table name from identifier: ",
                 addedTableName, returnedTableName);

    // Clean-up by dropping the table name that was added.
    tableMonitor.dropTableEntry(addedTableName);
  }
  

  /**
   * Tests the TableMonitor.isDBTableNameInUse() method. First, add a table
   * entry then assert that the table name is in use. Next, drop the table
   * entry and assert that the table name is not in use.
   * 
   * @throws SQLException
   */
  public void testIsDBTableNameInUse() throws SQLException {
    boolean inUse;
    
    // First, tell TableMonitor to add the table entry for the test table
    String addedTableName = tableMonitor.addTableEntry(entity);
    
    // Assert that the addTableEntry() operation succeeded
    assertNotNull("Failed to add table entry", addedTableName);

    // Next, assert that the added table name is now in use
    inUse = tableMonitor.isDBTableNameInUse(addedTableName);
    assertTrue("inUse is false but should be true: ", inUse);

    // Clean-up by dropping the table name that was added.
    tableMonitor.dropTableEntry(addedTableName);
    
    // Now assert that inUse is false
    inUse = tableMonitor.isDBTableNameInUse(addedTableName);
    assertFalse("inUse is true but should be false: ", inUse);
  }
    
 
  /**
   * Tests the TableMonitor.isTableInDB() method. Does so by creating a test
   * table. First drops the table in case it was already
   * present. Then creates the table, calls isTableInDB(), and asserts that
   * the table exists. Then drops the table again, calls isTableInDB(), and
   * asserts that the table does not exist.
   * 
   * @throws SQLException
   */
  public void testIsTableInDB() throws SQLException {
    String createString = String.format(
        "create table %s (COFFEE_NAME varchar(32), SUPPLIER_ID int, PRICE float, SALES int, TOTAL int)",
        DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
    String dropString = String.format("DROP TABLE %s", DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
    boolean isPresent;
    Statement stmt = null;

    try {
      stmt = dbConnection.createStatement();             
      stmt.executeUpdate(dropString);
    }
    catch(SQLException e) {
      // Ignore error if test table can't be dropped at this point.
    }
    finally {
      if (stmt != null) stmt.close();
    }

    try {
      stmt = dbConnection.createStatement();             
      stmt.executeUpdate(createString);
      isPresent = tableMonitor.isTableInDB(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
      assertTrue("Could not find table " + 
                 DatabaseAdapter.getLegalDBTableName(TEST_TABLE) + " but it should be in db",
                 isPresent);
    } 
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
      throw(e);
    }
    finally {
      if (stmt != null) stmt.close();
    }
   
    try {
      stmt = dbConnection.createStatement();             
      stmt.executeUpdate(dropString);
      isPresent = tableMonitor.isTableInDB(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
      assertFalse("Found table " + DatabaseAdapter.getLegalDBTableName(TEST_TABLE) + " but it should not be in db",
                  isPresent);
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
    }
    finally {
      if (stmt != null) stmt.close();
    }

  }
  
 
  /**
   * Test the TableMonitor.mangleName() method. Compare the returned 
   * string value to an expected string value.
   */
  public void testMangleName() {
    String tableName = "aTable";
    String expectedName = "aTable_XYZYX_1";
    
    String mangledName = tableMonitor.mangleName(tableName);
    assertEquals("Error testing mangleName(): " + 
                 expectedName + " != " + mangledName + "\n", 
                 expectedName, mangledName);

    tableName = "aTable_XYZYX_1";
    expectedName = "aTable_XYZYX_2";
    mangledName = tableMonitor.mangleName(tableName);
    assertEquals("Error testing mangleName(): " + 
        expectedName + " != " + mangledName + "\n", 
        expectedName, mangledName);
  }
  

  /**
   * Tests the TableMonitor.setLastUsageDate() method. First adds a table entry 
   * for a test table, calls setLastUsageDate() for a known date, then queries
   * the data table registry to check that the found date matches the known
   * date.
   * 
   * @throws SQLException
   */
  public void testSetLastUsageDate() throws SQLException {
    boolean success;
    long epochMilliseconds = 1156979161000l;
    String dataTableRegistryName = tableMonitor.getDataTableRegistryName();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    Date testDate = new Date(epochMilliseconds);
    String queryStr = String.format(
        "SELECT last_usage_date FROM %s WHERE table_name=%s",
        SqlEscape.name(dataTableRegistryName),
        SqlEscape.str(DatabaseAdapter.getLegalDBTableName(TEST_TABLE))
    );

    log.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    tableMonitor.addTableEntry(entity);
    success = tableMonitor.setLastUsageDate(DatabaseAdapter.getLegalDBTableName(TEST_TABLE), testDate);
    
    assertTrue("tableMonitor.setLastUsageDate() did not succeed. ", success);
    
    // Query the table registry to determine the last_usage_date value for
    // the test table and compare it to the known value
    try {
      Date foundDate = null;
      stmt = dbConnection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        foundDate = rs.getDate("last_usage_date");
      }
      
      String twoDates = "testDate = " + simpleDateFormat.format(testDate) 
                        + ", "
                        + "foundDate = " + simpleDateFormat.format(foundDate)
                        + ". ";
      assertEquals("Last usage date found does not match test value: " + 
                   twoDates, 
                   simpleDateFormat.format(testDate),
                   simpleDateFormat.format(foundDate));
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
    }
    finally {
      if (stmt != null) stmt.close();
    }

    // Clean-up table entry for test table
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
  }
  
  
  /**
   * Tests the TableMonitor.setTableExpirationPolicy() method. First adds a 
   * table entry for a test table, calls setTableExpirationPolicy() with a known
   * priority value, then queries the data table registry to check that the 
   * found priority value matches the known priority value.
   * 
   * @throws SQLException
   */
  public void testSetTableExpirationPolicy() throws SQLException {
    boolean success;
    int testPriority = 1;
    String dataTableRegistryName = tableMonitor.getDataTableRegistryName();
    String queryStr = String.format(
        "SELECT priority FROM %s WHERE table_name=%s",
        SqlEscape.name(dataTableRegistryName),
        SqlEscape.str(DatabaseAdapter.getLegalDBTableName(TEST_TABLE))
    );

    log.debug("queryStr: " + queryStr);

    Statement stmt = null;
    
    tableMonitor.addTableEntry(entity);
    success = tableMonitor.setTableExpirationPolicy(DatabaseAdapter.getLegalDBTableName(TEST_TABLE), testPriority);
    
    assertTrue("tableMonitor.setTableExpirationPolicy() did not succeed. ", 
               success);
    
    // Query the table registry to determine the priority value for
    // the test table and compare it to the known priority value
    try {
      int foundPriority = -99;
      stmt = dbConnection.createStatement();             
      ResultSet rs = stmt.executeQuery(queryStr);
      
      while (rs.next()) {
        foundPriority = rs.getInt("priority");
      }
      
      assertEquals("Priority found does not match test value: ",
                   foundPriority, testPriority);
    }
    catch(SQLException e) {
      System.err.println("SQLException: " + e.getMessage());
    }
    finally {
      if (stmt != null) stmt.close();
    }

    // Clean-up table entry for test table
    tableMonitor.dropTableEntry(DatabaseAdapter.getLegalDBTableName(TEST_TABLE));
  }
  
}
