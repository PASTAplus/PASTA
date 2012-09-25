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

import java.io.IOException;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.token.AuthToken;

/**
 * @author dcosta
 * @version 1.0
 * @created 25-Jan-2012 10:40:03 AM
 * 
 * The EventManagerClient class interacts with the Event Manager web service as
 * a client to notify it of changes to data packages in PASTA.
 */
public class EventManagerClient extends PASTAServiceClient {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EventManagerClient.class);
  private String host = null;
  private String urlHead = null; 

  
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
  
  
  private CookieStore getCookieStore(AuthToken authToken) {
    CookieStore cookieStore = new BasicCookieStore();    
    String tokenString = authToken.getTokenString();
    BasicClientCookie basicClientCookie = 
      new BasicClientCookie(DataPackageManagerResource.AUTH_TOKEN, tokenString);
    basicClientCookie.setVersion(0);
    basicClientCookie.setDomain(".lternet.edu");
    basicClientCookie.setPath("/");
    cookieStore.addCookie(basicClientCookie);

    return cookieStore;
  }


  /**
   * Notify the Event Manager of a change to a data package.
   * 
   * @param scope       The data package scope value, e.g. "knb-lter-lno"
   * @param identifier  The data package identifier value
   * @param revision    The data package revision value
   * @param user        The user
   * @param authToken   The authorization token object
   */
  public String notifyEventManager(String scope, 
                                  Integer identifier,
                                  Integer revision,
                                  String user, 
                                  AuthToken authToken)
      throws ClientProtocolException, 
             IOException, 
             ResourceNotFoundException,
             Exception {
    String locationURL = null;
    DefaultHttpClient httpClient = new DefaultHttpClient();
    String urlTail = makeUrlTail(scope, identifier.toString(), revision.toString(), null);
    String url = urlHead + urlTail;
    HttpPost httpPost = new HttpPost(url);
    BasicHttpContext localcontext = new BasicHttpContext();

    // Add CookieStore holding the auth-token cookie to the execution context
    CookieStore cookieStore = getCookieStore(authToken);
    localcontext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
    
    try {
      logger.warn("Posting to Event Manager at URL: " + url);
      HttpHost httpHost = new HttpHost(this.host, 8080, "http");
      HttpResponse httpResponse = httpClient.execute(httpHost, httpPost, localcontext);
      int statusCode = httpResponse.getStatusLine().getStatusCode();
      logger.warn("Response Code from Event Manager: " + statusCode);
      HttpEntity httpEntity = httpResponse.getEntity();
      String entityString = EntityUtils.toString(httpEntity);
      if (statusCode == HttpStatus.SC_OK) {
        Header[] locationHeaders = httpResponse.getHeaders("Location");
        if (locationHeaders.length > 0) {
          Header locationHeader = locationHeaders[0];
          locationURL = locationHeader.getValue();
        }
      }
      else {
        handleStatusCode(statusCode, entityString);
       }
    }
    finally {
      httpClient.getConnectionManager().shutdown();
    }

    return locationURL;
  }

}