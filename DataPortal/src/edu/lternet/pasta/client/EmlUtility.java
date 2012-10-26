/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.client;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.ParseException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.log4j.Logger;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;

import edu.lternet.pasta.portal.ConfigurationListener;

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
  
  private static final String emlHtmlHead;
  private static final String emlHtmlTail;
  
  static {
      emlHtmlHead = String.format("%s%s%s%s%s%s%s%s%s",
        "<!doctype html>\n",
        "<html>\n\n",
        "<head>\n",
        "    <title>Metadata Previewer</title>\n",
        "    <link rel=\"stylesheet\" type=\"text/css\" href=\"./css/lter-nis.css\">\n",
        "    <script src=\"./js/jquery-1.7.1.js\" type=\"text/javascript\"></script>\n",
        "    <script src=\"./js/toggle.js\" type=\"text/javascript\"></script>\n",
        "</head>\n\n",
        "<body>\n"
      );
 
      emlHtmlTail = String.format("%s%s",
        "</body>\n\n",
        "</html>\n"
      );
       
  }

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

    this.eml = eml;

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
  
  /**
   * Assembles an HTML-rendered EML document, surrounding the main body of the
   * HTML with head and tail portions of boilerplate HTML.
   * 
   * @param body  The body portion of the HTML as returned by the XSLT transformation.
   * @return the assembled HTML document
   */
  public static String assembleEmlHtml(String body) {
    return String.format("%s%s%s", emlHtmlHead, body, emlHtmlTail);                    
  }

  
  private String emlReferenceExpander(String xslPath) {

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
    }

    return xml;
    
  }

  /**
   * Transforms an EML XML document to an HTML document.
   * 
   * @param xslPath
   *          The path to the quality report XSL stylesheet.
   * 
   * @return The HTML table as a String object.
   */
  public String xmlToHtml(String xslPath) {

    String html = null;

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
      html = stringWriter.toString();
    } catch (TransformerConfigurationException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    } catch (TransformerFactoryConfigurationError e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    } catch (TransformerException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

    return html;
  }

  /**
   * @param args
   */
  public static void main(String[] args) {

    ConfigurationListener.configure();

    String cwd = System.getProperty("user.dir");
    File inFile = new File(cwd + "/documents/knb-lter-nin.1.1.eml.xml");
    File outFile = new File(cwd + "/WebRoot/knb-lter-nin.1.1.eml.html");
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

    String html = null;
    String emlXsl = cwd + "/WebRoot/WEB-INF/xsl/eml-2.1.0.xsl";

    html = eu.xmlToHtml(emlXsl);

    try {
      FileUtils.writeStringToFile(outFile, html);
    } catch (IOException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

  }

}
