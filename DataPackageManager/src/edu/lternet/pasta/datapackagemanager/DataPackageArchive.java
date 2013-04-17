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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @since Apr 12, 2013
 * 
 *        To manage the life-cycle of a data package archive.
 * 
 */
public class DataPackageArchive {

	/*
	 * Class variables
	 */

	private static final String SLASH = "/";
	private static final String URI_MIDDLE_DATA = "data/eml/";
	private static final String URI_MIDDLE_DATA_PACKAGE = "eml/";
	private static final String URI_MIDDLE_METADATA = "metadata/eml/";
	private static final String URI_MIDDLE_REPORT = "report/eml/";

	private static final String dirPath = "WebRoot/WEB-INF/conf";

	/*
	 * Instance variables
	 */

	private final Logger logger = Logger.getLogger(DataPackageArchive.class);

	private String tmpDir = null;

	/*
	 * Constructors
	 */

	public DataPackageArchive() throws Exception {

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		tmpDir = options.getOption("datapackagemanager.tmpDir");

		if (tmpDir == null || tmpDir.isEmpty()) {
			String gripe = "Temporary directory property not set!";
			throw new Exception(gripe);
		}

	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */

	/**
	 * Generate an "archive" of the data package by parsing and retrieving
	 * components of the data package resource map
	 * 
	 * @param scope
	 *          The scope value of the data package
	 * @param identifier
	 *          The identifier value of the data package
	 * @param revision
	 *          The revision value of the data package
	 * @param map
	 *          The resource map of the data package
	 * @param authToken
	 *          The authentication token of the user requesting the archive
	 * @param transaction
	 *          The transaction id of the request
	 * @return The file name of the data package archive
	 * @throws Exception
	 */
	public String createDataPackageArchive(String scope, Integer identifier,
	    Integer revision, String userId, AuthToken authToken, String transaction)
	    throws Exception {

		String zipName = transaction + ".zip";
		String zipPath = tmpDir + "/";

		StringBuffer manifest = new StringBuffer();

		EmlPackageId emlPackageId = new EmlPackageId(scope, identifier, revision);

		DataPackageManager dpm = null;

		/*
		 * It is necessary to create a temporary file while building the ZIP archive
		 * to prevent the client from accessing an incomplete product.
		 */
		String tmpName = DigestUtils.md5Hex(transaction);
		File zFile = new File(zipPath + tmpName);

		if (zFile.exists()) {
			String gripe = "The resource " + zipName + "already exists!";
			throw new ResourceExistsException(gripe);
		}

		try {
			dpm = new DataPackageManager();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw e;
		}

		FileOutputStream fOut = null;

		try {
			fOut = new FileOutputStream(zFile);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		if (dpm != null && fOut != null) {

			String map = null;

			try {
				map = dpm.readDataPackage(scope, identifier, revision.toString(),
				    authToken, userId);
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				throw e;
			}

			Scanner mapScanner = new Scanner(map);

			ZipOutputStream zOut = new ZipOutputStream(fOut);

			while (mapScanner.hasNextLine()) {

				FileInputStream fIn = null;
				String objectName = null;
				File file = null;

				String line = mapScanner.nextLine();

				if (line.contains(URI_MIDDLE_METADATA)) {

					try {
						file = dpm.getMetadataFile(scope, identifier, revision.toString(),
						    userId, authToken);
						objectName = emlPackageId.toString() + ".xml";
					} catch (ClassNotFoundException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (SQLException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

					if (file != null) {
						try {
							fIn = new FileInputStream(file);
							manifest.append(objectName + "\n");
						} catch (FileNotFoundException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					}

				} else if (line.contains(URI_MIDDLE_REPORT)) {

					try {
						file = dpm.readDataPackageReport(scope, identifier,
						    revision.toString(), emlPackageId, authToken, userId);
						objectName = emlPackageId.toString() + ".report.xml";
					} catch (ClassNotFoundException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (SQLException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}


					if (file != null) {
						try {
							fIn = new FileInputStream(file);
							manifest.append(objectName + "\n");
						} catch (FileNotFoundException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					}

				} else if (line.contains(URI_MIDDLE_DATA)) {

					String[] lineParts = line.split("/");
					String entityId = lineParts[lineParts.length - 1];
					String dataPackageResourceId = DataPackageManager.composeResourceId(
					    ResourceType.dataPackage, scope, identifier, revision, null);
					String entityResourceId = DataPackageManager.composeResourceId(
					    ResourceType.data, scope, identifier, revision, entityId);

					String entityName = null;
					String xml = null;

					try {
						entityName = dpm.readDataEntityName(dataPackageResourceId,
						    entityResourceId, authToken);
						xml = dpm.readMetadata(scope, identifier, revision.toString(),
						    userId, authToken);
						objectName = dpm.findObjectName(xml, entityName);
						file = dpm.getDataEntityFile(scope, identifier,
						    revision.toString(), entityId, authToken, userId);
					} catch (UnauthorizedException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
						manifest.append(objectName + "(access denied)\n");						
					} catch (ResourceNotFoundException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (ClassNotFoundException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (SQLException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					} catch (Exception e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

					if (file != null) {
						try {
							fIn = new FileInputStream(file);
							manifest.append(objectName + "\n");
						} catch (FileNotFoundException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						}
					}

				}

				if (objectName != null && fIn != null) {

					ZipEntry zipEntry = new ZipEntry(objectName);

					try {
						zOut.putNextEntry(zipEntry);

						int length;
						byte[] buffer = new byte[1024];

						while ((length = fIn.read(buffer)) > 0) {
							zOut.write(buffer, 0, length);
						}

						zOut.closeEntry();
						fIn.close();

					} catch (IOException e) {
						logger.error(e.getMessage());
						e.printStackTrace();
					}

				}

			}
			
			// Create ZIP archive manifest
			File mFile = new File(zipPath + transaction + ".txt");
			FileUtils.writeStringToFile(mFile, manifest.toString());
			ZipEntry zipEntry = new ZipEntry("manifest.txt");
			
			try {
				
				FileInputStream fIn = new FileInputStream(mFile);
				zOut.putNextEntry(zipEntry);

				int length;
				byte[] buffer = new byte[1024];

				while ((length = fIn.read(buffer)) > 0) {
					zOut.write(buffer, 0, length);
				}

				zOut.closeEntry();
				fIn.close();

			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}

			// Close ZIP archive
			zOut.close();
			
			FileUtils.forceDelete(mFile);

		}

		File tmpFile = new File(zipPath + tmpName);
		File zipFile = new File(zipPath + zipName);

		// Copy hidden ZIP archive to visible ZIP archive, thus making available
		if (!tmpFile.renameTo(zipFile)) {
			String gripe = "Error renaming " + tmpName + " to " + zipName + "!";
			throw new IOException();
		}

		return zipName;

	}

	/**
	 * Returns the file object of a data package archive identified by the
	 * transaction.
	 * 
	 * @param transaction
	 *          The transaction identifier of the data package archive
	 * @return The archive file object
	 * @throws FileNotFoundException
	 */
	public File getDataPackageArchiveFile(String transaction)
	    throws FileNotFoundException {

		String archive = tmpDir + "/" + transaction + ".zip";
		File file = new File(archive);

		if (!file.exists()) {
			String gripe = "The data package archive " + transaction
			    + ".zip does exist!";
			throw new FileNotFoundException(gripe);
		}

		return file;

	}

	/**
	 * Deletes the data package archive from the local file system.
	 * 
	 * @param transaction
	 *          The transaction identifier of the data package archive.
	 * @throws FileNotFoundException
	 */
	public void deleteDataPackageArchive(String transaction)
	    throws FileNotFoundException {

		String archive = tmpDir + "/" + transaction + ".zip";
		File file = new File(archive);

		if (!file.exists()) {
			String gripe = "The data package archive " + archive + "does exist!";
			throw new FileNotFoundException(gripe);
		} else {
			try {
				FileUtils.forceDelete(file);
			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
		}

	}

}
