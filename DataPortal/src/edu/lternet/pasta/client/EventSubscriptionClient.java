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
import java.io.UnsupportedEncodingException;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Mar 25, 2012
 * 
 *        The EventService supports the management of "event subscriptions" and
 *        interacts directly with PASTA. The user must be authorized and have a
 *        valid authentication token to utilize this service.
 * 
 */
public class EventSubscriptionClient extends PastaClient {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.EventSubscriptionClient.class);

  /*
   * Instance variables
   */

  private final String BASE_URL;
  private final String BASE_URL_SUBSCRIPTION;
  private final String BASE_URL_EVENT;

  /*
   * Constructors
   */

  /**
   * Creates a new EventService object and sets the user's authentication token
   * if it exists; otherwise an error.
   * 
   * @param uid
   *          The user's identifier as a String object.
   * 
   * @throws PastaAuthenticationException
   * @throws PastaConfigurationException
   */
  public EventSubscriptionClient(String uid)
      throws PastaAuthenticationException, PastaConfigurationException {

    super(uid);
    String pastaUrl = PastaClient.composePastaUrl(this.pastaProtocol, this.pastaHost, this.pastaPort);
    this.BASE_URL = pastaUrl + "/package/";
    BASE_URL_SUBSCRIPTION = BASE_URL + "event/eml";
    BASE_URL_EVENT = BASE_URL + "event/eml";
  }

  /*
   * Methods
   */

  /**
   * Create a new subscription in PASTA's Event Manager.
   * 
   * @param subscription
   *          The XML subscription as a String object.
   * 
   * @return The subscription identifier as a String object.
   * 
   * @throws PastaEventException
   */
  public String create(String subscription) throws PastaEventException {

    String sid = null;
    Integer statusCode = null;
    Header[] headers = null;
    HttpEntity responseEntity = null;
    String statusMessage = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpPost httpPost = new HttpPost(BASE_URL_SUBSCRIPTION);

    // Set header content
    if (this.token != null) {
      httpPost.setHeader("Cookie", "auth-token=" + this.token);
    }
    httpPost.setHeader("Content-Type", "application/xml");

    // Set subscription into the request entity
    StringEntity requestEntity = null;

    try {
      requestEntity = new StringEntity(subscription);
    } catch (UnsupportedEncodingException e1) {
      logger.error(e1.getMessage());
      e1.printStackTrace();
    }

    httpPost.setEntity(requestEntity);

    try {

      response = httpClient.execute(httpPost);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      headers = response.getAllHeaders();
      responseEntity = response.getEntity();

      if (responseEntity != null) {
        statusMessage = EntityUtils.toString(responseEntity);
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

    if (statusCode == HttpStatus.SC_CREATED) {

      String headerName = null;
      String headerValue = null;

      // Loop through all headers looking for the "Location" header.
      for (int i = 0; i < headers.length; i++) {
        headerName = headers[i].getName();

        if (headerName.equals("Location")) {

          headerValue = headers[i].getValue();
          String[] path = headerValue.split("/");
          sid = path[path.length - 1]; // the subscription identifier is in the
                                       // last field of the path array
          break;

        }

      }

    } else { // Something went wrong; return message from the response entity

      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + statusMessage + "'\n";
      throw new PastaEventException(gripe);

    }

    return sid;

  }

  /**
   * Returns the event subscription as String object based on the event
   * subscription identifier.
   * 
   * @param sid
   *          The event subscription identifier as a String object.
   * 
   * @return The event subscription in its native XML format as String object.
   * 
   * @throws PastaEventException
   */
  public String readBySid(String sid) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL_SUBSCRIPTION + "/" + sid);

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
      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return entity;

  }

  /**
   * Returns the event subscription as String object based on the set of
   * filters: "creator", "scope", "identifier", "revision" and or "url".
   * 
   * @param filter
   *          The filter URL query expression as a String object.
   * 
   * @return The event subscription in its native XML format as String object.
   * 
   * @throws PastaEventException
   */
  public String readByFilter(String filter) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL_SUBSCRIPTION + "?" + filter);

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
      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return entity;

  }

  /**
   * Returns the event subscription schema as a String object in its native XML
   * format.
   * 
   * @return The schema as a String object.
   * 
   * @throws PastaEventException
   */
  public String readSchema() throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpGet httpGet = new HttpGet(BASE_URL_SUBSCRIPTION + "/" + "schema");

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
      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

    return entity;

  }

  /**
   * Deletes the event subscription identified by its subscription identifier.
   * 
   * @param sid
   *          The subscription identifier as a String object.
   * 
   * @throws PastaEventException
   */
  public void deleteBySid(String sid) throws PastaEventException {

    String entity = null;
    Integer statusCode = null;
    HttpEntity responseEntity = null;

    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    HttpDelete httpDelete = new HttpDelete(BASE_URL_SUBSCRIPTION + "/" + sid);

    // Set header content
    if (this.token != null) {
      httpDelete.setHeader("Cookie", "auth-token=" + this.token);
    }

    try {

      response = httpClient.execute(httpDelete);
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
      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + entity + "'\n";
      throw new PastaEventException(gripe);

    }

  }
  
  /**
   * Test an existing subscription in PASTA's Event Manager by utilizing the
   * EventManager service that invokes the subscription's URL without
   * requiring a packageId value.
   * 
   * @param subscriptionId    the subscription identifier value, e.g. "44"
   * 
   * @throws PastaEventException
   */
  public void testSubscription(String subscriptionId) throws PastaEventException {

    Integer statusCode = null;
    HttpEntity responseEntity = null;
    String statusMessage = null;
    HttpClient httpClient = new DefaultHttpClient();
    HttpProtocolParams.setUseExpectContinue(httpClient.getParams(), false);
    HttpResponse response = null;
    String subscriptionURL = BASE_URL_EVENT + "/" + subscriptionId;
    HttpPost httpPost = new HttpPost(subscriptionURL);

    // Set header content
    if (this.token != null) {
      httpPost.setHeader("Cookie", "auth-token=" + this.token);
    }
    httpPost.setHeader("Content-Type", "application/xml");

    try {

      response = httpClient.execute(httpPost);
      statusCode = (Integer) response.getStatusLine().getStatusCode();
      responseEntity = response.getEntity();

      if (responseEntity != null) {
        statusMessage = EntityUtils.toString(responseEntity);
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
      String gripe = "The EventManager responded with response code '"
          + statusCode.toString() + "' and message '" + statusMessage + "'\n";
      throw new PastaEventException(gripe);
    }

  }

}
