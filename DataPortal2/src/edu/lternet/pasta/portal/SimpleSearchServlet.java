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
import edu.lternet.pasta.portal.search.SimpleSearch;
import edu.lternet.pasta.portal.search.TermsList;

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
		String html = null;
		String xml = null;
		TermsList termsList = new TermsList();
		HttpSession httpSession = request.getSession();
		String uid = (String) httpSession.getAttribute("uid");
		if (uid == null || uid.isEmpty())
			uid = "public";
		String terms = (String) request.getParameter("terms");
		String query = null;

		try {
			if (terms != null) {
				boolean tokenize = true;
				boolean isSiteTerm = false;
				if (terms.equals("*")) {
					query = SimpleSearch.buildPathQueryXml("", termsList,
							tokenize, isSiteTerm);
					termsList.addTerm("*");
				}
				else {
					query = SimpleSearch.buildPathQueryXml(terms, termsList,
							tokenize, isSiteTerm);
				}
			}

			DataPackageManagerClient dpmClient = new DataPackageManagerClient(
					uid);
			xml = dpmClient.searchDataPackages(query);
			ResultSetUtility resultSetUtility = new ResultSetUtility(xml);
			html = "<p> Terms used in this search: " + termsList.toHTML()
					+ "</p>\n";
			html += resultSetUtility.xmlToHtmlTable(cwd + xslpath);
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
