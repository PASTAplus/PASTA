/*
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
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

package edu.lternet.pasta.datapackagemanager.solr.search;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * The SimpleSolrSearch class executes a simple search by the Solr
 * server via the SolrJ API. The search results are returned as an XML
 * document.
 * 
 * @author dcosta
 *
 */
public class SimpleSolrSearch {

	/*
	 * Class fields
	 */

	private static final Logger logger = Logger.getLogger(SimpleSolrSearch.class);
	private static String[] multivalueFields = 
		{ "author", "coordinates",  "organization", "title" };
		
		
	/*
	 * Instance fields
	 */

	private Integer rows = null;
	private SolrServer solrServer;
	private SolrQuery solrQuery;
	

	/*
	 * Constructors
	 */
	
	public SimpleSolrSearch(String serverURL) {
		this.solrServer = new HttpSolrServer(serverURL);
		this.solrQuery = new SolrQuery();
	}
	
	
	/*
	 * Instance methods
	 */
	
	public void addFilterQuery(String filterText) {
		this.solrQuery.addFilterQuery(filterText);
	}
	
	
	public void addSort(String field, String orderStr) {
		SolrQuery.ORDER order = SolrQuery.ORDER.desc;
		if (orderStr.equals("asc")) {
			order = SolrQuery.ORDER.asc;
		}
		
		this.solrQuery.addSort(field, order);
	}
	
	
	/*
	 * Did this Solr query include the specified field name in 
	 * its "fl" (field list) parameter?
	 */
	private boolean hasField(String fieldName) {
		boolean found = false;
		
		String[] fieldsArray = getFields();
		if (fieldsArray != null) {
			for (String token : fieldsArray) {
				if (token.equals(fieldName)) {
					found = true;
					break;
				}
			}
		}
		
		return found;
	}
	
	
	/*
	 * Return an array of field names that were specified
	 * by the query in its "fl" (field list) parameter.
	 */
	private String[] getFields() {
		String[] fieldsArray = null;
		
		String fieldsStr = solrQuery.getFields();
		if ((fieldsStr != null) && (fieldsStr.length() > 0)) {
			fieldsArray = fieldsStr.split(",");
		}
		
		return fieldsArray;
	}
	
	
	/**
	 * Executes a Solr search.
	 * 
	 * @return an XML string containing the search results
	 * @throws SolrServerException
	 */
	public String search() 
			throws SolrServerException {
		QueryResponse queryResponse = solrServer.query(solrQuery);
		SolrDocumentList solrDocumentList = queryResponse.getResults();
		String xmlString = solrDocumentListToXML(solrDocumentList);
		Map<String,Object> debugMap = queryResponse.getDebugMap();
		if (debugMap != null) System.out.println(debugMap.toString());
		
		return xmlString;
	}
	
	
	public void setDebug(boolean debug) {
		this.solrQuery.set("debug", debug);
	}
	
	
	public void setDefType(String defType) {
		this.solrQuery.setParam("defType", defType);
	}
	
	
	public void setFields(String fields) {
		this.solrQuery.setFields(fields);
	}
	
	
	public void setQueryText(String queryText) {
		this.solrQuery.setQuery(queryText);
	}
	
	
	public void setRows(String rowsStr) {
		try {
		    this.rows = new Integer(rowsStr);
		    this.solrQuery.setRows(rows);
		}
		catch (NumberFormatException e) {
			logger.warn(String.format("Unable to parse specified 'rows' value: %s", rowsStr));
		}
	}
	
	
	public void setStart(String startStr) {
		try {
		    Integer start = new Integer(startStr);
		    this.solrQuery.setStart(start);
		}
		catch (NumberFormatException e) {
			logger.warn(String.format("Unable to parse specified 'start' value: %s", startStr));
		}
	}
	
	
	private String solrDocumentListToXML(SolrDocumentList solrDocumentList) {
		String xmlString = "";
		long numFound = solrDocumentList.getNumFound();
		long start = solrDocumentList.getStart();
		String[] fieldsArray = getFields();
		
		String firstLine = String.format("<resultset numFound='%d' start='%d' rows='%d'>\n", numFound, start, rows);
		StringBuilder sb = new StringBuilder(firstLine);
		
		for (SolrDocument solrDocument : solrDocumentList) {
			sb.append("  <document>\n");
			
			if (fieldsArray != null) {
				
				for (String fieldName : fieldsArray) {
					
					if (fieldName.equals("title")) {			
						String title = (String) solrDocument.getFirstValue(fieldName);
						sb.append(String.format("    <%s>%s</%s>\n",
                                                     fieldName, title, fieldName));
					}
					else if (fieldName.equals("organization")) {
						sb.append("    <organizations>\n");
						Collection<Object> organizations = solrDocument.getFieldValues("organization");
						if (organizations != null && organizations.size() > 0) {
							for (Object organization : organizations) {
								String organizationStr = (String) organization;
								sb.append(String.format("      <organization>%s</organization>\n", organizationStr));
							}
						}
						sb.append("    </organizations>\n");
					}
					else if (fieldName.equals("author")) {
						sb.append("    <authors>\n");
						Collection<Object> authors = solrDocument.getFieldValues("author");
						if (authors != null && authors.size() > 0) {
							for (Object author : authors) {
								String authorStr = (String) author;
								sb.append(String.format("      <author>%s</author>\n", authorStr));
							}
						}
						sb.append("    </authors>\n");
					}
					else if (fieldName.equals("coordinates")) {
						sb.append("    <spatialCoverage>\n");
						Collection<Object> spatialCoverage = solrDocument.getFieldValues("coordinates");
						if (spatialCoverage != null && spatialCoverage.size() > 0) {
							for (Object coordinates : spatialCoverage) {
								String coordinatesStr = (String) coordinates;
								sb.append(String.format("      <coordinates>%s</coordinates>\n", coordinatesStr));
							}
						}
						sb.append("    </spatialCoverage>\n");
					}
					else {					
						String fieldValue = "";
						if (fieldName.equals("pubdate")) {
							Date pubDate = (Date) solrDocument.getFieldValue(fieldName);
							SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
							if (pubDate != null) {
								fieldValue = sdf.format(pubDate);
							}
						}
						else {
							fieldValue = (String) solrDocument.getFieldValue(fieldName);
							if (fieldValue == null) fieldValue = "";
						}
						sb.append(String.format("    <%s>%s</%s>\n",
				                                     fieldName, fieldValue, fieldName));

						/*
						 * Support the older format for search results.
						 * 
						 * These element names ("docid", "packageId", and "pubDate")
						 * were never officially documented but some clients might rely 
						 * on them. They should be deprecated. They have been
						 * replaced with element names that exactly match their
						 * corresponding Solr field names: "id", "packageid", and "pubdate".
						 */
						if (fieldName.equals("id")) {
							sb.append(String.format("    <docid>%s</docid>\n", fieldValue));
						}
						else if (fieldName.equals("packageid")) {
							sb.append(String.format("    <packageId>%s</packageId>\n", fieldValue));
						}
						else if (fieldName.equals("pubdate")) {
							sb.append(String.format("    <pubDate>%s</pubDate>\n", fieldValue));
						}
					}
				}
			}
			
		    sb.append("  </document>\n");
		}
		
		sb.append("</resultset>\n");
		xmlString = sb.toString();
		
		return xmlString;
	}
	
	
	public static void main(String[] args) {
		String solrUrl = "http://localhost:8983/solr/collection1";
		SimpleSolrSearch simpleSolrSearch =  new SimpleSolrSearch(solrUrl);
		String queryText = args[0];
		String rowsStr = args[1];
		
		try {
			simpleSolrSearch.setQueryText(queryText);
			simpleSolrSearch.setRows(rowsStr);
			String xmlString = simpleSolrSearch.search();
			System.out.println(xmlString);
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
	
}
