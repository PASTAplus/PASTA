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
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author dcosta
 * @since April 2, 2012
 * 
 *  The DataPackageManagerClient supports the management of data packages
 *  in the NIS Data Portal. It interacts directly with the 
 *  DataPackageManager PASTA web service.
 * 
 */
public class DataPackageManagerClient extends PastaClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.DataPackageManagerClient.class);

  static final String pathqueryXML = 
    "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
    "<pathquery version=\"1.0\">\n" +
    "  <meta_file_id>unspecified</meta_file_id>\n" +
    "  <querytitle>unspecified</querytitle>\n" +
    "  <returnfield>dataset/title</returnfield>\n" +
    "  <returnfield>keyword</returnfield>\n" +
    "  <returnfield>originator/individualName/surName</returnfield>\n" +
    "  <returndoctype>eml://ecoinformatics.org/eml-2.0.0</returndoctype>\n" +
    "  <returndoctype>eml://ecoinformatics.org/eml-2.0.1</returndoctype>\n" +
    "  <returndoctype>eml://ecoinformatics.org/eml-2.1.0</returndoctype>\n" +
    "  <querygroup operator=\"UNION\">\n" +
    "    <queryterm casesensitive=\"false\" searchmode=\"contains\">\n" +
    "      <value>bug</value>\n" +
    "      <pathexpr>dataset/title</pathexpr>\n" +
    "    </queryterm>\n" +
    "    <queryterm casesensitive=\"false\" searchmode=\"contains\">\n" +
    "      <value>Carroll</value>\n" +
    "      <pathexpr>surName</pathexpr>\n" +
    "    </queryterm>\n" +
    "  </querygroup>\n" +
    "</pathquery>\n";


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
    String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol, this.pastaHost, this.pastaPort);
    this.BASE_URL = pastaUrl + "/package";
  }

  
  /*
   * Class Methods
   */
  
  /**
   * Determine the test identifier used for testing data package 
   * operations. Eliminate identifiers that were previously deleted 
   * or are currently in use.
   * 
   * @param  dpmClient  the DataPackageManagerClient object
   * @param  scope the scope value, e.g. "knb-lter-lno"
   * @return  an integer value appropriate for use as a test identifier
   */
  static Integer determineTestIdentifier(DataPackageManagerClient dpmClient, 
                                         String scope)
          throws Exception {
    Integer identifier = null;
    
    /*
     * Determine the test identifier. Eliminate identifiers that were
     * previously deleted or are currently in use.
     */
    TreeSet<String> deletedSet = new TreeSet<String>();
    String deletedDataPackages = dpmClient.listDeletedDataPackages();
    String[] deletedArray = deletedDataPackages.split("\n");
    for (int i = 0; i < deletedArray.length; i++) {
      if (deletedArray[i] != null && 
          !deletedArray[i].equals("") &&
          deletedArray[i].startsWith(scope)
          ) {
        deletedSet.add(deletedArray[i]);
      }
    }

    TreeSet<String> identifierSet = new TreeSet<String>();
    String dataPackageIdentifiers = dpmClient.listDataPackageIdentifiers(scope);
    String[] identifierArray = dataPackageIdentifiers.split("\n");
    for (int i = 0; i < identifierArray.length; i++) {
      if (identifierArray[i] != null && 
          !identifierArray[i].equals("")
          ) {
        identifierSet.add(identifierArray[i]);
      }
    }
    
    int identifierValue = 1;
    while (identifier == null) {
      String identifierString = "" + identifierValue;
      String scopeDotIdentifier = scope + "." + identifierValue;
      if (!deletedSet.contains(scopeDotIdentifier) &&
          !identifierSet.contains(identifierString)
         ) {
        identifier = new Integer(identifierValue);
      }
      else {
        identifierValue++;
      }
    }
    
    return identifier;
  }
  
  
  /**
   * main() program. Can be used as a lightweight unit test
   * to test the methods in this class.
   * 
   * @param args  No command arguments are passed to this program.
   */
  public static void main(String[] args) {
    String user = "ucarroll";
    String scope = "knb-lter-lno";
    Integer identifier = null;
    String revision = "1";
    String entityId = "NoneSuchBugCount";
    
    ConfigurationListener.configure();

    try {
      DataPackageManagerClient dpmClient = new DataPackageManagerClient(user);

      String dataPackageScopes = dpmClient.listDataPackageScopes();
      System.out.println("\nData package scopes:\n" + dataPackageScopes);
         
      // Create the test data package in PASTA
      identifier = determineTestIdentifier(dpmClient, scope);
      String testEMLPath = "test/data/NoneSuchBugCount.xml";
      File testEMLFile = new File(testEMLPath);
      String createPackageId = scope + "." + identifier.toString() + "." + revision;
      modifyTestEmlFile(testEMLFile, scope, createPackageId);
      String resourceMap = dpmClient.createDataPackage(testEMLFile);
      System.out.println("\nResource map:\n" + resourceMap);
   
      // Update the test data package in PASTA
      String dataPackageRevisions = dpmClient.listDataPackageRevisions(scope, identifier);
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
      String updatePackageId = scope + "." + identifier.toString() + "." + updateRevision;     
      modifyTestEmlFile(testEMLFile, scope, updatePackageId);
      resourceMap = dpmClient.updateDataPackage(scope, identifier, testEMLFile);
      System.out.println("\nResource map:\n" + resourceMap);

      String dataEntities = dpmClient.listDataEntities(scope, identifier, revision);
      System.out.println("\nData entities:\n" + dataEntities);
      
      String dataPackage = dpmClient.readDataPackage(scope, identifier, revision);
      System.out.println("\nData package:\n" + dataPackage);

      String metadata = dpmClient.readMetadata(scope, identifier, revision);
      System.out.println("\nMetadata:\n" + metadata);
      
      byte[] dataEntity = dpmClient.readDataEntity(scope, identifier, revision, entityId);
      System.out.println("\nData entity:\n" + dataEntity);
      
      String dataPackageReport = dpmClient.readDataPackageReport(scope, identifier, revision);
      System.out.println("\nData package report:\n" + dataPackageReport);

      String resultSetXML = dpmClient.searchDataPackages(pathqueryXML);
      System.out.println("\nResult set XML:\n" + resultSetXML);
      
      // Delete the test data package from PASTA
      dpmClient.deleteDataPackage(scope, identifier);
      System.out.println("\nDeleted data package: " + scope + "." + identifier);
    }
    catch (Exception e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
  }
  
  
  /**
   * Modifies the packageId value in a test EML file. Useful for testing
   * purposes.
   * 
   * @param testEmlFile     The test EML document file
   * @param scope           The scope value, e.g. "knb-lter-lno"
   * @param newPackageId    The packageId string to write to the file as the
   *                        new value of the packageId attribute, e.g.
   *                        "knb-lter-lno.100.1"
   */
  public static void modifyTestEmlFile(File testEmlFile, String scope, String newPackageId) {
    boolean append = false;
    String xmlString = FileUtility.fileToString(testEmlFile);
    Pattern pattern = Pattern.compile(scope + "\\.\\d+\\.\\d+");
    Matcher matcher = pattern.matcher(xmlString);  
    // Replace packageId value with new packageId value
    String modifiedXmlString = matcher.replaceAll(newPackageId);          
    FileWriter fileWriter;

    try {
      fileWriter = new FileWriter(testEmlFile);
      StringBuffer stringBuffer = new StringBuffer(modifiedXmlString);
      FileUtils.writeStringToFile(testEmlFile, modifiedXmlString, append);
    }
    catch (IOException e) {
      fail("IOException modifying packageId in test EML file: " + e.getMessage());
    }
  }
  
  
  /*
   * Instance Methods
   */
  
  /*
   * Documentation for the Data Package Manager web service 
   * methods and the status codes they return can be found at 
   * http://package.lternet.edu/package/docs/api
   */
  
  /**
   * Executes the 'createDataPackage' web service method.
   * 
   * @param  emlFile  the Level-0 EML document describing the data package
   * @return a string representation of the resource map for the
   *         newly created data package
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String createDataPackage(File emlFile) throws Exception {
    String contentType = "application/xml";
    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(BASE_URL + "/eml");
    String resourceMap = null;

    // Set header content
    if (this.token != null) {
      httpPost.setHeader("Cookie", "auth-token=" + this.token);
    }
    httpPost.setHeader("Content-Type", contentType);

    // Set the request entity
    HttpEntity fileEntity = new FileEntity(emlFile, contentType);
    httpPost.setEntity(fileEntity);

    try {
      HttpResponse httpResponse = httpClient.execute(httpPost);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      HttpEntity httpEntity = httpResponse.getEntity();
      String entityString = EntityUtils.toString(httpEntity);
      if (statusCode == HttpStatus.SC_OK) {
        resourceMap = entityString;
      }
      else {
        handleStatusCode(statusCode, entityString);
      }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return resourceMap;
  }

  
  /**
   * Executes the 'deleteDataPackage' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @return an empty string if the data package was successfully deleted
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String deleteDataPackage(String scope, Integer identifier) 
          throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }

  
  /**
   * Executes the 'evaluateDataPackage' web service method.
   * @param  emlFile  the Level-0 EML document describing the data package
   *         to be evaluated
   * @return a string holding the XML quality report document resulting
   *         from the evaluation
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String evaluateDataPackage(File emlFile) throws Exception {
    String contentType = "application/xml";
    HttpClient httpClient = new DefaultHttpClient();
    HttpPost httpPost = new HttpPost(BASE_URL + "/evaluate/eml");
    String qualityReport = null;

    // Set header content
    if (this.token != null) {
      httpPost.setHeader("Cookie", "auth-token=" + this.token);
    }
    httpPost.setHeader("Content-Type", contentType);

    // Set the request entity
    HttpEntity fileEntity = new FileEntity(emlFile, contentType);
    httpPost.setEntity(fileEntity);

    try {
      HttpResponse httpResponse = httpClient.execute(httpPost);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      HttpEntity httpEntity = httpResponse.getEntity();
      String entityString = EntityUtils.toString(httpEntity);
      if (statusCode == HttpStatus.SC_OK) {
        qualityReport = entityString;
      }
      else {
        handleStatusCode(statusCode, entityString);
      }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return qualityReport;
  }


  /**
   * Executes the 'listDataEntities' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @param revision the revision value, e.g. "1" or "newest"
   * @return a newline-separated list of data entity identifiers
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String listDataEntities(String scope, Integer identifier, String revision) 
          throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }

  
  /**
   * Executes the 'listDataPackageIdentifiers' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @return a newline-separated list of identifier values
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String listDataPackageIdentifiers(String scope)
          throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
      if (statusCode != HttpStatus.SC_OK && statusCode != HttpStatus.SC_NOT_FOUND) {
        handleStatusCode(statusCode, entityString);
      }
      else if (statusCode == HttpStatus.SC_NOT_FOUND) {
        entityString = "";
      }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }

  
  /**
   * Executes the 'listDataPackageRevisions' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @return a newline-separated list of revision values
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String listDataPackageRevisions(String scope, Integer identifier)
      throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    String urlTail = makeUrlTail(scope, identifier.toString(), null, null);
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }
  

  /**
   * Executes the 'listDataPackageScopes' web service method.
   * @return a newline-separated list of scope values
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String listDataPackageScopes() 
          throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }

  
  /**
   * Executes the 'listDeletedDataPackages' web service method.
   * @return a newline-separated list of packageId strings representing
   *         all the data packages that have been deleted from PASTA
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String listDeletedDataPackages()
          throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }

  
  /**
   * Executes the 'readDataEntity' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @param revision the revision value, e.g. "1"
   * @param entityId the entity identifier string, e.g. "NoneSuchBugCount"
   * @return a byte array containing the data entity
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public byte[] readDataEntity(String scope, Integer identifier,
      String revision, String entityId) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
    String urlTail = makeUrlTail(scope, identifier.toString(), revision,
        entityId);
    String url = BASE_URL + "/data/eml" + urlTail;
    HttpGet httpGet = new HttpGet(url);
    byte[] byteArray = null;

    // Set header content
    if (this.token != null) {
      httpGet.setHeader("Cookie", "auth-token=" + this.token);
    }

    try {
      HttpResponse httpResponse = httpClient.execute(httpGet);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      HttpEntity httpEntity = httpResponse.getEntity();
      byteArray = EntityUtils.toByteArray(httpEntity);
      ContentType contentType = ContentType.getOrDefault(httpEntity);
      this.contentType = contentType.toString();
      if (statusCode != HttpStatus.SC_OK) {
        handleStatusCode(statusCode, new String(byteArray));
      }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return byteArray;
  }


  /**
   * Executes the 'readDataPackage' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @param revision the revision value, e.g. "1"
   * @return the data package resource map
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String readDataPackage(String scope, 
                                Integer identifier,
                                String revision) 
        throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }

  
  /**
   * Executes the 'readDataPackageReport' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @param revision the revision value, e.g. "1"
   * @return the XML quality report document for the specified data package
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String readDataPackageReport(String scope, Integer identifier,
      String revision) throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }


  /**
   * Executes the 'readMetadata' web service method.
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @param revision the revision value, e.g. "1"
   * @return the Level-1 EML metadata document for the specified data package
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String readMetadata(String scope, Integer identifier, String revision)
      throws Exception {
    HttpClient httpClient = new DefaultHttpClient();
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
      entityString = EntityUtils.toString(httpEntity);
      if (statusCode != HttpStatus.SC_OK) {
        handleStatusCode(statusCode, entityString);
      }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return entityString;
  }


  /**
   * Executes the 'searchDataPackages' web service method.
   * @param pathQuery an XML pathquery string (conforming to Metacat pathquery syntax)
   * @return an XML resultset document (conforming to Metacat pathquery syntax)
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String searchDataPackages(String pathQuery)
    throws Exception {
    String contentType = "application/xml";
    HttpClient httpClient = new DefaultHttpClient();
    HttpPut httpPut = new HttpPut(BASE_URL + "/eml/search");
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
      }
      else {
        handleStatusCode(statusCode, entityString);
      }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return resultSetXML;
  }

  
  /**
   * Executes the 'updateDataPackage' web service method. 
   * @param scope the scope value, e.g. "knb-lter-lno"
   * @param identifier the identifier value, e.g. 10
   * @param  emlFile  the Level-0 EML document describing the data package
   *            to be updated
   * @return a string representation of the resource map for the
   *         updated data package
   * @see <a target="top" href="http://package.lternet.edu/package/docs/api">Data Package Manager web service API</a>   
   */
  public String updateDataPackage(String scope, Integer identifier, File emlFile) 
          throws Exception {
      final String contentType = "application/xml";
      HttpClient httpClient = new DefaultHttpClient();
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
      HttpEntity fileEntity = new FileEntity(emlFile, contentType);
      httpPut.setEntity(fileEntity);

      try {
        HttpResponse httpResponse = httpClient.execute(httpPut);
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        HttpEntity httpEntity = httpResponse.getEntity();
        String entityString = EntityUtils.toString(httpEntity);
        if (statusCode == HttpStatus.SC_OK) {
          resourceMap = entityString;
        }
        else {
          handleStatusCode(statusCode, entityString);
        }
      }
      finally {
        httpClient.getConnectionManager().shutdown();
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
  
}
