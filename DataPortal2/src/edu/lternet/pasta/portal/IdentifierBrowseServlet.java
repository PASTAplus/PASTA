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
import java.util.ArrayList;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;

public class IdentifierBrowseServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.IdentifierBrowseServlet.class);
  private static final long serialVersionUID = 1L;
  private static final String forward = "./dataPackageBrowser.jsp";
  private static final String browseMessage = "Select a data package "
  		+ "<em>scope.identifier</em> value to view the most current "
  		+ "revision of the data package lineage.";
  private static String pastaUriHead = null;

  /**
   * Constructor of the object.
   */
  public IdentifierBrowseServlet() {
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

    HttpSession httpSession = request.getSession();

    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty())
      uid = "public";

    String scope = request.getParameter("scope");

    String text = null;
    String html = null;
    Integer count = 0;

    if (scope != null && !(scope.isEmpty())) {

      try {

        DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
        text = dpmClient.listDataPackageIdentifiers(scope);

        StrTokenizer tokens = new StrTokenizer(text);

        html = "<ol>\n";

        ArrayList<String> arrayList = new ArrayList<String>();
        
        // Add scope/identifier values to a sorted set
        while (tokens.hasNext()) {
          arrayList.add(tokens.nextToken());
          count++;
        }
        
				// Output sorted set of scope/identifier values
				for (String identifier : arrayList) {

					html += "<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope=" + scope + "&identifier="
					    + identifier + "\">" + scope + "." + identifier
					    + "</a></li>\n";

				}

        html += "</ol>\n";

      } 
      catch (Exception e) {
    	  handleDataPortalError(logger, e);
      }    

    } 
    else {
      html = "<p class=\"warning\"> Error: \"scope\" field empty</p>\n";
    }

    request.setAttribute("browsemessage", browseMessage);
    request.setAttribute("html", html);
    request.setAttribute("count", count.toString());
    RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
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
		pastaUriHead = options.getString("pasta.uriHead");

		if ((pastaUriHead == null) || (pastaUriHead.equals(""))) {
			throw new ServletException(
			    "No value defined for 'pasta.uriHead' property.");
		}
    // Put your code here
  }

}
