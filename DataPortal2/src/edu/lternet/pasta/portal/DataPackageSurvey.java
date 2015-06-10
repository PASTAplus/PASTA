/*
 *
 * $Date: 2012-08-30 09:55:43 -0700 (Thu, 30 Aug 2012) $
 * $Author: dcosta $
 * $Revision: 2325 $
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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.AuditManagerClient;
import edu.lternet.pasta.client.RecentUpload;


public class DataPackageSurvey {

	/*
	 * Class variables
	 */

	
	/*
	 * Instance variables
	 */

	Integer numberOfDays = new Integer(100);
	
	
	/*
	 * Class methods
	 */
	
	
	/**
	 * Used in the footer.jsp to add HTML space padding to the title when needed
	 * to avoid formatting issues with Recently Added and Recently Updated
	 * display.
	 * 
	 * @param  the data package title
	 * @param  the minimum length of the title
	 * @return the space padding string needed to meet the minimum length,
	 *         a sequence of HTML &nbsp; characters
	 */
	public static String spacePadding(String title, int minLength) {
		String spacePadding = "";
		if (title.length() < minLength) {
			spacePadding = StringUtils.repeat("&nbsp;", (minLength - title.length()));
		}
		return spacePadding;
	}

	
	/*
	 * Instance methods
	 */

	/**
	 * Forces the refresh of the data package survey results stored in the
	 * AuditManagerClient class. We do this when we're certain that the
	 * results need to be refreshed, such as when an event subscription tells
	 * us that a data package in PASTA has been inserted or updated.
	 */
	public void refreshSurveyResults() {
		boolean forceRefresh = true;
		Integer limit = new Integer(2);
		AuditManagerClient.getRecentInserts(numberOfDays, limit, forceRefresh);
		AuditManagerClient.getRecentUpdates(numberOfDays, limit, forceRefresh);
	}
	
	
	/**
	 * Survey the audit manager to get a list of recent inserts and recent update in PASTA.
	 * 
	 * @param criterion  One of two supported values, "recentInserts" or "recentUpdates"
	 * @param n  The number of survey results to return
	 * @return  An array of strings which is 4 times the length of n. Each set of four
	 *          strings contains the scope, identifier, title, and date of the data
	 *          package insert or update.
	 */
	public String[] surveyDataPackages(String criterion, int n) {
		boolean forceRefresh = false;
		Integer limit = new Integer(n);
		int arrayLength = n * 4;
		String[] surveyResults = new String[arrayLength]; // (1) scope, (2) identifier, (3) title, (4) date
		List<RecentUpload> recentUploads = null;
		
		for (int i = 0; i < arrayLength; i++) {
			surveyResults[i] = "";
		}

		if (criterion == null || criterion.isEmpty()) {
			criterion = "recentInserts";
		}

		if (criterion.equals("recentInserts")) {
			recentUploads = AuditManagerClient.getRecentInserts(numberOfDays, limit, forceRefresh);
		}
		else if (criterion.equals("recentUpdates")) {
			recentUploads = AuditManagerClient.getRecentUpdates(numberOfDays, limit, forceRefresh);
		}
		
		if (recentUploads != null) {
			for (int i = 0; (i < n) && (i < recentUploads.size()); i++) {
				RecentUpload recentUpload = recentUploads.get(i);
				int j = (i * 4);
				surveyResults[j+0] = recentUpload.getScope();
				surveyResults[j+1] = recentUpload.getIdentifier().toString();
				surveyResults[j+2] = recentUpload.getTitle();
				surveyResults[j+3] = recentUpload.getUploadDate();
			}
		}

		return surveyResults;
	}
	
}
