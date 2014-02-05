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

import org.apache.log4j.Logger;

import edu.lternet.pasta.client.AuditManagerClient;
//import edu.lternet.pasta.client.PastaAuthenticationException;
//import edu.lternet.pasta.client.PastaConfigurationException;
//import edu.lternet.pasta.client.PastaEventException;


public class DataPackageSurvey {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.DataPackageSurvey.class);



	public String[] surveyDataPackages(String criterion, int n) {
		String[] surveyResults = null;
		AuditManagerClient auditClient = null;
		String auditXML = null;
		StringBuffer queryBuffer = new StringBuffer();
		String popularServiceMethod = "readDataPackage";
		String recentServiceMethod1 = "createDataPackage";
		String recentServiceMethod2 = "uploadDataPackage";
		String recentFromTime = "2013-10-01";
		String uid = "public";

		if (criterion == null || criterion.isEmpty()) {
			criterion = "popular";
		}

		if (criterion.equals("recent") || criterion.equals("popular")) {

			queryBuffer.append("?category=info");

			if (criterion.equals("recent")) {
				queryBuffer.append(String.format("&serviceMethod=%s",
						recentServiceMethod1));
				queryBuffer.append(String.format("&serviceMethod=%s",
						recentServiceMethod2));
				queryBuffer.append(String.format("&fromTime=%s",
						recentFromTime));
			}
			else {
				queryBuffer.append(String.format("&serviceMethod=%s",
						popularServiceMethod));
			}

			try {
				auditClient = new AuditManagerClient(uid);

				if (auditClient != null) {
					String queryStr = queryBuffer.toString();
					//auditXML = auditClient.reportByFilter(queryStr);
					surveyResults = parseAuditXML(auditXML, criterion, n);
				}
			}
			catch (Exception e) {

			}

		}

		return surveyResults;

	}
	
	
	public String[] parseAuditXML(String auditXML, String criterion, int n) {
		String[] surveyResults = null;

		if (criterion != null) {
			surveyResults = new String[n * 4]; // (1) scope, (2) identifier, (3) title, (4) date

			surveyResults[0] = "knb-lter-mcr";
			surveyResults[1] = "1036";
			surveyResults[2] = "MCR LTER: Coral Reef: Bathymetry Grid for North Shore";
			surveyResults[3] = "2013-12-06";

			surveyResults[4] = "knb-lter-sbc";
			surveyResults[5] = "21";
			surveyResults[6] = "SBCLTER: Reef: Net primary production, growth and standing crop of Macrocystis pyrifera in Southern California";
			surveyResults[7] = "2013-07-07";

			surveyResults[8] = "knb-lter-nin";
			surveyResults[9] = "1";
			surveyResults[10] = "Daily Water Sample Nutrient Data for North Inlet Estuary, South Carolina, from 1978 to 1992, North Inlet LTER";
			surveyResults[11] = "2014-01-13";

			surveyResults[12] = "knb-lter-nin";
			surveyResults[13] = "99";
			surveyResults[14] = "Meteorological data for North Inlet Estuary, South Carolina, from 1982 to 1985, North Inlet LTER";
			surveyResults[15] = "2014-01-15";
		}

		return surveyResults;
	}
 
}
