package edu.lternet.pasta.metadatamanager;

import java.io.IOException;

import org.apache.solr.client.solrj.SolrServerException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.datapackagemanager.solr.index.SolrIndex;


public class SolrMetadataCatalog implements MetadataCatalog {

    public String createEmlDocument(EmlPackageId epid, String emlDocument) {
    	return indexEmlDocument(epid, emlDocument);
    }

    
    public String deleteEmlDocument(EmlPackageId epid) {
    	return null;
    }
    
    
    private String indexEmlDocument(EmlPackageId epid, String emlDocument) {
    	String result = null;

    	SolrIndex solrIndex = new SolrIndex();
    	
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

    
    public String readEmlDocument(EmlPackageId epid) {
    	return null;
    }

    
    public String updateEmlDocument(EmlPackageId epid, String emlDocument) {
    	return indexEmlDocument(epid, emlDocument);
    }

}
