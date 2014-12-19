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

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.Entity;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Entity.EntityType;
import edu.lternet.pasta.portal.search.PageControl;
import edu.lternet.pasta.portal.search.Search;


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

  private String resultSet = null;
  private boolean includeEcotrends = false;
  private boolean includeLandsat5 = false;
  private int rows = Search.DEFAULT_ROWS;
  private Integer numFound = 0;
  private Integer start = 0;
  private PageControl pageControl = null;
  private String pageBodyHTML = "";
  private String pageHeaderHTML = "";
  

  /*
   * Constructors
   */

  /**
   * Constructs a new ResultSetUtility object from search results XML.
   * 
   * @param xml
   *          The search results XML as a String object.
   * 
   * @throws ParseException
   */
  public ResultSetUtility(String xml) throws ParseException {

    if (xml == null || xml.isEmpty()) {
      throw new ParseException("Result Set is empty", 0);
    }

    this.resultSet = xml;
    parseResultSet(xml);
    pageControl = new PageControl(numFound, start, rows);
    pageHeaderHTML = pageControl.composePageHeader();
    pageBodyHTML = pageControl.composePageBody();
  }

  
  /*
   * Methods
   */
  
  
  	public Integer getNumFound() {
  		return numFound;
  	}
  
  
  	private void parseResultSet(String xml) { 	        	  
  		if (xml != null) {
  			InputStream inputStream = null;
  			try {
  				inputStream = IOUtils.toInputStream(xml, "UTF-8");
  				DocumentBuilder documentBuilder = 
  	              DocumentBuilderFactory.newInstance().newDocumentBuilder();
  				CachedXPathAPI xpathapi = new CachedXPathAPI();

  				Document document = null;
  				document = documentBuilder.parse(inputStream);
  	      
  				if (document != null) {
  	        
  					Node numFoundNode = null;
  					numFoundNode = xpathapi.selectSingleNode(document, "//resultset/@numFound");

  					if (numFoundNode != null) {
  						String numFoundStr = numFoundNode.getNodeValue();
  						this.numFound = new Integer(numFoundStr);
  					}
  					
  					Node startNode = null;
  					startNode = xpathapi.selectSingleNode(document, "//resultset/@start");

  					if (startNode != null) {
  						String startStr = startNode.getNodeValue();
  						this.start = new Integer(startStr);
  					}
  					
  				}
  			}
  			catch (Exception e) {
  		        logger.error("Error parsing search result set: " + e.getMessage());
  			}
  			finally {
  				if (inputStream != null) {
  					try {
  						inputStream.close();
  					}
  					catch (IOException e) {
  						;
  					}
  				}
  			}
  		}
  	}
  	        

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
  	 * Sets the value of the docsPerPage instance variable.
  	 * 
  	 * @param n
  	 * 			the desired number of documents displayed per page
  	 */
	public void setRowsPerPage(int n) {
		this.rows = new Integer(n);
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
		
		// Pass the docsPerPage value as a parameter to the XSLT
		parameterMap.put("rows", new Integer(this.rows).toString());

		// Pass the includeEcotrends value as a parameter to the XSLT
		if (this.includeEcotrends) {
			parameterMap.put("includeEcotrends", "true");
		}

		// Pass the includeLandsat5 value as a parameter to the XSLT
		if (this.includeLandsat5) {
			parameterMap.put("includeLandsat5", "true");
		}

		String html = XSLTUtility.xmlToHtml(this.resultSet, xslPath,
				parameterMap);
		
		html = String.format("%s%s%s<br/>%s", pageHeaderHTML, pageBodyHTML, html, pageBodyHTML);
		return html;
	}

}
