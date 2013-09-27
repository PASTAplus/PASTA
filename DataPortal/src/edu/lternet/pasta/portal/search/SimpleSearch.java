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

import org.apache.commons.lang3.text.StrTokenizer;

/**
 * The SimpleSearch class supports query operations common to the simple
 * search and browse search interfaces.
 * 
 * @author dcosta
 *
 */
public class SimpleSearch {

  /**
   * Builds a PathQuery XML string for submission to the DataPackageManager
   * and then to Metacat.
   * 
   * @param terms        The string terms entered by the user (e.g. "climate change")
   * @param termsList    List of terms used in the search, which may include terms other
   * @param tokenize     If true, splits the terms string into tokens on space, tab, 
   *                     newline and formfeed, else splits only on newline treating
   *                     space-separated words as a single term.
   * @param isSiteQuery  true if we are querying by site name, else false
   * @return  the PathQuery XML string
   */
  public static String buildPathQueryXml(String terms, TermsList termsList, boolean tokenize, boolean isSiteQuery) {
    StrTokenizer strTokenizer = null;
    final String openQueryTerm = 
      "    <queryterm casesensitive=\"false\" searchmode=\"contains\">\n";
    final String closeQueryTerm = "    </queryterm>\n";
    String[] xpaths;
    String[] sitePaths = { "@packageId" };
    String[] nonSitePaths = { "dataset/title", "dataset/abstract", "dataset/keywordSet/keyword" };
    
    if (isSiteQuery)  {
      xpaths = sitePaths;
    }
    else {
      xpaths = nonSitePaths;
    }
    
    StringBuffer query = new StringBuffer(
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
        + "<pathquery version=\"1.0\">\n"
        + "  <meta_file_id>unspecified</meta_file_id>\n"
        + "  <querytitle>unspecified</querytitle>\n"
        + "  <returnfield>dataset/title</returnfield>\n"
        + "  <returnfield>dataset/creator/individualName/surName</returnfield>\n"
        + "  <returnfield>dataset/pubDate</returnfield>\n"
        + "  <returnfield>originator/individualName/surName</returnfield>\n"
        + "  <returndoctype>eml://ecoinformatics.org/eml-2.1.0</returndoctype>\n"
        + "  <returndoctype>eml://ecoinformatics.org/eml-2.1.1</returndoctype>\n"
        + "  <querygroup operator=\"UNION\">\n");
    
    if (tokenize) {
      strTokenizer = new StrTokenizer(terms);  // splits on space, tab, newline and formfeed
    }
    else {
      strTokenizer = new StrTokenizer(terms, "\n"); // splits only on newline
    }
    
    while(strTokenizer.hasNext()) {
      String token = strTokenizer.nextToken();    
      termsList.addTerm(token);     
      String value = "      <value>" + token + "</value>\n";
      
      for (String xpath : xpaths) {
        query.append(openQueryTerm);
        query.append(value);
        query.append(String.format("      <pathexpr>%s</pathexpr>\n", xpath));
        query.append(closeQueryTerm);
      }     
    }
    
    query.append("  </querygroup>\n" + "</pathquery>\n");

    String queryStr = query.toString();
    return queryStr;
  }
  
}
