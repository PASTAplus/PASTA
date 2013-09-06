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
import java.util.ArrayList;

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
	private static final String testToken = "dWlkPXVjYXJyb2xsLG89TFRFUixkYz1lY29pbmZvcm1hdGljcyxkYz1vcmcqaHR0cHM6Ly9wYXN0YS5sdGVybmV0LmVkdS9hdXRoZW50aWNhdGlvbioxMzcxMTg5Nzk4NDY5KmF1dGhlbnRpY2F0ZWQ=-DoU9U2H16wEO090IbLXenrGZdR48i+gQo3iJFTSLq3WYZ9VrGJ4dcctmI7AhR1o3VWWG01ezu/uNrkz0b/GpIQWlW2S1oh/qlMveMS9bLX0Azzn/U5JOpiBA4FCG4V/6s7JKp2WD1QWquv1KUk4pUMS7JT+xoBvlEMpLhrUIpj7k+u41YuEFhu4lTP4nLrl1yWQkYIzBULYuqx+B0bXeLCYAXJgpljs7QgCQAMaOGE6iBZSsCeVo8fclN9JiTzS6S52fDeTPnbdgl9iK75UQ1DEHJOggo7IT76gipxVnGhS09cC962RneFie4KnIwB8cRSq/oS0zHCmtUmr6vsZe2w==";
    private static final String testClearTextToken = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org*https://pasta.lternet.edu/authentication*1371189798469*authenticated";
    private static final String testDn = "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org";
    private static final String testAuthSystem = "https://pasta.lternet.edu/authentication";
    private static final Long testTimeToLive = 1371189798469L;
    private static final String testGroup = "authenticated";
    private static final String testSignature = "DoU9U2H16wEO090IbLXenrGZdR48i+gQo3iJFTSLq3WYZ9VrGJ4dcctmI7AhR1o3VWWG01ezu/uNrkz0b/GpIQWlW2S1oh/qlMveMS9bLX0Azzn/U5JOpiBA4FCG4V/6s7JKp2WD1QWquv1KUk4pUMS7JT+xoBvlEMpLhrUIpj7k+u41YuEFhu4lTP4nLrl1yWQkYIzBULYuqx+B0bXeLCYAXJgpljs7QgCQAMaOGE6iBZSsCeVo8fclN9JiTzS6S52fDeTPnbdgl9iK75UQ1DEHJOggo7IT76gipxVnGhS09cC962RneFie4KnIwB8cRSq/oS0zHCmtUmr6vsZe2w==";
	
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

        try {
            this.tokenManager.setToken(uid, token);
        } catch (SQLException e) {
            fail("SQL exception with call to setToken: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to setToken: " + e);
        }

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

        try {
            this.tokenManager.deleteToken(uid);
        } catch (SQLException e) {
            fail("SQL exception with call to deleteToken: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to deleteToken: " + e);
        }

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
    public void testGetCleartextToken() {


        String clearTextToken = null;

        try {
            clearTextToken = this.tokenManager.getCleartextToken(uid);
        }
        catch (SQLException e) {
            fail("SQL exception with call to getToken: " + e);
        }
        catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to getToken: " + e);
        }

        assertTrue(testClearTextToken.equals(clearTextToken));

    }

    @Test
    public void testGetUserDistinguishedName() {


        String dn = null;

        try {
            dn = this.tokenManager.getUserDistinguishedName(uid);
        }
        catch (SQLException e) {
            fail("SQL exception with call to getToken: " + e);
        }
        catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to getToken: " + e);
        }

        assertTrue(testDn.equals(dn));

    }

    @Test
    public void testGetTokenAuthenticationSystem() {


        String authSystem = null;

        try {
            authSystem = this.tokenManager.getTokenAuthenticationSystem(uid);
        }
        catch (SQLException e) {
            fail("SQL exception with call to getToken: " + e);
        }
        catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to getToken: " + e);
        }

        assertTrue(testAuthSystem.equals(authSystem));

    }

    @Test
    public void testGetTimeToLive() {


        Long ttl = null;

        try {
            ttl = this.tokenManager.getTokenTimeToLive(uid);
        }
        catch (SQLException e) {
            fail("SQL exception with call to getToken: " + e);
        }
        catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to getToken: " + e);
        }

        assertEquals(testTimeToLive, ttl);

    }

    @Test
    public void testGetUserGroups() {


        ArrayList<String> groups = new ArrayList<String>();

        try {
            groups = this.tokenManager.getUserGroups(uid);
        }
        catch (SQLException e) {
            fail("SQL exception with call to getToken: " + e);
        }
        catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to getToken: " + e);
        }

        for(String group: groups) {
            assertTrue(group.equals(testGroup));
        }

    }

    @Test
    public void testGetTokenSignature() {


        String signature = null;

        try {
            signature = this.tokenManager.getTokenSignature(uid);
        }
        catch (SQLException e) {
            fail("SQL exception with call to getToken: " + e);
        }
        catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to getToken: " + e);
        }

        assertTrue(testSignature.equals(signature));

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

        // Add token back into tokenstore
        try {
            this.tokenManager.setToken(uid, token);
        } catch (SQLException e) {
            fail("SQL exception with call to setToken: " + e);
        } catch (ClassNotFoundException e) {
            fail("ClassNotFoundException exception with call to setToken: " + e);
        }



    }

}
