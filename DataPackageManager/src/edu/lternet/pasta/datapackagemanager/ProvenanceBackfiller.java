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

package edu.lternet.pasta.datapackagemanager;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageMetadata;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;

/**
 * Class used to backfill the provenance table
 * 
 * @author dcosta
 * 
 */
public class ProvenanceBackfiller {

	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static Logger logger = Logger.getLogger(ProvenanceBackfiller.class);

	
	/*
	 * Get all data package revisions known to exist in PASTA (excluding deleted).
	 */
	private static List<EmlPackageId> getAllDataPackageRevisions(DataPackageRegistry dataPackageRegistry) 
			throws ClassNotFoundException, SQLException {
		List<EmlPackageId> allDataPackageRevisions = new ArrayList<EmlPackageId>();
		boolean includeInactive = false;
			
		ArrayList<String> packageIdList = dataPackageRegistry.listAllDataPackageRevisions(includeInactive);

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
				    EmlPackageId emlPackageId = new EmlPackageId(scope, identifier, revision);
				    allDataPackageRevisions.add(emlPackageId);
				}
			}
		}
			
		return allDataPackageRevisions;
	}

	
	/**
	 * Main program to backfill the provenance table. 
	 * It takes no command arguments. 
	 * Should be run in the DataPackageManager top-level directory.
	 * 
	 * @param args   Any arguments passed to the program are ignored.
	 */
	public static void main(String[] args) {
		int backfillCount = 0;
		int derivedDataPackageCount = 0;
		int recordsInserted = 0;
		final boolean evaluateMode = false;

		try {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
			ProvenanceIndex provenanceIndex = new ProvenanceIndex(dpr);
			EmlPackageIdFormat epif = new EmlPackageIdFormat();
			
			List<EmlPackageId> emlPackageIds = getAllDataPackageRevisions(dpr);

			for (EmlPackageId emlPackageId : emlPackageIds) {
				DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(emlPackageId);
				if (dataPackageMetadata != null) {
					File levelOneEMLFile = dataPackageMetadata.getMetadata(evaluateMode);
					String emlDocument = FileUtils.readFileToString(levelOneEMLFile);
					String packageId = epif.format(emlPackageId);
					System.err.println("  " + packageId);

					try {
						ArrayList<DataPackage.DataSource> sourceIds = 
								provenanceIndex.insertProvenanceRecords(packageId, emlDocument);
						if ((sourceIds != null) && (sourceIds.size() > 0)) {
							derivedDataPackageCount++;
							recordsInserted += sourceIds.size();
						}
					}
					catch (ProvenanceException e) {
						logger.error(e.getMessage());
					}

					backfillCount++;
				}
			}
		}
		catch (Exception e) {
			logger.error(String.format("An error occurred during provenance backfill processing: %s", e.getMessage()));
			e.printStackTrace();
		}
		
		System.err.println(String.format(
				"Finished provenance backfill processing. Provenance generated and stored for %d resource(s).",
				backfillCount));
		System.err.println(String.format(
				"Total number of derived data packages detected: %d.",
				derivedDataPackageCount));
		System.err.println(String.format(
				"Total records inserted into provenance table: %d.",
				recordsInserted));
	}

}
