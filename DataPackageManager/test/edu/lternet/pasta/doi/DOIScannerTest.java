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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;

import junit.extensions.TestSetup;
import junit.framework.TestCase;
import junit.framework.TestFailure;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import edu.lternet.pasta.doi.DOIScanner;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Nov 9, 2012
 * 
 */
public class DOIScannerTest {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger.getLogger(DOIScannerTest.class);
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	
	// The number of test resources that should be available
	private static final Integer RESOURCES = 16;
	
	// The number of test resources with DOIs after performing DOI registration
	// with DOIScanner.doScanToRegister()
	private static final Integer DOIS = 7;
	
	// The number test resources that have been obsoleted after performing
	// DOIScanner.doScanToObsolete()
	private static final Integer DEACTIVATED = 16;
	
	private static DOIScanner doiScanner = null;
	private static String dbDriver = null;
	private static String dbUser = null;
	private static String dbPassword = null;
	private static String dbUrl = null;

	/*
	 * Instance variables
	 */

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

		ConfigurationListener configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
		Options options = ConfigurationListener.getOptions();

		if (options == null) {
			Assert
			    .fail("Failed to load the Data Package Manager properties file: 'datapackagemanager.properties'");
		}

		Assert.assertNotNull("Failed to load 'datapackagemanager.properties'",
		    options);

		dbDriver = options.getOption("dbDriver");
		Assert.assertNotNull("Property 'dbDriver' not set", dbDriver);

		dbUser = options.getOption("dbUser");
		Assert.assertNotNull("Property 'dbUser' not set", dbUser);

		dbPassword = options.getOption("dbPassword");
		Assert.assertNotNull("Property 'dbPassword' not set", dbPassword);

		dbUrl = options.getOption("dbURL");
		Assert.assertNotNull("Property 'dbUrl' not set", dbUrl);

		Integer numberResources = getNumberResourcesPresent();
		if (numberResources != RESOURCES) {
			Assert.fail("Necessary resources are not present");
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

		doiScanner = new DOIScanner();
		
		// Explicitly set DOI testing to true
		doiScanner.doiTest = true;
		
		setResourceDoiNull();
		setResourceDateDeactivatedNull();

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

	}

/**
 * @throws java.lang.Exception
 */
@Test
public void testDoScanToRegister() throws Exception {
		
	doiScanner.doScanToRegister();
	
	Integer numberDois = getNumberResourcesWithDois();
	
	if (numberDois != DOIS) {
		Assert.fail("The number of resources with DOIs (" + numberDois.toString() +
				") does not equal the number test resources that should have DOIs (" +
				DOIS + ")");
	}
	
}

/**
 * @throws java.lang.Exception
 */
@Test
public void testDoScanToObsolete() throws Exception {
	
	doiScanner.doScanToRegister();
	
	setResourceDateDeactivatedNow();
	doiScanner.doScanToObsolete();
	
	Integer numberDeactivated = getNumberResourcesDeactivated();
	
	if (numberDeactivated != DEACTIVATED) {
		Assert.fail("The number of obsoleted resources (" + numberDeactivated +
				") do not equal the number of test resources expected to be " +
				"obsoleted (" + DEACTIVATED + ")");
	}
	
}

