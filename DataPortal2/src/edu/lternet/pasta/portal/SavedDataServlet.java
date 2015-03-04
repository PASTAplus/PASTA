/*
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 June 2012) $
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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.AuditManagerClient;
import edu.lternet.pasta.client.ReportUtility;
import edu.lternet.pasta.client.ResultSetUtility;
import edu.lternet.pasta.portal.search.BrowseTerm;
import edu.lternet.pasta.portal.search.TermsList;
import edu.lternet.pasta.portal.user.SavedData;


public class SavedDataServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(edu.lternet.pasta.portal.SavedDataServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslpath = null;
  
  
  /*
   * Instance variables
   */
  

  /*
   * Constructors
   */
  
  /**
   * Constructor of the object.
   */
  public SavedDataServlet() {
    super();
  }
  

  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */

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
		String forward = "./savedData.jsp";
		String html = null;
		TermsList termsList = null;
		String termsListHTML = "";
		String xml = null;
		HttpSession httpSession = request.getSession();
		
		String uid = (String) httpSession.getAttribute("uid");
		if (uid == null || uid.isEmpty()) {
			uid = "public";
		}

		String message = null;
		if (uid.equals("public")) {
			message = LOGIN_WARNING;
			forward = "./login.jsp";
		}
		else {
			SavedData savedData = new SavedData(uid);
			response.setContentType("text/html");

			try {
				xml = savedData.getSavedData();
				httpSession.setAttribute("termsListHTML", termsListHTML);
				ResultSetUtility resultSetUtility = new ResultSetUtility(xml);
				html = termsListHTML + resultSetUtility.xmlToHtmlTable(cwd + xslpath);
				request.setAttribute("searchresult", html);
			}
			catch (Exception e) {
				handleDataPortalError(logger, e);
			}
		}

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
	}


	/**
	 * Initialization of the servlet. <br>
	 * 
	 * @throws ServletException
	 *             if an error occurs
	 */
	public void init() throws ServletException {
		PropertiesConfiguration options = ConfigurationListener.getOptions();
		xslpath = options.getString("resultsetutility.xslpath");
		cwd = options.getString("system.cwd");
	}
  
}
