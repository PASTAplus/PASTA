package edu.lternet.pasta.datapackagemanager.solr.search;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SimpleSolrSearch {

	private SolrServer solrServer;


	/*
	 * Constructors
	 */
	
	public SimpleSolrSearch(String serverURL) {
		this.solrServer = new HttpSolrServer(serverURL);
	}
	
	
	/*
	 * Instance methods
	 */
	
	public String search(String queryText) 
			throws SolrServerException {
		String xmlString = null;
		SolrQuery solrQuery = new SolrQuery();
		//solrQuery.addFilterQuery("pubdate:[1900-01-01T00:00:00Z TO " + endDate + "]");
		solrQuery.setQuery(queryText);

		QueryResponse queryResponse = solrServer.query(solrQuery);
		SolrDocumentList solrDocumentList = queryResponse.getResults();
		xmlString = solrDocumentListToXML(solrDocumentList);
		
		return xmlString;
	}
	
	
	private String solrDocumentListToXML(SolrDocumentList solrDocumentList) {
		String xmlString = "";
		StringBuilder sb = new StringBuilder("<resultset>\n");
		
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
			if (authors != null) {
				for (Object author : authors) {
					String authorStr = (String) author;
					sb.append(String.format("      <author>%s</author>\n", authorStr));
				}
			}
			sb.append("    </authors>\n");
			
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
		
		try {
			String xmlString = simpleSolrSearch.search(queryText);
			System.out.println(xmlString);
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
}
