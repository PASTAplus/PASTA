package edu.lternet.pasta.dml.download;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit test class for the DownloadHandler class.
 * 
 * @author tao
 *
 */
public class DownloadHandlerTest extends TestCase
{
    /*
     * Class fields
     */
  
	private static final String METADATA_URL   = "https://pasta-s.lternet.edu/package/metadata/eml/knb-lter-arc/1424/5";
	private static final long METADATA_EXPECTED_SIZE = 27550; // Expected size of metadata file
	private static final String INCORRECT_METADATA_URL = "https://pasta-s.lternet.edu/package/metadata/eml/knb-lter-arc/1424/1";
	private static final String DATA_URL = "https://pasta-s.lternet.edu/package/data/eml/knb-lter-arc/1424/5/dec159de2bffcb968c7f3084a838ca18";
	private static final long DATA_EXPECTED_SIZE = 40309; // Expected size of data file

    
    /*
     * Instance fields
     */    
    private EcogridEndPointInterfaceTest endPointInfo = new EcogridEndPointInterfaceTest();

    
    /**
	 * Constructor 
	 * @param name The name of testing
	 */
	  public DownloadHandlerTest (String name)
	  {
	    super(name);
	  }
      
      
      /*
       * Class methods
       */
      
      /**
       * Create a suite of tests to be run together
       */
       public static Test suite()
       {
         TestSuite suite = new TestSuite();
         suite.addTest(new DownloadHandlerTest("initialize"));
         suite.addTest(new DownloadHandlerTest("testDownloadFailed"));
         suite.addTest(new DownloadHandlerTest("testDownloadSuccess"));  
         suite.addTest(new DownloadHandlerTest("tesDownloadHandlerWithSameUrl"));
         suite.addTest(new DownloadHandlerTest("testDownloadFromIncorrectURL"));
         suite.addTest(new DownloadHandlerTest("testCorrectURLByDownload"));
         suite.addTest(new DownloadHandlerTest("testInCorrectURLByDownload"));
         suite.addTest(new DownloadHandlerTest("testSameURLByDownload"));
         return suite;
       }

       
      /*
       * Instance methods
       */
      
