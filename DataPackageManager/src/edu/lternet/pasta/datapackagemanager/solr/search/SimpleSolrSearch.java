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

public class SimpleSolrSearch {

	/*
	 * Class fields
	 */

	private static final Logger logger = Logger.getLogger(SimpleSolrSearch.class);
		
		
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
		
		String firstLine = String.format("<resultset numFound='%d' start='%d' rows='%d'>\n", numFound, start, rows);
		StringBuilder sb = new StringBuilder(firstLine);
		
		for (SolrDocument solrDocument : solrDocumentList) {
			sb.append("  <document>\n");
			
			String yearStr = "";
			String packageId = (String) solrDocument.getFieldValue("packageid");
			Date pubDate = (Date) solrDocument.getFieldValue("pubdate");
			SimpleDateFormat sdf = new SimpleDateFormat("YYYY");
			if (pubDate != null) {
			    yearStr = sdf.format(pubDate);
			}
			String title = (String) solrDocument.getFirstValue("title");
			
			sb.append(String.format("    <packageId>%s</packageId>\n", packageId));
			sb.append(String.format("    <pubDate>%s</pubDate>\n", yearStr));
			sb.append(String.format("    <title>%s</title>\n", title));
			
			sb.append("    <authors>\n");
			Collection<Object> authors = solrDocument.getFieldValues("author");
			if (authors != null && authors.size() > 0) {
				for (Object author : authors) {
					String authorStr = (String) author;
					sb.append(String.format("      <author>%s</author>\n", authorStr));
				}
			}
			sb.append("    </authors>\n");
			
			sb.append("    <organizations>\n");
			Collection<Object> organizations = solrDocument.getFieldValues("organization");
			if (organizations != null && organizations.size() > 0) {
				for (Object organization : organizations) {
					String organizationStr = (String) organization;
					sb.append(String.format("      <organization>%s</organization>\n", organizationStr));
				}
			}
			sb.append("    </organizations>\n");
			
			
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
