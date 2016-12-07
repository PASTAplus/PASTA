/**
 *    '$RCSfile: Eml200Parser.java,v $'
 *
 *     '$Author: tao $'
 *       '$Date: 2007-01-22 22:04:02 $'
 *   '$Revision: 1.9 $'
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

package edu.lternet.pasta.dml.parser.eml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

//import org.apache.commons.logging.Log;
//import org.apache.commons.logging.LogFactory;
import org.apache.xpath.CachedXPathAPI;

//import org.kepler.objectmanager.data.DataType;
//import org.kepler.objectmanager.data.DataTypeResolver;
import edu.lternet.pasta.dml.parser.DataPackage;
import edu.lternet.pasta.dml.parser.DateTimeDomain;
import edu.lternet.pasta.dml.parser.Domain;
import edu.lternet.pasta.dml.parser.EnumeratedDomain;
import edu.lternet.pasta.dml.parser.NumericDomain;
import edu.lternet.pasta.dml.parser.Attribute;
import edu.lternet.pasta.dml.parser.AttributeList;
import edu.lternet.pasta.dml.parser.Entity;
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
 * @author tao
 * @deprecated Please use the generic.Eml200DataPackageParser class now
 */
public class Eml200Parser
{
    /*
     * Class fields
     */

    // constants
    public static final String DATATABLEENTITY = "//dataset/dataTable";
    public static final String SPATIALRASTERENTITY = "//dataset/spatialRaster";
    public static final String SPATIALVECTORENTITY = "//dataset/spatialVector";
    public static final String STOREDPROCEDUREENTITY = 
                                                    "//dataset/storedProcedure";
    public static final String VIEWENTITY = "//dataset/view";
    public static final String OTHERENTITY = "//dataset/otherEntity";
    private static final String PACKAGEIDPATH = "//eml/@packageId";

    // private static Log log;
    private static boolean isDebugging;
    private static final String ID = "id";
    private static final String TYPE_SYSTEM = "typeSystem";
 
    /*static {
      log = LogFactory.getLog( 
                   "org.ecoinformatics.seek.datasource.eml.eml2.Eml200Parser" );
      isDebugging = log.isDebugEnabled();
    }*/

    //private static String NAMESPACE = "eml://ecoinformatics.org/eml-2.0.0";

    
    /*
     * Instance fields
     */
    
