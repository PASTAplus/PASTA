/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
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
import java.util.Date;

import javax.ws.rs.core.HttpHeaders;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.security.token.AuthToken;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Apr 14, 2013
 * 
 *        Junit test case for the DataPackageError class.
 */
public class DataPackageArchiveTest {

	/*
	 * Class variables
	 */

	private static Logger logger = Logger.getLogger(DataPackageArchiveTest.class);
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static String entityDir = null;
	private static String transaction = null;
	private static final String scope = "knb-lter-nin";
	private static final Integer identifier = 1;
	private static final Integer revision = 1;
	private static final String packageId = String.format("%s.%s.%s", scope, identifier.toString(), revision.toString());
	private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
	private static String testArchive = null;
	private static String xslDir = null;

	/*
	 * Instance variables
	 */

	private AuthToken authToken = null;
	private HttpHeaders httpHeadersUser = null;
	private DataPackageArchive dpA = null;

	/*
	 * Constructors
	 */

	/*
	 * Class methods
	 */

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		entityDir = options.getOption("datapackagemanager.entityDir");

		if (entityDir == null || entityDir.isEmpty()) {
			String gripe = "Error: property 'tmpDir' not set!";
			throw new Exception(gripe);
		}

		xslDir = options.getOption("datapackagemanager.xslDir");

		if (xslDir == null || xslDir.isEmpty()) {
			String gripe = "Error: property 'xslDir' not set!";
			throw new Exception(gripe);
		}

		// Set transaction identifier based on wall-clock time
		Long time = new Date().getTime();
		transaction = time.toString();

		testArchive = packageId + ".zip";

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

	}

	/*
	 * Instance methods
	 */

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		// Create authentication token for testing purposes
		httpHeadersUser = new DummyCookieHttpHeaders(testUser);
		authToken = DataPackageManagerResource.getAuthToken(httpHeadersUser);

		if (authToken == null) {
			fail("TestUser authtoken is null!");
		}

		String userDN = authToken.getUserId();

		assertTrue("Test user authtoken string does not match test user DN!",
		    userDN.equals(testUser));

		try {
			dpA = new DataPackageArchive();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Failed to instantiate DataPackageArchive object!");
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

		dpA = null;
		String archive = String.format("%s/%s/%s.zip", entityDir, packageId, packageId);
		File file = new File(archive);

		// Clean up test archive
		if (file.exists()) {
			 FileUtils.forceDelete(file);
		}

	}

	/*
	 * Test the creation of a new data package archive using data package
	 * knb-lter-nin.1.1
	 */
	@Test
	public void testCreateDataPackageArchive() {

		String archive = null;
		File file = null;

		// Test for successful creation of test archive
		try {
			archive = dpA.createDataPackageArchive(scope, identifier, revision,
			    testUser, authToken, transaction, xslDir);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Failed to create test archive " + testArchive + "!");
		}

		assertTrue(
		    "Create archive name different than expected test archive name!",
		    testArchive.equals(archive));

		// Test existence of test archive
		String archivePath = String.format("%s/%s/%s.zip", entityDir, packageId, packageId);
		file = new File(archivePath);
		assertTrue("Test archive " + testArchive + " does not exist!",
		    file.exists());

		// TODO Add deflate routine for testing archived objects

	}

	/*
	 * Test the retrieval of the archive file object.
	 */
	@Test
	public void testGetDataPackageArchive() {

		File file = null;

		// Test for successful creation of test archive
		String packageId = String.format("%s.%s.%s", scope, identifier.toString(), revision.toString());
		try {
			dpA.createDataPackageArchive(scope, identifier, revision, testUser,
			    authToken, transaction, xslDir);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Failed to create test archive " + testArchive + "!");
		}

		// Test for getting file object of test archive
		try {
			file = dpA.getDataPackageArchiveFile(packageId);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Failed during get of data package archive!");
		}

		if (file != null) {

			// Test existence of test archive
			assertTrue("Test archive " + testArchive + " does not exist!",
			    file.exists());

		}

	}
	
	/*
	 * Test the deletion of the archive
	 */
	@Test
	public void testDeleteDataPackageArchive() {
		
		File file = null;

		// Test for successful creation of test archive
		String packageId = String.format("%s.%s.%s", scope, identifier.toString(), revision.toString());
		try {
			dpA.createDataPackageArchive(scope, identifier, revision, testUser,
			    authToken, transaction, xslDir);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Failed to create test archive " + testArchive + "!");
		}

		// Test deleting test archive
		try {
	    dpA.deleteDataPackageArchive(packageId);
    } catch (FileNotFoundException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
	    fail("Failed during delete of data package archive!");
    }
		
		// Test for successful deletion of test archive
		String archivePath = String.format("%s/%s/%s.zip", entityDir, packageId, packageId);
		file = new File(archivePath);
		assertFalse("Test archive still exists after attempted delete!", file.exists());
		
	}

}
