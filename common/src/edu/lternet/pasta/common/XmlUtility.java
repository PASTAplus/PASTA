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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.owasp.esapi.codecs.XMLEntityCodec;


/**
 * Used for parsing and formatting XML.
 */
public final class XmlUtility {

    private XmlUtility() {
        // preventing instantiation
    }

    /**
     * Returns an XML representation of the provided node.
     *
     * @param node the node to be represented in XML.
     *
     * @return a string containing an XML representation of the
     * provided DOM node.
     */
    public static String nodeToXmlString(Node node) {

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

    /**
     * Returns an empty document to which nodes can be appended.
     * @return an empty document to which nodes can be appended.
     */
    public static Document getEmptyDoc() {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try {
            return factory.newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e); // shouldn't be reached
        }

    }

    /**
     * This method ensures that the output String has only valid XML unicode characters as 
     * specified by the XML 1.0 standard. For reference, please see 
     * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the standard</a>. 
     * This method will return an empty String if the input is null or empty.
     *
     * @param in The String whose non-valid characters we want to remove.
     * @return The in String, stripped of non-valid characters.
     */
    public static String stripNonValidXMLCharacters(String in) {
        StringBuffer out = new StringBuffer(); // Used to hold the output.
        char current; // Used to reference the current character.

        if (in == null || ("".equals(in))) return ""; // vacancy test.
        for (int i = 0; i < in.length(); i++) {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if ((current == 0x9) ||
                (current == 0xA) ||
                (current == 0xD) ||
                ((current >= 0x20) && (current <= 0xD7FF)) ||
                ((current >= 0xE000) && (current <= 0xFFFD)) ||
                ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
            }
        }
        return out.toString();
    }

    /**
     * Parses the provided XML string as a (DOM) document.
     *
     * @param xmlString the XML string to be parsed.
     * @param schema the schema used to validate the provided XML.
     *
     * @return a document derived from the provided XML string.
     */
    public static Document xmlStringToDoc(String xmlString, Schema schema) {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setSchema(schema);

        InputSource source = new InputSource(new StringReader(xmlString));

        try {

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new XmlParsingErrorHandler(xmlString));
            return builder.parse(source);

        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e); // shouldn't be reached
        } catch (SAXException e) {
            throw new IllegalStateException(e); // shouldn't be reached
        } catch (IOException e) {
            throw new IllegalStateException(e); // shouldn't be reached
        }

    }

    /**
     * Parses the provided XML string as a (DOM) document.
     *
     * @param xmlString the XML string to be parsed.
     *
     * @return a document derived from the provided XML string.
     */
    public static Document xmlStringToDoc(String xmlString) {
        return xmlStringToDoc(xmlString, null);
    }

    /**
     * Returns the provided XML string as a schema.
     *
     * @param xmlString an XML schema.
     *
     * @return a schema object that corresponds to the provided string.
     */
    public static Schema xmlStringToSchema(String xmlString) {

        SchemaFactory factory =
            SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        factory.setErrorHandler(new XmlParsingErrorHandler(xmlString));

        StreamSource source = new StreamSource(new StringReader(xmlString));

        try {
            return factory.newSchema(source);
        } catch (SAXException e) {
            throw new IllegalStateException(e); // shouldn't be reached
        }
    }

    /**
   * Returns a safe encoded version of the XML string.
   * 
   * @param rawXml The non-encoded XML.
   * @return The encoded XML as a String object.
   */
	public static String xmlEncode(String rawXml) {

		String encodedXml = null;

		if (rawXml == null) {
			encodedXml = "";
		} else {

			// Encodings for XML
			XMLEntityCodec xmlEntityCodec = new XMLEntityCodec();
			char[] immune = new char[0];

			StringBuffer xml = new StringBuffer();

			for (int a = 0; a < rawXml.length(); a++) {
				xml.append(xmlEntityCodec.encodeCharacter(immune, rawXml.charAt(a)));
			}

			encodedXml = xml.toString();

		}

		return encodedXml;

	}

	
	/**
	 * Given a File object, builds and returns a Document object.
	 * 
	 * @param xmlFile   the File object containing the XML
	 * @return  the Document object built from the XML file
	 */
	public static Document xmlFileToDocument(File xmlFile) 
			throws SAXException, IOException, ParserConfigurationException {
		Document document = null;

		if (xmlFile != null) {
			DocumentBuilder documentBuilder = DocumentBuilderFactory
					.newInstance().newDocumentBuilder();
			InputStream fileInputStream = new FileInputStream(xmlFile);
			document = documentBuilder.parse(fileInputStream);
		}

		return document;
	}


    /**
     * Used for handling all XML parsing errors in {@link XmlUtility}. If
     * an error occurs while parsing an XML string, an
     * {@link XmlParsingException} is thrown that contains a descriptive
     * error message that is suitable for end-users. This message is derived
     * from the SAX Parser error message and the original XML string, which can
     * be obtained with {@code getCause().getMessage()} and {@code getXml()},
     * respectively.
     */
    public static final class XmlParsingErrorHandler implements ErrorHandler {

        private final String xml;

        /**
         * Constructs an object to handle errors that occur during parsing
         * of the provided XML string. If the provided string is not
         * {@code null}, it will be included in the error message if/when an
         * error occurs.
         *
         * @param xml the string that might produce parsing errors.
         */
        public XmlParsingErrorHandler(String xml) {
            this.xml = xml;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(SAXParseException e) throws SAXException {
            throw wrap(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            throw wrap(e);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void warning(SAXParseException e) throws SAXException {
            throw wrap(e);
        }

        private XmlParsingException wrap(SAXParseException e) {

            StringBuilder sb = new StringBuilder();

            sb.append("The following XML parsing error occurred: ");
            sb.append(e.getMessage());

            if (xml != null) {
                sb.append("\nThe XML that caused this error is:\n");
                sb.append(xml);
            }

            return new XmlParsingException(sb.toString(), e, xml);
        }
    }
}
