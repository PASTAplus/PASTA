/*
 *
 * $Date$
 * $Author$
 * $Revision$
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

package edu.lternet.pasta.client;

import static org.junit.Assert.fail;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.client.LoginClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.token.TokenManager;

/**
 * @author servilla
 * @since Mar 14, 2012
 * 
 */
public class LoginClientTest {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.client.LoginClientTest.class);

	private static String uid = null;
	private static String password = null;

	/*
	 * Instance variables
	 */

	private LoginClient loginClient = null;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		ConfigurationListener.configure();
		PropertiesConfiguration options = ConfigurationListener.getOptions();

		if (options == null) {
			fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
		} else {
			uid = options.getString("loginservice.uid");
			if (uid == null) {
				fail("No value found for LoginService property: 'loginservice.uid'");
			}
			password = options.getString("loginservice.password");
			if (password == null) {
				fail("No value found for LoginService property: 'loginservice.password'");
			}
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		TokenManager tokenManager = new TokenManager();

		// Clean up "tokenstore" database and remove user.
		tokenManager.deleteToken(uid);

		uid = null;
		password = null;
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLoginClientGoodUser() {

		try {
			this.loginClient = new LoginClient(uid, password);
		} catch (PastaAuthenticationException e) {
			fail("Good user '" + uid + "' failed to authenticate.");
		}

	}

	@Test
	public void testLoginClientBadUser() {

		// Set password to an incorrect password
		// to force authentication failure.
		password = "badpassword";

		try {
			this.loginClient = new LoginClient(uid, password);
		} catch (PastaAuthenticationException e) {
			// This exception should be caught in this test.
			logger.error("PastaAuthenticationException with call to LoginService: "
			    + e);
		}

	}

}
