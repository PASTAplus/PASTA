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

package edu.lternet.pasta.client;

import static org.junit.Assert.*;

import java.io.File;
import org.apache.commons.configuration.Configuration;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.LoginClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.token.TokenManager;

/**
 * @author dcosta
 * 
 *         Tests the operations of the DataPackageManager class.
 * 
 */
public class DataPackageManagerClientTest {

  /*
   * Class fields
   */

  private static DataPackageManagerClient dpmClient = null;
  private static String password = null;
  private static String testUser = null;
  private static File testEmlFile = null;
  private static String testEmlFileName = null;
  private static String testPath = null;
  private static String testScope = null;
  private static Integer testIdentifier = null;
  private static String testIdentifierStr = null;
  private static String testRevisionStr = null;
  private static String testUpdateRevisionStr = null;
  private static String testEntityId = null;
  private static String testEntityName = null;
  private static Integer testEntitySize = null;


	static final String solrQuery = "LTER";

  /*
   * Instance fields
   */

  TokenManager tokenManager = null;


  /*
   * Constructors
   */

  /*
   * Class methods
   */

  /**
   * Initialize objects before any tests are run.
   */
  @BeforeClass
  public static void setUpClass() {
    ConfigurationListener.configure();
    Configuration options = ConfigurationListener.getOptions();

    if (options == null) {
      fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
    }
    else {
      testUser = options.getString("eventservice.uid");
      if (testUser == null) {
        fail("No value found for property: 'eventservice.uid'");
      }
      password = options.getString("eventservice.password");
      if (password == null) {
        fail("No value found for property: 'eventservice.password'");
      }
      testScope = options.getString("dataportal.test.scope");
      if (testScope == null) {
        fail("No value found for DataPortal property 'dataportal.test.scope'");
      }
      testIdentifierStr = options.getString("dataportal.test.identifier");
      if (testIdentifierStr == null) {
        fail("No value found for DataPortal property 'dataportal.test.identifier'");
      }
      testRevisionStr = options.getString("dataportal.test.revision");
      if (testRevisionStr == null) {
        fail("No value found for DataPortal property 'dataportal.test.revision'");
      }
      testUpdateRevisionStr = options.getString("dataportal.test.revision.update");
      if (testUpdateRevisionStr == null) {
        fail("No value found for DataPortal property 'dataportal.test.revision.update'");
      }
      testEntityId = options.getString("dataportal.test.entity.id");
      if (testEntityId == null) {
        fail("No value found for DataPortal property 'dataportal.test.entity.id'");
      }
      testEntityName = options.getString("dataportal.test.entity.name");
      if (testEntityName == null) {
        fail("No value found for DataPortal property 'dataportal.test.entity.name'");
      }
      testEntitySize = options.getInteger("dataportal.test.entity.size", null);
      if (testEntitySize == null) {
        fail("No value found for DataPortal property 'dataportal.test.entity.size'");
      }
      testPath = options.getString("dataportal.test.path");
      if (testPath == null) {
        fail("No value found for DataPortal property 'dataportal.test.path'");
      }
      else {
        testEmlFileName = options.getString("dataportal.test.emlFileName");
        if (testEmlFileName == null) {
          fail("No value found for DataPortal property 'dataportal.test.emlFileName'");
        }
        else {
          testEmlFile = new File(testPath, testEmlFileName);
        }
      }
    }

    /*
     * Authenticate the test user
     */
    try {
      LoginClient loginClient = new LoginClient(testUser, password);
      System.err.println("User '" + testUser + "' authenticated.");
    } 
    catch (PastaAuthenticationException e) {
      fail("User '" + testUser + "' failed to authenticate.");
    }

    /*
     * Determine the test identifier value and modify the test
     * EML packageId attribute accordingly
     */
    try {
      dpmClient = new DataPackageManagerClient(testUser);
      testIdentifier = DataPackageManagerClient.determineTestIdentifier(dpmClient, testScope, testIdentifierStr);
      String testPackageId = testScope + "." + testIdentifier + "."
          + testRevisionStr;
      System.err.println("testPackageId: " + testPackageId);
      DataPackageManagerClient.modifyTestEmlFile(testEmlFile, testScope,
          testPackageId);
    }
    catch (Exception e) {
      fail(String.format("%s: %s", 
            "Error encountered while initializing identifier value prior to running JUnit test.",
            e.getMessage())
          );
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
  

  /**
   * Test the operations of the DataPackageManagerClient class
   */
  @Test
  public void testDataPackageManagerClient() {
	  testCreateDataPackage();
	  testEvaluateDataPackage();
	  testReadDataPackage();
	  testReadDataEntityName();
	  testReadDataPackageReport();
	  testReadMetadata();
	  testUpdateDataPackage();
	  testDeleteDataPackage();
  }

  /**
   * Test the status and message body of the Create Data Package use case
   */
  public void testCreateDataPackage() {
    String resourceMap = null;
    // Test CREATE for OK status
    try {
      resourceMap = dpmClient.createDataPackage(testEmlFile);
      assertFalse(resourceMap == null);
      if (resourceMap != null) {
        assertFalse(resourceMap.isEmpty());
        assertTrue(resourceMap.contains(testEntityId));
      }
      testListDataPackageScopes();
      testListDataPackageIdentifiers();
      testListDataPackageRevisions();
      testListDataEntities();
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Evaluate Data Package use case
   */
  public void testEvaluateDataPackage() {
    try {
      // Test Evaluate for OK status
      String qualityReport = dpmClient.evaluateDataPackage(testEmlFile);

      // Check the message body
      assertFalse(qualityReport == null);
      if (qualityReport != null) {
        assertFalse(qualityReport.isEmpty());
        assertTrue(qualityReport.contains("<qr:qualityReport"));
        assertTrue(qualityReport.contains("<entityReport"));
        assertTrue(qualityReport.contains(testEntityName));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the List Data Entities use case
   */
  private void testListDataEntities() {
    try {
      // Test READ for OK status
      String entityString = dpmClient.listDataEntities(testScope,
          testIdentifier, testRevisionStr);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testEntityId));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the List Data Package Identifiers use
   * case
   */
  private void testListDataPackageIdentifiers() {
    try {
      String entityString = dpmClient.listDataPackageIdentifiers(testScope);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testIdentifier.toString()));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the List Data Package Revisions use
   * case
   */
  private void testListDataPackageRevisions() {
    try {
      String entityString = dpmClient.listDataPackageRevisions(testScope,
          testIdentifier, null);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testRevisionStr));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the List Data Package Scopes use case
   */
  private void testListDataPackageScopes() {
    try {
      String entityString = dpmClient.listDataPackageScopes();

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testScope));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Read Data Package use case
   */
  public void testReadDataPackage() {
    try {
      String entityString = dpmClient.readDataPackage(testScope,
          testIdentifier, testRevisionStr);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testEntityId));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Read Data Entity
   */
  /*
  public void testReadDataEntity() {
    final long expectedLength = testEntitySize;
    
    try {
      byte[] dataEntity = dpmClient.readDataEntity(testScope, testIdentifier,
          testRevisionStr, testEntityId);

      // Check the message body
      assertNotNull(dataEntity);
      long entitySize = dataEntity.length;
      assertEquals(expectedLength, entitySize);
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }
  */

  /**
   * Test the status and message body of the Read Data Entity Name
   */
  public void testReadDataEntityName() {
    
    try {
      String entityName = dpmClient.readDataEntityName(testScope, testIdentifier,
          testRevisionStr, testEntityId);

      // Check the message body
      assertNotNull(entityName);
      assertEquals(testEntityName, entityName);
      
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Read Data Package Report use case
   */
  public void testReadDataPackageReport() {
    try {
      String entityString = dpmClient.readDataPackageReport(testScope,
          testIdentifier, testRevisionStr);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains("<qr:qualityReport"));
        assertTrue(entityString.contains("<entityReport"));
        assertTrue(entityString.contains(testEntityName));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Read Metadata use case
   */
  public void testReadMetadata() {
    try {
      String entityString = dpmClient.readMetadata(testScope, testIdentifier,
          testRevisionStr);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testEntityId));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Search Data Packages use case
   */
  @Test
  public void testSearchDataPackages() {
    try {
      String entityString = dpmClient.searchDataPackages(solrQuery);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains("<resultset"));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the Update Data Package use case
   */
  public void testUpdateDataPackage() {
    try {
      String testPackageId = testScope + "." + testIdentifier + "."
          + testUpdateRevisionStr;
      DataPackageManagerClient.modifyTestEmlFile(testEmlFile, testScope,
          testPackageId);
      String entityString = dpmClient.updateDataPackage(testScope,
          testIdentifier, testEmlFile);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertFalse(entityString.isEmpty());
        assertTrue(entityString.contains(testEntityId));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
    }
  }


  /**
   * Test the status and message body of the DELETE use case
   */
  public void testDeleteDataPackage() {
    try {
      String entityString = dpmClient.deleteDataPackage(testScope,
          testIdentifier);

      // Check the message body
      assertFalse(entityString == null);
      if (entityString != null) {
        assertTrue(entityString.isEmpty());
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      fail(e.getMessage());
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
    dpmClient = null;
  }

}
