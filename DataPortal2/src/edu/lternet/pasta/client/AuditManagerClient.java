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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.lternet.pasta.client.RecentUpload.Service;


/**
 * @author servilla
 * @since May 2, 2012
 * 
 *        The AuditManagerClient provides an interface to PASTA's Audit Manager
 *        service. Specifically, this class supports access to the Audit Manager
 *        reports.
 * 
 */
public class AuditManagerClient extends PastaClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.AuditManagerClient.class);
  
  private static List<RecentUpload> recentUploads = null;
  private static long lastRefreshTime = 0L; 

  
  /*
   * Instance variables
   */

  private final String BASE_URL;
  
  
  /*
   * Constructors
   */

  /**
   * Creates a new AuditManagerClient object.
   * 
   * @param uid
   *          The user's identifier as a String object.
   * 
   * @throws PastaAuthenticationException
   * @throws PastaConfigurationException
   */
  public AuditManagerClient(String uid) throws PastaAuthenticationException,
      PastaConfigurationException {

    super(uid);
    String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol, this.pastaHost, this.pastaPort);
    this.BASE_URL = pastaUrl + "/audit";
  }
  
  
  /*
   * Class methods
   */
  
  public static List<RecentUpload> getRecentInserts() {
	  List<RecentUpload> recentUploads = getRecentUploads();
	  List<RecentUpload> recentInserts = new ArrayList<RecentUpload>();
	  
	  for (RecentUpload recentUpload : recentUploads) {
		  if (recentUpload.getService() == Service.INSERT) {
			  recentInserts.add(recentUpload);
		  }
	  }
	  
	  return recentInserts;  
  }
  
  
  public static List<RecentUpload> getRecentUpdates() {
	  List<RecentUpload> recentUploads = getRecentUploads();
	  List<RecentUpload> recentUpdates = new ArrayList<RecentUpload>();
	  
	  for (RecentUpload recentUpload : recentUploads) {
		  if (recentUpload.getService() == Service.UPDATE) {
			  recentUpdates.add(recentUpload);
		  }
	  }
	  
	  return recentUpdates;  
  }
  
  
  public static List<RecentUpload> getRecentUploads() {
	  if (recentUploads == null || shouldRefresh()) {
		  try {
		     AuditManagerClient auditManagerClient = new AuditManagerClient("public");
		     recentUploads = auditManagerClient.recentUploads();
		     Date now = new Date();
		     lastRefreshTime = now.getTime();
		  }
		  catch (Exception e) {
			  logger.error("Error refreshing recent uploads: " + e.getMessage());
		  }
	  }
	  
	  return recentUploads;	  
  }
  
  
  private static boolean shouldRefresh() {
	  boolean shouldRefresh = false;
	  final long twelveHours = 12 * 60 * 60 * 1000;
	  Date now = new Date();
	  long nowTime = now.getTime();
	  long refreshTime = lastRefreshTime + twelveHours;
	  
	  if (refreshTime < nowTime) {
		  shouldRefresh = true;
	  }
	  
	  return shouldRefresh;
  }
  
  
  /*
   * Instance Methods
   */


  /**
   * 
   * @param oid
   * @return
   * @throws PastaEventException
   */
  public String reportByOid(String oid) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL + "/report/" + oid);

    // Set header content
    if (this.token != null) {
      httpGet.setHeader("Cookie", "auth-token=" + this.token);
    }

    try {

      response = httpClient.execute(httpGet);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      responseEntity = response.getEntity();

      if (responseEntity != null) {
        entity = EntityUtils.toString(responseEntity);
      }

    } catch (ClientProtocolException e) {
      logger.error(e);
      e.printStackTrace();
    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    if (statusCode != HttpStatus.SC_OK) {

      // Something went wrong; return message from the response entity
      String gripe = "The AuditManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return entity;

  }

  
  /**
   * Gets a list of recent uploads to PASTA.
   * 
   * @return
   * @throws PastaEventException
   */
	public List<RecentUpload> recentUploads() throws PastaEventException {
		List<RecentUpload> recent = new ArrayList<RecentUpload>();
		String entity = null;
		Integer statusCode = null;
		HttpEntity responseEntity = null;

		HttpClient httpClient = new DefaultHttpClient();
		HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
		HttpResponse response = null;
		HttpGet httpGet = new HttpGet(BASE_URL + "/recent-uploads");

		// Set header content
		if (this.token != null) {
			httpGet.setHeader("Cookie", "auth-token=" + this.token);
		}

		try {

			response = httpClient.execute(httpGet);
			statusCode = (Integer) response.getStatusLine().getStatusCode();
			responseEntity = response.getEntity();

			if (responseEntity != null) {
				entity = EntityUtils.toString(responseEntity);
				recent = parseRecentUploads(entity);
			}

		}
		catch (ClientProtocolException e) {
			logger.error(e);
			e.printStackTrace();
		}
		catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
		finally {
			httpClient.getConnectionManager().shutdown();
		}

		if (statusCode != HttpStatus.SC_OK) {

			// Something went wrong; return message from the response entity
			String gripe = "The AuditManager responded with response code '"
					+ statusCode.toString() + "' and message '" + entity
					+ "'\n";
			throw new PastaEventException(gripe);

		}

		return recent;

	}
	
	
	private List<RecentUpload> parseRecentUploads(String xmlString)
	        throws PastaEventException {
		List<RecentUpload> recent = new ArrayList<RecentUpload>();
		
	    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance(); 
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputStream inputStream = IOUtils.toInputStream(xmlString, "UTF-8");
			Document document = documentBuilder.parse(inputStream);
			Element documentElement = document.getDocumentElement();
			NodeList auditRecordList = documentElement.getElementsByTagName("auditRecord");
			
			for (int i = 0; i < auditRecordList.getLength(); i++) {
				Node auditRecordNode = auditRecordList.item(i);
				NodeList auditRecordChildren = auditRecordNode.getChildNodes();
				String uploadDate = null;
				String serviceMethod = null;
				String url = null;
				for (int j = 0; j < auditRecordChildren.getLength(); j++) {
					Node childNode = auditRecordChildren.item(j);
				    if (childNode instanceof Element) {
					    Element auditRecordElement = (Element) childNode;
					    if (auditRecordElement.getTagName().equals("entryTime")) {
						    Text text = (Text) auditRecordElement.getFirstChild();
						    uploadDate = text.getData().trim();
					    }
					    else if (auditRecordElement.getTagName().equals("serviceMethod")) {
						    Text text = (Text) auditRecordElement.getFirstChild();
						    serviceMethod = text.getData().trim();
					    }
					    else if (auditRecordElement.getTagName().equals("resourceId")) {
						    Text text = (Text) auditRecordElement.getFirstChild();
						    url = text.getData().trim();
					    }
				    }
				}
			    RecentUpload recentUpload = new RecentUpload(uploadDate, serviceMethod, url);
			    recent.add(recentUpload);
			}
		}
		catch (Exception e) {
			logger.error("Exception:\n" + e.getMessage());
			e.printStackTrace();
			throw new PastaEventException(e.getMessage());
		}
		
		return recent;
	}
	
	
  /**
   * Returns an audit report based on the provided query parameter filter.
   * 
   * @param filter The query parameter filter as a String object.
   * @return The XML document of the report as a String object.
   * @throws PastaEventException
   */
  public String reportByFilter(String filter) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL + "/report?" + filter);

    // Set header content
    if (this.token != null) {
      httpGet.setHeader("Cookie", "auth-token=" + this.token);
    }

    try {

      response = httpClient.execute(httpGet);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      responseEntity = response.getEntity();

      if (responseEntity != null) {
        entity = EntityUtils.toString(responseEntity);
      }

    } catch (ClientProtocolException e) {
      logger.error(e);
      e.printStackTrace();
    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    } finally {
      httpClient.getConnectionManager().shutdown();
    }

    if (statusCode != HttpStatus.SC_OK) {

      // Something went wrong; return message from the response entity
      String gripe = "The AuditManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return entity;

  }

}
