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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.ReportUtility;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;

public class ReportViewerServlet extends DataPortalServlet {
  
  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.ReportViewerServlet.class);
  private static final long serialVersionUID = 1L;
  
  private static String cwd = null;
  private static String xslpath = null;
  
  private static final String HTMLHEAD = "<html>\n"
      + "<head><title>Report Viewer</title>\n"
      + "<link rel=\"stylesheet\" type=\"text/css\" href=\"./css/lter-nis.css\">"
      + "</head><body>\n";
  
  private static final String HTMLTAIL = "</body></html>\n";

  /**
   * Constructor of the object.
   */
  public ReportViewerServlet() {
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
   * @param request the request send by the client to the server
   * @param response the response send by the server to the client
   * @throws ServletException if an error occurred
   * @throws IOException if an error occurred
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
   * @param request the request send by the client to the server
   * @param response the response send by the server to the client
   * @throws ServletException if an error occurred
   * @throws IOException if an error occurred
   */
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    
    HttpSession httpSession = request.getSession();

    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty())
      uid = "public";

    String packageId = request.getParameter("packageid");
    String encodedPath = request.getParameter("localPath");

    String scope = null;
    Integer identifier = null;
    String revision = null;
    
    String xml = null;
    String html = null;
    
    String[] tokens = packageId.split("\\.");
    
    if (tokens.length == 3) {
      
      scope = tokens[0];
      identifier = Integer.valueOf(tokens[1]);
      revision = tokens[2];

      try {       
        /*
         * The quality report XML could be read either from a local file
         * or from PASTA via the DataPackageManagerClient.
         */
        if (encodedPath != null && encodedPath.length() > 0) {
          URLCodec urlCodec = new URLCodec();
          String localPath = urlCodec.decode(encodedPath);
          File xmlFile = new File(localPath);
          if (xmlFile != null && xmlFile.exists()) {
            xml = FileUtils.readFileToString(xmlFile);
          }
        }
        else {
          DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
          xml = dpmClient.readDataPackageReport(scope, identifier, revision);
        }

        ReportUtility qrUtility = new ReportUtility(xml);
        html = HTMLHEAD + "<div class=\"qualityreport\">" + qrUtility.xmlToHtmlTable(cwd + xslpath) + "</div>" + HTMLTAIL;

      } catch (PastaAuthenticationException e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() + "</p>" + HTMLTAIL;
      } catch (PastaConfigurationException e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() + "</p>" +  HTMLTAIL;
      } catch (Exception e) {
        logger.error(e.getMessage());
        e.printStackTrace();
        html = HTMLHEAD + "<p class=\"warning\">" + e.getMessage() + "</p>" +  HTMLTAIL;
     }
      
    } else {
      html = HTMLHEAD
          + "<p class=\"warning\">\n"
          + "Error: packageId \"" + packageId + "\" "   
          + "is not in the correct form of \"scope\" . \"identifier\" . \"revision\" (e.g., knb-lter-lno.1.1)</p>\n"
          + HTMLTAIL;
    }

      
    response.setContentType("text/html");
    PrintWriter out = response.getWriter();
    out.print(html);
    out.flush();
    out.close();
    
  }

  /**
   * Initialization of the servlet. <br>
   *
   * @throws ServletException if an error occurs
   */
  public void init() throws ServletException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("reportutility.xslpath");
    cwd = options.getString("system.cwd");


  }

}
