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

import java.util.List;

import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;

/**
 * The SimpleSearch class supports query operations common to the simple
 * search and browse search interfaces.
 * 
 * @author dcosta
 *
 */
public class SimpleSearch extends Search {

	/*
	 * Class fields
	 */

    private static final Logger logger = Logger.getLogger(SimpleSearch.class);

	  
  /**
   * Builds a PathQuery XML string for submission to the DataPackageManager
   * and then to Metacat.
   * 
   * @param userInput    The terms entered by the user (e.g. "climate change")
   * @param termsList    List of terms used in the search, which may include terms other
   * @param isSiteQuery  true if we are querying by site name, else false
   * @return the PathQuery XML string
   */
  public static String buildPathQueryXml(String userInput, TermsList termsList, boolean isSiteQuery) {
    final String operator = "UNION";
    final String title = "Simple Search";
    final String searchMode = "contains";
    final String caseSensitive = "false";
    final String indent = "    ";
    
    List<String> sitePaths = getIndexedPaths(false, false, true, false);
    List<String> nonSitePaths = getIndexedPaths(true, true, true, true);
    List<String> xpaths = isSiteQuery ? sitePaths : nonSitePaths;
    List<String> terms = parseTerms(userInput);
    AdvancedSearchQueryGroup queryGroup = new AdvancedSearchQueryGroup(operator, indent);
    
    for (String term : terms) {
      termsList.addTerm(term);           
      for (String xpath : xpaths) {
        AdvancedSearchQueryTerm queryTerm = 
        		new AdvancedSearchQueryTerm(
        				searchMode, caseSensitive, xpath, term, indent + indent);
        queryGroup.addQueryTerm(queryTerm);
      }
    }
    
    AdvancedSearchPathQuery pathQuery = new AdvancedSearchPathQuery(title, queryGroup, indent);
    String pathqueryXML = pathQuery.pathqueryXML();
    logger.debug(pathqueryXML);
    return pathqueryXML;
  }
  
}
