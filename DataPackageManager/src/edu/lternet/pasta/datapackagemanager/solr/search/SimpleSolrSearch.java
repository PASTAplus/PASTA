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

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
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
	
	/**
	 * Default rows value. 
	 * @TODO This value needs to be kept consistent with the default rows value configured
	 *       in the solrconfig.xml file used by Solr. It would be good if this could somehow 
	 *       be automated, but that's not easy, so hard-code for now.
	 */
	private static final Integer ROWS_DEFAULT = new Integer(10); 
	
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

	private Integer rows = ROWS_DEFAULT;
	private SolrClient solrClient;
	private SolrQuery solrQuery;
	

	/*
	 * Constructors
	 */
	
	public SimpleSolrSearch(String serverURL) {
		this.solrClient = new HttpSolrClient.Builder(serverURL).build();
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
	
	
	private boolean isFloatField(String fieldName) {
		boolean isFloat = false;
		
		if (fieldName != null) {
			return (fieldName.equals("score")
				   );
		}
		
		return isFloat;
	}
	
	
	/**
	 * Executes a Solr search.
	 * 
	 * @return an XML string containing the search results
	 * @throws SolrServerException
	 */
	public String search() 
			throws IOException, SolrServerException {
		QueryResponse queryResponse = solrClient.query(solrQuery);
		solrClient.close();  // Must now explicitly close the MF solr connection
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
		
		String firstLine = String.format("<resultset numFound='%d' start='%d' rows='%d'>\n", numFound, start, this.rows);
		StringBuilder sb = new StringBuilder(firstLine);
		
		for (SolrDocument solrDocument : solrDocumentList) {
			sb.append(String.format("%s<document>\n", INDENT));
			
			if (fieldsArray != null) {
				
				for (String fieldName : fieldsArray) {
					
					String wrapperElement = wrapperElements.get(fieldName);
					
					if (fieldName.equals("title")) {			
						String rawTitle = (String) solrDocument.getFirstValue("title");
						String title = StringEscapeUtils.escapeXml(rawTitle);
						sb.append(String.format("%s%s<%s>%s</%s>\n",
												INDENT, INDENT, fieldName, title, fieldName));
					}
					else if (fieldName.equals("projectTitle") || fieldName.equals("relatedProjectTitle")) {			
						Object fieldValue = solrDocument.getFieldValue(fieldName);
						if (fieldValue != null) {
							Collection<Object> fieldValues = solrDocument.getFieldValues(fieldName);
							for (Object value : fieldValues) {
								String rawTitle = (String) value;
								String title = StringEscapeUtils.escapeXml(rawTitle);
								sb.append(String.format("%s%s<%s>%s</%s>\n", INDENT, INDENT, fieldName, title, fieldName));
							}
						}
						else {
							sb.append(String.format("%s%s<%s></%s>\n", INDENT, INDENT, fieldName, fieldName));
						}
					}
					else if (wrapperElement != null) {
						sb.append(String.format("%s%s<%s>\n", INDENT, INDENT, wrapperElement));
						Collection<Object> multiValues = solrDocument.getFieldValues(fieldName);
						if (multiValues != null && multiValues.size() > 0) {
							for (Object value : multiValues) {
								String rawValueStr = null;
								if (isDateField(fieldName)) {
									Date dateValue = (Date) value;
									dateValue = adjustDate(dateValue);
									String formatPattern = bestDateFormat(fieldName);
									SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
									if (dateValue != null) {
										rawValueStr = sdf.format(dateValue);
									}
								}
								else {
									rawValueStr = (String) value;
								}
								
								String valueStr = StringEscapeUtils.escapeXml(rawValueStr);
								sb.append(String.format("%s%s%s<%s>%s</%s>\n", 
										                INDENT, INDENT, INDENT, fieldName, valueStr, fieldName));
							}
						}
						sb.append(String.format("%s%s</%s>\n", INDENT, INDENT, wrapperElement));
					}
					else {
						String rawValueStr = null;
						Object o = solrDocument.getFieldValue(fieldName);
						if (o != null) {
							if (isDateField(fieldName)) {
								Date dateValue = (Date) o;
								dateValue = adjustDate(dateValue);
								String formatPattern = bestDateFormat(fieldName);
								SimpleDateFormat sdf = new SimpleDateFormat(formatPattern);
								if (dateValue != null) {
									rawValueStr = sdf.format(dateValue);
								}
							} else if (isFloatField(fieldName)) {
								Float floatValue = (Float) o;
								rawValueStr = floatValue.toString();
							} else {
								rawValueStr = o.toString();
							}
						}
						
						if (rawValueStr == null) { 
							rawValueStr = "";
						}
						
						String valueStr = StringEscapeUtils.escapeXml(rawValueStr);
						sb.append(String.format("%s%s<%s>%s</%s>\n",
				                                INDENT, INDENT, fieldName, valueStr, fieldName));
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
	
	
	public static void main(String[] args) throws IOException {
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
