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

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.AuditManagerClient;
import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaClient;
import edu.lternet.pasta.client.ReportUtility;

public class AuditReportServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.AuditReportServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String limit = null;
  private static String xslpath = null;

  /**
   * Constructor of the object.
   */
  public AuditReportServlet() {
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

	try {
    HttpSession httpSession = request.getSession();
    String xml = null;
    StringBuffer filter = new StringBuffer();

    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty())
      uid = "public";

    /*
     * Request and process filter parameters
     */
    
    String serviceMethodParam = (String) request.getParameter("serviceMethod");
    if (serviceMethodParam != null && 
    	!serviceMethodParam.isEmpty() && 
    	!serviceMethodParam.equalsIgnoreCase("all")
       ) {
        filter.append("&serviceMethod=" + serviceMethodParam);
    }

    String beginTime = "00:00:00";
    String endTime   = "00:00:00";
    String time = "";
    
    String beginDate = (String) request.getParameter("beginDate");
    if (beginDate != null && !beginDate.isEmpty()) {
  		time = (String) request.getParameter("beginTime");
  		if (time != null && !time.isEmpty()) beginTime = time;
    	if (filter.length() == 0) {
    		filter.append("fromTime=" + beginDate + "T" + beginTime);
    	} else {
    		filter.append("&fromTime=" + beginDate + "T" + beginTime);
    	}
    }
    
    String endDate = (String) request.getParameter("endDate");
    if (endDate != null && !endDate.isEmpty()) {
  		time = (String) request.getParameter("endTime");
  		if (time != null && !time.isEmpty()) endTime = time;
    	if (filter.length() == 0) {
    		filter.append("toTime=" + endDate + "T" + endTime);
    	} else {
    		filter.append("&toTime=" + endDate + "T" + endTime);
    	}
    }
    
    String debug = (String) request.getParameter("debug");
    if (debug != null && !debug.isEmpty()) {
    	if (filter.length() == 0) {
    		filter.append("category=" + debug);
    	} else {
    		filter.append("&category=" + debug);
    	}
    }
    
    String info = (String) request.getParameter("info");
    if (info != null && !info.isEmpty()) {
    	if (filter.length() == 0) {
    		filter.append("category=" + info);
    	} else {
    		filter.append("&category=" + info);
    	}
    }

    String warn = (String) request.getParameter("warn");
    if (warn != null && !warn.isEmpty()) {
    	if (filter.length() == 0) {
    		filter.append("category=" + warn);
    	} else {
    		filter.append("&category=" + warn);
    	}
    }

    String error = (String) request.getParameter("error");
    if (error != null && !error.isEmpty()) {
    	if (filter.length() == 0) {
        filter.append("category=" + error);    		
    	} else {
        filter.append("&category=" + error);
    	}
    }

    String userIdParam = (String) request.getParameter("userId");
    if (userIdParam != null && !userIdParam.isEmpty()) {
      String userParam = "public";
      if (!userIdParam.equalsIgnoreCase(userParam)) {
        userParam = PastaClient.composeDistinguishedName(userIdParam);
      }
    	if (filter.length() == 0) {
    		filter.append("user=" + userParam);
    	} else {
    		filter.append("&user=" + userParam);
    	}
    }

    String group = (String) request.getParameter("group");
    if (group != null && !group.isEmpty()) {
    	if (filter.length() == 0) {
    		filter.append("group=" + group);
    	} else {
    		filter.append("&group=" + group);
    	}
    }
    
    String code = (String) request.getParameter("code");
    if (code != null && !code.isEmpty() && !code.equalsIgnoreCase("all")) {
    	if (filter.length() == 0) {
    		filter.append("status=" + code);
    	} else {
    		filter.append("&status=" + code);
    	}
    }
     
    if (limit != null && !limit.isEmpty()) {
    	if (filter.length() == 0) {
    		filter.append("limit=" + limit);
    	} else {
    		filter.append("&limit=" + limit);
    	}
    }
    
    String message = null;

    if (uid.equals("public")) {
      message = LOGIN_WARNING;
      forward = "./login.jsp";
    } 
    else {

        logger.info(filter.toString());
        
        AuditManagerClient auditClient = new AuditManagerClient(uid);
        xml = auditClient.reportByFilter(filter.toString());

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
	 *             if an error occurs
	 */
	public void init() throws ServletException {

		PropertiesConfiguration options = ConfigurationListener.getOptions();
		
		// limits the number of audit records returned
		limit = options.getString("auditreport.limit");
		
		xslpath = options.getString("auditreport.xslpath");
		cwd = options.getString("system.cwd");
	}
	
	
	public String serviceMethodsHTML(String uid) throws ServletException {
		String html = "";
		StringBuffer htmlStringBuffer = new StringBuffer(
				String.format("  <option value=\"%s\">%s</option>\n", "all", "All Service Methods"));
		try {
			DataPackageManagerClient dpmc = new DataPackageManagerClient(uid);
			String serviceMethods = dpmc.listServiceMethods();
			if (serviceMethods != null && !serviceMethods.equals("")) {
				String[] serviceMethodsArray = serviceMethods.split("\n");
			    for (int i = 0; i < serviceMethodsArray.length; i++) {
			        String serviceMethod = serviceMethodsArray[i];
			        htmlStringBuffer.append(
			            String.format("  <option value=\"%s\">%s</option>\n", serviceMethod, serviceMethod));
			    }
			      
			    html = htmlStringBuffer.toString();
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}

		return html;
	}

}
