/**
*
* $Date$
* $Author$ dcosta
* $Revision$
*
* Copyright 2010 the University of New Mexico.
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
import java.io.IOException;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;


/**
* Class that retrieves data package Level-1 metadata files.
* 
* @author dcosta
* @created
*/
public class DataPackageMetadata {

 /*
  * Class fields
  */
 

 /*
  * Instance fields
  */
 
 private EmlPackageId emlPackageId = null;
 private final String FILE_NAME_EML = "Level-1-EML.xml";
 private final String FILE_NAME_DC = "Level-1-DC.xml";   // Dublin Core metadata file
 private String fileName;
 
 
 /*
  * Constructors
  */
 
 /**
  * Constructs a DataPackageMetadata object given an EmlPackageId.
  * 
  * @param  emlPackageId, an object that represents the packageId
  */
 public DataPackageMetadata(EmlPackageId emlPackageId){
   this.emlPackageId = emlPackageId;
   this.fileName = FILE_NAME_EML; // file name is EML by default
 }

 
 /*
  * Class methods
  */
 

 /*
  * Instance methods
  */

 /**
  * Gets the EML metadata from the file system and returns the file.
  * 
  * @param  evaluateMode   true if this is evaluate mode
  * @return the Level-1 EML metadata file, or null if it doesn't exist
  */
	public File getMetadata(boolean evaluateMode) throws IOException {
		File metadataFile = null; // The XML metadata file

		if (this.emlPackageId != null) {
			try {
				String baseDir = DataPackageManager.getMetadataResourceLocation(
						                   emlPackageId, ResourceType.metadata);
				FileSystemResource metadataResource = 
						new FileSystemResource(baseDir, emlPackageId);
				metadataResource.setEvaluateMode(evaluateMode);
				boolean isReportResource = false;
				String dirPath = metadataResource.getDirPath(isReportResource);
				String metadataFilename = fileName;
				metadataFile = new File(dirPath, metadataFilename);
				
				if (metadataFile == null || !metadataFile.exists()) {
					String msg = 
							String.format("Metadata file %s/%s does not exist.", 
									      dirPath, metadataFilename);
					throw new IOException(msg);
				}
			}
			catch (Exception e) {
				throw new IOException(
						"Metadata resource location could not be determined.");
			}
		}
		else {
			throw new IOException("No packageId was specified for the resource.");
		}

		return metadataFile;
	} 
 
	
 /**
  * Sets the isDublinCore variable to true or false. A value of true
  * indicates that we are working with Dublin Core metadata, else we
  * default to EML metadata.
  * 
  * @param isDublinCore   true indicates Dublin Core, false indicates EML
  */
 public void setDublinCore(boolean isDublinCore) {
	 fileName = isDublinCore ? FILE_NAME_DC : FILE_NAME_EML;
 }
 
}