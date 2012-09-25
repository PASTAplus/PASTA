/*
 *
 * $Date$
 * $Author$
 * $Revision$
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

package edu.lternet.pasta.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.XmlUtility;

public class TestXmlUtility {

    // Note: These tests are fairly weak
    
    private String xmlHeader;
    private String emlString;
    private String xmlString;
    private Document doc;
    private Element emlNode;
    
    @Before
    public void init() {
        
        xmlHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        emlString = "<eml packageId=\"test.1.2\">stuff</eml>";
        xmlString = xmlHeader + "\n" + emlString; 
        
        try {
            
            DocumentBuilder builder = 
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            doc = builder.newDocument();
        
            emlNode = doc.createElement("eml");
            emlNode.setAttribute("packageId", "test.1.2");
            emlNode.setTextContent("stuff");
            doc.appendChild(emlNode);
            
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Test
    public void testNodeToXmlString() {
        assertTrue(XmlUtility.nodeToXmlString(doc).contains(emlString));
    }
    
    @Test
    public void testGetEmptyDoc() {
        Document doc = XmlUtility.getEmptyDoc();
        assertEquals(0, doc.getChildNodes().getLength());
    }
    
    @Test
    public void testXmlStringToDoc() {
        
        Document parsed = XmlUtility.xmlStringToDoc(xmlString);
        Node parsedEml = parsed.getFirstChild();
        NamedNodeMap attrs = parsedEml.getAttributes();
        String parsedId = attrs.getNamedItem("packageId").getTextContent();
        
        assertEquals("eml", parsedEml.getNodeName());
        assertEquals(1, parsedEml.getChildNodes().getLength());
        assertEquals("stuff", parsedEml.getTextContent());
        
        assertEquals(1, attrs.getLength());
        assertEquals("test.1.2", parsedId);
    }
    
    private Schema getSchema() {
        String f = TestFileUtility.makeFileName("eml-subscription.xsd");
        String schema = FileUtility.fileToString(f);
        return XmlUtility.xmlStringToSchema(schema);
    }
    
    @Test
    public void testXmlStringToDocWithSchema() {
        String f = TestFileUtility.makeFileName("test_eml_subscription.xml");
        String xml = FileUtility.fileToString(f);
        Schema s = getSchema();
        Document d = XmlUtility.xmlStringToDoc(xml, s);
        
        assertEquals(1, d.getChildNodes().getLength());
        assertEquals("subscription", d.getChildNodes().item(0).getNodeName());
    }
    
    @Test
    public void testXmlStringToSchema() {
        Schema s = getSchema();
        assertNotNull(s);
    }
    
    @Test
    public void testXmlStringToDocumentParserError() {
        
        String xml = "<error></blah>";
        
        try {
            XmlUtility.xmlStringToDoc(xml);
        } catch (XmlParsingException e) {
            assertEquals(xml, e.getXml());
        }
    }
    
    @Test
    public void testXmlStringToDocumentWithSchemaParserError() {
        
        Schema schema = getSchema();
        String xml = "<error></blah>";
        
        try {
            XmlUtility.xmlStringToDoc(xml, schema);
        } catch (XmlParsingException e) {
            assertEquals(xml, e.getXml());
        }
    }
    
    @Test
    public void testXmlStringToSchemaParserError() {
        
        String xml = "<error></blah>";
        
        try {
            XmlUtility.xmlStringToSchema(xml);
        } catch (XmlParsingException e) {
            assertEquals(xml, e.getXml());
        }
    }
}
