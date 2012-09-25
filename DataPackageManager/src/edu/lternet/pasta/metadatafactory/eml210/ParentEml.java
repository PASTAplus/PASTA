/*
 *
 * $Date: 2011-02-10 13:45:37 -0700 (Thu, 10 Feb 2011) $
 * $Author: jwright $
 * $Revision: 696 $
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

import java.util.LinkedList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlUtility;

/**
 * Represents a "parent" EML document. A "parent" EML document
 * <em>E<sub>0</sub></em> describes a data set <em>D<sub>0</sub></em> that was
 * used to produce a new data set <em>D<sub>1</sub></em>. A new EML document
 * <em>E<sub>1</sub></em> must be produced to describe <em>D<sub>1</sub></em>,
 * and some elements of <em>E<sub>0</sub></em> should be included as provenance
 * in <em>E<sub>1</sub></em>. This class provides accessor methods for those
 * elements.
 * 
 */
public final class ParentEml {

    // XPath expressions
    private static final String TITLE = "//dataset/title";
    private static final String CREATOR = "//dataset/creator";
    private static final String CONTACT = "//dataset/contact";
    private static final String DATA_TABLE_ID = "//dataset/dataTable[@id]";
    private static final String ENTITY_NAME = "//dataset/dataTable/entityName";
    private static final String ID = "id";

    private final XPath xpath;
    private final Document eml;
    
    /**
     * Constructs a new parent EML document object.
     * 
     * @param eml the parent EML document.
     */
    public ParentEml(Document eml) {

        if (eml == null) {
            throw new IllegalArgumentException("null EML");
        }
        
        this.eml = eml;
        xpath = XPathFactory.newInstance().newXPath();

    }
    
    /**
     * Returns a hash code value.
     * 
     * @return a hash code value.
     */
    @Override
    public int hashCode() {
        return eml.hashCode();
    }

    /**
     * Returns the entire parent EML document.
     * 
     * @return the entire parent EML document.
     */
    public Document getDocument() {
        return eml;
    }

    /**
     * Returns the {@code //@packageId} attribute of this parent EML document.
     * 
     * @return the {@code //@packageId} attribute of this parent EML document.
     */
    public EmlPackageId getPackageId() {
        return EmlUtility.getEmlPackageId(eml);
    }
    
    /**
     * Returns a list of all {@code //dataset/title} elements contained in this
     * parent EML document.
     * 
     * @return a list of all {@code //dataset/title} elements contained in this
     *         parent EML document.
     */
    public List<Node> getTitles() {
        return evaluate(TITLE);
    }

    /**
     * Returns a list of all {@code //dataset/creator} elements contained in
     * this parent EML document.
     * 
     * @return a list of all {@code //dataset/creator} elements contained in
     *         this parent EML document.
     */
    public List<Node> getCreators() {
        return evaluate(CREATOR);
    }

    /**
     * Returns a list of all {@code //dataset/contact} elements contained in
     * this parent EML document.
     * 
     * @return a list of all {@code //dataset/contact} elements contained in
     *         this parent EML document.
     */
    public List<Node> getContacts() {
        return evaluate(CONTACT);
    }

    /**
     * Returns a list of all {@code //dataset/dataTable/@id} attribute values
     * contained in this parent EML document.
     * 
     * @return a list of all {@code //dataset/dataTable/@id} attribute values
     *         contained in this parent EML document.
     */
    public List<String> getDataTableIds() {
        
        List<String> tableIds = new LinkedList<String>();

        for (Node table : evaluate(DATA_TABLE_ID)) {
            Node id = table.getAttributes().getNamedItem(ID);
            tableIds.add(id.getTextContent());
        }

        return tableIds;
    }

    /**
     * Returns a list of values of all {@code //dataset/dataTable/entityName}
     * elements contained in this parent EML document.
     * 
     * @return a list of values of all {@code //dataset/dataTable/entityName}
     *         elements contained in this parent EML document.
     */
    public List<String> getDataTableEntityNames() {
        
        List<String> entityNames = new LinkedList<String>();
         
        for (Node n : evaluate(ENTITY_NAME)) {
            entityNames.add(n.getTextContent());
        }
        
        return entityNames;
    }

    private List<Node> evaluate(String xpathExpr) {

        NodeList result = null;

        try {
            result = (NodeList) 
                xpath.evaluate(xpathExpr, eml, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e);
        }

        List<Node> nodes = new LinkedList<Node>();

        for (int i = 0, size = result.getLength(); i < size; i++) {
            nodes.add(result.item(i));
        }

        return nodes;
    }

}
