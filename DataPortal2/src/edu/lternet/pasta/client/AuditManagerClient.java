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

import edu.lternet.pasta.portal.ConfigurationListener;

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
    this.BASE_URL = pastaUrl + "/audit/report";
  }

  
  /*
   * Methods
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
    HttpGet httpGet = new HttpGet(BASE_URL + "/" + oid);

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
    HttpGet httpGet = new HttpGet(BASE_URL + "/?" + filter);

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

  
  public static void main(String[] args) {
    
    ConfigurationListener.configure();
    
    try {
      AuditManagerClient amc = new AuditManagerClient("ucarroll");
      String report = amc.reportByOid("");
      logger.info("REPORT: \n" + report);
      
    } catch (PastaAuthenticationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (PastaConfigurationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (PastaEventException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
  }

}
