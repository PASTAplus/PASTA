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

import java.sql.SQLException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.security.access.UnauthorizedException;
import edu.lternet.pasta.portal.ConfigurationListener;
import edu.lternet.pasta.token.TokenManager;


/**
 * @author dcosta
 * @since April 2, 2012
 * 
 *        PastaClient is a parent to other client classes that interact
 *        with PASTA web services.
 * 
 */
public class PastaClient {
  
  /*
   * Class variables
   */
  
  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.PastaClient.class);
  final String SLASH = "/";
  
  /*
   * Instance variables
   */
  
  protected String pastaHost = null;
  protected String pastaProtocol = null;
  protected int pastaPort;
  protected String pastaUriHead = null;

  protected String uid = null;
  protected String token = null;
  
  /*
   * Constructors
   */

  public PastaClient(String uid) throws PastaAuthenticationException, PastaConfigurationException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();
    
    if (options == null) {
      throw new PastaConfigurationException();
    }

    this.uid = uid;
    String gripe = null;

    this.pastaHost = options.getString("pasta.hostname");
    this.pastaProtocol = options.getString("pasta.protocol");
    this.pastaPort = options.getInt("pasta.port");
    this.pastaUriHead = options.getString("pasta.uriHead");

    if (this.uid == null) {

      gripe = "User identifier \"uid\" is \"null\"";
      throw new PastaAuthenticationException(gripe);

    } else {

      if (!this.uid.equals("public")) {  // Get authentication token for uid

        TokenManager tokenManager = new TokenManager();

        try {

          this.token = tokenManager.getToken(uid);

          // Throw exception if user not "public"
          // and token not in store
          if (this.token == null) {
            gripe = "A token for user '" + this.uid
                + "' does not exist in the \"TokenStore\"";
            throw new PastaAuthenticationException(gripe);
          }

        } catch (SQLException e) {
          logger.error(e);
          e.printStackTrace();
        } catch (ClassNotFoundException e) {
          logger.error(e);
          e.printStackTrace();
        }
        
      }

    }

  }
  
  
  /*
   * Class methods
   */
  
  /**
   * Composes the LTER distinguished name for a given uid value.
   * 
   * @param uid  The uid value, e.g. "ucarroll"
   * @return     The distinguished name, 
   *               e.g. "uid=ucarroll,o=LTER,dc=ecoinformatics,dc=org"
   */
  public static String composeDistinguishedName(String uid) {
    String distinguishedName = "uid=" + uid + ",o=LTER,dc=ecoinformatics,dc=org";
    return distinguishedName;
  }
  
  
  /**
   * Composes the pasta URL from the pasta hostname and pasta protocol values
   * stored in the dataportal.properties file.
   * 
   * @param pastaProtocol     The PASTA protocol, e.g. "http"
   * @param pastaHostname     The PASTA hostname, e.g. "pasta-s.lternet.edu"
   * @param pastaPort         The PASTA port value, e.g. 8888 (may be null or empty string)
   * @return
   */
  public static String composePastaUrl(String pastaProtocol, String pastaHostname, Integer pastaPort) {
    String pastaUrl = pastaProtocol + "://" + pastaHostname;
    if (pastaPort != null && pastaPort > 0) {
      pastaUrl += ":" + pastaPort.toString();
    }
    return pastaUrl;
  }
  
  
  /*
   * Instance methods
   */
  
  
  /*
   * Gets the pastaHost instance variable.
   */
  public String getPastaHost() {
    return pastaHost;
  }
  
  
  /*
   * Gets the pastaHost instance variable.
   */
  public String getPastaUriHead() {
    return pastaUriHead;
  }
  
  
  /**
   * Handle an HTTP status code that represents some form of exception
   * 
   * @param statusCode   the HTTP status code integer value
   */
  protected void handleStatusCode(int statusCode, String entityString)
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
   * Compose a "URL Tail" string from the scope, identifier, revision,
   *   and entityId values. The revision and entityId values may be
   *   null.
   * @param scope       the scope value
   * @param identifier  the identifier value
   * @param revision    the revision value (may be null)
   * @param entityId    the entityId value (may be null)
   * @return a string, the "URL tail" fragment of a resource URI
   */
  protected String makeUrlTail(String scope, String identifier, String revision, String entityId) {
    String urlTail = null;
    StringBuffer urlTailBuffer = new StringBuffer("");
    
    urlTailBuffer.append(SLASH + scope);
    urlTailBuffer.append(SLASH + identifier);
    if (revision != null) { urlTailBuffer.append(SLASH + revision); }
    if (entityId != null) { urlTailBuffer.append(SLASH + entityId); }
    
    urlTail = urlTailBuffer.toString();
    return urlTail;
  }
  
}
