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
import java.util.HashMap;

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
import org.apache.commons.io.FileUtils;

import edu.lternet.pasta.portal.ConfigurationListener;

/**
 * @author servilla
 * @since Apr 6, 2012
 * 
 *        The QualtyReportUtility class provides utility methods for managing
 *        PASTA quality reports, including converting from xml to html.
 * 
 */
public class ResultSetUtility {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.client.ResultSetUtility.class);

  /*
   * Instance variables
   */

  String resultSet = null;
  boolean includeEcotrends = false;
  boolean includeLandsat5 = false;

  /*
   * Constructors
   */

  /**
   * Constructs a new QualityReportUtility object from the quality report XML.
   * 
   * @param qr
   *          The quality report XML as a String object.
   * 
   * @throws ParseException
   */
  public ResultSetUtility(String resultSet) throws ParseException {

    if (resultSet == null || resultSet.isEmpty()) {
      throw new ParseException("Result Set is empty", 0);
    }

    this.resultSet = resultSet;

  }

  
  /*
   * Methods
   */
  
  
  	/**
  	 * Sets the value of the includeEcotrends instance variable.
  	 * 
  	 * @param include 
  	 * 			true if EcoTrends data packages should be searched, else false
  	 */
	public void setIncludeEcotrends(boolean include) {
		this.includeEcotrends = include;
	}


  	/**
  	 * Sets the value of the includeLandsat5 instance variable.
  	 * 
  	 * @param include 
  	 * 			true if Landsat5 data packages should be searched, else false
  	 */
	public void setIncludeLandsat5(boolean include) {
		this.includeLandsat5 = include;
	}


	/**
	 * Transforms Metacat search results XML to an HTML table.
	 * 
	 * @param xslPath
	 *            The path to the search results-set stylesheet.
	 * 
	 * @return The HTML table as a String object.
	 */
	public String xmlToHtmlTable(String xslPath) throws ParseException {
		HashMap<String, String> parameterMap = new HashMap<String, String>();

		// Pass the includeEcotrends value as a parameter to the XSLT
		if (this.includeEcotrends) {
			parameterMap.put("includeEcotrends", "true");
		}

		// Pass the includeLandsat5 value as a parameter to the XSLT
		if (this.includeLandsat5) {
			parameterMap.put("includeLandsat5", "true");
		}

		String html = XSLTUtility.xmlToHtmlSaxon(this.resultSet, xslPath,
				parameterMap);
		return html;
	}

}
