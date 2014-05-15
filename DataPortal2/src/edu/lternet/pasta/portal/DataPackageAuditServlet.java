/*
 *
 * $Date: 2012-08-30 09:55:43 -0700 (Thu, 30 Aug 2012) $
 * $Author: dcosta $
 * $Revision: 2325 $
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
import edu.lternet.pasta.client.PastaClient;
import edu.lternet.pasta.client.ReportUtility;


public class DataPackageAuditServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.DataPackageAuditServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String limit = null;
  private static String xslpath = null;
  
  private static final String PACKAGE = "readDataPackage";
  private static final String METADATA = "readMetadata";
  private static final String REPORT = "readDataPackageReport";
  private static final String ENTITY = "readDataEntity";

  /**
   * Constructor of the object.
   */
  public DataPackageAuditServlet() {
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

	String forward = "./auditReportTable.jsp";

	AuditManagerClient auditClient = null;
    HttpSession httpSession = request.getSession();
    String xml = null;
    StringBuffer filter = new StringBuffer();
    String message = null;
    String pastaUriHead = null;
    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty()) {
      uid = "public";
    }
    
    try {
      auditClient = new AuditManagerClient(uid);
      pastaUriHead = auditClient.getPastaUriHead();
    
    /*
     * Request and process filter parameters
     */
    
    // Encode empty request parameters with SQL regex string "%" 
    String scope = "%25";
    String identifier = "%25";
    String revision = "%25";
    String resourceId = null;
    
    String value = "";
    
    value = request.getParameter("scope");
    if (value != null && !value.isEmpty()) scope = value;
    
    value = request.getParameter("identifier");
    if (value != null && !value.isEmpty()) identifier = value;

    value = request.getParameter("revision");
    if (value != null && !value.isEmpty()) revision = value;
    
    String packageId = scope + "." + identifier + "." + revision;
        
    String begin = (String) request.getParameter("begin");
    if (begin != null && !begin.isEmpty()) {
      filter.append("fromTime=" + begin + "&");
    }
    
    String end = (String) request.getParameter("end");
    if (end != null && !end.isEmpty()) {
      filter.append("toTime=" + end + "&");
    }
    
    // Filter on "info"
    filter.append("category=info&");
    
    boolean packageResource = false || (request.getParameter("package") != null);
    boolean metadataResource = false || (request.getParameter("metadata") != null);
    boolean dataResource = false || (request.getParameter("entity") != null);
    boolean reportResource = false || (request.getParameter("report") != null);
    boolean includeAllResources = false;
    
    if (!(packageResource || metadataResource || dataResource || reportResource)) {
    	includeAllResources = true;
    }
    
    // Filter on "readDataPackage"
    if (packageResource || includeAllResources) {
    	filter.append("serviceMethod=readDataPackage&");
    	resourceId = getResourceId(pastaUriHead, packageId, PACKAGE);
    	filter.append("resourceId=" + resourceId + "&");
    }

    // Filter on "readMetadata"
    if (metadataResource || includeAllResources) {
    	filter.append("serviceMethod=readMetadata&");
    	resourceId = getResourceId(pastaUriHead, packageId, METADATA);
    	filter.append("resourceId=" + resourceId + "&");
    }
    
    // Filter on "readDataEntity"
    if (dataResource || includeAllResources) {
    	filter.append("serviceMethod=readDataEntity&");
    	resourceId = getResourceId(pastaUriHead, packageId, ENTITY);
    	filter.append("resourceId=" + resourceId + "&");
    }

    // Filter on "readDataPackageReport"
    if (reportResource || includeAllResources) {
    	filter.append("serviceMethod=readDataPackageReport&");
    	resourceId = getResourceId(pastaUriHead, packageId, REPORT);
    	filter.append("resourceId=" + resourceId + "&");
    }

    String userIdParam = (String) request.getParameter("userId");
    if (userIdParam != null && !userIdParam.isEmpty()) {
      String userParam = "public";
      if (!userIdParam.equalsIgnoreCase(userParam)) {
        userParam = PastaClient.composeDistinguishedName(userIdParam);
      }
      filter.append("user=" + userParam + "&");
    }
    
    String groupParam = (String) request.getParameter("group");
    if (groupParam != null && !groupParam.isEmpty()) {
    	filter.append("group=" + groupParam + "&");
    }

    if (limit != null && !limit.isEmpty()) {
    	if (filter.length() == 0) {
    		filter.append("limit=" + limit);
    	} else {
    		filter.append("&limit=" + limit);
    	}
    }
    
    if (uid.equals("public")) {
        message = LOGIN_WARNING;
        forward = "./login.jsp";
    } 
    else if (auditClient != null) {
      	String filterStr = filter.toString();
        xml = auditClient.reportByFilter(filterStr);
        ReportUtility reportUtility = new ReportUtility(xml);
        message = reportUtility.xmlToHtmlTable(cwd + xslpath);
    }

    request.setAttribute("reportMessage", message);

    RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
    requestDispatcher.forward(request, response);
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
	// limits the number of audit records returned
	limit = options.getString("auditreport.limit");
	
    xslpath = options.getString("datapackageaudit.xslpath");
    cwd = options.getString("system.cwd");

  }
  
  private String getResourceId(String pastaUriHead, String packageId, String serviceMethod) {
  	
  	String resourceId = null;
  	
  	String [] packageParts = packageId.split("\\.");
  	
		if (packageParts.length == 3) {
			if (serviceMethod.equals(PACKAGE)) {
				resourceId = pastaUriHead + "eml/" + packageParts[0] + "/" + packageParts[1]
			    + "/" + packageParts[2];
			} else if (serviceMethod.equals(METADATA)) {
				resourceId = pastaUriHead + "metadata/eml/" + packageParts[0] + "/" + packageParts[1]
				    + "/" + packageParts[2];
			} else if (serviceMethod.equals(REPORT)) {
				resourceId = pastaUriHead + "report/eml/" + packageParts[0] + "/" + packageParts[1]
				    + "/" + packageParts[2];
			} else { //ENTITY
				resourceId = pastaUriHead + "data/eml/" + packageParts[0] + "/" + packageParts[1]
				    + "/" + packageParts[2] + "/%25";
			}
		}
  	
  	return resourceId;
  	
  }

}
