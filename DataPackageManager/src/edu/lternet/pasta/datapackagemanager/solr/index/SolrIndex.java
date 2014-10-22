package edu.lternet.pasta.datapackagemanager.solr.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;

public class SolrIndex {

	private final String docid = "552199";
	private final String serverURL = "http://localhost:8983/solr";

	public void addSampleDocument() 
			throws IOException, SolrServerException {
		SolrServer solrServer = new HttpSolrServer(serverURL);
		SolrInputDocument solrInputDocument = new SolrInputDocument();
		solrInputDocument.addField("id", this.docid);
		solrInputDocument.addField("name", "Gouda cheese wheel");
		solrInputDocument.addField("price", "49.99");
		UpdateResponse updateResponse = solrServer.add(solrInputDocument);
		int status = updateResponse.getStatus(); // Non-zero indicates failure
		System.out.println(String.format("Add of docid %s; update status %d", this.docid, status));

		// Remember to commit your changes!
		solrServer.commit();
	}
	

	public void deleteSampleDocument() 
			throws IOException, SolrServerException {
		SolrServer solrServer = new HttpSolrServer(serverURL);
		List<String> ids = new ArrayList<String>();
		ids.add(this.docid);
		UpdateResponse updateResponse = solrServer.deleteById(ids);
		int status = updateResponse.getStatus(); // Non-zero indicates failure
		System.out.println(String.format("Delete of docid %s; update status %d", this.docid, status));

		// Remember to commit your changes!
		solrServer.commit();
	}
	
	
    public String indexEmlDocument(EmlPackageId epid, String emlDocument)  
    		throws IOException, SolrServerException {
    	String result = null;
    	
    	String id = String.format("%s.%d", epid.getScope(), epid.getIdentifier());
    	
    	EMLParser emlParser = new EMLParser();
    	DataPackage dataPackage = emlParser.parseDocument(emlDocument);
    	
		if (dataPackage != null) {
			List<String> titles = dataPackage.getTitles();

			SolrServer solrServer = new HttpSolrServer(serverURL);
			SolrInputDocument solrInputDocument = new SolrInputDocument();

			solrInputDocument.addField("id", id);

			for (String title : titles) {
				solrInputDocument.addField("title", title);
			}

			UpdateResponse updateResponse = solrServer.add(solrInputDocument);
			int status = updateResponse.getStatus(); // Non-zero indicates failure
			System.out.println(String.format(
					"Add of id %s; update status %d", id, status));

			// Remember to commit your changes!
			solrServer.commit();
		}
		else {
			result = String.format("Solr indexing failed with error while parsing docid %s", id);
		}
    	
    	return result;
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
