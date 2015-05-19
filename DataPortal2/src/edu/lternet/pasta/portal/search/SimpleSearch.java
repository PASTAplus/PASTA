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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

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
   * Builds a query for submission to the DataPackageManager
   * and then to Solr.
   * 
   * @param userInput    The terms entered by the user (e.g. "climate change")
   * @param termsList    List of terms used in the search, which may include terms other
   * @param isSiteQuery  true if we are querying by site name, else false.
   * 
   *                     When true, the user input is being sent from the browse crawler
   *                     and its value will be a site acronym like "KNZ".
   *                     
   *                     When false, the user input is being sent from the search form
   *                     and will be anything that the end user has typed in the search box,
   *                     or, it may be a controlled vocabulary term sent from the browse
   *                     crawler. 
   * @return the Solr query string, including any filter queries, to be sent to Solr
   *         for processing
   */
	public String buildSolrQuery(String userInput, boolean isSiteQuery) {
		String solrQuery = null;
		String qString = DEFAULT_Q_STRING;
		String siteFilter = "";
		List<String> terms;

		if (userInput != null && !userInput.equals("")) {
			terms = parseTerms(userInput);
			
			for (String term : terms) {
				termsList.addTerm(term);
			}
			
			try {
				if (isSiteQuery) {
					siteFilter = String.format("&fq=scope:(knb-lter-%s)", userInput.toLowerCase());
				}
				else {
					String escapedInput = Search.escapeQueryChars(userInput);
					qString = URLEncoder.encode(escapedInput, "UTF-8");
				}
			}
			catch (UnsupportedEncodingException e) {

			}
			
			solrQuery = String.format(
					"defType=%s&q=%s%s&fq=%s&fq=%s&fl=%s&debug=%s",
					DEFAULT_DEFTYPE, qString, siteFilter, ECOTRENDS_FILTER,
					LANDSAT_FILTER, DEFAULT_FIELDS, DEFAULT_DEBUG);
		}

		return solrQuery;
	}
  
}
