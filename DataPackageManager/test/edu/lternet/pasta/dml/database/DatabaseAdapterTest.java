package edu.lternet.pasta.dml.database;

import java.util.ResourceBundle;

import edu.lternet.pasta.dml.quality.QualityReport;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


public class DatabaseAdapterTest extends TestCase {

    private static final String CONFIG_NAME = "datapackagemanager";
    private static ResourceBundle options = null;
    private static String preferredFormatStringsURL = null;

    /**
     * Loads Data Manager options from a configuration file.
     */
    private static void loadOptions() {
      try {
        // Load options
        options = ResourceBundle.getBundle(CONFIG_NAME);
        // URL for the CSV list of preferred format strings and corresponding regular expressions
        preferredFormatStringsURL = options.getString("dml.preferredFormatStringsURL");
      }
      catch (Exception e) {
        System.err.println("Error in loading options: " + e.getMessage());
      }
    }


  /**
   * Constructor 
   * @param name The name of testing
   */
  public DatabaseAdapterTest(String name) {
    super(name);
  }


  /**
   * Establish a testing framework by initializing appropriate objects.
   */
  protected void setUp() throws Exception {
      loadOptions();
      QualityReport.setPreferredFormatStrings(preferredFormatStringsURL);
  }


  /**
   * Release any objects and closes database connections after tests 
   * are complete.
   */
  protected void tearDown() throws Exception {

    super.tearDown();
  }


  /**
   * Test DatabaseAdapter.getLegalDBTableName() method. For each string in a 
   * list of bad (i.e. illegal) table names, ensure that the method returns 
   * the expected (i.e. legal) table name.
   */
  public void testGetLegalDBTableName() {
    String[] badNames = 
      {"table name", "table-name", "table.name", "1040 Forms"};
    
    String[] expectedNames = 
      {"table_name", "table_name", "table_name", "_1040_Forms"};
    
    for (int i = 0; i < badNames.length; i++) {
      String legalName = DatabaseAdapter.getLegalDBTableName(badNames[i]);
      assertEquals("Returned table name does not match expected name",
                   legalName, expectedNames[i]);
    }
  }
  
  
  
    public void testFormatStringMatchesDataValue() {
	    String[] formatStrings =
	    	{
	    			"YYYY-MM-DD",
	    			"YYYY-MM-DD hh:mm:ss.sss",
	    			"YYYY-MM-DDThh:mm:ss.sss",
	    			"YYYY-MM-DD hh:mm:ss.sssZ",
	    			"YYYY-MM-DD hh:mm:ss.sss+hh",
	    			"YYYY",
	    			"YYYY-MM-DD hh:mm:ss",
	    			"YYYY-MM-DDThh:mm",
	    			"YYYY-MM-DDThh:mm:ss",
	    			"YYYY-MM",
	    			"YYYY-MM-DD hh:mm",
	    			"YYYYMM",
	    			"YYYYDDD",
	    			"YYYYMMDD",
	        };
	      
	    String[] dataValues = 
		  {
				  "1976-09-23",
				  "1976-09-23 12:30:30.000",
				  "1976-09-23T12:30:30.000",
				  "1976-09-23 12:30:30.000Z",
				  "1976-09-23 10:30:30.000+10",
				  "1976",
				  "1976-09-23 12:30:30",
				  "1976-09-23T12:30",
				  "1976-09-23T12:59:50",
				  "1976-09",
				  "1976-09-23 12:30",
				  "197609",
				  "1976189",
				  "19760923",
				  "19760923",
		  };
	    
	    for (int i = 0; i < formatStrings.length; i++) {
	    	String formatStr = formatStrings[i];
	    	String dateStr = dataValues[i];
	        String msg = DatabaseAdapter.formatStringMatchesDataValue(formatStr, dateStr);
	        assertTrue(msg, (msg == null));
	    }
 }


  /**
   * Create a suite of tests to be run together
   */
  public static Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(new DatabaseAdapterTest("testGetLegalDBTableName"));
    suite.addTest(new DatabaseAdapterTest("testFormatStringMatchesDataValue"));
    return suite;
  }
  
}
