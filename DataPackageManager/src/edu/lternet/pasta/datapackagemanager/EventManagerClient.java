/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;


/**
 * @author dcosta
 * @version 1.0
 * @created 25-Jan-2012 10:40:03 AM
 * 
 * The EventManagerClient class interacts with the Event Manager web service as
 * a client to notify it of changes to data packages in PASTA.
 */
public class EventManagerClient extends PASTAServiceClient implements Runnable {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EventManagerClient.class);
  private String host = null;
  private String urlHead = null; 

  String scope = null;
  Integer identifier = null;
  Integer revision = null;
  String user = null;
  AuthToken authToken = null;

  
  /*
   * Constructors
   */
  
  public EventManagerClient(String eventmanagerHost) {
    this.host = eventmanagerHost;
    this.urlHead = "http://" + host + "/eventmanager/event/eml"; 
  }

  
  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */
  
  public void finalize() throws Throwable {
    super.finalize();
  }
  
  
  /**
   * Notify the Event Manager of a change to a data package.
   * 
   * @param scope       The data package scope value, e.g. "knb-lter-lno"
   * @param identifier  The data package identifier value
   * @param revision    The data package revision value
   * @param user        The user
   * 
   */
  public void notifyEventManager(String scope, Integer identifier, Integer revision) {
    this.scope = scope;
    this.identifier = identifier;
    this.revision = revision;

    Set<String> s = new TreeSet<String>();
    s.add("authenticated");
    this.authToken = AuthTokenFactory.makeCookieAuthToken("pasta", AuthSystemDef.KNB, 2000000000, s);
   
    Thread thread = new Thread(this);
    thread.start();
  }


  /**
   * Notify the Event Manager of a change to a data package.
   * Run in a separate thread.
   */
  public void run() {
    DefaultHttpClient httpClient = new DefaultHttpClient();
    String urlTail = makeUrlTail(scope, identifier.toString(), revision.toString(), null);
    String url = urlHead + urlTail;
    HttpPost httpPost = new HttpPost(url);
    BasicHttpContext localcontext = new BasicHttpContext();
    httpPost.setHeader("Cookie", "auth-token=" + authToken.getTokenString());
    
    try {
      logger.warn("Posting to Event Manager at URL: " + url);
      HttpHost httpHost = new HttpHost(this.host, 8080, "http");
      HttpResponse httpResponse = httpClient.execute(httpHost, httpPost, localcontext);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      logger.warn("Response Code from Event Manager: " + statusCode);
      HttpEntity httpEntity = httpResponse.getEntity();
      String entityString = EntityUtils.toString(httpEntity);
      if (statusCode != HttpStatus.SC_OK) {
        handleStatusCode(statusCode, entityString);
      }
    }
    catch (Exception e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }
  }

}