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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.regex.Pattern;

import org.ecoinformatics.datamanager.parser.DataPackage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.ucsb.nceas.utilities.Options;


public class EMLDataPackageTest {

  /*
   * Class fields
   */
  
  private static ConfigurationListener configurationListener = null;
  private static DataPackageManager dataPackageManager;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static Options options = null;
  private static File testEmlFile = null;
  private static String testEmlFileName = null;
  private static String testPath = null;
  private static String testScope = null;
  private static String testScopeBogus = null;
  private static String testIdentifierStr = null;
  private static String testRevisionStr = null;
  private static String testEntityId = null;
  private static String testEntityName = null;

  
  /*
   * Instance fields
   */
  
  EMLDataPackage emlDataPackage;
  
  
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
      
      try {
        dataPackageManager = new DataPackageManager();
      }
      catch (Exception e) {
        fail("Error encountered while constructing DataPackageManager object prior to running JUnit test.");
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
      boolean isEvaluate = false;
      DataPackage dataPackage = dataPackageManager.parseEml(testEmlFile, isEvaluate);
      this.emlDataPackage = new EMLDataPackage(dataPackage);
    }
    catch (ClassNotFoundException e) {
      fail("ClassNotFoundException while constructing EMLDataPackage object: " + e.getMessage());
    }
    catch (SQLException e) {
      fail("SQLException while constructing EMLDataPackage object: " + e.getMessage());
    }
    catch (FileNotFoundException e) {
      fail("FileNotFoundException while constructing EMLDataPackage object: " + e.getMessage());
    }
    catch (Exception e) {
      fail("Exception while constructing EMLDataPackage object: " + e.getMessage());
    }
  }
  
  
  /**
   * Test getAccessXML() method
   */
  @Test 
  public void testGetAccessXML() {
    String xmlString = emlDataPackage.getAccessXML();
    assertTrue(xmlString.contains("<access"));
    assertTrue(xmlString.contains("</access>"));
  }

  
  /**
   * Test getDocid() method
   */
  @Test 
  public void testGetDocid() {
    String docid = emlDataPackage.getDocid();
    boolean matches = Pattern.matches("^" + testScope + "\\.\\d+$", docid);
    assertTrue(matches);
  }

  
  /**
   * Test getPackageId() method
   */
  @Test 
  public void testGetPackageId() {
    String packageId = emlDataPackage.getPackageId();
    boolean matches = Pattern.matches("^" + testScope + "\\.\\d+\\.\\d+$", packageId);
    assertTrue(matches);
  }

  
  /**
   * Test isLevelZero() method
   */
  @Test 
  public void testIsLevelZero() {
    boolean isLevelZero = emlDataPackage.isLevelOne();
    assertFalse(isLevelZero);
  }

  
  /**
   * Test numerOfEntities() method
   */
  @Test 
  public void testNumberOfEntities() {
    long actual = emlDataPackage.numberOfEntities();
    long expected = 2;
    assertEquals("Wrong number of entities found", expected, actual);
  }

  
  /**
   * Test toLevelOne() method
   */
  @Test 
  public void testToLevelOne() {
    String entityName = "NoneSuchBugCount";
    String levelZeroURL = "http://trachyte.lternet.edu:8080/test/NoneSuchBugCount.txt";
    String levelOneURL = "https://pasta.lternet.edu/package/data/eml/knb-lter-lno/10032/1/NoneSuchBugCount";
    HashMap<String, String> entityURLMap = new HashMap<String, String>();
    entityURLMap.put(entityName, levelOneURL);
    
    try {
      File levelOneFile = emlDataPackage.toLevelOne(testEmlFile, entityURLMap);
      String xmlString = FileUtility.fileToString(levelOneFile);
      assertFalse(xmlString.contains(levelZeroURL));
      assertFalse(xmlString.contains("system=\"knb\""));
      assertFalse(xmlString.contains("authSystem=\"knb\""));
      assertTrue(xmlString.contains("system=\"https://environmentaldatainitiative.org\""));
      assertTrue(xmlString.contains("authSystem=\"https://environmentaldatainitiative.org/authentication\""));
      assertTrue(xmlString.contains(levelOneURL));
    }
    catch (Exception e) {
      fail("Exception in testToLevelOne(): " + e.getMessage());
    }
  }

  
  /**
   * Clean up and release any objects after each test is complete.
   */
  @After
  public void tearDownTest() {
    this.emlDataPackage = null;
  }
  
  
  /**
   * Release any objects after all tests are complete.
   */
  @AfterClass
  public static void tearDownClass() {
    dataPackageManager = null;
    configurationListener = null;
    options = null;
    testScope = null;
    testScopeBogus = null;
    testIdentifierStr = null;
    testEntityId = null;
    testPath = null;
    testEmlFileName = null;
    testEmlFile = null;
  }
  
}
