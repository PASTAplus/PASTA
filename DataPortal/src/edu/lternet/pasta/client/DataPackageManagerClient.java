/*
 *
 * $Date: 2012-04-02 11:08:40 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: 1889 $
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

import java.io.File;
import java.io.IOException;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author dcosta
 * @since April 2, 2012
 * 
 *        The DataPackageManagerClient supports the management of data packages
 *        in the NIS Data Portal. It interacts directly with the
 *        DataPackageManager PASTA web service.
 * 
 */
public class DataPackageManagerClient extends PastaClient {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.client.DataPackageManagerClient.class);

	static final String pathqueryXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
	    + "<pathquery version=\"1.0\">\n"
	    + "  <meta_file_id>unspecified</meta_file_id>\n"
	    + "  <querytitle>unspecified</querytitle>\n"
	    + "  <returnfield>dataset/title</returnfield>\n"
	    + "  <returnfield>keyword</returnfield>\n"
	    + "  <returnfield>originator/individualName/surName</returnfield>\n"
	    + "  <returndoctype>eml://ecoinformatics.org/eml-2.1.0</returndoctype>\n"
	    + "  <returndoctype>eml://ecoinformatics.org/eml-2.1.1</returndoctype>\n"
	    + "  <querygroup operator=\"UNION\">\n"
	    + "    <queryterm casesensitive=\"false\" searchmode=\"contains\">\n"
	    + "      <value>bug</value>\n"
	    + "      <pathexpr>dataset/title</pathexpr>\n"
	    + "    </queryterm>\n"
	    + "    <queryterm casesensitive=\"false\" searchmode=\"contains\">\n"
	    + "      <value>Carroll</value>\n"
	    + "      <pathexpr>surName</pathexpr>\n"
	    + "    </queryterm>\n"
	    + "  </querygroup>\n" + "</pathquery>\n";

	/*
	 * Instance variables
	 */

	private final String BASE_URL;
	String contentType = null;

	/*
	 * Constructors
	 */

	/**
	 * Creates a new DataPackageManagerClient object and sets the user's
	 * authentication token if it exists.
	 * 
	 * @param uid
	 *          The user's identifier as a String object.
	 * 
	 * @throws PastaAuthenticationException
	 */
	public DataPackageManagerClient(String uid)
	    throws PastaAuthenticationException, PastaConfigurationException {

		super(uid);
		String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol,
		    this.pastaHost, this.pastaPort);
		this.BASE_URL = pastaUrl + "/package";
	}

	/*
	 * Class Methods
	 */

	/**
	 * Determine the test identifier used for testing data package operations.
	 * Eliminate identifiers that were previously deleted or are currently in use.
	 * 
	 * @param dpmClient
	 *          the DataPackageManagerClient object
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param initialIdentifier  the starting identifier value; keep incrementing until
	 *          we find an identifier that hasn't been used
	 * @return an integer value appropriate for use as a test identifier
	 */
	static Integer determineTestIdentifier(DataPackageManagerClient dpmClient,
	    String scope, String initialIdentifier) throws Exception {
		Integer identifier = null;

		/*
		 * Determine the test identifier. Eliminate identifiers that were previously
		 * deleted or are currently in use.
		 */
		TreeSet<String> deletedSet = new TreeSet<String>();
		String deletedDataPackages = dpmClient.listDeletedDataPackages();
		String[] deletedArray = deletedDataPackages.split("\n");
		for (int i = 0; i < deletedArray.length; i++) {
			if (deletedArray[i] != null && !deletedArray[i].equals("")
			    && deletedArray[i].startsWith(scope)) {
				deletedSet.add(deletedArray[i]);
			}
		}

		TreeSet<String> identifierSet = new TreeSet<String>();
		String dataPackageIdentifiers = dpmClient.listDataPackageIdentifiers(scope);
		String[] identifierArray = dataPackageIdentifiers.split("\n");
		for (int i = 0; i < identifierArray.length; i++) {
			if (identifierArray[i] != null && !identifierArray[i].equals("")) {
				identifierSet.add(identifierArray[i]);
			}
		}

		int identifierValue = new Integer(initialIdentifier).intValue();
		while (identifier == null) {
			String identifierString = "" + identifierValue;
			String scopeDotIdentifier = scope + "." + identifierValue;
			if (!deletedSet.contains(scopeDotIdentifier)
			    && !identifierSet.contains(identifierString)) {
				identifier = new Integer(identifierValue);
			} else {
				identifierValue++;
			}
		}

		return identifier;
	}

	/**
	 * main() program. Can be used as a lightweight unit test to test the methods
	 * in this class.
	 * 
	 * @param args
	 *          No command arguments are passed to this program.
	 */
	public static void main(String[] args) {
		String user = "ucarroll";
		String scope = "knb-lter-lno";
		Integer identifier = null;
		String revision = "1";

		ConfigurationListener.configure();

		try {
			DataPackageManagerClient dpmClient = new DataPackageManagerClient(user);

			String dataPackageScopes = dpmClient.listDataPackageScopes();
			System.out.println("\nData package scopes:\n" + dataPackageScopes);

			// Create the test data package in PASTA
			identifier = determineTestIdentifier(dpmClient, scope, "1000");
			String testEMLPath = "test/data/NoneSuchBugCount.xml";
			File testEMLFile = new File(testEMLPath);
			String createPackageId = scope + "." + identifier.toString() + "."
			    + revision;
			modifyTestEmlFile(testEMLFile, scope, createPackageId);
			String resourceMap = dpmClient.createDataPackage(testEMLFile);
			System.out.println("\nResource map:\n" + resourceMap);

			// Update the test data package in PASTA
			String dataPackageRevisions = dpmClient.listDataPackageRevisions(scope,
			    identifier, null);
			System.out.println("\nData package revisions:\n" + dataPackageRevisions);
			String[] revisionStrings = dataPackageRevisions.split("\n");
			int maxRevision = -1;
			for (int i = 0; i < revisionStrings.length; i++) {
				String revStr = revisionStrings[i];
				if (revStr != null && !revStr.equals("")) {
					Integer revInteger = new Integer(revisionStrings[i]);
					int rev = revInteger.intValue();
					maxRevision = Math.max(maxRevision, rev);
				}
			}
			int updateRevision = maxRevision + 1;
			String updatePackageId = scope + "." + identifier.toString() + "."
			    + updateRevision;
			modifyTestEmlFile(testEMLFile, scope, updatePackageId);
			resourceMap = dpmClient.updateDataPackage(scope, identifier, testEMLFile);
			System.out.println("\nResource map:\n" + resourceMap);

			String dataEntities = dpmClient.listDataEntities(scope, identifier,
			    revision);
			System.out.println("\nData entities:\n" + dataEntities);

			String dataPackage = dpmClient.readDataPackage(scope, identifier,
			    revision);
			System.out.println("\nData package:\n" + dataPackage);

			String metadata = dpmClient.readMetadata(scope, identifier, revision);
			System.out.println("\nMetadata:\n" + metadata);

			// dpmClient.readDataEntity(scope, identifier, revision, entityId,
			// System.out);
			// System.out.println("\nData entity:\n" + dataEntity);

			String dataPackageReport = dpmClient.readDataPackageReport(scope,
			    identifier, revision);
			System.out.println("\nData package report:\n" + dataPackageReport);

			String resultSetXML = dpmClient.searchDataPackages(pathqueryXML);
			System.out.println("\nResult set XML:\n" + resultSetXML);

			// Delete the test data package from PASTA
			dpmClient.deleteDataPackage(scope, identifier);
			System.out.println("\nDeleted data package: " + scope + "." + identifier);
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Modifies the packageId value in a test EML file. Useful for testing
	 * purposes.
	 * 
	 * @param testEmlFile
	 *          The test EML document file
	 * @param scope
	 *          The scope value, e.g. "knb-lter-lno"
	 * @param newPackageId
	 *          The packageId string to write to the file as the new value of the
	 *          packageId attribute, e.g. "knb-lter-lno.100.1"
	 */
	public static void modifyTestEmlFile(File testEmlFile, String scope,
	    String newPackageId) {
		boolean append = false;
		String xmlString = FileUtility.fileToString(testEmlFile);
		Pattern pattern = Pattern.compile(scope + "\\.\\d+\\.\\d+");
		Matcher matcher = pattern.matcher(xmlString);
		// Replace packageId value with new packageId value
		String modifiedXmlString = matcher.replaceAll(newPackageId);

		try {
			FileUtils.writeStringToFile(testEmlFile, modifiedXmlString, append);
		} catch (IOException e) {
			fail("IOException modifying packageId in test EML file: "
			    + e.getMessage());
		}
	}

	/*
	 * Instance Methods
	 */

	/*
	 * Documentation for the Data Package Manager web service methods and the
	 * status codes they return can be found at
	 * http://package.lternet.edu/package/docs/api
	 */

	/**
	 * Executes the 'createDataPackage' web service method.
	 * 
	 * @param emlFile
	 *          the Level-0 EML document describing the data package
	 * @return a string representation of the resource map for the newly created
	 *         data package
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String createDataPackage(File emlFile) throws Exception {

		String contentType = "application/xml";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(BASE_URL + "/eml");
		String resourceMap = null;

		// Set header content
		if (this.token != null) {
			httpPost.setHeader("Cookie", "auth-token=" + this.token);
		}
		
		httpPost.setHeader("Content-Type", contentType);

		// Set the request entity
		HttpEntity fileEntity = new FileEntity(emlFile, ContentType.create(contentType));
		httpPost.setEntity(fileEntity);

		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);

			if (statusCode == HttpStatus.SC_ACCEPTED) {
				
				EmlPackageId emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				String packageScope = emlPackageId.getScope();
				Integer packageIdentifier = emlPackageId.getIdentifier();
				Integer packageRevision = emlPackageId.getRevision();

				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition 
				Thread.sleep(initialSleepTime);
				
				while (idleTime <= maxIdleTime) {
					logger.info(idleTime);
					try {
						String errorText = readDataPackageError(entityString);
						throw new Exception(errorText);
					} catch (ResourceNotFoundException e) {
						logger.error(e.getMessage());
						try {
							resourceMap = readDataPackage(packageScope, packageIdentifier,
									packageRevision.toString());
							break;
						} catch (ResourceNotFoundException e1) {
							logger.error(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				if (idleTime > maxIdleTime) {
					String gripe = "Fiddle sticks!  Creating this data package has "
					    + "exceeded our patience and we have been forced to terminate "
					    + "this browser process, but the data package may still be "
					    + "created in PASTA if an error was not encountered.  Please "
					    + "check the audit logs or the Data Package Browser at a later "
					    + "time.";
					throw new Exception(gripe);
				}

			} else {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return resourceMap;
	}

	/**
	 * Executes the 'createDataPackage' web service method.
	 * 
	 * @param emlFile
	 *          the Level-0 EML document describing the data package
	 * @return a string representation of the resource map for the newly created
	 *         data package
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String getDataPackageArchive(String scope, Integer identifier,
	    String revision, HttpServletResponse servletResponse) throws Exception {

		String contentType = "text/plain";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		HttpPost httpPost = new HttpPost(BASE_URL + "/archive/eml" + urlTail);
		String resourceMap = null;

		// Set header content
		if (this.token != null) {
			httpPost.setHeader("Cookie", "auth-token=" + this.token);
		}
		
		httpPost.setHeader("Content-Type", contentType);

		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);

			if (statusCode == HttpStatus.SC_ACCEPTED) {
				
				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition 
				Thread.sleep(initialSleepTime);
				
				while (idleTime <= maxIdleTime) {
					logger.info(idleTime);
					try {
						String errorText = readDataPackageError(entityString);
						throw new Exception(errorText);
					} catch (ResourceNotFoundException e) {
						logger.error(e.getMessage());
						try {
							readDataPackageArchive(scope, identifier, revision, entityString, servletResponse);
							break;
						} catch (ResourceNotFoundException e1) {
							logger.error(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				if (idleTime > maxIdleTime) {
					String gripe = "Fiddle sticks!  Creating this data package archive has "
					    + "exceeded our patience and we have been forced to terminate "
					    + "this browser process, but the data package may still be "
					    + "created in PASTA if an error was not encountered.  Please "
					    + "check the audit logs or the Data Package Browser at a later "
					    + "time.";
					throw new Exception(gripe);
				}

			} else {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return resourceMap;
	}

	/**
	 * Executes the 'deleteDataPackage' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @return an empty string if the data package was successfully deleted
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String deleteDataPackage(String scope, Integer identifier)
	    throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), null, null);
		HttpDelete httpDelete = new HttpDelete(BASE_URL + "/eml" + urlTail);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpDelete.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpDelete);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'evaluateDataPackage' web service method.
	 * 
	 * @param emlFile
	 *          the Level-0 EML document describing the data package to be
	 *          evaluated
	 * @return a string holding the XML quality report document resulting from the
	 *         evaluation
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String evaluateDataPackage(File emlFile) throws Exception {
		String contentType = "application/xml";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPost httpPost = new HttpPost(BASE_URL + "/evaluate/eml");
		String qualityReport = null;

		// Set header content
		if (this.token != null) {
			httpPost.setHeader("Cookie", "auth-token=" + this.token);
		}
		httpPost.setHeader("Content-Type", contentType);

		// Set the request entity
		HttpEntity fileEntity = new FileEntity(emlFile, ContentType.create(contentType));
		httpPost.setEntity(fileEntity);

		try {
			HttpResponse httpResponse = httpClient.execute(httpPost);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);
						
			if (statusCode == HttpStatus.SC_ACCEPTED) {

				EmlPackageId emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				if (emlPackageId != null) {
				String packageScope = emlPackageId.getScope();
				Integer packageIdentifier = emlPackageId.getIdentifier();
				Integer packageRevision = emlPackageId.getRevision();

				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition
				Thread.sleep(initialSleepTime);

				while (idleTime <= maxIdleTime) {
					logger.info(idleTime);
					try {
						String errorText = readDataPackageError(entityString);
						throw new Exception(errorText);
					} catch (ResourceNotFoundException e) {
						logger.error(e.getMessage());
						try {
							qualityReport = readEvaluateReport(packageScope, packageIdentifier,
							    packageRevision.toString(), entityString);
							break;
						} catch (ResourceNotFoundException e1) {
							logger.error(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				if (idleTime > maxIdleTime) {
					String gripe = "Fiddle sticks!  Creating this data package has "
					    + "exceeded our patience and we have been forced to terminate "
					    + "this browser process, but the data package may still be "
					    + "created in PASTA if an error was not encountered.  Please "
					    + "check the audit logs or the Data Package Browser at a later "
					    + "time.";
					throw new Exception(gripe);
				}
				}
				else {
					throw new UserErrorException("An EML packageId value could not be parsed from the file. Check that the file contains valid EML.");
				}
			
			} else {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return qualityReport;
	}
	
	/**
	 * Determines whether the user has permission to read the resource identified
	 * by the resourceId.
	 * 
	 * @param resourceId
	 *          The resource identifier of the specific resource.
	 * @return Boolean whether the user has permission.
	 * @throws Exception
	 */
	public Boolean isAuthorized(String resourceId) throws Exception {
		
		Boolean isAuthorized = false;
		
		// Re-encode "+" to its character reference value of %2B to mitigate
		// an issue with the HttpGet call that performs the decoding - this is
		// a kludge to deal with encoding nonsense.
		resourceId = resourceId.replace("+", "%2B");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = BASE_URL + "/authz?resourceId=" + resourceId;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode == HttpStatus.SC_OK) {
				isAuthorized = true;
			} else if (statusCode != HttpStatus.SC_UNAUTHORIZED) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}
		
		return isAuthorized;
		
	}

	/**
	 * Executes the 'listDataEntities' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1" or "newest"
	 * @return a newline-separated list of data entity identifiers
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDataEntities(String scope, Integer identifier,
	    String revision) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/data/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'listDataPackageIdentifiers' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @return a newline-separated list of identifier values
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDataPackageIdentifiers(String scope) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = BASE_URL + "/eml/" + scope;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK
			    && statusCode != HttpStatus.SC_NOT_FOUND) {
				handleStatusCode(statusCode, entityString);
			} else if (statusCode == HttpStatus.SC_NOT_FOUND) {
				entityString = "";
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'listDataPackageRevisions' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @return a newline-separated list of revision values
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDataPackageRevisions(String scope, Integer identifier, String filter)
	    throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), null, null);
		
		// Test for "oldest" or "newest" filter
		if (filter == null) {
			filter = "";
		} else {
			filter = "?filter=" + filter;
		}
		
		String url = BASE_URL + "/eml" + urlTail + filter;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'listDataPackageScopes' web service method.
	 * 
	 * @return a newline-separated list of scope values
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDataPackageScopes() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = BASE_URL + "/eml";
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'listDeletedDataPackages' web service method.
	 * 
	 * @return a newline-separated list of packageId strings representing all the
	 *         data packages that have been deleted from PASTA
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDeletedDataPackages() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = BASE_URL + "/eml/deleted";
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'readDataEntity' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @param entityId
	 *          the entity identifier string, e.g. "NoneSuchBugCount"
	 * @param servletResponse
	 *          the servlet response object for returning content to the client
	 *          browser
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public void readDataEntity(String scope, Integer identifier, String revision,
	    String entityId, HttpServletResponse servletResponse) throws Exception {

		HttpResponse httpResponse = null;

		if (servletResponse == null) {
			String gripe = "Servlet response object is null!";
			throw new Exception(gripe);
		}

		// Re-encode "%" to its character reference value of %25 to mitigate
		// an issue with the HttpGet call that performs the decoding - this is
		// a kludge to deal with encoding nonsense.
		entityId = entityId.replace("%", "%25");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision,
		    entityId);
		String url = BASE_URL + "/data/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			httpResponse = httpClient.execute(httpGet);

			getInfo(httpResponse);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();

			if (statusCode != HttpStatus.SC_OK) {
				String gripe = "An error occurred while attempting to read the data enity: "
				    + entityId;
				if (statusCode == HttpStatus.SC_UNAUTHORIZED) {
					if (this.uid.equals("public")) {
						gripe = String.format("%s. %s", gripe,
										"You may need to log in before you can access the data entity.");
					}
					else {
						gripe = String.format("%s. %s", gripe,
										"You may not have permission to access the data entity.");
					}
				}
				handleStatusCode(statusCode, gripe);
			} else {

				Header[] headers = httpResponse.getAllHeaders();
				
				// Copy httpResponse headers to servletResponse headers
				for (int i = 0; i < headers.length; i++) {
					Header header = headers[i];
					servletResponse.setHeader(header.getName(), header.getValue());
				}

				httpEntity.writeTo(servletResponse.getOutputStream());
				
			}

		} finally {
			closeHttpClient(httpClient);
		}

	}

	/**
	 * Executes the 'readDataEntityName' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @param entityId
	 *          the entity identifier string, e.g. "NoneSuchBugCount"
	 * @return the data entity name
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String readDataEntityName(String scope, Integer identifier,
	    String revision, String entityId) throws Exception {

		// Re-encode "%" to its character reference value of %25 to mitigate
		// an issue with the HttpGet call that performs the decoding - this is
		// a kludge to deal with encoding nonsense.
		entityId = entityId.replace("%", "%25");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision,
		    entityId);
		String url = BASE_URL + "/name/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			ContentType contentType = ContentType.getOrDefault(httpEntity);
			this.contentType = contentType.toString();
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'readDataPackage' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @return the data package resource map
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String readDataPackage(String scope, Integer identifier,
	    String revision) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'readDataPackageArchive' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @param transaction
	 *          the transaction identifier string
	 * @param servletResponse
	 *          the servlet response object for returning content to the client
	 *          browser
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public void readDataPackageArchive(String scope, Integer identifier,
		                               String revision, String transaction, 
		                               HttpServletResponse servletResponse) 
		                            		   throws Exception {

		HttpResponse httpResponse = null;

		if (servletResponse == null) {
			String gripe = "Servlet response object is null!";
			throw new Exception(gripe);
		}

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = String.format("%s/archive/eml/%s/%d/%s/%s",  
				                    BASE_URL, scope, identifier, revision, transaction);
		HttpGet httpGet = new HttpGet(url);

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			httpResponse = httpClient.execute(httpGet);

			getInfo(httpResponse);

			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();

			if (statusCode != HttpStatus.SC_OK) {
				String gripe = "An error occurred while attempting to read the data package archive: "
				    + transaction;
				handleStatusCode(statusCode, gripe);
			} else {

				Header[] headers = httpResponse.getAllHeaders();
				
				// Copy httpResponse headers to servletResponse headers
				for (int i = 0; i < headers.length; i++) {
					Header header = headers[i];
					servletResponse.setHeader(header.getName(), header.getValue());
				}

				httpEntity.writeTo(servletResponse.getOutputStream());
				
			}

		} finally {
			closeHttpClient(httpClient);
		}

	}

	
	/**
	 * Executes the 'readDataPackageReport' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @return the XML quality report document for the specified data package
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String readDataPackageReport(String scope, Integer identifier,
	    String revision) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/report/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'readEvaluateReport' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @param transaction
	 * 				  the transaction value
	 * @return the XML quality report document for the specified data package
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String readEvaluateReport(String scope, Integer identifier,
	    String revision, String transaction) throws Exception {
		String contentType = "application/xml";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = "/" + transaction;
		String url = BASE_URL + "/evaluate/report/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}
		httpGet.setHeader("Accept", contentType);

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
			
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Executes the 'readMetadata' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @return the Level-1 EML metadata document for the specified data package
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String readMetadata(String scope, Integer identifier, String revision)
	    throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/metadata/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity, "UTF-8");
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	/**
	 * Returns the DOI for the data package map resource identified by the scope,
	 * identifier, and revision.
	 * 
	 * @param scope
	 * @param identifier
	 * @param revision
	 * @return DOI for the data package
	 * @throws Exception
	 */
	public String readDataPackageDoi(String scope, Integer identifier,
	    String revision) throws Exception {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/doi/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;

	}

	/**
	 * Executes the "readDataPackageError" websevice method.
	 * 
	 * @param scope
	 *          The package scope value
	 * @param identifier
	 *          The package identifier value
	 * @param revision
	 *          The package revision value
	 * @param transaction
	 *          The data package transaction
	 * @return The error message
	 * @throws Exception
	 */
	public String readDataPackageError(String transaction) throws Exception {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = "/" + transaction;
		String url = BASE_URL + "/error/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;

	}

	/**
	 * Returns the DOI for the metadata resource identified by the scope,
	 * identifier, and revision.
	 * 
	 * @param scope
	 * @param identifier
	 * @param revision
	 * @return DOI for the metadata resource
	 * @throws Exception
	 */
	public String readMetadataDoi(String scope, Integer identifier,
	    String revision) throws Exception {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/metadata/doi/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;

	}

	/**
	 * Returns the DOI for the data package quality report resource identified by
	 * the scope, identifier, and revision.
	 * 
	 * @param scope
	 * @param identifier
	 * @param revision
	 * @return DOI for the data package quality report resource
	 * @throws Exception
	 */
	public String readDataPackageReportDoi(String scope, Integer identifier,
	    String revision) throws Exception {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/report/doi/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;

	}

	/**
	 * Returns the DOI for the data entity resource identified by the scope,
	 * identifier, revision, and entity identifier.
	 * 
	 * @param scope
	 * @param identifier
	 * @param revision
	 * @param entityId
	 * @return DOI for the data entity resource
	 * @throws Exception
	 */
	public String readDataEntityDoi(String scope, Integer identifier,
	    String revision, String entityId) throws Exception {

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision,
		    entityId);
		String url = BASE_URL + "/data/doi/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		String entityString = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			entityString = EntityUtils.toString(httpEntity);
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return entityString;

	}

	/**
	 * Executes the 'searchDataPackages' web service method.
	 * 
	 * @param pathQuery
	 *          an XML pathquery string (conforming to Metacat pathquery syntax)
	 * @return an XML resultset document (conforming to Metacat pathquery syntax)
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String searchDataPackages(String pathQuery) throws Exception {
		String contentType = "application/xml";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpPut httpPut = new HttpPut(BASE_URL + "/search/eml");
		String resultSetXML = null;

		// Set header content
		if (this.token != null) {
			httpPut.setHeader("Cookie", "auth-token=" + this.token);
		}
		httpPut.setHeader("Content-Type", contentType);

		// Set the request entity
		HttpEntity stringEntity = new StringEntity(pathQuery);
		httpPut.setEntity(stringEntity);

		try {
			HttpResponse httpResponse = httpClient.execute(httpPut);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);
			if (statusCode == HttpStatus.SC_OK) {
				resultSetXML = entityString;
			} else {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return resultSetXML;
	}

	/**
	 * Executes the 'updateDataPackage' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param emlFile
	 *          the Level-0 EML document describing the data package to be updated
	 * @return a string representation of the resource map for the updated data
	 *         package
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String updateDataPackage(String scope, Integer identifier, File emlFile)
	    throws Exception {
		
		final String contentType = "application/xml";
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), null, null);
		final String url = BASE_URL + "/eml" + urlTail;
		HttpPut httpPut = new HttpPut(url);
		String resourceMap = null;

		// Set header content
		if (this.token != null) {
			httpPut.setHeader("Cookie", "auth-token=" + this.token);
		}
		httpPut.setHeader("Content-Type", contentType);

		// Set the request entity
		HttpEntity fileEntity = new FileEntity(emlFile, ContentType.create(contentType));
		httpPut.setEntity(fileEntity);

		try {
			HttpResponse httpResponse = httpClient.execute(httpPut);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);
			
			if (statusCode == HttpStatus.SC_ACCEPTED) {
				
				EmlPackageId emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				String packageScope = emlPackageId.getScope();
				Integer packageIdentifier = emlPackageId.getIdentifier();
				Integer packageRevision = emlPackageId.getRevision();

				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition 
				Thread.sleep(initialSleepTime);
				
				while (idleTime <= maxIdleTime) {
					logger.info(idleTime);
					try {
						String errorText = readDataPackageError(entityString);
						throw new Exception(errorText);
					} catch (ResourceNotFoundException e) {
						logger.error(e.getMessage());
						try {
							resourceMap = readDataPackage(packageScope, packageIdentifier,
									packageRevision.toString());
							break;
						} catch (ResourceNotFoundException e1) {
							logger.error(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				if (idleTime > maxIdleTime) {
					String gripe = "Fiddle sticks!  Updating this data package has "
					    + "exceeded our patience and we have been forced to terminate "
					    + "this browser process, but the data package may still be "
					    + "created in PASTA if an error was not encountered.  Please "
					    + "check the audit logs or the Data Package Browser at a later "
					    + "time.";
					throw new Exception(gripe);
				}

			} else {
				handleStatusCode(statusCode, entityString);
			}
		} finally {
			closeHttpClient(httpClient);
		}

		return resourceMap;
	}

	/**
	 * Returns the content type of the last operation that sets it.
	 * 
	 * @return The content type as a String object
	 */
	public String getContentType() {
		String contentType = this.contentType;
		return contentType;
	}

	/**
	 * Returns the PASTA data package resource URI.
	 * 
	 * @param scope
	 * @param identifier
	 * @param revision
	 * @return PASTA data package resource URI
	 */
	public String getPastaPackageUri(String scope, Integer identifier,
	    String revision) {

		String uri = null;

		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		uri = this.pastaUriHead + "eml" + urlTail;

		return uri;

	}

	private void getInfo(HttpResponse r) {

		Header[] headers = r.getAllHeaders();

		for (int i = 0; i < headers.length; i++) {
			System.out.println(headers[i].getName() + ": " + headers[i].getValue());
		}

		HttpEntity re = r.getEntity();

		if (re.isChunked()) {
			System.out.println("Entity is chunked");
		} else {
			System.out.println("Entity is not chunked");
		}

		if (re.isStreaming()) {
			System.out.println("Entity is streaming");
		} else {
			System.out.println("Entity is not streaming");
		}

		if (re.isRepeatable()) {
			System.out.println("Entity is repeatable");
		} else {
			System.out.println("Entity is not repeatable");
		}

	}
}
