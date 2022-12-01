package edu.lternet.pasta.metadatamanager;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.FileUtils;
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
     * Class methods
     */
    
    /**
     * Main program for testing indexing of EML documents
     */
    public static void main(String[] args) {
    	String emlFilePath = args[0];
    	File emlFile = new File(emlFilePath);
    	String scope = "knb-lter-nwk";
    	Integer identifier = new Integer(1424);
    	Integer revision = new Integer(35);
    	String solrUrl = "http://localhost:8983/solr/collection1";
    	try {
    		String emlXML = FileUtils.readFileToString(emlFile);
    		EmlPackageId epi = new EmlPackageId(scope, identifier, revision);
    		SolrMetadataCatalog smc = new SolrMetadataCatalog(solrUrl);
    		smc.indexEmlDocument(epi, emlXML);
    	}
    	catch (Exception e) {
    		e.printStackTrace();
    	}
    }

   
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

    
    public String indexDoi(EmlPackageId epid, String doi) {
    	String result = null;
    	SolrIndex solrIndex = new SolrIndex(solrUrl);
    	
    	try {
    		result = solrIndex.indexDoi(epid, doi);
    		solrIndex.commit(); // Always commit after individual document uploads
    	}
    	catch (IOException | SolrServerException e) {
    		e.printStackTrace();
    		result = e.getMessage();
    	}
    	
    	return result;
    }

    
    public String query(UriInfo uriInfo) throws IOException {
    	String result = null;
    	
    	SimpleSolrSearch simpleSolrSearch = new SimpleSolrSearch(solrUrl);
    	
    	try {
            QueryString queryStr = new QueryString(uriInfo);
            Map<String, List<String>> queryParams = queryStr.getParams();
            
			if (queryParams != null) {

				for (String key : queryParams.keySet()) {
					if (key.equalsIgnoreCase("q")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		simpleSolrSearch.setQueryText(value);
					}
					else if (key.equals("fl")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		simpleSolrSearch.setFields(value);
					}
					else if (key.equals("fq")) {
						List<String> values = queryParams.get(key);
						for (String fq : values) {
							/*
							 * Turn filter query caching off for spatial, scope, and date filters.
							 * Set the cost of spatial filtering to 100 to invoke it as a post filter.
							 * (See "Solr in Action", section 7.3.2)
							 */
							if (fq.startsWith("coordinates:")) {
								fq = "{!cache=false cost=100}" + fq;
							}
							else if (fq.startsWith("scope:")) {
								fq = "{!cache=false cost=1}" + fq;
							}
							else if (fq.contains("date:")) {
								fq = "{!cache=false cost=2}" + fq;
							}
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
					else if (key.equals("sort")) {
						List<String> values = queryParams.get(key);
						for (String value : values) {
							String[] tokens = value.split(",");
						
							if (tokens != null && tokens.length == 2) {
								String field = tokens[0];
								String order = tokens[1];
								simpleSolrSearch.addSort(field, order);
							}
					    }
					}
					else if (key.equalsIgnoreCase("debug")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		if (value != null && value.equals("true")) {
			    			simpleSolrSearch.setDebug(true);
			    		}
					}
					else if (key.equalsIgnoreCase("defType")) {
						List<String> values = queryParams.get(key);
						String value = values.get(0);
			    		simpleSolrSearch.setDefType(value);
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
