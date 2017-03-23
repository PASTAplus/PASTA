/*
 *
 * $Date: 2012-07-24 14:37:49 -0700 (Tue, 24 Jul 2012) $
 * $Author: dcosta $
 * $Revision: 2255 $
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

import java.util.Iterator;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.lternet.pasta.metadatafactory.eml210.ParentEml;

/**
 * Appends {@code //dataset/methods/methodStep} elements to provided
 * {@code org.w3c.dom.Document} objects to record provenance information.
 *
 */
public final class MethodStepFactory {

    // Relevant EML tag names
    private static final String METHOD_STEP = "methodStep";
    private static final String DATA_SOURCE = "dataSource";
    private static final String DESCRIPTION = "description";
    private static final String PARA = "para";
    private static final String DISTRIBUTION = "distribution";
    private static final String ONLINE = "online";
    private static final String ONLINE_DESCRIPTION = "onlineDescription";
    private static final String URL = "url";

    /**
     * Appends {@code methodStep} elements to the provided XML document. These
     * elements are appended to the {@code //methods} element.
     *
     * @param xml
     *            the XML document to which elements will be appended.
     *
     * @param parent
     *            the parent EML document from which provenance metatdata will
     *            be obtained.
     *
     * @param entityNames
     *            the list of entity names ({@code
     *            /eml/dataset/dataTable/entityName}) to be included in
     *            provenance metadata.
     *
     * @throws IllegalArgumentException
     *             if any of the provided arguments are {@code null};
     *
     * @throws XmlParsingException
     *             if the provided XML document does not have one and only one
     *             {@code //methods} element; or if a provided entity name does
     *             not exist in the provided parent EML.
     */
    public void append(Document xml,
                       ParentEml parent,
                       List<String> entityNames) {

        checkArgs(xml, parent, entityNames);

        Node methods = getMethodsNode(xml);

        Element methodStep = xml.createElement(METHOD_STEP);
        methods.insertBefore(methodStep, methods.getFirstChild());

        // Do not change the order of these method invocations.
        // They must satisfy the sequence order of the EML schema.
        appendDataSource(xml, parent, methodStep);
        appendDescription(xml, entityNames, methodStep);
    }

    
    /**
     * Appends {@code methodStep} elements to the provided XML document. These
     * elements are appended to the {@code //methods} element.
     *
     * @param xml
     *            the XML document to which elements will be appended.
     *
     * @param parent
     *            the parent EML document from which provenance metatdata will
     *            be obtained.
     *
     * @param entityNames
     *            the list of entity names ({@code
     *            /eml/dataset/dataTable/entityName}) to be included in
     *            provenance metadata.
     *
     * @throws IllegalArgumentException
     *             if any of the provided arguments are {@code null};
     *
     * @throws XmlParsingException
     *             if the provided XML document does not have one and only one
     *             {@code //methods} element; or if a provided entity name does
     *             not exist in the provided parent EML.
     */
    public void appendMethodStepMetadata(Document xml,
                       ParentEml parent,
                       List<String> entityNames) {

        checkArgs(xml, parent, entityNames);
        Element methodStep = xml.getDocumentElement();
        
        // Do not change the order of these method invocations.
        // They must satisfy the sequence order of the EML schema.
        appendDataSource(xml, parent, methodStep);
        appendDescription(xml, entityNames, methodStep);
    }

    
    private void checkArgs(Document doc, ParentEml parent, List<String> names) {

        if (doc == null) {
            throw new IllegalArgumentException("null document");
        }

        if (parent == null) {
            throw new IllegalArgumentException("null parent");
        }

        if (names == null) {
            throw new IllegalArgumentException("null entity names");
        }

        List<String> parentNames = parent.getDataTableEntityNames();

        for (String providedEntityName : names) {

            if (!parentNames.contains(providedEntityName)) {

                EmlPackageIdFormat f = new EmlPackageIdFormat(Delimiter.DOT);
                StringBuilder sb = new StringBuilder();

                sb.append("The provided entityName '");
                sb.append(providedEntityName);
                sb.append("' is not present in the EML document ");
                sb.append("with the packageId '");
                sb.append(f.format(parent.getPackageId()));
                sb.append("'.");

                throw new XmlParsingException(sb.toString(), doc);
            }
        }

    }

