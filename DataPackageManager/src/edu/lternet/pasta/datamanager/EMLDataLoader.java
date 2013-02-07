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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.ecoinformatics.datamanager.download.DataSourceNotFoundException;
import org.ecoinformatics.datamanager.download.DataStorageInterface;

import edu.lternet.pasta.common.EmlPackageId;


/**
 * This class implements:
 *   org.ecoinformatics.datamanager.download.DataStorageInterface
 *   
 * The purpose of this class in to interact with the Data Manager library's
 * download handler. 
 * 
 * The methods that are required by the DataStorageInterface 
 * interface are:
 * 
 *      <ul>
 *      <li>doesDataExist()</li> 
 *      <li>finishSerialize()</li>
 *      <li>getException()</li>
 *      <li>isCompleted()</li>
 *      <li>isSuccess()</li>
 *      <li>load()</li>
 *      <li>startSerialize()</li>
 *      </ul>
 * 
 * @author  dcosta
 * @created 07-Dec-2010 4:00:00 PM
 *
 */
public class EMLDataLoader implements DataStorageInterface {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EMLDataLoader.class);
  
  private boolean evaluateMode = false;
  FileOutputStream fileOutputStream = null;
  
  private HashMap<String, EmlPackageId> urlEmlPackageIdMap = null;
  private HashMap<String, String> urlEntityIdMap = null;
    
    
  /*
   * Constructors
   */
  
  public EMLDataLoader() {
    this.urlEmlPackageIdMap = new HashMap<String, EmlPackageId>();
    this.urlEntityIdMap = new HashMap<String, String>();
  }
  

  /*
   * Class methods
   */
  
  /*
   * Instance methods
   */
  
  /**
   * Stores mappings of the data URL to the packageId, and the data url
   * to the entityId.
   * 
   * @param  url        the data URL
   * @param  packageId  the packageId string
   * @param  entitytId  the entityId string
   */
  public void putUrlMapEntries(String url, EmlPackageId emlPackageId, String entityId) {
    this.urlEmlPackageIdMap.put(url, emlPackageId);
    this.urlEntityIdMap.put(url, entityId);
  }

  
  /**
   * Retrieves the packageId corresponding to a data URL.
   * 
   * @param   url    the data URL
   * @return  the packageId String that was previously mapped to the URL
   */
  public EmlPackageId getUrlEmlPackageIdMapEntry(String url) {
    return this.urlEmlPackageIdMap.get(url);
  }

  
  /**
   * Retrieves the entityId corresponding to a data URL.
   * 
   * @param   url   the data URL
   * @return  the entityId String that was previously mapped to the URL
   */
  public String getUrlEntityIdMapEntry(String url) {
    return this.urlEntityIdMap.get(url);
  }
  
  
  /**
   * Retrieves the evaluateMode boolean value.
   * 
   * @return evaluateMode, true if evaluate mode is set
   */
  public boolean isEvaluateMode() {
    return evaluateMode;
  }

  
  /**
   * Maps an entity URL to an entity identifier.
   * 
   * @param   url  the URL to be mapped.
   * @return  identifier, a string
   */
  public String mapURLToIdentifier(String url) {
    String identifier = getUrlEntityIdMapEntry(url);
          
    return identifier;
  }


  /**
   * Sets the evaluateMode boolean value.
   * 
   * @param evaluateMode, the boolean value to set
   */
  public void setEvaluateMode(boolean evaluateMode) {
    this.evaluateMode = evaluateMode;
  }

  
  /**
   * Method to test if data has already been downloaded or not.
   * 
   * @param   url   the URL to the data
   * @return  true if the data has already been downloaded, else false
   */
  public boolean doesDataExist(String url) {
    boolean exists = false;
    
    /*
     * Commenting out the following block of code so that we
     * always return false and cause the data to be downloaded. 
     * See Trac #700: Data Manager Library returns stale data entities from bad uploads
     */
    
    /*
     * If this is evaluate mode, always return false because we
     * want the data to be re-evaluated. If this is not evaluate
     * mode, then check to see whether the data entity exists.
     *
    if (!this.evaluateMode) {
      EmlPackageId emlPackageId = getUrlEmlPackageIdMapEntry(url);
      String entityId = getUrlEntityIdMapEntry(url);
        
      EMLFileSystemEntity emlFileSystemEntity = 
                           new EMLFileSystemEntity(emlPackageId, entityId);
      if (emlFileSystemEntity != null) {
        exists = emlFileSystemEntity.exists();
      }
    }*/
        
    return exists;
  }
    
      
  /**
   * Closes the OutputStream after serialization has completed.
   * 
   * @param identifier   the identifier of the stream that has finished 
   *                     serializing
   * @param errorCode    the error code string that was generated during
   *                     serialization
   */
  public void finishSerialize(String identifier, String errorCode) {
    logger.warn("EMLDataLoader.finishSerialize()");
    if (fileOutputStream != null) {
      try {
        logger.warn("Output stream finished serializing:\n" +
                    "  identifier: " + identifier + "\n" +
                    "  errorCode:  " + errorCode + "\n");
        fileOutputStream.close();
      }
      catch(IOException e) {
        // Data Storage Interface doesn't allow this method to throw
        // an exception, so log an error.
        logger.error("Error closing the output stream: " + e.getMessage());
      }
    }
  }
    
      
  /**
   * Gets the Exception that happened during serialization.
   * 
   * @return Exception that happened in serialization
   */
  public Exception getException() {
    return null;
  }
          

  /**
   * Gets the status of the serialize process to determine whether it
   * has completed or not.
   * 
   * @param  identifier Identifier of the entity which is being serialized
   * @return The boolean value if serialize is completed or not
   */
  public boolean isCompleted(String identifier) {
    return true;
  }
    
      
  /**
   * Gets the result of serialize process - success or failure.
   * 
   * @param   identifier Identifier of the entity which has been serialized
   * @return  success or failure
   */
  public boolean isSuccess(String identifier) {
    return true;
  }
      
      
  /**
   * Load data from data storage system. Returns an input stream from which
   * the data can be loaded.
   * 
   * @param  url   the URL of the data source to be loaded
   * @return inputStream, an input stream from which the data can be loaded
   * @throws DataSourceNotFoundException when the input stream cannot be opened
   */
  public InputStream load(String url) 
          throws DataSourceNotFoundException {
    EmlPackageId emlPackageId = getUrlEmlPackageIdMapEntry(url);
    String entityId = getUrlEntityIdMapEntry(url);       
    EMLFileSystemEntity emlFileSystemEntity = 
                           new EMLFileSystemEntity(emlPackageId, entityId);
    emlFileSystemEntity.setEvaluateMode(this.evaluateMode);
    FileInputStream inputStream = null;
         
    try {
      File entityFile = emlFileSystemEntity.getEntityFile();
      inputStream = new FileInputStream(entityFile);
    }
    catch(Exception e) {
      throw new DataSourceNotFoundException(e.getMessage());
    }
    
        
    return inputStream;
  }
    
      
  /**
   * Starts to serialize the remote input stream. 
   * The OutputStream is the destination.
   * 
   * @param   url the URL of the stream to be serialized.
   * @return  the OutputStream to be serialized
   */
  public OutputStream startSerialize(String url) {
    logger.warn("EMLDataLoader.startSerialize()");
    BufferedOutputStream bufferedOutputStream = null;
    EmlPackageId emlPackageId = getUrlEmlPackageIdMapEntry(url);
    String entityId = getUrlEntityIdMapEntry(url);       
    EMLFileSystemEntity emlFileSystemEntity = 
                           new EMLFileSystemEntity(emlPackageId, entityId);
    emlFileSystemEntity.setEvaluateMode(this.evaluateMode);
    File entityFile = emlFileSystemEntity.getEntityFile();
        
    try {
      fileOutputStream = new FileOutputStream(entityFile);
      bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
    }
    catch(FileNotFoundException e) {
      // Data Storage Interface doesn't allow this method to throw
      // an exception, so log an error.
      logger.error("Error opening the output stream: " + e.getMessage());
    }
          
    return bufferedOutputStream;
  }
    
}
