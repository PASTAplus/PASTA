/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011-2015 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.datamanager;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.datapackagemanager.checksum.DigestUtilsWrapper;
import edu.lternet.pasta.doi.ConfigurationException;
import edu.lternet.pasta.doi.Resource;
import edu.ucsb.nceas.utilities.Options;


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
	
	/*
	 * Class variables
	 */
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static Logger logger = Logger.getLogger(StorageManager.class);

	/*
	 * Instance variables
	 */
	private EmlPackageId emlPackageId;
	private DataPackageRegistry dataPackageRegistry;
	private String packageId;

	/*
	 * Class methods
	 */
	
	/*
	 * Get all data package revisions known to exist in PASTA (including deleted).
	 */
	private static List<EmlPackageId> getAllDataPackageRevisions(DataPackageRegistry dataPackageRegistry) 
			throws ClassNotFoundException, SQLException {
			List<EmlPackageId> allDataPackageRevisions = new ArrayList<EmlPackageId>();
			
			ArrayList<String> packageIdList = dataPackageRegistry.listAllDataPackageRevisions();

			for (String packageId : packageIdList) {
				if (packageId != null && packageId.contains(".")) {
					String[] elements = packageId.split("\\.");
				    if (elements.length != 3) {
				        String msg = 
				        	String.format(
				        		"The packageId '%s' does not conform to the " +
				        	    "standard format <scope>.<identifier>.<revision>", 
				        	    packageId);	
				        throw new IllegalArgumentException(msg);
				    }
				    else {
				        String scope = elements[0];
				        Integer identifier = new Integer(elements[1]);
				        Integer revision = new Integer(elements[2]);
				        EmlPackageId emlPackageId = 
				        	new EmlPackageId(scope, identifier, revision);
				        allDataPackageRevisions.add(emlPackageId);
					}
				}
			}
			
			return allDataPackageRevisions;
		}

	/**
	 * Loads Data Manager options from a configuration file.
	 * 
	 * @param options
	 *          Configuration options object.
	 */
	private static DataPackageRegistry loadOptions(Options options) 
			throws ConfigurationException, ClassNotFoundException, SQLException {
		DataPackageRegistry dataPackageRegistry = null;

		if (options != null) {

			// Load database connection options
			String dbDriver = options.getOption("dbDriver");
			String dbURL = options.getOption("dbURL");
			String dbUser = options.getOption("dbUser");
			String dbPassword = options.getOption("dbPassword");
			dataPackageRegistry = new DataPackageRegistry(dbDriver, dbURL, dbUser, dbPassword);
		} else {
			throw new ConfigurationException("Configuration options failed to load.");
		}

		return dataPackageRegistry;
	}
	
	
	/**
	 * The main program has two modes of operation. If 3 arguments are passed, it
	 * operates on a single data package of the specified scope, identifier, and
	 * revision. Or, if zero arguments are passed, it operates on all the data
	 * package in PASTA.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Options options = ConfigurationListener.getOptions();
		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}
		
		/*
		 * If the program is passed three arguments, then optimize data storage
		 * for a specific scope, identifier, and revision.
		 */
		if (args.length == 3) {
			String scope = args[0];
			Integer identifier = new Integer(args[1]);
			Integer revision = new Integer(args[2]);

			try {
				DataPackageRegistry dataPackageRegistry = loadOptions(options);
				EmlPackageId emlPackageId = new EmlPackageId(scope, identifier,
						revision);
				StorageManager storageManager = new StorageManager(dataPackageRegistry, emlPackageId);
				storageManager.optimizeStorage();
			}
			catch (Exception e) {
				logger.error("main method failed with error: " + e.getMessage());
			}
		}
		/*
		 * If the program is passed 0 arguments, then optimize data storage
		 * for all data packages in PASTA, active and deleted.
		 */
		else if (args.length == 0) {
			try {
				DataPackageRegistry dataPackageRegistry = loadOptions(options);
				optimizeAllPastaData(dataPackageRegistry);
			}
			catch (Exception e) {
				logger.error("main method failed with error: " + e.getMessage());
			}
		}
		else {
			logger.error("Specify the scope, identifier, and revision arguments");
		}
		
	}

	
	/**
	 * Static method to optimize storage for all data entities of all
	 * data packages, whether active or deleted, in PASTA.
	 */
	public static void optimizeAllPastaData(DataPackageRegistry dataPackageRegistry) {		
		try {
			List<EmlPackageId> emlPackageIds = getAllDataPackageRevisions(dataPackageRegistry);

			for (EmlPackageId emlPackageId : emlPackageIds) {
				EmlPackageIdFormat epif = new EmlPackageIdFormat();
				String packageId = epif.format(emlPackageId);
				
				try {
					StorageManager storageManager = new StorageManager(dataPackageRegistry, emlPackageId);
					storageManager.optimizeStorage();
				}
				catch (Exception e) {
					logger.error(
							String.format("Exception optimizing data storage for data package %s: %s",
									      packageId,
									      e.getMessage()
									     )
								);
				}
			}
		}
		catch (Exception e) {
			logger.error(String.format("Error optimizing data storage: %s", e.getMessage()));
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Constructors
	 */
	
	/**
	 * Constructs a StorageManager object which is responsible for optimizing data
	 * storage on a specific revision of a single data package.
	 * 
	 * @param dataPackageRegistry 
	 *     a DataPackageRegistry object for reading from the resource registry
	 * @param emlPackageId        
	 *     the package id of the data package whose data storage is to be optimized
	 */
	public StorageManager(DataPackageRegistry dataPackageRegistry, EmlPackageId emlPackageId) {
		this.emlPackageId = emlPackageId;
		this.dataPackageRegistry = dataPackageRegistry;
		EmlPackageIdFormat epif = new EmlPackageIdFormat();
		this.packageId = epif.format(emlPackageId);
	}
	

	/*
	 * Instance methods
	 */
	
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
							    	String msg = String.format("Deleting entity for storage optimization: %s %s",
							    			                   packageId, entityId);
							    	System.err.println(msg);
									// Delete the data entity for this revision from the disk
									fse.deleteEntity();
									
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

								    try {
								    	Path returnPath = Files.createLink(path, previousPath);
								    	if (returnPath != null) {
								    		// We are done with optimizing this data entity
								    		hasBeenOptimized = true;
								    	}
								    }
								    catch (IOException e) {
								    	msg = String.format("Error creating hard link from %s to %s",
								    			            filePathStr, previousFilePathStr);
								    	System.err.println(msg);
								    }							    								
								}							
							}
						}
					}
				}
			}
		}
	}
	
	
	/*
	 * Return an array list of EMLFileSystemEntity objects for this data package revision.
	 */
	private ArrayList<EMLFileSystemEntity> getFileSystemEntities(EmlPackageId emlPackageId) {
		ArrayList<EMLFileSystemEntity> fileSystemEntities = new ArrayList<EMLFileSystemEntity>();
		
		try {
			boolean publicOnly = false;
			ArrayList<Resource> dataPackageResources = 
					dataPackageRegistry.listDataPackageResources(this.packageId, publicOnly);
			for (Resource resource : dataPackageResources) {
				String resourceType = resource.getResourceType();
				if (resourceType != null && resourceType.equals("data")) {
					String resourceLocation = resource.getResourceLocation();
					String entityId = resource.getEntityId();
					EMLFileSystemEntity efse = new EMLFileSystemEntity(resourceLocation, emlPackageId, entityId);
					String checksum = resource.getSha1Checksum();
					efse.setChecksum(checksum);
					fileSystemEntities.add(efse);
				}
			}
		}
		catch (Exception e) {
			logger.error(String.format("Unable to determine entity ids for data package %s: %s",
					                   packageId, e.getMessage()));
		}
		
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
					                   packageId, e.getMessage()));
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
