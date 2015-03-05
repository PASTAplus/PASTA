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
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.LoginClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.token.TokenManager;

/**
 * The LoginServlet manages user authentication between the NIS Data Portal
 * web-browser interface and the NIS Data Portal/PASTA LoginService.
 * 
 * @author servilla
 * @since Mar 14, 2012
 * 
 */
public class LoginServlet extends DataPortalServlet {

  /**
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.LoginServlet.class);
  private static final long serialVersionUID = 1L;

  /**
   * Constructor of the object.
   */
  public LoginServlet() {
    super();
  }

  /**
   * Destruction of the servlet. <br>
   */
  @Override
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
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    // Pass request onto "doPost".
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
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    HttpSession httpSession = request.getSession();

    String from = (String) httpSession.getAttribute("from");

    String uid = request.getParameter("uid");
    String password = request.getParameter("password");
    String forward = null;

    try {

      new LoginClient(uid, password);
      httpSession.setAttribute("uid", uid);

      if (from == null || from.isEmpty()) {
        forward = "./home.jsp";
      } else {
        forward = from;
        httpSession.removeAttribute("from");
      }
      
    } catch (PastaAuthenticationException e) {
      String message = "<strong><em>Login failed</em></strong> for user <kbd class=\"nis\">" + uid + "</kbd>.";
      forward = "./login.jsp";
      request.setAttribute("message", message);
    }

    try {
        TokenManager tm = new TokenManager();
        logger.info(tm.getCleartextToken(uid));
        logger.info(tm.getUserDistinguishedName(uid));
        logger.info(tm.getTokenAuthenticationSystem(uid));
        logger.info(tm.getTokenTimeToLive(uid));

        ArrayList<String> groups = tm.getUserGroups(uid);

        for (String group : groups) {
            logger.info(group);
        }

        logger.info(tm.getTokenSignature(uid));

        // Let's try to alter the token
        /*
        String token = tm.getToken(uid);
        token = Escalator.addGroup(token, "super");
        tm.setToken(uid, token);

        logger.info(tm.getCleartextToken(uid));
        */

    }
    catch (ClassNotFoundException e) {
        e.printStackTrace();
    }
    catch (java.sql.SQLException e) {
        e.printStackTrace();
    }

      RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
    requestDispatcher.forward(request, response);

  }

  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  @Override
  public void init() throws ServletException {

    PropertiesConfiguration options = ConfigurationListener.getOptions();

  }

}
