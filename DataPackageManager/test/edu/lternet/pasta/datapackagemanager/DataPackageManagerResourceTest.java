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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.utility.PastaUtility;
import edu.ucsb.nceas.utilities.IOUtil;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author dcosta
 * 
 * Tests the operations of the DataPackageManager class.
 *
 */
public class DataPackageManagerResourceTest {

  /*
   * Class fields
   */
  
  private static ConfigurationListener configurationListener = null;
  private static DataPackageManagerResource dataPackageManagerResource;
  private static DataPackageManager dataPackageManager;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static boolean isWindowsPlatform = false;
  private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
  private static final String testUserAcl = "uid=gmn-pasta,o=EDI,dc=edirepository,dc=org";
  private static Options options = null;
  private static String testDoi = null;
  private static File testEmlFile = null;
  private static File testEmlFile_2_2 = null;
  private static String testEmlFileName = null;
  private static String testEmlFileName_2_2 = null;
  private static String testPath = null;
  private static String testScope = null;
  private static String testScopeBogus = null;
  private static Integer testIdentifier = null;
  private static Integer testIdentifier_2_2 = null;
  private static String testIdentifierStr = null;
  private static Integer testRevision = null;
  private static String testRevisionStr = null;
  private static Integer testUpdateRevision = null;
  private static String testEntityId = null;
  private static String testEntityName = null;
  private static String testEntityId2 = null;   // a second test entity
  private static String testEntityName2 = null; // a second test entity
  private static String testEntitySize = null;
  private static String testEntitySize2 = null;
  private static String testMaxIdleTimeStr = null;
  private static Integer testMaxIdleTime = null;
  private static String testIdleSleepTimeStr = null;
  private static Integer testIdleSleepTime = null;
  private static String testInitialSleepTimeStr = null;
  private static Integer testInitialSleepTime = null;
  private static String testPackageId = null;
  private static String testPackageId_2_2 = null;

  private static final String ACL_START_TEXT = "<access:access";
  private static final String ACL_START_TEXT_2_2 = "<access:access xmlns:access=\"https://eml.ecoinformatics.org/access-2.2.0\"";
  private static final String ACL_END_TEXT = "</access:access>";
  private static final String RMD_START_TEXT = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<resourceMetadata>";
  private static final String RMD_END_TEXT = "</resourceMetadata>";
  private static final String METADATA_FORMAT_START_TEXT = "eml://ecoinformatics.org/eml-2.";
  private static final String SOLR_QUERY = "LTER";
  private static final String RMD_TEST_SCOPE = "knb-lter-nin";
  private static final Integer RMD_TEST_IDENTIFIER = new Integer(1);
  private static final Integer RMD_TEST_REVISION = new Integer(1);
  private static final String RMD_TEST_ENTITY_ID = "67e99349d1666e6f4955e9dda42c3cc2";

  private static final Integer ACL_TEST_IDENTIFIER_OFFSET = 1000000;

  
  /*
   * Instance fields
   */
  
  private String transaction = null;
  private String transactionBogus = "1000000000000";


  /*
   * Constructors
   */
  
  
  /*
   * Class methods
   */
  
  public static void modifyTestEmlFile(String testScope, File testEmlFile, String newPackageId) {
    String xmlString = FileUtility.fileToString(testEmlFile);
    Pattern pattern = Pattern.compile(testScope + "\\.\\d+\\.\\d+");
    Matcher matcher = pattern.matcher(xmlString);  
    // Replace packageId value with new packageId value
    String modifiedXmlString = matcher.replaceAll(newPackageId);          
    FileWriter fileWriter;

    try {
      fileWriter = new FileWriter(testEmlFile);
      StringBuffer stringBuffer = new StringBuffer(modifiedXmlString);
      IOUtil.writeToWriter(stringBuffer, fileWriter, true);
    }
    catch (IOException e) {
      fail("IOException modifying packageId in test EML file: " + e.getMessage());
    }
  }
  

