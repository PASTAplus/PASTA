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
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.client.EventSubscriptionClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.token.TokenManager;

/**
 * @author servilla
 * @since Mar 25, 2012
 * 
 */
public class EventSubscriptionClientTest {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.EventSubscriptionClientTest.class);

  private static String uid = null;
  private static String password = null;
  private static String token = null;

  /*
   * Instance variables
   */

  EventSubscriptionClient eventService = null;
  TokenManager tokenManager = null;

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
      uid = options.getString("eventservice.uid");
      if (uid == null) {
        fail("No value found for EventService property: 'eventservice.uid'");
      }
      password = options.getString("eventservice.password");
      if (password == null) {
        fail("No value found for EventService property: 'eventservice.password'");
      }
      token = options.getString("eventservice.token");
      if (token == null) {
        fail("No value found for EventService property: 'eventservice.token'");
      }

    }

  }

  /**
   * @throws java.lang.Exception
   */
  @AfterClass
  public static void tearDownAfterClass() throws Exception {
  }

  /**
   * @throws java.lang.Exception
   */
  @Before
  public void setUp() throws Exception {

    this.tokenManager = new TokenManager();
    this.tokenManager.setToken(uid, token);

  }

  /**
   * @throws java.lang.Exception
   */
  @After
  public void tearDown() throws Exception {

    this.tokenManager.deleteToken(uid);

  }

  @Test
  public void testEventServiceConstructor() {

    try {
      this.eventService = new EventSubscriptionClient(uid);
    } catch (PastaAuthenticationException e) {
      fail("Token does not exist for user '" + uid + "'");
    } catch (PastaConfigurationException e) {
      fail("EventSubscriptionClient construction failed to perform configuration.");
    }

  }

}
