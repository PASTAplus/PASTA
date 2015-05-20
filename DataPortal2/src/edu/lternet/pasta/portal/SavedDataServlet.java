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

import edu.lternet.pasta.client.ResultSetUtility;
import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.portal.search.Search;
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
		HttpSession httpSession = request.getSession();
		String forward = (String) request.getParameter("forward");
		String startStr = (String) request.getParameter("start");
		String rowsStr = (String) request.getParameter("rows");
		String sort = (String) request.getParameter("sort");
		
		if (sort == null || sort.equals("")) {
			sort = String.format("%s,%s", Search.PACKAGEID_SORT, Search.SORT_ORDER_ASC);
		}

		if (forward == null) forward = "savedData.jsp";
		
		String uid = (String) httpSession.getAttribute("uid");
		if (uid == null || uid.isEmpty()) {
			uid = "public";
		}

		String message = null;
		if (uid.equals("public")) {
			message = LOGIN_WARNING;
			forward = "./login.jsp";
			request.setAttribute("message", message);
			request.setAttribute("from", "savedDataServlet");
		}
		else {
			String operation = (String) request.getParameter("operation"); // "save" or "unsave"
			String packageId = (String) request.getParameter("packageId");
			SavedData savedData = new SavedData(uid);
			
			/*
			 * First, execute any operations that change the state of the saved data
			 */
			if (operation != null && packageId != null) {
				EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
				EmlPackageId emlPackageId = emlPackageIdFormat.parse(packageId);
				String scope = emlPackageId.getScope();
				Integer identifier = emlPackageId.getIdentifier();
				Integer revision = emlPackageId.getRevision();

				if (operation.equals("save")) {
					savedData.addDocid(scope, identifier, revision);
				}
				else if (operation.equals("unsave")) {
					savedData.removeDocid(scope, identifier);
				}
			}
			
			/*
			 * If we are forwarding to the saved data page, update the HTML
			 * to be displayed
			 */
			if (forward != null && forward.equals("savedData.jsp")) {
				String html = "<p>There are no data packages on your data shelf.</p>";
				String termsListHTML = "";
				String xml = null;
				response.setContentType("text/html");
				try {
					xml = savedData.getSavedDataXML(startStr, rowsStr, sort);
					if (xml != null) {
						httpSession.setAttribute("termsListHTML", termsListHTML);
						boolean isSavedDataPage = true;
						ResultSetUtility resultSetUtility = new ResultSetUtility(xml, sort, savedData, isSavedDataPage);
						html = resultSetUtility.xmlToHtmlTable(cwd + xslpath);
					}
					request.setAttribute("searchresult", html);
				}
				catch (Exception e) {
					handleDataPortalError(logger, e);
				}
			}
		}

		/*
		 * Forward to login.jsp or savedData.jsp, but not to other values because
		 * they use AJAX and we don't want a page refresh
		 */
		if (forward != null && 
		    !forward.equals("") && 
		    !forward.equals("savedData") &&
		    !forward.equals("simpleSearch")
		   ) {
			RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
			requestDispatcher.forward(request, response);
		}
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
