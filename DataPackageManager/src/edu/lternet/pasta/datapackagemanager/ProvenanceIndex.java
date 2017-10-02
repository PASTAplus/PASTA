package edu.lternet.pasta.datapackagemanager;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.DataPackage.DataSource;
import edu.lternet.pasta.common.eml.EMLParser;
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
    public ArrayList<DataSource> insertProvenanceRecords(String derivedId, String emlDocument)  
    		throws ProvenanceException, SQLException, ClassNotFoundException {
    	ArrayList<DataSource> dataSources = null;
    	
    	EMLParser emlParser = new EMLParser();
    	DataPackage dataPackage = emlParser.parseDocument(emlDocument);
    	
		if (dataPackage != null) {
	    	ArrayList<String> derivedTitles = dataPackage.getTitles();
	    	String derivedTitle = derivedTitles.size() > 0 ? derivedTitles.get(0) : null;
			/*
			 * Add PASTA identifier values of source data packages to track provenance
			 */
			dataSources = dataPackage.getDataSources();
			for (DataSource dataSource : dataSources) {
				String sourceId = null;
				String sourceTitle = dataSource.getSourceTitle();
				String sourceURL = dataSource.getSourceURL();
				boolean isPastaDataSource = DataPackageManager.isPastaDataSource(sourceURL);
				if (isPastaDataSource) {
					sourceId = DataPackageManager.pastaURLtoPackageId(sourceURL);
				}
			    dpr.insertProvMatrix(derivedId, derivedTitle, sourceId, sourceTitle, sourceURL);
			}
		}
		else {
			String result = String.format("Provenance table insert failed while parsing docid %s",
					                      derivedId);
			throw new ProvenanceException(result);
		}
		
		return dataSources;
    }
    
}
