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

import java.sql.SQLException;
import java.util.ArrayList;

import edu.lternet.pasta.common.edi.EdiToken;
import edu.lternet.pasta.common.edi.IAM;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.ucsb.nceas.utilities.Options;
import org.apache.log4j.Logger;
import org.json.JSONObject;


public class Authorizer {
  
  public enum AccessType {
    allow, deny
  }
  
  public enum AccessOrder {
    allowFirst, denyFirst
  }
  
  
  /*
   * Class variables
   */
  private static final Logger logger = Logger.getLogger(Authorizer.class);
  private static boolean EDI_AUTH_USE;
  private static String EDI_AUTH_PROTOCOL;
  private static String EDI_AUTH_HOST;
  private static Integer EDI_AUTH_PORT;

    /*
   * Instance variables
   */
  
  DataPackageRegistry dataPackageRegistry = null;
  
  
  /*
   * Constructors
   */
  
  Authorizer(DataPackageRegistry dataPackageRegistry) {
    this.dataPackageRegistry = dataPackageRegistry;

      Options options = ConfigurationListener.getOptions();
      EDI_AUTH_USE = Boolean.parseBoolean(options.getOption("edi.auth.use"));
      EDI_AUTH_PROTOCOL = options.getOption("edi.auth.protocol");
      EDI_AUTH_HOST = options.getOption("edi.auth.host");
      EDI_AUTH_PORT = Integer.parseInt(options.getOption("edi.auth.port"));

  }
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  /**
   * Boolean to determine whether the specified user is authorized
   * with the specified permission to access or modify the specified
   * resource.
   * 
   * @param authToken       The AuthToken object
   * @param resourceId      The resource identifier
   * @param permission      The permission type
   * @return true if the user (authToken) is authorized to access the resource 
   *         with the specified permission, else false
   */
  public boolean isAuthorized(
          AuthToken authToken,
          String ediToken,
          String resourceId,
          Rule.Permission permission
  ) throws ClassNotFoundException, SQLException {

    boolean isAuthorized = false;
    
    boolean hasResource = dataPackageRegistry.hasResource(resourceId);
    if (!hasResource) {
      throw new ResourceNotFoundException("Resource not found with resourceId: " + resourceId);
    }
    
    String principalOwner = dataPackageRegistry.getPrincipalOwner(resourceId);
    
    if (principalOwner != null) {
      ArrayList<Rule> ruleList = dataPackageRegistry.getAccessControlRules(resourceId);
      AccessMatrix accessMatrix = new AccessMatrix(ruleList);
      isAuthorized = accessMatrix.isAuthorized(authToken, principalOwner, permission);
    }

      /*
       * EDI IAM authorization
       */
      boolean ediAuthorized = false;
      if (ediToken != null) {
          IAM iam = new IAM(EDI_AUTH_PROTOCOL, EDI_AUTH_HOST, EDI_AUTH_PORT);
          iam.setEdiToken(ediToken);
          try {
              JSONObject response = iam.isAuthorized(resourceId, permission.toString());
              logger.info(response.toString());
              ediAuthorized = true;
          }
          catch (Exception e) {
              String msg = "EDI Authorization Error: " + e.getMessage();
              logger.error(msg);
          }
      }

      // Authorization congruence test
      if ((ediToken != null) && (ediAuthorized != isAuthorized)) {
          String ediId = new EdiToken(ediToken).getSubject();
          String line;
          StringBuilder msg = new StringBuilder();
          line = "EDI/PASTA authorization congruence error: ";
          msg.append(line);
          line = String.format("EDI isAuthorized (%b); PASTA isAuthorized (%b); ", ediAuthorized, isAuthorized);
          msg.append(line);
          line = String.format(
                  "edi_subj: %s; authtoken_subj: %s; resource: %s; permission: %s",
                  ediId, authToken.getUserId(), resourceId, permission.toString()
          );
          msg.append(line);
          logger.error(msg);
      }

      if (EDI_AUTH_USE) {
          isAuthorized = ediAuthorized;
      }

      return isAuthorized;
  }
  
  
  /**
   * Stores an access matrix (a set of access records) to the
   * data package registry for the specified resource identifier.
   * 
   * @param  resourceId  the resource identifier for which the rules apply
   * @param  accessMatrix  the access matrix (list of rules)
   * @param  mayOverwrite  true if existing records may be overwritten
   *                       (used only for evaluate mode)
   */
  public void storeAccessMatrix(String resourceId, 
                                AccessMatrix accessMatrix, 
                                boolean mayOverwrite) 
          throws ClassNotFoundException, SQLException {
    if (accessMatrix != null) {
      ArrayList<Rule> ruleList = accessMatrix.getRuleList();
      dataPackageRegistry.addAccessControlRules(resourceId, ruleList, mayOverwrite);
    }
  }

}
