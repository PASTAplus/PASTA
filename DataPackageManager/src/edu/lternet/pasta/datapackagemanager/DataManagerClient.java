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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.lternet.pasta.dml.parser.DataPackage;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.datamanager.EMLDataManager;

/**
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 * 
 * The DataManagerClient class interacts with the Data Manager as
 * a client. It was originally a client to the Data Manager web service,
 * but now it acts as a client to the 'edu.lternet.pasta.datamanager'
 * Java package.
 */
public class DataManagerClient {

  /*
   * Class fields
   */
  
  
  
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
	 * Create data entities in the Data Manager
	 * 
	 * @param dataPackage  the DataPackage object describing the data entities
	 *                     to be created
	 * @param transaction  the transaction identifier
   * @return             A Map of entityId / entityName pairs
	 */
	public Map<String, String> createDataEntities(DataPackage dataPackage, String transaction) 
	        throws IOException,
	               MalformedURLException,
	               Exception {
    boolean evaluateMode = false;    
    EMLDataManager emlDataManager = new EMLDataManager();
    Map<String, String> entityPairs = emlDataManager.createDataEntities(dataPackage, evaluateMode, transaction);
      
    return entityPairs;
	}

	
  /**
   * Evaluate data entities in the Data Manager
   * 
   * @param dataPackage  the DataPackage object describing the data entities
   *                     to be created
   * @param transaction  the transaction identifier
   * @return             A Map of entityId / entityName pairs
   */
  public Map<String, String> evaluateDataEntities(DataPackage dataPackage, String transaction)
      throws IOException, Exception {
    boolean evaluateMode = true;
    EMLDataManager emlDataManager = new EMLDataManager();
    Map<String, String> entityIdNamePairs = emlDataManager.createDataEntities(dataPackage, evaluateMode, transaction);

    return entityIdNamePairs;
  }

	
  /**
   * Returns a data entity File object.
   * 
   * @param resourceLocation The base storage location for the entity resource
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param entityId    The entityId of the entity to be read
   * @return            A File object of the entity data
   */
	public File getDataEntityFile(String resourceLocation,
	                             String scope, 
	                             Integer identifier, 
	                             String revision, 
	                             String entityId) 
	        throws IOException, 
	               ResourceNotFoundException,
	               Exception {

		File file = null;

		EMLDataManager emlDataManager = new EMLDataManager(); 

    file = emlDataManager.readDataEntity(resourceLocation, scope, identifier.toString(), revision, entityId);

    return file;
	}

}