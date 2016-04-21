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
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;

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
		String serviceMethod = "createDataPackage";
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
				String transactionId = entityString;
				EmlPackageId emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				EmlPackageIdFormat epif = new EmlPackageIdFormat();
				String packageId = epif.format(emlPackageId);
				String packageScope = emlPackageId.getScope();
				Integer packageIdentifier = emlPackageId.getIdentifier();
				Integer packageRevision = emlPackageId.getRevision();

				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition 
				Thread.sleep(initialSleepTime);
				
				while (idleTime <= maxIdleTime) {
					logIdleTime(serviceMethod, emlPackageId.toString(), idleTime);
					
					try {
						String errorText = readDataPackageError(transactionId);
						throw new Exception(errorText);
					} 
					catch (ResourceNotFoundException e) {
						logger.info(e.getMessage());
						
						try {
							resourceMap = readDataPackage(packageScope, packageIdentifier,
									packageRevision.toString());
							break;
						} 
						catch (ResourceNotFoundException e1) {
							logger.info(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				fiddlesticks(serviceMethod, idleTime, packageId, transactionId);

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
	public String getDataPackageArchive(String scope, Integer identifier, String revision, HttpServletResponse servletResponse) 
			throws Exception {
		String serviceMethod = "getDataPackageArchive";
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
			EmlPackageId emlPackageId = new EmlPackageId(scope, identifier, new Integer(revision));
			EmlPackageIdFormat epif = new EmlPackageIdFormat();
			String packageId = epif.format(emlPackageId);
			
			if (statusCode == HttpStatus.SC_ACCEPTED) {
				String transactionId = entityString;
				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition 
				Thread.sleep(initialSleepTime);
				
				while (idleTime <= maxIdleTime) {
					logIdleTime(serviceMethod, emlPackageId.toString(), idleTime);
					
					try {
						String errorText = readDataPackageError(transactionId);
						throw new Exception(errorText);
					} 
					catch (ResourceNotFoundException e) {
						logger.info(e.getMessage());
						
						try {
							readDataPackageArchive(scope, identifier, revision, transactionId, servletResponse);
							break;
						} 
						catch (ResourceNotFoundException e1) {
							logger.info(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				fiddlesticks(serviceMethod, idleTime, packageId, transactionId);

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
		String serviceMethod = "evaluateDataPackage";
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
				String transactionId = entityString;
				EmlPackageId emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				
				if (emlPackageId != null) {
					EmlPackageIdFormat epif = new EmlPackageIdFormat();
					String packageId = epif.format(emlPackageId);
					String scope = emlPackageId.getScope();
					Integer identifier = emlPackageId.getIdentifier();
					Integer revision = emlPackageId.getRevision();
					Integer idleTime = 0;

					/*
					 * Initial sleep period to mitigate potential error-check race condition
					 */
					Thread.sleep(initialSleepTime);

					while (idleTime <= maxIdleTime) {
						logIdleTime(serviceMethod, emlPackageId.toString(), idleTime);

						try {
							String errorText = readDataPackageError(transactionId);
							throw new Exception(errorText);
						}
						catch (ResourceNotFoundException e) {
							/*
							 * The transactionId is no longer found, so the transaction has completed.
							 */
							logger.info(e.getMessage());

							try {
								qualityReport = readEvaluateReport(scope, identifier, revision.toString(), transactionId);
								break;
							}
							catch (ResourceNotFoundException e1) {
								logger.info(e1.getMessage());
								Thread.sleep(idleSleepTime);
								idleTime += idleSleepTime;
							}
						}
					}

					fiddlesticks(serviceMethod, idleTime, packageId, transactionId);
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
	
	
	/*
	 * Throw an exception if the amount of idle time exceeds the maximum value.
	 * Generate a message appropriate to the operation being performed.
	 */
	private void fiddlesticks(String serviceMethod, Integer idleTime, String packageId, String transactionId) 
			throws PastaIdleTimeException {
		String msg = null;
		String verb = "";
		String advice = "";
		String archiveAdvice = "";
		String evaluateURL = String.format("%s/evaluate/report/eml/%s", this.BASE_URL, transactionId);
		String evaluateAdvice = 
				String.format("You may check the availability of an evaluate report at a later time using the following URL: %s",
						     evaluateURL);
		
		String uploadAdvice = String.format("Please check the <a class='searchsubcat' href='scopebrowse'>Browse Data by Package Identifier</a> " +
		                                    "page at a later time to see if '%s' was successfully uploaded.",
		                                    packageId);
		
		if (idleTime > maxIdleTime) {

			switch(serviceMethod) {
		    	case "createDataPackage":
		    		verb = "uploading";
					advice = uploadAdvice;
		    		break;
		    	case "evaluateDataPackage": 
		    		verb = "evaluating";
		    		advice = evaluateAdvice; 		
		    		break;
		    	case "getDataPackageArchive": 
		    		verb = "creating an archive of";
		    		advice = archiveAdvice;
		    		break;
		    	case "updateDataPackage":
		    		verb = "uploading";
					advice = uploadAdvice;
		    		break;
		    	default: 
		    		throw new IllegalArgumentException("Unknown service method: " + serviceMethod);
		    }
			
			msg = String.format("PASTA is still %s data package '%s' so we won't keep your browser waiting any longer. %s", 
					            verb, packageId, advice);
	
			throw new PastaIdleTimeException(msg);
		}
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
	 * Executes the 'listDataDescendants' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1" or "newest"
	 * @return a newline-separated list of data package identifiers,
	 *         possibly empty
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDataDescendants(String scope, Integer identifier,
	    String revision) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/descendants/eml" + urlTail;
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
	 * Executes the 'listDataSources' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1" or "newest"
	 * @return a newline-separated list of data package metadata identifiers,
	 *         possibly empty
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listDataSources(String scope, Integer identifier,
	    String revision) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/sources/eml" + urlTail;
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
	 * Executes the 'listRecentUploads' web service method.
	 * 
	 * @return an XML string representing a list of recent data package
	 *         inserts or updates, up to the specified limit
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listRecentUploads(String serviceMethod, int limit) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String type = serviceMethod.equalsIgnoreCase("createDataPackage") ? "insert" : "update";
		String url = String.format("%s/uploads/eml?type=%s&limit=%d", BASE_URL, type, limit);
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
	 * Executes the 'listServiceMethods' web service method.
	 * 
	 * @return a newline-separated list of service method names representing all the
	 *         service methods supported by the Data Package Manager
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String listServiceMethods() throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String url = BASE_URL + "/service-methods";
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
	 * Executes the 'readDataEntityNames' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * 
	 * @return a list of data entity identifiers and their corresponding entity names,
	 *         one entity per line with id and name separated by a comma. Note that
	 *         many entity names themselves contain commas, so when parsing the return
	 *         value, split the string only up to the first occurrence of a comma.
	 */
	public String readDataEntityNames(String scope, Integer identifier, String revision) 
			throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
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
		} 
		finally {
			closeHttpClient(httpClient);
		}

		return entityString;
	}

	
	/**
	 * Executes the 'readDataEntitySize' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * @param entityId
	 *          the entity identifier string, e.g. "NoneSuchBugCount"
	 * @return the size of the data entity resource in bytes
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public Long readDataEntitySize(String scope, Integer identifier,
	    String revision, String entityId) throws Exception {

		// Re-encode "%" to its character reference value of %25 to mitigate
		// an issue with the HttpGet call that performs the decoding - this is
		// a kludge to deal with encoding nonsense.
		entityId = entityId.replace("%", "%25");

		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision,
		    entityId);
		String url = BASE_URL + "/data/size/eml" + urlTail;
		HttpGet httpGet = new HttpGet(url);
		Long entitySize = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
			int statusCode = httpResponse.getStatusLine().getStatusCode();
			HttpEntity httpEntity = httpResponse.getEntity();
			String entityString = EntityUtils.toString(httpEntity);
			if (entityString != null) {				
				try {
					entitySize = new Long(entityString);
				}
				catch (NumberFormatException e) {
					logger.error("Unable to determine entity size of entity: " + entityId);
				}
			}
			ContentType contentType = ContentType.getOrDefault(httpEntity);
			this.contentType = contentType.toString();
			if (statusCode != HttpStatus.SC_OK) {
				handleStatusCode(statusCode, entityString);
			}
		} 
		finally {
			closeHttpClient(httpClient);
		}

		return entitySize;
	}

	
	/**
	 * Executes the 'readDataEntitySizes' web service method.
	 * 
	 * @param scope
	 *          the scope value, e.g. "knb-lter-lno"
	 * @param identifier
	 *          the identifier value, e.g. 10
	 * @param revision
	 *          the revision value, e.g. "1"
	 * 
	 * @return a list of data entities and their corresponding entity sizes in bytes
	 */
	public String readDataEntitySizes(String scope, Integer identifier, String revision) 
			throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		String urlTail = makeUrlTail(scope, identifier.toString(), revision, null);
		String url = BASE_URL + "/data/size/eml" + urlTail;
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
		} 
		finally {
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
	
	
	private void logIdleTime(String methodName, String packageId, Integer idleTime) {
		logger.info(String.format("%s: %s; Idle Time: %d", methodName, packageId, idleTime));
	}

	
	/**
	 * Executes the 'searchDataPackages' web service method.
	 * 
	 * @param solrQuery
	 *          a Solr query string
	 * @return an XML resultset document
	 * @see <a target="top"
	 *      href="http://package.lternet.edu/package/docs/api">Data Package
	 *      Manager web service API</a>
	 */
	public String searchDataPackages(String solrQuery) throws Exception {
		CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpGet httpGet = new HttpGet(BASE_URL + "/search/eml?" + solrQuery);
		String resultSetXML = null;

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {
			HttpResponse httpResponse = httpClient.execute(httpGet);
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
		String serviceMethod = "updateDataPackage";
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
				String transactionId = entityString;
				EmlPackageId emlPackageId = EmlUtility.emlPackageIdFromEML(emlFile);
				EmlPackageIdFormat epif = new EmlPackageIdFormat();
				String packageId = epif.format(emlPackageId);
				String packageScope = emlPackageId.getScope();
				Integer packageIdentifier = emlPackageId.getIdentifier();
				Integer packageRevision = emlPackageId.getRevision();

				Integer idleTime = 0;

				// Initial sleep period to mitigate potential error-check race condition 
				Thread.sleep(initialSleepTime);
				
				while (idleTime <= maxIdleTime) {
					logIdleTime(serviceMethod, emlPackageId.toString(), idleTime);
					
					try {
						String errorText = readDataPackageError(transactionId);
						throw new Exception(errorText);
					} 
					catch (ResourceNotFoundException e) {
						logger.info(e.getMessage());
						
						try {
							resourceMap = readDataPackage(packageScope, packageIdentifier, packageRevision.toString());
							break;
						} 
						catch (ResourceNotFoundException e1) {
							logger.info(e1.getMessage());
							Thread.sleep(idleSleepTime);
							idleTime += idleSleepTime;
						}
					}
				}

				fiddlesticks(serviceMethod, idleTime, packageId, transactionId);

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
