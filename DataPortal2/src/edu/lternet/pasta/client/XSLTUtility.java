/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

package edu.lternet.pasta.client;

import net.sf.saxon.s9api.*;
import org.apache.log4j.Logger;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;

/**
 * @author costa
 * @since Jul 23, 2014
 * 
 *        The XSLTUtility class provides utility methods for converting from XML
 *        to HTML.
 * 
 */
public class XSLTUtility {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
			.getLogger(edu.lternet.pasta.client.XSLTUtility.class);


	/*
	 * Instance variables
	 */

	/*
	 * Constructors
	 */

	/*
	 * Methods
	 */

	/**
	 * Transforms an EML XML document to an HTML document.
	 * 
	 * @param xml
	 *            The EML XML document to be transformed
	 * @param xslPath
	 *            The path to the quality report XSL stylesheet.
	 * @param parameters
	 *            The parameters and their associated values, passed
	 *            to the XSLT processor in a map object
	 * 
	 * @return The HTML document as a String object.
	 */
	public static String xmlToHtml(String xml, String xslPath,
			HashMap<String, String> parameters) {
		String html = null;
		File styleSheet = new File(xslPath);
		StringReader stringReader = new StringReader(xml);
		StringWriter stringWriter = new StringWriter();
		StreamSource styleSource = new StreamSource(styleSheet);
		Result result = new StreamResult(stringWriter);
		Source source = new StreamSource(stringReader);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer(
					styleSource);
			if (parameters != null) {
				for (String parameterName : parameters.keySet()) {
					String parameterValue = parameters.get(parameterName);
					if (parameterValue != null && !parameterValue.equals("")) {
						transformer.setParameter(parameterName, parameterValue);
					}
				}
			}
			transformer.transform(source, result);
			html = stringWriter.toString();
		}
		catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		catch (TransformerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return html;
	}


	/**
	 * Transforms an EML XML document to an HTML document using the Saxon XSLT
	 * engine which can process XSLT 2.0.
	 * 
	 * @param xml
	 *            The EML XML document to be transformed
	 * @param xslPath
	 *            The path to the quality report XSL stylesheet.
	 * @param parameters
	 *            The parameters and their associated values, passed
	 *            to the XSLT processor in a map object
	 * 
	 * @return The HTML document as a String object.
	 */
	public static String xmlToHtmlSaxon(String xml, String xslPath, HashMap<String, String> parameters) 
			throws ParseException {
		String html = null;
		File xsltFile = new File(xslPath);
		StringReader stringReader = new StringReader(xml);
		StringWriter stringWriter = new StringWriter();
		StreamSource xsltSource = new StreamSource(xsltFile);
		Source source = new StreamSource(stringReader);

		try {
			Processor processor = new Processor(false);
			XsltCompiler xsltCompiler = processor.newXsltCompiler();
			XsltExecutable xsltExecutable = xsltCompiler.compile(xsltSource);
			XdmNode xdmNode = processor.newDocumentBuilder().build(source);
			Serializer out = new Serializer();
			out.setOutputProperty(Serializer.Property.METHOD, "html");
			out.setOutputProperty(Serializer.Property.INDENT, "yes");
			out.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
			out.setOutputWriter(stringWriter);
			XsltTransformer xsltTransformer = xsltExecutable.load();
			xsltTransformer.setInitialContextNode(xdmNode);
			if (parameters != null) {
				for (String parameterName : parameters.keySet()) {
					String parameterValue = parameters.get(parameterName);
					if (parameterValue != null && !parameterValue.equals("")) {
						QName qName = new QName(parameterName);
						XdmAtomicValue xdmAtomicValue = new XdmAtomicValue(
								parameterValue, ItemType.STRING);
						xsltTransformer.setParameter(qName, xdmAtomicValue);
					}
				}
			}
			xsltTransformer.setDestination(out);
			xsltTransformer.transform();
			html = stringWriter.toString();
		}
		catch (SaxonApiException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw new ParseException("EML Parse Error: " + e.getMessage(), 0);
		}

		return html;
	}

}
