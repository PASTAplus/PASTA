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
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Mar 28, 2013
 * 
 *        Class that manages data package error messages. Error messages are
 *        persisted in the local directory for PASTA metadata/<packageId> in the
 *        form of errorlog.<transaction>.txt.
 * 
 */
public class DataPackageError {

	/*
	 * Class variables
	 */

	private static Logger logger = Logger.getLogger(DataPackageError.class);

	private static final String dirPath = "WebRoot/WEB-INF/conf";

	/*
	 * Instance variables
	 */

	String metadataDir = null;

	/*
	 * Constructors
	 */

	public DataPackageError() throws Exception {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		metadataDir = options.getOption("datapackagemanager.metadataDir");

		if (metadataDir == null || metadataDir.isEmpty()) {
			String gripe = "The Data Package Manager property for the \"metadata\" "
			    + "directory was not properly defined.";
			throw new Exception(gripe);
		}

	}

	/*
	 * Class methods
	 */

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	/*
	 * Instance methods
	 */

	/**
	 * Write the error message from the exception object to the file
	 * "errorlog.<transaction>.txt" in the "metadata" directory for the given
	 * package identifier.
	 * 
	 * @param packageId
	 *          The data package identifier
	 * @param transaction
	 *          The transaction identifier
	 * @param error
	 *          The exception object of the error
	 */
	public void writeError(String packageId, String transaction, Exception error) {

		String packagePath = metadataDir + "/" + packageId;
		File packageDir = new File(packagePath);

		if (!packageDir.exists()) {
			try {
				FileUtils.forceMkdir(packageDir);
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}
		String filePath = metadataDir + "/" + packageId + "/";
		String fileName = "errorlog." + transaction + ".txt";
		File file = new File(filePath + fileName);

		try {
			FileUtils.writeStringToFile(file, error.getMessage(), "UTF-8");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	/**
	 * Read the error message from the file errorlog.<transaction>.txt in the
	 * "metadata" directory for the given package identifier.
	 * 
	 * @param packageId
	 *          The data package identifier
	 * @param transaction
	 *          The transaction identifier
	 * @return The error message
	 * @throws FileNotFoundException
	 */
	public String readError(String packageId, String transaction)
	    throws FileNotFoundException {

		String error = null;

		String packagePath = metadataDir + "/" + packageId;
		File packageDir = new File(packagePath);

		if (!packageDir.exists()) {
			String gripe = "The directory " + packageDir + " does not exist in the "
			    + "metadata directory path " + metadataDir;
			throw new FileNotFoundException(gripe);
		}

		String filePath = metadataDir + "/" + packageId + "/";
		String fileName = "errorlog." + transaction + ".txt";
		File file = new File(filePath + fileName);

		if (!file.exists()) {
			String gripe = "The error file " + fileName + " was not found in the "
			    + "directory " + filePath;
			throw new FileNotFoundException(gripe);
		}

		try {
			error = FileUtils.readFileToString(file, "UTF-8");
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return error;

	}

	/**
	 * Delete the error file errorlog.[transaction].txt in the "metadata"
	 * directory for the given package identifier.
	 * 
	 * @param packageId
	 *          The data package identifier
	 * @param transaction
	 *          The transaction identifier
	 * @throws FileNotFoundException
	 */
	public void deleteError(String packageId, String transaction)
	    throws FileNotFoundException {

		String packagePath = metadataDir + "/" + packageId;
		File packageDir = new File(packagePath);

		if (!packageDir.exists()) {
			String gripe = "The directory " + packageDir + "does not exist in the "
			    + "metadata directory path " + metadataDir;
			throw new FileNotFoundException(gripe);
		}

		String filePath = metadataDir + "/" + packageId + "/";
		String fileName = "errorlog." + transaction + ".txt";
		File file = new File(filePath + fileName);

		if (!file.exists()) {
			String gripe = "The error file " + fileName + "was not found in the "
			    + "directory" + filePath + ".";
			throw new FileNotFoundException(gripe);
		}

		try {
			FileUtils.forceDelete(file);
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

}
