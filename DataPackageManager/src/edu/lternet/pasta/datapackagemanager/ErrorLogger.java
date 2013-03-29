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

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Mar 28, 2013
 * 
 *        Manages errors that occur during createDataPackage, updateDataPackage,
 *        and evaluateDataPackage that must be recorded for future access.
 * 
 */
public class ErrorLogger {

	/*
	 * Class variables
	 */

	private static Logger logger = Logger.getLogger(ErrorLogger.class);
	private static final String dirPath = "WebRoot/WEB-INF/conf";

	/*
	 * Instance variables
	 */

	/*
	 * Constructors
	 */

	/**
	 * A single constructor for each error to be recorded.
	 * 
	 * @param packageId The package identifier of the offending data package
	 * @param transaction The transaction identifier
	 * @param error The exception object of the error
	 */
	public ErrorLogger(String packageId, String transaction, Exception error) {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		String metadataDir = options.getOption("datapackagemanager.metadataDir");

		if (metadataDir != null && !metadataDir.isEmpty()) {

			File packageDir = new File(metadataDir + "/" + packageId);

			try {

				if (!packageDir.exists()) {
					FileUtils.forceMkdir(packageDir);
				}

				File errorFile = new File(metadataDir + "/" + packageId + "/errorlog."
				    + transaction + ".txt");
				FileUtils.writeStringToFile(errorFile, error.getMessage(), "UTF-8");

			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

		}

		logger.error("ErrorLogger: " + error.getMessage());

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

}
