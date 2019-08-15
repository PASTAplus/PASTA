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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.ucsb.nceas.utilities.Options;


public class DataPackageManagerTest {

	/*
	 * Class fields
	 */

	private static ConfigurationListener configurationListener = null;
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static Options options = null;
	private static String testPath = null;
	private static File testEmlFile = null;
	private static String testEmlFileName = null;


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
	    }
	      
	}


  /**
   * Test DataPackageManager.isPastaDataSource() method.
   */
  @Test 
  public void testIsPastaDataSource() {
    String pastaUriHead = options.getOption("datapackagemanager.pastaUriHead");
    if (pastaUriHead == null) {
        fail("No value found for DataPackageManager property 'datapackagemanager.pastaUriHead'");
    }

    final String valid1 =  pastaUriHead + "eml/knb-lter-hbr/58/5";
    final String invalid1 = "https:/pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5";
    final String invalid2 = "http://pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5";
    final String invalid3 = "https://pasta.lternet.edu/package/eml/knb-lter-hbr/58/5";
    
    assertTrue(String.format("Expected %s to validate", 
    		   valid1), 
    		   DataPackageManager.isPastaDataSource(valid1));
    
    assertTrue(String.format("Expected %s to invalidate because single slash not double slash", 
    		                 invalid1), 
    		   !DataPackageManager.isPastaDataSource(invalid1));
    
    assertTrue(String.format("Expected %s to invalidate because 'http' not 'https'", 
    		                 invalid2), 
    		   !DataPackageManager.isPastaDataSource(invalid2));
    
    assertTrue(String.format("Expected %s to invalidate because different tier", 
    		                 invalid3),
    		   !DataPackageManager.isPastaDataSource(invalid3));
  }

  
  /**
   * Test DataPackageManager.isValidScope() method.
   */
  @Test 
  public void testIsValidScope() {
    final String valid1 = "edi";
    final String valid2 = "knb-lter-and";
    final String invalid1 = "knb-lter-zzz";
    final String invalid2 = "bogus-scope";
    
    assertTrue(String.format("Expected %s to validate", valid1), DataPackageManager.isValidScope(valid1));
    assertTrue(String.format("Expected %s to validate", valid2), DataPackageManager.isValidScope(valid2));
    assertTrue(String.format("Expected %s to invalidate", invalid1), !DataPackageManager.isValidScope(invalid1));
    assertTrue(String.format("Expected %s to invalidate", invalid2), !DataPackageManager.isValidScope(invalid2));
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
  	  DataPackageManager dataPackageManager = new DataPackageManager();
      String xmlString = dataPackageManager.toLevelOne(testEmlFile, entityURLMap);
      assertFalse(xmlString.contains(levelZeroURL));
      assertFalse(xmlString.contains("system=\"knb\""));
      assertFalse(xmlString.contains("authSystem=\"knb\""));
      assertTrue(xmlString.contains("system=\"https://pasta.edirepository.org\""));
      assertTrue(xmlString.contains("authSystem=\"https://pasta.edirepository.org/authentication\""));
      assertTrue(xmlString.contains(levelOneURL));
    }
    catch (Exception e) {
      fail("Exception in testToLevelOne(): " + e.getMessage());
    }
  }

}
