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

import java.io.File;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.common.XmlUtility;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * Constructs Level-1 metadata from Level-0 metadata
 * 
 */
public final class LevelOneEMLFactory {

  /*
   * Class variables and constants
   */
  private static final Logger logger = Logger.getLogger(LevelOneEMLFactory.class);

  private static final String ABSTRACT_PATH = "//dataset/abstract";
  private static final String ACCESS_ALLOW_PATH = "//eml/access/allow";
  private static final String ACCESS_ALLOW_PRINCIPAL_PATH = "//eml/access/allow/principal";
  private static final String ACCESS_PATH = "//access";
  private static final String ACKNOWLEDGEMENTS_PATH = "//dataset/acknowledgements";
  private static final String ADDITIONAL_INFO_PATH = "//dataset/additionalInfo";
  private static final String ANNOTATION_PATH = "//dataset/annotation";
  private static final String ASSOCIATED_PARTY_PATH = "//dataset/associatedParty";
  private static final String CONTACT_PATH = "//dataset/contact";
  private static final String COVERAGE_PATH = "//dataset/coverage";
  private static final String CREATOR_PATH = "//dataset/creator";
  private static final String DATASET_PATH = "//eml/dataset";
  private static final String DATA_TABLE_PATH = "//dataset/dataTable";
  private static final String DISTRIBUTION_PATH = "//dataset/distribution";
  private static final String ENTITY_NAME = "entityName";
  private static final String ENTITY_PATH_PARENT = "//dataset/";
  private static final String GETTING_STARTED_PATH = "//dataset/gettingStarted";
  private static final String INTRODUCTION_PATH = "//dataset/introduction";
  private static final String KEYWORD_SET_PATH = "//dataset/keywordSet";
  private static final String LANGUAGE_PATH = "//dataset/language";
  private static final String LEVEL_ONE_AUTH_SYSTEM_ATTRIBUTE = "https://pasta.edirepository.org/authentication";
  private static final String LICENSED_PATH = "//dataset/licensed";
  private static final String MAINTENANCE_PATH = "//dataset/maintenance";
  private static final String METADATA_PROVIDER_PATH = "//dataset/metadataProvider";
  private static final String METHODS_PATH = "//dataset/methods";
  private static final String OBJECT_NAME = "physical/objectName";
  private static final String ONLINE_URL = "physical/distribution/online/url";
  private static final String OTHER_ENTITY = "otherEntity";
  private static final String OTHER_ENTITY_PATH = "//dataset/otherEntity";
  private static final String PROJECT_PATH = "//dataset/project";
  private static final String PUB_DATE_PATH = "//dataset/pubDate";
  private static final String PUBLISHER_PATH = "//dataset/publisher";
  private static final String PUB_PLACE_PATH = "//dataset/pubPlace";
  private static final String PURPOSE_PATH = "//dataset/purpose";
  private static final String SERIES_PATH = "//dataset/series";
  private static final String SHORTNAME_PATH = "//dataset/shortName";
  private static final String SPATIAL_RASTER_ENTITY = "spatialRaster";
  private static final String SPATIAL_RASTER_PATH = "//dataset/spatialRaster";
  private static final String SPATIAL_VECTOR_ENTITY = "spatialVector";
  private static final String SPATIAL_VECTOR_PATH = "//dataset/spatialVector";
  private static final String STORED_PROCEDURE_ENTITY = "storedProcedure";
  private static final String STORED_PROCEDURE_PATH = "//dataset/storedProcedure";
  private static final String SYSTEM_ATTRIBUTE_PATH = "//@system";
  private static final String TABLE_ENTITY = "dataTable";
  private static final String TITLE_PATH = "//dataset/title";
  private static final String VIEW_ENTITY = "view";
  private static final String VIEW_PATH = "//dataset/view";

  public static final String INTELLECTUAL_RIGHTS_PATH = "//dataset/intellectualRights";
  public static final String LEVEL_ONE_SYSTEM_ATTRIBUTE = "https://pasta.edirepository.org";
  private static final String DOI_SYSTEM_VALUE = "https://doi.org";

  private static final String INTELLECTUAL_RIGHTS_CC_BY = 
    "This information is released under the Creative Commons license - Attribution - CC BY (https://creativecommons.org/licenses/by/4.0/). The consumer of these data (\"Data User\" herein) is required to cite it appropriately in any publication that results from its use. The Data User should realize that these data may be actively used by others for ongoing research and that coordination may be necessary to prevent duplicate publication. The Data User is urged to contact the authors of these data if any questions about methodology or results occur. Where appropriate, the Data User is encouraged to consider collaboration or co-authorship with the authors. The Data User should realize that misinterpretation of data may occur if used out of context of the original study. While substantial efforts are made to ensure the accuracy of data and associated documentation, complete accuracy of data sets cannot be guaranteed. All data are made available \"as is.\" The Data User should be aware, however, that data are updated periodically and it is the responsibility of the Data User to check for new versions of the data. The data authors and the repository where these data were obtained shall not be liable for damages resulting from any use or misinterpretation of the data. Thank you.";

