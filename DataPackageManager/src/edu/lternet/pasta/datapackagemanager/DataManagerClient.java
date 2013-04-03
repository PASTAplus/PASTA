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

import org.ecoinformatics.datamanager.parser.DataPackage;

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
   * @return             A list of entityId / entityName pairs
	 */
	public String createDataEntities(DataPackage dataPackage) 
	        throws IOException,
	               MalformedURLException,
	               Exception {
    boolean evaluateMode = false;
    StringBuffer stringBuffer = new StringBuffer("");
    
    EMLDataManager emlDataManager = new EMLDataManager();
    ArrayList<String> entityPairs = emlDataManager.createDataEntities(dataPackage, evaluateMode);
      
    for (String entityURL : entityPairs) {
      stringBuffer.append(entityURL + "\n");
    }
    
    String entityPairsList = stringBuffer.toString();
    return entityPairsList;
	}

	
	/**
	 * Delete data entities for the specified scope and identifier from the
	 * Data Manager.
	 * 
	 * @param   scope       the scope of the data package to be deleted
	 * @param   identifier  the identifier of the data package to be deleted
	 * @return  true if the data entities were successfully deleted, else false
	 */
	public boolean deleteDataEntities(String scope, Integer identifier) 
	        throws IOException, 
	               Exception {
		boolean deleted = false;
		int rowCount = 0;
		
    EMLDataManager emlDataManager = new EMLDataManager();
    rowCount = emlDataManager.deleteDataEntities(scope, identifier.toString());
    deleted = (rowCount > 0);
		
		return deleted;
	}

	
  /**
   * Delete data entities for the specified scope, identifier,
   * and revision from the Data Manager. This is typically used
   * in a rollback procedure during a failed Create or Update.
   * 
   * @param   scope       the scope of the data package to be deleted
   * @param   identifier  the identifier of the data package to be deleted
   * @param   revision    the revision of the data package to be deleted
   * @return  true if the data entities were successfully deleted, else false
   */
  public boolean deleteDataEntities(String scope, 
                                    Integer identifier, 
                                    Integer revision) 
          throws IOException, 
                 Exception {
    boolean deleted = false;
    int rowCount = 0;
    
    EMLDataManager emlDataManager = new EMLDataManager();
    rowCount = emlDataManager.deleteDataEntities(scope, 
                                                 identifier.toString(), 
                                                 revision.toString());
    deleted = (rowCount > 0);
    
    return deleted;
  }

  
  /**
   * Evaluate data entities in the Data Manager
   * 
   * @param dataPackage  the DataPackage object describing the data entities
   *                     to be created
   * @return             A list of entityId / entityName pairs
   */
  public String evaluateDataEntities(DataPackage dataPackage)
      throws IOException, Exception {
    
    StringBuffer stringBuffer = new StringBuffer("");
    boolean evaluateMode = true;
    
    EMLDataManager emlDataManager = new EMLDataManager();
    ArrayList<String> entityURLs = emlDataManager.createDataEntities(dataPackage, evaluateMode);
      
    for (String entityURL : entityURLs) {
      stringBuffer.append(entityURL + "\n");
    }
     
    String entityIdNamePairs = stringBuffer.toString();
    return entityIdNamePairs;
  }

	
  /**
   * Reads a data entity and returns it as a byte array.
   * 
   * @param resourceLocation The base storage location for the entity resource
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param entityId    The entityId of the entity to be read
   * @return            A byte[] containing the entity data
   */
	public byte[] readDataEntity(String resourceLocation,
	                             String scope, 
	                             Integer identifier, 
	                             String revision, 
	                             String entityId) 
	        throws IOException, 
	               ResourceNotFoundException,
	               Exception {
    byte[] byteArray = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    InputStream inputStream = null;	  
    EMLDataManager emlDataManager = new EMLDataManager(); 

    /*
     * Handle symbolic revisions such as "newest" and "oldest".
     */
    if (revision != null) {
      if (revision.equals("newest")) {
        Integer newest = emlDataManager.getNewestRevision(scope, identifier.toString());
        if (newest != null) { revision = newest.toString(); }
      }
      else if (revision.equals("oldest")) {
        Integer oldest = emlDataManager.getOldestRevision(scope, identifier.toString());
        if (oldest != null) { revision = oldest.toString(); }
      }
    }
    
    File file = emlDataManager.readDataEntity(resourceLocation, scope, identifier.toString(), revision, entityId);
    
    inputStream = new FileInputStream(file);
    byteArrayOutputStream = new ByteArrayOutputStream(); 
    byte[] b = new byte[1024];
    // Read the first kilobyte
    int bytesRead = inputStream.read(b, 0, 1024);
    while (bytesRead > -1) {
      byteArrayOutputStream.write(b, 0, bytesRead);
      // Read the next kilobyte
      bytesRead = inputStream.read(b, 0, 1024);
    }
    byteArrayOutputStream.flush();
    byteArray = byteArrayOutputStream.toByteArray();

    if (inputStream != null) {
      inputStream.close();
    }
    if (byteArrayOutputStream != null) {
      byteArrayOutputStream.close();
    }

    return byteArray;
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
    byte[] byteArray = null;
    ByteArrayOutputStream byteArrayOutputStream = null;
    InputStream inputStream = null;	  
    EMLDataManager emlDataManager = new EMLDataManager(); 

    /*
     * Handle symbolic revisions such as "newest" and "oldest".
     */
    if (revision != null) {
      if (revision.equals("newest")) {
        Integer newest = emlDataManager.getNewestRevision(scope, identifier.toString());
        if (newest != null) { revision = newest.toString(); }
      }
      else if (revision.equals("oldest")) {
        Integer oldest = emlDataManager.getOldestRevision(scope, identifier.toString());
        if (oldest != null) { revision = oldest.toString(); }
      }
    }
    
    file = emlDataManager.readDataEntity(resourceLocation, scope, identifier.toString(), revision, entityId);

    return file;
	}

	/*
	 * The following group of methods for reading data package reports and
	 * entity reports is now deprecated. These methods were useful when the Data
	 * Manager was a separate service, but they are no longer needed by the
	 * Data Package Manager because it is now able to do all of its own report
	 * processing.
	 */
		
  /**
   * Reads a data entity evaluate-report and returns it as a String.
   * 
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param entityId    The entityId of the entity report to be read
   * @param produceHTML Boolean to determine whether the report should be 
   *                      represented in HTML.
   * @return            The entity report, an XML or an HTML string.
   *
  public String readDataEntityEvaluateReport(String scope, Integer identifier, 
                                             String revision, String entityId, 
                                             boolean produceHTML) 
      throws IOException, 
             ResourceNotFoundException,
             Exception {
    String reportString = null;
    boolean evaluateMode = true;
    
    reportString = readDataEntityReportAux(scope, identifier, revision, entityId, produceHTML, evaluateMode);

    return reportString;
  }
  
  
  /**
   * Reads a data entity report and returns it as a String.
   * 
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param entityId    The entityId of the entity report to be read
   * @param produceHTML Boolean to determine whether the report should be 
   *                      represented in HTML.
   * @return            The entity report, an XML or an HTML string.
   *
  public String readDataEntityReport(String scope, Integer identifier, 
                                     String revision, String entityId, 
                                     boolean produceHTML) 
      throws IOException, 
             ResourceNotFoundException,
             Exception {
    String reportString = null;
    boolean evaluateMode = false;
    
    reportString = readDataEntityReportAux(scope, identifier, revision, entityId, produceHTML, evaluateMode);

    return reportString;
  }
  
  
  /**
   * Auxiliary method to support both the readDataEntityEvaluateReport() and
   * readDataEntityReport() methods. The only difference between the two methods
   * is the urlFragment that gets passed in.
   * 
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param entityId    The entityId of the entity report to be read
   * @param produceHTML Boolean to determine whether the report should be 
   *                      represented in HTML.
   * @param evaluateMode true if this is evaluate mode
   * @return            The entity report, an XML or an HTML string.
   *
  private String readDataEntityReportAux(String scope, Integer identifier, 
                                         String revision, String entityId,
                                         boolean produceHTML, boolean evaluateMode)
      throws IOException, 
             ResourceNotFoundException,
             Exception {
    String reportString = null;  
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    
    EMLDataManager emlDataManager = new EMLDataManager(); 
    EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision);
    String packageId = emlPackageIdFormat.format(emlPackageId);
    File xmlFile = emlDataManager.getEntityReport(emlPackageId, entityId, evaluateMode);
    
    if (xmlFile != null && xmlFile.exists()) {
      if (produceHTML) {
        Options options = ConfigurationListener.getOptions();
        String xslPath = null;
        if (options != null) {
          xslPath = options.getOption("datapackagemanager.xslPath");
        }
          
        reportString = qualityReportXMLtoHTML(xmlFile, xslPath);
      }
      else {
        reportString = FileUtility.fileToString(xmlFile);
      }
    }
    else {
      ResourceNotFoundException e = new ResourceNotFoundException(
          "Unable to access entity report file for packageId: " + packageId +
                                                "; entityId: " + entityId);
      throw(e);
    }
    
    return reportString;
  }
  
  
  /**
   * Reads a data package quality evaluate-report and returns it as a String.
   * 
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param produceHTML Boolean to determine whether the report should be 
   *                      represented in HTML.
   * @return            The quality report, an XML or an HTML string.
   *
  public String readDataPackageEvaluateReport(String scope, 
                                              Integer identifier, 
                                              String revision, 
                                              boolean produceHTML) 
      throws IOException, 
             ResourceNotFoundException,
             Exception {
    String reportString = null;
    boolean evaluateMode = true;
    
    reportString = readDataPackageReportAux(scope, identifier, revision, produceHTML, evaluateMode);

    return reportString;
  }
  
  
  /**
   * Reads a data package quality report and returns it as a String.
   * 
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param produceHTML Boolean to determine whether the report should be 
   *                      represented in HTML.
   * @return            The quality report, an XML or an HTML string.
   *
  public String readDataPackageReport(String scope, 
                                      Integer identifier, 
                                      String revision,
                                      boolean produceHTML) 
      throws IOException, 
             ResourceNotFoundException,
             Exception {
    String reportString = null;
    boolean evaluateMode = false;
    
    reportString = readDataPackageReportAux(scope, identifier, revision, produceHTML, evaluateMode);

    return reportString;
  }
  
  
  /**
   * Auxiliary method to support both the readDataPackageEvaluateReport() and
   * readDataPackageReport() methods. The only difference between the two methods
   * is the urlFragment that gets passed in.
   * 
   * @param scope       The scope of the metadata document
   * @param identifier  The identifier of the metadata document
   * @param revision    The revision of the metadata document
   * @param produceHTML Boolean to determine whether the report should be 
   *                      represented in HTML.
   * @param evaluateMode true if this is evaluate mode
   * @return            The entity report, an XML or an HTML string.
   *
  private String readDataPackageReportAux(String scope, 
                                          Integer identifier, 
                                          String revision,
                                          boolean produceHTML, 
                                          boolean evaluateMode)
      throws IOException, 
             ResourceNotFoundException,
             Exception {
    String reportString = null;  
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    
    EMLDataManager emlDataManager = new EMLDataManager(); 
    EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope, identifier.toString(), revision);
    String packageId = emlPackageIdFormat.format(emlPackageId);
    File xmlFile = emlDataManager.getQualityReport(emlPackageId, evaluateMode);
    
    if (xmlFile != null && xmlFile.exists()) {
      if (produceHTML) {
        Options options = ConfigurationListener.getOptions();
        String xslPath = null;
        if (options != null) {
          xslPath = options.getOption("datapackagemanager.xslPath");
        }
          
        reportString = qualityReportXMLtoHTML(xmlFile, xslPath);
      }
      else {
        reportString = FileUtility.fileToString(xmlFile);
      }
    }
    else {
      ResourceNotFoundException e = new ResourceNotFoundException(
          "Unable to access quality report file for packageId: " + packageId);
      throw(e);
    }
    
    return reportString;
  }
    End of deprecated methods
  */

}