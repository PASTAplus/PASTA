/**
 *    '$RCSfile: GenericDataPackageParser.java,v $'
 *
 *     '$Author: leinfelder $'
 *       '$Date: 2007-10-18 00:45:08 $'
 *   '$Revision: 1.1 $'
 *
 *  For Details: http://kepler.ecoinformatics.org
 *
 * Copyright (c) 2003 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the
 * above copyright notice and the following two paragraphs appear in
 * all copies of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN
 * IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY
 * OF SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY
 * OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 * UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */

package edu.lternet.pasta.dml.parser.generic;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.xpath.CachedXPathAPI;

import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.dml.parser.DateTimeDomain;
import edu.lternet.pasta.dml.parser.Domain;
import edu.lternet.pasta.dml.parser.EnumeratedDomain;
import edu.lternet.pasta.dml.parser.NumericDomain;
import edu.lternet.pasta.dml.parser.Attribute;
import edu.lternet.pasta.dml.parser.AttributeList;
import edu.lternet.pasta.dml.parser.Entity;
import edu.lternet.pasta.dml.parser.Party;
import edu.lternet.pasta.dml.parser.StorageType;
import edu.lternet.pasta.dml.parser.TextComplexDataFormat;
import edu.lternet.pasta.dml.parser.TextDelimitedDataFormat;
import edu.lternet.pasta.dml.parser.TextDomain;
import edu.lternet.pasta.dml.parser.TextWidthFixedDataFormat;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This is plugin Parser which parses EML 2.0.0 metadata files to 
 * get the metadata information which decribes data file.
 * 
 * Note that the term "generic" is misleading in that a generic document
 * needs to have an EML-compliant dataset element somewhere within it.
 * This class simply allows more general forms of schema to be parsed. 
 * 
 * @author tao
 * @author leinfelder (refactored to this form from orginal EML200Parser)
 */
public class GenericDataPackageParser implements DataPackageParserInterface
{
    /*
     * Class fields
     */

    // private static Log log;
    private static boolean isDebugging;
    private static final String ID = "id";
 
    /*static {
      log = LogFactory.getLog( 
                   "org.ecoinformatics.seek.datasource.eml.eml2.Eml200Parser" );
      isDebugging = log.isDebugEnabled();
    }*/
    
    /*
     * Instance fields
     */
    
    // previously these were constants, now member variables with defaults
    protected String packageIdPath = null;
    protected String pubDatePath = null;
    protected String fundingPath = null;
    protected String dataTableEntityPath = null;
    protected String spatialRasterEntityPath = null;
    protected String spatialVectorEntityPath  = null;
    protected String storedProcedureEntityPath = null;
    protected String viewEntityPath = null;
    protected String otherEntityPath = null;
    
    protected String accessPath = null;
    protected String datasetTitlePath = null;
    protected String datasetCreatorPath = null;
    protected String datasetAbstractPath = null;
    protected String entityAccessPath = null;
    protected String entityPhysicalAuthenticationPath = null;
    protected String entityPhysicalSizePath = null;
    protected String alternateIdentifierPath = null;
    
