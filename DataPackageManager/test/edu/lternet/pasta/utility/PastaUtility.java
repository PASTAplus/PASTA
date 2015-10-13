/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011-2015 the University of New Mexico.
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

package edu.lternet.pasta.utility;

import static org.junit.Assert.fail;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;


/**
 * @author dcosta
 * 
 * This class holds utility methods that can be used by several of the
 * test methods.
 *
 */
public class PastaUtility {

	/**
	 * Wait for PASTA to finish creating or updating a data package before
	 * proceeding to the next part of the test.
	 * 
	 * @param dataPackageManager   a DataPackageManager object
	 * @param transaction          the transaction string
	 * @param testInitialSleepTime the amount of time to wait initially, in miliseconds
	 * @param testMaxIdleTime      the maximum amount of time to wait, in miliseconds
	 * @param testIdleSleepTime    the amount of time to wait during each iteration, in miliseconds
	 * @param testPackageId        the package ID of the test document
	 * @param testScope            the scope of the test document
	 * @param testIdentifier       the identifier of the test document
	 * @param testRevision         the revision of the test document
	 * @throws Exception
	 */
	public static void waitForPastaUpload(DataPackageManager dataPackageManager,
											String transaction,
											Integer testInitialSleepTime,
											Integer testMaxIdleTime,
											Integer testIdleSleepTime,
											String testPackageId,
											String testScope,
											Integer testIdentifier,
											Integer testRevision
			                              ) throws Exception {

		DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
		boolean wasUploaded = false;

		// Ensure that the test data package has been successfully uploaded to PASTA
		if (transaction != null && dpr != null) {
			Integer timeCounter = testInitialSleepTime;
			Thread.sleep(testInitialSleepTime);
			while (!wasUploaded && (timeCounter <= testMaxIdleTime)) {
				try {
					String error = dataPackageManager.readDataPackageError(transaction);
					fail(error);
				} 
				catch (ResourceNotFoundException e) {
					Thread.sleep(testIdleSleepTime);
					timeCounter += testIdleSleepTime;
					wasUploaded = dpr.hasDataPackage(testScope, testIdentifier, testRevision.toString());
				}
			}

			if (timeCounter > testMaxIdleTime) {
				fail("Time to create data package '" + testPackageId
						+ "' exceeded max time of " + testMaxIdleTime / 1000 + " seconds!");
			}
		}
		else {
			fail("Unknown error creating test data package: " + testPackageId);
		}

	}

}
