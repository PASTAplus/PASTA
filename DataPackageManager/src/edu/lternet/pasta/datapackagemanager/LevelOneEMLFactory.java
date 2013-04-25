/*
 *
 * $Date: 2011-04-29 13:03:20 -0700 (Fri, 29 Apr 2011) $
 * $Author: dcosta $
 * $Revision: 1026 $
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

package edu.lternet.pasta.datapackagemanager;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Constructs Level-1 metadata from Level-0 metadata
 * 
 */
public final class LevelOneEMLFactory {

  /*
   * Class variables and constants
   */
  private static final Logger logger = Logger.getLogger(LevelOneEMLFactory.class);

  private static final String ACCESS_PATH = "//access";
  private static final String CONTACT_PATH = "//dataset/contact";
  private static final String ENTITY_NAME = "entityName";
  private static final String ENTITY_PATH_PARENT = "//dataset/";
  private static final String LEVEL_ONE_AUTH_SYSTEM_ATTRIBUTE = "https://pasta.lternet.edu/authentication";
  public static final String LEVEL_ONE_SYSTEM_ATTRIBUTE = "https://pasta.lternet.edu";
  private static final String OBJECT_NAME = "physical/objectName";
  private static final String ONLINE_URL = "physical/distribution/online/url";
  private static final String OTHER_ENTITY = "otherEntity";
  private static final String SPATIAL_RASTER_ENTITY = "spatialRaster";
  private static final String SPATIAL_VECTOR_ENTITY = "spatialVector";
  private static final String STORED_PROCEDURE_ENTITY = "storedProcedure";
  private static final String SYSTEM_ATTRIBUTE_PATH = "//@system";
  private static final String TABLE_ENTITY = "dataTable";
  private static final String VIEW_ENTITY = "view";
  
  
  /*
   * Instance variables
   */
  
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
   * Constructors
   */

  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */

  /**
   * Make a Level-1 EML document from a Level-0 EML document.
   * 
   * @param  levelZeroEMLDocument  the Level-0 EML Document
   * @param  entityHashMap         a map of entity names and their associated entity IDs (URIs)
   * @return the Level-1 EML Document, a Document object
   * @throws TransformerException
   */
  public Document make(Document levelZeroEMLDocument, HashMap<String, String> entityHashMap) 
          throws TransformerException {
    if (levelZeroEMLDocument == null) {
      throw new IllegalArgumentException(
          "null Document object passed to LevelOneEMLFactory.make() method");
    }
    //String systemAttribute = getSystemAttribute(levelZeroEMLDocument);
    setSystemAttribute(levelZeroEMLDocument);
    appendContact(levelZeroEMLDocument);
    modifyDataURLs(levelZeroEMLDocument, entityHashMap);
    modifyAccessElementAttributes(levelZeroEMLDocument);

    return levelZeroEMLDocument;
  }


  /*
   * Append a Level-1 contact element to document containing contact
   * info for the LTER Network Office (LNO)
   */
  private void appendContact(Document doc) {
    Element lnoContact = doc.createElement("contact");
    Element positionName = doc.createElement("positionName");
    positionName.appendChild(doc.createTextNode("Information Manager"));
    Element organizationName = doc.createElement("organizationName");
    organizationName.appendChild(doc.createTextNode("LTER Network Office"));
    Element address = doc.createElement("address");
    Element deliveryPoint1 = doc.createElement("deliveryPoint");
    deliveryPoint1.appendChild(doc.createTextNode("UNM Biology Department, MSC03-2020"));
    Element deliveryPoint2 = doc.createElement("deliveryPoint");
    deliveryPoint2.appendChild(doc.createTextNode("1 University of New Mexico"));
    Element city = doc.createElement("city");
    city.appendChild(doc.createTextNode("Albuquerque"));    
    Element administrativeArea = doc.createElement("administrativeArea");
    administrativeArea.appendChild(doc.createTextNode("NM"));    
    Element postalCode = doc.createElement("postalCode");
    postalCode.appendChild(doc.createTextNode("87131-0001"));    
    Element country = doc.createElement("country");
    country.appendChild(doc.createTextNode("USA"));   
    address.appendChild(deliveryPoint1);
    address.appendChild(deliveryPoint2);
    address.appendChild(city);
    address.appendChild(administrativeArea);
    address.appendChild(postalCode);
    address.appendChild(country);
    Element phone1 = doc.createElement("phone");
    phone1.setAttribute("phonetype", "voice");
    phone1.appendChild(doc.createTextNode("505 277-2535"));
    Element phone2 = doc.createElement("phone");
    phone2.setAttribute("phonetype", "fax");
    phone2.appendChild(doc.createTextNode("505 277-2541"));
    Element electronicMailAddress = doc.createElement("electronicMailAddress");
    electronicMailAddress.appendChild(doc.createTextNode("tech-support@lternet.edu"));    
    Element onlineUrl = doc.createElement("onlineUrl");
    onlineUrl.appendChild(doc.createTextNode("http://www.lternet.edu"));    
    lnoContact.appendChild(positionName);
    lnoContact.appendChild(organizationName);
    lnoContact.appendChild(address);
    lnoContact.appendChild(phone1);
    lnoContact.appendChild(phone2);
    lnoContact.appendChild(electronicMailAddress);
    lnoContact.appendChild(onlineUrl);
    NodeList contacts = getContacts(doc);  
    Node datasetNode = getDatasetNode(doc);
    datasetNode.insertBefore(lnoContact, contacts.item(0));
  }


