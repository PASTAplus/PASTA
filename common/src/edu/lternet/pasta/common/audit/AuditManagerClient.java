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

package edu.lternet.pasta.common.audit;

import java.util.Set;
import java.util.TreeSet;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.audit.AuditRecord;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.common.security.auth.AuthSystemDef;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;


/**
* @author dcosta
* @version 1.0
* @created 25-Jan-2012 10:40:03 AM
* 
* The AuditManagerClient class interacts with the Audit Manager web service as
* a client to log audits from a PASTA web service.
*/
public class AuditManagerClient implements Runnable {

 /*
  * Class fields
  */
 

 /*
  * Instance fields
  */
 
 private Logger logger = Logger.getLogger(AuditManagerClient.class);
 private String host = null;
 private String urlHead = null; 

 private AuditRecord auditRecord = null;
 private AuthToken authToken = null;

 
 /*
  * Constructors
  */
 
 public AuditManagerClient(String auditManagerHost) {
   this.host = auditManagerHost;
   this.urlHead = "http://" + host + "/audit"; 
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
  * Handle an HTTP status code that represents some form of exception
  * 
  * @param statusCode   the HTTP status code integer value
  */
 private void handleStatusCode(int statusCode, String entityString)
     throws Exception {
   final Exception e;
   
   switch (statusCode) {
     case HttpStatus.SC_BAD_REQUEST:
       e = new UserErrorException(entityString);
       throw(e);
       
     case HttpStatus.SC_CONFLICT:
       e = new ResourceExistsException(entityString);
       throw(e);
       
     case HttpStatus.SC_EXPECTATION_FAILED:
       e = new Exception(entityString);
       throw(e);
          
     case HttpStatus.SC_FORBIDDEN:
       e = new UnauthorizedException(entityString);
       throw(e);
     
     case HttpStatus.SC_GONE:
       e = new ResourceDeletedException(entityString);
       throw(e);

     case HttpStatus.SC_NOT_FOUND:
       e = new ResourceNotFoundException(entityString);
       throw(e);

     case HttpStatus.SC_UNAUTHORIZED:
       e = new UnauthorizedException(entityString);
       throw(e);
       
     default:
       e = new Exception(entityString);
       throw(e);
   }
 }


 /**
  * Logs an audit record with the Audit Manager.
  * 
  * @param scope       The data package scope value, e.g. "knb-lter-lno"
  * @param identifier  The data package identifier value
  * @param revision    The data package revision value
  * @param user        The user
  * 
  */
 public void logAudit(AuditRecord auditRecord) {
   this.auditRecord = auditRecord;

   Set<String> s = new TreeSet<String>();
   s.add("authenticated");
   this.authToken = AuthTokenFactory.makeCookieAuthToken("pasta", AuthSystemDef.KNB, 2000000000, s);
  
   Thread thread = new Thread(this);
   thread.start();
 }


 /**
  * Logs an audit record with the Audit Manager.
  * Run in a separate thread.
  */
 public void run() {
   DefaultHttpClient httpClient = new DefaultHttpClient();
   String url = this.urlHead;
   HttpPost httpPost = new HttpPost(url);
   
   BasicHttpContext localcontext = new BasicHttpContext();
   httpPost.setHeader("Cookie", "auth-token=" + authToken.getTokenString());
   
   try {
     logger.warn("Posting to Audit Manager at URL: " + url);
     // Set the request entity
     String xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
     String auditEntryXML = xmlHeader + auditRecord.toXML();
     HttpEntity stringEntity = new StringEntity(auditEntryXML);
     httpPost.setEntity(stringEntity);
     HttpHost httpHost = new HttpHost(this.host, 8080, "http");
     HttpResponse httpResponse = httpClient.execute(httpHost, httpPost, localcontext);
     int statusCode = httpResponse.getStatusLine().getStatusCode();
     logger.warn("Response Code from Audit Manager: " + statusCode);
     HttpEntity httpEntity = httpResponse.getEntity();
     String entityString = EntityUtils.toString(httpEntity);
     if (statusCode != HttpStatus.SC_OK && 
         statusCode != HttpStatus.SC_CREATED) {
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