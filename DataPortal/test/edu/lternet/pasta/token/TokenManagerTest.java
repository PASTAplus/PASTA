/**
 *
 * $Date:$
 * $Author:$
 * $Revision:$
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.token;

import static org.junit.Assert.*;

import java.sql.SQLException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.portal.ConfigurationListener;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Mar 13, 2012
 *
 */
public class TokenManagerTest {
	
	/*
	 * Class variables
	 */
	
	private static final Logger logger = Logger.getLogger(edu.lternet.pasta.token.TokenManagerTest.class);
	
	private static String uid = null;
	private static String token = null;
	private static final String testToken = "sz46tDcFxqLby2TtlBARREdqGFSSRFbjSHPvMw0hgXLsG2uGlDWrOzjf/zM7Yd7g4n8pK5qKzohvP9UdYqf/xyx/RBAUU1QYwmUXTA5NnUZ5qHjYCtx3Y+DgwyNsQPoz6dQqR92BWWsWb39BilwfaRoyg8vRbmJ3CFRslvB5WfUqEI2OIhD2h3VyYXq8V7f8X4IZDSHWMWNXYMuxC3eQ+A==";
	
	/*
	 * Instance variables
	 */
	
	private TokenManager tokenManager;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
		ConfigurationListener.configure();
		Configuration options = ConfigurationListener.getOptions();
 
		if (options == null) {
			fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
		} else {
			uid = options.getString("tokenmanager.uid");
			if (uid == null) {
				fail("No value found for DataPortal property: 'tokenmanager.uid'");
			}
			token = options.getString("tokenmanager.token");
			if (token == null) {
				fail("No value found for DataPortal property: 'tokenmanager.token'");
			}
			if (!testToken.equals(token)) {
				fail("DataPortal property 'token' does not match test token");
			}
		}
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		
		uid = null;
		token = null;
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		this.tokenManager = new TokenManager();
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
		this.tokenManager = null;
		
	}

	@Test
	public void testSetToken() {
		
		try {
			this.tokenManager.setToken(uid, token);		
		} catch (SQLException e) {
			fail("SQL exception with call to setToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to setToken: " + e);
		}
	
	}

	@Test
	public void testGetToken() {
		
		token = null; 
		
		try {
			token = this.tokenManager.getToken(uid);		
		} catch (SQLException e) {
			fail("SQL exception with call to getToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to getToken: " + e);
		}
 
		boolean isTokenEqual = testToken.equals(token);
		// Test whether the token returned from the database is equal to the test token.
		assertTrue(isTokenEqual);
		
	}
	
	@Test
	public void testDeleteToken() {
		
		try {
			this.tokenManager.deleteToken(uid);
		} catch (SQLException e) {
			fail("SQL exception with call to deleteToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to deleteToken: " + e);
		}
		
		// Now attempt to read the deleted token from the "tokenstore".
		try {
			this.tokenManager.getToken(uid);
		} catch (SQLException e) {
			// This exception should be caught in this test.
			logger.error("SQL exception with call to deleteToken: " + e);
		} catch (ClassNotFoundException e) {
			fail("ClassNotFoundException exception with call to deleteToken: " + e);
		}

		
	}

}
