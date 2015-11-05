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

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.ResultSetUtility;
import edu.lternet.pasta.portal.search.Search;
import edu.lternet.pasta.portal.search.SolrAdvancedSearch;
import edu.lternet.pasta.portal.search.TermsList;
import edu.lternet.pasta.portal.user.SavedData;


public class AdvancedSearchServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.AdvancedSearchServlet.class);
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
  public AdvancedSearchServlet() {
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
    
    String forward = "./searchResult.jsp";
    String html = "";
    TermsList termsList = null;
    String termsListHTML= "";
    String xml = null;

    HttpSession httpSession = request.getSession();
    
    String uid = (String) httpSession.getAttribute("uid");
    
    if (uid == null || uid.isEmpty()) uid = "public";
    
    String boundaryContained = request.getParameter("boundaryContained");
    String boundsChangedCount = request.getParameter("boundsChangedCount");
    String dateField = request.getParameter("dateField");
    String startDate = request.getParameter("startDate");
    String endDate = request.getParameter("endDate");
    String datesContained = request.getParameter("datesContained");
    String creatorOrganization = request.getParameter("creatorOrganization");
    String creatorName = request.getParameter("creatorName");
    String locationName = request.getParameter("locationName");
    String namedTimescale = request.getParameter("namedTimescale");
    String[] siteValues = request.getParameterValues("siteValues");
    String subjectField = request.getParameter("subjectField");
    String subjectValue = request.getParameter("subjectValue");
    String specific = request.getParameter("specific");
    String related = request.getParameter("related");
    String relatedSpecific = request.getParameter("relatedSpecific");
    String ecotrends = request.getParameter("ecotrends");
    String landsat5 = request.getParameter("landsat5");
    String taxon = request.getParameter("taxon");
    String identifier = request.getParameter("identifier");
    
    String northBound = request.getParameter("northBound");
    String southBound = request.getParameter("southBound");
    String eastBound = request.getParameter("eastBound");
    String westBound = request.getParameter("westBound");
    
    boolean isBoundaryContainedChecked = (boundaryContained != null);
    boolean isDatesContainedChecked = (datesContained != null);
    boolean isIncludeEcotrendsChecked = (ecotrends != null);
    boolean isIncludeLandsat5Checked = (landsat5 != null);
    boolean isSpecificChecked = (specific != null);
    boolean isRelatedChecked = (related != null);
    boolean isRelatedSpecificChecked = (relatedSpecific != null);
       
    SolrAdvancedSearch solrAdvancedSearch = new SolrAdvancedSearch(
      creatorName,
      creatorOrganization,
      dateField,
      startDate,
      endDate,
      namedTimescale,
      siteValues,
      subjectField,
      subjectValue,
      isIncludeEcotrendsChecked,
      isIncludeLandsat5Checked,
      isDatesContainedChecked,
      isSpecificChecked,
      isRelatedChecked,
      isRelatedSpecificChecked,
      taxon,
      identifier,
      isBoundaryContainedChecked,
      boundsChangedCount,
      northBound,
      southBound,
      eastBound,
      westBound,
      locationName
      );

		try {
			xml = solrAdvancedSearch.executeSearch(uid);
			String queryText = solrAdvancedSearch.getQueryString();
			httpSession.setAttribute("queryText", queryText);

			termsList = solrAdvancedSearch.getTermsList();
			if ((termsList != null) && (termsList.size() > 0)) {
				termsListHTML = termsList.toHTML();
				httpSession.setAttribute("termsListHTML", termsListHTML);
			}

			ResultSetUtility resultSetUtility = null;
			if (uid.equals("public")) {
				resultSetUtility = new ResultSetUtility(xml, Search.DEFAULT_SORT);
			}
			else {
				boolean isSavedDataPage = false;
				SavedData savedData = new SavedData(uid);
				resultSetUtility = new ResultSetUtility(xml, Search.DEFAULT_SORT, savedData, isSavedDataPage);
			}
			
			String mapButtonHTML = resultSetUtility.getMapButtonHTML();
			request.setAttribute("mapButtonHTML", mapButtonHTML);
			//String relevanceHTML = resultSetUtility.getRelevanceHTML();
			//request.setAttribute("relevanceHTML", relevanceHTML);
			html = resultSetUtility.xmlToHtmlTable(cwd + xslpath);
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

    PropertiesConfiguration options = ConfigurationListener.getOptions();
    xslpath = options.getString("resultsetutility.xslpath");
    logger.debug("XSLPATH: " + xslpath);
    cwd = options.getString("system.cwd");
    logger.debug("CWD: " + cwd);
  }
  
}