  /**
   * Returns a list of all {@code //dataset/contact} elements contained in
   * this EML document.
   * 
   * @return a list of all {@code //dataset/contact} elements contained in
   *         the EML document.
   */
  private NodeList getContacts(Document emlDocument) {
    NodeList nodeList = null;

    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      nodeList = (NodeList) xPath.evaluate(CONTACT_PATH, emlDocument, XPathConstants.NODESET);
    }
    catch (XPathExpressionException e) {
      throw new IllegalStateException(e);
    }

    return nodeList;
  }


  /**
   * Returns the dataset element node contained in
   * this EML document.
   * 
   * @return  the dataset element Node object
   */
  private Node getDatasetNode(Document emlDocument) {
    Node node = null;

    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      node = (Node) xPath.evaluate("//dataset", emlDocument, XPathConstants.NODE);
    }
    catch (XPathExpressionException e) {
      throw new IllegalStateException(e);
    }

    return node;
  }


  /**
   * Returns the system attribute value of the provided EML document. If the
   * document does not contain the attribute {@code //@system}, or if it does
   * not have a value, an empty string is returned.
   * 
   * @param emlDocument
   *          an EML document Document object
   * 
   * @return the system attribute value
   */
  private String getSystemAttribute(Document levelZeroEMLDocument) {
    String systemAttribute = "";

    try {
      XPath xpath = XPathFactory.newInstance().newXPath();
      systemAttribute = xpath.evaluate(SYSTEM_ATTRIBUTE_PATH, levelZeroEMLDocument);
    }
    catch (XPathExpressionException e) {
      throw new IllegalStateException(e); // Should never be reached
    }

    return systemAttribute;
  }
  
  
  private void modifyAccessElementAttributes(Document emlDocument)
          throws TransformerException {
    CachedXPathAPI xpathapi = new CachedXPathAPI();

    // Parse the access elements
    NodeList accessNodeList = xpathapi.selectNodeList(emlDocument, ACCESS_PATH);
    if (accessNodeList != null) {
      for (int i = 0; i < accessNodeList.getLength(); i++) {
        boolean hasSystemAttribute = false;
        Element accessElement = (Element) accessNodeList.item(i);
        NamedNodeMap accessAttributesList = accessElement.getAttributes();
        
        for (int j = 0; j < accessAttributesList.getLength(); j++) {
          Node attributeNode = accessAttributesList.item(j);
          String nodeName = attributeNode.getNodeName();
          String nodeValue = attributeNode.getNodeValue();
          if (nodeName.equals("authSystem")) {
            attributeNode.setNodeValue(LEVEL_ONE_AUTH_SYSTEM_ATTRIBUTE);
          }
          else if (nodeName.equals("system")) {
            attributeNode.setNodeValue(LEVEL_ONE_SYSTEM_ATTRIBUTE);
            hasSystemAttribute = true;
          }
        }
        
        /*
         * No @system attribute was found in the access element, so we
         * need to add one.
         */
        if (!hasSystemAttribute) {
          Attr systemAttribute = emlDocument.createAttribute("system");
          systemAttribute.setTextContent(LEVEL_ONE_SYSTEM_ATTRIBUTE);
          accessElement.setAttributeNode(systemAttribute);
        }
      }
    }
  }


  /*
   * Modify the documents data URLs to Level-1 data URLs
   */
  private void modifyDataURLs(Document emlDocument, HashMap<String, String> entityHashMap) 
          throws TransformerException {
    CachedXPathAPI xpathapi = new CachedXPathAPI();
    
    for (int j = 0; j < ENTITY_TYPES.length; j++) {
      String ENTITY_TYPE = ENTITY_TYPES[j];
      String ENTITY_PATH = ENTITY_PATH_PARENT + ENTITY_TYPE;
      logger.debug("ENTITY_PATH: " + ENTITY_PATH);
  
      // Parse the entity name
      NodeList entityNodeList = xpathapi.selectNodeList(emlDocument, ENTITY_PATH);
  
      if (entityNodeList != null) {
        for (int i = 0; i < entityNodeList.getLength(); i++) {
          Node entityNode = entityNodeList.item(i);
      
          // Get the entityName
          NodeList entityNameNodeList = xpathapi.selectNodeList(entityNode, ENTITY_NAME);
      
          if (entityNameNodeList != null && entityNameNodeList.getLength() > 0) {
            String entityName = entityNameNodeList.item(0).getTextContent();
            logger.debug("entityName: " + entityName);
            Set<Entry<String, String>> entrySet = entityHashMap.entrySet();
            for (Entry<String, String> entry : entrySet) {
              String entryKey = entry.getKey();
              String entryValue = entry.getValue();
              if (entryKey.equals(entityName)) {
                logger.debug("Matched entityName: " + entityName);
      
                // Get the objectName
                NodeList objectNameNodeList = xpathapi.selectNodeList(entityNode, OBJECT_NAME);
      
                if (objectNameNodeList != null && objectNameNodeList.getLength() > 0) {
                  String objectName = objectNameNodeList.item(0).getTextContent();
                  logger.debug("objectName: " + objectName);
                }
      
                // Get the distribution information
                NodeList urlNodeList = xpathapi.selectNodeList(entityNode, ONLINE_URL);
      
                if (urlNodeList != null && urlNodeList.getLength() > 0) {
                  String url = urlNodeList.item(0).getTextContent();
                  logger.debug("Changing data URL from:\n  " + url + "\nto:\n  " + entryValue);
                  urlNodeList.item(0).setTextContent(entryValue);
                }
              }
            }
          }
        }
      }
    }
  }
    

  /*
   * Get the value of the @system attribute
   */
  private void setSystemAttribute(Document levelZeroEMLDocument) {
    Element rootElement = levelZeroEMLDocument.getDocumentElement();
    rootElement.setAttribute("system", LEVEL_ONE_SYSTEM_ATTRIBUTE);
  }

}
