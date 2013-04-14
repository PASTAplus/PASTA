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
	private static String tmpDir = null;
	private static String transaction = null;
	private static final String scope = "knb-lter-nin";
	private static final Integer identifier = 1;
	private static final Integer revision = 1;
	private static final Long sizeOfTestArchive = 239584L;
	private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
	private static String testArchive = null;

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

		tmpDir = options.getOption("datapackagemanager.tmpDir");

		if (tmpDir == null || tmpDir.isEmpty()) {
			String gripe = "Error directory property not set!";
			throw new Exception(gripe);
		}

		// Set transaction identifier based on wall-clock time
		Long time = new Date().getTime();
		transaction = time.toString();
		testArchive = transaction + ".zip";

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

		File file = new File(tmpDir + "/" + transaction + ".zip");

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

		try {
			archive = dpA.createDataPackageArchive(scope, identifier, revision,
			    testUser, authToken, transaction);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Failed to create test archive " + testArchive + "!");
		}

		assertTrue(
		    "Create archive name different than expected test archive name!",
		    testArchive.equals(archive));

		// Test existence of archive
		File file = new File(tmpDir + "/" + testArchive);
		assertTrue("Test archive " + testArchive + " does not exist!",
		    file.exists());

		// Test size of archive
		Long sizeOfArchive = FileUtils.sizeOf(file);
		assertEquals("Created archive is not the same size as test archive!",
		    sizeOfTestArchive, sizeOfArchive);

	}

}