	  /**
	   * Establish a testing framework by initializing appropriate objects.
	   */
	  protected void setUp() throws Exception
	  {
	    super.setUp();
	    
	  }
      
      
	  /**
	   * Release any objects and closes database connections after tests 
	   * are complete.
	   */
	  protected void tearDown() throws Exception
	  {
	    super.tearDown();
	  }
	  
      
      /**
       * Run an initial test that always passes to check that the test harness
       * is working.
       */
      public void initialize() {
        assertTrue(1 == 1);
      }
        
        
	  /**
	   * Tests downloading from http protocol with successful result.
	   *
	   */
	  public void testDownloadSuccess()
	  {
		  testDownloadByThread(true, METADATA_URL, METADATA_URL, true);
	  }
	  
      
	  /**
	   * Tests downloading from http protocol with failed result
       * (no StorageInterface).
	   * 
	   */
	  public void testDownloadFailed()
	  {
		  String url = "https://knb.ecoinformatics.org/knb/metacat/tao.1.1";
		  testDownloadByThread(false, url, url, false);
	  }
	  
      
	  /**
	   * Tests downloading from http protocol with failed result
     * (from incorrect url).
	   * 
	   */
	  public void testDownloadFromIncorrectURL()
	  {
		  testDownloadByThread(false, INCORRECT_METADATA_URL, INCORRECT_METADATA_URL, true);
	  }
      
      
	  /*
	   * Tests download process by creating a thread which initalizes from 
       * download handler.
	   */
	  private void testDownloadByThread(boolean success, 
                                        String url, 
                                        String identifier, 
                                        boolean hasDataStorage)
	  {	  
      System.err.printf("url: %s,  identifier: %s\n", url, identifier);
      System.err.println("Starting DownloadHandler.");
		  DownloadHandler handler = 
                                 DownloadHandler.getInstance(url, endPointInfo);
		  DataStorageTest dataStorage = new DataStorageTest();
          
		  if (hasDataStorage)
		  {
		    DataStorageTest[] list = new DataStorageTest[1];
		    list[0] = dataStorage;
		    handler.setDataStorageClassList(list);
		  }
          
		  assertTrue(handler.isBusy() == false);
		  assertTrue(handler.isSuccess() == false);
		  Thread downloadThread = new Thread(handler);
	    System.err.println("Starting DownloadHandler.");
		  downloadThread.start();
          
		  while(!handler.isCompleted())
		  {
	      System.err.println("Waiting for DownloadHandler to complete.");
	      try {
	       Thread.sleep(1000);
	      }
	      catch (InterruptedException e) {
	        System.err.println(e.getMessage());
	      }
		  }
	    System.err.println("DownloadHandler finished.");
          
		  //assertTrue(handler.isSuccess() == true);
          
		  if (success)
		  {
			  assertTrue("The data should exist only if the handler returned a success status",
			             dataStorage.doesDataExist(identifier) == true);
			  assertTrue("The handler returned a failed status but was expected to succeed",
			             handler.isSuccess() == true);
              
			  if (identifier == METADATA_URL)
			  {
			    long fileSize = dataStorage.getEntitySize(identifier);
				  assertTrue("The file size was not the expected value", 
				             fileSize == METADATA_EXPECTED_SIZE);
	        System.err.println("expected: " + METADATA_EXPECTED_SIZE + "; found: " + fileSize);
			  }		  
		  }
		  else
		  {
			  //assertTrue(dataStorage.doesDataExist(identifier) == false);
			  assertTrue("The handler returned a success status but was expected to fail",
			             handler.isSuccess() == false);
		  }
          
		  assertTrue("The handler reports that it is busy but it should not be busy", 
		             handler.isBusy() == false);
	  }
	  
	  
	  /**
	   * Test two DownloadHandler with same url.
	   *
	   */
	  public void tesDownloadHandlerWithSameUrl()
	  {
		  processDownloadHandlersWithSameUrl(METADATA_URL);
	  }
	  
	  
	  /*
	   * Process two downloadhandler with same url.
	   */
	  private void processDownloadHandlersWithSameUrl(String url)
	  {
		  
		  DownloadHandler handler = 
                                 DownloadHandler.getInstance(url, endPointInfo);
		  DataStorageTest dataStorage = new DataStorageTest();
     	  DataStorageTest[] list = new DataStorageTest[1];
		  list[0] = dataStorage;
		  handler.setDataStorageClassList(list);
		  
		  assertTrue(handler.isBusy() == false);
		  assertTrue(handler.isSuccess() == false);
		  Thread downloadThread = new Thread(handler);
		  downloadThread.start();
		  // start the second handler
		  DownloadHandler handler2 = 
                                 DownloadHandler.getInstance(url, endPointInfo);
		  handler2.setDataStorageClassList(list);
		  Thread downloadThread2 = new Thread(handler2);
		  downloadThread2.start();
		  //assertTrue(handler == handler2);
		  System.err.println("the handler is "+handler);
		  System.err.println("the handler2 is "+handler2);
          
		  try {
		  while(!handler.isCompleted())
		  {
	      System.err.println("Waiting for DownloadHandler to complete.");
	      Thread.sleep(1000);
		  }
          
		  while(!handler2.isCompleted())
		  {
	      System.err.println("Waiting for DownloadHandler to complete.");
	      Thread.sleep(1000);
		  }
		  }
		  catch (InterruptedException e) {
        System.err.println(e.getMessage());
		  }
          
		  System.err.println("the handler is ===="+handler);
		  System.err.println("the handler2 is ===="+handler2);
		  //assertTrue(handler.isSuccess() == true);
		  assertTrue(dataStorage.doesDataExist(url) == true);
	      assertTrue(handler.isSuccess() == true);
		  assertTrue(handler.isBusy() == false);
		  assertTrue(handler2.isSuccess() == true);
		  assertTrue(handler2.isBusy() == false);
	  }
	  
      
	  /**
	   * Test download method by a correct url.
       * 
	   * @throws Exception
	   */
	  public void testCorrectURLByDownload() throws Exception
	  {
		  String url = METADATA_URL;
		  testDownloadMethod(true, url);
	  }
	  
      
	  /**
	   * Tests download method with an incorrect url.
       * 
	   * @throws Exception
	   */
	  public void testInCorrectURLByDownload() throws Exception
	  {
		  String url = INCORRECT_METADATA_URL;
		  testDownloadMethod(false, url);
	  }
	  
      
	  /**
	   * Tests download method by calling download same url.
       * 
	   * @throws Exception
	   */
	  public void testSameURLByDownload() throws Exception
	  {
		  DataStorageTest dataStorage = new DataStorageTest();
		  DataStorageTest[] list = new DataStorageTest[1];
		  list[0] = dataStorage;
		  DownloadHandler handler = DownloadHandler.getInstance(DATA_URL, endPointInfo);
		  DownloadHandler handler1 = DownloadHandler.getInstance(DATA_URL, endPointInfo);
		  boolean result1 = handler.download(list);
		  boolean result2 = handler1.download(list);
		  assertTrue(result1 == true);
		  assertTrue(result2 == true);
		  assertTrue(dataStorage.doesDataExist(DATA_URL) == true);
		  long fileSize = dataStorage.getEntitySize(DATA_URL);
		  System.err.println("expected: " + DATA_EXPECTED_SIZE + "; found: " + fileSize);
		  assertTrue(fileSize == DATA_EXPECTED_SIZE);
	  }
	  
      
	  /*
	   * This method will test download method in DownloadHandler.
	   */
	  private void testDownloadMethod(boolean success, String url) 
               throws Exception
	  {
		  DownloadHandler handler = 
                                 DownloadHandler.getInstance(url, endPointInfo);
		  DataStorageTest dataStorage = new DataStorageTest();
		  DataStorageTest[] list = new DataStorageTest[1];
		  list[0] = dataStorage;
		  boolean result = false;
          
		  try
		  {
	        result = handler.download(list);
		  }
		  catch (Exception e)
		  {
			  if (url == INCORRECT_METADATA_URL)
			  {
		        assertTrue("The exception should be an instance of " +
                           "DataSourceNotFoundException", 
						   e instanceof DataSourceNotFoundException);
			  }
		  }
          
		  if (url == METADATA_URL)
		  {
		    long fileSize = dataStorage.getEntitySize(url);
		    System.err.println("expected: " + METADATA_EXPECTED_SIZE + "; found: " + fileSize);
			  assertTrue(fileSize == METADATA_EXPECTED_SIZE);
		  }
          
		  assertTrue("The handler reports that it is busy but it should not be busy", 
		             handler.isBusy() == false);
		  assertTrue("The handler returned '" + result + "' but the expected return value was '" + success + "'",
		             result == success);
		  assertTrue("The data should exist only if the handler returned a success status", 
		             dataStorage.doesDataExist(url) == success);
	  }
	  
}
