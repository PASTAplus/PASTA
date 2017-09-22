/*
 * $Date: 2014-05-20 12:23:25 -0700 (Fri, 22 June 2012) $
 * $Author: dcosta $
 * $Revision: 2145 $
 *
 * Copyright 2011,2012 the University of New Mexico.
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

package edu.lternet.pasta.portal;

/**
 * The Tooltips class holds text content for tooltips that are displayed
 * in the data portal user interface. This allows all tooltips to be
 * edited in one place and also allows for sharing of text amongst multiple 
 * tooltips.
 * 
 * @author dcosta
 *
 */
public class Tooltip {
	
	public static final String DESKTOP_HARVEST = 
			"With this option, data URLs in the EML will be ignored. You will be asked to upload a data file " +
	        "for every data entity in the package.";

	public static final String SEARCH_TERMS = 
			"Enclose terms containing spaces within quotes, e.g., \"Puerto Rico\"";

	public static final String USE_CHECKSUM = 
			"When selecting this option, you allow PASTA to skip data upload of a data entity if PASTA " +
	        "already has a previous revision whose checksum matches the checksum that you have documented in the EML.";
}
