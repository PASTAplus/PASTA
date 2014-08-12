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

import edu.lternet.pasta.common.HTMLUtility;
import edu.lternet.pasta.portal.ConfigurationListener;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;
import java.util.HashMap;

/**
 * @author servilla
 * @since Apr 12, 2012
 * 
 *        The EmlUtility class provides utility methods for managing PASTA EML
 *        documents, including converting from xml to html.
 * 
 */
public class EmlUtility {

  /*
   * Class variables
   */
  
  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.EmlUtility.class);

  /*
   * Instance variables
   */

  String eml = null;

  /*
   * Constructors
   */

  /**
   * Constructs a new EmlUtility object.
   * 
   * @param eml
   *          The EML XML as a String object.
   * 
   * @throws ParseException
   */
  public EmlUtility(String eml) throws ParseException {

    if (eml == null || eml.isEmpty()) {
      throw new ParseException("EML is empty", 0);
    }

    this.eml = HTMLUtility.stripNonValidHTMLCharacters(eml);

    // Properties configuration for local XSLT path and current working
    // directory.
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    String idref = options.getString("emlutility.idref");
    String cwd = options.getString("system.cwd");

    // Expand EML references into full canonical form.
    this.eml = this.emlReferenceExpander(cwd + idref);

  }

  /*
   * Methods
   */
  
  private String emlReferenceExpander(String xslPath) throws ParseException {

    String xml = null;

    File styleSheet = new File(xslPath);

    StringReader stringReader = new StringReader(this.eml);
    StringWriter stringWriter = new StringWriter();
    StreamSource styleSource = new StreamSource(styleSheet);
    Result result = new StreamResult(stringWriter);
    Source source = new StreamSource(stringReader);

    try {
      Transformer t = TransformerFactory.newInstance().newTransformer(
          styleSource);
      t.transform(source, result);
      xml = stringWriter.toString();
    } catch (TransformerConfigurationException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    } catch (TransformerFactoryConfigurationError e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    } catch (TransformerException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      throw new ParseException("EML Parse Error: " + e.getMessage(), 0);
    }

    return xml;
    
  }

  
	/**
	 * Transforms an EML XML document to an HTML document.
	 * 
	 * @param xslPath
	 *            The path to the quality report XSL stylesheet.
	 * @param parameters
	 *            The parameters and their associated values, passed
	 *            to the XSLT processor in a map object
	 *
	 * @return The HTML document as a String object.
	 */
	public String xmlToHtml(String xslPath, HashMap<String, String> parameters) {
		String html = XSLTUtility.xmlToHtml(this.eml, xslPath, parameters);
		return html;
	}


	/**
	 * Transforms an EML XML document to an HTML document using the Saxon XSLT
	 * engine which can process XSLT 2.0.
	 * 
	 * @param xslPath
	 *            The path to the quality report XSL stylesheet.
	 * 
	 * @return The HTML document as a String object.
	 */
	public String xmlToHtmlSaxon(String xslPath,
			HashMap<String, String> parameters) throws ParseException {
		String html = XSLTUtility.xmlToHtmlSaxon(this.eml, xslPath, parameters);
		return html;
	}


  /**
   * @param args   String array with three arguments:
   *   arg[0] absolute path to the input XML file
   *   arg[1] absolute path to the output HTML file
   *   arg[2] absolute path to the EML XSLT stylesheet
   */
  public static void main(String[] args) {

    String inputPath = args[0];
    String outputPath = args[1];
    String emlXslPath = args[2];
    ConfigurationListener.configure();

    File inFile = new File(inputPath);
    File outFile = new File(outputPath);
    String eml = null;

    try {
      eml = FileUtils.readFileToString(inFile);
    } catch (IOException e1) {
      logger.error(e1.getMessage());
      e1.printStackTrace();
    }

    EmlUtility eu = null;

    try {
      eu = new EmlUtility(eml);
    } catch (ParseException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

    String html = eu.xmlToHtml(emlXslPath, null);

    try {
      FileUtils.writeStringToFile(outFile, html);
    } catch (IOException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

  }

}
