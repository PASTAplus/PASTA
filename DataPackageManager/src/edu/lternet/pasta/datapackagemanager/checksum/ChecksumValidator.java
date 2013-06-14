/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager.checksum;

import java.io.File;
import java.util.ArrayList;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataManagerClient;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageMetadata;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.datapackagemanager.DataPackageReport;
import edu.lternet.pasta.doi.Resource;

/**
 * Class used to validate checksum values for PASTA resources by comparing
 * the value stored in the resource registry with the dynamically calculated
 * value.
 * 
 * @author dcosta
 * 
 */
public class ChecksumValidator {

	private static final String dirPath = "WebRoot/WEB-INF/conf";


	/**
	 * Main program. No command arguments. Should be run in the
	 * DataPackageManager top-level directory. Exits with a 0
	 * exit status if no errors discovered, else exits with a
	 * non-zero error status if at least one error was discovered.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int errorCount = 0;
		int resourceCount = 0;
		try {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			DataPackageManager dpm = new DataPackageManager();
			DataPackageRegistry dpr = DataPackageManager
					.makeDataPackageRegistry();
			ArrayList<Resource> resources = dpr.listChecksumableResources();

			for (Resource resource : resources) {
				resourceCount++;
				String resourceId = resource.getResourceId();
				String resourceType = resource.getResourceType();
				String scope = resource.getScope();
				Integer identifier = resource.getIdentifier();
				Integer revision = resource.getRevision();
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
						identifier.toString(), revision.toString());
				String storedChecksum = resource.getSha1Checksum();
				String calculatedChecksum = null;

				if (resourceType != null) {
					try {
						if (resourceType.equals("data")) {
							String entityId = resource.getEntityId();
							DataManagerClient dataManagerClient = new DataManagerClient();
							String resourceLocation = dpr.getResourceLocation(resourceId);
							File file = dataManagerClient.getDataEntityFile(
								resourceLocation, scope, identifier,
								revision.toString(), entityId);
							calculatedChecksum = dpm.calculateChecksum(resourceId, file);
						}
						else if (resourceType.equals("metadata")) {
							DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(emlPackageId);
							if (dataPackageMetadata != null) {
								boolean evaluateMode = false;
								File file = dataPackageMetadata.getMetadata(evaluateMode);
								calculatedChecksum = dpm.calculateChecksum(resourceId, file);
							}
						}
						else if (resourceType.equals("report")) {
							DataPackageReport dataPackageReport = new DataPackageReport(emlPackageId);
							boolean evaluate = false;
							String transaction = null;
							if (dataPackageReport != null) {
								File file = dataPackageReport.getReport(evaluate, transaction);
								calculatedChecksum = dpm.calculateChecksum(resourceId, file);
							}
						}
						else {
							System.err.println(String.format("Unknown resourceType '%s' for resource %s", resourceType, resourceId));
						}
					
						if (storedChecksum == null) {
							System.err.println(String.format("Stored checksum for resource %s is null", resourceId));
							errorCount++;
						}
						else if (calculatedChecksum == null) {
							System.err.println(String.format("Failed to calculate checksum for resource %s", resourceId));
							errorCount++;
						}
						else if (!storedChecksum.equals(calculatedChecksum)) {
							System.err.println(String.format("Checksums do not match for resource %s\n  Stored checksum:     %s\n  Calculated checksum: %s", 
																resourceId, storedChecksum, calculatedChecksum));
							errorCount++;
						}
					}
					catch (Exception e) {
						System.err.println(
								String.format("Exception while calculating checksum for resource %s: %s", 
										      resourceId, e.getMessage()));
						errorCount++;
					}
				}
				else {
					System.err.println(String.format("Resource type is null for resource %s", resourceId));
					errorCount++;
				}
			}
		}
		catch (Exception e) {
			System.err.println("Exception constructing DataPackageManager object: "+ e.getMessage());
		}

		System.err.println(String.format("Finished processing %d resources. %d resource(s) had errors.", 
				                         resourceCount, errorCount));
		int errorStatus = (errorCount == 0) ? 0 : 1;
		System.exit(errorStatus);
	}

}
