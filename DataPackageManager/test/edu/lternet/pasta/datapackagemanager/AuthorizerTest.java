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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.sql.SQLException;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.ucsb.nceas.utilities.Options;


public class AuthorizerTest {

  /*
   * Class fields
   */
  
  private static ConfigurationListener configurationListener = null;
  private static DataPackageManagerResource dataPackageManagerResource;
  private static DataPackageManager dataPackageManager;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static final String testUserOwner = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
  private static final String testUserLNO = "uid=LNO,o=LTER,dc=ecoinformatics,dc=org";
  private static final String testUserAuthenticated = "uid=dcosta,o=LTER,dc=ecoinformatics,dc=org";
  private static Options options = null;
  private static File testEmlFile = null;
  private static String testEmlFileName = null;
  private static String testPath = null;
  private static String testScope = null;
  private static String testScopeBogus = null;
  private static Integer testIdentifier = null;
  private static String testIdentifierStr = null;
  private static Integer testRevision = null;
  private static String testRevisionStr = null;
  private static String testEntityId = null;
  private static String testEntityName = null;
  
  /*
   * Instance fields
   */
  
  Authorizer authorizer;
  
  
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
    configurationListener = new ConfigurationListener();
    configurationListener.initialize(dirPath);
    options = ConfigurationListener.getOptions();
    
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
      testEntityId = options.getOption("datapackagemanager.test.entity.id");
      if (testEntityId == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.id'");
      }
      testEntityName = options.getOption("datapackagemanager.test.entity.name");
      if (testEntityName == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.test.entity.name'");
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
      }
      
      testIdentifier = new Integer(testIdentifierStr);
      testRevision = new Integer(testRevisionStr);

      dataPackageManagerResource = new DataPackageManagerResource();
      try {
        dataPackageManager = new DataPackageManager();
      }
      catch (Exception e) {
        fail("Error encountered while constructing DataPackageManager object prior to running JUnit test.");
      }
      
      try {
        Integer newestRevision = dataPackageManager.getNewestRevision(testScope, testIdentifier);
        while (newestRevision != null) {
          testIdentifier += 1;
          newestRevision = dataPackageManager.getNewestRevision(testScope, testIdentifier);
        }
        String testPackageId = testScope + "." + testIdentifier + "." + testRevision;
        System.err.println("testPackageId: " + testPackageId);
        DataPackageManagerResourceTest.modifyTestEmlFile(testEmlFile, testPackageId);
      }
      catch (Exception e) {
        fail("Error encountered while initializing identifier value prior to running JUnit test.");
      }
      
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
    try {
      DataPackageRegistry dataPackageRegistry = DataPackageManager.makeDataPackageRegistry();
      this.authorizer = new Authorizer(dataPackageRegistry);
    }
    catch (ClassNotFoundException e) {
      fail("ClassNotFoundException while making DataPackageRegistry object: " + e.getMessage());
    }
    catch (SQLException e) {
      fail("SQLException while making DataPackageRegistry object: " + e.getMessage());
    }
  }
  
  
  /**
   * Test the isAuthorized() method
   */
  @Test public void testIsAuthorized() {
    AuthToken authToken = null;
    boolean isAuthorized = false;
    HttpHeaders httpHeadersOwner = new DummyCookieHttpHeaders(testUserOwner);
    HttpHeaders httpHeadersLNO = new DummyCookieHttpHeaders(testUserLNO);
    HttpHeaders httpHeadersAuthenticated = new DummyCookieHttpHeaders(testUserAuthenticated);
    String dataPackageResourceId = null;
    String dataEntityResourceId = null;
    
    // Create the data package to test
    Response response = dataPackageManagerResource.createDataPackage(httpHeadersOwner, testEmlFile);
    int statusCode = response.getStatus();
    assertEquals("Error creating data package", 202, statusCode);
    
    try {
      Thread.sleep(15000);  // Give PASTA a chance to create the data package
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    
    try {
    
    dataPackageResourceId = DataPackageManager.composeResourceId(
        ResourceType.dataPackage, testScope, testIdentifier, testRevision, testEntityId);
    dataEntityResourceId = DataPackageManager.composeResourceId(
        ResourceType.data, testScope, testIdentifier, testRevision, testEntityId);

    // Test owner privileges
    authToken = DataPackageManagerResource.getAuthToken(httpHeadersOwner);
    // Test whether the owner can read the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.read);
    assertTrue(isAuthorized);
    // Test whether the owner can read the data entity (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.read);  
    assertTrue(isAuthorized);
    // Test whether the owner can write to the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.write);
    assertTrue(isAuthorized);
    // Test whether the owner can write to the data entity (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.write);  
    assertTrue(isAuthorized);
    // Test whether the owner can change permission the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.changePermission);
    assertTrue(isAuthorized);
    // Test whether the owner can change permission the data entity (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.changePermission);  
    assertTrue(isAuthorized);
    
    // Test LNO privileges (LNO is granted all privileges to the test data package and entity)
    authToken = DataPackageManagerResource.getAuthToken(httpHeadersLNO);
    // Test whether LNO can read the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.read);  
    assertTrue(isAuthorized);
    // Test whether LNO can read the data entity (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.read);  
    assertTrue(isAuthorized);
    // Test whether LNO can write to the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.write);
    assertTrue(isAuthorized);
    // Test whether LNO can write to the data entity (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.write);  
    assertTrue(isAuthorized);
    // Test whether LNO can change permission the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.changePermission);
    assertTrue(isAuthorized);
    // Test whether LNO can change permission the data entity (should not)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.changePermission);  
    assertTrue(isAuthorized == false);
    

    // Test authenticated privileges
    authToken = DataPackageManagerResource.getAuthToken(httpHeadersAuthenticated);
    // Test whether an authenticated user can read the data package (should)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.read);  
    assertTrue(isAuthorized);
    // Test whether an authenticated user can read the data entity (should not)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.read);  
    assertTrue(isAuthorized == false);  
    // Test whether an authenticated user can write to the data package (should not)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.write);
    assertTrue(isAuthorized == false);
    // Test whether an authenticated user can write to the data entity (should not)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.write);  
    assertTrue(isAuthorized == false);
    // Test whether an authenticated user can change permission the data package (should not)
    isAuthorized = authorizer.isAuthorized(authToken, dataPackageResourceId, Rule.Permission.changePermission);
    assertTrue(isAuthorized == false);
    // Test whether an authenticated user can change permission the data entity (should not)
    isAuthorized = authorizer.isAuthorized(authToken, dataEntityResourceId, Rule.Permission.changePermission);  
    assertTrue(isAuthorized == false);
    
    // Delete the test data package
    response = dataPackageManagerResource.deleteDataPackage(httpHeadersOwner, testScope, testIdentifier);
    statusCode = response.getStatus();
    assertEquals("Error deleting data package", 200, statusCode);
    }
    catch (ClassNotFoundException e) {
      fail("ClassNotFoundException while making DataPackageRegistry object: " + e.getMessage());
    }
    catch (SQLException e) {
      fail("SQLException while making DataPackageRegistry object: " + e.getMessage());
    }
  }
  
  
  /**
   * Clean up and release any objects after each test is complete.
   */
  @After
  public void tearDownTest() {
    this.authorizer = null;
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
