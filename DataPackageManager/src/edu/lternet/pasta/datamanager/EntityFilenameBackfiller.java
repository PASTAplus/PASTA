/**
*
* $Date$
* $Author: dcosta $
* $Revision$
*
* Copyright 2011-2018 the University of New Mexico.
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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.doi.Resource;


/**
* Class used to back-fill data entity filename values for data packages
* 
* @author Duane Costa
* 
*/
public class EntityFilenameBackfiller {

    /*
     * Class variables
     */
    
    private static Logger logger = Logger.getLogger(EntityFilenameBackfiller.class);
    
    private static final String dirPath = "WebRoot/WEB-INF/conf";


    /**
     * Main program. No command arguments. Should be run in the
     * DataPackageManager top-level directory.
     * 
     * @param args
     */
    public static void main(String[] args) {
        int count = 0;
        
        try {
            ConfigurationListener configurationListener = new ConfigurationListener();
            configurationListener.initialize(dirPath);
            DataPackageManager dpm = new DataPackageManager();
            DataPackageRegistry dpr = DataPackageManager.makeDataPackageRegistry();
            ArrayList<Resource> resources = dpr.listFilenamelessResources();

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
                        // Determine the filename of the data entity resource and store it in the resource registry
                        String entityName = dpr.getDataEntityName(resourceId);
                        String msg = String.format("Processing packageId: %s; entityName: %s",
                                                   emlPackageId.toString(), entityName);
                        logger.info(msg);
                        String filename = dpm.getEntityObjectName(emlPackageId, entityName);
                        if (filename != null && !filename.isEmpty()) {
                            dpm.storeResourceFilename(resourceId, filename);
                            count++;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println("Exception constructing DataPackageManager object: " + e.getMessage());
        }

        System.err.println(
                String.format("Finished backfill processing. Entity filenames generated and stored for %d resource(s).",
                              count));
    }

}
