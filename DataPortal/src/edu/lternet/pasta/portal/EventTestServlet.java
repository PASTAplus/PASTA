/*
 *
 * $Date: 2012-05-02 12:01:55 -0700 (Wed, 02 May 2012) $
 * $Author: dcosta $
 * $Revision: 2104 $
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
import java.text.ParseException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.EventSubscriptionClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.client.PastaEventException;
import edu.lternet.pasta.client.SubscriptionUtility;

public class EventTestServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.EventTestServlet.class);
  private static final long serialVersionUID = 1L;

  private static final String forward = "./eventSubscribe.jsp";

  private static String cwd = null;
  private static String xslpath = null;

  /**
   * Constructor of the object.
   */
  public EventTestServlet() {
    super();
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
    String xml = null;

    String uid = (String) httpSession.getAttribute("uid");

    if (uid == null || uid.isEmpty())
      uid = "public";

    String subscriptionId = request.getParameter("subscriptionid");

    String message = null;
    String type = null;

    if (uid.equals("public")) {

      message = LOGIN_WARNING;
      type = "warning";

    } else {

      try {
        EventSubscriptionClient eventClient = new EventSubscriptionClient(uid);
        eventClient.testSubscription(subscriptionId);
        xml = eventClient.readBySid(subscriptionId);
        SubscriptionUtility subscriptionUtility = new SubscriptionUtility(xml);
        message = "<h3 align='center'>Event subscription with identifier '" + subscriptionId
            + "' has been tested by posting to the Target URL:</h3>";
        message += subscriptionUtility.xmlToHtml(cwd + xslpath);
        type = "info";

      } 
      catch (Exception e) {
    	  String errorMessage = e.getMessage();
    	  if (errorMessage == null) {
    		  Throwable t = e.getCause();
    		  if (t != null) {
    			  errorMessage = t.getMessage();
    		  }
    	  }
          logger.error(errorMessage);
          e.printStackTrace();
          throw new ServletException(errorMessage);
      }

    }

    request.setAttribute("testmessage", message);
    request.setAttribute("type", type);

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
    xslpath = options.getString("subscriptionutility.xslpath");
    cwd = options.getString("system.cwd");

  }

}