  private static final String INTELLECTUAL_RIGHTS_CC0 = 
    "This data package is released to the \"public domain\" under Creative Commons CC0 1.0 \"No Rights Reserved\" (see: https://creativecommons.org/publicdomain/zero/1.0/). It is considered professional etiquette to provide attribution of the original work if this data package is shared in whole or by individual components. A generic citation is provided for this data package on the website https://portal.edirepository.org (herein \"website\") in the summary metadata page. Communication (and collaboration) with the creators of this data package is recommended to prevent duplicate research or publication. This data package (and its components) is made available \"as is\" and with no warranty of accuracy or fitness for use. The creators of this data package and the website shall not be liable for any damages resulting from misinterpretation or misuse of the data package or its components. Periodic updates of this data package may be available from the website. Thank you.";
  
  private final static String EDI =  "uid=EDI,o=EDI,dc=edirepository,dc=org";
  private final static String EDI2 = "uid=EDI,o=edi,dc=edirepository,dc=org";
  private final static String SPU = "SPU_EDI_Data_Author";

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
    
    /* Only append the Level-1 contact element if the document doesn't already have one
    if (!hasLevelOneContact(levelZeroEMLDocument)) {
    	appendContact(levelZeroEMLDocument);
    }*/
    
    modifyDataURLs(levelZeroEMLDocument, entityHashMap);
    modifyAccessElementAttributes(levelZeroEMLDocument);
    modifyPubDate(levelZeroEMLDocument);
    modifyPublisher(levelZeroEMLDocument);
    modifyPubPlace(levelZeroEMLDocument);
    checkIntellectualRights(levelZeroEMLDocument);
    checkEdiPrincipal(levelZeroEMLDocument);
	checkSpuPrincipal(levelZeroEMLDocument);

