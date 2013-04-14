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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import edu.lternet.pasta.common.ResourceExistsException;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Apr 13, 2013
 * 
 *        Junit test case for the DataPackageError cleass.
 */
public class DataPackageErrorTest {

	/*
	 * Class variables
	 */

	private static Logger logger = Logger.getLogger(DataPackageErrorTest.class);
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static String errorDir = null;
	private static String transaction = null;

	/*
	 * Instance variables
	 */

	private DataPackageError dpE = null;

	/*
	 * Constructors
	 */

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
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

		errorDir = options.getOption("datapackagemanager.errorDir");

		if (errorDir == null || errorDir.isEmpty()) {
			String gripe = "Error directory property not set!";
			throw new Exception(gripe);
		}

		// Set transaction identifier based on wall-clock time
		Long time = new Date().getTime();
		transaction = time.toString();

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

		try {
			dpE = new DataPackageError();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("DataPackageError instantiation.");
		}

	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {

		dpE = null;
		
		File file = new File(errorDir + "/" + transaction + ".txt");
		
		if (file.exists()) {
			FileUtils.forceDelete(file);
		}

	}

	@Test
	public void testWriteError() {

		String gripe = "Junit Test Exception!";
		Exception exception = new Exception(gripe);

		// Test writing the error
		try {
			dpE.writeError(transaction, exception);
		} catch (ResourceExistsException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		}

		// Test existence of error file
		File file = new File(errorDir + "/" + transaction + ".txt");
		assertTrue("Data package error file does not exist!", file.exists());

		// Test correct error message
		String errorMsg = null;
		if (file != null && file.exists()) {
			try {
				errorMsg = FileUtils.readFileToString(file);
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

			assertTrue(
			    "Read error message does not match original exception error message!",
			    errorMsg.equals(gripe));

		}

		// Test writing to an existing error
		try {

			dpE.writeError(transaction, exception);
			fail("Identifying existing resource failed!");
			
		} catch (ResourceExistsException e) {

			logger.info(e.getMessage());

			String errorName = transaction + ".txt";
			String existGripe = "The resource " + errorName + " already exists!";
			assertTrue(
			    "ResourceExistException message does not matach expected resource exist message!",
			    existGripe.equals(e.getMessage()));

		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		}
		
	}
	
	@Test
	public void testReadError() {
		
		String gripe = "Junit Test Exception!";
		Exception exception = new Exception(gripe);

		// Write the error to be read
		try {
			dpE.writeError(transaction, exception);
		} catch (ResourceExistsException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		}

		try {
			
	    String errorMsg = dpE.readError(transaction);
			assertTrue(
			    "Read error message does not match original exception error message!",
			    errorMsg.equals(gripe));

		} catch (FileNotFoundException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
	    fail("Expected error message file not found!");
    }
		
	}
	
	@Test
	public void testDeleteError()	{
		
		String gripe = "Junit Test Exception!";
		Exception exception = new Exception(gripe);

		// Write the error to be deleted
		try {
			dpE.writeError(transaction, exception);
		} catch (ResourceExistsException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			fail("Writing data package error failure!");
		}
		
		try {
	    
			dpE.deleteError(transaction);
	    
			// Test existence of error file
			File file = new File(errorDir + "/" + transaction + ".txt");

			if (file.exists()) {
				fail("Error message file still exists!");
			}
	    
    } catch (FileNotFoundException e) {
	    logger.error(e.getMessage());
	    e.printStackTrace();
	    fail("Expected error message file not found!");
    }

	}

}
