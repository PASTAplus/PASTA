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
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.EmlUtility;
import edu.lternet.pasta.common.ResourceNotFoundException;
import edu.lternet.pasta.common.UserErrorException;

public class MetadataViewerServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.MetadataViewerServlet.class);
  private static final long serialVersionUID = 1L;

  private static final String forward = "./metadataViewer.jsp";

  private static String cwd = null;
  private static String xslpath = null;

  /**
   * Constructor of the object.
   */
  public MetadataViewerServlet() {
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
    if (uid == null || uid.isEmpty()) uid = "public";
    String message = null;
    String metadataStr = null;

    /*
     * The packageId may be set by either a post parameter or by a query
     * parameter using a URL string
     */
    String packageId = request.getParameter("packageid");
    String url = request.getParameter("url");

    String contentType = request.getParameter("contentType");
    if (contentType == null || contentType.isEmpty()) {
      contentType = "text/html"; // Reset to default content type
    }

    boolean isValidPackageId = false;
    String[] tokens = null;
    String scope = null;
    Integer identifier = null;
    String revision = null;

    try {
    if (packageId != null && !packageId.isEmpty()) {

      tokens = packageId.split("\\.");

      if (tokens.length == 3) {
        isValidPackageId = true;
        scope = tokens[0];
        identifier = Integer.valueOf(tokens[1]);
        revision = tokens[2];     
      } 
      else {
        message = 
        	String.format(
        		"packageId '%s' is not in the correct form of 'scope.identifier.revision' (e.g., 'knb-lter-lno.1.1').",
        		packageId);
        throw new UserErrorException(message);
      }
    } else if (url != null && !(url.isEmpty())) {
      tokens = url.split("/");

      if (tokens.length >= 3) {
        isValidPackageId = true;
        scope = tokens[tokens.length - 3];
        identifier = Integer.valueOf(tokens[tokens.length - 2]);
        revision = tokens[tokens.length - 1];
        packageId = scope + "." + identifier.toString() + "." + revision;
      }

    } else {
      message = "A packageId or metadata URL was not found.";
      throw new ResourceNotFoundException(message);
    }

    if (isValidPackageId) {

        String dataPackageDOI = null;
        String xml = null;
        DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
        xml = dpmClient.readMetadata(scope, identifier, revision);

        if (contentType.equals("application/xml")) {
          metadataStr = xml;
          response.setContentType("application/xml");
          response.setCharacterEncoding("UTF-8");
          PrintWriter out = response.getWriter();
          out.print(metadataStr);
          out.flush();
          out.close();      
        } 
        else {
          EmlUtility emlUtility = new EmlUtility(xml);
          HashMap<String, String> parameterMap = new HashMap<String, String>();
          String pastUriHead = dpmClient.getPastaUriHead();
          String resourceId = packageIdToResourceId(pastUriHead, packageId);
          // Pass the resourceId as a parameter to the XSLT
          if (resourceId != null && !resourceId.equals("")) {
            parameterMap.put("resourceId", resourceId);
          }
          // Pass the data package DOI as a parameter to the XSLT
          try {
            dataPackageDOI = dpmClient.readDataPackageDoi(scope, identifier, revision);
            if (dataPackageDOI != null && !dataPackageDOI.equals("")) {
              parameterMap.put("dataPackageDOI", dataPackageDOI);
            }
          }
          catch (Exception e) {
            // No DOI was read. Just continue on.
          }
          metadataStr = emlUtility.xmlToHtmlSaxon(cwd + xslpath, parameterMap);
          
          request.setAttribute("metadataHtml", metadataStr);
          request.setAttribute("packageId", packageId);
          RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
          requestDispatcher.forward(request, response);
        }
      }

    }
    catch (Exception e) {
  	  handleDataPortalError(logger, e);
    }
    
  }

  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("emlutility.xslpath");
    cwd = options.getString("system.cwd");

  }

}
