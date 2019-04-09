/*
 *
 * $Date: 2014-05-01 11:10:19 -0700 (Mon, 02 Apr 2012) $
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

package edu.lternet.pasta.portal;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.portal.statistics.GrowthStats;

/**
 * Servlet implementation class ArchiveCleanerServlet
 */
@WebServlet(urlPatterns = { "/dynamic-content-refresh" })
public class DynamicContentServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = Logger
			.getLogger(DynamicContentServlet.class);
	
	
	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}


	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {	
		refreshGrowthStats();
		refreshSurveyResults();
	}
	
	
	/*
	 * Refresh the growth stats and store them in a context attribute.
	 */
	private void refreshGrowthStats() throws ServletException {
        ServletContext servletContext = getServletContext();
		String numDataPackages = null;
		String numDataPackagesSites = null;
		String numDataPackagesAll = null;
		String numDataPackagesSitesAll = null;
				
        logger.info("Refreshing PASTA data package growth stats.");

        try {
			PastaStatistics pastaStats = new PastaStatistics("public");
			numDataPackages = pastaStats.getNumDataPackages(true).toString();
			numDataPackagesSites = pastaStats.getNumDataPackages(false).toString();
			numDataPackagesAll = pastaStats.getNumDataPackagesAllRevisions(true).toString();
			numDataPackagesSitesAll = pastaStats.getNumDataPackagesAllRevisions(false).toString();
		}
		catch (PastaConfigurationException | PastaAuthenticationException e) {
			ServletException se = new ServletException("Pasta statistics exception");
			se.initCause(e);
			throw se;		
		}

        GrowthStats gs = new GrowthStats();
        String googleChartJson = gs.getGoogleChartJson(new GregorianCalendar(), Calendar.MONTH);

        /* Lock the servlet context object to guarantee that only one thread at a
         * time can be getting or setting the context attribute. 
         */
        synchronized(servletContext) {
        	if (numDataPackages != null) 
        		servletContext.setAttribute("numDataPackages", numDataPackages);
        	
        	if (numDataPackagesSites != null) 
        		servletContext.setAttribute("numDataPackagesSites", numDataPackagesSites);
        	
        	if (numDataPackagesAll != null) 
        		servletContext.setAttribute("numDataPackagesAll", numDataPackagesAll);
        	
        	if (numDataPackagesSitesAll != null) 
        		servletContext.setAttribute("numDataPackagesSitesAll", numDataPackagesSitesAll);
        	
        	if (googleChartJson != null) 
        		servletContext.setAttribute("googleChartJson", googleChartJson);
        }
	}


	/*
	 * Refresh the survey results. The DataPackageSurvey class handles the
	 * refresh and storage logic.
	 */
	private void refreshSurveyResults() {
		DataPackageSurvey dps = new DataPackageSurvey();
		long sleepTime = 3000L;
		
        /* Sleep to allow audit record to be logged prior to beginning the refresh
        try {
			Thread.sleep(sleepTime);
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}*/
        
		logger.info("Refreshing recent data package inserts and updates.");
		dps.refreshSurveyResults();
	}


}
