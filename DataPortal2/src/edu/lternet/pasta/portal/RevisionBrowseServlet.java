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

import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.common.UserErrorException;

public class RevisionBrowseServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.RevisionBrowseServlet.class);
  private static final long serialVersionUID = 1L;
  private static final String forward = "./dataPackageBrowser.jsp";
  private static final String browseMessage = "Select a data package "
  		+ "<em>scope.identifer.revision</em> value to view a specific "
  		+ "revision of the data package lineage.";

  /**
   * Constructor of the object.
   */
  public RevisionBrowseServlet() {
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
		String identifier = request.getParameter("identifier");
		Integer id = Integer.valueOf(identifier);
		String text = null;
		String html = null;
		Integer count = 0;

		try {

			if (scope != null && !(scope.isEmpty()) && identifier != null
					&& !(identifier.isEmpty())) {
				DataPackageManagerClient dpmClient = new DataPackageManagerClient(
						uid);
				text = dpmClient.listDataPackageRevisions(scope, id, null);
				StrTokenizer tokens = new StrTokenizer(text);
				html = "<ol>\n";

				while (tokens.hasNext()) {
					String revision = tokens.nextToken();					
					String resourceMetadata = dpmClient.readResourceMetadata(scope, id, revision);
					String dateCreated = "";
					
					if (resourceMetadata != null) {
						// <dateCreated>2013-01-10 15:56:22.264</dateCreated>
						String[] lines = resourceMetadata.split("\\n");
						for (String line : lines) {
							String trimmedLine = line.trim();
							if (trimmedLine != null && trimmedLine.startsWith("<dateCreated>")) {
                                String dateStr = trimmedLine.substring(13, 23);
								dateCreated = String.format("&nbsp;&nbsp;(<small><em>Uploaded %s</em></small>)", dateStr);
							}
						}
					}
					
					html += String.format("<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope=%s" +
							"&identifier=%s&revision=%s\">%s.%s.%s</a>%s</li>\n",
							scope, identifier, revision, scope, identifier, revision, dateCreated);
					
					count++;
				}

				html += "</ol>\n";
			}
			else {
				String msg = "The 'scope', 'identifier', or 'revision' field of the packageId is empty.";
				throw new UserErrorException(msg);
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}

		request.setAttribute("browsemessage", browseMessage);
		request.setAttribute("html", html);
		request.setAttribute("count", count.toString());
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
    // Put your code here
  }

}
