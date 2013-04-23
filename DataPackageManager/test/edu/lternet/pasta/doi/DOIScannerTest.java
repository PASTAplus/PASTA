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

package edu.lternet.pasta.doi;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.http.HttpEntity;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageManagerResource;
import edu.lternet.pasta.datapackagemanager.DataPackageManagerResourceTest;
import edu.lternet.pasta.datapackagemanager.DummyCookieHttpHeaders;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Apr 22, 2013
 * 
 */
public class DOIScannerTest {

	/*
	 * Class variables
	 */

	private static ConfigurationListener configurationListener = null;
	private static DataPackageManagerResource dataPackageManagerResource;
	private static DataPackageManager dataPackageManager;
	private static DOIScanner doiScanner;
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static final String testUser = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
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
	private static String testMaxIdleTimeStr = null;
	private static Integer testMaxIdleTime = null;
	private static String testIdleSleepTimeStr = null;
	private static Integer testIdleSleepTime = null;
	private static String testInitialSleepTimeStr = null;
	private static Integer testInitialSleepTime = null;
	private static String testPackageId = null;

	private static String dbDriver = null;
	private static String dbUser = null;
	private static String dbPassword = null;
	private static String dbUrl = null;

	/*
	 * Instance variables
	 */

	private HttpHeaders httpHeaders = null;
	private Response response = null;
	private int statusCode;

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

		configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
		options = ConfigurationListener.getOptions();

		if (options == null) {
			fail("Failed to load DataPackageManager properties file");
		} else {
			dbUser = options.getOption("dbUser");
			if (dbUser == null) {
				fail("No value found for DataPackageManager property 'dbUser'");
			}
			dbDriver = options.getOption("dbDriver");
			if (dbDriver == null) {
				fail("No value found for DataPackageManager property 'dbDriver'");
			}
			dbPassword = options.getOption("dbPassword");
			if (dbPassword == null) {
				fail("No value found for DataPackageManager property 'dbPassword'");
			}
			dbUrl = options.getOption("dbURL");
			if (dbUrl == null) {
				fail("No value found for DataPackageManager property 'dbURL'");
			}
			testScope = options.getOption("datapackagemanager.test.scope");
			if (testScope == null) {
				fail("No value found for DataPackageManager property 'datapackagemanager.test.scope'");
			}
			testScopeBogus = options.getOption("datapackagemanager.test.scope.bogus");
			if (testScopeBogus == null) {
				fail("No value found for DataPackageManager property 'datapackagemanager.test.scope.bogus'");
			}
			testIdentifierStr = options
			    .getOption("datapackagemanager.test.identifier");
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
			testMaxIdleTimeStr = options
			    .getOption("datapackagemanager.test.maxidletime");
			if (testMaxIdleTimeStr == null) {
				fail("No value found for DataPackageManager property 'datapackagemanager.test.maxidletime'");
			}
			testIdleSleepTimeStr = options
			    .getOption("datapackagemanager.test.idlesleeptime");
			if (testIdleSleepTimeStr == null) {
				fail("No value found for DataPackageManager property 'datapackagemanager.test.idlesleeptime'");
			}
			testInitialSleepTimeStr = options
			    .getOption("datapackagemanager.test.initialsleeptime");
			if (testInitialSleepTimeStr == null) {
				fail("No value found for DataPackageManager property 'datapackagemanager.test.initialsleeptime'");
			} else {
				testEmlFileName = options
				    .getOption("datapackagemanager.test.emlFileName");
				if (testEmlFileName == null) {
					fail("No value found for DataPackageManager property 'datapackagemanager.test.emlFileName'");
				} else {
					testEmlFile = new File(testPath, testEmlFileName);
				}
			}

			testIdentifier = new Integer(testIdentifierStr);
			testRevision = new Integer(testRevisionStr);
			testMaxIdleTime = new Integer(testMaxIdleTimeStr);
			testIdleSleepTime = new Integer(testIdleSleepTimeStr);
			testInitialSleepTime = new Integer(testInitialSleepTimeStr);

			dataPackageManagerResource = new DataPackageManagerResource();
			try {
				dataPackageManager = new DataPackageManager();
			} catch (Exception e) {
				fail("Error encountered while constructing DataPackageManager object prior to running JUnit test.");
			}

			try {
				Integer newestRevision = dataPackageManager.getNewestRevision(
				    testScope, testIdentifier);
				while (newestRevision != null) {
					testIdentifier += 1;
					newestRevision = dataPackageManager.getNewestRevision(testScope,
					    testIdentifier);
				}
				testPackageId = testScope + "." + testIdentifier + "." + testRevision;
				System.err.println("testPackageId: " + testPackageId);
				DataPackageManagerResourceTest.modifyTestEmlFile(testEmlFile,
				    testPackageId);
			} catch (Exception e) {
				fail("Error encountered while initializing identifier value prior to running JUnit test.");
			}

		}
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

