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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

import org.ecoinformatics.datamanager.parser.DataPackage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.ucsb.nceas.utilities.Options;


public class EMLEntityTest {

  /*
   * Class fields
   */
  
  private static ConfigurationListener configurationListener = null;
  private static DataPackageManager dataPackageManager;
  private static final String dirPath = "WebRoot/WEB-INF/conf";
  private static Options options = null;
  private static String qualityReportWithErrorPath = "test/data/qualityReportWithError.xml";
  private static String qualityReportWithoutErrorPath = "test/data/qualityReportWithoutError.xml";
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
  
  EMLEntity emlEntity;
  
  
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
      boolean evaluateMode = false;
      DataPackage dataPackage = dataPackageManager.parseEml(testEmlFile, evaluateMode);
      EMLDataPackage emlDataPackage = new EMLDataPackage(dataPackage);
      emlEntity = new EMLEntity(emlDataPackage);
      emlEntity.setEntityName(testEntityName);
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
    String xmlString = emlEntity.getAccessXML();
    assertTrue(xmlString.contains("<access"));
    assertTrue(xmlString.contains("</access>"));
    // The test entity has deny public read
    assertTrue(xmlString.contains("<deny>"));
    assertTrue(xmlString.contains("</deny>"));
  }

  
  /**
   * Test findErrorStatus() method. This tests whether we can find
   * an error status in an error report file. This logic is critical
   * because it determines whether the data for an entity is valid
   * or invalid.
   */
  @Test 
  public void testFindErrorStatus() {
    File testQualityReportWithErrorFile = new File(qualityReportWithErrorPath);
    File testQualityReportWithoutErrorFile = new File(qualityReportWithoutErrorPath);
    
    assertTrue(testQualityReportWithErrorFile != null);
    assertTrue(testQualityReportWithoutErrorFile != null);
    
    String testQualityReportWithErrorStr = FileUtility.fileToString(testQualityReportWithErrorFile);
    String testQualityReportWithoutErrorStr = FileUtility.fileToString(testQualityReportWithoutErrorFile);
    
    try {
      assertTrue(emlEntity.findErrorStatus(testQualityReportWithErrorStr));
      assertFalse(emlEntity.findErrorStatus(testQualityReportWithoutErrorStr));
    }
    catch (IOException e) {
      fail("IOException while testing EMLEntity.findErrorStatus() method: " + e.getMessage());
    }
    catch (IllegalStateException e) {
      fail("IllegalStateException while testing EMLEntity.findErrorStatus() method: " + e.getMessage());
    }
  }

  
  /**
   * Clean up and release any objects after each test is complete.
   */
  @After
  public void tearDownTest() {
    this.emlEntity = null;
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
