package edu.lternet.pasta.datapackagemanager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.ISO8601Utility;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;


public class ProvenanceIndex {

	  /*
	   * Class fields
	   */

	private static final Logger logger = Logger.getLogger(ProvenanceIndex.class);
	
	
	/*
	 * Instance fields
	 */
  
	private DataPackageRegistry dpr;
	
	
	/*
	 * Constructors
	 */
	
	public ProvenanceIndex(DataPackageRegistry dpr) {
		this.dpr = dpr;
	}
	
	
	
	/*
	 * Instance methods
	 */
	
	/**
	 * Deletes provenance records for the specified package identifier,
	 * including all revisions of this identifier.
	 * 
	 * @param  epid    The EML package id of the document whose provenance records 
	 *                 are to be deleted.
	 * @throws IOException
	 * @throws SolrServerException
	 */
	public void deleteProvenanceRecords(EmlPackageId epid) 
			throws IOException, SolrServerException {
		// Note that the revision value is not included, since we wish to delete all revisions
    	String id = String.format("%s.%d", epid.getScope(), epid.getIdentifier());

    	//dpr.deleteProvenance(id);
		logger.info(String.format("Deleted provenance records for document id %s", id));
	}
	
	
	/**
	 * Delete provenance records for the specified package identifier.
	 * This is used to rollback a failed data package upload for a specific
	 * revision of a data package.
	 * 
	 * @param  packageId  the package identifier including revision, e.g. 'knb-lter-nin.1.1'
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 */
	public void rollbackProvenanceRecords(String packageId) 
			throws IOException, ClassNotFoundException, SQLException {
		dpr.deleteProvenanceRecords(packageId);
	}
	
	
	/**
	 * Inserts provenance records for an EML document.
	 * 
	 * @param derivedId     the EML package id object of the EML document
	 * @param emlDocument   the EML document string
	 * @return sourceIds    a list of package id strings
	 */
    public ArrayList<String> insertProvenanceRecords(String derivedPackageId, String emlDocument)  
    		throws ProvenanceException, SQLException, ClassNotFoundException {
    	ArrayList<String> sourceIds = new ArrayList<String>();
    	
    	EMLParser emlParser = new EMLParser();
    	DataPackage dataPackage = emlParser.parseDocument(emlDocument);
    	
		if (dataPackage != null) {
			/*
			 * Add PASTA identifier values of source data packages to track provenance
			 */
			ArrayList<String> dataSources = dataPackage.getDataSources();
			for (String dataSourceURL : dataSources) {
				boolean isPastaDataSource = DataPackageManager.isPastaDataSource(dataSourceURL);
				if (isPastaDataSource) {
					String sourcePackageId = pastaURLtoPackageId(dataSourceURL);
					dpr.insertProvenance(derivedPackageId, sourcePackageId, isPastaDataSource);
					sourceIds.add(sourcePackageId);
					logger.info(
							String.format("Added provenance record: derived '%s' depends on source '%s'",
									      derivedPackageId, sourcePackageId));
				}
				else {
					dpr.insertProvenance(derivedPackageId, dataSourceURL, isPastaDataSource);
					sourceIds.add(dataSourceURL);
					logger.info(
						String.format("Added provenance record: derived data package '%s' depends on external data source '%s'",
									  derivedPackageId, dataSourceURL));
				}
			}

		}
		else {
			String result = String.format("Provenance table insert failed while parsing docid %s",
					                      derivedPackageId);
			throw new ProvenanceException(result);
		}
		
		return sourceIds;
    }
    
    
	/**
	 * Converts a pastaURL string to a packageId string, or null if the pastaURL
	 * does not match the recognized PASTA url pattern.
	 * 
	 * @param pastaURL  the pastaURL string, 
	 *                  e.g. https://pasta-d.lternet.edu/package/eml/knb-lter-hbr/58/5
	 * @return the packageId string, 
	 *                  e.g. knb-lter-hbr.58.5
	 */
	private String pastaURLtoPackageId(String pastaURL) {
		String packageId = null;

		if (pastaURL != null) {
			final String patternString = "^.*/eml/(\\S+)/(\\d+)/(\\d+)$";
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(pastaURL);
			if (matcher.matches()) {
				String scope = matcher.group(1);
				String identifier = matcher.group(2);
				String revision = matcher.group(3);
				packageId = String.format("%s.%s.%s", scope, identifier,
						revision);
			}
		}

		return packageId;
	}	  

}
