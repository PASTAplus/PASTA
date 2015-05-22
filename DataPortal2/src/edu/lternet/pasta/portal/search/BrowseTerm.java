/*
 *
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 June 2012) $
 * $Author: dcosta $
 * $Revision: 2145 $
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

package edu.lternet.pasta.portal.search;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.client.ResultSetUtility;


/**
 * The BrowseTerm class models the data and methods for an individual
 * browse term.
 * 
 * @author dcosta
 *
 */
public class BrowseTerm {

  /*
   * Class fields
   */
  private static final Logger logger = Logger.getLogger(BrowseTerm.class);
  
  
  /*
   * Instance fields
   */
  private String browseTermPath = null;
  private int matchCount = 0;
  private String queryString = null;
  private TermsList termsList;
  private final String value;    // Text value of this browse term, e.g. "percent carbon"
  private String displayValue;

  // The term ID in the LTER Controlled Vocabulary
  private String termId = null;  
  private int level = 1;

  
  /*
   * Constructors
   */
 
  /**
   * Constructor. Initialize this browse term with its text value, e.g. "percent carbon"
   */
  public BrowseTerm(String value) {
    String fileSeparator = System.getProperty("file.separator");
    this.value = value;
    this.displayValue = value;
    if (isLTERSite()) {
        LTERSite lterSite = new LTERSite(value);
        displayValue = lterSite.getSiteName();
    }   
    this.browseTermPath = BrowseSearch.browseCacheDir + fileSeparator + fileName() + ".term";
    this.queryString = composeQueryString();
    this.termsList = new TermsList();
    termsList.addTerm(displayValue);
  }


  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  private int calculateIndent() {
	  return (level + 1) * 4;
  }
  
  
  /*
   * Passes through to use the SimpleSearch logic to compose the query string.
   */
  private String composeQueryString() {
    boolean isSiteQuery = isLTERSite();
    String searchValue = null;
    
    if (isSiteQuery) { 
    	searchValue = this.value.toLowerCase();
    } 
    else {
    	searchValue = this.value;
    }
    
    if (searchValue != null) {
    	if (searchValue.contains(" ") && (!searchValue.startsWith("\""))) {
    		searchValue = String.format("\"%s\"", searchValue);
    	}
    }
    
    SimpleSearch simpleSearch = new SimpleSearch();
    String queryString = simpleSearch.buildSolrQuery(searchValue, isLTERSite());
    
    return queryString;
  }
  
  
  /**
   * Queries the Data Package Manager for data packages matching this browse term's value.
   */
  public void crawl() {
    String searchResults = runQuery(); 
    writeSearchResults(searchResults);
  }
  
  
  public boolean isLterSiteTerm(String value) {
	  LTERSite lterSite = new LTERSite(value);
	  return lterSite.isValidSite();
  }
  

  /**
   * Generates an XML string for storing this browse term in the browse cache
   * on disk as a <term> element.
   */
  String toXML() {
    String cacheString = null;
    String searchResults = readSearchResults();
    StringBuffer stringBuffer = new StringBuffer("");
    int indent = calculateIndent();
    
    for (int i = 0; i < indent; i++) {
    	stringBuffer.append(" ");
    }
    stringBuffer.append(String.format("<term level='%d' hasMoreDown='0'>\n", 
    		                          this.level));
    for (int i = 0; i < indent + 4; i++) {
    	stringBuffer.append(" ");
    }
    stringBuffer.append("<value>" + value + "</value>\n");

    if (searchResults != null) {
        /* Count the number of matching documents */
        this.matchCount = 0;

        try {
    		ResultSetUtility resultSetUtility = new ResultSetUtility(searchResults, Search.DEFAULT_SORT);
    		Integer numFound = resultSetUtility.getNumFound();
    		this.matchCount = numFound;
    	}
    	catch (ParseException e) {
    		logger.error(String.format("Error parsing search results: %s", e.getMessage()));
    		e.printStackTrace();
    	}
    	      
      for (int i = 0; i < indent + 4; i++) {
      	stringBuffer.append(" ");
      }
      
      stringBuffer.append(String.format("<matchCount>%d</matchCount>\n", this.matchCount));
    }
    
    for (int i = 0; i < indent; i++) {
    	stringBuffer.append(" ");
    }
    stringBuffer.append("</term>\n");
    
    cacheString = stringBuffer.toString();
    return cacheString;
  }

  
  /**
   * Returns the match count for this browse term.
   * 
   * @return   matchCount, the number of datasets that matched this browse term
   */
  public int getMatchCount() {
    return matchCount;
  }
 
  
  /**
   * Returns this browse term's value.
   * 
   * @return   value, e.g. "bird"
   */
  public String getValue() {
    return value;
  }
  
  
  private boolean isLTERSite() {
    boolean isSite = false;
    
    LTERSite lterSite = new LTERSite(this.value);
    isSite = lterSite.isValidSite();
    
    return isSite;
  }
  
  
  /**
   * Runs the query for a browse search.
   * 
   * @return resultsetXML  XML search results from the Data Package Manager web service
   */
  public String runQuery() {
    String uid = "public";         // All browse-based searches are by public user
    String resultsetXML = null;
    
    try {  
      DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
      String extendedQueryString = String.format("%s&start=%d&rows=%d&sort=%s", 
    		  this.queryString, Search.DEFAULT_START, Search.DEFAULT_ROWS, Search.DEFAULT_SORT);		
      resultsetXML = dpmClient.searchDataPackages(extendedQueryString);    
    } 
    catch (PastaAuthenticationException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    } 
    catch (PastaConfigurationException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    } 
    catch (Exception e) {
      logger.error(e.getMessage());
      e.printStackTrace();
    }

    return resultsetXML;
  }


