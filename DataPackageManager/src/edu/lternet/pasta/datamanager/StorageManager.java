package edu.lternet.pasta.datamanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.datapackagemanager.checksum.DigestUtilsWrapper;

/**
 * Class to optimize the data storage for a given revision of
 * a given data package. For each data entity in this revision
 * it will try to find a duplicate data entity from a previous
 * revision that it can link to. 
 * 
 * @author dcosta
 *
 */
public class StorageManager {
	
	private static Logger logger = Logger.getLogger(StorageManager.class);

	private EmlPackageId emlPackageId;
	private DataPackageRegistry dataPackageRegistry;

	
	public StorageManager(DataPackageRegistry dataPackageRegistry, EmlPackageId emlPackageId) {
		this.emlPackageId = emlPackageId;
		this.dataPackageRegistry = dataPackageRegistry;
	}
	

	public void optimizeStorage() {
		String scope = emlPackageId.getScope();
		Integer identifier = emlPackageId.getIdentifier();
		Integer revision = emlPackageId.getRevision();
		
		// Get all prior revisions of this revision, ordered from lowest to highest
		ArrayList<Integer> priorRevisions = getPriorRevisions(emlPackageId);
		
		// If there is at least one prior revision
		if (priorRevisions != null && priorRevisions.size() > 0) {

			// Get all data entities for this revision
			ArrayList<EMLFileSystemEntity> fileSystemEntities = getFileSystemEntities(emlPackageId);
			
			// For each prior revision, ordered from lowest to highest
			for (Integer priorRevision : priorRevisions) {
				
				// For each data entity for this revision
				for (EMLFileSystemEntity fse : fileSystemEntities) {
					EmlPackageId priorEmlPackageId = new EmlPackageId(scope, identifier, priorRevision);
					String resourceLocation = fse.getResourceLocation();
					
					// For each data entity for the prior revision
					boolean hasBeenOptimized = false;					
					ArrayList<EMLFileSystemEntity> priorFileSystemEntities = getFileSystemEntities(priorEmlPackageId);
					for (EMLFileSystemEntity pfse : priorFileSystemEntities) {
						// If we haven't yet optimized this date entity by linking it to a prior revision's data entity
						if (!hasBeenOptimized) {

							String checksum = fse.getChecksum();
							String priorChecksum = pfse.getChecksum();
						
							// If the sha1_checksum of the data entity for this revision EQUALS
							// the sha1_checksum of the data entity for the prior revision,
							// and if the data entity file for the prior revision can be verified 
							// to exist on disk with the given checksum value
							if (checksum != null &&
								checksum.equals(priorChecksum) &&
								(verifyChecksum(pfse, priorChecksum))
							   ) {
						 					
								// If the resource_location value of the data entity for this revision EQUALS
				                // the resource_location value of the data entity for the prior revision
				                // (i.e. the data entities reside on the same disk)
								String priorResourceLocation = pfse.getResourceLocation();
								
								if (resourceLocation != null &&
								    resourceLocation.equals(priorResourceLocation)) {
									
									String entityId = fse.getEntityId();
							    	String msg = String.format("Deleting entity for storage optimization: %s",
							    			                   entityId);
							    	System.err.println(msg);
									// Delete the data entity for this revision from the disk
							    	/*
									fse.deleteEntity();
									*/
									
									// Create a hard link from the path of the data entity for this revision
					                // to the path of the data entity for the prior revision.
									FileSystem fileSystem = FileSystems.getDefault();

									File entityFile = fse.getEntityFile();
									String filePathStr = entityFile.getAbsolutePath();
									Path path = fileSystem.getPath(filePathStr);

									File previousEntityFile = pfse.getEntityFile();
									String previousFilePathStr = previousEntityFile.getAbsolutePath();
								    Path previousPath = fileSystem.getPath(previousFilePathStr);
										
							    	String createLinkMsg = String.format("Creating hard link from %s to %s",
			    			                   filePathStr, previousFilePathStr);
							    	System.err.println(createLinkMsg);
							    	/*
								    try {
								    	Path returnPath = Files.createLink(path, previousPath);
								    	if (returnPath != null) {
								    		// We are done with optimizing this data entity
								    		hasBeenOptimized = true;
								    	}
								    }
								    catch (IOException e) {
								    	String msg = String.format("Error creating hard link from %s to %s",
								    			                   filePathStr, previousFilePathStr);
								    	System.err.println(msg);
								    }							    								
								    */
								}							
							}
						}
					}
				}
			}
		}
	}
	
	
	private ArrayList<EMLFileSystemEntity> getFileSystemEntities(EmlPackageId emlPackageId) {
		ArrayList<EMLFileSystemEntity> fileSystemEntities = new ArrayList<EMLFileSystemEntity>();
		
		return fileSystemEntities;
	}
	
	
	/*
	 * Return an array of prior revisions, ordered from lowest to highest
	 */
	private ArrayList<Integer> getPriorRevisions(EmlPackageId emlPackageId) {
		ArrayList<Integer> priorRevisions = null;
		
		try {
			priorRevisions = dataPackageRegistry.listPriorRevisions(emlPackageId);
		}
		catch (Exception e) {
			logger.error(String.format("Unable to determine prior revisions for data package %s: %s",
					                   emlPackageId.toString(), e.getMessage()));
		}
		
		return priorRevisions;
	}
	
	
	/*
	 * Boolean to determine whether the checksum stored in the resource registry for
	 * a data entity matches the calculated checksum value of the file on disk.
	 */
	private boolean verifyChecksum(EMLFileSystemEntity emlFileSystemEntity, String checksum) {
		boolean verified = false;				
		File entityFile = emlFileSystemEntity.getEntityFile();

		try {
			String sha1Checksum = DigestUtilsWrapper.getSHA1Checksum(entityFile);

			if (checksum.equals(sha1Checksum)) {
				verified = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
		}

		return verified;
	}

}
