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

package edu.lternet.pasta.portal;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.portal.search.MapResultSetUtility;

public class MapSearchServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.MapSearchServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslpath = null;
  private static final String forward = "./mapSearchResult.jsp";
  
  
  /*
   * Instance variables
   */
  

  /**
   * Constructor of the object.
   */
  public MapSearchServlet() {
    super();
  }

  
  /**
   * Destruction of the servlet. <br>
   */
  public void destroy() {
    super.destroy(); // Just puts "destroy" string in log
    // Put your code here
  }

  
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
		String mapSearchResults = null;
		HttpSession httpSession = request.getSession();
		
		String uid = (String) httpSession.getAttribute("uid");
		if (uid == null || uid.isEmpty()) uid = "public";
		
		String queryText = (String) httpSession.getAttribute("queryText");
		mapSearchResults = executeQuery(uid, queryText);
		
		request.setAttribute("mapSearchResults", mapSearchResults);
		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
	}
	
	
	/*
	 * Executes the query via the DataPackageManagerClient object
	 */
	private String executeQuery(String uid, String queryText)
			throws ServletException {
		String mapSearchResults = null;
		final int ROWS_MAX = 100000;

		try {
			DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
			String extendedQueryText = String.format("%s&rows=%d", queryText, ROWS_MAX);
			String xml = dpmClient.searchDataPackages(extendedQueryText);
			MapResultSetUtility	mapResultSetUtility = new MapResultSetUtility();
			mapSearchResults = mapResultSetUtility.parseResultSet(xml);
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}
		
		return mapSearchResults;
	}


  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("mapresultsetutility.xslpath");
    logger.debug("XSLPATH: " + xslpath);
    cwd = options.getString("system.cwd");
    logger.debug("CWD: " + cwd);
  }

  
}
