package edu.lternet.pasta.dml.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.MalformedURLException;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.dml.quality.QualityReport;
import edu.ucsb.nceas.utilities.Options;


public class DatabaseAdapterTest {

    private static ConfigurationListener configurationListener = null;
    private static final String dirPath = "WebRoot/WEB-INF/conf";
    private static Options options = null;
    private static String preferredFormatStringsURL = null;

    /**
     * Initialize objects before any tests are run.
     */
    @BeforeClass
    public static void setUpClass() {
      configurationListener = new ConfigurationListener();
      configurationListener.initialize(dirPath);
      options = ConfigurationListener.getOptions();
      
      if (options == null) {
        fail("Failed to load DataPackageManager properties file");
      }
      else {
          preferredFormatStringsURL = options.getOption("dml.preferredFormatStringsURL");
        if (preferredFormatStringsURL == null) {
            fail("No value found for DataPackageManager property 'dml.preferredFormatStringsURL'");
        }
      }
    }
    
    
  /**
   * Initialize objects before each test is run.
   */
  @Before
  public void setUpTest() {
    try {
        QualityReport.setPreferredFormatStrings(preferredFormatStringsURL);
    }
    catch (MalformedURLException e) {
        fail("MalformedURLException while loading preferred datetime format strings: " + e.getMessage());
      }
    catch (IOException e) {
        fail("IOException while loading preferred datetime format strings: " + e.getMessage());
    }
  }

  
  /**
   * Test DatabaseAdapter.getLegalDBTableName() method. For each string in a 
   * list of bad (i.e. illegal) table names, ensure that the method returns 
   * the expected (i.e. legal) table name.
   */
  @Test public void testGetLegalDBTableName() {
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
  
  
  @Test public void testFormatStringMatchesDataValue() {
	    String[] testData =
	    	{
	    			"YYYY-MM-DD,1976-09-23",
	    			"YYYY-MM-DD hh:mm:ss.sss,1976-09-23 12:30:30.000",
	    			"YYYY-MM-DDThh:mm:ss.sss,1976-09-23T12:30:30.000",
	    			"YYYY-MM-DD hh:mm:ss.sssZ,1976-09-23 12:30:30.000Z",
	    			"YYYY-MM-DD hh:mm:ss.sss+hh,1976-09-23 10:30:30.000+10",
	    			"YYYY,1976",
	    			"YYYY-MM-DD hh:mm:ss,1976-09-23 12:30:30",
	    			"YYYY-MM-DDThh:mm,1976-09-23T12:30",
	    			"YYYY-MM-DDThh:mm:ss,1976-09-23T12:59:50",
	    			"YYYY-MM,1976-09",
	    			"YYYY-MM-DD hh:mm,1976-09-23 12:30",
	    			"YYYYMM,197609",
	    			"YYYYDDD,1976189",
	    			"YYYYMMDD,19760923"
	        };
	      
        for (int i = 0; i < testData.length; i++) {
            String[] pair = testData[i].split(",");
            assertTrue("Bad test data value: " + testData[i], (pair != null && pair.length == 2));
            String formatStr = pair[0];
            String dateStr = pair[1];
            String msg = DatabaseAdapter.formatStringMatchesDataValue(formatStr, dateStr);
            assertTrue(msg, (msg == null));
        }
     }


  /**
   * Release any objects after all tests are complete.
   */
  @AfterClass
  public static void tearDownClass() {
    configurationListener = null;
    options = null;
  }
  
}
