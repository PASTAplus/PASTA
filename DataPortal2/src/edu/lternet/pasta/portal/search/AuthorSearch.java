/*
 *
 * $Date: 2015-11-04 12:23:25 -0700 (Wed, 4 Nov 2015) $
 * $Author: dcosta $
 * $Revision:  $
 *
 * Copyright 2011-2015 the University of New Mexico.
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

import java.util.Date;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;


/**
 * The AuthorSearch class supports advanced search on authors and
 * their organizations.
 * 
 * @author Duane Costa
 *
 */
public class AuthorSearch extends Search {

	/*
	 * Class fields
	 */

	private static final Logger logger = Logger.getLogger(AuthorSearch.class);

	protected final static String AUTHOR_Q_STRING = "author:*";
	protected final static String ORGANIZATION_Q_STRING = "organization:*";
	protected final static String AUTHOR_FIELDS = "author";
	protected final static String ORGANIZATION_FIELDS = "organization";
	public final static int AUTHOR_ROWS = 100000;

	private static String[] authors = { "" };
	private static String[] organizations = { "" };
	private static long lastRefreshTime = 0L;


    /**
     * Builds a query on all author names in PASTA for submission to 
     * the DataPackageManager and then to Solr.
     * 
     * @return the Solr query string to be sent to Solr for searching
     *         all author names
     */
  	public static String buildAuthorQuery() {
  		String solrQuery = null;
  		String qString = AUTHOR_Q_STRING;

  		solrQuery = String.format(
  					"defType=%s&q=%s&fq=%s&fq=%s&fl=%s&debug=%s&start=%d&rows=%d",
  					DEFAULT_DEFTYPE, qString, ECOTRENDS_FILTER, LANDSAT_FILTER, AUTHOR_FIELDS, 
  					DEFAULT_DEBUG, DEFAULT_START, AUTHOR_ROWS);

  		return solrQuery;
  	}
    

  	
    /**
     * Builds a query on all author organizations in PASTA for submission to 
     * the DataPackageManager and then to Solr.
     * 
     * @return the Solr query string to be sent to Solr for searching
     *         all author organizations
     */
  	public static String buildOrganizationQuery() {
  		String solrQuery = null;
  		String qString = ORGANIZATION_Q_STRING;

  		solrQuery = String.format(
  					"defType=%s&q=%s&fq=%s&fq=%s&fl=%s&debug=%s&start=%d&rows=%d",
  					DEFAULT_DEFTYPE, qString, ECOTRENDS_FILTER, LANDSAT_FILTER, ORGANIZATION_FIELDS, 
  					DEFAULT_DEBUG, DEFAULT_START, AUTHOR_ROWS);

  		return solrQuery;
  	}
    