    private void appendDataSource(Document emlDoc,
                                  ParentEml parentEml,
                                  Node methodStep) {

        Element dataSource = emlDoc.createElement(DATA_SOURCE);
        methodStep.appendChild(dataSource);

        // Do not change the order of these method invocations.
        // They must satisfy the sequence order of the EML schema.

        // Appending nodes from parent EML document to dataSource

        for (Node contact : parentEml.getContacts()) {
            contact = emlDoc.adoptNode(contact.cloneNode(true));
            /*
             * If the contact is for the LTER Network Office, exclude it from
             * the provenance metadata since it is no longer in operation.
             */
            boolean isLNOContact = isLNOContact(contact);
            if (!isLNOContact) {
                dataSource.appendChild(contact);
            }
        }

        appendDistribution(emlDoc, parentEml, dataSource);

        for (Node creator : parentEml.getCreators()) {
            creator = emlDoc.adoptNode(creator.cloneNode(true));
            dataSource.insertBefore(creator, dataSource.getFirstChild());
        }

        for (Node title : parentEml.getTitles()) {
            title = emlDoc.adoptNode(title.cloneNode(true));
            dataSource.insertBefore(title, dataSource.getFirstChild());
        }

    }
    
    
    /*
     * Is this an obsolete contact for the LNO?
     */
    private boolean isLNOContact(Node contactNode) {
    	boolean isLNOContact = false;
    	String contactText = contactNode.getTextContent();
    	
    	if (contactText != null &&
    		contactText.contains("LTER Network Office") &&
    		contactText.contains("tech-support@lternet.edu")
    	   ) {
    		isLNOContact = true;
    	}
    	
    	return isLNOContact;
    }

    
    private void appendDistribution(Document emlDoc,
                                    ParentEml parentEml,
                                    Node dataSource) {

        Element distribution = emlDoc.createElement(DISTRIBUTION);
        dataSource.insertBefore(distribution, dataSource.getFirstChild());

        Element onlineElement = emlDoc.createElement(ONLINE);
        distribution.appendChild(onlineElement);

        Element onlineDescriptionElement = emlDoc.createElement(ONLINE_DESCRIPTION);
        onlineElement.appendChild(onlineDescriptionElement);

        String onlineDescriptionStr = 
            "This online link references an EML document that describes data " +
            "used in the creation of this derivative data package.";
        Text onlineDescriptionText = emlDoc.createTextNode(onlineDescriptionStr);
        onlineDescriptionElement.appendChild(onlineDescriptionText);

        Element urlElement = emlDoc.createElement(URL);
        urlElement.setAttribute("function", "information");
        onlineElement.appendChild(urlElement);

        String parentUrl = getMetadataUrl(parentEml.getPackageId());

        Text urlText = emlDoc.createTextNode(parentUrl);
        urlElement.appendChild(urlText);
    }
    
    
    /**
     * Returns a URL for the EML document with the provided packageId in PASTA's
     * Data Package Manager.
     *
     * @param packageId
     *            the packageId of the EML document.
     *
     * @return a URL for the EML document with the provided packageId in PASTA's
     *         Data Package Manager.
     *
     * @throws IllegalArgumentException
     *             if the provided packageId contains a {@code null} value.
     */
    private String getMetadataUrl(EmlPackageId packageId) {
      if ((packageId == null) || (!packageId.allElementsHaveValues())) {
        String s = "incomplete packageId: " + packageId;
        throw new IllegalArgumentException(s);
      }

      String entityId = null;
      String scope = packageId.getScope();
      Integer identifier = packageId.getIdentifier();
      Integer revision = packageId.getRevision();
      String metadataId = DataPackageManager.composeResourceId(ResourceType.metadata, scope, identifier, 
          revision, entityId);
      return metadataId;
    }


    private void appendDescription(Document emlDoc,
                                   List<String> entityNames,
                                   Node methodStep) {

        Element description;
        Element para;

        // Build description element container
        description = emlDoc.createElement(DESCRIPTION);
        methodStep.insertBefore(description, methodStep.getFirstChild());

        // Build provenance-based metadata statement node
        String provenance = "This method step describes provenance-based metadata"
            + " as specified in the LTER EML Best Practices.";

        para = emlDoc.createElement(PARA);
        description.appendChild(para);

        Text provenanceText = emlDoc.createTextNode(provenance);

        para.appendChild(provenanceText);

        // Build entity name specific metadata node
        para = emlDoc.createElement(PARA);
        description.appendChild(para);

        String entityNameDescription = makeEntityNameString(entityNames);

        Text entityText = emlDoc.createTextNode(entityNameDescription);
        para.appendChild(entityText);

    }

    private String makeEntityNameString(List<String> entityNames) {

        if (entityNames.isEmpty()) {
            // There were no data entity names specified on the URL query string for this packageId.
            return "This provenance metadata does not contain entity specific information.";
        }

        StringBuilder sb = new StringBuilder();

        Iterator<String> i = entityNames.iterator();

        while (i.hasNext()) {

            sb.append("Entity Name: ");
            sb.append(i.next());

            if (i.hasNext()) {
                sb.append(", ");
            }
        }

        return sb.toString();
    }

    private Node getMethodsNode(Document emlDoc) {

        XPath xpath = XPathFactory.newInstance().newXPath();

        NodeList list = null;

        try {
            list = (NodeList)
                xpath.evaluate("//methods", emlDoc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new IllegalStateException(e); // should never be reached
        }

        int size = list.getLength();

        if (size < 1) {
            String s = "A 'methods' element could not be found in the " +
                "provided XML.";
            throw new XmlParsingException(s, emlDoc);
        }

        if (size > 1) {
            String s = "More than one 'methods' element was found in the " +
                "provided XML.";
            throw new XmlParsingException(s, emlDoc);
        }

        return list.item(0);
    }

}