  /**
   * Sets the matchCount instance field for this browse term. This is done when
   * the browse cache is read from file.
   * 
   * @param matchCount
   *          the number of datasets matched by this browse term
   */
  public void setMatchCount(int matchCount) {
    this.matchCount = matchCount;
  }

  
  /**
   * Create the HTML to display this browse term on the browse page.
   * If this browse term matches at least one document, then display it as a 
   * link; otherwise, just display it as a text value.
   * 
   * @return htmlString, the HTML string to be displayed on the browse page.
   */
  public String toHTML() {
    String htmlString;
    StringBuffer stringBuffer = new StringBuffer("");
    
    if (matchCount > 0) {
      String encodedValue = value;     
      try {
        encodedValue = URLEncoder.encode(value, "UTF-8");
      }
      catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      
      stringBuffer.append(String.format("<a class='browseterm' href='./browseServlet?searchValue=%s'>", 
    		                            encodedValue));
      stringBuffer.append(displayValue);
      stringBuffer.append(" (" + matchCount + ")");
      stringBuffer.append("</a>");
    }
    else {
      stringBuffer.append(String.format("<span class='browsetermnolink'>%s</span>", displayValue));
    }

    htmlString = stringBuffer.toString();
    return htmlString;
  }

  
  /**
   * Composes the filename for this browse term by replacing spaces with
   * underscores.
   * 
   * @return
   */
  private String fileName () {
    return value.replace(' ', '_');
  }
  
  
  public String getQueryString() {
	  return queryString;
  }
  
  
  /*
   * Accessor method for the termsList instance variable.
   * 
   * @return  termsList, a TermsList object
   */
  public TermsList getTermsList() {
    return termsList;
  }
  
  
  /**
   * Reads the search results from file.
   * 
   * @return  searchResults, an XML string
   */
  public String readSearchResults() {
    File browseCacheFile = new File(browseTermPath);
    String searchResults = null;
    
    try {
    	if (browseCacheFile.exists()) {
            searchResults = FileUtils.readFileToString(browseCacheFile);
    	}
    }
    catch (IOException e) {
      e.printStackTrace();
    }
    
    return searchResults;
  }
  
  
  public void setLevel(int n) {
	  this.level = n;
  }
  
 
  public void setTermId(String id) {
	  this.termId = id;
  }
  
 
  /**
   * Writes the search results to file.
   * 
   * @param searchResults      an XML string holding the search results
   */
  private void writeSearchResults(String searchResults) {
    File browseCacheFile = new File(browseTermPath);
    StringBuffer stringBuffer = new StringBuffer("");
    
    stringBuffer.append(searchResults);
    
    try {
      FileUtils.writeStringToFile(browseCacheFile, stringBuffer.toString());
    }
    catch (IOException e) {
      logger.error("IOException:\n" + e.getMessage());
      e.printStackTrace();
    }
  }

}
