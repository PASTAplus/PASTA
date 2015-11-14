/*
 *
 * $Date: 2012-04-02 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
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

package edu.lternet.pasta.common.eml;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Node;


public class TemporalCoverage {

	private static final Logger logger = Logger.getLogger(TemporalCoverage.class);

	private String beginDate, endDate;
	private TreeSet<String> alternativeTimeScales = new TreeSet<String>();
	private TreeSet<String> singleDateTimes = new TreeSet<String>();
	
	
	public void addAlternativeTimeScale(CachedXPathAPI xpathapi, Node alternativeTimeScaleNode) {
		try {
			if (alternativeTimeScaleNode != null) {
				String alternativeTimeScale = "";
				Node timeScaleNameNode = xpathapi.selectSingleNode(alternativeTimeScaleNode, "timeScaleName");
				if (timeScaleNameNode != null) {
					String timeScaleName = timeScaleNameNode.getTextContent().trim();
					alternativeTimeScale += timeScaleName;
				}
				Node timeScaleAgeEstimateNode = xpathapi.selectSingleNode(alternativeTimeScaleNode, "timeScaleAgeEstimate");
				if (timeScaleAgeEstimateNode != null) {
					String timeScaleAgeEstimate = timeScaleAgeEstimateNode.getTextContent().trim();
					alternativeTimeScale += "; Age Estimate: " + timeScaleAgeEstimate;
				}
				alternativeTimeScale = alternativeTimeScale.trim();
				if (!alternativeTimeScale.equals("")) {
					alternativeTimeScales.add(alternativeTimeScale);
				}
			}
		}
		catch (TransformerException e) {
			logger.error("Error parsing document: TransformerException");
			e.printStackTrace();
		}
	}
	
	
	public void setBeginDate(String beginDate) {
		if (beginDate != null && beginDate.trim().length() > 0) {
			this.beginDate = beginDate;
		}
	}
	
	
	public void setEndDate(String endDate) {
		if (endDate != null && endDate.trim().length() > 0) {
			this.endDate = endDate;
		}
	}
	
	
	public void addSingleDateTime(String singleDateTime) {
		if (singleDateTime != null && singleDateTime.trim().length() > 0) {
			this.singleDateTimes.add(singleDateTime.trim());
		}
	}
	
	
	public String getBeginDate() {
		return beginDate;
	}
	
	
	public String getEndDate() {
		return endDate;
	}
	
	
	public Set<String> getAlternativeTimeScales() {
		return alternativeTimeScales;
	}

	
	public Set<String> getSingleDateTimes() {
		return singleDateTimes;
	}
			
}
