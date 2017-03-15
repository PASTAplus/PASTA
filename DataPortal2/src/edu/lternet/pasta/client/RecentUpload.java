/*
 *
 * $Date$
 * $Author$
 * $Revision$
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

package edu.lternet.pasta.client;

import java.util.ArrayList;
import java.util.HashMap;

import edu.lternet.pasta.common.DataPackageUpload;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;


public class RecentUpload extends DataPackageUpload {
	
	/*
	 *  Class fields
	 */
	static HashMap<String, String> knownTitles = new HashMap<String, String>();
	
	
	/*
	 *  Instance fields	
	 */

	String title;
	
	
	/*
	 *  Constructors
	 */
	
	/**
	 * Constructor to create a RecentUpload object.
	 * 
	 * @param dpmClient   the DataPackageManager client
	 * @param xml         the XML string containing information about the upload
	 */
	public RecentUpload(DataPackageManagerClient dpmClient, String uploadDate, String serviceMethod, 
            String scope, Integer identifier, Integer revision) {
		super(uploadDate, serviceMethod, scope, identifier, revision, null);
		
		String knownTitle = RecentUpload.getKnownTitle(packageId);
		if (knownTitle == null) {
			this.title = parseTitle(dpmClient);
			RecentUpload.addKnownTitle(packageId, title);
		}
		else {
			this.title = knownTitle;
		}
	}
	
	
	/*
	 * Class methods
	 */
	
	private static void addKnownTitle(String packageId, String title) {
		knownTitles.put(packageId, title);
	}
	
	
	private static String getKnownTitle(String packageId) {
		return knownTitles.get(packageId);
	}
	
	
	/*
	 * Instance methods
	 */
	
	private String parseTitle(DataPackageManagerClient dpmClient) {
		String title = "";

		try {
			String eml = dpmClient.readMetadata(scope, identifier, revision.toString());
			EMLParser emlParser = new EMLParser();
			DataPackage dataPackage = emlParser.parseDocument(eml);
			ArrayList<String> titleList = dataPackage.getTitles();
			if (titleList.size() > 0) {
				title = titleList.get(0);
			}
		}
		catch (Exception e) {

		}

		return title;
	}


	public String getTitle() {
		return title;
	}

}
