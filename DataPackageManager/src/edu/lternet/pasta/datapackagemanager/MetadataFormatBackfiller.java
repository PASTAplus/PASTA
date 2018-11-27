/**
*
* $Date$
* $Author: dcosta $
* $Revision$
*
* Copyright 2011-2014 the University of New Mexico.
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
import java.util.ArrayList;

import edu.lternet.pasta.dml.parser.DataPackage;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageMetadata;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.doi.Resource;

/**
* Class used to backfill checksum values for data packages that preceded the
* checksum enhancement.
* 
* @author dcosta
* 
*/
public class MetadataFormatBackfiller {

	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static final String EML_2_1_0 = "eml://ecoinformatics.org/eml-2.1.0";
	private static final String EML_2_1_1 = "eml://ecoinformatics.org/eml-2.1.1";
	private static final String EML_2_2_0 = "eml://ecoinformatics.org/eml-2.2.0";


	/**
	 * Main program. No command arguments. Should be run in the
	 * DataPackageManager top-level directory.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int backfillCount = 0;
		boolean isEvaluate = false;

		try {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			DataPackageManager dpm = new DataPackageManager();
			DataPackageRegistry dpr = DataPackageManager
					.makeDataPackageRegistry();
			ArrayList<Resource> resources = dpr.listFormatlessResources();
			if (resources != null) {
				System.out.println(
						String.format("Found %d resources without format type", 
								      resources.size()));
			}

			for (Resource resource : resources) {
				String resourceId = resource.getResourceId();
				String resourceType = resource.getResourceType();
				String scope = resource.getScope();
				Integer identifier = resource.getIdentifier();
				Integer revision = resource.getRevision();
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(scope,
						identifier.toString(), revision.toString());

				if ((resourceType != null) && 
					(resourceType.equals("metadata"))
				) {
					DataPackageMetadata dataPackageMetadata = new DataPackageMetadata(
									emlPackageId);
					if (dataPackageMetadata != null) {
						DataPackage dataPackage = null;
						File emlFile = null;
						String formatType = null;
						
						try {
							emlFile = dataPackageMetadata.getMetadata(isEvaluate);
							dataPackage = dpm.parseEml(emlFile, isEvaluate);
							formatType = dataPackage.getEmlNamespace();
						}
						catch (Exception e) {
							System.err.println(
									String.format("Exception parsing %s: %s",
									              resourceId, e.getMessage()));
							/*
							 * We were unable to parse the EML, perhaps because of the following error:
							 *   Invalid byte 1 of 1-byte UTF-8 sequence.
							 * As an alternative, let's do a brute force grep for the format type.
							 */
							if (emlFile != null) {
								String emlString = FileUtility.fileToString(emlFile);
								if (emlString != null) {
									if (emlString.contains(EML_2_1_0)) {
										formatType = EML_2_1_0;
									}
									else if (emlString.contains(EML_2_1_1)) {
										formatType = EML_2_1_1;
									}
									else if (emlString.contains(EML_2_2_0)) {
										formatType = EML_2_2_0;
									}
								}
							}
						}
						
						if (formatType != null) {
							dpr.updateFormatType(resourceId, formatType);
							backfillCount++;
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.err
					.println("Exception constructing DataPackageManager object: "
							+ e.getMessage());
		}

		System.err.println(String.format(
				"Finished backfill processing. Format types generated and stored for %d resource(s).",
				backfillCount));
	}

}
