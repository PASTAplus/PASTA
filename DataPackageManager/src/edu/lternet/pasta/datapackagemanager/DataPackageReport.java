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
import edu.lternet.pasta.datamanager.EMLQualityReport;


/**
* Class that creates, stores, and retrieves data package quality reports.
* 
* @author dcosta
* @created
*/
public class DataPackageReport {

 /*
  * Class fields
  */
 

 /*
  * Instance fields
  */
 
 private EmlPackageId emlPackageId = null;
 
 
 /*
  * Constructors
  */
 
 /**
  * Constructs a DataPackageReport given an EmlPackageId.
  * 
  * @param  emlPackageId, an object that represents the packageId
  */
 public DataPackageReport(EmlPackageId emlPackageId){
   this.emlPackageId = emlPackageId;
 }

 
 /*
  * Class methods
  */
 

 /*
  * Instance methods
  */

 /**
  * Deletes a report from the file system.
  * 
  * @return  true if the quality report was successfully deleted, else false
  */
 public boolean deleteReport(){
   boolean success = false;
   
   // We only delete quality reports in non-evaluate mode
   boolean evaluate = false;
   String transaction = null;
   
   File reportFile = getReport(evaluate, transaction);
   
   if (reportFile != null) {
     success = reportFile.delete();
   }
   
   return success;
 }

 
 /**
  * Gets the quality report from the file system and returns the file.
  * 
  * Note that in this method we are assuming some unification between the
  * Data Package Manager and the Data Manager, at least to the extent
  * that they exist on the same file system. The method retrieves the
  * report, that was created by the Data Manager, directly from the file 
  * system rather than through a Data Manager service method. The
  * alternative would be to use the DataManagerClient class to run a
  * service method that interacts with the Data Manager service (which
  * could potentially be on another host.)
  * 
  * @param  evaluate     true if this is an evaluate report
  * @param  transaction  the transaction identifier, may be null 
  *                      (should always be non-null when evaluate is true)
  * @return the quality report XML file, or null if it doesn't exist
  */
 public File getReport(boolean evaluate, String transaction) {
   String reportFilename = null;
   File xmlFile = null;
   
   if (this.emlPackageId != null) {
     FileSystemResource reportResource = new FileSystemResource(emlPackageId);
     reportResource.setEvaluateMode(evaluate);
     String dirPath = reportResource.getDirPath();
     EMLQualityReport emlQualityReport = new EMLQualityReport(this.emlPackageId);
     if (emlQualityReport != null) {
       reportFilename = emlQualityReport.composeQualityReportFilename(evaluate, transaction);
       File qualityReportFile = new File(dirPath, reportFilename);
     
       if (qualityReportFile != null && qualityReportFile.exists()) {
         xmlFile = qualityReportFile;
       }
     }
   }
     
   return xmlFile;
 }
 
}