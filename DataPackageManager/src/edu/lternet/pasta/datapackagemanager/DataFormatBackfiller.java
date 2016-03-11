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

import java.util.ArrayList;


import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.doi.Resource;

/**
 * Class used to transfer data format values for data packages that preceded the
 * migration of the data_format column from the data_cache_registry table to the
 * resource_registry table.
 * 
 * @author dcosta
 * 
 */
public class DataFormatBackfiller {

	private static final String dirPath = "WebRoot/WEB-INF/conf";


	/**
	 * Main program. No command arguments. Should be run in the
	 * DataPackageManager top-level directory.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		int backfillCount = 0;

		try {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
			ArrayList<Resource> resources = dpr.listDataFormatlessResources();

			if (resources != null) {
				System.out.println(String.format("Found %d resources without data format", resources.size()));
			}

			for (Resource resource : resources) {
				String resourceId = resource.getResourceId();
				String resourceType = resource.getResourceType();

				if ((resourceType != null) && (resourceType.equals("data"))) {
					String entityId = resource.getEntityId();
					String dataFormat = dpr.getDataCacheDataFormat(entityId);
					if (dataFormat != null) {
						dpr.updateDataFormat(resourceId, dataFormat);
						backfillCount++;
					}
				}
			}
		}
		catch (Exception e) {
			System.err.println("Exception constructing DataPackageManager object: " + e.getMessage());
		}

		System.err.println(String.format(
				"Finished backfill processing. Data formats stored for %d resource(s).", backfillCount));
	}

}
