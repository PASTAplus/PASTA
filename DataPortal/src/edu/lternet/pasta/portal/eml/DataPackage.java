/*
 *
 * $Date: 2012-04-02 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
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

package edu.lternet.pasta.portal.eml;

import java.util.ArrayList;


/**
 * DataPackage holds metadata values about a data package that were parsed 
 * from an EML document.
 *  
 * @author dcosta
 *
 */
public class DataPackage {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  
  String packageId = null;
  ArrayList<Entity> entityList = null;

  
  /*
   * Constructors
   */
  
  
  /*
   * Initialize the array lists when constructing this DataPackage object.
   */
  
  DataPackage() {
    this.entityList = new ArrayList<Entity>();
  }
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  
  public ArrayList<Entity> getEntityList() {
    return entityList;
  }

  
  public String getPackageId() {
    return packageId;
  }

  
  public void setPackageId(String packageId) {
    this.packageId = packageId;
  }
  
}