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

/**
 * @author servilla
 * @since Apr 6, 2012
 * 
 *        The ReportUtility class provides utility methods for managing
 *        PASTA reports, including converting from xml to html.
 * 
 */
public class ReportUtility {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.ReportUtility.class);

  /*
   * Instance variables
   */

  String report = null;

  /*
   * Constructors
   */

  /**
   * Constructs a new ReportUtility object from the report XML.
   * 
   * @param report
   *          The report XML as a String object.
   * 
   * @throws ParseException
   */
  public ReportUtility(String report) throws ParseException {

    if (report == null || report.isEmpty()) {
      throw new ParseException("Quality Report is empty", 0);
    }

    this.report = report;

  }

  /*
   * Methods
   */

  /**
   * Transforms a report XML document to an HTML table.
   * 
   * @param xslPath
   *          The path to the quality report XSL stylesheet.
   * 
   * @return The HTML table as a String object.
   */
  public String xmlToHtmlTable(String xslPath) {

    String html = null;

    File styleSheet = new File(xslPath);

    StringReader stringReader = new StringReader(this.report);
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

}
