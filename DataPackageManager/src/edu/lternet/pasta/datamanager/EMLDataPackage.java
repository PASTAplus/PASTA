/**
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
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

package edu.lternet.pasta.datamanager;

import org.apache.log4j.Logger;
import org.ecoinformatics.datamanager.parser.DataPackage;

import edu.lternet.pasta.common.EmlPackageId;


/**
 * Wrapper for the DataPackage class in the Data Manager Library.
 * 
 * @author dcosta
 * @created 16-Dec-2011 4:30:03 PM
 */
public class EMLDataPackage {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EMLDataPackage.class);
  
  private DataPackage dataPackage = null;
  private EmlPackageId emlPackageId;
  protected String packageId;
  
  
  /*
   * Constructors
   */
  
  
  /**
   * Explicit default constructor is defined for the subclasses of EMLDataPackage.
   */
  EMLDataPackage() {
    
  }
    

  /**
   * Constructs an EMLDataPackage object with a specified
   * 'org.ecoinformatics.datamanager.parser.DataPackage' object. 
   * EMLDataPackage acts as a wrapper that encapsulates its Data 
   * Manager Library DataPackage object.
   * 
   * @param dataPackage  A Data Package object as defined by the
   *                     Data Manager Library.
   */
  public EMLDataPackage(DataPackage dataPackage) {
    this.dataPackage = dataPackage;
    this.packageId = dataPackage.getPackageId();
  }
  

  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */
  
  /* Access methods */
  
  /**
   * Gets the EmlPackageId object for this data package.
   * 
   * @return  the EmlPackageId object
   */
  public EmlPackageId getEmlPackageId() {
    return emlPackageId;
  }

  
  /**
   * Gets the Data Manager Library DataPackage object that this object encapsulates.
   * 
   * @return  a DataPackage object
   */
  public DataPackage getDataPackage() {
    return dataPackage;
  }

  
  /**
   * Get the packageId string for this data package.
   * 
   * @return  the packageId string
   */
  public String getPackageId() {
    return packageId;
  }
 
}