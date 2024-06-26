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

package edu.lternet.pasta.common.eml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.function.Consumer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.eml.Entity.EntityType;


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
  public static final String CREATOR_PATH = "//eml/dataset/creator";
  public static final String ENTITY_PATH_PARENT = "//dataset/";
  public static final String PUB_DATE_PATH = "//eml/dataset/pubDate";
  public static final String TEMPORAL_COVERAGE_PATH = "//eml/dataset/coverage/temporalCoverage";
  public static final String SINGLE_DATE_TIME_PATH = "//eml/dataset/coverage/temporalCoverage/singleDateTime/calendarDate";
  public static final String BEGIN_DATE_PATH = "//eml/dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate";
  public static final String END_DATE_PATH = "//eml/dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate";
  public static final String GEOGRAPHIC_COVERAGE_PATH = "//eml/dataset/coverage/geographicCoverage";
  public static final String TITLE_PATH = "//dataset/title";
  public static final String ABSTRACT_PATH = "//dataset/abstract";
  public static final String INTELLECTUAL_RIGHTS_PATH = "//dataset/intellectualRights";
  public static final String DATA_SOURCE_PATH = "//methods/methodStep/dataSource";
  public static final String DATA_SOURCE_URL_PATH = "distribution/online/url";
  public static final String DATA_SOURCE_TITLE_PATH = "title";
  public static final String FUNDING_PATH = "//dataset/project/funding";
  public static final String METHODS_PATH = "//dataset/methods";
  public static final String KEYWORD_PATH = "//keyword";
  public static final String TAXONOMIC_COVERAGE_PATH = "//dataset/coverage/taxonomicCoverage";
  public static final String GEOGRAPHIC_DESCRIPTION_PATH = "//dataset/coverage/geographicCoverage/geographicDescription";
  public static final String NAMED_TIME_SCALE_PATH = "//timeScaleName";
  public static final String OTHER_ENTITY = "otherEntity";
  public static final String TABLE_ENTITY = "dataTable";
  public static final String SPATIAL_RASTER_ENTITY = "spatialRaster";
  public static final String SPATIAL_VECTOR_ENTITY = "spatialVector";
  public static final String STORED_PROCEDURE_ENTITY = "storedProcedure";
  public static final String VIEW_ENTITY = "view";
  private static final String PACKAGE_ID_PATH = "//eml/@packageId";
  public static final String PROJECT_ABSTRACT_PATH = "//dataset/project/abstract";
  public static final String PROJECT_TITLE_PATH = "//dataset/project/title";
  public static final String RELATED_PROJECT_TITLE_PATH = "//dataset/project/relatedProject/title";
  
  private static final String ENTITY_NAME = "entityName";
  private static final String OBJECT_NAME = "physical/objectName";
  private static final String ONLINE_URL = "physical/distribution/online/url";
  private static final String OFFLINE_PATH = "physical/distribution/offline";
  
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
  boolean eagerEscape = true;
  
  
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
   * Sets the boolean value of eagerEscape to control whether certain metadata fields
   * should be XML-escaped as soon as they are parsed out of the EML. Currently the set of
   * fields that would be eager-escape (if set to true) are:
   *     title
   *     projectTitle
   *     relatedProjectTitle
   *     givenNames
   *     surName
   *     organizationName
   *     positionName
   *     
   * The last four in the above list are parsed by the ResponsibleParty class, but the
   * eager-escape behavior is still controlled in this class.
   * 
   * @param isEager  true if the parser should "eager escape" certain fields,
   *                 else false.
   */
  public void setEagerEscape(boolean isEager) {
	  this.eagerEscape = isEager;
  }
  
  
  /**
   * XML-encode the raw XML passed to this method, but only if eagerEscape is true,
   * otherwise just return the original string.
   * 
   * @param  rawXml  The raw XML string.
   * @return The XML-encoded string if eagerEscape is true, otherwise the
   *         original raw string.
   */
  public String xmlEncodeIfEager(String rawXml) {
	  String s = this.eagerEscape ? XmlUtility.xmlEncode(rawXml) : rawXml;
	  return s;
  }
  
  
  /**
   * Parses an EML document.
   * 
   * @param   xml          The XML string representation of the EML document
   * @return  dataPackage  a DataPackage object holding parsed values
   */
  public DataPackage parseDocument(String xml) {
    DataPackage dataPackage = null;
    
    if (xml != null) {
      try {
        InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
        dataPackage = parseDocument(inputStream);
      }
      catch (Exception e) {
        logger.error("Error parsing EML metacdata: " + e.getMessage());
      }
    }
    
    return dataPackage;
  }
  
  
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
        
        // Parse the title nodes
        NodeList titleNodeList = xpathapi.selectNodeList(document, TITLE_PATH);
        for (int i = 0; i < titleNodeList.getLength(); i++) {
          String title = titleNodeList.item(i).getTextContent();
          title = xmlEncodeIfEager(title);
          dataPackage.titles.add(title);
        }

        // Parse the project title nodes
        NodeList projectTitleNodeList = xpathapi.selectNodeList(document, PROJECT_TITLE_PATH);
        for (int i = 0; i < projectTitleNodeList.getLength(); i++) {
          String projectTitle = projectTitleNodeList.item(i).getTextContent();
          projectTitle = xmlEncodeIfEager(projectTitle);
          dataPackage.projectTitles.add(projectTitle);
        }

        // Parse the related project title nodes
        NodeList relatedProjectTitleNodeList = xpathapi.selectNodeList(document, RELATED_PROJECT_TITLE_PATH);
        for (int i = 0; i < relatedProjectTitleNodeList.getLength(); i++) {
          String relatedProjectTitle = relatedProjectTitleNodeList.item(i).getTextContent();
          relatedProjectTitle = xmlEncodeIfEager(relatedProjectTitle);
          dataPackage.relatedProjectTitles.add(relatedProjectTitle);
        }

        // Parse the abstract text
        Node abstractNode = xpathapi.selectSingleNode(document, ABSTRACT_PATH);
        if (abstractNode != null) {
          String abstractText = abstractNode.getTextContent().trim();
          this.dataPackage.setAbstractText(abstractText);
        }

        // Parse the project abstract text
        Node projectAbstractNode = xpathapi.selectSingleNode(document, PROJECT_ABSTRACT_PATH);
        if (projectAbstractNode != null) {
          String projectAbstractText = projectAbstractNode.getTextContent().trim();
          this.dataPackage.setProjectAbstractText(projectAbstractText);
        }

        // Parse the intellectualRights to determine whether the element node is present
        Node intellectualRightsNode = xpathapi.selectSingleNode(document, INTELLECTUAL_RIGHTS_PATH);
        if (intellectualRightsNode != null && intellectualRightsNode.hasChildNodes()) {
          String intellectualRightsText = intellectualRightsNode.getTextContent().trim();
          this.dataPackage.setIntellectualRightsText(intellectualRightsText);
          this.dataPackage.setIntellectualRights(true);
        }

        // Parse the methods text
        Node methodsNode = xpathapi.selectSingleNode(document, METHODS_PATH);
        if (methodsNode != null) {
          String methodsText = methodsNode.getTextContent().trim();
          this.dataPackage.setMethodsText(methodsText);
        }

        // Parse the funding text
        Node fundingNode = xpathapi.selectSingleNode(document, FUNDING_PATH);
        if (fundingNode != null) {
          String fundingText = fundingNode.getTextContent().trim();
          this.dataPackage.setFundingText(fundingText);
        }

        // Parse the geographic coverage nodes
        NodeList geoNodeList = xpathapi.selectNodeList(document, GEOGRAPHIC_COVERAGE_PATH);
        for (int i = 0; i < geoNodeList.getLength(); i++) {
            Node geoNode = geoNodeList.item(i);
            NodeList geoChildNodes = geoNode.getChildNodes();
            for (int j = 0; j < geoChildNodes.getLength(); j++) {
            	Node geoChildNode = geoChildNodes.item(j);
            	if (geoChildNode.getNodeName().equals("boundingCoordinates")) {
            		String north = null;
            		String south = null;
            		String east = null;
            		String west = null;

            		Node northNode = xpathapi.selectSingleNode(geoChildNode, "northBoundingCoordinate");
                    if (northNode != null) {
                      north = northNode.getTextContent().trim();
                    }

                    Node southNode = xpathapi.selectSingleNode(geoChildNode, "southBoundingCoordinate");
                    if (southNode != null) {
                      south = southNode.getTextContent().trim();
                    }

                    Node eastNode = xpathapi.selectSingleNode(geoChildNode, "eastBoundingCoordinate");
                    if (eastNode != null) {
                      east = eastNode.getTextContent().trim();
                    }

                    Node westNode = xpathapi.selectSingleNode(geoChildNode, "westBoundingCoordinate");
                    if (westNode != null) {
                      west = westNode.getTextContent().trim();
                    }
                    
                    this.dataPackage.addBoundingCoordinates(north, south, east, west);
            	}
            }      
        }

        // Parse geographicDescription node
        Node geographicDescriptionNode = xpathapi.selectSingleNode(document, GEOGRAPHIC_DESCRIPTION_PATH);
        if (geographicDescriptionNode != null) {
          String geographicDescriptionText = geographicDescriptionNode.getTextContent().trim();
          this.dataPackage.setGeographicDescriptionText(geographicDescriptionText);
        }

        // Parse the taxonomic coverage node
        Node taxonomicCoverageNode = xpathapi.selectSingleNode(document, TAXONOMIC_COVERAGE_PATH);
        if (taxonomicCoverageNode != null) {
          String taxonomicCoverageText = taxonomicCoverageNode.getTextContent().trim();
          this.dataPackage.setTaxonomicCoverageText(taxonomicCoverageText);
        }

        // Parse the creator nodes
        NodeList creatorNodeList = xpathapi.selectNodeList(document, CREATOR_PATH);
        if (creatorNodeList != null) {
          for (int i =0; i < creatorNodeList.getLength(); i++) {
            Node creatorNode = creatorNodeList.item(i);
            ResponsibleParty rp = new ResponsibleParty(this, "creator");
            parseResponsibleParty(creatorNode, rp);
            dataPackage.addCreator(rp);
          }
        }

        // Parse the dataSource URLs
        NodeList dataSourceNodeList = xpathapi.selectNodeList(document, DATA_SOURCE_PATH);
        if (dataSourceNodeList != null) {
          for (int i =0; i < dataSourceNodeList.getLength(); i++) {
        	  String title = "";
        	  String url = "";
            Node dataSourceNode = dataSourceNodeList.item(i);
            NodeList dataSourceChildNodes = dataSourceNode.getChildNodes();
            for (int j = 0; j < dataSourceChildNodes.getLength(); j++) {
            	Node dataSourceChildNode = dataSourceChildNodes.item(j);
            	if (dataSourceChildNode.getNodeName().equals("title")) {
            		title = dataSourceChildNode.getTextContent();
            	}
            	else if (dataSourceChildNode.getNodeName().equals("distribution")) {
            		Node onlineNode = xpathapi.selectSingleNode(dataSourceChildNode, "online");
            		if (onlineNode != null) {
            			Node urlNode = xpathapi.selectSingleNode(onlineNode, "url");
            			if (urlNode != null) {
            				url = urlNode.getTextContent();
            			}
            		}

            	}
            }
            dataPackage.addDataSource(null, title, url);
          }
        }

        // Parse the keyword nodes
        NodeList keywordNodeList = xpathapi.selectNodeList(document, KEYWORD_PATH);
        if (keywordNodeList != null) {
          for (int i =0; i < keywordNodeList.getLength(); i++) {
            Node keywordNode = keywordNodeList.item(i);
            String keyword = keywordNode.getTextContent();
            dataPackage.addKeyword(keyword);
          }
        }

        // Parse temporal coverage nodes
        NodeList temporalNodeList = xpathapi.selectNodeList(document, TEMPORAL_COVERAGE_PATH);
        for (int i = 0; i < temporalNodeList.getLength(); i++) {
        	TemporalCoverage temporalCoverage = new TemporalCoverage();
            Node temporalNode = temporalNodeList.item(i);
            NodeList temporalChildNodes = temporalNode.getChildNodes();
            for (int j = 0; j < temporalChildNodes.getLength(); j++) {
            	Node temporalChildNode = temporalChildNodes.item(j);
            	if (temporalChildNode.getNodeName().equals("rangeOfDates")) {

            		Node beginDateNode = xpathapi.selectSingleNode(temporalChildNode, "beginDate");
                    if (beginDateNode != null) {
                    	Node calendarDateNode = xpathapi.selectSingleNode(beginDateNode, "calendarDate");
                    	if (calendarDateNode != null) {
                    		String calendarDate = calendarDateNode.getTextContent().trim();
                    		temporalCoverage.setBeginDate(calendarDate);
                    	}
                    	else {
                        	Node alternativeTimeScaleNode = xpathapi.selectSingleNode(beginDateNode, "alternativeTimeScale");
                        	if (alternativeTimeScaleNode != null) {
                        		temporalCoverage.addAlternativeTimeScale(xpathapi, alternativeTimeScaleNode);
                        	}
                    	}
                    }

            		Node endDateNode = xpathapi.selectSingleNode(temporalChildNode, "endDate");
                    if (endDateNode != null) {
                    	Node calendarDateNode = xpathapi.selectSingleNode(endDateNode, "calendarDate");
                    	if (calendarDateNode != null) {
                    		String calendarDate = calendarDateNode.getTextContent().trim();
                    		temporalCoverage.setEndDate(calendarDate);
                    	}
                    	else {
                        	Node alternativeTimeScaleNode = xpathapi.selectSingleNode(endDateNode, "alternativeTimeScale");
                        	if (alternativeTimeScaleNode != null) {
                        		temporalCoverage.addAlternativeTimeScale(xpathapi, alternativeTimeScaleNode);
                        	}
                    	}
                    }
            	}
            	else if (temporalChildNode.getNodeName().equals("singleDateTime")) {
            		Node singleDateTimeNode = temporalChildNode;
                    Node calendarDateNode = xpathapi.selectSingleNode(singleDateTimeNode, "calendarDate");
                    if (calendarDateNode != null) {
                    	String calendarDate = calendarDateNode.getTextContent().trim();
                    	temporalCoverage.addSingleDateTime(calendarDate);
                    }
                    else {
                        Node alternativeTimeScaleNode = xpathapi.selectSingleNode(singleDateTimeNode, "alternativeTimeScale");
                        if (alternativeTimeScaleNode != null) {
                        	temporalCoverage.addAlternativeTimeScale(xpathapi, alternativeTimeScaleNode);
                    	}
                    }
            	}
            }
            
            dataPackage.addTemporalCoverage(temporalCoverage);
        }

        // Parse the pubDate node
        Node pubDateNode = xpathapi.selectSingleNode(document, PUB_DATE_PATH);
        if (pubDateNode != null) {
          String pubDate = pubDateNode.getTextContent();
          this.dataPackage.setPubDate(pubDate);
        }

       for (int j = 0; j < ENTITY_TYPES.length; j++) {
          String elementName = ENTITY_TYPES[j];
          String elementPath = ENTITY_PATH_PARENT + elementName;
          EntityType entityType = Entity.entityTypeFromElementName(elementName);
      
          // Parse the entity name
          NodeList entityNodeList = 
                                xpathapi.selectNodeList(document, elementPath);
      
          if (entityNodeList != null) {
            for (int i = 0; i < entityNodeList.getLength(); i++) {
              Entity entity = new Entity();
              entity.setEntityType(entityType);
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
          
              // check for physical/distribution/offline
              NodeList offlineNodeList = xpathapi.selectNodeList(
                                             entityNode,
                                             OFFLINE_PATH
                                 );
          
              if (offlineNodeList != null && offlineNodeList.getLength() > 0) {
                String offlineText = offlineNodeList.item(0).getTextContent();
                entity.setOfflineText(offlineText);
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


  // parseResponsibleParty

  public void parseResponsibleParty(Node node, ResponsibleParty rp)
  {
    if (node instanceof Element) {
      Element el = (Element) node;
      String tagName = el.getTagName();
      if (tagName.equals("contact") ||
          tagName.equals("creator") ||
          tagName.equals("metadataProvider")) {
        NodeList nodeList = el.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node childNode = nodeList.item(i);
          if (childNode instanceof Element) {
            _parseElements(rp, (Element) childNode);
          }
        }
      }
    }
  }

  private void _parseElements(ResponsibleParty rp, Element el)
  {
    switch (el.getTagName()) {
      case "individualName":
        _parseIndividualName(rp, el);
        break;
      case "organizationName":
        _set(rp::setOrganizationName, el);
        break;
      case "positionName":
        _set(rp::setPositionName, el);
        break;
      case "address":
        _parseAddress(rp, el);
        break;
      case "phone":
        String attr = el.getAttribute("phonetype");
        if (!(attr.equals("facsimile") || attr.equals("fax"))) {
          _set(rp::setPhone, el);
        }
        break;
      case "electronicMailAddress":
        _set(rp::setElectronicMailAddress, el);
        break;
      case "onlineUrl":
        _set(rp::setOnlineUrl, el);
        break;
      case "userId":
        _set(rp::setUserId, el);
        rp.setUserIdDirectory(el.getAttribute("directory"));
        break;
    }
  }

  private void _parseAddress(ResponsibleParty rp, Element addressEl)
  {
    NodeList nodeList = addressEl.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        switch (el.getTagName()) {
          case "deliveryPoint":
            _set(rp::addDeliveryPoint, el);
            break;
          case "city":
            _set(rp::setCity, el);
            break;
          case "administrativeArea":
            _set(rp::setAdministrativeArea, el);
            break;
          case "postalCode":
            _set(rp::setPostalCode, el);
            break;
          case "country":
            _set(rp::setCountry, el);
            break;
        }
      }
    }
  }

  private void _parseIndividualName(ResponsibleParty rp, Element nameEl)
  {
    NodeList nodeList = nameEl.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node instanceof Element) {
        Element el = (Element) node;
        switch (el.getTagName()) {
          case "salutation":
            _set(rp::setSalutation, el);
            break;
          case "givenName":
            _set(rp::addGivenName, el);
            break;
          case "surName":
            _set(rp::setSurName, el);
            break;
        }
      }
    }
  }

  // Get the text value of the last child text node of the given element and call the
  // given setter with it, causing the value to be set in the bound ResponsibleParty
  // object. If there are no child text nodes, the setter is not called.
  private void _set(Consumer<String> rp_setter, Element el)
  {
    NodeList nodeList = el.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node instanceof Text) {
        Text textNode = (Text) node;
        String nodeValue = textNode.getNodeValue();
        logger.debug(String.format("%s: %s", rp_setter, nodeValue));
        rp_setter.accept(nodeValue);
      }
    }
  }
}
