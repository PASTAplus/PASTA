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
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;

public class MapBrowseServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.MapBrowseServlet.class);
  private static final long serialVersionUID = 1L;
 
  private static final String forward = "./dataPackageBrowser.jsp";
  private static final String browseMessage = "Select a URL to view the resource:";

  /**
   * Instance variables
   */

  private Integer count = 0;
  private String pastaUriHead;

  /**
   * Constructor of the object.
   */
  public MapBrowseServlet() {
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

    String text = null;
    String html = null;
    String packageId = null;
    Integer id = null;
    boolean isPackageId = false;

    // Accept either packageId or url parameters
    String scope = request.getParameter("scope");
    String identifier = request.getParameter("identifier");
    String revision = request.getParameter("revision");
    String url = request.getParameter("url");

    if (scope != null && !(scope.isEmpty()) &&
        identifier != null && !(identifier.isEmpty()) && revision != null && !(revision.isEmpty())) {

      packageId = scope + "." + identifier + "." + revision;
      id = Integer.valueOf(identifier);
      isPackageId = true;

    } else if (url != null && !url.isEmpty()) {
      
      String[] tokens = url.split("/");

      if (tokens.length >= 3) {
        scope = tokens[tokens.length - 3];
        identifier = tokens[tokens.length - 2];
        id = Integer.valueOf(identifier);
        revision = tokens[tokens.length - 1];
        packageId = scope + "." + identifier + "." + revision;
        isPackageId = true;
      }

    } else {
      html = "<p class=\"warning\">Error: a packageId or metadata URL was not found.</p>\n";
    }
 
      
    if (isPackageId) {

      try {

        DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
        text = dpmClient.readDataPackage(scope, id, revision);

        StrTokenizer tokens = new StrTokenizer(text);

        html = this.mapFormatter(tokens, packageId);

      } catch (PastaAuthenticationException e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        html = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
      } catch (PastaConfigurationException e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        html = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
      } catch (Exception e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        html = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
      }

    } else {
      html = "<p class=\"warning\"> Error: \"scope\" and or \"identifier\" and or \"revision\" field(s) empty</p>\n";
    }

    httpSession.setAttribute("browsemessage", browseMessage);
    httpSession.setAttribute("html", html);
    httpSession.setAttribute("count", this.count.toString());
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
      throw new ServletException("No value defined for 'pasta.uriHead' property.");
    }
  }

  /**
   * Formats the output for the data package resource map.
   * 
   * @param tokens PASTA resource map URIs tokenized as a StrTokenizer object.
   * 
   * @return The formatted resource map as HTML
   */
  private String mapFormatter(StrTokenizer tokens, String packageId) {

    String html = null;

    String metadataUri = pastaUriHead + "metadata/eml";
    String reportUri = pastaUriHead + "report";
    String dataUri = pastaUriHead + "data/eml";

    String[] uriTokens = null;
    String entityName = null;
    String resource = null;

    String metadata = null;
    String report = null;
    String data = null;

    URLCodec urlCodec = new URLCodec();

    while (tokens.hasNext()) {
      resource = tokens.nextToken();

      if (resource.contains(metadataUri)) {
        metadata = "<li><a href=\"./metadataviewer?packageid=" + packageId
            + "\" target=\"_blank\">" + resource + "</a></li>\n";
      } else if (resource.contains(reportUri)) {
        report = "<li><a href=\"./reportviewer?packageid=" + packageId
            + "\" target=\"_blank\">" + resource + "</a></li>\n";
      } else if (resource.contains(dataUri)) {

        uriTokens = resource.split("/");

        // Safe URL encoding of entity name
        try {
          entityName = urlCodec.encode(uriTokens[uriTokens.length - 1]);
        } catch (EncoderException e) {
          logger.error(e.getMessage());
          e.printStackTrace();
        }

        if (data == null) {
          data = "<li><a href=\"./dataviewer?packageid=" + packageId
              + "&entityname=" + entityName + "\" target=\"_blank\">"
              + resource + "</a></li>\n";
        } else {
          data += "<li><a href=\"./dataviewer?packageid=" + packageId
              + "&entityname=" + entityName + "\" target=\"_blank\">"
              + resource + "</a></li>\n";
        }

      }

      this.count++;
      
    }
    

    html = "<h4 align=\"left\">Metadata</h4>\n";
    html += "<ul>\n";
    html += metadata;
    html += "</ul>\n";
    html += "<h4 align=\"left\">Data</h4>\n";
    html += "<ol>\n";
    html += data;
    html += "</ol>\n";
    html += "<h4 align=\"left\">Quality Report</h4>\n";
    html += "<ul>\n";
    html += report;
    html += "</ul>\n";

    this.count += 6; // Two for each header

    return html;

  }

}