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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import edu.lternet.pasta.dml.parser.DataPackage;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.datamanager.EMLDataManager;

/**
 * @author dcosta
 * @version 1.0
 * @created 16-Aug-2011 1:40:03 PM
 * 
 * EMLDataPackage models a single EML data package in PASTA. It makes
 * use of the DataPackage class in the Data Manager Library.
 */
public class EMLDataPackage {

  /*
   * Class fields
   */
  private static final String DOT = ".";
  
  
  /*
   * Instance fields
   */
  
    private Logger logger = Logger.getLogger(EMLDataPackage.class);
  
	public DataPackage dataPackage;   // A Data Manager Library data package
	private ArrayList<EMLEntity> emlEntityList = null;
    private EmlPackageId emlPackageId = null;
	private String scope = null;
	private Integer identifier = null;
	private Integer revision = null;

	
  /*
   * Constructors
   */
  
  /**
   * Constructs an EMLDataPackage object with a specified
   * 'edu.lternet.pasta.dml.parser.DataPackage' object. EMLDataPackage 
   * acts as a wrapper that encapsulates its Data Manager data package object.
   * 
   * @param dataPackage  A DataPackage object as defined by the Data Manager 
   *                     Library.
   */
  public EMLDataPackage(DataPackage dataPackage)
          throws Exception {
    if (dataPackage == null) {
      String message = "DataPackage is null";
      throw new Exception(message);      
    }
    
    String packageId = dataPackage.getPackageId();
    
    if (packageId == null || packageId.equals("")) {
      String message = "No packageId value found in document. This may not be a valid EML document.";
      throw new Exception(message);
    }
    else {
      EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
      this.emlPackageId = emlPackageIdFormat.parse(packageId);
      this.scope = emlPackageId.getScope();
      this.identifier = emlPackageId.getIdentifier();
      this.revision = emlPackageId.getRevision();
    }
    
    this.dataPackage = dataPackage;
    this.emlEntityList = new ArrayList<EMLEntity>();
  }


  /*
   * Class methods
   */
  
  /**
   * Compose a docid (the scope-dot-identifier without the
   * revision value) from a specified scope and identifier values.
   * 
   * @return  the docid string
   */
  public static String composeDocid(String scope, Integer identifier) {
    return scope + DOT + identifier;
  }


  /**
   * Compose a packageId string (scope-dot-identifier-dot-revision)
   * from the specified scope, identifier, and revision values.
   * 
   * @return  the docid string
   */
  public static String composePackageId(String scope, Integer identifier, String revision) {
    return scope + DOT + identifier + DOT + revision;
  }


  /*
   * Instance methods
   */
  
  public void addEMLEntity(EMLEntity emlEntity) {
    if (emlEntity != null) {
      emlEntityList.add(emlEntity);
    }
  }
  
  
	private boolean isEmptyDirectory(File dirFile) {
		boolean isEmpty = false;

		if (dirFile != null && dirFile.isDirectory()) {
			if (dirFile.list().length == 0) {
				isEmpty = true;
			} 
		} 
		else {
			logger.error(String.format("%s is not a directory", dirFile.getPath()));
		}

		return isEmpty;
	}
  
	
	/**
	 * Deletes data package resources from the file system.
	 * 
	 * @param isEvaluate
	 *            true if this evaluate mode.
	 */
	public void deleteDataPackageResources(boolean isEvaluate) throws IOException {
		boolean success = false;

		 /* 
		  * Only evaluate mode data package resources are allowed to be deleted
		  * from the file system.
		  */
		if (isEvaluate && this.emlPackageId != null) {

			/*
			 * Cleanup the data entity files that were downloaded for 
			 * the evaluate operation
			 */
			StringBuffer stringBuffer = new StringBuffer("");
			String baseDir = EMLDataManager.getEntityDir();
			stringBuffer.append(baseDir);
			stringBuffer.append("/");

			String packageId = dataPackage.getPackageId();
			stringBuffer.append(packageId);
			String packageIdPath = stringBuffer.toString();
			File packageIdDir = new File(packageIdPath);

			stringBuffer.append("/evaluate");
			String evaluateDirPath = stringBuffer.toString();
			File evaluateDir = new File(evaluateDirPath);
			if (evaluateDir != null && evaluateDir.exists()) {
				success = FileUtils.deleteQuietly(evaluateDir);
				if (!success) {
					logger.warn(
							String.format("Unable to delete %s after completion of evaluate", 
									      evaluateDirPath));
				}
				else {
					logger.info(
							String.format("Deleted %s after completion of evaluate", 
									      evaluateDirPath));
				}
			}

			/*
			 * Cleanup the top-level packageId directory, but only if it is
			 * empty! We don't an evaluate operation to remove valid resources
			 * that were previously uploaded into PASTA! 
			 */
			if (packageIdDir != null && 
				packageIdDir.exists() && 
				isEmptyDirectory(packageIdDir)
			   ) {
				success = FileUtils.deleteQuietly(packageIdDir);
				if (!success) {
					logger.warn(String.format("Unable to delete %s after completion of evaluate",
							                  packageIdPath));
				}
				else {
					logger.info(
							String.format("Deleted %s after completion of evaluate", 
									      packageIdPath));
				}
			}

		}
	}
   
