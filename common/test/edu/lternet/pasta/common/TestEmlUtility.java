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

import static org.junit.Assert.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.IllegalEmlPackageIdException;

public class TestEmlUtility {

    private Document goodDoc;
    private Document docWithNullPackageId;
    private Document docWithoutPackageIdAttr;
    
    @Before
    public void init() {
        
        try {
            
            DocumentBuilder builder = 
                DocumentBuilderFactory.newInstance().newDocumentBuilder();
            
            goodDoc = builder.newDocument();
        
            Element emlNode = goodDoc.createElement("eml");
            emlNode.setAttribute("packageId", "test.1.2");
            emlNode.setTextContent("stuff");
            goodDoc.appendChild(emlNode);

            docWithNullPackageId = builder.newDocument();
            emlNode = (Element) docWithNullPackageId.importNode(emlNode, true);
            emlNode.setAttribute("packageId", null);
            
            docWithoutPackageIdAttr = builder.newDocument();
            emlNode = docWithoutPackageIdAttr.createElement("eml");
            docWithoutPackageIdAttr.adoptNode(emlNode);
            
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }
    
    @Test
    public void testGetRawEmlPackageId() {
        String packageId = EmlUtility.getRawEmlPackageId(goodDoc);
        assertEquals("test.1.2", packageId);
    }
    
    @Test
    public void testGetEmlPackageId() {
        EmlPackageId packageId = EmlUtility.getEmlPackageId(goodDoc);
        assertEquals("test", packageId.getScope());
        assertEquals(1, packageId.getIdentifier().intValue());
        assertEquals(2, packageId.getRevision().intValue());
    }
    
    @Test
    public void testGetRawEmlPackageIdWithEmpty() {
        String packageId = EmlUtility.getRawEmlPackageId(docWithNullPackageId);
        assertEquals("", packageId);
    }

    @Test(expected=IllegalEmlPackageIdException.class)
    public void testGetEmlPackageIdWithEmpty() {
        EmlPackageId packageId = EmlUtility.getEmlPackageId(docWithNullPackageId);
        assertNull(packageId.getScope());
        assertNull(packageId.getIdentifier());
        assertNull(packageId.getRevision());
    }

    @Test
    public void testGetRawEmlPackageIdWithoutAttr() {
        String packageId = 
            EmlUtility.getRawEmlPackageId(docWithoutPackageIdAttr);
        assertEquals("", packageId);
    }
    
    @Test(expected=IllegalEmlPackageIdException.class)
    public void testGetEmlPackageIdWithoutAttr() {
        EmlUtility.getEmlPackageId(docWithoutPackageIdAttr);
    }
    
}
