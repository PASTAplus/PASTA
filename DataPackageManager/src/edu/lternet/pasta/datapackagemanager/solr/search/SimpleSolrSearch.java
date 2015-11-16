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
import java.util.TreeMap;

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
	
	private static final String[] ALL_FIELDS =
		{ 
			"abstract",
			"begindate",
			"doi",
			"enddate",
			"funding",
			"geographicdescription",
			"id",
			"methods",
			"packageid",
			"pubdate",
			"responsibleParties",
			"scope",
			"site",
			"taxonomic",
			"title",
		
			// multivalued fields
			"author",
			"coordinates",
			"derivedFrom",
			"keyword",
			"organization",
			"singledate",
			"timescale"
		};
	
	/*
	 * Search results for multivalued fields need to be wrapped inside a parent element.
	 * For example, "author" elements are wrapped inside an "authors" element.
	 */
	private static TreeMap<String, String> wrapperElements = null;
	
	static {
		wrapperElements = new TreeMap<String, String>();
		wrapperElements.put("author", "authors");
		wrapperElements.put("coordinates", "spatialCoverage");
		wrapperElements.put("derivedFrom", "sources");
		wrapperElements.put("keyword", "keywords");
		wrapperElements.put("organization", "organizations");
		wrapperElements.put("singledate", "singledates");
		wrapperElements.put("timescale", "timescales");
	}
		
	
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
	 * Return an array of field names that were specified
	 * by the query in its "fl" (field list) parameter.
	 */
	private String[] getFieldList() {
		final String WILDCARD = "*";
		String[] fieldsArray = null;
		
		String fieldsStr = solrQuery.getFields();
		if ((fieldsStr != null) && (fieldsStr.length() > 0)) {
			if (fieldsStr.equals(WILDCARD)) {
				fieldsArray = ALL_FIELDS;
			}
			else {
				fieldsArray = fieldsStr.split(",");
			}
		}
		
		return fieldsArray;
	}
	
	
	private boolean isDateField(String fieldName) {
		boolean isDate = false;
		
		if (fieldName != null) {
			return (fieldName.equals("pubdate") ||
					fieldName.equals("singledate") ||
					fieldName.equals("begindate") ||
					fieldName.equals("enddate")
				   );
		}
		
		return isDate;
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
		final String INDENT = "    ";
		long numFound = solrDocumentList.getNumFound();
		long start = solrDocumentList.getStart();
		String[] fieldsArray = getFieldList();
		
		String firstLine = String.format("<resultset numFound='%d' start='%d' rows='%d'>\n", numFound, start, rows);
		StringBuilder sb = new StringBuilder(firstLine);
		
		for (SolrDocument solrDocument : solrDocumentList) {
			sb.append(String.format("%s<document>\n", INDENT));
			
			if (fieldsArray != null) {
				
				for (String fieldName : fieldsArray) {
					
					String wrapperElement = wrapperElements.get(fieldName);
					
					if (fieldName.equals("title")) {			
						String title = (String) solrDocument.getFirstValue("title");
						sb.append(String.format("%s%s<%s>%s</%s>\n",
												INDENT, INDENT, fieldName, title, fieldName));
					}
					else if (wrapperElement != null) {
						sb.append(String.format("%s%s<%s>\n", INDENT, INDENT, wrapperElement));
						Collection<Object> multiValues = solrDocument.getFieldValues(fieldName);
						if (multiValues != null && multiValues.size() > 0) {
							for (Object value : multiValues) {
								String valueStr = null;
								if (isDateField(fieldName)) {
									Date dateValue = (Date) value;
									dateValue = adjustDate(dateValue);
									String formatPattern = bestDateFormat(fieldName);
									SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
									if (dateValue != null) {
										valueStr = sdf.format(dateValue);
									}
								}
								else {
									valueStr = (String) value;
								}
								
								sb.append(String.format("%s%s%s<%s>%s</%s>\n", 
										                INDENT, INDENT, INDENT, fieldName, valueStr, fieldName));
							}
						}
						sb.append(String.format("%s%s</%s>\n", INDENT, INDENT, wrapperElement));
					}
					else {					
						String fieldValue = "";
						if (isDateField(fieldName)) {
							Date dateValue = (Date) solrDocument.getFieldValue(fieldName);
							dateValue = adjustDate(dateValue);
							String formatPattern = bestDateFormat(fieldName);
							SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
							if (dateValue != null) {
								fieldValue = sdf.format(dateValue);
							}
						}
						else {
							fieldValue = (String) solrDocument.getFieldValue(fieldName);
							if (fieldValue == null) fieldValue = "";
						}
						sb.append(String.format("%s%s<%s>%s</%s>\n",
				                                INDENT, INDENT, fieldName, fieldValue, fieldName));

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
							sb.append(String.format("%s%s<docid>%s</docid>\n", INDENT, INDENT, fieldValue));
						}
						else if (fieldName.equals("packageid")) {
							sb.append(String.format("%s%s<packageId>%s</packageId>\n", INDENT, INDENT, fieldValue));
						}
						else if (fieldName.equals("pubdate")) {
							sb.append(String.format("%s%s<pubDate>%s</pubDate>\n", INDENT, INDENT, fieldValue));
						}
					}
				}
			}
			
		    sb.append(String.format("%s</document>\n", INDENT));
		}
		
		sb.append("</resultset>\n");
		xmlString = sb.toString();
		
		return xmlString;
	}
	
	
	private Date adjustDate(Date date) {
		Date adjustedDate = null;
		long twelveHours = (3600 * 1000 * 12);
		
		if (date != null) {
			long adjustedTime = date.getTime() + twelveHours;
			adjustedDate = new Date(adjustedTime);
		}
		
		return adjustedDate;
	}
	
	private String bestDateFormat(String fieldName) {
		String bestDateFormat = "yyyy-MM-dd";
		if (fieldName != null && fieldName.equalsIgnoreCase("pubdate")) {
			bestDateFormat = "YYYY";
		}
		
		return bestDateFormat;
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