    //private Hashtable entityHash = new Hashtable();
    //private Hashtable fileHash = new Hashtable();
    private int numEntities = 0;
    //private int numRecords = -1;
    private Entity entityObject = null;
    //private DataTypeResolver dtr = DataTypeResolver.instanceOf();
    private int elementId = 0;
    //private boolean hasImageEntity = false;
    private int numberOfComplexFormats = 0;
    // Associates attributeList id values with attributeList objects
    private Hashtable<String, AttributeList> attributeListIdHash = 
                                     new Hashtable<String, AttributeList>();
    //private boolean hasMissingValue = false;
    private DataPackage emlDataPackage = null;
    
    
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
    
    
    /**
     * Parses the EML package as InputSource object as input.
     * 
     * @param source The InputSource which contains metadata source
     */
    public void parse(InputSource source) throws Exception
    {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance()
                        .newDocumentBuilder();
        Document doc = builder.parse(source);
        parseDocument(doc);
    }

    
    /**
     * Parses the EML package as InputStream object as input.
     * 
     * @param is The InputStream which contains medadata source
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
        
        try {
        	// process packageid
        	Node packageIdNode = xpathapi.selectSingleNode(doc, PACKAGEIDPATH);
            
        	if (packageIdNode != null)
        	{
        	   //System.out.println("in packageIdNode is not null");
        	   packageId          = packageIdNode.getNodeValue();
        	}
            
        	emlDataPackage        = new DataPackage(packageId);
            // now dataTable, spatialRaster and spatialVector are handled
            dataTableEntities     = xpathapi.selectNodeList(doc, DATATABLEENTITY);
            spatialRasterEntities = 
                              xpathapi.selectNodeList(doc, SPATIALRASTERENTITY);
            spatialVectorEntities = 
                              xpathapi.selectNodeList(doc, SPATIALVECTORENTITY);
            otherEntities         = xpathapi.selectNodeList(doc, OTHERENTITY);
            viewEntities          = xpathapi.selectNodeList(doc, VIEWENTITY);
            
            
        } catch (Exception e) {
            throw new Exception(
                            "Error extracting entities from eml2.0.0 package.");
        }
        
        try {
            //log.debug("Processing entities");
            processEntities(xpathapi, dataTableEntities, DATATABLEENTITY, packageId);
            //TODO: current we still treat them as TableEntity java object, 
            //in future we need add new SpatialRasterEntity and SpatialVector
            // object for them
            processEntities(xpathapi, 
                            spatialRasterEntities, 
                            SPATIALRASTERENTITY, packageId);
            processEntities(xpathapi, 
                            spatialVectorEntities, 
                            SPATIALVECTORENTITY, packageId);
            processEntities(xpathapi, otherEntities, OTHERENTITY, packageId);
            processEntities(xpathapi, viewEntities, VIEWENTITY, packageId);
            //log.debug("Done processing entities");
        } catch (Exception e) {
            throw new Exception("Error processing entities: " + e.getMessage());
        }
    }

    
    /**
     * Returns a hashtable of entity names hashed to the entity description
     * metadata that goes with each entity.
     */
    /*public Hashtable getEntityHash()
    {
        return entityHash;
    }*/
    
    
    /**
     * Method to get the data package metadata object.
     * 
     * @return the value of the emlDataPackage field, a DataPackage
     */
    public DataPackage getDataPackage()
    {
    	return emlDataPackage;
    }

    
    /**
     * Gets the number of records in this dataItem.
     *
     * @param   entityId the id of the entity object to get the record count for
     * @return  the number of records in the entity object
     */
    /*public int getRecordCount(String entityId)
    {
        return ((Entity) entityHash.get(entityId)).getNumRecords();
    }*/

    
    /**
     * Gets the total number of entities in the data item collection that was
     * passed to this class when the object was created.
     * 
     * @return  the number of entities in the data item collection
     */
    /*public int getEntityCount()
    {
        return numEntities;
    }*/

    
    /**
     * Gets the number of attributes in the given entity.
     *
     * @param  entityId the id of the entity object that you want the attribute
     *         count for
     * @return the number of attributes in the entity
     */
    /*public int getAttributeCount(String entityId)
    {
        Attribute[] attArray = ((Entity) entityHash.get(entityId))
                        .getAttributes();
        return attArray.length;
    }*/
    
    
    /**
     * Boolean to determine whether the entity has a missing value declaration.
     * 
     * @return value of hasMissingValue, a boolean
     */
    /*public boolean hasMissingValue()
    {
    	return hasMissingValue;
    }*/
    
    
    /**
     * Method to get the boolean hasImageEntity. If the eml document has
     * SpatialRaster or SpatialVector entity, this variable should be true.
     * 
     * @return boolean, the value of the hasImageEntity field
     */
    /*public boolean getHasImageEntity()
    {
      return this.hasImageEntity;
      
    }*/
    
    
    /**
     * Processes the attributeList element.
     * 
     * @param  xpathapi  XPath API
     * @param  attributeListNodeList   a NodeList
     * @param  entObj    the entity object whose attribute list is processed
     */
    private void processAttributeList(CachedXPathAPI xpathapi, 
                                      NodeList attributeListNodeList, 
                                      Entity entObj) 
            throws Exception
    {
        AttributeList attributeList = new AttributeList();
        Node attributeListNode = attributeListNodeList.item(0);
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
            processAttributes(xpathapi, attributeNodeList, attributeList);
            
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
     */
    private void processAttributes(CachedXPathAPI xpathapi, 
                                   NodeList attributesNodeList, 
                                   AttributeList attributeList)
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
                
                if (childNodeName.equals("attributeName")) {
                    attName = childNode.getFirstChild().getNodeValue()
                                   .trim().replace('.', '_');
                } 
                else if (childNodeName.equals("attributeLabel")) {
                    attLabel = childNode.getFirstChild().getNodeValue().trim();
                } 
                else if (childNodeName.equals("attributeDefinition")) {
                    attDefinition = childNode.getFirstChild().getNodeValue().trim();
                }
                // Process storageType elements
                else if (childNodeName.equals("storageType")) {
                  String storageTypeTextValue = 
                      childNode.getFirstChild().getNodeValue().trim();
                  NamedNodeMap storageTypeAttributesMap = childNode.getAttributes();
                  StorageType storageType;
                  String typeSystem = "";
                  Node typeSystemNode = null;
                  
                  // Determine whether the typeSystem attribute was specified
                  if (storageTypeAttributesMap != null) {
                      typeSystemNode =  
                          storageTypeAttributesMap.getNamedItem(TYPE_SYSTEM);
                        
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
                                    String definition = 
                                      defintionNode.
                                      getFirstChild().
                                      getNodeValue();
                                	
                                    if(isDebugging) {
                                      //log.debug(
                                      // "The definition value is "+definition);
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
                            DateTimeDomain date = new DateTimeDomain();
                            String formatString = 
                              (xpathapi.selectSingleNode(measurementScaleChildNode,
                                                         "./formatString")).
                                          getFirstChild().
                                          getNodeValue();
                            
                        	  if (isDebugging) {
                        	    //log.debug(
                              //          "The format string in date time is " 
                              //          + formatString);
                        	  }
                            date.setFormatString(formatString);
                            domain = date;
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
                                 String packageId)
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
        String entityName = "";
        String entityDescription = "";
        String entityOrientation = "";
        String entityCaseSensitive = "";
        String entityNumberOfRecords = "-1";
        String onlineUrl = "";
        String numHeaderLines = "0";
        int numFooterLines = 0;
        String fieldDelimiter = null;
        String recordDelimiter = "";
        String compressionMethod = "";
        String encodingMethod = "";
        String quoteCharacter = null;
        String literalCharacter = null;
        boolean isImageEntity   = false;
        boolean isGZipDataFile  = false;
        boolean isZipDataFile   = false;
        boolean isTarDataFile   = false;
        boolean isSimpleDelimited = true;
        boolean isCollapseDelimiters = false;
        TextComplexDataFormat[] formatArray = null;
         
        for (int i = 0; i < entityNodeListLength; i++) {
            
            if (xpath != null && (xpath.equals(SPATIALRASTERENTITY) 
                                  || xpath.equals(SPATIALVECTORENTITY)))
            {
              isImageEntity = true;
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

                if (childName.equals("entityName")) {
                    entityName = childNode.getFirstChild().getNodeValue();
                } else if (childName.equals("entityDescription")) {
                    entityDescription = childNode.getFirstChild().getNodeValue();
                } else if (childName.equals("caseSensitive")) {
                    entityCaseSensitive = childNode.getFirstChild().getNodeValue();
                } else if (childName.equals("numberOfRecords")) {
                    entityNumberOfRecords = childNode.getFirstChild()
                                    .getNodeValue();
                    /*numRecords = (new Integer(entityNumberOfRecords))
                                    .intValue();*/
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
                    numHeaderLines = 
                        numHeaderLinesNode.getFirstChild().getNodeValue();
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
                        (new Integer(numFooterLinesStr.trim())).intValue();
                }
            }
           
           // Here is the simple delimited data file
           NodeList fieldDelimiterNodeList = 
               xpathapi.selectNodeList(
                 entityNode,
                 "physical/dataFormat/textFormat/simpleDelimited/fieldDelimiter"
                                      );
           
           if (fieldDelimiterNodeList != null && 
               fieldDelimiterNodeList.getLength() >0
              ) {
               fieldDelimiter = 
                 fieldDelimiterNodeList.item(0).getFirstChild().getNodeValue();
           }
           
           NodeList collapseDelimitersNodeList = 
             xpathapi.selectNodeList(entityNode,
               "physical/dataFormat/textFormat/simpleDelimited/collapseDelimiters");
           
           if (collapseDelimitersNodeList != null && 
               collapseDelimitersNodeList.getLength() > 0
              ) {
             
               String collapseDelimiters = 
                   collapseDelimitersNodeList.item(0).getFirstChild().getNodeValue();
               
               if (collapseDelimiters.equalsIgnoreCase("yes"))
               {
                   isCollapseDelimiters = true;
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
           }
           
           // For complex format data file
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
                 
                 if (complexChildNode != null && 
                     complexChildNode.getNodeName().equals("textFixed")
                    )
                 {
                     TextWidthFixedDataFormat textWidthFixedDataFormat = 
                         handleTextFixedDataFormatNode(complexChildNode);
                     
                     if (textWidthFixedDataFormat != null)
                     {
                        formatVector.add(textWidthFixedDataFormat);
                        //complexFormatsNumber++;
                     }
                 }
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
           }
           
           NodeList recordDelimiterNodeList = 
               xpathapi.selectNodeList(entityNode,
                             "physical/dataFormat/textFormat/recordDelimiter");
           
           if ((recordDelimiterNodeList != null) && 
               (recordDelimiterNodeList.getLength() > 0)
              ) {
              recordDelimiter = 
                recordDelimiterNodeList.item(0).getFirstChild().getNodeValue();
           } else {
              recordDelimiter = "\\r\\n";
           }
           
           // Get the distribution information
           NodeList urlNodeList = xpathapi.selectNodeList(entityNode,
                                           "physical/distribution/online/url");
           
           if (urlNodeList != null && urlNodeList.getLength() >0)
           {
               onlineUrl = urlNodeList.item(0).getFirstChild().getNodeValue();
              
          	   if(isDebugging) {
        		       //log.debug("The url is "+ onlineUrl);
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
              
          	  if (isDebugging) {
                  //log.debug("Compression method is "+compressionMethod);
        	    }
              
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
            
        	    if (isDebugging) {
        		      //log.debug("encoding method is "+encodingMethod);
        	    }
            
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

          entityObject = new Entity(id, 
                                    entityName.trim(),
                                    entityDescription.trim(), 
                                    new Boolean(entityCaseSensitive),
                                    entityOrientation, 
                                    new Integer(entityNumberOfRecords).
                                                           intValue());
          
          entityObject.setNumHeaderLines((new Integer(numHeaderLines))
                                                         .intValue());
          entityObject.setNumFooterLines(numFooterLines);
          entityObject.setSimpleDelimited(isSimpleDelimited);
          
          // For simple delimited data file
          if (fieldDelimiter != null)
          {
             entityObject.setFieldDelimiter(fieldDelimiter);
          }
          
          if (quoteCharacter != null)
          {
        	  entityObject.setQuoteCharacter(quoteCharacter);
          }
          
          if (literalCharacter != null)
          {
        	  entityObject.setLiteralCharacter(literalCharacter);
          }
          
          entityObject.setCollapseDelimiters(isCollapseDelimiters);         
          entityObject.setRecordDelimiter(recordDelimiter);
          entityObject.setURL(onlineUrl);
          entityObject.setCompressionMethod(compressionMethod);
          entityObject.setIsImageEntity(isImageEntity);
          entityObject.setHasGZipDataFile(isGZipDataFile);
          entityObject.setHasZipDataFile(isZipDataFile);
          entityObject.setHasTarDataFile(isTarDataFile);
          entityObject.setPackageId(packageId);
            
          try {
              NodeList attributeListNodeList = 
                  xpathapi.selectNodeList(entityNode, "attributeList");
              processAttributeList(xpathapi, attributeListNodeList, entityObject);
              entityObject.setDataFormatArray(formatArray);  
          } catch (Exception e) {
                throw new Exception("Error parsing attributes: " + 
                                    e.getMessage());
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
              
          	  if (isDebugging) {
        		    //log.debug("The filed width for fix width in eml is "
                //          + fieldWidth);
        	    }
              
              textWidthFixedDataFormat = new TextWidthFixedDataFormat(fieldWidth);
           }
           else if (elementName != null && 
                    elementName.equals("fieldStartColumn") && 
                    textWidthFixedDataFormat != null)
           {
               String startColumnStr = childNode.getFirstChild().getNodeValue();
               int startColumn  = (new Integer(startColumnStr)).intValue();
               
           	   if (isDebugging) {
        		     //log.debug("The start column is " + startColumn);
        	     }
               
               textWidthFixedDataFormat.setFieldStartColumn(startColumn);
           }
           else if (elementName != null && 
                    elementName.equals("lineNumber") && 
                    textWidthFixedDataFormat != null)
           {
               String lineNumberStr = childNode.getFirstChild().getNodeValue();
               int lineNumber  = (new Integer(lineNumberStr)).intValue();
               
           	   if (isDebugging) {
        		     //log.debug("The start column is " + lineNumber);
        	     }
               
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
           
           if (elementName != null && elementName.equals("fieldDelimiter"))
           {
              String fieldDelimiter = childNode.getFirstChild().getNodeValue();
              
          	  if (isDebugging) {
        		    //log.debug("The field delimiter for complex format in eml is " +
                //          fieldDelimiter);
        	    }
              
              textDelimitedDataFormat = new TextDelimitedDataFormat(fieldDelimiter);
           }
           else if (elementName != null && 
                    elementName.equals("lineNumber") && 
                    textDelimitedDataFormat != null)
           {
               String lineNumberStr = childNode.getFirstChild().getNodeValue();
               int lineNumber = (new Integer(lineNumberStr)).intValue();
               
           	   if (isDebugging) {
        		     //log.debug("The line number is " + lineNumber);
        	     }
               
               textDelimitedDataFormat.setLineNumber(lineNumber);
           }
           else if (elementName != null && 
                    elementName.equals("collapseDelimiters") && 
                    textDelimitedDataFormat != null)
           {
               String collapseDelimiters = 
                   childNode.getFirstChild().getNodeValue();
               
           	   if (isDebugging) {
        		     //log.debug("The collapse delimiter: " + collapse);
        	     }
               
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
}
