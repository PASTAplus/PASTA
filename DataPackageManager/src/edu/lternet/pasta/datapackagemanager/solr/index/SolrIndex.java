package edu.lternet.pasta.datapackagemanager.solr.index;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

public class SolrIndex {

	public void addSampleDocument() throws IOException, SolrServerException {

		String urlString = "http://localhost:8983/solr";
		SolrServer solr = new HttpSolrServer(urlString);
		SolrInputDocument document = new SolrInputDocument();
		document.addField("id", "552199");
		document.addField("name", "Gouda cheese wheel");
		document.addField("price", "49.99");
		UpdateResponse response = solr.add(document);

		// Remember to commit your changes!

		solr.commit();

	}


	public static void main(String[] args) {
		SolrIndex solrIndex = new SolrIndex();

		try {
			solrIndex.addSampleDocument();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (SolrServerException e) {
			e.printStackTrace();
		}
	}

}
