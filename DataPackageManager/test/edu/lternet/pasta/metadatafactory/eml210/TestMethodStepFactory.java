/*
 *
 * $Date: 2012-02-06 15:33:02 -0700 (Mon, 06 Feb 2012) $
 * $Author: jmoss $
 * $Revision: 1634 $
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

package edu.lternet.pasta.metadatafactory.eml210;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import javax.xml.validation.Schema;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.metadatafactory.eml210.MethodStepFactory;
import edu.lternet.pasta.metadatafactory.eml210.ParentEml;

public class TestMethodStepFactory {

    private String parentEmlFileName;
    private Document doc;
    private ParentEml parent;
    private MethodStepFactory factory;
    
    private void readParentEml(String fileName) throws Exception {
        parentEmlFileName = fileName;
        doc = parseEml(parentEmlFileName);
        parent = new ParentEml(doc);
    }
    
    private Document parseEml(String fileName) throws Exception {
        fileName = "test/data/provenance/" + fileName;
        File file = FileUtility.assertCanRead(fileName);
        return XmlUtility.xmlStringToDoc(FileUtility.fileToString(file));
    }
    
    private NodeList getNodeList(String xpath, Document eml) throws Exception {
        XPath x = XPathFactory.newInstance().newXPath();
        return (NodeList) x.evaluate(xpath, eml, XPathConstants.NODESET);
    }
    
    private NodeList getNodesFromParent(String nodeName, Document parent) 
        throws Exception {
        return getNodeList("//dataset/" + nodeName, parent);
    }
    
    private NodeList getNodesFromChild(String nodeName, Document child) 
        throws Exception {
        String xpath = "//dataset/methods/methodStep/dataSource/" + nodeName;
        return getNodeList(xpath, child);
    }
    
    private boolean equalNodes(Document child, Document parent, String nodeName)
        throws Exception {
        
        NodeList parentNodes = getNodesFromParent(nodeName, parent);
        NodeList childNodes = getNodesFromChild(nodeName, child);

        if (parentNodes.getLength() != childNodes.getLength()) {
            return false;
        }
        
        for (int i = 0; i < parentNodes.getLength(); i ++) {
            
            if (!contains(parentNodes.item(i).getTextContent(), childNodes)) {
                return false;
            }
            
        }
        
        return true;
    }
    
    private boolean contains(String text, NodeList list) {

        for (int i = 0; i < list.getLength(); i ++) {
            if (list.item(i).getTextContent().equals(text)) {
                return true;
            }
        }
        
        return false;
    }

    
    
    
    @Before
    public void init() {
        factory = new MethodStepFactory();
    }
    
    @Test(expected=XmlParsingException.class)
    public void testAppendWithNonexistentMethodsNode() throws Exception {

        readParentEml("test_2.xml");
        
        // Does not contain an /eml/dataset/methods node
        Document xmlWithoutMethodsNode = parseEml("test_1.xml");
        
        List<String> entityNames = parent.getDataTableEntityNames();
        
        factory.append(xmlWithoutMethodsNode, parent, entityNames);
    }

    @Test(expected=XmlParsingException.class )
    public void testAppendWithNonexistentEntityName() throws Exception {

        readParentEml("test_2.xml");
        Document childEml = parseEml("test_2.xml");
        
        List<String> entityNames = new LinkedList<String>();
        entityNames.add("nonexistent_data_table_id...surely");
        
        factory.append(childEml, parent, entityNames);
    }
    
    @Test(expected=XmlParsingException.class)
    public void testAppendWithNullEntityName() throws Exception {

        readParentEml("test_2.xml");
        Document childEml = parseEml("test_2.xml");
        
        List<String> entityNames = new LinkedList<String>();
        entityNames.add(null);
        
        factory.append(childEml, parent, entityNames);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendWithNullEntityNames() throws Exception {

        readParentEml("test_2.xml");
        Document childEml = parseEml("test_2.xml");
        
        factory.append(childEml, parent, null);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendWithNullDocument() throws Exception {

        readParentEml("test_2.xml");
        
        factory.append(null, parent, new LinkedList<String>());
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testAppendWithNullParent() throws Exception {

        Document childEml = parseEml("test_2.xml");
        
        factory.append(childEml, null, new LinkedList<String>());
    }
    
    
    @Test
    public void testAppendForAlteredChildEml() throws Exception {

        readParentEml("test_4.xml");
        
        // Contains an /eml/dataset/methods node
        Document emlWithMethodsNode = parseEml("test_4.xml");
        
        // Using same EML for parent and child for convenience
        assertFalse(doc.isSameNode(emlWithMethodsNode));
        assertTrue(doc.isEqualNode(emlWithMethodsNode));
        
        List<String> entityNames = parent.getDataTableEntityNames();
        
        factory.append(emlWithMethodsNode, parent, entityNames);
        
        // Asserting a change has occurred in the child
        assertFalse(doc.isEqualNode(emlWithMethodsNode));
    }
    
    @Test
    public void testAppendFromParentWithSingleNodes() throws Exception {
        
        // Using same XML for parent and child for convenience
        readParentEml("test_4.xml");
        Document child = parseEml("test_4.xml");
        
        assertFalse(doc.isSameNode(child));
        assertTrue(doc.isEqualNode(child));
        
        assertFalse(equalNodes(doc, child, "title"));
        assertFalse(equalNodes(doc, child, "creator"));
        assertFalse(equalNodes(doc, child, "contact"));

        List<String> entityNames = parent.getDataTableEntityNames();
        
        factory.append(child, parent, entityNames);
        
        assertTrue(equalNodes(child, doc, "title"));
        assertTrue(equalNodes(child, doc, "creator"));
        assertTrue(equalNodes(child, doc, "contact"));
    }
    
    @Test
    public void testAppendWithEmlSchemaValidation() throws Exception {

        // Parsing EML schema
        String fileName = "test/data/eml-schema/eml.xsd";
        String schemaString = FileUtility.fileToString(fileName);
        Schema schema = XmlUtility.xmlStringToSchema(schemaString);
        
        // Parsing parent EML
        fileName = "test/data/provenance/NoneSuchBugCount.xml";
        String emlString = FileUtility.fileToString(fileName);
        Document eml = XmlUtility.xmlStringToDoc(emlString, schema);
        parent = new ParentEml(eml);

        // Parsing EML to be appended to (using same for convenience)
        emlString = FileUtility.fileToString(fileName);
        Document child = XmlUtility.xmlStringToDoc(emlString);

        // Appending provenance
        List<String> entityNames = parent.getDataTableEntityNames();
        factory.append(child, parent, entityNames);
        
        // Checking result against EML schema
        String appended = XmlUtility.nodeToXmlString(child);
        //System.out.println(appended);
        XmlUtility.xmlStringToDoc(appended, schema);
    }
}