    private int numEntities = 0;
    private Entity entityObject = null;
    private int elementId = 0;
    private int numberOfComplexFormats = 0;
    // Associates attributeList id values with attributeList objects
    private Hashtable<String, AttributeList> attributeListIdHash = 
                                     new Hashtable<String, AttributeList>();
    private DataPackage emlDataPackage = null;
    private final String DEFAULT_RECORD_DELIMITER = "\\r\\n";
    
    
    /**
     * Default constructor - no custom xpath parameters
     */
    public GenericDataPackageParser() {
    	//sets the default path values for documents
		this.initDefaultXPaths();
    }
    
    
    /**
     * Constructor that accepts only the packageIdPath.
     * Allows packageId to be located anywhere in schema,
     * but assumes default (EML) placement of dataset
     * @param packageIdPath path expression specifying where to look for packageId
     */
    public GenericDataPackageParser(String packageIdPath) {
    	//sets the default path values for documents
    	this.initDefaultXPaths();
    	
    	//set the param
		this.packageIdPath = packageIdPath;
    }

    
    /**
	 * sets the default xpath strings for locating data package elements
	 * note that root element can be anything with a packageId attribute
	 */
	private void initDefaultXPaths() {
		// sets the default path values for documents
		packageIdPath = "//*/@packageId";
		pubDatePath = "//dataset/pubDate";
		dataTableEntityPath = "//dataset/dataTable";
		spatialRasterEntityPath = "//dataset/spatialRaster";
		spatialVectorEntityPath = "//dataset/spatialVector";
		storedProcedureEntityPath = "//dataset/storedProcedure";
		viewEntityPath = "//dataset/view";
		otherEntityPath = "//dataset/otherEntity";
		accessPath = "//access";
		datasetTitlePath = "//dataset/title";
		datasetCreatorPath = "//dataset/creator";
		datasetAbstractPath = "//dataset/abstract";
		entityAccessPath = "physical/distribution/access";
		entityPhysicalAuthenticationPath = "physical/authentication";
		entityPhysicalSizePath = "physical/size";
		alternateIdentifierPath = "//dataset/alternateIdentifier";
		fundingPath = "//dataset/project/funding";
	}

	
	/**
     * Returns a hashtable of with the id of the entity as the key and the data
     * file id to which the entity refers as the value. This way, if you want to
     * know what data file goes with an entity, you can do a get on this hash
     * for the id of the entity. Note that the entity id is the XML entity id
     * from the generated input step, not the id of the entity file itself.
     * 
     * @return fileHash, a HashTable of entity ids mapped to data file ids
     */
    /*public Hashtable getDataFilesHash()
    {
        return fileHash;
    }*/
    
    
    /* (non-Javadoc)
	 * @see edu.lternet.pasta.dml.parser.generic.GenericDatasetParserInterface#parse(org.xml.sax.InputSource)
	 */
    public void parse(InputSource source) throws Exception
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
        Document doc = builder.parse(source);
        parseDocument(doc);
    }

    
    /* (non-Javadoc)
	 * @see edu.lternet.pasta.dml.parser.generic.GenericDatasetParserInterface#parse(java.io.InputStream)
	 */
    public void parse(InputStream is) throws Exception
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
        Document doc = builder.parse(is);
        parseDocument(doc);
    }
    
    
    /**
     * Parses the EML document. Now except dataTable, spatialRaster and 
     * spatialVector entities are added. 
     * 
     * @param doc  the Document object to be parsed
     */
    private void parseDocument(Document doc) throws Exception
    {
        NodeList dataTableEntities;
        NodeList spatialRasterEntities;
        NodeList spatialVectorEntities;
        NodeList otherEntities;
        NodeList viewEntities;
        CachedXPathAPI xpathapi = new CachedXPathAPI();
        String packageId = null;
        String emlNamespace = null;
        
        try {
          // process packageid
        	Node packageIdNode = xpathapi.selectSingleNode(doc, packageIdPath);
            
        	if (packageIdNode != null)
        	{
        	   //System.out.println("in packageIdNode is not null");
        	   packageId          = packageIdNode.getNodeValue();
        	}
        	
          emlDataPackage        = new DataPackage(packageId);
          
          emlNamespace = parseEmlNamespace(doc);
          if (emlDataPackage != null) {
            emlDataPackage.setEmlNamespace(emlNamespace);
          }
          
          emlDataPackage.checkSchemaValid(doc, emlNamespace);
          emlDataPackage.checkParserValid(doc);
          emlDataPackage.checkSchemaValidDereferenced(doc, emlNamespace);
          
          String systemValue = parseSystemAttribute(doc);
          if (systemValue != null) {
            emlDataPackage.setSystem(systemValue);
          }
          
          int nKeywordElements = countElements(xpathapi, doc, "keyword");
          emlDataPackage.setNumberOfKeywordElements(nKeywordElements);
          int nMethodsElements = countElements(xpathapi, doc, "methods");
          emlDataPackage.setNumberOfMethodsElements(nMethodsElements);
          int nCoverageElements = countElements(xpathapi, doc, "coverage");
          emlDataPackage.setNumberOfCoverageElements(nCoverageElements);
          int nGeographicCoverageElements = countElements(xpathapi, doc, "geographicCoverage");
          emlDataPackage.setNumberOfGeographicCoverageElements(nGeographicCoverageElements);
          int nTaxonomicCoverageElements = countElements(xpathapi, doc, "taxonomicCoverage");
          emlDataPackage.setNumberOfTaxonomicCoverageElements(nTaxonomicCoverageElements);
          int nTemporalCoverageElements = countElements(xpathapi, doc, "temporalCoverage");
          emlDataPackage.setNumberOfTemporalCoverageElements(nTemporalCoverageElements);
            
            // now dataTable, spatialRaster and spatialVector are handled
            dataTableEntities              = xpathapi.selectNodeList(doc, dataTableEntityPath);
            spatialRasterEntities = 
                              xpathapi.selectNodeList(doc, spatialRasterEntityPath);
            spatialVectorEntities = 
                              xpathapi.selectNodeList(doc, spatialVectorEntityPath);
            otherEntities         = xpathapi.selectNodeList(doc, otherEntityPath);
            viewEntities          = xpathapi.selectNodeList(doc, viewEntityPath);
            
            
            // Store <access> XML block because some applications may need it
            Node accessNode = xpathapi.selectSingleNode(doc, accessPath);
            if (accessNode != null) {
              String accessXML = nodeToXmlString(accessNode);
              emlDataPackage.setAccessXML(accessXML);
            }
            
            // Store the dataset alternate identifiers and their system attribute values
            NodeList alternateIdentifierNodeList = xpathapi.selectNodeList(doc, alternateIdentifierPath);
            if (alternateIdentifierNodeList != null) {
            	for (int i = 0; i < alternateIdentifierNodeList.getLength(); i++) {
            		String systemAttributeValue = null;
            		Node alternateIdentifierNode = alternateIdentifierNodeList.item(i);
            		NamedNodeMap attributeNames = alternateIdentifierNode.getAttributes();
            		if ((attributeNames != null) && (attributeNames.getLength() > 0)) {
            		    Node systemAttributeNode = attributeNames.getNamedItem("system");
            		    if (systemAttributeNode != null) {
            		        systemAttributeValue = systemAttributeNode.getNodeValue();
            		    }
            		}
            		String alternateIdentifierValue = alternateIdentifierNode.getTextContent();
            		emlDataPackage.putAlternateIdentifier(alternateIdentifierValue, systemAttributeValue);
            	}
            }
            
            emlDataPackage.checkAlternateIdentifiers();
            	
            // Store the dataset title
            Node datasetTitleNode = xpathapi.selectSingleNode(doc, datasetTitlePath);
            if (datasetTitleNode != null) {
              String titleText = datasetTitleNode.getTextContent();
              emlDataPackage.setTitle(titleText);
            }
            
            // Store the funding
            Node fundingNode = xpathapi.selectSingleNode(doc, fundingPath);
            String fundingText = null;
            if (fundingNode != null) {
                fundingText = fundingNode.getTextContent();
            }
            emlDataPackage.setFunding(fundingText);
           
            // Store the dataset creators
            NodeList datasetCreatorNodeList = xpathapi.selectNodeList(doc, datasetCreatorPath);
            if (datasetCreatorNodeList != null) {
            	for (int i = 0; i < datasetCreatorNodeList.getLength(); i++) {
            		Node datasetCreatorNode = datasetCreatorNodeList.item(i);
            		
					String surName = null;
                    List<String> givenNames = null;
					String organization = null;
					
					Node surNameNode = xpathapi.selectSingleNode(datasetCreatorNode, "individualName/surName");
            		if (surNameNode != null) {
            			surName = surNameNode.getTextContent();
            		}
            		
					Node givenNameNode = xpathapi.selectSingleNode(datasetCreatorNode, "individualName/givenName");
            		if (givenNameNode != null) {
            			if (givenNames == null) {
            				givenNames = new ArrayList<String>();
            			}
            			givenNames.add(givenNameNode.getTextContent());
            		}
            		
            		Node orgNode = xpathapi.selectSingleNode(datasetCreatorNode, "organizationName");
            		if (orgNode != null) {
            			organization = orgNode.getTextContent();
            		}
            		
					Party party = new Party(surName, givenNames, organization);
					emlDataPackage.getCreators().add(party );
            	}
            }
            
            // Store the pubDate
            String pubDate = null;
            Node pubDateNode = xpathapi.selectSingleNode(doc, pubDatePath);
            if (pubDateNode != null) {
              pubDate = pubDateNode.getTextContent().trim();
            }
            emlDataPackage.setPubDate(pubDate);
            
            // Parse the dataset abstract text
            NodeList datasetAbstractNodeList = xpathapi.selectNodeList(doc, datasetAbstractPath);
            parseDatasetAbstract(datasetAbstractNodeList);
      
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception(
                            "Error extracting entities from eml2.0.0 package.", e);
        }
        
        try {
            processEntities(xpathapi, 
                            dataTableEntities, 
                            dataTableEntityPath, 
                            packageId, 
                            emlNamespace);

            processEntities(xpathapi, 
                            spatialRasterEntities, 
                            spatialRasterEntityPath, 
                            packageId, 
                            emlNamespace);
            
            processEntities(xpathapi, 
                            spatialVectorEntities, 
                            spatialVectorEntityPath, 
                            packageId, 
                            emlNamespace);
            
            processEntities(xpathapi, 
                            otherEntities, 
                            otherEntityPath, 
                            packageId, 
                            emlNamespace);
            
            processEntities(xpathapi, 
                            viewEntities, 
                            viewEntityPath, 
                            packageId, 
                            emlNamespace);
        } 
        catch (Exception e) {
            throw new Exception("Error processing entities: " + e.getMessage(), e);
        }
    }

    
    /* (non-Javadoc)
	 * @see edu.lternet.pasta.dml.parser.generic.GenericDatasetParserInterface#getDataPackage()
	 */
    public DataPackage getDataPackage()
    {
    	return emlDataPackage;
    }

    
    /*
     * Parses the "xmlns:eml" attribute value from the
     * "eml:eml" element. This value indicates the version of
     * EML, e.g. "eml://ecoinformatics.org/eml-2.2.0"
     */
    private String parseEmlNamespace(Document doc) {
      String namespaceURI = null;
      
      if (doc != null) {
        NodeList docNodes = doc.getChildNodes();
      
        if (docNodes != null) {
          int len = docNodes.getLength();
          for (int i = 0; i < len; i++) {
            Node docNode = docNodes.item(i);
            String name = docNode.getNodeName();
          
            if (name!= null && name.equals("eml:eml")) {
              NamedNodeMap attributeMap = docNode.getAttributes();
              int mapLength = attributeMap.getLength();
              for (int m = 0; m < mapLength; m++) {
                Node attNode = attributeMap.item(m);
                String attNodeName = attNode.getNodeName();
                String attNodeValue = attNode.getNodeValue();
                if (attNodeName.equals("xmlns:eml")) {
                  namespaceURI = attNodeValue;
                }
              }
            }
          }
        }
      }

      return namespaceURI;
    }

    
    /*
     * Counts the number of elements with the specified elementName. This
     * is used to implement presence/absence quality checks while parsing EML 2.1.0
     * documents.
     */
    private int countElements(CachedXPathAPI xpathapi, Document doc, String elementName) {
      int nMethodsElements = 0;
      final String xPath = "//" + elementName;
      
      try {
        NodeList methodsList = xpathapi.selectNodeList(doc, xPath);        
        nMethodsElements = methodsList.getLength();
      }
      catch (TransformerException e) {
        System.err.println(
            "TransformerException while detecting 'methods' element: " + 
            e.getMessage());
      }
      
      return nMethodsElements;
    }

    
    /*
     * Parses the "@system" attribute value from the
     * "eml:eml" element.
     */
    private String parseSystemAttribute(Document doc) {
      String systemValue = null;
      
      if (doc != null) {
        NodeList docNodes = doc.getChildNodes();
      
        if (docNodes != null) {
          int len = docNodes.getLength();
          for (int i = 0; i < len; i++) {
            Node docNode = docNodes.item(i);
            String name = docNode.getNodeName();
          
            if (name!= null && name.equals("eml:eml")) {
              NamedNodeMap attributeMap = docNode.getAttributes();
              int mapLength = attributeMap.getLength();
              for (int m = 0; m < mapLength; m++) {
                Node attNode = attributeMap.item(m);
                String attNodeName = attNode.getNodeName();
                String attNodeValue = attNode.getNodeValue();
                if (attNodeName.equals("system")) {
                  systemValue = attNodeValue;
                }
              }
            }
          }
        }
      }

      return systemValue;
    }

    
    /**
     * Processes the attributeList element.
     * 
     * @param  xpathapi  XPath API
     * @param  attributeListNodeList   a NodeList
     * @param  xpath     the XPath path string to the data entity 
     * @param  entity    the entity object whose attribute list is processed
     */
    private void processAttributeList(CachedXPathAPI xpathapi, 
                                      NodeList attributeListNodeList, 
                                      String xpath,
                                      Entity entity) 
            throws Exception
    {
        AttributeList attributeList = new AttributeList();
        Node attributeListNode = attributeListNodeList.item(0);
        
        /*
         * It is allowable in EML to omit the attributeList for an
         * 'otherEntity' data entity.
         */
        if (attributeListNode == null) {
          if (xpath != null && xpath.equals(otherEntityPath)) {
            System.err.println(
                "No attributeList was specified for otherEntity '" +
                entity.getName() + "'. This is allowable in EML."
                              );
            return;
          }
          else {
            throw new Exception(
                "No attributeList was specified for entity '" + 
                entity.getName() + "'.");
          }
        }
        
        // Get attributeList element's id attribute
        NamedNodeMap attributeListNodeAttributes = 
            attributeListNode.getAttributes();
        String idString = null;
        
        if (attributeListNodeAttributes != null)
        {
          Node idNode = attributeListNodeAttributes.getNamedItem(ID);
          
          if (idNode != null)
          {
            idString = idNode.getNodeValue();
            attributeList.setId(idString);
            
        	  if (isDebugging) {
               //log.debug("The id value for the attributelist is " + idString);
        	  }
          }
        }
        
        NodeList attributeNodeList = 
          xpathapi.selectNodeList(attributeListNode, "attribute");
        NodeList referencesNodeList = 
          xpathapi.selectNodeList(attributeListNode, "references");
        
        if (attributeNodeList != null && 
            attributeNodeList.getLength() > 0) 
        {
            processAttributes(xpathapi, attributeNodeList, attributeList, entity);
            
            if (idString != null)
            {
               attributeListIdHash.put(idString , attributeList);
            }
        }
        else if (referencesNodeList != null && 
                 referencesNodeList.getLength() > 0) 
        {
            // get the references id 
            Node referencesNode = referencesNodeList.item(0);
            
        	  if (isDebugging) {
                //log.debug("The reference node's name is "+
                //          referenceNode.getNodeName());
        	  }
            
            String referencesId = referencesNode.getFirstChild().getNodeValue();
            
        	  if (isDebugging) {
        		  //log.debug("the reference id is "+ referenceId);
        	  }
            
            attributeList = (AttributeList) attributeListIdHash.get(referencesId);
        }
        else
        {
       	    //log.debug(
            //    "The children name of attribute list couldn't be understood");
            throw new Exception(" couldn't be a child of attributeList");
        }
          
        if (!entityObject.isSimpleDelimited())
        {
           int numberOfAttributes = attributeList.getAttributes().length;
           
           if (numberOfAttributes != numberOfComplexFormats || 
                (
                  (numberOfAttributes == numberOfComplexFormats) && 
                  (numberOfComplexFormats == 0)
                )
              )
           {
               throw new Exception("Complex format elements should have " +
                                   "same number as attribute number");
           }
           else
           {
               //entityObject.setDataFormatArray(formatArray);
           }
        }
       
        entityObject.setAttributeList(attributeList);
    }

    
    /**
     * Processes the attributes in an attribute list. Called by
     * processAttributeList().
     * 
     * @param  xpathapi           the XPath API
     * @param  attributesNodeList a node list
     * @param  attributeList      an AttributeList object
     * @param  entity             the entity object whose attribute list is being processed
     */
    private void processAttributes(CachedXPathAPI xpathapi, 
                                   NodeList attributesNodeList, 
                                   AttributeList attributeList,
                                   Entity entity)
            throws Exception
    {
        int attributesNodeListLength = attributesNodeList.getLength();
        
        // Process each attribute
        for (int i = 0; i < attributesNodeListLength; i++) {
            Node attributeNode = attributesNodeList.item(i);
            NodeList attributeNodeChildren = attributeNode.getChildNodes();
            //NamedNodeMap attAttributes = att.getAttributes();

            String attName = "";
            String attLabel = "";
            String attDefinition = "";
            String attUnit = "";
            String attUnitType = "";
            String attMeasurementScale = "";
            String attPrecision = "";
            Domain domain = null;
            String id = null;
            Vector missingValueCodeVector = new Vector();
            double numberPrecision = 0;
            ArrayList<StorageType> storageTypeArray = 
                new ArrayList<StorageType>();
            
            // get attribute id
            NamedNodeMap attributeNodeAttributesMap = 
                attributeNode.getAttributes();
            
            if (attributeNodeAttributesMap != null)
            {
                Node idNode =  attributeNodeAttributesMap.getNamedItem(ID);
                  
                if (idNode != null)
                {
                    id = idNode.getNodeValue();
                }
            }
            
            elementId++;

            for (int j = 0; j < attributeNodeChildren.getLength(); j++) {
                Node childNode = attributeNodeChildren.item(j);
                String childNodeName = childNode.getNodeName();
                String childNodeValue = childNode.getFirstChild() == null ? null: childNode.getFirstChild().getNodeValue();
                childNodeValue = childNodeValue == null ? childNodeValue : childNodeValue.trim();
                if (childNodeName.equals("attributeName")) {
                	if (childNodeValue != null) {
	                    attName = childNodeValue.replace('.', '_');
                	}
                } 
                else if (childNodeName.equals("attributeLabel")) {
                    attLabel = childNodeValue;
                } 
                else if (childNodeName.equals("attributeDefinition")) {
                    attDefinition = childNodeValue;
                }
                // Process storageType elements
                else if (childNodeName.equals("storageType")) {
                  String storageTypeTextValue = childNodeValue;
                  NamedNodeMap storageTypeAttributesMap = childNode.getAttributes();
                  StorageType storageType;
                  String typeSystem = "";
                  Node typeSystemNode = null;
                  
                  // Determine whether the typeSystem attribute was specified
                  if (storageTypeAttributesMap != null) {
                      typeSystemNode =  
                          storageTypeAttributesMap.getNamedItem(typeSystem);
                        
                      if (typeSystemNode != null) {
                          typeSystem = typeSystemNode.getNodeValue();
                      }
                  }
                                
                  // Use the appropriate StorageType constructor depending on 
                  // whether the 'typeSystem' attribute was specified
                  if (!typeSystem.equals("")) {
                      storageType = new StorageType(storageTypeTextValue, typeSystem);
                  }
                  else {
                      storageType = new StorageType(storageTypeTextValue);
                  }            
                  
                  storageTypeArray.add(storageType);
                }
                else if (childNodeName.equals("measurementScale")) {
                    //unit is tricky because it can be custom or standard
                    //Vector info = new Vector();
                    //int domainType = Domain.DOM_NONE;
                    NodeList measurementScaleChildNodes = childNode.getChildNodes();
                    
                    for (int k = 0; k < measurementScaleChildNodes.getLength(); k++) {
                        Node measurementScaleChildNode = 
                            measurementScaleChildNodes.item(k);
                        String measurementScaleChildNodeName = 
                            measurementScaleChildNode.getNodeName();
                        
                        if (measurementScaleChildNodeName.equals("interval") || 
                            measurementScaleChildNodeName.equals("ratio")
                           ) {
                            String numberType = null;
                            String min = "", max = "";
                            Node standardUnitNode = 
                              xpathapi.selectSingleNode(measurementScaleChildNode,
                                                        "unit/standardUnit");
                            Node customUnitNode = 
                              xpathapi.selectSingleNode(measurementScaleChildNode,
                                                        "unit/customUnit");
                            
                            if (standardUnitNode != null) {
                                attUnit = standardUnitNode.getFirstChild().getNodeValue();
                                attUnitType = Attribute.STANDARDUNIT;
                            } else if (customUnitNode != null) {
                                attUnit = customUnitNode.getFirstChild().getNodeValue();
                                attUnitType = Attribute.CUSTOMUNIT;
                            } else {
                                System.err.println("Unable to determine attribute unit.");
                            }
                            
                            Node precisionNode = 
                                xpathapi.selectSingleNode(measurementScaleChildNode,
                                                          "precision");
                            
                            if (precisionNode != null) {
                                // precision is optional in EML201 so if it is
                                // not provided, the attPrecision will be the
                                // empty string
                                attPrecision = precisionNode.getFirstChild()
                                                        .getNodeValue();
                                numberPrecision = 
                                       (new Double(attPrecision)).doubleValue();
                                
                            }
                            
                            Node numericDomainNode = 
                                xpathapi.selectSingleNode(measurementScaleChildNode,
                                                          "numericDomain");
                            NodeList numericDomainChildNodes = 
                                numericDomainNode.getChildNodes();
                            
                            for (int index = 0; 
                                 index < numericDomainChildNodes.getLength(); 
                                 index++)
                            {
                                String numericDomainChildNodeName = 
                                    numericDomainChildNodes.item(index).getNodeName();
                                
                                if (numericDomainChildNodeName.equals("numberType"))
                                {
                                    // Got number type
                                    numberType = 
                                        numericDomainChildNodes.item(index)
                                                               .getFirstChild()
                                                               .getNodeValue();
                                    
                                	  if (isDebugging) {
                                      //log.debug("The number type is "+ numberType);
                                	  }
                                }
                                else if (numericDomainChildNodeName.equals("boundsGroup"))
                                {
                                    // Got bounds group
                                    NodeList boundsNodeList = 
                                        xpathapi.selectNodeList(numericDomainNode, 
                                                                "./bounds");
                                    
                                    for (i = 0; i < boundsNodeList.getLength(); i++)
                                    {
                                        NodeList aNodeList;
                                        Node boundsNode;

                                        //String exclMin = null, exclMax = null;
                                        try
                                        {
                                            aNodeList = xpathapi.selectNodeList(
                                                    boundsNodeList.item(i),
                                                    "./minimum");
                                            boundsNode = aNodeList.item(0);
                                            min = boundsNode.getFirstChild()
                                                    .getNodeValue();
                                            /*exclMin = bound.getAttributes()
                                                    .getNamedItem("exclusive")
                                                    .getNodeValue();*/
                                            aNodeList = xpathapi.selectNodeList(
                                                    boundsNodeList.item(0),
                                                    "./maximum");
                                            boundsNode = aNodeList.item(0);
                                            max = boundsNode.getFirstChild()
                                                    .getNodeValue();
                                            /*exclMax = bound.getAttributes()
                                                    .getNamedItem("exclusive")
                                                    .getNodeValue();*/
                                        }
                                        catch (Exception e)
                                        {
                                        	//log.debug("Error in handle bound ", e);
                                        }
                                     }
                                 }
                            }
                            
                            Double minNum = null;
                            Double maxNum = null;
                            
                            if (!min.trim().equals(""))
                            {
                                minNum = new Double(min);
                            }
                            
                            if (!max.trim().equals(""))
                            {
                            	maxNum = new Double(max);
                            }
                            
                            NumericDomain numericDomain = 
                                  new NumericDomain(numberType, minNum, maxNum);
                            numericDomain.setPrecision(numberPrecision);
                            domain = numericDomain;
                           
                        } else if (measurementScaleChildNodeName.equals("nominal")
                                || measurementScaleChildNodeName.equals("ordinal")) {
                            NodeList nonNumericDomainChildNodes = 
                              xpathapi.selectSingleNode(measurementScaleChildNode,
                                                        "nonNumericDomain")
                                      .getChildNodes();
                            
                            for (int m = 0; 
                                 m < nonNumericDomainChildNodes.getLength(); 
                                 m++)
                            {
                                Node nonNumericDomainChildNode = 
                                    nonNumericDomainChildNodes.item(m);
                                String nonNumericDomainChildNodeName = 
                                    nonNumericDomainChildNode.getNodeName();
                                
                                if (nonNumericDomainChildNodeName.
                                        equals("textDomain")) {
                                    TextDomain textDomain = new TextDomain();
                                    NodeList definitionNodeList = 
                                      xpathapi.selectNodeList(
                                                  nonNumericDomainChildNode, 
                                                  "./definition");
                                    
                                    Node defintionNode = definitionNodeList.item(0);
                                    
                                    String definition = null;          
                                    if (defintionNode != null) {
                                    	definition = 
                                    	  defintionNode.getFirstChild() == null ? 
                                            null : 
                                    	    defintionNode.getFirstChild().getNodeValue();
                                    }
                                	
                                    textDomain.setDefinition(definition);
                                    NodeList patternNodeList = 
                                      xpathapi.selectNodeList(
                                          nonNumericDomainChildNode,
                                          "./pattern");
                                    
                                    String[] patternList = 
                                      new String[patternNodeList.getLength()];
                                    
                                    for (int l = 0; 
                                         l < patternNodeList.getLength(); 
                                         l++) {
                                      patternList[l] = patternNodeList.item(l).
                                                       getFirstChild().
                                                       getNodeValue();
                                    }
                                    
                                    if (patternList.length > 0)
                                    {
                                      textDomain.setPattern(patternList);
                                    }
                                    
                                    domain = textDomain;
                                   
                                } else if (nonNumericDomainChildNodeName.
                                           equals("enumeratedDomain")) {
                                    EnumeratedDomain enumeratedDomain = 
                                        new EnumeratedDomain();
                                    Vector info = new Vector();
                                    
                                    NodeList codeDefinitionNodeList = 
                                      xpathapi.selectNodeList(
                                          nonNumericDomainChildNode,
                                          "./codeDefinition");
                                    
                                    for (int l = 0; 
                                         l < codeDefinitionNodeList.getLength(); 
                                         l++) {
                                        info.add(codeDefinitionNodeList.item(l).
                                                 getFirstChild().getNodeValue());
                                    }
                                    
                                    enumeratedDomain.setInfo(info);
                                    domain = enumeratedDomain;
                                }
                            }
                        } else if (measurementScaleChildNodeName.
                                       equalsIgnoreCase("datetime")) {
                            DateTimeDomain dateTimeDomain = new DateTimeDomain();
                            String formatString = 
                              (xpathapi.selectSingleNode(measurementScaleChildNode,
                                                         "./formatString")).
                                          getFirstChild().
                                          getNodeValue();
                            
                            dateTimeDomain.setFormatString(formatString);
                            domain = dateTimeDomain;
                            entity.checkDateTimeFormatString(formatString);
                        }
                    }
                }
                else if (childNodeName.equals("missingValueCode"))
                {
               		  //log.debug("in missingValueCode");
                    NodeList missingValueCodeChildNodes = 
                        childNode.getChildNodes();
                    
                    for (int k = 0; 
                         k < missingValueCodeChildNodes.getLength(); 
                         k++) 
                    {
                        Node missingValueCodeChildNode = 
                            missingValueCodeChildNodes.item(k);
                        String missingValueCodeChildNodeName = 
                            missingValueCodeChildNode.getNodeName();
                        
                        if (missingValueCodeChildNodeName.equals("code"))
                        {
                            Node missingValueCodeTextNode = 
                                missingValueCodeChildNode.getFirstChild();
                            
                            if (missingValueCodeTextNode != null)
                            {
	                            String missingValueCode = 
                                  missingValueCodeTextNode.getNodeValue();
                                
	                        	  if(isDebugging) {
	                        	    //log.debug("the missing code is "+missingCode);
	                        	  }
                                
	                            missingValueCodeVector.add(missingValueCode);
	                            //hasMissingValue = true;
                            }
                        }
                    }
                }
            }
            
            /******************************************************
             * need to use domain type to replace data type
             ******************************************************/
            /*String resolvedType = null;
            //DataType dataType = domain.getDataType();
            //resolvedType = dataType.getName();
        	if(isDebugging) {
        		//log.debug("The final type is " + resolvedType);
        	}*/
           
            Attribute attObj = 
              new Attribute(id, attName, attLabel,
                            attDefinition, attUnit, attUnitType,
                            attMeasurementScale, domain);
            
            // Add storageType elements to the Attribute object 
            // if any were parsed in the EML
            for (StorageType storageType : storageTypeArray) {
                attObj.addStorageType(storageType);
            }
            
            // Add missing value code into attribute
            for (int k = 0; k < missingValueCodeVector.size(); k++)
            {
                String missingValueCode = 
                       (String) missingValueCodeVector.elementAt(k);
            	  if (isDebugging) {
            		  //log.debug("the mssing value code " + missingCodeValue + 
                  //          " was added to attribute");
            	  }
                
                attObj.addMissingValueCode(missingValueCode);
            }
            
            attributeList.add(attObj);
        }
    }
    

    /**
     * Pulls the entity information out of the XML and stores it in a hash table.
     */
    private void processEntities(CachedXPathAPI xpathapi, 
                                 NodeList entitiesNodeList, 
                                 String xpath, 
                                 String packageId,
                                 String emlNamespace)
            throws SAXException,
                   javax.xml.transform.TransformerException, 
                   Exception
    { 
        // Make sure that entities is not null
        if (entitiesNodeList == null)
        {
          return;
        }
        
        int entityNodeListLength = entitiesNodeList.getLength();
        numEntities = numEntities + entityNodeListLength;

        for (int i = 0; i < entityNodeListLength; i++) {
        
            String entityAccessXML = null;
            String entityName = "";
            String entityDescription = "";
            String entityOrientation = "";
            String entityCaseSensitive = "";
            String onlineUrl = "";
            String onlineUrlFunction = null;
            String format = null;
            Integer numHeaderLines = null;
            Integer numFooterLines = null;
            String fieldDelimiter = null;
            String recordDelimiter = DEFAULT_RECORD_DELIMITER;
            String metadataRecordDelimiter = null; // The record delimiter specified in the metadata
            String compressionMethod = "";
            String encodingMethod = "";
            String quoteCharacter = null;
            String literalCharacter = null;
            TextComplexDataFormat[] formatArray = null;        
            String entityNumberOfRecords = "-1";
            boolean hasDistributionOnline = false;
            boolean hasDistributionOffline = false;
            boolean hasDistributionInline = false;
            boolean hasNumberOfRecords = false;
            boolean hasPhysicalAuthentication = false;
            boolean isExternallyDefinedFormat = false;
            boolean isDataTableEntity = false;
            boolean isOtherEntity = false;
            boolean isImageEntity   = false;
            boolean isGZipDataFile  = false;
            boolean isZipDataFile   = false;
            boolean isTarDataFile   = false;
            boolean isSimpleDelimited = true;
            boolean isTextFixed = false;
            boolean isCollapseDelimiters = false;
            String sizeString = null;
            String unitString = null;
            HashMap<String,String> integrityMap = new HashMap<String,String>();

            if (xpath != null) {
				if (xpath.equals(dataTableEntityPath)) {
					isDataTableEntity = true;
				} else if (xpath.equals(spatialRasterEntityPath) || xpath.equals(spatialVectorEntityPath)) {
					isImageEntity = true;
				} else if (xpath.equals(otherEntityPath)) {
					isOtherEntity = true;
				}
            }
            
             //go through the entities and put the information into the hash.
            elementId++;
            Node entityNode = entitiesNodeList.item(i);
            String id = null;
            NamedNodeMap entityNodeAttributes = entityNode.getAttributes();
            
            if (entityNodeAttributes != null) {        
                Node idNode = entityNodeAttributes.getNamedItem(ID);
                
                if (idNode != null)
                {
                   id = idNode.getNodeValue();
                }
            }
            
            NodeList entityNodeChildren = entityNode.getChildNodes();
            
            for (int j = 0; j < entityNodeChildren.getLength(); j++) {
                Node childNode = entityNodeChildren.item(j);
                String childName = childNode.getNodeName();
                String childValue = childNode.getFirstChild() == null ? null: childNode.getFirstChild().getNodeValue();

                if (childName.equals("entityName")) {
                    entityName = childValue;
                } else if (childName.equals("entityDescription")) {
                    entityDescription = childValue;
                } else if (childName.equals("caseSensitive")) {
                    entityCaseSensitive = childValue;
                } else if (childName.equals("numberOfRecords")) {
                    entityNumberOfRecords = childValue;
                    hasNumberOfRecords = true;
                }                
               
            }
            
            NodeList attributeOrientationNodeList = 
                xpathapi.selectNodeList(
                    entityNode,
                    "physical/dataFormat/textFormat/attributeOrientation");
            
            if (attributeOrientationNodeList != null && 
                attributeOrientationNodeList.getLength() > 0) {
                entityOrientation = attributeOrientationNodeList.
                                      item(0).getFirstChild().getNodeValue();

               }

            NodeList numHeaderLinesNodeList = xpathapi.selectNodeList(entityNode,
                       "physical/dataFormat/textFormat/numHeaderLines");
            
            if ((numHeaderLinesNodeList != null) && 
                (numHeaderLinesNodeList.getLength() > 0)
               ) {
                Node numHeaderLinesNode = numHeaderLinesNodeList.item(0);
                   
                if (numHeaderLinesNode != null) {
                    String numHeaderLinesStr = 
                        numHeaderLinesNode.getFirstChild().getNodeValue();
                    numHeaderLines = new Integer(numHeaderLinesStr.trim());
                }
            }
            
            NodeList numFooterLinesNodeList = 
              xpathapi.selectNodeList(
                  entityNode,
                  "physical/dataFormat/textFormat/numFooterLines"
                                     );
            
            if ((numFooterLinesNodeList != null) && 
                (numFooterLinesNodeList.getLength() > 0)
               ) {
                Node numFooterLinesNode = numFooterLinesNodeList.item(0);
            
                if (numFooterLinesNode != null) {
                    String numFooterLinesStr = 
                        numFooterLinesNode.getFirstChild().getNodeValue();
                    numFooterLines = 
                        new Integer(numFooterLinesStr.trim());
                }
            }
           
           /*
            * Simple delimited data file
            */
           NodeList fieldDelimiterNodeList = 
               xpathapi.selectNodeList(
                 entityNode,
                 "physical/dataFormat/textFormat/simpleDelimited/fieldDelimiter"
                                      );
           
           if (fieldDelimiterNodeList != null && 
               (fieldDelimiterNodeList.getLength() > 0)
              ) {
             Node fieldDelimiterNode = fieldDelimiterNodeList.item(0);
             if (fieldDelimiterNode != null) {
               Node firstChild = fieldDelimiterNode.getFirstChild();
               if (firstChild != null) {
                 fieldDelimiter = firstChild.getNodeValue();
               }
             }
           }
           
           NodeList collapseDelimitersNodeList = 
             xpathapi.selectNodeList(entityNode,
               "physical/dataFormat/textFormat/simpleDelimited/collapseDelimiters");
           
           if (collapseDelimitersNodeList != null && 
               collapseDelimitersNodeList.getLength() > 0
              ) {     	   
        	   Node firstNode = collapseDelimitersNodeList.item(0);
        	   if (firstNode != null) {
        		   Node firstChild = firstNode.getFirstChild();
        		   if (firstChild != null) {
        			   String collapseDelimiters = firstChild.getNodeValue();
        			   if (collapseDelimiters != null) {
        	               if (collapseDelimiters.equalsIgnoreCase("yes")) {
        	                   isCollapseDelimiters = true;
        	               } 
        			   }
        		   }
        	   }
           }
           
           NodeList quoteCharacterNodeList = 
             xpathapi.selectNodeList(entityNode,
               "physical/dataFormat/textFormat/simpleDelimited/quoteCharacter");
           
           if (quoteCharacterNodeList != null && 
        		   quoteCharacterNodeList.getLength() > 0
        		  ) {
                quoteCharacter = 
                  quoteCharacterNodeList.item(0).getFirstChild().getNodeValue();
           }
           
           NodeList literalCharacterNodeList = 
             xpathapi.selectNodeList(entityNode,
             "physical/dataFormat/textFormat/simpleDelimited/literalCharacter");
           
           if (literalCharacterNodeList != null && 
        		   literalCharacterNodeList.getLength() > 0
        		  ) {
                literalCharacter = 
                  literalCharacterNodeList.item(0).getFirstChild().getNodeValue(); 
           } // End simple delimited data file
           
           /*
            *  Complex format data file
            */
           NodeList complexNodeList = 
             xpathapi.selectNodeList(entityNode,
                                     "physical/dataFormat/textFormat/complex");
           
           if (complexNodeList != null && 
               complexNodeList.getLength() > 0
              ) {
        	   //log.debug("in handle complex text data format");
             isSimpleDelimited = false;
             Node complexNode = complexNodeList.item(0);
             NodeList complexChildNodes = complexNode.getChildNodes();
             int complexChildNodesLength = complexChildNodes.getLength();
             Vector formatVector = new Vector();
             
             for (int k = 0; k < complexChildNodesLength; k++)
             {
                 Node complexChildNode = complexChildNodes.item(k);
                 
                 /*
                  * complex, textFixed
                  */
                 if (complexChildNode != null && 
                     complexChildNode.getNodeName().equals("textFixed")
                    )
                 {
                     TextWidthFixedDataFormat textWidthFixedDataFormat = 
                         handleTextFixedDataFormatNode(complexChildNode);
                     
                     if (textWidthFixedDataFormat != null)
                     {
                        formatVector.add(textWidthFixedDataFormat);
                        isTextFixed = true;
                        //complexFormatsNumber++;
                     }
                 }
                 /*
                  * complex, textDelimited
                  */
                 else if (complexChildNode != null && 
                          complexChildNode.getNodeName().equals("textDelimited")
                         )
                 {
                     TextDelimitedDataFormat textDelimitedDataFormat = 
                         handleComplexDelimitedDataFormatNode(complexChildNode);
                     
                     if (textDelimitedDataFormat != null)
                     {
                         formatVector.add(textDelimitedDataFormat);
                         //complexFormatsNumber++;
                     }
                 }
             }
             
             // Transfer vector to array
             numberOfComplexFormats = formatVector.size();
             formatArray = new TextComplexDataFormat[numberOfComplexFormats];
             for (int j = 0; j < numberOfComplexFormats; j++)
             {
                 formatArray[j] =
                               (TextComplexDataFormat)formatVector.elementAt(j);
             }
           } // End complex format data file
           
           NodeList recordDelimiterNodeList = 
               xpathapi.selectNodeList(entityNode,
                             "physical/dataFormat/textFormat/recordDelimiter");
           
           if ((recordDelimiterNodeList != null) && 
               (recordDelimiterNodeList.getLength() > 0)
              ) {
             Node firstNode = recordDelimiterNodeList.item(0);
             if (firstNode != null) {
               Node firstChild = firstNode.getFirstChild();
               if (firstChild != null) {
                 metadataRecordDelimiter = firstChild.getNodeValue();
                 recordDelimiter = metadataRecordDelimiter;
               }
             }
           }
           
           // Store the entity access XML since some applications may need it
           Node entityAccessNode = xpathapi.selectSingleNode(
                                              entityNode, 
                                              entityAccessPath);
           if (entityAccessNode != null) {
             entityAccessXML = nodeToXmlString(entityAccessNode);
           }
           
           // Store the entity physical authentication XML since some applications may need it
           NodeList physicalAuthenticationNodeList = xpathapi.selectNodeList(
                                              entityNode, 
                                              entityPhysicalAuthenticationPath);
           int physicalAuthenticationNodeListLength = physicalAuthenticationNodeList.getLength();
           if ((physicalAuthenticationNodeList != null) &&
        	   (physicalAuthenticationNodeListLength > 0)
        	  ) {
        	   hasPhysicalAuthentication = true;
        	   
        	   for (int j = 0; j < physicalAuthenticationNodeListLength; j++) {
        		   String methodText = null;
        		   Node physicalAuthenticationNode = physicalAuthenticationNodeList.item(j);
                   String hashString = physicalAuthenticationNode.getTextContent();
        		   NamedNodeMap paAttributes = physicalAuthenticationNode.getAttributes();
        		   int nAttributes = paAttributes.getLength();
        		   for (int k = 0; k < nAttributes; k++) {
        			   Node attributeNode = paAttributes.item(k);
        			   String nodeName = attributeNode.getNodeName();
        			   if (nodeName.equals("method")) {
        				   methodText = attributeNode.getNodeValue();
        			   }
        		   }
        		   
        		   integrityMap.put(methodText, hashString);
        	   }
        	   
           }
           
           // Store the entity access XML since some applications may need it
           Node entityPhysicalSizeNode = xpathapi.selectSingleNode(
                                              entityNode, 
                                              entityPhysicalSizePath);
           if (entityPhysicalSizeNode != null) {
               sizeString = entityPhysicalSizeNode.getTextContent();
    		   NamedNodeMap sizeAttributes = entityPhysicalSizeNode.getAttributes();
    		   int nAttributes = sizeAttributes.getLength();
    		   for (int k = 0; k < nAttributes; k++) {
    			   Node attributeNode = sizeAttributes.item(k);
    			   String nodeName = attributeNode.getNodeName();
    			   if (nodeName.equals("unit")) {
    				   unitString = attributeNode.getNodeValue();
    			   }
    		   }
               
           }
           
           NodeList onlineNodeList = xpathapi.selectNodeList(
                                              entityNode,
                                              "physical/distribution/online");
           NodeList offlineNodeList = xpathapi.selectNodeList(
                                              entityNode,
                                              "physical/distribution/offline");
           NodeList inlineNodeList = xpathapi.selectNodeList(
                                              entityNode,
                                              "physical/distribution/inline");
           if (onlineNodeList != null && onlineNodeList.getLength() > 0) {
             hasDistributionOnline = true;
           }
           if (offlineNodeList != null && offlineNodeList.getLength() > 0) {
             hasDistributionOffline = true;
           }
           if (inlineNodeList != null && inlineNodeList.getLength() > 0) {
             hasDistributionInline = true;
           }
           
           
           // Get the distribution information
           NodeList urlNodeList = xpathapi.selectNodeList(entityNode,
                                           "physical/distribution/online/url");
           
           if (urlNodeList != null && urlNodeList.getLength() > 0) {
             int len = urlNodeList.getLength();
             for (int j = 0; j < len; j++) {
               Node urlNode = urlNodeList.item(j);
               String urlText = urlNode.getTextContent();
               String functionText = null;
               NamedNodeMap urlAttributes = urlNode.getAttributes();
               int nAttributes = urlAttributes.getLength();
               for (int k = 0; k < nAttributes; k++) {
                 Node attributeNode = urlAttributes.item(k);
                 String nodeName = attributeNode.getNodeName();
                 if (nodeName.equals("function")) {
                   functionText = attributeNode.getNodeValue();             
                 }
               }
               
               /*
                * Unless this URL has attribute function="information", 
                * assign it as the download URL for this entity and stop
                * processing any additional distribution URLs.
                */
               if (functionText == null ||
                   !functionText.equalsIgnoreCase("information")) {
                 onlineUrl = urlText;
                 onlineUrlFunction = functionText;
                 break;
               }
             }
           }
                      
           /**
            * Determine file format (mime)
            * Note: this could be better fleshed out in cases where the delimiter is known
            * 
            * physical/dataFormat/textFormat
            * physical/dataFormat/binaryRasterFormat
            * physical/dataFormat/externallyDefinedFormat/formatName
            */
           NodeList formatNodeList = xpathapi.selectNodeList(entityNode, "physical/dataFormat/externallyDefinedFormat/formatName");
           if (formatNodeList != null && formatNodeList.getLength() > 0) {
        	   format = formatNodeList.item(0).getFirstChild().getNodeValue();
        	   isExternallyDefinedFormat = true;
           } else {
        	   // try binary raster
        	   formatNodeList = xpathapi.selectNodeList(entityNode, "physical/dataFormat/binaryRasterFormat");
               if (formatNodeList != null && formatNodeList.getLength() > 0) {
            	   format = "application/octet-stream";
               } else {
            	   formatNodeList = xpathapi.selectNodeList(entityNode, "physical/dataFormat/textFormat");
                   if (formatNodeList != null && formatNodeList.getLength() > 0) {
                	   format = "text/plain";
                   }
                   if (isSimpleDelimited) {
                	   format = "text/csv";
                   }
               }   
           }

           // Get the compressionMethod information
           NodeList compressionMethodNodeList = 
             xpathapi.selectNodeList(entityNode, "physical/compressionMethod");
           
           if (compressionMethodNodeList != null && 
               compressionMethodNodeList.getLength() >0
              ) {
              compressionMethod = 
                compressionMethodNodeList.item(0).getFirstChild().getNodeValue();
              
              if (compressionMethod != null && 
                  compressionMethod.equals(Entity.GZIP))
              {
                isGZipDataFile = true;
              }
              else if (compressionMethod != null && 
                       compressionMethod.equals(Entity.ZIP))
              {
                isZipDataFile = true;
              }
          }
          
          // Get encoding method info (mainly for tar file)
          NodeList encodingMethodNodeList = 
              xpathapi.selectNodeList(entityNode, "physical/encodingMethod");
          
          if (encodingMethodNodeList != null && 
              encodingMethodNodeList.getLength() > 0
             ) {
              encodingMethod = 
                encodingMethodNodeList.item(0).getFirstChild().getNodeValue();
            
              if (encodingMethod != null && encodingMethod.equals(Entity.TAR))
              {
                  isTarDataFile = true;
              }
          }

          if (entityOrientation.trim().equals("column")) {
              entityOrientation = Entity.COLUMNMAJOR;
          } else {
              entityOrientation = Entity.ROWMAJOR;
          }

          if (entityCaseSensitive.equals("yes")) {
              entityCaseSensitive = "true";
          } else {
              entityCaseSensitive = "false";
          }
          
          System.err.println(String.format("Package ID: %s  Entity: %s", packageId, entityName));

          entityObject = new Entity(id, 
                                    entityName == null ? null: entityName.trim(),
                                    entityDescription == null ? null: entityDescription.trim(), 
                                    new Boolean(entityCaseSensitive),
                                    entityOrientation, 
                                    new Integer(entityNumberOfRecords).intValue(),
                                    emlNamespace);
          
          entityObject.setExternallyDefinedFormat(isExternallyDefinedFormat);
          entityObject.setNumHeaderLines(numHeaderLines);
          entityObject.setNumFooterLines(numFooterLines);
          entityObject.setSimpleDelimited(isSimpleDelimited);
          entityObject.setTextFixed(isTextFixed);
            
          if (quoteCharacter != null) {
              entityObject.setQuoteCharacter(quoteCharacter);
          }
          
          if (literalCharacter != null) {
              entityObject.setLiteralCharacter(literalCharacter);
          }
          
          entityObject.setCollapseDelimiters(isCollapseDelimiters);         
          entityObject.setRecordDelimiter(recordDelimiter);
          entityObject.setURL(onlineUrl);
          entityObject.setURLFunction(onlineUrlFunction);
          entityObject.setDataFormat(format);
          entityObject.setCompressionMethod(compressionMethod);
          entityObject.setIsDataTableEntity(isDataTableEntity);
          entityObject.setIsImageEntity(isImageEntity);
          entityObject.setIsOtherEntity(isOtherEntity);
          entityObject.setHasGZipDataFile(isGZipDataFile);
          entityObject.setHasZipDataFile(isZipDataFile);
          entityObject.setHasTarDataFile(isTarDataFile);
          entityObject.setPackageId(packageId);
          entityObject.setHasDistributionOnline(hasDistributionOnline);
          entityObject.setHasDistributionOffline(hasDistributionOffline);
          entityObject.setHasDistributionInline(hasDistributionInline);
          entityObject.setEntityAccessXML(entityAccessXML);
          entityObject.setFieldDelimiter(fieldDelimiter);
          entityObject.setMetadataRecordDelimiter(metadataRecordDelimiter);        
          entityObject.setHasNumberOfRecords(hasNumberOfRecords);
          entityObject.setHasPhysicalAuthentication(hasPhysicalAuthentication);
          
          if (sizeString != null) { 
        	  entityObject.setPhysicalSize(sizeString);
          }
          
          if (unitString != null) {
        	  entityObject.setPhysicalSizeUnit(unitString);
          }
          entityObject.checkEntitySizePresence();
          
          /*
           * If any physical/authentication nodes were parsed, store the
           * integrity hash method and its associated hash value in the
           * entity object.
           */
          for (String hashMethod : integrityMap.keySet()) {
        	  String hashValue = integrityMap.get(hashMethod);
        	  entityObject.addPhysicalAuthentication(hashMethod, hashValue);
          }
          entityObject.checkIntegrityChecksumPresence();
          
          try {
              NodeList attributeListNodeList = 
                  xpathapi.selectNodeList(entityNode, "attributeList");
              processAttributeList(xpathapi, attributeListNodeList, xpath, entityObject);
              entityObject.setDataFormatArray(formatArray);  
          } catch (Exception e) {
                throw new Exception("Error parsing attributes: " + 
                                    e.getMessage(), e);
          }
          
          //entityHash.put(Integer.toString(elementId), entityObject);
          emlDataPackage.add(entityObject);
          //fileHash.put(elementId, onlineUrl); 
        } // end for loop
        
    }
    
    
    /**
     * This method will digest a text fixed data format node and return
     * a TextFixedDataFormat object.
     * 
     * @param  node the Node object to be processed
     */
    private TextWidthFixedDataFormat handleTextFixedDataFormatNode(Node node) 
            throws Exception
    {
       TextWidthFixedDataFormat textWidthFixedDataFormat = null;
       
       if (node == null)
       {
           return textWidthFixedDataFormat;
       }
       
       NodeList childNodes = node.getChildNodes();
       int length = childNodes.getLength();
       
       for (int i = 0; i < length; i++)
       {
           Node childNode = childNodes.item(i);
           String elementName = childNode.getNodeName();
           
           if (elementName != null && elementName.equals("fieldWidth"))
           {
              String fieldWidthStr = childNode.getFirstChild().getNodeValue();          
              int fieldWidth = (new Integer(fieldWidthStr)).intValue();
              
              textWidthFixedDataFormat = new TextWidthFixedDataFormat(fieldWidth);
           }
           else if (elementName != null && 
                    elementName.equals("fieldStartColumn") && 
                    textWidthFixedDataFormat != null)
           {
               String startColumnStr = childNode.getFirstChild().getNodeValue();
               int startColumn  = (new Integer(startColumnStr)).intValue();               
               textWidthFixedDataFormat.setFieldStartColumn(startColumn);
           }
           else if (elementName != null && 
                    elementName.equals("lineNumber") && 
                    textWidthFixedDataFormat != null)
           {
               String lineNumberStr = childNode.getFirstChild().getNodeValue();
               int lineNumber  = (new Integer(lineNumberStr)).intValue();
               textWidthFixedDataFormat.setLineNumber(lineNumber);
           }
       }
       
       return textWidthFixedDataFormat;
    }
    
    
    /*
     * This method will digest a complex delimited data format node 
     * and return a TextDelimitedDataFormat object.
     */
    private TextDelimitedDataFormat handleComplexDelimitedDataFormatNode(
                                                                      Node node)
            throws Exception
    {
       TextDelimitedDataFormat textDelimitedDataFormat = null;
       
       if (node == null)
       {
           return textDelimitedDataFormat;
       }
       
       NodeList childNodes = node.getChildNodes();
       int length = childNodes.getLength();
       Vector quoteList = new Vector();
       
       for (int i = 0; i < length; i++)
       {
           Node childNode = childNodes.item(i);
           String elementName = childNode.getNodeName();
           String fieldDelimiter = null;
           
           if (elementName != null && 
               elementName.equals("fieldDelimiter")
              ) {
             Node firstChild = childNode.getFirstChild();
             if (firstChild != null) {
               fieldDelimiter = firstChild.getNodeValue();
             }           
             textDelimitedDataFormat = new TextDelimitedDataFormat(fieldDelimiter);
           }
           else if (elementName != null && 
                    elementName.equals("lineNumber") && 
                    textDelimitedDataFormat != null)
           {
               String lineNumberStr = childNode.getFirstChild().getNodeValue();
               int lineNumber = (new Integer(lineNumberStr)).intValue();
               textDelimitedDataFormat.setLineNumber(lineNumber);
           }
           else if (elementName != null && 
                    elementName.equals("collapseDelimiters") && 
                    textDelimitedDataFormat != null)
           {
               String collapseDelimiters = 
                   childNode.getFirstChild().getNodeValue();
               textDelimitedDataFormat.
                   setCollapseDelimiters(collapseDelimiters);
           }
           else if (elementName != null && 
                    elementName.equals("quoteCharacter") && 
                    textDelimitedDataFormat != null)
           {
               String quoteCharacter = 
                   childNode.getFirstChild().getNodeValue();
               quoteList.add(quoteCharacter); 
           }
       } // end for loop
       
       // set up quoteList
       if (textDelimitedDataFormat != null)
       {
           int size = quoteList.size();
           String[] quoteCharacterArray = new String[size];
           
           for (int i = 0; i < size; i++)
           {
               quoteCharacterArray[i] = (String)quoteList.elementAt(i);
           }
           
           textDelimitedDataFormat.setQuoteCharacterArray(quoteCharacterArray);
       }
       
       return textDelimitedDataFormat;
    }
    
    /**
     * Returns an XML representation of the provided node.
     *  
     * @param node the node to be represented in XML.
     * 
     * @return a string containing an XML representation of the 
     * provided DOM node. 
     */
    public String nodeToXmlString(Node node) {
        
        try {
            
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            
            DOMSource source = new DOMSource(node);
            StreamResult result = new StreamResult(new StringWriter());

            t.transform(source, result);

            return result.getWriter().toString();

        } catch (TransformerException e) {
            throw new IllegalStateException(e);
        } catch (TransformerFactoryConfigurationError e) {
            throw new IllegalStateException(e);
        }

    }
    
    
    /*
     * Parses the dataset abstract text content for purposes of quality reporting.
     */
    private void parseDatasetAbstract(NodeList datasetAbstractNodeList) {
      if (datasetAbstractNodeList != null) {
        StringBuffer stringBuffer = new StringBuffer("");
        for (int i = 0; i < datasetAbstractNodeList.getLength(); i++) {
          Node paraNode = datasetAbstractNodeList.item(i);
          String paraText = paraNode.getTextContent();
          stringBuffer.append(" " + paraText);
        }
        String abstractText = stringBuffer.toString();
        emlDataPackage.setAbstract(abstractText);
        emlDataPackage.checkDatasetAbstract(abstractText);
      }      
    }
    
}
