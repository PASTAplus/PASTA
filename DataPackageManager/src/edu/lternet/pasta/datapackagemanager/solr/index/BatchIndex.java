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
	
	private static final int COMMIT_FREQUENCY = 100;
	private static final String dirPath = "WebRoot/WEB-INF/conf";


	public static void indexAllPastaMetadata() {
		ConfigurationListener configurationListener = new ConfigurationListener();
		configurationListener.initialize(dirPath);
		Options options = ConfigurationListener.getOptions();
		String solrUrl = options.getOption("datapackagemanager.metadatacatalog.solrUrl");
		SolrIndex solrIndex = new SolrIndex(solrUrl);

		try {
			DataPackageManager dpm = new DataPackageManager();
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
