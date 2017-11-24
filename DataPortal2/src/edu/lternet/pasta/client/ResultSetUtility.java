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
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import edu.lternet.pasta.portal.search.PageControl;
import edu.lternet.pasta.portal.search.Search;
import edu.lternet.pasta.portal.user.SavedData;


/**
 * @author servilla
 * @since Apr 6, 2012
 * 
 *        The ResultSetUtility class parses XML search results and
 *        renders them as HTML.
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
  private int rows = Search.DEFAULT_ROWS;
  private Integer numFound = 0;
  private Integer start = 0;
  private PageControl pageControl = null;
  private String pageBodyHTML = "";
  private String pageHeaderHTML = "";
  private String mapButtonHTML = "";
  private String relevanceHTML = "";
  private SavedData savedData = null;
  private boolean isSavedDataPage;
  private boolean showSavedData = true;
  private String sort = null;
  private String htmlTable = null;
  

  /*
   * Constructors
   */

  /**
   * Constructs a new ResultSetUtility object from search results XML,
   * specifying whether the search results represent saved data packages
   * or regular search results.
   * 
   * @param xml
   *          The search results XML as a String object.
   * 
   * @throws ParseException
   */
  public ResultSetUtility(String xml, String sort, SavedData savedData, boolean isSavedDataPage) throws ParseException {

    if (xml == null || xml.isEmpty()) {
    	String msg = "An error prevented this search from completing successfully and no search results are available. A network connection may have failed or one of the servers may be unavailable.";
        throw new ParseException(msg, 0);
    }

    this.resultSet = xml;
    this.sort = sort;
    this.savedData = savedData;
    this.isSavedDataPage = isSavedDataPage;
    parseResultSet(xml);
    pageControl = new PageControl(numFound, start, rows, sort, isSavedDataPage);
    pageHeaderHTML = pageControl.composePageHeader();
    pageBodyHTML = pageControl.composePageBody();
    mapButtonHTML = composeMapButtonHTML();
    relevanceHTML = composeRelevanceHTML();
  }

  
  /**
   * Constructs a new ResultSetUtility object from search results XML.
   * 
   * @param xml
   *          The search results XML as a String object.
   * 
   * @throws ParseException
   */
  public ResultSetUtility(String xml, String sort) throws ParseException {
	  this(xml, sort, null, false);
	  this.showSavedData = false;
  }

  
  /*
   * Methods
   */
  
  
  	public String getHTMLTable() {
  		return htmlTable;
  	}
  
  
  	public Integer getNumFound() {
  		return numFound;
  	}
  	
  	
  	public String getMapButtonHTML () {
  		return mapButtonHTML;
  	}
  	
  	
 	public String getRelevanceHTML () {
  		return relevanceHTML;
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
  	 * Sets the value of the docsPerPage instance variable.
  	 * 
  	 * @param n
  	 * 			the desired number of documents displayed per page
  	 */
	public void setRowsPerPage(int n) {
		this.rows = new Integer(n);
	}


	/**
	 * Transforms Solr search results XML to an HTML table.
	 * 
	 * @param xslPath
	 *            The path to the search results-set stylesheet.
	 * 
	 * @return The HTML table as a String object.
	 */
	public String xmlToHtmlTable(String xslPath) throws ParseException {
		String html = "";
		HashMap<String, String> parameterMap = new HashMap<String, String>();
		
		if (numFound > 0) {
			String tableHeaderHTML = composeTableHeaderHTML(this.showSavedData);
		
			String savedDataList = "";	
			if (savedData != null) {
				savedDataList = savedData.getSavedDataList();
			}
		
			// Pass the docsPerPage value as a parameter to the XSLT
			parameterMap.put("start", start.toString());
			parameterMap.put("rows", new Integer(this.rows).toString());
			parameterMap.put("isSavedDataPage", new Boolean(this.isSavedDataPage).toString());
			parameterMap.put("savedDataList", savedDataList);
			parameterMap.put("showSavedData", new Boolean(this.showSavedData).toString());

			String tableRowsHTML = XSLTUtility.xmlToHtml(this.resultSet, xslPath,
				parameterMap);
		
			String tableFooterHTML = composeTableFooterHTML();
			   
			StringBuilder sb = new StringBuilder("");
			sb.append(pageHeaderHTML);
			sb.append(pageBodyHTML);
			sb.append(tableHeaderHTML);
			sb.append(tableRowsHTML);
			sb.append(tableFooterHTML);
			sb.append(pageBodyHTML);
			html = sb.toString();
		}
		else {
			html = composeNoMatchesHTML(isSavedDataPage);
		}

		htmlTable = html;
		return html;
	}
	
	
	private String composeMapButtonHTML() {
		String html = "";
		
		if (this.numFound > 0) {
			String servlet = "./mapSearchServlet";
			StringBuilder sb = new StringBuilder();
			sb.append(String.format("  <form id=\"mapsearch\" action=\"%s\" method=\"post\" name=\"mapsearch\">", servlet));
			sb.append("    <input class=\"btn btn-info btn-default\" name=\"submit\" type=\"submit\" value=\"View Map of Search Results\" />");
		    sb.append("  </form>\n");
			html = sb.toString();
		}

		return html;
	}
	
	
	private String composeRelevanceHTML() {
		String html = "";
		StringBuilder sb = new StringBuilder();
		String disabled = "disabled";
		
		if (this.numFound < 2) {
			return html;
		}
		else if (!this.sort.equals(Search.DEFAULT_SORT)) {
			disabled = "";
		}
		
		String servlet = "./simpleSearch";
		String relevanceSort = pageControl.getRelevanceSort();
		String relevanceURL = String.format("%s?start=0&rows=10&sort=%s", servlet, relevanceSort); 
		sb.append(String.format("  <form id=\"relevance\" action=\"%s\" method=\"post\" name=\"relevance\">", relevanceURL));
		sb.append(String.format("    <input class=\"btn btn-info btn-default\" name=\"submit\" type=\"submit\" value=\"Reset Sort Order (most relevant first)\" %s />", disabled));
	    sb.append("  </form>\n");
		html = sb.toString();
		return html;
	}
	
	
	private String composeTableHeaderHTML(boolean showSavedData) {
		StringBuilder html = new StringBuilder("\n");
		html.append("<table width=\"100%\">\n");
		html.append("    <tbody>\n");
		html.append("        <tr>\n");
		
		String titleSort = pageControl.getTitleSort();
		String creatorsSort = pageControl.getCreatorsSort();
		String pubDateSort = pageControl.getPubDateSort();
		String packageIdSort = pageControl.getPackageIdSort();
		
		String titleDirection = pageControl.getTitleDirection();
		String creatorsDirection = pageControl.getCreatorsDirection();
		String pubDateDirection = pageControl.getPubDateDirection();
		String packageIdDirection = pageControl.getPackageIdDirection();

		String servlet = isSavedDataPage ? "savedDataServlet" : "simpleSearch";
		String titleWidth = showSavedData ? "45%" : "50%";
		String creatorsWidth = showSavedData ? "20%" : "25%";
		String pubDateWidth = "10%";
		String packageIdWidth = "15%";
		String dataShelfWidth = "10%";
		
		/*
		 * Only provide column-sort links if there's more than one data package
		 */
		if (numFound > 1) {
			html.append(String.format("            <th class=\"nis\" width=\"%s\"><a class='searchsubcat' href='%s?start=0&rows=10&sort=%s'>Title</a>%s</th>\n", titleWidth, servlet, titleSort, titleDirection));
			html.append(String.format("            <th class=\"nis\" width=\"%s\"><a class='searchsubcat' href='%s?start=0&rows=10&sort=%s'>Creators</a>%s</th>\n", creatorsWidth, servlet, creatorsSort, creatorsDirection));
			html.append(String.format("            <th class=\"nis\" width=\"%s\"><a class='searchsubcat' href='%s?start=0&rows=10&sort=%s'>Publication Date</a>%s</th>\n", pubDateWidth, servlet, pubDateSort, pubDateDirection));
			html.append(String.format("            <th class=\"nis\" width=\"%s\"><a class='searchsubcat' href='%s?start=0&rows=10&sort=%s'>Package Id</a>%s</th>\n", packageIdWidth, servlet, packageIdSort, packageIdDirection));
		}
		else {
			html.append(String.format("            <th class=\"nis\" width=\"%s\">Title</th>\n", titleWidth));
			html.append(String.format("            <th class=\"nis\" width=\"%s\">Creators</th>\n", creatorsWidth));
			html.append(String.format("            <th class=\"nis\" width=\"%s\">Publication Date</th>\n", pubDateWidth));
			html.append(String.format("            <th class=\"nis\" width=\"%s\">Package Id</th>\n", packageIdWidth));
		}

		// Display this table column only if we're displaying the data shelf for a logged-in user
		if (showSavedData) {
    		String dataShelfStartTag = isSavedDataPage ? "" : "<a href='savedDataServlet'>";
    		String dataShelfEndTag =   isSavedDataPage ? "" : "</a>";
    		html.append(String.format("            <th class=\"nis\" width=\"%s\">%s<img alt='Your Data Shelf' src='images/data_shelf.png' title='Your Data Shelf' width='60' height='60'></img>%s</th>\n", dataShelfWidth, dataShelfStartTag, dataShelfEndTag));
		}
		
		html.append("        </tr>\n");

		String htmlStr = html.toString();
		return htmlStr;
	}

	
	private String composeTableFooterHTML() {
		StringBuilder html = new StringBuilder("\n");
		html.append("    </tbody>\n");
		html.append("</table>\n");	
		
		return html.toString();
	}

	
	private String composeNoMatchesHTML(boolean isSavedDataPage) {
		String html = "";
		
		if (isSavedDataPage) {
			html = "<p>There are no data packages on your data shelf.</p>";
		}
		else {
			html = "<p>No matching data packages were found.</p>";
		}
		
		return html;
	}

}
