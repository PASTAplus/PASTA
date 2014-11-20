package edu.lternet.pasta.metadatamanager;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.apache.solr.client.solrj.SolrServerException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.QueryString;
import edu.lternet.pasta.datapackagemanager.solr.index.SolrIndex;
import edu.lternet.pasta.datapackagemanager.solr.search.SimpleSolrSearch;


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
    		solrIndex.deleteEmlDocument(epid);
    		solrIndex.commit(); // Always commit after individual document deletes
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
    		solrIndex.commit(); // Always commit after individual document uploads
    	}
    	catch (IOException | SolrServerException e) {
    		e.printStackTrace();
    		result = e.getMessage();
    	}
    	
    	return result;
    }

    
    public String query(UriInfo uriInfo) {
    	String result = null;
    	
    	SimpleSolrSearch simpleSolrSearch = new SimpleSolrSearch(solrUrl);
    	
    	try {
            QueryString queryString = new QueryString(uriInfo);
            Map<String, List<String>> queryParams = queryString.getParams();
            
			if (queryParams != null) {

				for (String key : queryParams.keySet()) {
					if (key.equalsIgnoreCase("q")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		simpleSolrSearch.setQueryText(value);
					}
					else if (key.equals("fq")) {
						List<String> values = queryParams.get(key);
						for (String fq : values) {
							simpleSolrSearch.addFilterQuery(fq);
						}
					}
					else if (key.equals("start")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		simpleSolrSearch.setStart(value);
					}
					else if (key.equals("rows")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		simpleSolrSearch.setRows(value);
					}
				}
			}            
            
    		result = simpleSolrSearch.search();
    	}
    	catch (SolrServerException e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }

    
    public String updateEmlDocument(EmlPackageId epid, String emlDocument) {
    	return indexEmlDocument(epid, emlDocument);
    }

}
