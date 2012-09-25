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

package edu.lternet.pasta.eventmanager;

import java.io.File;
import java.util.Collection;

import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.XmlParsingException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.EmlPackageIdFormat.Delimiter;
import edu.lternet.pasta.eventmanager.EmlSubscription.SubscriptionBuilder;

/**
 * Used to parse and format <em>EML modification</em> event subscriptions as
 * XML. Subscriptions can exist in various states - when a user is attempting to
 * create a subscription, when a subscription has been "created" but not yet
 * persisted to a database, and when a subscription exists in a database. The
 * only XML strings parsed by this class are those in the first category, which
 * contain only packageId, URL, and access control rule attributes. XML strings
 * produced by this class only belong to the second and third categories. If a
 * provided {@code Subscription} is inactive, an
 * {@link IllegalArgumentException} is thrown.
 */
public class XmlSubscriptionFormatV1 {

    public static final String SUBSCRIPTION = "subscription";

    public static final String SUBSCRIPTIONS = "subscriptions";

    public static final String TYPE = "type";

    public static final String EML = "eml";

    public static final String PACKAGE_ID = "packageId";

    public static final String URL = "url";

    public static final String SUBSCRIPTION_ID = "id";

    public static final String CREATOR = "creator";

    /**
     * Parses the provided XML string and returns a corresponding subscription
     * builder. The provided XML is validated against a schema that allows
     * only packageId, URL, and access control rule elements to be present.
     *
     * @param subscriptionXml the XML representation of a subscription.
     *
     * @return a subscription builder containing packageId, URL, and
     * access control rule attributes obtained from the provided XML.
     *
     * @throws XmlParsingException if any parsing error occurs, including the
     * parsing of packageIds and URLs in the provided XML.
     */
    public SubscriptionBuilder parse(String subscriptionXml) {

        Document doc = toDocument(subscriptionXml);

        NodeList nodes = doc.getFirstChild().getChildNodes();
        SubscriptionBuilder sb = new SubscriptionBuilder();

        try {
            setPackageId(sb, nodes);
            setUrl(sb, nodes);
        } catch (IllegalArgumentException e) {
            String s = "An error was detected while parsing the provided " +
                       "XML (shown below). " + e.getMessage() + "\n\n" +
                       subscriptionXml;
            throw new XmlParsingException(s, e, subscriptionXml);
        }

        return sb;
    }

    private void setPackageId(SubscriptionBuilder sb, NodeList nodes) {

        EmlPackageIdFormat formatter = new EmlPackageIdFormat(Delimiter.DOT);

        String packageId = getNodeText(nodes, PACKAGE_ID);

        EmlPackageId epi = formatter.parse(packageId);
        sb.setEmlPackageId(epi);
    }

    private void setUrl(SubscriptionBuilder sb, NodeList nodes) {
        String url = getNodeText(nodes, URL);
        sb.setUrl(new SubscribedUrl(url));
    }

    private String getNodeText(NodeList nodes, String nodeName) {
        return getNode(nodes, nodeName).getTextContent();
        //String xmlText = getNode(nodes, nodeName).getTextContent();
        //return StringEscapeUtils.unescapeXml(xmlText);
    }

    private Node getNode(NodeList nodes, String nodeName) {

        for (int i = 0, size = nodes.getLength(); i < size; i ++) {

            Node n = nodes.item(i);

            if (n.getNodeName().equals(nodeName)) {
                return n;
            }

        }

        String s = "Node is missing, but validation didn't catch it: " +
                    nodeName;
        throw new IllegalStateException(s);
    }

    /**
     * Returns an XML representation of the provided subscription. If the
     * subscription ID is {@code null}, because the subscription has not been
     * persisted to a database, it is omitted from the returned XML.
     *
     * @param subscription the subscription to be formatted.
     * @return an XML representation of the provided subscription.
     */
    public String format(EmlSubscription subscription) {
        return XmlUtility.nodeToXmlString(toDocument(subscription));
    }

    /**
     * Parses the provided XML string and returns a corresponding document.
     *
     * @param subscriptionXml the XML representation of a subscription.
     *
     * @return a document containing elements, attributes, and valued obtained
     * from the provided XML.
     */
    private Document toDocument(String subscriptionXml) {

        File xsdFile = ConfigurationListener.getEmlSubscriptionSchemaFile();

        String schemaString = FileUtility.fileToString(xsdFile);

        Schema schema = XmlUtility.xmlStringToSchema(schemaString);

        return XmlUtility.xmlStringToDoc(subscriptionXml, schema);
    }

    /**
     * Returns a document corresponding to the provided subscription.
     *
     * @param subscription the subscription to be represented as a document.
     *
     * @return a document containing elements, attributes, and valued obtained
     * from the provided subscription.
     */
    private Document toDocument(EmlSubscription subscription) {

        if (!subscription.isActive()) {
            throw new IllegalArgumentException("inactive :" + subscription);
        }

        Document doc = XmlUtility.getEmptyDoc();

        Element root = doc.createElement(SUBSCRIPTION);
        Element creator = doc.createElement(CREATOR);
        Element packageId = doc.createElement(PACKAGE_ID);
        Element url = doc.createElement(URL);

        EmlPackageIdFormat formatter = new EmlPackageIdFormat(Delimiter.DOT);

        root.setAttribute(TYPE, EML);

        Long longId = subscription.getSubscriptionId();

        if (longId != null) {
            Element id = doc.createElement(SUBSCRIPTION_ID);
            setText(id, longId.toString());
            root.appendChild(id);
        }

        setText(creator, subscription.getCreator());
        setText(packageId, formatter.format(subscription.getPackageId()));
        setText(url, subscription.getUrl());

        doc.appendChild(root);
        root.appendChild(creator);
        root.appendChild(packageId);
        root.appendChild(url);

        return doc;
    }

    private void setText(Element e, String text) {
        //e.setTextContent(StringEscapeUtils.escapeXml(text));
        e.setTextContent(text);
    }

    /**
     * Returns a document corresponding to the provided collection of
     * subscriptions.
     *
     * @param subscriptions the subscriptions to be represented as a document.
     *
     * @return a document containing elements, attributes, and valued obtained
     * from the provided collection of subscriptions.
     */
    private Document toDocument(Collection<EmlSubscription> subscriptions) {

        Document doc = XmlUtility.getEmptyDoc();

        Element root = doc.createElement(SUBSCRIPTIONS);
        doc.appendChild(root);

        for (EmlSubscription s : subscriptions) {
            Node node = toDocument(s).getFirstChild();
            node = doc.adoptNode(node);
            root.appendChild(node);
        }

        return doc;
    }

    /**
     * Returns an XML string corresponding to the provided collection of
     * subscriptions. If a subscription ID is {@code null}, because the
     * subscription has not been persisted to a database, it is omitted from the
     * returned XML.
     *
     * @param subscriptions the subscriptions to be represented as XML.
     *
     * @return an XML string containing elements, attributes, and valued
     * obtained from the provided collection of subscriptions.
     */
    public String format(Collection<EmlSubscription> subscriptions) {
        return XmlUtility.nodeToXmlString(toDocument(subscriptions));
    }
}
