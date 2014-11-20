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

import edu.lternet.pasta.common.EmlPackageId;


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
 private final String FILE_NAME = "Level-1-EML.xml";
 
 
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
 public File getMetadata(boolean evaluateMode) {
   File xmlFile = null;
   
   if (this.emlPackageId != null) {
     FileSystemResource metadataResource = new FileSystemResource(emlPackageId);
     metadataResource.setEvaluateMode(evaluateMode);
     String dirPath = metadataResource.getDirPath();   
     String metadataFilename = FILE_NAME;
     File metadataFile = new File(dirPath, metadataFilename);
     
     if (metadataFile != null && metadataFile.exists()) {
       xmlFile = metadataFile;
     }
   }
     
   return xmlFile;
 }
 
}