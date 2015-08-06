package edu.lternet.pasta.datapackagemanager.solr.index;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServerException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageMetadata;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.ucsb.nceas.utilities.Options;


/**
 * This class performs batch indexing of EML documents. Typically, this
 * is used when we want to intialize a Solr index with all of the
 * EML metadata in the PASTA repository.
 * 
 * @author dcosta
 *
 */
public class BatchIndex {
	
	private static String dbDriver = null;
	private static String dbURL = null;
	private static String dbUser = null;
	private static String dbPassword = null;

	private static final int COMMIT_FREQUENCY = 100;
	private static final String dirPath = "WebRoot/WEB-INF/conf";


	public static void indexAllPastaMetadata() {
		ConfigurationListener configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
		Options options = ConfigurationListener.getOptions();
		dbDriver = options.getOption("dbDriver");
		dbURL = options.getOption("dbURL");
		dbUser = options.getOption("dbUser");
		dbPassword = options.getOption("dbPassword");
		String solrUrl = options.getOption("datapackagemanager.metadatacatalog.solrUrl");
		SolrIndex solrIndex = new SolrIndex(solrUrl);

		try {
			DataPackageManager dpm = new DataPackageManager();
			DataPackageRegistry dpr = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
			List<EmlPackageId> emlPackageIds = getAllLatestEML(dpm);

			int i = 1;
			for (EmlPackageId emlPackageId : emlPackageIds) {
				String emlDocument = null;
				DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
					    emlPackageId);
				if (dataPackageMetadata != null) {
					boolean evaluateMode = false;
					File levelOneEMLFile = dataPackageMetadata.getMetadata(evaluateMode);
					emlDocument = FileUtils.readFileToString(levelOneEMLFile);
				}

				try {
					solrIndex.indexEmlDocument(emlPackageId, emlDocument);
					
					String resourceId = getResourceId(dpr, emlPackageId);
					String doi = dpr.getDoi(resourceId);
					if (doi != null && !doi.equals("")) {
						solrIndex.indexDoi(emlPackageId, doi);
					}
					
					boolean commit = (i % COMMIT_FREQUENCY == 0);
					
					if (commit) {
						System.err.println(String.format("Executing Solr commit after document %d: %s",
												         i, emlPackageId));
						solrIndex.commit();
					}
					
					i++;
				}
				catch (IOException | SolrServerException e) {
					e.printStackTrace();
					return;
				}
			}
			
			System.err.println(String.format("Executing Solr commit after final document in batch: %d", i));
			solrIndex.commit();
		}
		catch (Exception e) {
			System.err
					.println("Exception constructing DataPackageManager object: "
							+ e.getMessage());
		}
	}
	
	
	private static String getResourceId(DataPackageRegistry dpr, EmlPackageId epid) {
		String resourceId = null;
		
		if (dpr != null && epid != null) {
			String scope = epid.getScope();
			Integer identifier = epid.getIdentifier();
			Integer revision = epid.getRevision();
			
			try {
				ArrayList<String> resourceIds = dpr.getDataPackageResources(scope, identifier, revision);
				for (String rid : resourceIds) {
					if (rid.contains("/package/eml/")) {
						// This is the dataPackage resource, which holds the DOI value for the data package
						resourceId = rid;
					}
				}
			}
			catch (Exception e) {
				;
			}		
		}
		
		return resourceId;
	}
	
	
	private static List<EmlPackageId> getAllLatestEML(DataPackageManager dpm) 
		throws ClassNotFoundException, SQLException {
		List<EmlPackageId> allLatestEML = new ArrayList<EmlPackageId>();
		
		String docIdsString = dpm.listActiveDataPackages();
		String[] docIds = docIdsString.split("\n");
		if (docIds != null && docIds.length > 0) {
			for (String docid : docIds) {
				if (docid != null && docid.contains(".")) {
			        String[] elements = docid.split("\\.");
			        if (elements.length != 2) {
			        	String msg = 
			        		String.format(
			        				"The docid '%s' does not conform to the " +
			        	            "standard format <scope>.<identifier>", 
			        	            docid);	
			            throw new IllegalArgumentException(msg);
			        }
			        else {
			        	String scope = elements[0];
			        	Integer identifier = new Integer(elements[1]);
			        	Integer revision = dpm.getNewestRevision(scope, identifier);
			        	EmlPackageId emlPackageId = 
			        			new EmlPackageId(scope, identifier, revision);
			        	allLatestEML.add(emlPackageId);
			        }
				}
			}
		}
		
		return allLatestEML;
	}
	
	
	public static void main(String[] args) {
		indexAllPastaMetadata();
	}

}
