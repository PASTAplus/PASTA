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
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.datamanager.EMLDataManager;
import edu.ucsb.nceas.utilities.IOUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;

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
  
  private final String LEVEL_ONE_FILE_NAME = "Level-1-EML.xml";
  private final String LEVEL_ZERO_FILE_NAME = "Level-0-EML.xml";
  
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
   * 'org.ecoinformatics.datamanager.parser.DataPackage' object. EMLDataPackage 
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
  
  
  /**
   * Deletes data package resources from the file system. Only evaluate
   * mode data package are allowed to be deleted from the file system.
   * 
   * @param   isEvaluate  true if this evaluate mode. 
   */
   public void deleteDataPackageResources(boolean isEvaluate)
           throws IOException {
     if (isEvaluate && this.emlPackageId != null) {
       // First cleanup the metadata files
       FileSystemResource dataPackageResource = 
           new FileSystemResource(emlPackageId);
       dataPackageResource.setEvaluateMode(isEvaluate);
       String dirPath = dataPackageResource.getDirPath();   
       File dirFile = new File(dirPath);  
       if (dirFile != null && dirFile.exists()) { 
         //FileUtils.deleteQuietly(dirFile);
       }
       
       // Now cleanup the data entity files  
       StringBuffer stringBuffer = new StringBuffer("");
       String baseDir = EMLDataManager.getEntityDir();
       stringBuffer.append(baseDir);
       stringBuffer.append("/");
       String packageId = dataPackage.getPackageId();
       stringBuffer.append(packageId);
       stringBuffer.append("/evaluate");
       dirPath = stringBuffer.toString();
       dirFile = new File(dirPath);  
       if (dirFile != null && dirFile.exists()) { 
         FileUtils.deleteQuietly(dirFile);
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

  
	/**
	 * Stores a local copy of EML metadata on the file system.
	 * 
	 * @param   xmlContent   the XML string content
	 * @param   isLevelZero  true if this is Level-0 metadata,
	 *                       false if this is Level-1 metadata
	 * @return  the EML file that was created
	 */
	 public File storeMetadata(String xmlContent, boolean isLevelZero) 
	         throws IOException {
	   File emlFile = null;
	   
	   if (this.emlPackageId != null) {
	     FileSystemResource reportResource = 
	         new FileSystemResource(emlPackageId);
	     String dirPath = reportResource.getDirPath();   
	     File dirFile = new File(dirPath);  
	     if (dirFile != null && !dirFile.exists()) { dirFile.mkdirs(); }
	     String emlFilename = (isLevelZero ? LEVEL_ZERO_FILE_NAME : LEVEL_ONE_FILE_NAME);
	     emlFile = new File(dirPath, emlFilename);
	     FileWriter fileWriter = null;
	     
	     StringBuffer stringBuffer = new StringBuffer(xmlContent);
	     try {
	       fileWriter = new FileWriter(emlFile);
	       IOUtil.writeToWriter(stringBuffer, fileWriter, true);
	     }
	     catch (IOException e) {
	       logger.error("IOException storing quality report:\n" + 
	                    e.getMessage());
	       e.printStackTrace();
	       throw(e);
	     }
	     finally {
	       if (fileWriter != null) { fileWriter.close(); }
	     }
	   }
	   
	   return emlFile;
	 }

	 
	/**
	 * Derive a Level-1 EML file from a Level-0 EML file.
	 * 
	 * @param levelZeroEMLFile    the Level-0 EML file
	 * @param entityURIHashMap       an association where the keys
	 *         are entity names (<entityName>) and the values are
	 *         the PASTA data URLs that should replace the site
	 *         data URLs in the Level-1 EML document.
	 * @return a Level-1 EML file
	 * @throws IOException
	 * @throws TransformerException
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 */
 public File toLevelOne(File levelZeroEMLFile,
                        HashMap<String, String> entityURIHashMap) 
         throws IOException,
                TransformerException, SAXException, ParserConfigurationException {
    String levelOneEMLString = null;
    File levelOneEMLFile = null;
    Document levelZeroEMLDocument = XmlUtility.xmlFileToDocument(levelZeroEMLFile);
    Node documentElement = levelZeroEMLDocument.getDocumentElement();
    String levelZeroEMLString = XMLUtilities
        .getDOMTreeAsString(documentElement);
    LevelOneEMLFactory levelOneEMLFactory = new LevelOneEMLFactory();
    Document levelOneEMLDocument = levelOneEMLFactory.make(
        levelZeroEMLDocument, entityURIHashMap);
    documentElement = levelOneEMLDocument.getDocumentElement();
    levelOneEMLString = XMLUtilities.getDOMTreeAsString(documentElement);
    
    // Convert to dereferenced EML
    // levelOneEMLString = DataPackage.dereferenceEML(levelOneEMLString);

    try {
      boolean isLevelZero = true;
      storeMetadata(levelZeroEMLString, isLevelZero);

      isLevelZero = false;
      levelOneEMLFile = storeMetadata(levelOneEMLString, isLevelZero);      
    }
    catch (IOException e) {
      throw (e);
    }

    return levelOneEMLFile;
  }

}