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

import org.apache.http.HttpStatus;
import edu.lternet.pasta.common.ResourceDeletedException;
import edu.lternet.pasta.common.ResourceExistsException;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.security.access.UnauthorizedException;


/**
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 * 
 * The PASTAServiceClient class is an abstract parent class to
 * client classes for specific PASTA web services.
 */
public abstract class PASTAServiceClient {

  /*
   * Class fields
   */
  
  final String SLASH = "/";

  
  /*
   * Instance fields
   */
  
  /*
   * Constructors
   */
  
  
  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */
  
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
