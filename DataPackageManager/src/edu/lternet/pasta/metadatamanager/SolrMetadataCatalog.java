package edu.lternet.pasta.metadatamanager;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.datapackagemanager.solr.index.SolrIndex;


public class SolrMetadataCatalog implements MetadataCatalog {
	
	/*
	 * Instance variables
	 */
    private String solrUrl = null;

   
    /*
     * Instance methods
     */
    
    public SolrMetadataCatalog(String solrUrl) {
		this.solrUrl = solrUrl;
	}

    
    public String createEmlDocument(EmlPackageId epid, String emlDocument) {
    	return indexEmlDocument(epid, emlDocument);
    }

    
    public String deleteEmlDocument(EmlPackageId epid) {
    	String result = null;
    	
    	SolrIndex solrIndex = new SolrIndex(solrUrl);
    	
    	try {
    		result = solrIndex.deleteEmlDocument(epid);
    	}
    	catch (IOException | SolrServerException e) {
    		e.printStackTrace();
    		result = e.getMessage();
    	}
    	
    	return result;
    }
    
    
    private String indexEmlDocument(EmlPackageId epid, String emlDocument) {
    	String result = null;

    	SolrIndex solrIndex = new SolrIndex(solrUrl);
    	
    	try {
    		result = solrIndex.indexEmlDocument(epid, emlDocument);
    	}
    	catch (IOException | SolrServerException e) {
    		e.printStackTrace();
    		result = e.getMessage();
    	}
    	
    	return result;
    }

    
    public String query(String xmlQuery) {
    	return null;
    }

    
    public String updateEmlDocument(EmlPackageId epid, String emlDocument) {
    	return indexEmlDocument(epid, emlDocument);
    }

}
