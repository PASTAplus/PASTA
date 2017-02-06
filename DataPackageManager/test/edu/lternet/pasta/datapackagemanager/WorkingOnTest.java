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

import java.util.Map;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.ucsb.nceas.utilities.Options;


public class WorkingOnTest {

	  /*
	   * Class fields
	   */
	  
	  private static ConfigurationListener configurationListener = null;
	  private static final String dirPath = "WebRoot/WEB-INF/conf";
	  private static Options options = null;
	  private static final String TEST_SCOPE = "knb-lter-xyz";
	  private static final Integer TEST_IDENTIFIER = new Integer(1);
	  private static final Integer TEST_REVISION = new Integer(1);
	  private static String dbDriver;           // database driver
	  private static String dbURL;              // database URL
	  private static String dbUser;             // database user name
	  private static String dbPassword;         // database user password

	  
	  /*
	   * Instance fields
	   */
	  
	  WorkingOn workingOn;
	  
	  
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
			dbDriver = options.getOption("dbDriver");
			dbURL = options.getOption("dbURL");
			dbUser = options.getOption("dbUser");
			dbPassword = options.getOption("dbPassword");
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
	      workingOn = new WorkingOn(dbDriver, dbURL, dbUser, dbPassword);
	    }
	    catch (Exception e) {
	      fail("Exception while constructing WorkingOn object: " + e.getMessage());
	    }
	  }

	  
	  /*
	   * Test whether PASTA will insert an intellectualRights element
	   * in a Level-0 document that has none.
	   */
	  @Test public void testIsActive() {
		try {
		    workingOn.addDataPackage(TEST_SCOPE, TEST_IDENTIFIER, TEST_REVISION);
		    Map<String, String> active = workingOn.listActiveDataPackages();
			System.out.println("Package ID       Start Date");
			for (String key : active.keySet()) {
				System.out.println(String.format("  %s  %s", key, active.get(key)));
			}			
			boolean isActive = workingOn.isActive(TEST_SCOPE, TEST_IDENTIFIER, TEST_REVISION);
			assertTrue(isActive);

			workingOn.updateEndDate(TEST_SCOPE, TEST_IDENTIFIER, TEST_REVISION);
		    active = workingOn.listActiveDataPackages();
			System.out.println("Package ID       Start Date");
			for (String key : active.keySet()) {
				System.out.println(String.format("  %s  %s", key, active.get(key)));
			}
			isActive = workingOn.isActive(TEST_SCOPE, TEST_IDENTIFIER, TEST_REVISION);
			assertFalse(isActive);
		}
		catch (Exception e) {
			fail("Failed with exception: " + e.getMessage());
		}
	}

  
	  /**
	   * Clean up after the tests have run
	   */
	  @After
	  public void tearDownTest() {
	    try {
	      workingOn.deleteDataPackage(TEST_SCOPE, TEST_IDENTIFIER, TEST_REVISION);
	    }
	    catch (Exception e) {
	      fail("Exception while cleaning up data package entries in working_on table");
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
