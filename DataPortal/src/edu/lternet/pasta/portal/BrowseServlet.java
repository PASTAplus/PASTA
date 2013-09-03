/*
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 June 2012) $
 * $Author: dcosta $
 * $Revision: 2145 $
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

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.ResultSetUtility;
import edu.lternet.pasta.portal.search.BrowseGroup;
import edu.lternet.pasta.portal.search.BrowseTerm;
import edu.lternet.pasta.portal.search.BrowseSearch;
import edu.lternet.pasta.portal.search.TermsList;


public class BrowseServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger = Logger.getLogger(edu.lternet.pasta.portal.BrowseServlet.class);
  private static final long serialVersionUID = 1L;

  private static String cwd = null;
  private static String xslpath = null;
  
  
  /*
   * Instance variables
   */
  

  /*
   * Constructors
   */
  
  /**
   * Constructor of the object.
   */
  public BrowseServlet() {
    super();
  }
  

  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */

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
    String forward = "./browseResult.jsp";
    String html = null;
    TermsList termsList = null;
    String termsListHTML= "";
    String xml = null;

    HttpSession httpSession = request.getSession();
    
    String uid = (String) httpSession.getAttribute("uid");   
    if (uid == null || uid.isEmpty()) { uid = "public"; }
    
    String searchValue = request.getParameter("searchValue");      
    BrowseTerm browseTerm = new BrowseTerm(searchValue);
    
    // Tell the web server that the response is HTML
    response.setContentType("text/html");
    
    try {
      xml = browseTerm.readSearchResults();
      
      termsList = browseTerm.getTermsList();
      if (termsList != null) {
        termsListHTML = termsList.toHTML();
      }

      ResultSetUtility resultSetUtility = new ResultSetUtility(xml);  
      html = "<p> Terms used in this search: " + termsListHTML + "</p>\n";     
      html += resultSetUtility.xmlToHtmlTable(cwd + xslpath);
      request.setAttribute("searchresult", html);
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
    ServletContext servletContext = getServletContext();
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("resultsetutility.xslpath");
    cwd = options.getString("system.cwd");
    String browseDirPath = options.getString("browse.dir");
    BrowseSearch.setBrowseCacheDir(browseDirPath);
    BrowseSearch browseSearch = null;
    BrowseGroup browseGroup = null;

    File browseKeywordFile = new File(BrowseSearch.browseKeywordPath);
    if (browseKeywordFile.exists()) {
      browseSearch = new BrowseSearch();
      browseGroup = browseSearch.readBrowseCache(browseKeywordFile);
      
      /* Lock the servlet context object to guarantee that only one thread at a
       * time can be getting or setting the context attribute. 
       */
      synchronized(servletContext) {
        servletContext.setAttribute("browseKeywordHTML", browseGroup.toHTML());
      }
    }
    else {
      logger.warn("Missing browse keyword file at location: " + BrowseSearch.browseKeywordPath);
    }
  }
  
}
