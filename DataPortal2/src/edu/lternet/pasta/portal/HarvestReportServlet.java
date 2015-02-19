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

package edu.lternet.pasta.portal;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;


public class HarvestReportServlet extends DataPortalServlet  {
  
  /*
   * Class variables
   */

  private static String harvesterPath = null;
  private static long harvesterReportTTL;  // harvester report time to live in milliseconds
  public static long harvesterReportDaysToLive; // harvester report time to live in days
  private static long harvesterReportDaysToLiveDefault = 180; // default harvester report time to live in days
  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.HarvestReportServlet.class);
  private static final long serialVersionUID = 1L;
  
  
  /*
   * Instance variables
   */
  


  /*
   * Constructors
   */
  
  
  /*
   * Class methods
   */
  
  /**
   * Accesss the harvesterPath class variable.
   * 
   * @return  the harvester path setting
   */
  public static String getHarvesterPath() {
    return harvesterPath;
  }
  
  
  /*
   * Instance methods
   */

  /**
   * The doGet method of the servlet. <br>
   * 
   * This method is called when a form has its tag value method equals to get.
   * 
   * @param request
   *          the request send by the client to the server
   * @param response
   *          the response send by the server to the client
   * @throws ServletException
   *           if an error occurred
   * @throws IOException
   *           if an error occurred
   */
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    // Pass request onto "doPost".
    doPost(request, response);
  }

  
  /**
   * The doPost method of the servlet. <br>
   * 
   * This method is called when a form has its tag value method equals to post.
   * 
   * @param request
   *          the request send by the client to the server
   * @param response
   *          the response send by the server to the client
   * @throws ServletException
   *           if an error occurred
   * @throws IOException
   *           if an error occurred
   */
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			HttpSession httpSession = request.getSession();
			String uid = (String) httpSession.getAttribute("uid");
			String warningMessage = null;

			if (uid == null) {
				warningMessage = "<p class=\"warning\">" + LOGIN_WARNING
						+ "</p>";
				request.setAttribute("message", warningMessage);
			}
			else {
				String reportId = request.getParameter("reportId");
				if (reportId != null && reportId.length() > 0) {
					httpSession.setAttribute("harvestReportID", reportId);
				}
			}

			if (warningMessage == null || warningMessage.length() == 0) {
				response.sendRedirect("./harvestReport.jsp");
			}
			else {
				RequestDispatcher requestDispatcher = request
						.getRequestDispatcher("./harvestReport.jsp");
				requestDispatcher.forward(request, response);
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}
	}
 
  
	/*
	 * Converts days to milliseconds.
	 */
	private long daysToMilliseconds(long days) {
		return days * 24 * 60 * 60 * 1000;
	}


  	/*
  	 * Spawns off a thread to execute the harvest report manager
  	 * to purge old harvest reports at start-up.
  	 */
  	private void executeHarvesterReportManager() {
  		HarvestReportManager harvestReportManager = new HarvestReportManager(harvesterPath, harvesterReportTTL);
		ExecutorService executorService = Executors.newCachedThreadPool();
		executorService.execute(harvestReportManager);
		executorService.shutdown();
  	}
  
  
	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		Configuration options = ConfigurationListener.getOptions();
		harvesterPath = options.getString("harvester.path");

		/*
		 * Purge old harvest reports
		 */

		/*
		 * Use default TTL value if the property was not specified or is 0 or
		 * less
		 */
		try {
			harvesterReportDaysToLive = options.getLong("harvester.report.daysToLive");
			if (harvesterReportDaysToLive <= 0) {
				harvesterReportDaysToLive = harvesterReportDaysToLiveDefault;
			}
		}
		catch (java.util.NoSuchElementException e) {
			harvesterReportDaysToLive = harvesterReportDaysToLiveDefault;
		}

		harvesterReportTTL = daysToMilliseconds(harvesterReportDaysToLive);
		executeHarvesterReportManager();
	}

}
