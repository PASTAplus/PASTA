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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;


/**
 * @author costa
 * @since June 26, 2014
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
  
  /*
   * The cache of RecentUpload objects.
   */
  private static List<RecentUpload> recentInserts = null;
  private static List<RecentUpload> recentUpdates = null;
  private static long recentInsertsLastRefreshTime = 0L;
  private static long recentUpdatesLastRefreshTime = 0L;
  
  
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
  
  /**
   * Retrieves a list of recent inserts.
   * 
   * @param numberOfDays   the number of prior days to search for inserts, for example,
   *                       the past 100 days
   * @param limit          an upper limit on the number of matches returned
   * @param forceRefresh   if true, refresh the search results regardless of when 
   *                       they were last refreshed
   * 
   * @return A list of RecentUpload objects, where each upload was an insert,
   *         i.e. the serviceMethod for each is "createDataPackage".
   */
	synchronized public static List<RecentUpload> getRecentInserts(Integer numberOfDays, Integer limit, boolean forceRefresh) {
		if (forceRefresh || 
			recentInserts == null || 
			recentInserts.size() < limit || 
			shouldRefresh(recentInsertsLastRefreshTime)
		   ) {
			recentInserts = getRecentUploads("createDataPackage", numberOfDays, limit);
		    Date now = new Date();
		    recentInsertsLastRefreshTime = now.getTime();
		}
		return recentInserts;
	}
  
  
  /**
   * Retrieves a list of recent updates.
   *  
   * @param numberOfDays   the number of prior days to search for inserts, for example,
   *                       the past 100 days
   * @param limit          an upper limit on the number of matches returned
   * @param forceRefresh   if true, refresh the search results regardless of when 
   *                       they were last refreshed
   * 
   * @return A list of RecentUpload objects, where each upload was an update,
   *         i.e. the serviceMethod for each is "updateDataPackage".
   */
	synchronized public static List<RecentUpload> getRecentUpdates(Integer numberOfDays, Integer limit, boolean forceRefresh) {
		if (forceRefresh || 
			recentUpdates == null || 
			recentUpdates.size() < limit || 
			shouldRefresh(recentUpdatesLastRefreshTime)
		   ) {
			recentUpdates = getRecentUploads("updateDataPackage", numberOfDays, limit);
		    Date now = new Date();
		    recentUpdatesLastRefreshTime = now.getTime();
		}
		return recentUpdates;
	}
  
  
  /*
   * Retrieves the current cache of RecentUpload objects. Generates a new
   * list of RecentUpload objects if the cache is empty or if it's time
   * to refresh the cache.
   */
	private static List<RecentUpload> getRecentUploads(String serviceMethod,
			Integer numberOfDays, Integer limit) {
		List<RecentUpload> recentUploads = new ArrayList<RecentUpload>();
		String uploadType = serviceMethod.equals("createDataPackage") ? "inserts" : "updates";

		logger.info(String.format("Start refresh of recent %s", uploadType));

		try {
			DataPackageManagerClient dpmc = new DataPackageManagerClient("public");
			// recentUploads = auditManagerClient.recentUploads(serviceMethod, numberOfDays, limit);
			String recentUploadsXML = dpmc.listRecentUploads(serviceMethod, limit);
			if ((recentUploadsXML != null) && (!recentUploadsXML.equals(""))) {
				recentUploads = parseRecentUploadsXML(dpmc, recentUploadsXML);
			}
		}
		catch (Exception e) {
			logger.error("Error refreshing recent uploads: " + e.getMessage());
		}

		logger.info(String.format("Finish refresh of recent %s", uploadType));

		return recentUploads;
	}
	
	
	private static List<RecentUpload> parseRecentUploadsXML(DataPackageManagerClient dpmc, String xml) {
		List<RecentUpload> recentUploads = new ArrayList<RecentUpload>();
		String[] lines = xml.split("\n");
		StringBuilder sb = new StringBuilder("");
		
		for (String line : lines) {
			line = line + "\n";
			if (line.contains("<dataPackageUpload>")) {
				sb = new StringBuilder(line);
			}
			else if (line.contains("</dataPackageUpload>")) {
				sb.append(line);
				String dataPackageXML = sb.toString();
				String uploadDate = parseElement(dataPackageXML, "uploadDate");
				String serviceMethod = parseElement(dataPackageXML, "serviceMethod");
				String scope = parseElement(dataPackageXML, "scope");
				Integer identifier = new Integer(parseElement(dataPackageXML, "identifier"));
				Integer revision = new Integer(parseElement(dataPackageXML, "revision"));
				RecentUpload recentUpload = new RecentUpload(dpmc, uploadDate, serviceMethod, scope, identifier, revision);
				recentUploads.add(recentUpload);
			}
			else {
				sb.append(line);
			}
		}		
		
		return recentUploads;
	}
	
	
	private static String parseElement(String xml, String elementName) {
		String elementText = "";

		String startTag = String.format("<%s>", elementName);
		String endTag = String.format("</%s>", elementName);
		int start = xml.indexOf(startTag) + startTag.length();
		int end = xml.indexOf(endTag);
		elementText = xml.substring(start, end);

		return elementText;
	}
		  

  /*
   * Boolean to determine whether the cache of recent uploads is due to be
   * refreshed. Returns true is the current time has advanced
   * past the last refresh time plus a time-to-live period.
   */
  private static boolean shouldRefresh(long lastRefreshTime) {
	  int hours = 1;
	  boolean shouldRefresh = false;
	  final long refreshInterval = (long) (hours * 60 * 60 * 1000);
	  Date now = new Date();
	  long nowTime = now.getTime();
	  long refreshTime = lastRefreshTime + refreshInterval;
	  
	  if (refreshTime < nowTime) {
		  shouldRefresh = true;
	  }
	  
	  return shouldRefresh;
  }
  
  
  /*
   * Instance Methods
   */
  
  
  /*
   * Compose a fromTime date string to use with generating the list of
   * recent uploads.
   */
  private String composeFromTime(int numberOfDays) {
	  String fromTimeStr = "";
	  SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	  final long nDays = numberOfDays * 24 * 60 * 60 * 1000L; // set the time period for recent uploads
	  
	  Date now = new Date();
	  long nowTime = now.getTime();
	  long fromTime = nowTime - nDays; 
	  Date fromTimeDate = new Date(fromTime);
	  fromTimeStr = simpleDateFormat.format(fromTimeDate);
	  
	  return fromTimeStr;
  }


  /**
   * Returns an audit report based on the provided oid (object id) value
   * 
   * @param  oid 	the oid (object id) value of the audit report to be returned
   * @return The XML document of the report as a String object.
   * @throws PastaEventException
   */
  public String reportByOid(String oid) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
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
    } 
    finally {
		closeHttpClient(httpClient);
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
   * @return a list of RecentUpload objects
   * @throws PastaEventException
   *
	private List<RecentUpload> recentUploads(String serviceMethod, Integer numberOfDays, Integer limit) throws 
	        PastaAuthenticationException, PastaConfigurationException, PastaEventException {
		List<RecentUpload> recentUploadsList = new ArrayList<RecentUpload>();
		String entity = null;
		Integer statusCode = null;
		HttpEntity responseEntity = null;
		String fromTime = composeFromTime(numberOfDays);

	    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
		HttpResponse response = null;
		String url = String.format("%s/recent-uploads?serviceMethod=%s&fromTime=%s&limit=%d", BASE_URL, serviceMethod, fromTime, limit);
		HttpGet httpGet = new HttpGet(url);

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
				recentUploadsList = parseRecentUploads(entity);
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
			closeHttpClient(httpClient);
	 	}

		if (statusCode != HttpStatus.SC_OK) {
			// Something went wrong; return message from the response entity
			String gripe = "The AuditManager responded with response code '"
					+ statusCode.toString() + "' and message '" + entity
					+ "'\n";
			throw new PastaEventException(gripe);
		}
		
		return recentUploadsList;
	}*/
	
	
	  /**
	   * Gets a list of recent uploads to PASTA.
	   * 
	   * @return a list of RecentUpload objects
	   * @throws PastaEventException
	   *
		private List<RecentUpload> recentUploads(String serviceMethod, Integer numberOfDays, Integer limit) 
				throws Exception {
			List<RecentUpload> recentUploadsList = null;
			String fromTime = composeFromTime(numberOfDays);
			
			recentUploadsList = getRecentUploads(serviceMethod, fromTime, limit);
	
			return recentUploadsList;
		}*/
		
		
		/**
		 * Gets a list of recent uploads, either inserts or updates.
		 * 
		 * @param serviceMethod  one of "createDataPackage" or "updateDataPackage"
		 * @param fromTime       the cut-off date for how far back we want to query, e.g. '2015-01-01'
		 * @param limit          a limit on the number of uploads to return, e.g. 2
		 *
		public List<RecentUpload> getRecentUploads(String serviceMethod, String fromTime, Integer limit) 
				throws Exception {
			List<RecentUpload> recentUploadsList = new ArrayList<RecentUpload>();
			DataPackageManagerClient dpmClient = new DataPackageManagerClient("public");
			return recentUploadsList;
		}*/


	/*
	 * Parses the recent uploads audit records XML returned by the Audit Manager.
	 * Converts it to a list of RecentUpload objects.
	 *
	private List<RecentUpload> parseRecentUploads(String xmlString)
	        throws PastaAuthenticationException, PastaConfigurationException, PastaEventException {
		int insertCount = 0;
		int updateCount = 0;
		/*
		 * We only want to display public documents as recent uploads. The list of recent
		 * uploads is stored as a class variable, not as a session variable.
		 *
		DataPackageManagerClient dpmClient = new DataPackageManagerClient("public");
		List<RecentUpload> recent = new ArrayList<RecentUpload>();
		
	    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance(); 
		try {
			DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
			InputStream inputStream = IOUtils.toInputStream(xmlString, "UTF-8");
			Document document = documentBuilder.parse(inputStream);
			Element documentElement = document.getDocumentElement();
			NodeList auditRecordList = documentElement.getElementsByTagName("auditRecord");
			int nAuditRecords = auditRecordList.getLength();
			
			for (int i = 0; i < nAuditRecords; i++) {
				Node auditRecordNode = auditRecordList.item(i);
				NodeList auditRecordChildren = auditRecordNode.getChildNodes();
				String uploadDate = null;
				String serviceMethod = null;
				String resourceId = null;
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
						    resourceId = text.getData().trim();
					    }
				    }
				}
				
				/*
				 * Add the data package to the list of recent uploads only if the user 
				 * is authorized to read it.
				 *
				boolean isAuthorized = dpmClient.isAuthorized(resourceId);
				if (isAuthorized) {
					if ((serviceMethod.equals("createDataPackage")) || 
					    (serviceMethod.equals("updateDataPackage"))
					   ) {
						try {
							RecentUpload recentUpload = 
									new RecentUpload(dpmClient, uploadDate, serviceMethod, resourceId);
							String title = recentUpload.getTitle();
							/*
							 * Add the data package to the list only if we have a title
							 * to display.
							 *
							if (title != null && !title.equals("")) {
								recent.add(recentUpload);
								if (serviceMethod.equals("createDataPackage")) {
									insertCount++;
								}
								else if (serviceMethod.equals("updateDataPackage")) {
								    updateCount++;
								}
							}
						}
						catch (ResourceNotFoundException e) {
							/*
							 * If not found, do nothing. The data package has
							 * been deleted or replaced by a higher revision, so
							 * we want to exclude it from the list of recent
							 * uploads.
							 *
						}
					}
				}
			}
		}
		catch (Exception e) {
			logger.error("Exception:\n" + e.getMessage());
			e.printStackTrace();
			throw new PastaEventException(e.getMessage());
		}
		
		return recent;
	}*/
	
	
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

    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
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
    } 
    finally {
		closeHttpClient(httpClient);
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