  /**
   * Initialize objects before any tests are run.
   */
  @BeforeClass
  public static void setUpClass() {
    configurationListener = new ConfigurationListener();
    configurationListener.initialize(dirPath);
    options = ConfigurationListener.getOptions();
    
    String platformName = System.getProperty("os.name");
    System.err.println(String.format("os.name property: %s", platformName));
    if (platformName.startsWith("Windows")) {
    	isWindowsPlatform = true;
    }
    
    if (options == null) {
      fail("Failed to load DataPackageManager properties file");
    }
    else {
      testScope = options.getOption("datapackagemanager.test.scope");
      if (testScope == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.scope'");
      }
      testScopeBogus = options.getOption("datapackagemanager.test.scope.bogus");
      if (testScopeBogus == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.scope.bogus'");
      }
      testIdentifierStr = options.getOption("datapackagemanager.test.identifier");
      if (testIdentifierStr == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.identifier'");
      }
      testRevisionStr = options.getOption("datapackagemanager.test.revision");
      if (testRevisionStr == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.revision'");
      }
      testDoi = options.getOption("datapackagemanager.test.doi");
      if (testScope == testDoi) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.doi'");
      }
      testEntityId = options.getOption("datapackagemanager.test.entity.id");
      if (testEntityId == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.id'");
      }
      testEntityName = options.getOption("datapackagemanager.test.entity.name");
      if (testEntityName == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.name'");
      }
      testEntityId2 = options.getOption("datapackagemanager.test.entity2.id");
      if (testEntityId2 == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity2.id'");
      }
      testEntityName2 = options.getOption("datapackagemanager.test.entity2.name");
      if (testEntityName2 == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity2.name'");
      }
      testEntitySize = options.getOption("datapackagemanager.test.entity.size");
      if (testEntitySize == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.size'");
      }
      testEntitySize2 = options.getOption("datapackagemanager.test.entity2.size");
      if (testEntitySize2 == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity2.size'");
      }
      testPath = options.getOption("datapackagemanager.test.path");
      if (testPath == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.path'");
      }
      else {
        testEmlFileName = options.getOption("datapackagemanager.test.emlFileName");
        if (testEmlFileName == null) {
          fail("No value found for DataPackageManager property 'datapackagemanager.test.emlFileName'");
        }
        else {
          testEmlFile = new File(testPath, testEmlFileName);
        }
        testEmlFileName_2_2 = options.getOption("datapackagemanager.test.emlFileName.eml.2.2");
        if (testEmlFileName_2_2 == null) {
          fail("No value found for DataPackageManager property 'datapackagemanager.test.emlFileName.eml.2.2'");
        }
        else {
          testEmlFile_2_2 = new File(testPath, testEmlFileName_2_2);
        }
      }
      
      testMaxIdleTimeStr = options.getOption("datapackagemanager.test.maxidletime");
      if (testMaxIdleTimeStr == null) {
    	  fail("No value found for DataPackageManager property 'datapackagemanager.test.maxidletime'");
      }
			
      testIdleSleepTimeStr = options.getOption("datapackagemanager.test.idlesleeptime");
      if (testIdleSleepTimeStr == null) {
    	  fail("No value found for DataPackageManager property 'datapackagemanager.test.idlesleeptime'");
      }
			
      testInitialSleepTimeStr = options.getOption("datapackagemanager.test.initialsleeptime");
      if (testInitialSleepTimeStr == null) {
    	  fail("No value found for DataPackageManager property 'datapackagemanager.test.initialsleeptime'");
      }

      //testEmlFileLevelOne = new File(testPath, testEmlFileNameLevelOne);   
      //testEmlFileNoEntities = new File(testPath, testEmlFileNameNoEntities);   
      testIdentifier = new Integer(testIdentifierStr);
      testRevision = new Integer(testRevisionStr);
      testUpdateRevision = testRevision + 1;
      testPackageId = testScope + "." + testIdentifier + "." + testRevision;
      testMaxIdleTime = new Integer(testMaxIdleTimeStr);
      testIdleSleepTime = new Integer(testIdleSleepTimeStr);
      testInitialSleepTime = new Integer(testInitialSleepTimeStr);

      dataPackageManagerResource = new DataPackageManagerResource();
      try {
        dataPackageManager = new DataPackageManager();
      }
      catch (Exception e) {
        fail("Error encountered while constructing DataPackageManager object prior to running JUnit test.");
      }
      
      Integer newestRevision = null;

      try {
        newestRevision = dataPackageManager.getNewestRevision(testScope, testIdentifier);
        while (newestRevision != null) {
          testIdentifier += 1;
          newestRevision = dataPackageManager.getNewestRevision(testScope, testIdentifier);
        }
      }
      catch (ResourceNotFoundException e) {
    	  newestRevision = null;
      }
      catch (Exception e) {
        fail("Error encountered while initializing identifier value prior to running JUnit test: " +
             e.getMessage());       		
      }

      testPackageId = testScope + "." + testIdentifier + "." + testRevision;
      System.err.println("testPackageId: " + testPackageId);
      DataPackageManagerResourceTest.modifyTestEmlFile(testScope, testEmlFile, testPackageId);

      Integer newestRevision_2_2 = null;
      testIdentifier_2_2 = testIdentifier + ACL_TEST_IDENTIFIER_OFFSET;

      try {
        newestRevision_2_2 = dataPackageManager.getNewestRevision(testScope, testIdentifier_2_2);
        while (newestRevision_2_2 != null) {
          testIdentifier_2_2 += 1;
          newestRevision_2_2 = dataPackageManager.getNewestRevision(testScope, testIdentifier_2_2);
        }
      }
      catch (ResourceNotFoundException e) {
        newestRevision_2_2 = null;
      }
      catch (Exception e) {
        fail("Error encountered while initializing identifier value prior to running JUnit test: " +
                e.getMessage());
      }

      testPackageId_2_2 = testScope + "." + testIdentifier_2_2 + "." + testRevision;
      System.err.println("testPackageId_2_2: " + testPackageId_2_2);

      DataPackageManagerResourceTest.modifyTestEmlFile(testScope, testEmlFile_2_2, testPackageId_2_2);

    }
  }
  
  
  /*
   * Instance methods
   */
  
  /**
   * Initialize objects before each test is run.
   */
  @Before
  public void setUpTest() {
  }
  
  
  @Test public void runOrderedTests() {
	  testEvaluateDataPackage();
	  testCreateDataPackage();
	  testListDataPackageScopes();
	  testListDataPackageIdentifiers();
	  testUpdateDataPackage();
	  testStorageManager();
	  testDeleteDataPackage();
	  testListDataPackageRevisions();
	  testListDataEntities();
	  testReadDataPackage();
	  testReadMetadata();
	  testReadMetadataDublinCore();
	  testReadMetadataFormat();
	  testReadDataEntity();
	  testReadDataEntityName();
	  testReadDataEntityNames();
	  testReadDataEntitySize();
	  testReadDataEntitySizes();
	  testReadDataPackageReport();
 }
  
  
  /**
   * Test the status and message body of the Create Data Package use case
   */
  private void testCreateDataPackage() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    String errorSnippet = "Attempting to insert a data package that already exists in PASTA";
    
    // Test CREATE for OK status
    Response response = dataPackageManagerResource.createDataPackage(httpHeaders, testEmlFile);
    int statusCode = response.getStatus();
    assertEquals(202, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertTrue(entityString != null);
    assertTrue(entityString.startsWith("create_"));
    this.transaction = entityString;
    waitForPastaUpload(testRevision);
    
    // Test readDataPackage for OK status
    response = dataPackageManagerResource.readDataPackage(httpHeaders, testScope, testIdentifier, testRevision.toString(), null);
    statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityId));
    }

    /* Test for Conflict state on a second CREATE of the same data package */
    response = dataPackageManagerResource.createDataPackage(httpHeaders, testEmlFile);
    statusCode = response.getStatus();
    assertEquals(202, statusCode);

    // Check the message body
    entityString = (String) response.getEntity();
    assertTrue(entityString != null);
    assertTrue(entityString.startsWith("create_"));
    this.transaction = entityString;

    try {
    	Thread.sleep(15000);
    }
    catch (Exception e) {
    	;
    }

    testReadDataPackageError(testRevision.toString(), errorSnippet);
  }

    /**
     * Test the status and message body of the Create Data Package use case when
     * attempting to create a data package that is already Level-1 (should fail)
     *
     * @TODO: For this test to work, need to re-implement EMLDataPackage.isLevelOne()
     *        Current implementation is based on the old criteria for a Level-1 data package.
    @Test public void testCreateDataPackageLevelOne() {
      HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
      String testEntityIdLevelOne = "knb-lter-lno.11111.1";

      // Test CREATE for 400 status
      Response response = dataPackageManagerResource.createDataPackage(httpHeaders, testEmlFileLevelOne);
      int statusCode = response.getStatus();
      assertTrue(statusCode == 400);

      // Check the message body
      String entityString = (String) response.getEntity();
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testEntityIdLevelOne));
      }
    }*/
  

  /**
   * Test the status and message body of the Create Data Package use case when
   * attempting to create a data package containing no entities (should fail)
   *
  @Test public void testCreateDataPackageNoEntities() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    String testEntityIdNoEntities = "knb-lter-lno.99999.1";
    
    // Test CREATE for 400 status
    Response response = dataPackageManagerResource.createDataPackage(httpHeaders, testEmlFileNoEntities);
    int statusCode = response.getStatus();
    assertTrue(statusCode == 400);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityIdNoEntities));
    }
  }*/
  

  /**
   * Test the status and message body of the Evaluate Data Package use case
   */
  private void testEvaluateDataPackage() {
    DummyCookieHttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    List<MediaType> acceptHeaders = new ArrayList<MediaType>();
    MediaType xmlMediaType = new MediaType("application", "xml");
    acceptHeaders.add(xmlMediaType);
    httpHeaders.setAcceptHeaders(acceptHeaders);
    
	HashMap<String, String> query = new HashMap<String, String>();	
	query.put("useChecksum", "true");
	UriInfo uriInfo = new edu.lternet.pasta.eventmanager.DummyUriInfo(query);

    // Test Evaluate for OK status
    Response response = dataPackageManagerResource.evaluateDataPackage(httpHeaders, uriInfo, testEmlFile);
    assertEquals(202, response.getStatus());
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertTrue(entityString != null);
    assertTrue(entityString.startsWith("evaluate_"));
    this.transaction = entityString;

    try {
    	Thread.sleep(30000);
    }
    catch (Exception e) {
    	;
    }
    
    testReadEvaluateReport();
  }
  

  /**
   * Test the status and message body of the Evaluate Data Package use case when
   * attempting to evaluate a data package containing no entities (should fail)
   *
  @Test public void testEvaluateDataPackageNoEntities() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    String testEntityIdNoEntities = "knb-lter-lno.99999.1";
    
    // Test CREATE for OK status
    Response response = dataPackageManagerResource.evaluateDataPackage(httpHeaders, testEmlFileNoEntities);
    int statusCode = response.getStatus();
    assertTrue(statusCode == 400);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityIdNoEntities));
    }
  }*/
  

  /**
   * Test the status and message body of the List Data Entities use case
   */
  private void testListDataEntities() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.listDataEntities(httpHeaders, testScope, testIdentifier, testRevision.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityId));
    }
  }
    

  /**
   * Test the status and message body of the List Data Package Identifiers use case
   */
  private void testListDataPackageIdentifiers() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.listDataPackageIdentifiers(httpHeaders, testScope);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testIdentifier.toString()));
    }
  }
    

  /**
   * Test the status and message body of the List Data Package Revisions use case
   */
  private void testListDataPackageRevisions() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.listDataPackageRevisions(httpHeaders, testScope, testIdentifier.toString(),"");
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testRevision.toString()));
      assertTrue(entityString.contains(testUpdateRevision.toString()));
    }
  }
    

  /**
   * Test the status and message body of the List Data Package Scopes use case
   */
  private void testListDataPackageScopes() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.listDataPackageScopes(httpHeaders);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testScope));
    }
  }
    

  /**
   * Test the status and message body of the Read Data Package use case
   */
  private void testReadDataPackage() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataPackage(httpHeaders, testScope, testIdentifier, testRevision.toString(), null);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityId));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataPackage(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString(), null);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package From DOI use case.
   */
  @Test
  public void testReadDataPackageFromDoi() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    String[] doiParts = testDoi.split("/");
    
    assertEquals(doiParts.length, 3);
    
    String shoulder = doiParts[0];
    String pasta = doiParts[1];
    String md5 = doiParts[2];
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataPackageFromDoi(httpHeaders, shoulder, pasta, md5, null);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains("package/eml"));
      assertTrue(entityString.contains("package/metadata/eml"));
      assertTrue(entityString.contains("package/report/eml"));
      assertTrue(entityString.contains("package/data/eml"));
    }

    // Test for BAD REQUEST status with a null DOI value.
    response = dataPackageManagerResource.readDataPackageFromDoi(httpHeaders, null, null, null, null);
    assertEquals(400, response.getStatus());

    // Test for NOT FOUND status with a bogus DOI value.
    response = dataPackageManagerResource.readDataPackageFromDoi(httpHeaders, "a", "b", "c", null);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package Error use case.
   */
  private void testReadDataPackageError(String revision, String errorSnippet) {
    DummyCookieHttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Get an XML report. Test REPORT for OK status
    Response response = dataPackageManagerResource.readDataPackageError(httpHeaders, this.transaction);
    assertEquals(200, response.getStatus());
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(errorSnippet));
    } 
  }
    

  /**
   * Test the status and message body of the Read Data Entity
   */
  private void testReadDataEntity() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntity(httpHeaders, testScope, testIdentifier, testRevision.toString(), testEntityId);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    File dataEntity = (File) response.getEntity();
    assertNotNull(dataEntity);
    long fileSize = FileUtils.sizeOf(dataEntity);
    assertTrue(fileSize > 880L);

    // Test for 404 NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntity(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString(), testEntityId);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Entity Name operation
   */
  private void testReadDataEntityName() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntityName(httpHeaders, testScope, testIdentifier, testRevision.toString(), testEntityId);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().equals(testEntityName));
    }

    // Test for 404 NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntityName(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString(), testEntityId);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Entity Names operation
   */
  private void testReadDataEntityNames() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntityNames(httpHeaders, testScope, testIdentifier, testRevision);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      String testString = String.format("%s,%s\n", testEntityId, testEntityName);
      assertTrue(entityString.contains(testString));
      String testString2 = String.format("%s,%s\n", testEntityId2, testEntityName2);
      assertTrue(entityString.contains(testString2));
    }

    // Test for 404 NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntityNames(httpHeaders, testScopeBogus, testIdentifier, testRevision);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Entity Size operation
   */
  private void testReadDataEntitySize() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntitySize(httpHeaders, testScope, testIdentifier, testRevision.toString(), testEntityId);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().equals(testEntitySize));
    }

    // Test for 404 NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntitySize(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString(), testEntityId);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Entity Sizes operation
   */
  private void testReadDataEntitySizes() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntitySizes(httpHeaders, testScope, testIdentifier, testRevision);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      String testString = String.format("%s,%s\n", testEntityId, testEntitySize);
      assertTrue(entityString.contains(testString));
      String testString2 = String.format("%s,%s\n", testEntityId2, testEntitySize2);
      assertTrue(entityString.contains(testString2));
    }

    // Test for 404 NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntitySizes(httpHeaders, testScopeBogus, testIdentifier, testRevision);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package Report use case
   */
  private void testReadDataPackageReport() {
    DummyCookieHttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    List<MediaType> acceptHeaders = new ArrayList<MediaType>();
    MediaType xmlMediaType = new MediaType("application", "xml");
    MediaType htmlMediaType = new MediaType("text", "html");
    acceptHeaders.add(xmlMediaType);
    httpHeaders.setAcceptHeaders(acceptHeaders);
    
    // Get an XML report. Test REPORT for OK status
    Response response = dataPackageManagerResource.readDataPackageReport(httpHeaders, testScope, testIdentifier, testRevision.toString());
    assertEquals(200, response.getStatus());
    
    // Check the message body
    File entityFile = (File) response.getEntity();
    assertTrue(entityFile.exists());
    String entityString = FileUtility.fileToString(entityFile);
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains("<qr:qualityReport"));
      assertTrue(entityString.contains("<entityReport"));
      assertTrue(entityString.contains(testEntityName));
    } 

    // Get an HTML report. Test REPORT for OK status
    System.setProperty("javax.xml.transform.TransformerFactory", 
           "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    acceptHeaders.add(htmlMediaType);
    httpHeaders.setAcceptHeaders(acceptHeaders);
    response = dataPackageManagerResource.readDataPackageReport(httpHeaders, testScope, testIdentifier, testRevision.toString());
    assertEquals(200, response.getStatus());
    
    // Check the message body
    entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains("<table"));
      assertTrue(entityString.contains(testEntityName));
    } 
    
    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataPackageReport(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Evaluate Report use case.
   * This is a private method that gets called by the testEvaluateDataPackage() unit test
   * because there is an order dependency between the two.
   */
  private void testReadEvaluateReport() {
    DummyCookieHttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    List<MediaType> acceptHeaders = new ArrayList<MediaType>();
    MediaType xmlMediaType = new MediaType("application", "xml");
    MediaType htmlMediaType = new MediaType("text", "html");
    acceptHeaders.add(xmlMediaType);
    httpHeaders.setAcceptHeaders(acceptHeaders);
    
    // Get an XML report. Test REPORT for OK status
    Response response = dataPackageManagerResource.readEvaluateReport(httpHeaders, this.transaction);
    assertEquals(200, response.getStatus());
    
    // Check the message body
    File entityFile = (File) response.getEntity();
    assertTrue(entityFile.exists());
    String entityString = FileUtility.fileToString(entityFile);
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains("<qr:qualityReport"));
      assertTrue(entityString.contains("<entityReport"));
      assertTrue(entityString.contains(testEntityName));
    } 

    // Get an HTML report. Test REPORT for OK status
    System.setProperty("javax.xml.transform.TransformerFactory", 
           "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
    acceptHeaders.add(htmlMediaType);
    httpHeaders.setAcceptHeaders(acceptHeaders);
    response = dataPackageManagerResource.readEvaluateReport(httpHeaders, this.transaction);
    assertEquals(200, response.getStatus());
    
    // Check the message body
    entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains("<table"));
      assertTrue(entityString.contains(testEntityName));
    } 
    
    // Test for NOT FOUND status with a bogus transaction value
    response = dataPackageManagerResource.readEvaluateReport(httpHeaders, this.transactionBogus);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Metadata use case
   */
  private void testReadMetadata() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readMetadata(httpHeaders, testScope, testIdentifier, testRevision.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityId));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readMetadata(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Metadata Dublin Core use case
   */
  private void testReadMetadataDublinCore() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readMetadataDublinCore(httpHeaders, testScope, testIdentifier, testRevision.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
        assertFalse(entityString.isEmpty());
        String packageId = String.format("%s.%d.%d", testScope, testIdentifier, testRevision);
        String dcIdentiferElement = String.format("<dc:identifier>%s</dc:identifier>", packageId);
        System.err.println("dcIdentiferElement: " + dcIdentiferElement);
        assertTrue(entityString.contains(dcIdentiferElement));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readMetadataDublinCore(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Entity ACL operation
   */
  @Test public void testReadDataEntityAcl() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUserAcl);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntityAcl(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString(), RMD_TEST_ENTITY_ID);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(ACL_START_TEXT));
      assertTrue(entityString.trim().endsWith(ACL_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntityAcl(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString(), RMD_TEST_ENTITY_ID);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package ACL operation
   */
  @Test public void testReadDataPackageAcl() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUserAcl);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataPackageAcl(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(ACL_START_TEXT));
      assertTrue(entityString.trim().endsWith(ACL_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataPackageAcl(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package Report ACL operation
   */
  @Test public void testReadDataPackageReportAcl() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUserAcl);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataPackageReportAcl(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(ACL_START_TEXT));
      assertTrue(entityString.trim().endsWith(ACL_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataPackageReportAcl(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Metadata ACL operation
   */
  @Test public void testReadMetadataAcl() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUserAcl);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readMetadataAcl(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(ACL_START_TEXT));
      assertTrue(entityString.trim().endsWith(ACL_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readMetadataAcl(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Entity Resource Metadata operation
   */
  @Test public void testReadDataEntityRmd() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataEntityRmd(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString(), RMD_TEST_ENTITY_ID);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(RMD_START_TEXT));
      assertTrue(entityString.trim().endsWith(RMD_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataEntityRmd(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString(), RMD_TEST_ENTITY_ID);
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package Resource Metadata operation
   */
  @Test public void testReadDataPackageRmd() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataPackageRmd(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(RMD_START_TEXT));
      assertTrue(entityString.trim().endsWith(RMD_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataPackageRmd(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Data Package Report Resource Metadata operation
   */
  @Test public void testReadDataPackageReportRmd() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readDataPackageReportRmd(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(RMD_START_TEXT));
      assertTrue(entityString.trim().endsWith(RMD_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readDataPackageReportRmd(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Metadata Resource Metadata operation
   */
  @Test public void testReadMetadataRmd() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readMetadataRmd(httpHeaders, RMD_TEST_SCOPE, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(RMD_START_TEXT));
      assertTrue(entityString.trim().endsWith(RMD_END_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readMetadataRmd(httpHeaders, testScopeBogus, RMD_TEST_IDENTIFIER, RMD_TEST_REVISION.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Read Metadata Format operation
   */
  private void testReadMetadataFormat() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUserAcl);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.readMetadataFormat(httpHeaders, testScope, testIdentifier, testRevision.toString());
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.trim().startsWith(METADATA_FORMAT_START_TEXT));
    }

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.readMetadataFormat(httpHeaders, testScopeBogus, testIdentifier, testRevision.toString());
    assertEquals(404, response.getStatus());
  }
    

  /**
   * Test the status and message body of the Search Data Packages use case
   */
  @Test
  public void testSearchDataPackages() {
	HashMap<String, String> query = new HashMap<String, String>();	
	query.put("q", SOLR_QUERY);
	query.put("fl", "id,packageid,title,author,organization,pubdate,coordinates,doi,funding");
	UriInfo uriInfo = new edu.lternet.pasta.eventmanager.DummyUriInfo(query);
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test READ for OK status
    Response response = dataPackageManagerResource.searchDataPackages(httpHeaders, uriInfo);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains("<resultset"));
      assertTrue(entityString.contains("packageid"));
    }
  }
    

  /**
   * Test that the StorageManager has optimized the data
   * storage for two data entities with the same checksum value.
   * For this test we can use the original revision value and
   * the update revision value because we know that they have
   * the same data entity.
   */
  private void testStorageManager() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);   
	FileSystem fileSystem = FileSystems.getDefault();

    Response response = dataPackageManagerResource.readDataEntity(httpHeaders, testScope, testIdentifier, testRevision.toString(), testEntityId);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);   
    File revisionDataEntity = (File) response.getEntity(); // Check the message body
    assertNotNull(revisionDataEntity);
	String revisionFilePathStr = revisionDataEntity.getAbsolutePath();
	Path revisionPath = fileSystem.getPath(revisionFilePathStr);
	System.err.println(String.format("revisionPath: %s", revisionFilePathStr));
	
    response = dataPackageManagerResource.readDataEntity(httpHeaders, testScope, testIdentifier, testUpdateRevision.toString(), testEntityId);
    statusCode = response.getStatus();
    assertEquals(200, statusCode);     
    File updateRevisionDataEntity = (File) response.getEntity(); // Check the message body
    assertNotNull(updateRevisionDataEntity);
	String updateRevisionFilePathStr = updateRevisionDataEntity.getAbsolutePath();
	Path updateRevisionPath = fileSystem.getPath(updateRevisionFilePathStr);
	System.err.println(String.format("updateRevisionPath: %s", updateRevisionFilePathStr));
	
    response = dataPackageManagerResource.readDataEntity(httpHeaders, testScope, testIdentifier, testRevision.toString(), testEntityId2);
    statusCode = response.getStatus();
    assertEquals(200, statusCode);   
    File revisionDataEntity2 = (File) response.getEntity(); // Check the message body
    assertNotNull(revisionDataEntity);
	String revisionFilePathStr2 = revisionDataEntity2.getAbsolutePath();
	Path revisionPath2 = fileSystem.getPath(revisionFilePathStr2);
	System.err.println(String.format("revisionPath2: %s", revisionFilePathStr2));
	
    response = dataPackageManagerResource.readDataEntity(httpHeaders, testScope, testIdentifier, testUpdateRevision.toString(), testEntityId2);
    statusCode = response.getStatus();
    assertEquals(200, statusCode);     
    File updateRevisionDataEntity2 = (File) response.getEntity(); // Check the message body
    assertNotNull(updateRevisionDataEntity2);
	String updateRevisionFilePathStr2 = updateRevisionDataEntity2.getAbsolutePath();
	Path updateRevisionPath2 = fileSystem.getPath(updateRevisionFilePathStr2);
	System.err.println(String.format("updateRevisionPath2: %s", updateRevisionFilePathStr2));
	
	try {
		// Get the unique file keys (i.e. inodes) for the revision's data entities
		BasicFileAttributes revisionAttributes = Files.readAttributes(revisionPath, BasicFileAttributes.class);
		Object revisionKey = revisionAttributes.fileKey();
		BasicFileAttributes revisionAttributes2 = Files.readAttributes(revisionPath2, BasicFileAttributes.class);
		Object revisionKey2 = revisionAttributes2.fileKey();

		// Get the unique file keys (i.e. inodes) for the updated revision's data entities
		BasicFileAttributes updateRevisionAttributes = Files.readAttributes(updateRevisionPath, BasicFileAttributes.class);
		Object updateRevisionKey = updateRevisionAttributes.fileKey();
		BasicFileAttributes updateRevisionAttributes2 = Files.readAttributes(updateRevisionPath2, BasicFileAttributes.class);
		Object updateRevisionKey2 = updateRevisionAttributes2.fileKey();
		
		/*
		 * The fileKey() method returns null on the Windows platform, so
		 * this test really only works on Unix/Linux platform.
		 */
		if (isWindowsPlatform) {
			assertTrue(
					   (revisionKey == null) && 
					   (updateRevisionKey == null) &&
					   (revisionKey2 == null) && 
					   (updateRevisionKey2 == null)
					   );
		}
		else {
			assertTrue(revisionKey != null);
			assertTrue(updateRevisionKey != null);
			assertTrue(revisionKey2 != null);
			assertTrue(updateRevisionKey2 != null);
			assertTrue(revisionKey.equals(updateRevisionKey));
			assertTrue(revisionKey2.equals(updateRevisionKey2));
			assertFalse(revisionKey.equals(updateRevisionKey2));
			assertFalse(revisionKey2.equals(updateRevisionKey));
		}
	}
	catch (IOException e) {
		e.printStackTrace();
	}

  }
    

  /**
   * Test the status and message body of the Update Data Package use case
   */
  private void testUpdateDataPackage() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    String conflictError = "but an equal or higher revision";
    
	HashMap<String, String> query = new HashMap<String, String>();	
	query.put("useChecksum", "true");
	UriInfo uriInfo = new edu.lternet.pasta.eventmanager.DummyUriInfo(query);

	// Test UPDATE for OK status
    String testPackageId = testScope + "." + testIdentifier + "." + testUpdateRevision;
    modifyTestEmlFile(testScope, testEmlFile, testPackageId);
    Response response = dataPackageManagerResource.updateDataPackage(httpHeaders, uriInfo, testScope, testIdentifier, testEmlFile);
    int statusCode = response.getStatus();
    assertEquals(202, statusCode);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertTrue(entityString != null);
    assertTrue(entityString.startsWith("update_"));
    this.transaction = entityString;
    waitForPastaUpload(testUpdateRevision);

    // Test readDataPackage for OK status
    response = dataPackageManagerResource.readDataPackage(httpHeaders, testScope, testIdentifier, testUpdateRevision.toString(), null);
    statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Check the message body
    entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityId));
    }

    /* 
     * Test for Conflict state on a second UPDATE of the same data package 
     */
    response = dataPackageManagerResource.updateDataPackage(httpHeaders, uriInfo, testScope, testIdentifier, testEmlFile);
    statusCode = response.getStatus();
    assertEquals(202, statusCode);
    entityString = (String) response.getEntity(); // Check the message body
    assertTrue(entityString != null);
    assertTrue(entityString.startsWith("update_"));
    this.transaction = entityString;

    try {
    	Thread.sleep(15000);
    }
    catch (Exception e) {
    	;
    }

    testReadDataPackageError(testUpdateRevision.toString(), conflictError);
  }

  
  /**
   * Test the status and message body of the Create Data Package use case when
   * attempting to create a data package containing no entities (should fail)
   *
  @Test public void testUpdateDataPackageNoEntities() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    String utcString = "1364505531871";
    String testEntityIdNoEntities = "knb-lter-lno.99999.1";
    String testScopeNoEntities = "knb-lter-lno";
    Integer testIdentifierNoEntities = new Integer(99999);
    
    // Test CREATE for OK status
    Response response = dataPackageManagerResource.updateDataPackage(
        httpHeaders, testScopeNoEntities, testIdentifierNoEntities, testEmlFileNoEntities);
    int statusCode = response.getStatus();
    assertTrue(statusCode == 400);
    
    // Check the message body
    String entityString = (String) response.getEntity();
    assertFalse(entityString == null);
    if (entityString != null) {
      assertFalse(entityString.isEmpty());
      assertTrue(entityString.contains(testEntityIdNoEntities));
    }
  }*/
  

  /**
   * Test the status and message body of the DELETE use case
   */
  private void testDeleteDataPackage() {
    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(testUser);
    
    // Test DELETE for OK status
    Response response = dataPackageManagerResource.deleteDataPackage(httpHeaders, testScope, testIdentifier);
    int statusCode = response.getStatus();
    assertEquals(200, statusCode);
    
    // Test for Conflict status on a second DELETE
    response = dataPackageManagerResource.deleteDataPackage(httpHeaders, testScope, testIdentifier);
    statusCode = response.getStatus();
    assertEquals(409, statusCode);

    // Test for NOT FOUND status with a bogus package id
    response = dataPackageManagerResource.deleteDataPackage(httpHeaders, testScopeBogus, testIdentifier);
    statusCode = response.getStatus();
    assertEquals(404, statusCode);
  }
  
  
  private void waitForPastaUpload(Integer revision) {
    try {
    	// Ensure that the test data package has been successfully created
    	PastaUtility.waitForPastaUpload(
    			dataPackageManager,
    			this.transaction,
    			testInitialSleepTime,
    		    testMaxIdleTime,
    			testIdleSleepTime,
    		    testPackageId,
    			testScope,
    			testIdentifier,
    			revision
              );
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  private void waitForPastaUpload_2_2(Integer revision) {
    try {
      // Ensure that the test data package has been successfully created
      PastaUtility.waitForPastaUpload(
              dataPackageManager,
              this.transaction,
              testInitialSleepTime,
              testMaxIdleTime,
              testIdleSleepTime,
              testPackageId_2_2,
              testScope,
              testIdentifier_2_2,
              revision
      );
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }


  /**
   * Clean up and release any objects after each test is complete.
   */
  @After
  public void tearDownTest() {
  }
  
  
  /**
   * Release any objects after all tests are complete.
   */
  @AfterClass
  public static void tearDownClass() {
    dataPackageManagerResource = null;
    dataPackageManager = null;
    configurationListener = null;
    options = null;
    testScope = null;
    testScopeBogus = null;
    testIdentifierStr = null;
    testRevision = null;
    testEntityId = null;
    testPath = null;
    testEmlFileName = null;
    testEmlFile = null;
  }
  
}
