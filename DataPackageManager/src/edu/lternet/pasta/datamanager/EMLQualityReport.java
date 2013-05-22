/**
 *
 * $Date$
 * $Author$
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

package edu.lternet.pasta.datamanager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.ecoinformatics.datamanager.parser.DataPackage;
import org.ecoinformatics.datamanager.quality.QualityReport;

import edu.lternet.pasta.common.EmlPackageId;
import edu.ucsb.nceas.utilities.IOUtil;

/**
 * Class that stores and retrieves quality reports.
 * 
 * @author dcosta
 * @created 16-Dec-2011 4:30:02 PM
 */
public class EMLQualityReport {

  /*
   * Class fields
   */
  
  private static String QUALITY_REPORT_FILE_NAME = "quality_report.xml";
  

  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(EMLQualityReport.class);
  
  private EmlPackageId emlPackageId = null;
  private EMLDataPackage emlDataPackage = null;
  
  
  /*
   * Constructors
   */
  
  /**
   * Constructs an EMLQualityReport given an emlPackageId.
   * 
   * @param  emlPackageId, an object that represents the packageId
   * @param  entityId, the entity identifier
   */
  public EMLQualityReport(EmlPackageId emlPackageId){
    this.emlPackageId = emlPackageId;
  }

  
  /**
   * Constructs an EMLQualityReport given an emlPackageId and 
   * EMLDataPackage.
   * 
   * @param  emlPackageId an object that represents the packageId
   * @param  emlDataPackage  the EMLDataPackage object
   */
  public EMLQualityReport(EmlPackageId emlPackageId, EMLDataPackage emlDataPackage) {
    this.emlPackageId = emlPackageId;
    this.emlDataPackage = emlDataPackage;
  }

  
  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */

	/**
	 * Stores a quality report on the file system.
	 * 
	 * @param   evaluateMode   true if this is evaluate mode
	 * @param   transaction    the transaction identifier
	 * @return  true if success storing the report, else false
	 */
	public boolean storeQualityReport(boolean evaluateMode, String transaction) 
	        throws IOException {
	  boolean success = false;
	  
	  if (this.emlPackageId != null) {
	    EMLFileSystemDataPackage emlFileSystemDataPackage = 
	        new EMLFileSystemDataPackage(emlPackageId);
	    emlFileSystemDataPackage.setEvaluateMode(evaluateMode);
	    String dirPath = emlFileSystemDataPackage.getDirPath();   
	    File dirFile = new File(dirPath);	 
	    if (dirFile != null && !dirFile.exists()) { dirFile.mkdirs(); }
      String qualityReportFilename = composeQualityReportFilename(evaluateMode, transaction);
	    File qualityReportFile = new File(dirPath, qualityReportFilename);
	    FileWriter fileWriter;
	    if (emlDataPackage != null) {
        DataPackage dataPackage = emlDataPackage.getDataPackage();
        if (dataPackage != null) {
          QualityReport qualityReport = dataPackage.getQualityReport();
          if (qualityReport != null) {
            String qualityReportXML = qualityReport.toXML();
            if (qualityReportXML != null) {
              StringBuffer stringBuffer = new StringBuffer(qualityReportXML);
  	          try {
	              fileWriter = new FileWriter(qualityReportFile);
	              IOUtil.writeToWriter(stringBuffer, fileWriter, true);
	            }
	            catch (IOException e) {
	              logger.error("IOException storing quality report:\n" + 
	                           e.getMessage());
	              e.printStackTrace();
	              throw(e);
	            }
	            finally {
	              success = (qualityReportFile != null) && 
	                        (qualityReportFile.exists());
	            }
            }
          }
	      }
	    }
	  }
      
	  return success;	  
	}
	
	
	public String composeQualityReportFilename(boolean evaluateMode, String transaction) {
	  String filename = null;
	  
	  // If evaluate mode, tag the quality report file name with the transaction identifier
	  if (evaluateMode) {
	    filename = String.format("%s.xml", transaction);
	  }
	  else {
	    filename = QUALITY_REPORT_FILE_NAME;
	  }
	  
	  return filename;
	}

}