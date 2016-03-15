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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.xpath.CachedXPathAPI;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.parser.Entity;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * @author dcosta
 * @version 1.0
 * @created 03-Oct-2011 1:40:03 PM
 * 
 * EMLEntity models a single EML data entity in PASTA.
 */
public class EMLEntity {
  
  /*
   * Class fields
   */
  
  private static final String STATUS_PATH = "//qualityCheck/status";
  private static final String ERROR_STATUS = "error";

  /*
   * Instance fields
   */
  
  private EMLDataPackage emlDataPackage;  // The data package holding this entity
  private Entity entity = null;
  private String entityId = null;
  private String entityName = null;
  private String entityReport = null;
  private String entityURI = null;
  
  
  /*
   * Constructors
   */
  
  /**
   * Constructs an EMLEntity and sets a pointer back to the
   * EMLDataPackage object that contains this entity
   * 
   * @param  dataPackage   the data package object which
   *                       this entity is contained in
   */
  public EMLEntity(EMLDataPackage dataPackage) {
    this.emlDataPackage = dataPackage;
  }
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  /**
   * Boolean to determine whether an entity report contains a status
   * field with an error status.
   * 
   * @param entityReport    The entity report XML
   * @return                true if error status found, else false
   * @throws IOException
   */
  public boolean findErrorStatus(String entityReport) 
          throws IOException {
    boolean findErrorStatus = false;
    InputStream is = new ByteArrayInputStream(entityReport.getBytes());

    try {
      DocumentBuilder documentBuilder = 
        DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = documentBuilder.parse(is);   
      NodeList statusNodeList = null;
      CachedXPathAPI xpathapi = new CachedXPathAPI();

      // Process <status> elements
      statusNodeList = xpathapi.selectNodeList(document, STATUS_PATH);  
      if (statusNodeList != null) {
        for (int i = 0; i < statusNodeList.getLength(); i++) {
          // process 'status' element
          Node statusNode = statusNodeList.item(i);

          if (statusNode != null) {
            String statusValue = statusNode.getTextContent();
            if (statusValue.equals(ERROR_STATUS)) {
              findErrorStatus = true;
            }
          }
        }
      }
    }
    catch (TransformerException e) {
      System.err.println("TransformerException while parsing quality report: " + e.getMessage());
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    catch (ParserConfigurationException e) {
      System.err.println("ParserConfigurationException while parsing quality report: " + e.getMessage());
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    catch (SAXException e) {
      System.err.println("SAXException while parsing quality report: " + e.getMessage());
      e.printStackTrace();
      throw new IllegalStateException(e);
    }
    
    return findErrorStatus;
  }


  /**
   * Gets an <access> XML string for this entity.
   * If none is stored locally for the entity,
   * retrieves the <access> XML string for the
   * data package.
   * 
   * @return   an XML string or null
   */
  public String getAccessXML() {
    String xmlString = null;
    
    if (this.entity != null) {
      xmlString = entity.getEntityAccessXML();
    }
  
    /*
     * If no <access> element found for the entity, retrieve
     * it from the data package
     */
    if (xmlString == null) {
      if (emlDataPackage != null) {
        xmlString = emlDataPackage.getAccessXML();
      }
    }
    
    return xmlString;
  }
  
  
  public String getDataFormat() {
    String dataFormat = null;
    
    if (this.entity != null) {
    	dataFormat = entity.getDataFormat();
    }
      
    return dataFormat;
  }
  
  
  public String getEntityId() {
    return entityId;
  }
  

  public String getEntityName() {
    return entityName;
  }
  

  public String getEntityReport() {
    return entityReport;
  }
  

  public String getEntityURI() {
    return entityURI;
  }
  

  public void setEntityId(String entityId) {
    this.entityId = entityId;
  }
  

  public void setEntityName(String entityName) {
    this.entityName = entityName;
    
    if (emlDataPackage != null) {
      DataPackage dataPackage = emlDataPackage.getDataPackage();
      this.entity = dataPackage.getEntity(entityName);
    }
  }
  

  public void setEntityReport(String entityReport) {
    this.entityReport = entityReport;
  }
  
  
  public void setEntityURI(String entityURI) {
    this.entityURI = entityURI;
  }
  
}