    return levelZeroEMLDocument;
  }
  
  
	/**
	 * Enhance a Level-1 EML document with additional information such as the
	 * DOI identifier.
	 * 
	 * @param levelOneEMLDocument
	 *            the original Level-1 EML Document
	 * @param alternateID
	 * 			  alternate identifier (e.g., DOI)
	 * @param attributeValue
	 * 			  value of alternate identifier attribute
	 * @return the enhanced Level-1 EML Document, a Document object
	 * @throws TransformerException
	 */
	public Document enhance(Document levelOneEMLDocument, String alternateID, String attributeValue)
			throws TransformerException {
		if (levelOneEMLDocument == null) {
			throw new IllegalArgumentException(
					"null Document object passed to LevelOneEMLFactory.enhance() method");
		}

		addAlternateIdentifier(levelOneEMLDocument, alternateID, attributeValue);
		if (attributeValue.equals(DOI_SYSTEM_VALUE)) {
			addDatasetDistribution(levelOneEMLDocument, alternateID.replaceFirst("doi:", ""));
		}

		return levelOneEMLDocument;
	}
  
	
    /**
     * Check to see whether the Level-0 EML already has an intellectualRights
     * or a licensed element and if not, add a default intellectualRights element.
     * 
     * @param emlDocument  the Level-0 EML document to be checked
     */
  	void checkIntellectualRights(Document emlDocument)
        throws TransformerException {
  		boolean hasIntellectualRights = hasIntellectualRights(emlDocument);
  		boolean hasLicensed = hasLicensed(emlDocument);
  		
  		if (hasIntellectualRights) {
  			logger.info("An intellectualRights element was found in the Level-0 EML.");
  		}
  		else if (hasLicensed) {
  			logger.info("A licensed element was found in the Level-0 EML.");
		}
  		else {
  			addDefaultIntellectualRights(emlDocument);
  		}
  	}

  	
    /**
     * Check to see whether the Level-0 EML with scope of "EDI" already has an
     * principal element for uid=EDI..., and if not, add one.
     * 
     * @param emlDocument  the Level-0 EML document to be checked
     */
  	void checkEdiPrincipal(Document emlDocument)
  			throws TransformerException {
		String packageId = getPackageIdAttribute(emlDocument);
		if (packageId != null) {
			String scope = packageId.split("\\.")[0];
			if (scope.equals("edi")) {
				boolean hasEdiPrincipal = hasPrincipal(emlDocument, EDI);
				boolean hasEdi2Principal = hasPrincipal(emlDocument, EDI2);
  		
				if (hasEdiPrincipal || hasEdi2Principal) {
					logger.info("An access/allow/principal element for EDI was found in the Level-0 EML.");
				}
				else {
					addGodPrincipal(emlDocument, EDI);
				}
			}
		}
  	}


	/**
	 * Check to see whether the Level-0 EML with scope of "cos-spu" already has an
	 * principal element for uid=EDI..., and if not, add one.
	 *
	 * @param emlDocument  the Level-0 EML document to be checked
	 */
	void checkSpuPrincipal(Document emlDocument)
			throws TransformerException {
		String packageId = getPackageIdAttribute(emlDocument);
		if (packageId != null) {
			String scope = packageId.split("\\.")[0];
			if (scope.equals("cos-spu")) {
				boolean hasSpuPrincipal = hasPrincipal(emlDocument, SPU);

				if (hasSpuPrincipal) {
					logger.info("An access/allow/principal element for SPU was found in the Level-0 EML.");
				}
				else {
					addGodPrincipal(emlDocument, SPU);
				}
			}
		}
	}



	/**
	 * Count the number of elements at a given path. This method is useful
	 * for JUnit tests.
	 * 
	 * @param   emlDocument  the EML Document
	 * @param   elementPath  the path being tested, e.g. //dataset/contact
	 * @return  elementCount  the number of elements found
	 */
	public int elementCount(Document emlDocument, String elementPath)
	          throws TransformerException {
		int elementCount = -1;
	    CachedXPathAPI xpathapi = new CachedXPathAPI();

	    // Parse the access elements
		NodeList elementNodes = xpathapi.selectNodeList(emlDocument, elementPath);
		if (elementNodes != null) {
			elementCount = elementNodes.getLength();
		}

		return elementCount;
	}


	/**
	 * Boolean to determine whether this data package contains an
	 * element at the specified element path.
	 * 
	 * @param   emlDocument  the EML Document
	 * @param   elementPath  the path being tested, e.g. //dataset/contact
	 * @return  true if this data package has an intellectualRights element,
	 *          else false
	 */
	private boolean hasElement(Document emlDocument, String elementPath)
	          throws TransformerException {
		boolean hasElement = false;
	    CachedXPathAPI xpathapi = new CachedXPathAPI();

	    // Parse the access elements
		NodeList elementNodes = xpathapi.selectNodeList(emlDocument, elementPath);
		hasElement = (elementNodes != null) && (elementNodes.getLength() > 0);

		return hasElement;
	}


	/**
	 * Boolean to determine whether this data package contains an
	 * intellectualRights element.
	 * 
	 * @param   emlDocument  the EML Document
	 * @return  true if this data package has an intellectualRights element,
	 *          else false
	 */
	private boolean hasIntellectualRights(Document emlDocument)
	          throws TransformerException {
		return hasElement(emlDocument, INTELLECTUAL_RIGHTS_PATH);
	}


	/**
	 * Boolean to determine whether this data package contains a
	 * Level-1 contact element. If it does, we don't want to add
	 * a duplicate element.
	 * 
     * @param   emlDocument  the Level-0 EML Document
	 * @return  true if this data package has a Level-1 contact element,
	 *          else false
	 */
	public boolean hasLevelOneContact(Document emlDocument)
	          throws TransformerException {
		boolean hasLevelOneContact = false;
	    CachedXPathAPI xpathapi = new CachedXPathAPI();

	    // Parse the access elements
		NodeList datasetContacts = xpathapi.selectNodeList(emlDocument, CONTACT_PATH);
		if (datasetContacts != null) {
			for (int i = 0; i < datasetContacts.getLength(); i++) {
				boolean hasPositionName = false;
				boolean hasOrganizationName = false;
				Element contactElement = (Element) datasetContacts.item(i);

				NodeList positionNames = contactElement.getElementsByTagName("positionName");
				if (positionNames != null) {
					for (int j = 0; j < positionNames.getLength(); j++) {
						Element positionNameElement = (Element) positionNames.item(j);
						String positionName = positionNameElement.getTextContent();
						if ("Information Manager".equals(positionName)) {
							hasPositionName = true;
						}
					}
				}
	
				NodeList organizationNames = contactElement.getElementsByTagName("organizationName");
				if (organizationNames != null) {
					for (int j = 0; j < organizationNames.getLength(); j++) {
						Element organizationNameElement = (Element) organizationNames.item(j);
						String organizationName = organizationNameElement.getTextContent();
						if ("Environmental Data Initiative".equals(organizationName)) {
							hasOrganizationName = true;
						}
					}
				}
				
				hasLevelOneContact = hasLevelOneContact || (hasPositionName && hasOrganizationName);
			}
		}

		return hasLevelOneContact;
	}


	/**
	 * Boolean to determine whether this data package contains an access 
	 * control element for the given principal.
	 * 
     * @param   emlDocument  the Level-0 EML Document
	 * @param   principal     the principal to be checked
	 * @return  true if this data package has a //access/allow/principal element for principal
	 */
	public boolean hasPrincipal(Document emlDocument, String principal)
	          throws TransformerException {
		boolean hasPrincipal = false;
	    CachedXPathAPI xpathapi = new CachedXPathAPI();

	    // Parse the //eml/access/allow/principal elements
		NodeList principalNodeList = xpathapi.selectNodeList(emlDocument, ACCESS_ALLOW_PRINCIPAL_PATH);
		if (principalNodeList != null) {
			for (int i = 0; i < principalNodeList.getLength(); i++) {
				Element principalElement = (Element) principalNodeList.item(i);
				String principalText = principalElement.getTextContent();

				if (principal.equals(principalText)) {
					hasPrincipal = true;
				}
			}
		}

		return hasPrincipal;
	}


	/**
	 * Boolean to determine whether this data package contains an
	 * licensed element.
	 *
	 * @param   emlDocument  the EML Document
	 * @return  true if this data package has a licensed element,
	 *          else false
	 */
	private boolean hasLicensed(Document emlDocument)
	          throws TransformerException {
		return hasElement(emlDocument, LICENSED_PATH);
	}


  /*
   * Append a Level-1 contact element to document containing contact
   * info for the Environmental Data Initiative (EDI)
   */
  private void appendContact(Document doc) {
    Element lnoContact = doc.createElement("contact");
    Element positionName = doc.createElement("positionName");
    positionName.appendChild(doc.createTextNode("Information Manager"));
    Element organizationName = doc.createElement("organizationName");
    organizationName.appendChild(doc.createTextNode("Environmental Data Initiative"));
    Element address = doc.createElement("address");
    Element deliveryPoint1 = doc.createElement("deliveryPoint");
    deliveryPoint1.appendChild(doc.createTextNode("Center for Limnology"));
    Element deliveryPoint2 = doc.createElement("deliveryPoint");
    deliveryPoint2.appendChild(doc.createTextNode("University of Wisconsin"));
    Element city = doc.createElement("city");
    city.appendChild(doc.createTextNode("Madison"));    
    Element administrativeArea = doc.createElement("administrativeArea");
    administrativeArea.appendChild(doc.createTextNode("WI"));    
    Element postalCode = doc.createElement("postalCode");
    postalCode.appendChild(doc.createTextNode("53706"));    
    Element country = doc.createElement("country");
    country.appendChild(doc.createTextNode("USA"));   
    address.appendChild(deliveryPoint1);
    address.appendChild(deliveryPoint2);
    address.appendChild(city);
    address.appendChild(administrativeArea);
    address.appendChild(postalCode);
    address.appendChild(country);
    //Element phone1 = doc.createElement("phone");
    //phone1.setAttribute("phonetype", "voice");
    //phone1.appendChild(doc.createTextNode("505 277-2535"));
    //Element phone2 = doc.createElement("phone");
    //phone2.setAttribute("phonetype", "fax");
    //phone2.appendChild(doc.createTextNode("505 277-2541"));
    Element electronicMailAddress = doc.createElement("electronicMailAddress");
    electronicMailAddress.appendChild(doc.createTextNode("info@edirepository.org"));
    Element onlineUrl = doc.createElement("onlineUrl");
    onlineUrl.appendChild(doc.createTextNode("http://edirepository.org"));
    lnoContact.appendChild(positionName);
    lnoContact.appendChild(organizationName);
    lnoContact.appendChild(address);
    //lnoContact.appendChild(phone1);
    //lnoContact.appendChild(phone2);
    lnoContact.appendChild(electronicMailAddress);
    lnoContact.appendChild(onlineUrl);
    NodeList contacts = getContacts(doc);  
    Node datasetNode = getDatasetNode(doc);
    datasetNode.insertBefore(lnoContact, contacts.item(0));
  }


  /*
   * Append a Level-1 intellectualRights element to document.
   */
	private void addDefaultIntellectualRights(Document doc)
			throws TransformerException {
		Element intellectualRightsElement = doc.createElement("intellectualRights");
		Element paraElement = doc.createElement("para");
		String packageId = getPackageIdAttribute(doc);
		boolean isLTER = (packageId != null) && packageId.startsWith("knb-lter");
		String intellectualRightsText = isLTER ? INTELLECTUAL_RIGHTS_CC_BY : INTELLECTUAL_RIGHTS_CC0;
		paraElement.appendChild(doc.createTextNode(intellectualRightsText));
		intellectualRightsElement.appendChild(paraElement);
		Node datasetNode = getDatasetNode(doc);
		
		/* 
		 * Determine where to insert the intellectualRights element. This
		 * depends on the presence of nearby optional elements.
		 */
		String insertBefore = null;
		if (hasElement(doc, LICENSED_PATH)) {
			insertBefore = LICENSED_PATH;
		}
		else if (hasElement(doc, DISTRIBUTION_PATH)) {
			insertBefore = DISTRIBUTION_PATH;
		}
		else if (hasElement(doc, COVERAGE_PATH)) {
			insertBefore = COVERAGE_PATH;
		}
		else if (hasElement(doc, ANNOTATION_PATH)) {
			insertBefore = ANNOTATION_PATH;
		}
		else if (hasElement(doc, PURPOSE_PATH)) {
			insertBefore = PURPOSE_PATH;
		}
		else if (hasElement(doc, INTRODUCTION_PATH)) {
			insertBefore = INTRODUCTION_PATH;
		}
		else if (hasElement(doc, GETTING_STARTED_PATH)) {
			insertBefore = GETTING_STARTED_PATH;
		}
		else if (hasElement(doc, ACKNOWLEDGEMENTS_PATH)) {
			insertBefore = ACKNOWLEDGEMENTS_PATH;
		}
		else if (hasElement(doc, MAINTENANCE_PATH)) {
			insertBefore = MAINTENANCE_PATH;
		}
		else {
			insertBefore = CONTACT_PATH;
		}
		
		NodeList insertNodeList = getElementNodeList(doc, insertBefore);
		Node insertNode = insertNodeList.item(0);		
		datasetNode.insertBefore(intellectualRightsElement, insertNode);
	}


  /*
   * Add Dataset-level distribution with link to DOI landing page
   */
  private void addDatasetDistribution(Document doc, String doi) throws TransformerException {
		String url = String.format("%s/%s", DOI_SYSTEM_VALUE, doi);

		Element distributionElement = doc.createElement("distribution");
		distributionElement.setAttribute("id", doi);
		distributionElement.setAttribute("system", LEVEL_ONE_SYSTEM_ATTRIBUTE);
		distributionElement.setAttribute("scope", "system");
		Element onlineElement = doc.createElement("online");
		Element urlElement = doc.createElement("url");
		urlElement.setAttribute("function", "information");

		urlElement.appendChild(doc.createTextNode(url));
		onlineElement.appendChild(urlElement);
		distributionElement.appendChild(onlineElement);

		String insertBefore;

		if (hasElement(doc, COVERAGE_PATH)) {
			insertBefore = COVERAGE_PATH;
		}
		else if (hasElement(doc, ANNOTATION_PATH)) {
			insertBefore = ANNOTATION_PATH;
		}
		else if (hasElement(doc, PURPOSE_PATH)) {
			insertBefore = PURPOSE_PATH;
		}
		else if (hasElement(doc, INTRODUCTION_PATH)) {
			insertBefore = INTRODUCTION_PATH;
		}
		else if (hasElement(doc, GETTING_STARTED_PATH)) {
			insertBefore = GETTING_STARTED_PATH;
		}
		else if (hasElement(doc, ACKNOWLEDGEMENTS_PATH)) {
			insertBefore = ACKNOWLEDGEMENTS_PATH;
		}
		else if (hasElement(doc, MAINTENANCE_PATH)) {
			insertBefore = MAINTENANCE_PATH;
		}
		else {
			insertBefore = CONTACT_PATH;
		}

		Node datasetNode = getDatasetNode(doc);
		NodeList insertNodeList = getElementNodeList(doc, insertBefore);
		Node insertNode = insertNodeList.item(0);
		datasetNode.insertBefore(distributionElement, insertNode);

  }


   /*
   * Add a Level-1 access/allow element with EDI principal to document.
   */
	private void addGodPrincipal(Document doc, String principal)
			throws TransformerException {
		Element allowElement = doc.createElement("allow");
		Element principalElement = doc.createElement("principal");
		principalElement.appendChild(doc.createTextNode(principal));
		allowElement.appendChild(principalElement);
		Element permissionElement = doc.createElement("permission");
		permissionElement.appendChild(doc.createTextNode("all"));
		allowElement.appendChild(permissionElement);

		/*
		 * Insert the access/allow element before an existing access/allow element.
		 */
		if (hasElement(doc, ACCESS_ALLOW_PATH)) {
			Node accessNode = getAccessNode(doc);
			if (accessNode != null) {
				String insertBefore = ACCESS_ALLOW_PATH;
				NodeList insertNodeList = getElementNodeList(doc, insertBefore);
				Node insertNode = insertNodeList.item(0);
				accessNode.insertBefore(allowElement, insertNode);
			}
		}
		else {
			Node emlNode = getEmlNode(doc);
			Element accessElement = doc.createElement("access");
		    //<access authSystem="https://pasta.edirepository.org/authentication" order="allowFirst" scope="document" system="https://pasta.edirepository.org">
			accessElement.setAttribute("authSystem", "https://pasta.edirepository.org/authentication");
			accessElement.setAttribute("order", "allowFirst");
			accessElement.setAttribute("scope", "document");
			accessElement.setAttribute("system", "https://pasta.edirepository.org");
			accessElement.appendChild(allowElement);
			String insertBefore = DATASET_PATH;
			NodeList insertNodeList = getElementNodeList(doc, insertBefore);
			Node insertNode = insertNodeList.item(0);
			emlNode.insertBefore(accessElement, insertNode);
		}
	}

	private void addPubDate(Document doc, String pubDate) throws TransformerException {
		String insertBefore = null;

		if (hasElement(doc, LANGUAGE_PATH)) {
			insertBefore = LANGUAGE_PATH;
		}
		else if (hasElement(doc, SERIES_PATH)) {
			insertBefore = SERIES_PATH;
		}
		else if (hasElement(doc, ABSTRACT_PATH)) {
			insertBefore = ABSTRACT_PATH;
		}
		else if (hasElement(doc, KEYWORD_SET_PATH)) {
			insertBefore = KEYWORD_SET_PATH;
		}
		else if (hasElement(doc, ADDITIONAL_INFO_PATH)) {
			insertBefore = ADDITIONAL_INFO_PATH;
		}
		else if (hasElement(doc, INTELLECTUAL_RIGHTS_PATH)) {
			insertBefore = INTELLECTUAL_RIGHTS_PATH;
		}
		else if (hasElement(doc, LICENSED_PATH)) {
			insertBefore = LICENSED_PATH;
		}
		else if (hasElement(doc, DISTRIBUTION_PATH)) {
			insertBefore = DISTRIBUTION_PATH;
		}
		else if (hasElement(doc, COVERAGE_PATH)) {
			insertBefore = COVERAGE_PATH;
		}
		else if (hasElement(doc, ANNOTATION_PATH)) {
			insertBefore = ANNOTATION_PATH;
		}
		else if (hasElement(doc, PURPOSE_PATH)) {
			insertBefore = PURPOSE_PATH;
		}
		else if (hasElement(doc, INTRODUCTION_PATH)) {
			insertBefore = INTRODUCTION_PATH;
		}
		else if (hasElement(doc, GETTING_STARTED_PATH)) {
			insertBefore = GETTING_STARTED_PATH;
		}
		else if (hasElement(doc, ACKNOWLEDGEMENTS_PATH)) {
			insertBefore = ACKNOWLEDGEMENTS_PATH;
		}
		else if (hasElement(doc, MAINTENANCE_PATH)) {
			insertBefore = MAINTENANCE_PATH;
		}
		else {
			insertBefore = CONTACT_PATH;
		}

		Element pubDateElement = doc.createElement("pubDate");
		pubDateElement.appendChild(doc.createTextNode(pubDate));
		Node datasetNode = getDatasetNode(doc);
		NodeList insertNodeList = getElementNodeList(doc, insertBefore);
		Node insertNode = insertNodeList.item(0);
		datasetNode.insertBefore(pubDateElement, insertNode);

	}

	private void addPubPlace(Document doc, String pubPlace)
		throws TransformerException {

		String insertBefore = null;

		if (hasElement(doc, METHODS_PATH)) {
			insertBefore = METHODS_PATH;
		}
		else if (hasElement(doc, PROJECT_PATH)) {
			insertBefore = PROJECT_PATH;
		}
		else if (hasElement(doc, DATA_TABLE_PATH)) {
			insertBefore = DATA_TABLE_PATH;
		}
		else if (hasElement(doc, SPATIAL_RASTER_PATH)) {
			insertBefore = SPATIAL_RASTER_PATH;
		}
		else if (hasElement(doc, SPATIAL_VECTOR_PATH)) {
			insertBefore = SPATIAL_VECTOR_PATH;
		}
		else if (hasElement(doc, STORED_PROCEDURE_PATH)) {
			insertBefore = STORED_PROCEDURE_PATH;
		}
		else if (hasElement(doc, VIEW_PATH)) {
			insertBefore = VIEW_PATH;
		}
		else if (hasElement(doc, OTHER_ENTITY_PATH)) {
			insertBefore = OTHER_ENTITY_PATH;
		}

		Element pubPlaceElement = doc.createElement("pubPlace");
		pubPlaceElement.appendChild(doc.createTextNode(pubPlace));
		Node datasetNode = getDatasetNode(doc);
		NodeList insertNodeList = getElementNodeList(doc, insertBefore);
		Node insertNode = insertNodeList.item(0);
		datasetNode.insertBefore(pubPlaceElement, insertNode);

	}

	private void addPublisher(Document doc)
		throws TransformerException {

		String insertBefore = null;

		if (hasElement(doc, PUB_PLACE_PATH)) {
			insertBefore = PUB_PLACE_PATH;
		}
		else if (hasElement(doc, METHODS_PATH)) {
			insertBefore = METHODS_PATH;
		}
		else if (hasElement(doc, PROJECT_PATH)) {
			insertBefore = PROJECT_PATH;
		}
		else if (hasElement(doc, DATA_TABLE_PATH)) {
			insertBefore = DATA_TABLE_PATH;
		}
		else if (hasElement(doc, SPATIAL_RASTER_PATH)) {
			insertBefore = SPATIAL_RASTER_PATH;
		}
		else if (hasElement(doc, SPATIAL_VECTOR_PATH)) {
			insertBefore = SPATIAL_VECTOR_PATH;
		}
		else if (hasElement(doc, STORED_PROCEDURE_PATH)) {
			insertBefore = STORED_PROCEDURE_PATH;
		}
		else if (hasElement(doc, VIEW_PATH)) {
			insertBefore = VIEW_PATH;
		}
		else if (hasElement(doc, OTHER_ENTITY_PATH)) {
			insertBefore = OTHER_ENTITY_PATH;
		}

		String orgName = "Environmental Data Initiative";
		String orgEmail = "info@edirepository.org";
		String orgOnlineUrl = "https://edirepository.org";
		String orgUserId = "0330j0z60";
		String orgUserIdAttributeName = "directory";
		String orgUserIdAttributeValue = "https://ror.org";

		Element publisherElement = doc.createElement("publisher");

		Element orgNameElement = doc.createElement("organizationName");
		orgNameElement.appendChild(doc.createTextNode(orgName));
		publisherElement.appendChild(orgNameElement);

		Element orgEmailElement = doc.createElement("electronicMailAddress");
		orgEmailElement.appendChild(doc.createTextNode(orgEmail));
		publisherElement.appendChild(orgEmailElement);

		Element orgOnlineUrlElement = doc.createElement("onlineUrl");
		orgOnlineUrlElement.appendChild(doc.createTextNode(orgOnlineUrl));
		publisherElement.appendChild(orgOnlineUrlElement);

		Element orgUserIdElement = doc.createElement("userId");
		orgUserIdElement.appendChild(doc.createTextNode(orgUserId));
		orgUserIdElement.setAttribute(orgUserIdAttributeName, orgUserIdAttributeValue);
		publisherElement.appendChild(orgUserIdElement);

		Node datasetNode = getDatasetNode(doc);
		NodeList insertNodeList = getElementNodeList(doc, insertBefore);
		Node insertNode = insertNodeList.item(0);
		datasetNode.insertBefore(publisherElement, insertNode);

	}

    /**
     * Add an alternateIdentifier element to the Level-1 metadata document.
     *
     * @param doc                   The document object
     * @param alternateID           The alternate identifier string value
     * @param attributeValue        The value of the system attribute
     *
     * @throws TransformerException
     */
	public void addAlternateIdentifier(Document doc, String alternateID, String attributeValue)
			throws TransformerException {
		final String elementName = "alternateIdentifier";
		final String attributeName = "system";
		Element element = doc.createElement(elementName);
		element.appendChild(doc.createTextNode(alternateID));
		if (attributeValue != null) {
			element.setAttribute(attributeName, attributeValue);
		}
		Node datasetNode = getDatasetNode(doc);

		/*
		 * Determine where to insert the alternateIdentifier element. This
		 * depends on the presence of nearby optional elements.
		 */
		String insertBefore = null;
		if (hasElement(doc, SHORTNAME_PATH)) {
			insertBefore = SHORTNAME_PATH;  // insert before the shortName
		}
		else {
			insertBefore = TITLE_PATH; // no shortName, so insert before the title
		}

		NodeList insertNodeList = getElementNodeList(doc, insertBefore);
		Node insertNode = insertNodeList.item(0);
		datasetNode.insertBefore(element, insertNode);
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
   * Returns a list of all elements contained in
   * this EML document
   * 
   * @return a list of all {@code //dataset/contact} elements contained in
   *         the EML document.
   */
  private NodeList getElementNodeList(Document emlDocument, String elementPath) {
    NodeList nodeList = null;

    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      nodeList = (NodeList) xPath.evaluate(elementPath, emlDocument, XPathConstants.NODESET);
    }
    catch (XPathExpressionException e) {
      throw new IllegalStateException(e);
    }

    return nodeList;
  }


  /**
   * Returns the access element node contained in this EML document.
   * 
   * @return  the access element Node object
   */
  private Node getAccessNode(Document emlDocument) {
    Node node = null;

    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      node = (Node) xPath.evaluate("//access", emlDocument, XPathConstants.NODE);
    }
    catch (XPathExpressionException e) {
      throw new IllegalStateException(e);
    }

    return node;
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
   * Returns the eml element node contained in
   * this EML document.
   * 
   * @return  the eml element Node object
   */
  private Node getEmlNode(Document emlDocument) {
    Node node = null;

    node = emlDocument.getFirstChild();

    return node;
  }


  /**
   * Returns the system attribute value of the provided EML document. If the
   * document does not contain the attribute {@code //@system}, or if it does
   * not have a value, an empty string is returned.
   * 
   * @param levelZeroEMLDocument
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

    EMLReferences emlReferences = new EMLReferences(emlDocument);
    NodeList referencesNodeList = emlReferences.getReferences();
	EMLAnnotationReferences emlAnnotationReferences = new EMLAnnotationReferences(emlDocument);
	ArrayList<Node> annotationReferencesArrayList = emlAnnotationReferences.getReferences();
	String entityId;
    
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
              if (entryKey.trim().equals(entityName.trim())) {
                logger.debug("Matched entityName: " + entityName);

				String[] entryValueParts = entryValue.split("/");
				String entityHash = entryValueParts[entryValueParts.length - 1];

				entityId = ((Element)entityNode).getAttribute("id");

				((Element)entityNode).setAttribute("id", entityHash);
				((Element)entityNode).setAttribute("scope", "document");
				((Element)entityNode).setAttribute("system", LEVEL_ONE_SYSTEM_ATTRIBUTE);

				// Update any corresponding "references" element to the new entity hash "id" attribute
				if (!entityId.isEmpty()) {
					for (int k = 0; k < referencesNodeList.getLength(); k++) {
						Node referencesNode = referencesNodeList.item(k);
						String referenceNodeText = referencesNode.getTextContent();
						if (entityId.equals(referenceNodeText)) {
							referencesNode.setTextContent(entityHash);
						}
					}
				}

				// Update any corresponding "annotation" element with a "references" attribute to the new
				// entity hash "id" attribute
				if (!entityId.isEmpty()) {
					for (int k = 0; k < annotationReferencesArrayList.size(); k++) {
						Node annotationNode = annotationReferencesArrayList.get(k);
						String annotationNodeAttributeText = ((Element)annotationNode).getAttribute("references");
						if (entityId.equals(annotationNodeAttributeText)) {
							((Element)annotationNode).setAttribute("references", entityHash);
						}
					}
				}

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
				  ((Element)urlNodeList.item(0)).setAttribute("function", "download");
                }
              }
            }
          }
        }
      }
    }
  }

  private void modifyPubDate(Document emlDocument)
    throws TransformerException {

    CachedXPathAPI xpathapi = new CachedXPathAPI();
    Node pubDateNode = xpathapi.selectSingleNode(emlDocument, PUB_DATE_PATH);

    LocalDate now = LocalDate.now();
    String pubDate = now.toString();

    if (pubDateNode != null) {
    	pubDateNode.setTextContent(pubDate);
	}
    else {
    	addPubDate(emlDocument, pubDate);
	}

  }

  private void modifyPubPlace(Document emlDocument)
    throws TransformerException {

    CachedXPathAPI xpathapi = new CachedXPathAPI();
    Node pubPlaceNode = xpathapi.selectSingleNode(emlDocument, PUB_PLACE_PATH);

    String pubPlace = "Environmental Data Initiative";

    if (pubPlaceNode != null) {
    	pubPlaceNode.setTextContent(pubPlace);
	}
    else {
    	addPubPlace(emlDocument, pubPlace);
	}

  }

  private void modifyPublisher(Document doc) throws TransformerException {

    CachedXPathAPI xpathapi = new CachedXPathAPI();
    Node publisherNode = xpathapi.selectSingleNode(doc, PUBLISHER_PATH);
    Node datasetNode = getDatasetNode(doc);

    if (publisherNode != null) {
    	datasetNode.removeChild(publisherNode);
	}
	addPublisher(doc);

  }

  /*
   * Set the value of the @system attribute
   */
  private void setSystemAttribute(Document levelZeroEMLDocument) {
    Element rootElement = levelZeroEMLDocument.getDocumentElement();
    rootElement.setAttribute("system", LEVEL_ONE_SYSTEM_ATTRIBUTE);
  }
  
  
  /*
   * Get the value of the @packageId attribute
   */
  private String getPackageIdAttribute(Document levelZeroEMLDocument) {
    Element rootElement = levelZeroEMLDocument.getDocumentElement();
    String packageId = rootElement.getAttribute("packageId");
    return packageId;
  }
  
  
    /**
     * This main program can be used for testing the insertion of a default
     * intellectualRights element.
     * 
     * @param args     No command-line arguments are processed
     */
	public static void main(String[] args) {
		String filePath = "/home/pasta/git/NIS/DataPackageManager/test/data/HasIntellectualRights.xml";
		//String filePath = "/home/pasta/git/NIS/DataPackageManager/test/data/HasNoIntellectualRights.xml";
		String newFilePath = String.format("%s_new", filePath);
		File newFile = new File(newFilePath);
		LevelOneEMLFactory loef = new LevelOneEMLFactory();
		File levelZeroEMLFile = new File(filePath);
		//String levelOneEMLString = null;
		//Node documentElement = levelZeroEMLDocument.getDocumentElement();
		//String levelZeroEMLString = XMLUtilities.getDOMTreeAsString(documentElement);
		try {
			Document levelZeroEMLDocument = XmlUtility.xmlFileToDocument(levelZeroEMLFile);
			loef.checkIntellectualRights(levelZeroEMLDocument);
		    Node documentElement = levelZeroEMLDocument.getDocumentElement();
		    String levelOneEMLString = XMLUtilities.getDOMTreeAsString(documentElement);
		    FileUtils.writeStringToFile(newFile, levelOneEMLString, "UTF-8");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
