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

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.datamanager.EMLQualityReport;
import edu.ucsb.nceas.utilities.Options;


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
 
	private static Logger logger = Logger.getLogger(DataPackageReport.class);
	private static final String dirPath = "WebRoot/WEB-INF/conf";
	private static final String REPORT_DIR_DEFAULT = "/home/pasta/local/report";

 /*
  * Instance fields
  */
 
 private EmlPackageId emlPackageId = null;
 private String reportDir = null;
 
 /*
  * Constructors
  */
 
 /**
  * Constructs a DataPackageReport given an EmlPackageId.
  * 
  * @param  emlPackageId, an object that represents the packageId
  */
 public DataPackageReport(EmlPackageId emlPackageId) {
   this.emlPackageId = emlPackageId;

		Options options = null;
		options = ConfigurationListener.getOptions();

		if (options == null) {
			ConfigurationListener configurationListener = new ConfigurationListener();
			configurationListener.initialize(dirPath);
			options = ConfigurationListener.getOptions();
		}

		reportDir = options.getOption("datapackagemanager.reportDir");

		if (reportDir == null || reportDir.isEmpty()) {
			String gripe = "Report directory property not set! Reverting to "
			    + "default report directory: " + REPORT_DIR_DEFAULT;
			logger.warn(gripe);
		}
   
 }

 
 /*
  * Class methods
  */
 

 /*
  * Instance methods
  */

 /**
  * Return the evaluate quality report for the given transaction identifier.
  * 
  * @param transaction The evaluate quality report transaction identifier.
  * @return The evaluate quality report File object.
  */
 public File getEvaluateReportFile(String transaction) {
	 File rFile = null;
	 rFile = new File(reportDir, String.format("%s.xml", transaction));
	 return rFile;
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
 public File getReport(boolean evaluate, String transaction) 
         throws IOException {
   String reportFilename = null;
   File xmlFile = null;
   
   if (this.emlPackageId != null) {
     FileSystemResource reportResource = new FileSystemResource(emlPackageId);
     reportResource.setEvaluateMode(evaluate);
     boolean isReportResource = true;
     String dirPath = reportResource.getDirPath(isReportResource);
     EMLQualityReport emlQualityReport = new EMLQualityReport(this.emlPackageId);
     if (emlQualityReport != null) {
       reportFilename = emlQualityReport.composeQualityReportFilename(evaluate, transaction);
       File qualityReportFile = new File(dirPath, reportFilename);
     
       if (qualityReportFile != null && qualityReportFile.exists()) {
         xmlFile = qualityReportFile;
       }
       else {
      	 String msg = String.format("Report file %s/%s does not exist.",
      			                    dirPath, reportFilename);
      	 throw new IOException(msg);
       }
       
     }
   }
     
   return xmlFile;
 }
 
}