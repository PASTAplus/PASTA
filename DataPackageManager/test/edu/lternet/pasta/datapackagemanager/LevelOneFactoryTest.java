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
import static org.junit.Assert.fail;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.lternet.pasta.common.XmlUtility;
import edu.ucsb.nceas.utilities.Options;
import edu.ucsb.nceas.utilities.XMLUtilities;


public class LevelOneFactoryTest {

	  /*
	   * Class fields
	   */
	  
	  private static ConfigurationListener configurationListener = null;
	  private static final String dirPath = "WebRoot/WEB-INF/conf";
	  private static Options options = null;
	  private static String hasNoIntellectualRightsEML = "HasNoIntellectualRights.xml";
	  private static String hasIntellectualRightsEML =   "HasIntellectualRights.xml";
	  private static String testPath = null;

	  
	  /*
	   * Instance fields
	   */
	  
	  LevelOneEMLFactory levelOneEMLFactory;
	  
	  
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
	      testPath = options.getOption("datapackagemanager.test.path");
	      if (testPath == null) {
	        fail("No value found for DataPackageManager property 'datapackagemanager.test.path'");
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
	      levelOneEMLFactory = new LevelOneEMLFactory();
	    }
	    catch (Exception e) {
	      fail("Exception while constructing EMLDataPackage object: " + e.getMessage());
	    }
	  }

	  
	  /*
	   * Test whether PASTA will insert an intellectualRights element
	   * in a Level-0 document that has none.
	   */
	  @Test public void testHasNoIntellectualRights() {
		  String path = String.format("%s/%s", testPath, hasNoIntellectualRightsEML);
		  testIntellectualRights(path, 0);
	  }
	  
	  
	  /*
	   * Test whether PASTA handles a Level-0 EML that already contains
	   */
	  @Test public void testHasIntellectualRights() {
		  String path = String.format("%s/%s", testPath, hasIntellectualRightsEML);
		  testIntellectualRights(path, 1);
	  }
	  
	  
	  /*
	   * Tests the creation of a Level-1 EML document with regard to the
	   * addition of an intellectualRights element if one was not present to
	   * start with.
	   * 
	   * @param path to the Level-0 test document
	   * @param initialCount can be 0 or 1, the count of intellectualRights
	   *        elements in the Level-0 test document
	   */
	private void testIntellectualRights(String filePath, int initialCount) {
		// We want 1 and only 1 intellectualRights element in the Level-1 EML
		int finalCount = 1; 
		File levelZeroEMLFile = new File(filePath);
		assertTrue(levelZeroEMLFile.exists());
		final String INTELLECTUAL_RIGHTS_OPEN_TAG = "<intellectualRights>";
		final String INTELLECTUAL_RIGHTS_END_TAG = "</intellectualRights>";

		try {
			Document levelZeroEMLDocument = XmlUtility.xmlFileToDocument(levelZeroEMLFile);
			int elementCount = levelOneEMLFactory.elementCount(levelZeroEMLDocument,
					LevelOneEMLFactory.INTELLECTUAL_RIGHTS_PATH);
			assertTrue(elementCount == initialCount);
			levelOneEMLFactory.checkIntellectualRights(levelZeroEMLDocument);
			Node documentElement = levelZeroEMLDocument.getDocumentElement();
			String levelOneEMLString = XMLUtilities.getDOMTreeAsString(documentElement);
			assertTrue(levelOneEMLString != null);
			assertTrue(levelOneEMLString.contains(INTELLECTUAL_RIGHTS_OPEN_TAG));
			assertTrue(levelOneEMLString.contains(INTELLECTUAL_RIGHTS_END_TAG));
			elementCount = levelOneEMLFactory.elementCount(levelZeroEMLDocument,
					LevelOneEMLFactory.INTELLECTUAL_RIGHTS_PATH);
			assertTrue(elementCount == finalCount);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
  
    
    /**
     * Release any objects after all tests are complete.
     */
    @AfterClass
    public static void tearDownClass() {
      configurationListener = null;
      options = null;
      testPath = null;
    }
    
}