	/**
	 * Returns a connection to the database.
	 * 
	 * @return conn The database Connection object
	 */
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
				logger.warn("SQLState: " + warn.getSQLState());
				logger.warn("Message:  " + warn.getMessage());
				logger.warn("Vendor: " + warn.getErrorCode());
				warn = warn.getNextWarning();
			}
		}

		return conn;

	}

	/**
	 * Determines if the necessary test resources (16 total) are present in the
	 * Data Package Manager resource registry:
	 *  knb-lter-atz.1.1 - 4 resources
	 *  knb-lter-atz.2.1 - 4 resources
	 *  knb-lter-atz.2.1 - 4 resources
	 *  knb-lter-atz.2.1 - 4 resources
	 * 
	 * @return Boolean of test resource presence
	 */
	private static Integer getNumberResourcesPresent() throws Exception {

		Integer nResources = null;

		Connection conn = null;
		conn = getConnection();
		Assert.assertNotNull("Database connection null", conn);

		String queryString = "SELECT count(*)"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " package_id LIKE 'knb-lter-atz.%';";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			result.next();
			nResources = result.getInt(1);

		} finally {
			conn.close();
		}

		return nResources;

	}
	
	/**
	 * Sets the test resource attribute 'doi' to NULL in the resource_registry
	 * table for all test resources.
	 * 
	 * @throws Exception
	 */
	private static void setResourceDoiNull() throws Exception {

		Connection conn = null;
		conn = getConnection();
		Assert.assertNotNull("Database connection null", conn);

		String queryString = "UPDATE datapackagemanager.resource_registry"
		    + " SET doi=NULL WHERE package_id LIKE 'knb-lter-atz.%';";

		Statement stat = null;
		Integer rowCount = null;

		try {
			stat = conn.createStatement();
			rowCount = stat.executeUpdate(queryString);
		} finally {
			conn.close();
		}

		if (rowCount != 16) {
			Assert.fail("Did not successfully set resource 'doi' to NULL");
		}

	}
	
	/**
	 * Sets the test resource attribute 'date_deactivated' to NULL in the
	 * resource_registry table for all test resources.
	 * 
	 * @throws Exception
	 */
	private static void setResourceDateDeactivatedNull() throws Exception {

		Connection conn = null;
		conn = getConnection();
		Assert.assertNotNull("Database connection null", conn);

		String queryString = "UPDATE datapackagemanager.resource_registry"
		    + " SET date_deactivated=NULL WHERE package_id LIKE 'knb-lter-atz.%';";

		Statement stat = null;
		Integer rowCount = null;

		try {
			stat = conn.createStatement();
			rowCount = stat.executeUpdate(queryString);
		} finally {
			conn.close();
		}

		if (rowCount != 16) {
			Assert.fail("Did not successfully set resource 'date_deactivated' to NULL");
		}

	}
	
	/**
	 * Sets the test resource attributes 'date_deactivated' to the current
	 * date/time in the resource_registry table for all test resources - 
	 * effectively deleting the resource.
	 * 
	 * @throws Exception
	 */
	private static void setResourceDateDeactivatedNow() throws Exception {

		Connection conn = null;
		conn = getConnection();
		Assert.assertNotNull("Database connection null", conn);

		String queryString = "UPDATE datapackagemanager.resource_registry"
		    + " SET date_deactivated=now() WHERE package_id LIKE 'knb-lter-atz.%';";

		Statement stat = null;
		Integer rowCount = null;

		try {
			stat = conn.createStatement();
			rowCount = stat.executeUpdate(queryString);
		} finally {
			conn.close();
		}

		if (rowCount != 16) {
			Assert.fail("Did not successfully set resource 'date_deactivated' to now()");
		}

	}

	/**
	 * Returns the number of test resources that have DOIs.
	 * 
	 * @return The number of test resources that have DOIs
	 * @throws Exception
	 */
	private static Integer getNumberResourcesWithDois() throws Exception {

		Integer nResources = null;

		Connection conn = null;
		conn = getConnection();
		Assert.assertNotNull("Database connection null", conn);

		String queryString = "SELECT count(*)"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " package_id LIKE 'knb-lter-atz.%' AND doi IS NOT NULL;";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			result.next();
			nResources = result.getInt(1);
			
		} finally {
			conn.close();
		}

		return nResources;

	}

	/**
	 * Returns the number of test resources that are deactivated and DO NOT have
	 * DOIs.
	 * 
	 * @return The number of test resources that have DOIs
	 * @throws Exception
	 */
	private static Integer getNumberResourcesDeactivated() throws Exception {

		Integer nResources = null;

		Connection conn = null;
		conn = getConnection();
		Assert.assertNotNull("Database connection null", conn);

		String queryString = "SELECT count(*)"
		    + " FROM datapackagemanager.resource_registry WHERE"
		    + " package_id LIKE 'knb-lter-atz.%' AND date_deactivated IS NOT NULL"
		    + " AND doi IS NULL;";

		Statement stat = null;

		try {

			stat = conn.createStatement();
			ResultSet result = stat.executeQuery(queryString);

			result.next();
			nResources = result.getInt(1);
			
		} finally {
			conn.close();
		}

		return nResources;

	}

}
