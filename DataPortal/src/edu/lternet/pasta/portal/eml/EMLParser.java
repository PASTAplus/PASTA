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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/*
 * This class parses EML metadata for values needed by the DAS, such
 * as entity names, email addresses, and data URLs.
 */
public class EMLParser {

  /*
   * Class fields
   */

  private static final Logger logger = Logger.getLogger(EMLParser.class);

  // constants
  public static final String ENTITY_PATH_PARENT = "//dataset/";
  public static final String OTHER_ENTITY = "otherEntity";
  public static final String TABLE_ENTITY = "dataTable";
  public static final String SPATIAL_RASTER_ENTITY = "spatialRaster";
  public static final String SPATIAL_VECTOR_ENTITY = "spatialVector";
  public static final String STORED_PROCEDURE_ENTITY = 
                                                  "storedProcedure";
  public static final String VIEW_ENTITY = "view";
  private static final String PACKAGE_ID_PATH = "//eml/@packageId";
  
  private static final String ENTITY_NAME = "entityName";
  private static final String OBJECT_NAME = "physical/objectName";
  private static final String ONLINE_URL = "physical/distribution/online/url";
  
  String[] ENTITY_TYPES = 
  {
      OTHER_ENTITY,
      SPATIAL_RASTER_ENTITY,
      SPATIAL_VECTOR_ENTITY,
      STORED_PROCEDURE_ENTITY,
      TABLE_ENTITY,
      VIEW_ENTITY
  };
  
  /*
   * Instance fields
   */
  
  DataPackage dataPackage = null;
  
  
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
   * Parses an EML document.
   * 
   * @param   inputStream          the input stream to the EML document
   * @return  dataPackage          a DataPackage object holding parsed values
   */
  public DataPackage parseDocument(InputStream inputStream) 
          throws ParserConfigurationException {
    
    this.dataPackage = new DataPackage();
    ArrayList<Entity> entityList = dataPackage.getEntityList();
    
    DocumentBuilder documentBuilder = 
              DocumentBuilderFactory.newInstance().newDocumentBuilder();
    CachedXPathAPI xpathapi = new CachedXPathAPI();

    Document document = null;

    try {
      document = documentBuilder.parse(inputStream);
      
      if (document != null) {
        // process packageId
        Node packageIdNode = null;
        packageIdNode = xpathapi.selectSingleNode(document, PACKAGE_ID_PATH);

        if (packageIdNode != null) {
          String packageId = packageIdNode.getNodeValue();
          this.dataPackage.setPackageId(packageId);
        }
        
        for (int j = 0; j < ENTITY_TYPES.length; j++) {
          String ENTITY_TYPE = ENTITY_TYPES[j];
          String ENTITY_PATH = ENTITY_PATH_PARENT + ENTITY_TYPE;
      
          // Parse the entity name
          NodeList entityNodeList = 
                                xpathapi.selectNodeList(document, ENTITY_PATH);
      
          if (entityNodeList != null) {
            for (int i = 0; i < entityNodeList.getLength(); i++) {
              Entity entity = new Entity();
              Node entityNode = entityNodeList.item(i);
          
              // get the entityName
              NodeList entityNameNodeList = xpathapi.selectNodeList(
                                          entityNode,
                                          ENTITY_NAME
                                                                 );
          
              if (entityNameNodeList != null && entityNameNodeList.getLength() > 0) {
                String entityName = entityNameNodeList.item(0).getTextContent();
                entity.setName(entityName);
              }
          
          
              // get the objectName
              NodeList objectNameNodeList = xpathapi.selectNodeList(
                                          entityNode,
                                          OBJECT_NAME
                                                                 );
          
              if (objectNameNodeList != null && objectNameNodeList.getLength() > 0) {
                String objectName = objectNameNodeList.item(0).getTextContent();
                entity.setObjectName(objectName);
              }
          
              // get the distribution information
              NodeList urlNodeList = xpathapi.selectNodeList(
                                             entityNode,
                                             ONLINE_URL
                                 );
          
              if (urlNodeList != null && urlNodeList.getLength() > 0) {
                String url = urlNodeList.item(0).getTextContent();
                entity.setUrl(url);
              }
          
              entityList.add(entity);
            }
          } 
        } 
      }
    }
    catch (SAXException e) {
      logger.error("Error parsing document: SAXException");
      e.printStackTrace();
    } 
    catch (IOException e) {
      logger.error("Error parsing document: IOException");
      e.printStackTrace();
    }
    catch (TransformerException e) {
      logger.error("Error parsing document: TransformerException");
      e.printStackTrace();
    }

    return this.dataPackage;
  }
  
}
