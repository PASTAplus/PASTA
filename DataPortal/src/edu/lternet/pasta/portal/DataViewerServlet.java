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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.Entity;

public class DataViewerServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.DataViewerServlet.class);
  private static final long serialVersionUID = 1L;

  private static final String HTMLHEAD = "<html>\n"
      + "<head><title>Data Viewer</title>\n"
      + "<link rel=\"stylesheet\" type=\"text/css\" href=\"./css/lter-nis.css\">"
      + "</head><body><div class=\"body\">\n";

  private static final String HTMLTAIL = "</div></body></html>\n";

  /**
   * Constructor of the object.
   */
  public DataViewerServlet() {
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

    String packageId = request.getParameter("packageid");
    String entityId = request.getParameter("entityid");

    String scope = null;
    Integer identifier = null;
    String revision = null;

    String xml = null;
    String html = null;
    byte[] byteArray = null;

    String[] tokens = packageId.split("\\.");

    if (tokens.length == 3 && entityId != null && !(entityId.isEmpty())) {

      String fileName = entityId;
      String objectName = null;
      scope = tokens[0];
      identifier = Integer.valueOf(tokens[1]);
      revision = tokens[2];

      try {

        DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
        String entityName = dpmClient.readDataEntityName(scope, identifier, revision, entityId);
        xml = dpmClient.readMetadata(scope, identifier, revision);
        objectName = findObjectName(xml, entityName);
        if (objectName != null) { fileName = objectName; }
        byteArray = dpmClient
            .readDataEntity(scope, identifier, revision, entityId);
        if (byteArray != null) { // Download file as binary stream
          response.setHeader("Content-Disposition", "attachment; filename="
              + fileName);
          String contentType = dpmClient.getContentType();
          response.setContentType(contentType);
          OutputStream out = response.getOutputStream();
          out.write(byteArray);
        }

      } catch (PastaAuthenticationException e) {
        logger.error("PastaAuthenticationException: " + e.getMessage());
        e.printStackTrace();
        html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() + "</p>"
            + HTMLTAIL;
      } catch (PastaConfigurationException e) {
        logger.error("PastaConfigurationException: " + e.getMessage());
        e.printStackTrace();
        html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() + "</p>"
            + HTMLTAIL;
      } catch (Exception e) {
        logger.error("Exception: " + e.getMessage());
        e.printStackTrace();
        
        if (e.getMessage().contains("User public does not have permission")) {
        	html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() +
        			" -- <a href=\"./login.jsp\">logging into the NIS</a> <em>may</em> let you read this data entity." +
        			"</p>" + HTMLTAIL;
        } else {
            html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() + "</p>"
            + HTMLTAIL;
        }
        
      }

    } else {
      html = HTMLHEAD
          + "<p class=\"warning\">\n"
          + "Error: packageId \""
          + packageId
          + "\" "
          + "not in the correct form of \"scope\" . \"identifier\" . \"revision\" (e.g., knb-lter-lno.1.1)</p>\n"
          + HTMLTAIL;
    }
    
    if (html != null) {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.print(html);
      out.flush();
      out.close();
    }

  }
  

  /*
   * Matches the specified 'entityName' value with the entity names
   * found in the EML document string, and returns the corresponding 
   * objectName value for the matching entity, if an objectName was
   * specified for the matching entity.
   * 
   * Returns null if:
   *   (1) The EML document fails to parse, or
   *   (2) No entities match the specified entityName value, or
   *   (3) The matching entity does not specify an objectName in
   *       the EML document.
   */
  private String findObjectName(String xml, String entityName) {
    String objectName = null;
    EMLParser emlParser = new EMLParser();
    
    if (xml != null && entityName != null) {
      try {
        InputStream inputStream = IOUtils.toInputStream(xml, "UTF-8");
        DataPackage dataPackage = emlParser.parseDocument(inputStream);
        
        if (dataPackage != null) {
          objectName = dataPackage.findObjectName(entityName);
        }
      }
      catch (Exception e) {
        logger.error("Error parsing EML metacdata: " + e.getMessage());
      }
    }
    
    return objectName;
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
