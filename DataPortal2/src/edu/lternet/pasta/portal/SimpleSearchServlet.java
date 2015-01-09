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
import edu.lternet.pasta.client.ResultSetUtility;
import edu.lternet.pasta.portal.search.Search;
import edu.lternet.pasta.portal.search.SimpleSearch;

public class SimpleSearchServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.SimpleSearchServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslpath = null;

  private static final String forward = "./searchResult.jsp";
  
  /*
   * Instance variables
   */
  

  /**
   * Constructor of the object.
   */
  public SimpleSearchServlet() {
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
		String html = null;
		String xml = null;
		HttpSession httpSession = request.getSession();
		String queryText = null;
		
		String q = (String) request.getParameter("q");
		if (q == null || q.equals("")) {
			// if no q param was passed, look for query stored in the session
			queryText = (String) httpSession.getAttribute("queryText");
			html = (String) httpSession.getAttribute("termsListHTML");
		}
		else {
			queryText = q;
		}
		
		String start = (String) request.getParameter("start");
		String rows = (String) request.getParameter("rows");
		
		String uid = (String) httpSession.getAttribute("uid");
		if (uid == null || uid.isEmpty()) uid = "public";

		if (queryText != null) {
			try {
				queryText = String.format("%s&start=%s&rows=%s", queryText,
						start, rows);
				DataPackageManagerClient dpmClient = new DataPackageManagerClient(
						uid);
				xml = dpmClient.searchDataPackages(queryText);
				ResultSetUtility resultSetUtility = new ResultSetUtility(xml);
				if (html == null) html = "";
				html = html + resultSetUtility.xmlToHtmlTable(cwd + xslpath);
			}
			catch (Exception e) {
				handleDataPortalError(logger, e);
			}

			request.setAttribute("searchresult", html);
		}

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
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
		String html = null;
		String xml = null;
		HttpSession httpSession = request.getSession();
		
		String uid = (String) httpSession.getAttribute("uid");
		if (uid == null || uid.isEmpty()) uid = "public";
		
		String userInput = (String) request.getParameter("terms");

		try {
			html = "<p> Terms used in this search: <b>" + userInput + "</b></p>\n";
		    httpSession.setAttribute("termsListHTML", html);
			String queryText = SimpleSearch.buildSolrQuery(userInput, false);
			httpSession.setAttribute("queryText", queryText);
			queryText = String.format("%s&start=%d&rows=%d", queryText, 0, Search.DEFAULT_ROWS);
			DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
			xml = dpmClient.searchDataPackages(queryText);
			ResultSetUtility resultSetUtility = new ResultSetUtility(xml);
			html = html + resultSetUtility.xmlToHtmlTable(cwd + xslpath);
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}

		request.setAttribute("searchresult", html);
		RequestDispatcher requestDispatcher = request
				.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
	}


  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("resultsetutility.xslpath");
    logger.debug("XSLPATH: " + xslpath);
    cwd = options.getString("system.cwd");
    logger.debug("CWD: " + cwd);

  }

  
}