  /**
   * Accessor method for getting the Data Manager Library
   * Data Package object.
   * 
   * @return  the dataPackage instance variable, a Data Manager Library
   *          data package
   */
  public DataPackage getDataPackage() {
    return dataPackage;
  }
  
  
	/**
	 * Get the docid (the scope-dot-identifier without the
	 * revision value).
	 * 
	 * @return  the docid string
	 */
	public String getDocid() {
	  return scope + "." + identifier;
	}
	
	
	/**
	 * Gets the <access> element XML string for this data package.
	 * 
	 * @return  an XML string holding the <access> element for the
	 *          data package.
	 */
	public String getAccessXML() {
	  String xmlString = null;
	  
	  xmlString = dataPackage.getAccessXML();
	  
	  return xmlString;
	}
	
	
	/**
	 * Gets the list of EMLEntity objects for this data package.
	 * 
	 * @return   emlEntityList value
	 */
	public ArrayList<EMLEntity> getEMLEntityList() {
	  return emlEntityList;
	}

	
	/**
	 * Return the metadata format type, which is equivalent to the
	 * EML namespace value of the data package.
	 * 
	 * @return  the metadata format type, 
	 *          e.g. "eml://ecoinformatics.org/eml-2.1.1"
	 */
	public String getFormatType() {
		return dataPackage.getEmlNamespace();
	}
	
	
	public Integer getIdentifier() {
	  return this.identifier;
	}
	
	
	public String getPackageId(){
		return dataPackage.getPackageId();
	}
	
	
	public Integer getRevision() {
	  return this.revision;
	}
	
	
	public String getScope() {
	  return this.scope;
	}

	
  /*
   * Determines whether the metadata document describes at
   * least one data entity.
   * 
   * @return true if the data package has at least one
   *         entity, else false
   */
  public boolean hasEntity() {
    boolean hasEntity = false;
    
    if (this.dataPackage != null) {
      int n = numberOfEntities();
      if (n > 0) {
        hasEntity = true;
      }
    }

    return hasEntity;
  }
  
  
	/**
	 * Boolean to determine whether the data entities for this
	 * data package are all valid.
	 * 
	 * @return true if all of the data entities are valid, else false
	 */
	public boolean isDataValid() {
		boolean isValid = true;
		
		boolean hasError = dataPackage.hasQualityError();
		isValid = !hasError;
		
		return isValid;
	}

	
	/**
	 * Boolean to determine whether this is a Level-1
	 * data package.
	 * 
	 * @return  true if this is a Level-1 data package,
	 *          else false
	 */
	public boolean isLevelOne() {
		boolean isLevelOne = false;
		
		if (this.dataPackage != null) {
	    String systemAttributeValue = dataPackage.getSystem();
	    if (systemAttributeValue != null) {
	      if (systemAttributeValue.equals(LevelOneEMLFactory.LEVEL_ONE_SYSTEM_ATTRIBUTE)
	         ) {
	        isLevelOne = true;
	      }
	    }
		}
		
		return isLevelOne;
	}

	
  /*
   * Determines whether the metadata document is valid for PASTA.
   * For now, just check that it can be parsed and that one or
   * more data entities can be found.
   */
  public boolean isMetadataValid() {
    boolean isValid = true;
    
    if (this.dataPackage != null) {
      int n = numberOfEntities();
      if (n < 1) {
        isValid = false;
      }
      else if (dataPackage.hasDatasetQualityError()) {
        isValid = false;
      }
    }
    else {
      // Should never be reached since the dataPackage 
      // should always be non-null.
      isValid = false;
    }
    
    return isValid;
  }
  
  
  /**
   * Returns the number of entities in the data package.
   * 
   * @return -1 if unknown, else a whole number indicating the 
   *         number of entities in the data package
   */
  public int numberOfEntities() {
    int n = -1;
    if (this.dataPackage != null) {
      n = dataPackage.getEntityNumber();
    }
    return n;
  }

  
}