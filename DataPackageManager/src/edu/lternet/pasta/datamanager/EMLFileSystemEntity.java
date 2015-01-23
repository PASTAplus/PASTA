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

import java.io.File;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;

/**
 * 
 * Manages the creation, storage, retrieval, and deletion of local
 * file system entities.
 * 
 * @author dcosta
 * @created 18-Nov-2010 4:30:02 PM
 */
public class EMLFileSystemEntity extends EMLEntity {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private String baseDir = null;  /* Top-level directory for this entity */
  private String checksum = null;
  private boolean evaluateMode = false;
  
  /*
   * Indicates whether data storage for this file system entity been optimized.
   * By default, we assume that it has not until we run the data storage optimization
   * logic on it (see the StorageManager class).
   */
  private boolean optimized = false;     
  
  
  /*
   * Constructors
   */
  
  /**
   * Constructs an EMLFileSystemEntity object based on the EmlPackageId
   * and the entityId.
   * 
   * @param  emlPackageId   an EMLPackageId object
   * @param  entityId       a String, the entity identifier
   */
  public EMLFileSystemEntity(EmlPackageId emlPackageId, String entityId) {
    this.baseDir = EMLDataManager.getEntityDir();
    this.entityId = entityId;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    if (emlPackageIdFormat != null) {
      this.packageId = emlPackageIdFormat.format(emlPackageId);
    }
  }


  /**
   * Constructs an EMLFileSystemEntity object based on a known base
   * directory, the EmlPackageId and the entityId. We use this constructor
   * when the base directory has been previously stored in the Data Package 
   * Registry, so it is typically used when reading an existing data entity.
   * 
   * @param  resourceLocation the location of this entity resource (for now, we
   *           only support file system locations)
   * @param  emlPackageId   an EMLPackageId object
   * @param  entityId       a String, the entity identifier
   */
  public EMLFileSystemEntity(String resourceLocation, EmlPackageId emlPackageId, String entityId) {
    this.baseDir = resourceLocation;
    this.entityId = entityId;
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    if (emlPackageIdFormat != null) {
      this.packageId = emlPackageIdFormat.format(emlPackageId);
    }
  }


  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */

	/**
	 * Deletes this entity from the file system.
	 * 
	 * @param emlPackageId  The emlPackageId object
	 * @param entityId      The entity id string
	 * @return true if the entity was successfully deleted, else false
	 */
	public boolean deleteEntity() {
		boolean success = false;
		File entityFile = getEntityFile();
		
		if (entityFile != null) {
		  success = entityFile.delete();
		}
		
		// Do some housekeeping on empty directories
		String dirPath = getDirPath();
		if (dirPath != null) {
		  File dirFile = new File(dirPath);
		  if (dirFile != null && dirFile.exists()) {
		    dirFile.delete();
		  }
		}
		
		return success;
	}
	
	
	/**
	 * Boolean to determine whether the entity data exists on the file system.
	 * 
	 * @return  true if it exists, else false
	 */
	public boolean exists() {
	  boolean exists = false;
	  File entityFile = getEntityFile();
	  
	  if (entityFile != null) {
	    exists = entityFile.exists();
	  }
	  
	  return exists;
	}
	
	
	/**
	 * Get the checksum value for this data entity
	 */
	public String getChecksum() {
		return checksum;
	}

	
  /**
   * Returns a path to the file system directory where the entity is stored.
   * 
   * @return  dirPath, the path to the directory, a String
   */
  public String getDirPath() {
    String dirPath = null;
    StringBuffer stringBuffer = new StringBuffer("");
    stringBuffer.append(this.baseDir);
    stringBuffer.append("/");
    stringBuffer.append(this.packageId);
    if (this.evaluateMode) {
      stringBuffer.append("/evaluate");
    }
    dirPath = stringBuffer.toString();
    
    return dirPath;
  }

  
	/**
	 * Access the entity file from the file system. Creates the
	 * directory that stores the entity if it doesn't already exist
	 * on the file system.
	 * 
	 * @return  the File object holding the entity data.
	 */
	public File getEntityFile() {
	  File entityFile = null;
	  
	  String dirPath = getDirPath();
	  
	  File dirFile = new File(dirPath);
	  
	  if (dirFile != null && !dirFile.exists()) {
	    dirFile.mkdirs();
	  }
	  
	  entityFile = new File(dirPath, entityId);
	  
	  return entityFile;
	}
	
	
	/**
	 * Returns the resource location value for this file system entity, as
	 * stored in the baseDir instance variable.
	 * 
	 * @return  baseDir, the resource location value
	 */
	public String getResourceLocation() {
		return baseDir;
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
   * Retrieves the optimized boolean value.
   * 
   * @return optimized, true if data storage for this entity has been optimized
   */
  public boolean isOptimized() {
	  return optimized;
  }
  
  
  /**
   * Sets the checksum value for this data entity.
   * 
   * @param checksum  the checksum value
   */
  public void setChecksum(String checksum) {
	  this.checksum = checksum;
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
   * Sets the optimized boolean value.
   * 
   * @param wasOptimized, the boolean value to set. Typically we would only
   *                   set this to true and only after the optimization logic
   *                   was successfully executed on this entity.
   */
  public void setOptimized(boolean wasOptimized) {
    this.optimized = wasOptimized;
  }

}