		httpHeaders = new DummyCookieHttpHeaders(testUser);

		// Test CREATE for OK status
		response = dataPackageManagerResource.createDataPackage(httpHeaders,
		    testEmlFile);
		statusCode = response.getStatus();
		assertEquals(202, statusCode);

		String transaction = (String) response.getEntity();

		// Ensure that the test data package has been successfully created
		if (transaction != null) {
			Integer timeCounter = testInitialSleepTime;
			Thread.sleep(testInitialSleepTime);
			while (timeCounter <= testMaxIdleTime) {
				try {
					String error = dataPackageManager.readDataPackageError(transaction);
					fail(error);
				} catch (ResourceNotFoundException e) {
					try {
						String revisions = dataPackageManager.listDataPackageRevisions(
						    testScope, testIdentifier);
						break;
					} catch (ResourceNotFoundException e1) {
						timeCounter += testIdleSleepTime;
						Thread.sleep(testIdleSleepTime);
					}
				}
			}
			if (timeCounter > testMaxIdleTime) {
				fail("Time to create data package '" + testPackageId
				    + "' exceeded max time of " + testMaxIdleTime / 1000 + " seconds!");
			}
		} else {
			fail("Unknown error creating test data package: " + testPackageId);
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

	/**
	 * Test the process of generating DOIs for all data packages without DOIs,
	 * including the single test instance of knb-lter-xyz.*.*.; test deleting the
	 * test data package and the subsequent obsolescence of the same test data
	 * package.
	 */
	@Test
	public void testDOIScan() {

		String doi = null;
		
		try {
			doiScanner = new DOIScanner();
		} catch (ConfigurationException e) {
			fail(e.getMessage());
		} catch (ClassNotFoundException e) {
			fail(e.getMessage());
		} catch (SQLException e) {
			fail(e.getMessage());
		}

		doiScanner.setDoiTest(true);

		// Test DOI registration through scanning the resource registry for active
		// and public data packages without DOIs
		try {
			doiScanner.doScanToRegister();
		} catch (DOIException e) {
			fail(e.getMessage());
		}

		// Test that the test data package did receive a DOI
		response = dataPackageManagerResource.readDataPackageDoi(httpHeaders,
		    testScope, testIdentifier, testRevisionStr);
		statusCode = response.getStatus();
		assertEquals(200, statusCode);
		doi = (String) response.getEntity();
		System.err.printf("%s\n", doi);

		// Test DELETE for OK status
		response = dataPackageManagerResource.deleteDataPackage(httpHeaders,
		    testScope, testIdentifier);
		statusCode = response.getStatus();
		assertEquals(200, statusCode);

		// Test DOI obsolescence through scanning the resource registry for inactive
		// data packages with DOIs and then removing that DOI
		try {
			doiScanner.doScanToObsolete();
		} catch (DOIException e) {
			fail(e.getMessage());
		}

		// Test that the test data package DOI is no longer available
		response = dataPackageManagerResource.readDataPackageDoi(httpHeaders,
		    testScope, testIdentifier, testRevisionStr);
		statusCode = response.getStatus();
		assertEquals(404, statusCode);

		// Test that the test data package DOI no longer exists in the resource
		// registry
		doi = null;
		doi = getDoiValue(testPackageId);
		assertNull("Expected DOI object to be null!", doi);

	}

	private String getDoiValue(String packageId) {

		String doi = null;

		Connection conn = null;
		try {
			conn = getConnection();
		} catch (Exception e) {
			fail(e.getMessage());
		}
		assertNotNull("Database connection null", conn);

		String queryString = "SELECT doi"
		    + " FROM datapackagemanager.resource_registry WHERE" + " package_id='"
		    + packageId + "' AND resource_type='dataPackage';";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			result.next();
			doi = result.getString("doi");

		} catch (SQLException e) {
			fail(e.getMessage());
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				fail(e.getMessage());
			}
		}

		return doi;

	}

	private static Connection getConnection() throws Exception {
		Connection conn = null;
		SQLWarning warn;

		Class.forName(dbDriver);

		// Make the database connection
		conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

		// If a SQLWarning object is available, print its warning(s).
		// There may be multiple warnings chained.
		warn = conn.getWarnings();

		if (warn != null) {
			while (warn != null) {
				System.err.println("SQLState: " + warn.getSQLState());
				System.err.println("Message:  " + warn.getMessage());
				System.err.println("Vendor: " + warn.getErrorCode());
				warn = warn.getNextWarning();
			}
		}

		return conn;

	}

}
