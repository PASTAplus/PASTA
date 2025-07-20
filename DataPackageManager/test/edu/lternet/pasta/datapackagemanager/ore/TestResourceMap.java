	/*
	 *
	 * $Date: 2017-04-21 15:33:02 -0700 (Mon, 06 Feb 2012) $
	 * $Author: dcosta $
	 * $Revision: 1634 $
	 *
	 * Copyright 2010 the University of New Mexico.
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
package edu.lternet.pasta.datapackagemanager.ore;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.ws.rs.core.HttpHeaders;

import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageManagerResource;
import edu.lternet.pasta.datapackagemanager.DummyCookieHttpHeaders;
import edu.ucsb.nceas.utilities.Options;

public class TestResourceMap {

		/*
		 * Class fields
		 */

		private static ConfigurationListener configurationListener = null;
		private static final String dirPath = "WebRoot/WEB-INF/conf";
		private static Options options = null;
		private static String testPath = null;
		private static String testResourceMapFileName = "resourceMap.xml";
		private static String testScope = "knb-lter-nin";
		private static Integer testIdentifier = new Integer(1);
		private static Integer testRevision = new Integer(1);
		private static String testRevisionStr;
		private static String testString = null;
        private static String testEdiToken = null;
	
		static {
			testRevisionStr = testRevision.toString();
		}


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
		      testString = options.getOption("datapackagemanager.test.string");
		      if (testString == null) {
				  fail("No value found for DataPackageManager property 'datapackagemanager.test.string'");
			  }
              testEdiToken = options.getOption("datapackagemanager.test.edi.token");
              if (testEdiToken == null) {
                  fail("No value found for DataPackageManager property 'datapackagemanager.test.edi.token'");
              }
		    }
		}

	  
	  /**
	   * Test generation of an RDF-XML Resource Map by comparing it to a known substring that
	   * we expect it to contain.
	   */
	  @Test 
	  public void testToXML() {
		AuthToken authToken = null;	    
	    String user = "public";
	    HttpHeaders httpHeaders = new DummyCookieHttpHeaders(user);
	    authToken = DataPackageManagerResource.getAuthToken(httpHeaders);
	    boolean oreFormat = true;
	    
	    try {
	  	  DataPackageManager dataPackageManager = new DataPackageManager();
	      String xmlString = 
	    		  dataPackageManager.readDataPackage(testScope, testIdentifier, testRevisionStr,
	    				                             authToken, testEdiToken, user, oreFormat);
	      boolean containsSubstring = xmlString.contains(testString);
	      assertTrue(containsSubstring);
	      
	    }
	    catch (Exception e) {
	      fail("Exception in testToLevelOne(): " + e.getMessage());
	    }
	  }

	}
