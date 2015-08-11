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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.client.PastaClient;
import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author Duane Costa
 * @since  August 11, 2015
 * 
 */
public class PastaClientTest {

	/*
	 * Class variables
	 */

	
	/*
	 * Instance variables
	 */

	
	/*
	 * Methods
	 */

	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ConfigurationListener.configure();
		PropertiesConfiguration options = ConfigurationListener.getOptions();

		if (options == null) {
			fail("Failed to load the DataPortal properties file: 'dataportal.properties'");
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
	}

	
	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	
	  /**
	   * Test PastaClient.pastaURLtoPackageId() method.
	   */
	  @Test 
	  public void testPastaURLtoPackageId() {
	    final String pastaURL = "https://pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5";
	    final String expectedPackageId = "knb-lter-hbr.58.5";
	    
	    String packageId = PastaClient.pastaURLtoPackageId(pastaURL);
	    assertTrue(String.format("Expected %s", expectedPackageId), expectedPackageId.equals(packageId));
	  }

}
