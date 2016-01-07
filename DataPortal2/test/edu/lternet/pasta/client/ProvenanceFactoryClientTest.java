/*
 *
 * $Date: 2012-04-16 21:17:52 -0600 (Mon, 16 Apr 2012) $
 * $Author: mservilla $
 * $Revision: 1983 $
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

import static org.junit.Assert.*;
import static org.junit.Assert.fail;

import java.sql.SQLException;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.client.LoginClient;
import edu.lternet.pasta.client.ProvenanceFactoryClient;
import edu.lternet.pasta.token.TokenManager;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author servilla
 * @since Aug 14, 2012
 * 
 */
public class ProvenanceFactoryClientTest {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.client.ProvenanceFactoryClientTest.class);

	private static String uid = null;
	private static String password = null;
	private static String pid = null;
	private static LoginClient loginClient = null;

	/*
	 * Instance variables
	 */

	private ProvenanceFactoryClient provenanceFactoryClient = null;

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
			pid = options.getString("provenancefactoryclient.testpackageid");
			if (pid == null) {
				fail("No value found for ProvenanceFactoryClient property: 'provenancefactoryclient.testpackageid'");
			}
		}
		
		// Authenticate test user
		try {
			loginClient = new LoginClient(uid, password);
		} catch (PastaAuthenticationException e) {
			fail("User '" + uid + "' failed to authenticate.");
		}

	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {

		TokenManager tokenManager = new TokenManager();

		// Clean up "tokenstore" database and remove user.
		try {
			tokenManager.deleteToken(uid);
		}
		catch (ClassNotFoundException | SQLException e) {
			// no-op since we don't want test to fail if token is not found
		}

		uid = null;
		password = null;

	}


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		try {
			provenanceFactoryClient = new ProvenanceFactoryClient(uid);
		}
		catch (PastaAuthenticationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		catch (PastaConfigurationException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		provenanceFactoryClient = null;
	}


	@Test
	public void testGetProvenanceByPid() {
		String provenanceFragment = null;
		String provenanceStatement = "This method step describes provenance-based metadata as specified in the LTER EML Best Practices.";

		try {
			// Test for successful return of the provenance fragment for the packageId
			provenanceFragment = provenanceFactoryClient.getProvenanceByPid(pid);
		}
		catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertFalse(provenanceFragment == null);

		// Confirm that a provenance metadata fragment is returned.
		if (provenanceFragment != null) {
			assertFalse(provenanceFragment.isEmpty());
			assertTrue(provenanceFragment.contains(provenanceStatement));
		}
	}

}
