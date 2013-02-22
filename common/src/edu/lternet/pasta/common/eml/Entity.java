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

package edu.lternet.pasta.common.eml;

/**
 * An Entity holds metadata values about one of the entities in a data package.
 * For example, the entity name, the object name, and the data URL are stored.
 * 
 * @author dcosta
 *
 */
public class Entity {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  
  String name = null;
  String objectName = null;
  String url = null;

  
  /*
   * Constructors
   */
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  
  /* Getter and setter instance methods */
  
  public String getName() {
    return name;
  }

  
  public String getObjectName() {
    return objectName;
  }

  
  public String getUrl() {
    return url;
  }

  
  public void setName(String name) {
    this.name = name;
  }
  
  
  public void setObjectName(String objectName) {
    this.objectName = objectName;
  }
  
  
  public void setUrl(String url) {
    this.url = url;
  }

}
