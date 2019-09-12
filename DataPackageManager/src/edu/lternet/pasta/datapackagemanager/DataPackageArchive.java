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
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.ucsb.nceas.utilities.Options;

/**
 * @author servilla
 * @author Duane Costa
 * @since Apr 12, 2013
 * 
 *        To manage the life-cycle of a data package archive.
 * 
 */
public class DataPackageArchive {

	/*
	 * Class variables
	 */

	private static final String URI_MIDDLE_DATA = "data/eml/";
	private static final String URI_MIDDLE_METADATA = "metadata/eml/";
	private static final String URI_MIDDLE_REPORT = "report/eml/";
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static final String XSLT_FILE_NAME = "eml_text-21.xsl";

	/*
	 * Instance variables
	 */

	private final Logger logger = Logger.getLogger(DataPackageArchive.class);

	private String archiveDir = null;
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

		archiveDir = options.getOption("datapackagemanager.archiveDir");

		if (archiveDir == null || archiveDir.isEmpty()) {
			String gripe = "Entity directory property not set!";
			throw new Exception(gripe);
		}

		tmpDir = options.getOption("datapackagemanager.tmpDir");

		if (tmpDir == null || tmpDir.isEmpty()) {
			String gripe = "Tmp directory property not set!";
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
	 * components of the data package resource map. In addition, a text rendering
	 * of the EML metadata is generated and included in the archive.
	 * 
	 * @param scope
	 *          The scope value of the data package
	 * @param identifier
	 *          The identifier value of the data package
	 * @param revision
	 *          The revision value of the data package
	 * @param authToken
	 *          The authentication token of the user requesting the archive
	 * @param transaction
	 *          The transaction id of the request
	 * @param xslDir
	 *          The directory where the XSLT for transforming metadata to text is located
	 * @return The file name of the data package archive
	 * @throws Exception
	 */
	public String createDataPackageArchive(String scope, Integer identifier, Integer revision, 
			                               String userId, AuthToken authToken, String transaction,
	                                       String xslDir)
	    		throws Exception {
		DataPackageManager dataPackageManager = null;
		EmlPackageId emlPackageId = new EmlPackageId(scope, identifier, revision);
		StringBuffer manifestStringBuffer = new StringBuffer();
		Date now = new Date();

		String packageId = String.format("%s.%s.%s", scope, identifier.toString(), revision.toString());
		String zipName = packageId + ".zip";
		manifestStringBuffer.append("Manifest file for " + zipName + " created on " + now.toString() + "\n");

		String zipPath = String.format("%s/%s", archiveDir, zipName);
		File zipFile = new File(zipPath);

		if (!zipFile.exists()) {
			String msg = String.format("Begin creation of archive %s", zipPath);
			logger.warn(msg);

			/*
			 * It is necessary to create a temporary file while building the ZIP archive
			 * to prevent the client from accessing an incomplete product.
			 */
			String tmpZipFileName = DigestUtils.md5Hex(transaction);
			String tmpZipPath = String.format("%s/%s", tmpDir, tmpZipFileName);
			File tmpZipFile = new File(tmpZipPath);

			if (tmpZipFile.exists()) {
				String gripe = "The resource " + zipName + "already exists!";
				throw new ResourceExistsException(gripe);
			}

			try {
				dataPackageManager = new DataPackageManager();
			}
			catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				throw e;
			}

			FileOutputStream tmpZipFileOutputStream = null;

			try {
				tmpZipFileOutputStream = new FileOutputStream(tmpZipFile);
			}
			catch (FileNotFoundException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				throw(e);
			}

			if (dataPackageManager != null && tmpZipFileOutputStream != null) {
				String resourceMapStr = null;

				try {
					boolean oreFormat = false;
					resourceMapStr = dataPackageManager.readDataPackage(scope, identifier, revision.toString(),
						authToken, userId, oreFormat);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					tmpZipFileOutputStream.close();
					throw e;
				}

				Scanner mapScanner = new Scanner(resourceMapStr);
				ZipOutputStream zipOutputStream = new ZipOutputStream(tmpZipFileOutputStream);

				while (mapScanner.hasNextLine()) {
					FileInputStream fileInputStream = null;
					FileInputStream txtFileInputStream = null;  // For the text rendering of the EML file
					String objectName = null;
					String txtObjectName = null;
					File txtFile = null;
					String line = mapScanner.nextLine();

					if (line.contains(URI_MIDDLE_METADATA)) {

						try {
							 File metadataFile = dataPackageManager.getMetadataFile(scope, identifier, revision.toString(),
								userId, authToken);
							objectName = emlPackageId.toString() + ".xml";
							txtObjectName = emlPackageId.toString() + ".txt";

							if (metadataFile != null) {
								try {
									fileInputStream = new FileInputStream(metadataFile);
									Long size = FileUtils.sizeOf(metadataFile);
									manifestStringBuffer.append(objectName + " (" + size.toString() +" bytes)\n");

									String xslPath = String.format("%s/%s", xslDir, XSLT_FILE_NAME);
									txtFile = transformMetadata(metadataFile, xslPath, txtObjectName);

									if (txtFile != null) {
										txtFileInputStream = new FileInputStream(txtFile);
										size = FileUtils.sizeOf(txtFile);
										manifestStringBuffer.append(txtObjectName + " (" + size.toString() +" bytes)\n");
									}
								} catch (FileNotFoundException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
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

					} else if (line.contains(URI_MIDDLE_REPORT)) {

						try {
							File reportFile = dataPackageManager.readDataPackageReport(scope, identifier,
								revision.toString(), emlPackageId, authToken, userId);
							objectName = emlPackageId.toString() + ".report.xml";

							if (reportFile != null) {
								try {
									fileInputStream = new FileInputStream(reportFile);
									Long size = FileUtils.sizeOf(reportFile);
									manifestStringBuffer.append(objectName + " (" + size.toString() +" bytes)\n");
								}
								catch (FileNotFoundException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
						} catch (ClassNotFoundException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
						} catch (SQLException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
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
							entityName = dataPackageManager.readDataEntityName(dataPackageResourceId,
								entityResourceId, authToken);
							xml = dataPackageManager.readMetadata(scope, identifier, revision.toString(),
								userId, authToken);
							objectName = dataPackageManager.findObjectName(xml, entityName);
							File entityFile = dataPackageManager.getDataEntityFile(scope, identifier,
								revision.toString(), entityId, authToken, userId);

							if (entityFile != null) {
								try {
									fileInputStream = new FileInputStream(entityFile);
									Long size = FileUtils.sizeOf(entityFile);
									manifestStringBuffer.append(objectName + " (" + size.toString() +" bytes)\n");
								} catch (FileNotFoundException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
							}
						} catch (UnauthorizedException e) {
							logger.error(e.getMessage());
							e.printStackTrace();
							manifestStringBuffer.append(objectName + " (access denied)\n");
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
					}

					processZipEntry(fileInputStream, zipOutputStream, objectName);
					processZipEntry(txtFileInputStream, zipOutputStream, txtObjectName);
					txtObjectName = null; // prevent it from being processed more than once
					if (txtFile != null) { FileUtils.forceDelete(txtFile); }
				}

				if (mapScanner != null) {
					mapScanner.close();
				}

				// Create ZIP archive manifest
				String manifestObjectName = "manifest.txt";
				String manifestPath = String.format("%s/%s", archiveDir, manifestObjectName);
				File manifestFile = new File(manifestPath);
				FileUtils.writeStringToFile(manifestFile, manifestStringBuffer.toString());
				FileInputStream manifestFileInputStream = new FileInputStream(manifestFile);
				processZipEntry(manifestFileInputStream, zipOutputStream, manifestObjectName);

				FileUtils.forceDelete(manifestFile);
				zipOutputStream.close();
			}


			// Copy temporary ZIP archive to permanent ZIP archive, thus making available
			try {
				FileUtils.copyFile(tmpZipFile, zipFile);
				FileUtils.forceDelete(tmpZipFile);
			} catch (Exception e) {
				String gripe = String.format("Error copying %s to %s", tmpZipPath, zipPath);
				logger.error(gripe);
				logger.error(e.getMessage());
				throw(e);
			}

		}

		return zipName;
	}
	
	
	private void processZipEntry(FileInputStream fileInputStream, 
			                     ZipOutputStream zipOutputStream, 
			                     String objectName) {
		if (objectName != null && fileInputStream != null) {
			ZipEntry zipEntry = new ZipEntry(objectName);

			try {
				zipOutputStream.putNextEntry(zipEntry);

				int length;
				byte[] buffer = new byte[1024];

				while ((length = fileInputStream.read(buffer)) > 0) {
					zipOutputStream.write(buffer, 0, length);
				}

				zipOutputStream.closeEntry();
				fileInputStream.close();

			} catch (IOException e) {
				logger.error(e.getMessage());
				e.printStackTrace();
			}
	    }
	}
	
	
	private File transformMetadata(File emlFile, String xslPath, String txtObjectName) {
		File txtFile = null;
		String txtPath = String.format("%s/%s", this.archiveDir, txtObjectName);
		if (emlFile.exists()) {
			try {
				logger.info(String.format("Generating text rendering of EML metadata for: %s", emlFile.getName()));
				String emlXml = FileUtils.readFileToString(emlFile);
				HashMap<String, String> parametersMap = null;
				String emlTxt = transformXML(emlXml, xslPath, parametersMap);
				txtFile = new File(txtPath);
			    FileUtils.writeStringToFile(txtFile, emlTxt);
			} 
			catch (IOException e) {
				logger.error("Error reading EML metadata file: " + emlFile.getName());
				e.printStackTrace();
			}
		}
		
		return txtFile;
	}
	
	
	private String transformXML(String xml, String xslPath, HashMap<String, String> parameters) {
		String outputString = null;
		File styleSheet = new File(xslPath);
		StringReader stringReader = new StringReader(xml);
		StringWriter stringWriter = new StringWriter();
		StreamSource styleSource = new StreamSource(styleSheet);
		Result result = new StreamResult(stringWriter);
		Source source = new StreamSource(stringReader);

		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer(styleSource);
			if (parameters != null) {
				for (String parameterName : parameters.keySet()) {
					String parameterValue = parameters.get(parameterName);
					if (parameterValue != null && !parameterValue.equals("")) {
						transformer.setParameter(parameterName, parameterValue);
					}
				}
			}
			transformer.transform(source, result);
			outputString = stringWriter.toString();
		}
		catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		catch (TransformerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return outputString;
	}


		/**
		 * Returns the file object of a data package archive identified by the
		 * transaction.
		 * 
		 * @param packageId
		 *          The package identifier of the data package archive
		 * @return The archive file object
		 * @throws FileNotFoundException
		 */
		public File getDataPackageArchiveFile(String packageId)
		    throws FileNotFoundException {

			String archive = String.format("%s/%s.zip", archiveDir, packageId);
			File file = new File(archive);

			if (!file.exists()) {
				String gripe = String.format("The data package archive %s does not exist", archive);
				throw new FileNotFoundException(gripe);
			}

			return file;
		}

		
		/**
		 * Deletes the data package archive from the local file system.
		 * 
		 * @param packageId
		 *          The package identifier of the data package archive.
		 * @throws FileNotFoundException
		 */
		public void deleteDataPackageArchive(String packageId)
		    throws FileNotFoundException {

			String archive = String.format("%s/%s.zip", archiveDir, packageId);
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
