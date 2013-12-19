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
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.ReportUtility;
import edu.lternet.pasta.common.UserErrorException;

public class UploadEvaluateServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.UploadEvaluateServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslpath = null;

  private static final String HTMLHEAD = "<html lang=\"en\">\n" +
	      "<head>\n" +
	      "    <title>LTER :: Network Data Portal</title>\n" +
	      CSS_LINK_ELEMENTS +
	      "</head>\n\n" + 
	      "<body>\n" +
	      "    <div class=\"body\">\n";

  private static final String HTMLTAIL = "    </div>\n</body>\n</html>\n";

  /**
   * Constructor of the object.
   */
  public UploadEvaluateServlet() {
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

    String html = null;

    boolean isMultipart = ServletFileUpload.isMultipartContent(request);

    if (isMultipart) {

      // Create a factory for disk-based file items
      FileItemFactory factory = new DiskFileItemFactory();

      // Create a new file upload handler
      ServletFileUpload upload = new ServletFileUpload(factory);

      // Parse the request
      try {

        List /* FileItem */items = upload.parseRequest(request);

        // Process the uploaded items
        Iterator iter = items.iterator();
        
        while (iter.hasNext()) {
          
          FileItem item = (FileItem) iter.next();

          if (!(item.isFormField())) {
  
            File eml = processUploadedFile(item);

            DataPackageManagerClient dpmClient = new DataPackageManagerClient(
                uid);
            String xml = dpmClient.evaluateDataPackage(eml);

            ReportUtility qrUtility = new ReportUtility(xml);
            String htmlTable = qrUtility.xmlToHtmlTable(cwd + xslpath);
            
            if (htmlTable == null) {
            	String msg = "The uploaded file could not be evaluated.";
            	throw new UserErrorException(msg);
            }
            else {
            html = HTMLHEAD + "<div class=\"qualityreport\">"
                + htmlTable + "</div>" + HTMLTAIL;
            }

          }
          
        }

      } 
      catch (Exception e) {
    	  handleDataPortalError(logger, e);
      }    
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
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("reportutility.xslpath");
    cwd = options.getString("system.cwd");

  }

  /**
   * Process the uploaded file
   * 
   * @param item The multipart form file data.
   * 
   * @return The uploaded file as File object.
   * 
   * @throws Exception
   */
  private File processUploadedFile(FileItem item) throws Exception {

    File eml = null;

    // Process a file upload
    if (!item.isFormField()) {

      // Get object information
      String fieldName = item.getFieldName();
      String fileName = item.getName();
      String contentType = item.getContentType();
      boolean isInMemory = item.isInMemory();
      long sizeInBytes = item.getSize();

      String tmpdir = System.getProperty("java.io.tmpdir");

      logger.debug("FILE: " + tmpdir + "/" + fileName);

      eml = new File(tmpdir + "/" + fileName);

      item.write(eml);

    }

    return eml;
  }

}
