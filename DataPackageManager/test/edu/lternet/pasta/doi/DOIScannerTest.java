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
	private DOIScanner doiScanner = null;

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
	public void testDBConnection() throws Exception {

		Connection conn = doiScanner.getConnection();

		assertNotNull("Database connection is null", conn);

	}

	/**
	 * @throws java.lang.Exception
	 */
	@Test
	public void testGetResourceList() throws Exception {

		ArrayList<Resource> resourceList = null;
		resourceList = doiScanner.getDoiResourceList();
		
	}

}
