package edu.lternet.pasta.datapackagemanager.solr.search;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

public class SimpleSolrSearch {

	private String solrUrl = "http://localhost:8983/solr/collection1";
	private SolrServer server;


	public SimpleSolrSearch() {
		server = new HttpSolrServer(solrUrl);
	}


	public Collection<Integer> search(String searchTerms, 
									  double maxPrice) 
			throws SolrServerException {
		SolrQuery solrQuery = new SolrQuery();
		solrQuery.addFilterQuery("price:[1 TO " + maxPrice + "]");
		solrQuery.setQuery(searchTerms);

		QueryResponse queryResponse = server.query(solrQuery);
		SolrDocumentList solrDocumentList = queryResponse.getResults();
		List<Integer> docIds = new ArrayList<>();
		
		for (SolrDocument solrDocument : solrDocumentList) {
			int id = Integer.parseInt((String) solrDocument.getFirstValue("id"));
			docIds.add(id);
		}
		
		return docIds;
	}
	
	
	public static void main(String[] args) {
		SimpleSolrSearch simpleSolrSearch =  new SimpleSolrSearch();
		String searchTerms = "Gouda";
		double maxPrice = 50.0;
		
		try {
			Collection<Integer> docIds = simpleSolrSearch.search(searchTerms, maxPrice);
			System.out.println(String.format("%d docIds matched", docIds.size()));
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
	}
}
