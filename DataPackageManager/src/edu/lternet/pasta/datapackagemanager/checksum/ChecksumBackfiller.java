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
 * Class used to backfill checksum values for data packages that preceded the
 * checksum enhancement.
 * 
 * @author dcosta
 * 
 */
public class ChecksumBackfiller {

	private static final String dirPath = "WebRoot/WEB-INF/conf";


	/**
	 * Main program. No command arguments. Should be run in the
	 * DataPackageManager top-level directory.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int checksumCount = 0;

		try {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			DataPackageManager dpm = new DataPackageManager();
			DataPackageRegistry dpr = DataPackageManager
					.makeDataPackageRegistry();

			ArrayList<Resource> resources = dpr.listSha1ChecksumlessResources();
			for (Resource resource : resources) {
				String resourceId = resource.getResourceId();
				String resourceType = resource.getResourceType();
				String scope = resource.getScope();
				Integer identifier = resource.getIdentifier();
				Integer revision = resource.getRevision();
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
						identifier.toString(), revision.toString());

				if (resourceType != null) {
					if (resourceType.equals("data")) {
						// Store the checksum of the data entity resource
						String entityId = resource.getEntityId();
						DataManagerClient dataManagerClient = new DataManagerClient();
						String resourceLocation = dpr
								.getResourceLocation(resourceId);
						File file = dataManagerClient.getDataEntityFile(
								resourceLocation, scope, identifier,
								revision.toString(), entityId);
						dpm.storeSHA1Checksum(resourceId, file);
						checksumCount++;
					}
					else {
						if (resourceType.equals("metadata")) {
							DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
									emlPackageId);
							if (dataPackageMetadata != null) {
								boolean evaluateMode = false;
								File file = dataPackageMetadata
										.getMetadata(evaluateMode);
								dpm.storeSHA1Checksum(resourceId, file);
								checksumCount++;
							}
						}
						else {
							if (resourceType.equals("report")) {
								DataPackageReport dataPackageReport = new DataPackageReport(
										emlPackageId);
								boolean evaluate = false;
								String transaction = null;
								if (dataPackageReport != null) {
									File file = dataPackageReport.getReport(
											evaluate, transaction);
									dpm.storeSHA1Checksum(resourceId, file);
									checksumCount++;
								}
							}
							else {
								System.err.println("Unknown resource type: "
										+ resourceType);
							}
						}
					}
				}
			}
			System.err.println(String.format(
					"Finished SHA-1 backfill processing. SHA-1 checksums generated and stored for %d resource(s).",
					checksumCount));
			
			checksumCount = 0;			
			resources = dpr.listMd5ChecksumlessResources();
			for (Resource resource : resources) {
				String resourceId = resource.getResourceId();
				String resourceType = resource.getResourceType();
				String scope = resource.getScope();
				Integer identifier = resource.getIdentifier();
				Integer revision = resource.getRevision();
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
						identifier.toString(), revision.toString());

				if (resourceType != null) {
					if (resourceType.equals("data")) {
						// Store the checksum of the data entity resource
						String entityId = resource.getEntityId();
						DataManagerClient dataManagerClient = new DataManagerClient();
						String resourceLocation = dpr
								.getResourceLocation(resourceId);
						File file = dataManagerClient.getDataEntityFile(
								resourceLocation, scope, identifier,
								revision.toString(), entityId);
						dpm.storeMD5Checksum(resourceId, file);
						checksumCount++;
					}
					else {
						if (resourceType.equals("metadata")) {
							DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
									emlPackageId);
							if (dataPackageMetadata != null) {
								boolean evaluateMode = false;
								File file = dataPackageMetadata
										.getMetadata(evaluateMode);
								dpm.storeMD5Checksum(resourceId, file);
								checksumCount++;
							}
						}
						else {
							if (resourceType.equals("report")) {
								DataPackageReport dataPackageReport = new DataPackageReport(
										emlPackageId);
								boolean evaluate = false;
								String transaction = null;
								if (dataPackageReport != null) {
									File file = dataPackageReport.getReport(
											evaluate, transaction);
									dpm.storeMD5Checksum(resourceId, file);
									checksumCount++;
								}
							}
							else {
								System.err.println("Unknown resource type: "
										+ resourceType);
							}
						}
					}
				}
			}
			System.err.println(String.format(
					"Finished MD5 backfill processing. MD5 checksums generated and stored for %d resource(s).",
					checksumCount));
		}
		catch (Exception e) {
			System.err
					.println("Exception constructing DataPackageManager object: "
							+ e.getMessage());
		}
	}

}