  	/**
     * Composes an HTML string, a list of options elements, 
     * one per author name value.
     * For use inside an HTML <select> element.
     * 
     * @return a string of HTML holding the options list
     *         of author names
     */
	public static String composeAuthorNameOptions() {
		StringBuffer htmlStringBuffer = new StringBuffer("");
		String indent = "    ";

		htmlStringBuffer.append(String.format("%s<option value=\"\"></option>\n", indent));
		for (int i = 0; i < authors.length; i++) {
			String author = authors[i];
			htmlStringBuffer.append(indent);
			htmlStringBuffer.append("<option value=\"");
			htmlStringBuffer.append(author);
			htmlStringBuffer.append("\"");
			htmlStringBuffer.append(">");
			htmlStringBuffer.append(author);
			htmlStringBuffer.append("</option>\n");
		}

		return htmlStringBuffer.toString();
	}

    
  	/**
     * Composes an HTML string, a list of options elements, 
     * one per author organization value.
     * For use inside an HTML <select> element.
     * 
     * @return a string of HTML holding the options list
     *         of author organizations
     */
	public static String composeAuthorOrganizationOptions() {
		StringBuffer htmlStringBuffer = new StringBuffer("");
		String indent = "    ";

		htmlStringBuffer.append(String.format("%s<option value=\"\"></option>\n", indent));
		for (int i = 0; i < organizations.length; i++) {
			String organization = organizations[i];
			htmlStringBuffer.append(indent);
			htmlStringBuffer.append("<option value=\"");
			htmlStringBuffer.append(organization);
			htmlStringBuffer.append("\"");
			htmlStringBuffer.append(">");
			htmlStringBuffer.append(organization);
			htmlStringBuffer.append("</option>\n");
		}

		return htmlStringBuffer.toString();
	}
	
	
	/**
	 * Updates the authors and organizations values by executing a
	 * Solr query for each.
	 */
	public static void updateAuthorsAndOrganizations() {
		if (authors.length < 2 || 
			organizations.length < 2 || 
			shouldRefresh(lastRefreshTime)
		   ) {
			try {
				logger.warn("Begin refreshing creator names cache.");
				String authorQuery = buildAuthorQuery();
				updateAuthors(authorQuery);
				logger.warn("Finished refreshing creator names cache.");

				logger.warn("Begin refreshing creator organizations cache.");
				String organizationQuery = buildOrganizationQuery();
				updateOrganizations(organizationQuery);
				logger.warn("Finished refreshing creator organizations cache.");

				Date now = new Date();
				lastRefreshTime = now.getTime();
			}
			catch (Exception e) {
				logger.error("Error updating creators and organizations: "
						+ e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Boolean to determine whether the cache is due to be
	 * refreshed. Returns true is the current time has advanced past the last
	 * refresh time plus a time-to-live period.
	 * 
	 * @param  lastRefreshTime   the time when the cache was last refreshed
	 */
	private static boolean shouldRefresh(long lastRefreshTime) {
		double hours = 0.5;
		boolean shouldRefresh = false;
		final long timeToLive = (long) (hours * 60 * 60 * 1000);
		Date now = new Date();
		long nowTime = now.getTime();
		long refreshTime = lastRefreshTime + timeToLive;

		if (refreshTime < nowTime) {
			shouldRefresh = true;
		}

		return shouldRefresh;
	}

	  
	/**
	 * Updates the author name values by executing a Solr query.
	 * 
	 * @param authorQuery    the query string to send to Solr
	 * @throws Exception    
	 */
	private static void updateAuthors(String authorQuery) throws Exception {
		String authorsXML = executeQuery(authorQuery);
		String[] authorsUpdated = parseQueryResults(authorsXML, "author");
		authors = authorsUpdated;
	}
	
	
	/**
	 * Updates the author organization values by executing a Solr query.
	 * 
	 * @param organizationQuery   the query string to send to Solr
	 * @throws Exception    
	 */
	private static void updateOrganizations(String organizationQuery) throws Exception {
		String organizationsXML = executeQuery(organizationQuery);
		String[] organizationsUpdated = parseQueryResults(organizationsXML, "organization");
		organizations = organizationsUpdated;
	}
	
	
	/**
	 * Parses the Solr query results using regular expression matching (as
	 * opposed to XML parsing)
	 * 
	 * @param xml             the Solr query results, an XML document string
	 * @param fieldName       the field name to parse out of the XML, e.g. "author"
	 * @return                a String array of field values parsed from the XML
	 */
	private static String[] parseQueryResults(String xml, String fieldName) {
		String[] values = null;
		final String patternStr = String.format("^\\s*<%s>(.+)</%s>\\s*$", fieldName, fieldName);
		Pattern pattern = Pattern.compile(patternStr);
		TreeSet<String> valueSet = new TreeSet<String>();
		
		if (xml != null) {
			String[] lines = xml.split("\n");
			for (String line : lines) {
				Matcher matcher = pattern.matcher(line);
				if (matcher.matches()) {
					String capturedValue = matcher.group(1).trim();
					String unescapedXML = StringEscapeUtils.unescapeXml(capturedValue);
					String trimmedXML = unescapedXML.replace("\r", " ").replace("\n", " ").replaceAll("\\s+", " ").trim();
					String escapedXML = StringEscapeUtils.escapeXml(trimmedXML);
					valueSet.add(escapedXML);
				}
			}
			
			values = valueSet.toArray(new String[valueSet.size()]);
		}
		
		return values;
	}
	
	
	/**
	 * Executes a Solr query for authors or organizations by using
	 * the Data Package Manager's "search" web service
	 * 
	 * @param queryText     the query text sent to Solr
	 * @return              the XML query results return by the web service
	 * @throws Exception    
	 */
	private static String executeQuery(String queryText) throws Exception
	{
		String uid = "public";
		DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
		String xml = dpmClient.searchDataPackages(queryText);
		return xml;
	}
      
}
