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

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;


public class DataPackageManagerTest {

	/*
	 * Class fields
	 */

	private static ConfigurationListener configurationListener = null;
	private static final String dirPath = "WebRoot/WEB-INF/conf";


	/**
	 * Initialize objects before any tests are run.
	 */
	@BeforeClass
	public static void setUpClass() {
		configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
	}


  /**
   * Test DataPackageManager.isPastaDataSource() method.
   */
  @Test 
  public void testIsPastaDataSource() {
    final String valid1 = "https://pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5";
    final String valid2 = "http://pasta.lternet.edu/package/eml/knb-lter-hbr/58/5";
    final String invalid1 = "https:/pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5";
    final String invalid2 = "http://pasta.lternet.ed";
    
    assertTrue(String.format("Expected %s to validate", valid1), DataPackageManager.isPastaDataSource(valid1));
    assertTrue(String.format("Expected %s to validate", valid2), DataPackageManager.isPastaDataSource(valid2));
    assertTrue(String.format("Expected %s to invalidate", invalid1), !DataPackageManager.isPastaDataSource(invalid1));
    assertTrue(String.format("Expected %s to invalidate", invalid2), !DataPackageManager.isPastaDataSource(invalid2));
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

}
