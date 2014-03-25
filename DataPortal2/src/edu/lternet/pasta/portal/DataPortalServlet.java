/*
 *
 * $Date: 2012-04-02 11:10:19 -0700 (Mon, 02 Apr 2012) $
 * $Author: dcosta $
 * $Revision: $
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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;


public class DataPortalServlet extends HttpServlet {
  
  /*
   * Class variables
   */
  
  protected static final String LOGIN_WARNING = "You must login before using this tool.";
  private static final long serialVersionUID = 1L;
  
  protected static final String CSS_LINK_ELEMENTS = 
    String.format(
//		  "%s\n%s\n%s\n%s\n",
		  "%s\n",
		  "    <link href=\"css/style_slate.css\" media=\"all\" rel=\"stylesheet\" type=\"text/css\">"
		  //"    <link href=\"bootstrap/css/bootstrap.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\">",
		  //"    <link href=\"bootstrap/css/bootstrap-responsive.css\" media=\"screen\" rel=\"stylesheet\" type=\"text/css\">",
    );
		  
  

  /*
   * Instance variables
   */
  
  
  /*
   * Constructors
   */
  
  
  /*
   * Class methods
   */
  
  /**
   * Accesses the login warning string. Used by JSP pages that do not
   * inherit from DataPortalServlet.
   */
  public static String getLoginWarning() {
    return LOGIN_WARNING;
  }
  
  
  /**
   * Composes the title text appropriate for a given page in the
   * Data Portal web application.
   * 
   * @param pageTitle   A short page title, e.g. "About", "Home".
   *           Best practice is for title length to be
   *           70 characters or less but oftentimes it's not possible
   *           to keep the title that short.
   * @return the text to be inserted inside the <title> element
   *         of a particular Data Portal web page
   */
  public static String getTitleText(String pageTitle) {
	  return String.format(
			  "Data Portal - %s | Long Term Ecological Research Network (LTER)",
			  pageTitle);
  }

  
  /*
   * Instance methods
   */
  
  
  /*
   * Generalized error handler for Data Portal servlets
   */
  protected void handleDataPortalError(Logger logger, Exception e) throws ServletException {
	  String className = e.getClass().getName();
	  String errorMessage = null;
	  String eMessage = e.getMessage();
	  if (eMessage == null) {
		  Throwable t = e.getCause();
		  if (t != null) {
			  eMessage = t.getMessage();
		  }
	  }
	  
	  if (className.equals("javax.servlet.ServletException")) {
	      errorMessage = String.format("%s: %s", this.getClass().getName(), eMessage);
	  }
	  else {
	      errorMessage = String.format("%s: %s", className, eMessage);
	  }
      logger.error(errorMessage);
      e.printStackTrace();
      
      // If user not logged in, add suggestion for user to log in
      if (errorMessage.contains("User public does not have permission")) {
    	  String suggestion = 
    			  String.format(
                      " <a href='./login.jsp'>Logging into the LTER Data Portal</a> <em>may</em> let you read this resource.");
    	  errorMessage = errorMessage + suggestion;
      }
    	  
      throw new ServletException(errorMessage);
  }
  
  
  /*
   * Composes a data package resource identifier based on the PASTA URI
   * head value and a specific packageId value.
   */
  protected String packageIdToResourceId(String pastaUriHead, String packageId) {
    String resourceId = null;
    final String SLASH = "/";
    
    if (pastaUriHead != null) {
      EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
      
      try {
        EmlPackageId emlPackageId = emlPackageIdFormat.parse(packageId);
        String scope = emlPackageId.getScope();
        Integer identifier = emlPackageId.getIdentifier();
        Integer revision = emlPackageId.getRevision();
      
        if (scope != null && identifier != null && revision != null) {        
          resourceId = pastaUriHead + "eml" + SLASH + 
                       scope + SLASH + identifier + SLASH + revision;
        } 
      }
      catch (IllegalArgumentException e) {
        
      }
    }
    
    return resourceId;
  }

  
  /**
   * Destruction of the servlet. <br>
   */
  public void destroy() {
    super.destroy(); // Just puts "destroy" string in log
    // Put your code here
  }

}
