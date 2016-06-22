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

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;

/**
 * 
 * Manages the creation, storage, retrieval, and deletion of local
 * file system data packages.
 * 
 * @author dcosta
 * @created 16-Dec-2011 4:30:02 PM
 */
public class EMLFileSystemDataPackage extends EMLDataPackage {

  /*
   * Class fields
   */
  

  /*
   * Instance fields
   */
  
  private String baseDir = null;  /* Top-level directory for all entities */
  private boolean evaluateMode = false;
  
  
  /*
   * Constructors
   */
  
  /**
   * Constructs an EMLFileSystemDataPackage object based on the EmlPackageId.
   * 
   * @param  emlPackageId   an EMLPackageId object
   */
  public EMLFileSystemDataPackage(EmlPackageId emlPackageId) {
    this.baseDir = EMLDataManager.getMetadataDir();
    EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
    if (emlPackageIdFormat != null) {
      this.packageId = emlPackageIdFormat.format(emlPackageId);
    }
  }


  /*
   * Class methods
   */
  

  /*
   * Instance methods
   */

  /**
   * Returns a path to the file system directory where the data package is stored.
   * 
   * @return  dirPath, the path to the directory, a String
   */
  public String getDirPath() {
    String dirPath = null;

    if (this.evaluateMode) {
      dirPath = EMLDataManager.getReportDir();
    } else {
	    StringBuffer stringBuffer = new StringBuffer("");
	    stringBuffer.append(this.baseDir);
	    stringBuffer.append("/");
	    stringBuffer.append(this.packageId);
	    dirPath = stringBuffer.toString();
    }
    
    return dirPath;
  }

  
  /**
   * Retrieves the evaluateMode boolean value.
   * 
   * @return evaluateMode, true if evaluate mode is set
   */
  public boolean isEvaluateMode() {
    return evaluateMode;
  }

  
  /**
   * Sets the evaluateMode boolean value.
   * 
   * @param evaluateMode, the boolean value to set
   */
  public void setEvaluateMode(boolean evaluateMode) {
    this.evaluateMode = evaluateMode;
  }

}
