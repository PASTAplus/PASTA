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

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.portal.eml.EmlObject;
import edu.lternet.pasta.portal.eml.Title;
import edu.lternet.pasta.portal.eml.Creator;

public class DataPackageCitationServlet extends DataPortalServlet {

	/**
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(DataPackageCitationServlet.class);
	private static final long serialVersionUID = 1L;

	private static final String forward = "./dataPackageCitation.jsp";
	private static final String PUBLISHER = "Long Term Ecological Research Network. ";

	/**
	 * Instance variables
	 */

	private String pastaUriHead;
	private String uid = null;
	private String citationMessage = null;

	/**
	 * Constructor of the object.
	 */
	public DataPackageCitationServlet() {
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

		this.uid = (String) httpSession.getAttribute("uid");

		if (this.uid == null || this.uid.isEmpty())
			this.uid = "public";

		String html = null;
		Integer id = null;
		boolean isPackageId = false;

		// Accept either packageId or url parameters
		String scope = request.getParameter("scope");
		String identifier = request.getParameter("identifier");
		String revision = request.getParameter("revision");
		String url = request.getParameter("url");

		if (scope != null && !(scope.isEmpty()) && identifier != null
		    && !(identifier.isEmpty()) && revision != null && !(revision.isEmpty())) {

			id = Integer.valueOf(identifier);
			isPackageId = true;

		} else if (url != null && !url.isEmpty()) {

			String[] tokens = url.split("/");

			if (tokens.length >= 3) {
				scope = tokens[tokens.length - 3];
				identifier = tokens[tokens.length - 2];
				id = Integer.valueOf(identifier);
				revision = tokens[tokens.length - 1];
				isPackageId = true;
			}

		} else {
			html = "<p class=\"warning\">Error: a packageId or metadata URL was not found.</p>\n";
		}

		if (isPackageId) {

			html = this.mapFormatter(scope, id, revision);

		} else {
			html = "<p class=\"warning\"> Error: \"scope\" and or \"identifier\" and or \"revision\" field(s) empty</p>\n";
		}

		httpSession.setAttribute("citationmessage", citationMessage);
		httpSession.setAttribute("html", html);
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
			throw new ServletException(
			    "No value defined for 'pasta.uriHead' property.");
		}
	}

	/**
	 * Formats the output for the data package resource map.
	 * 
	 * @param scope
	 *          The data package scope (namespace) value
	 * @param id
	 *          The data package identifier (accession number) value
	 * @param revision
	 *          The data package revision value
	 * 
	 * @return The formatted resource map as HTML
	 */
	private String mapFormatter(String scope, Integer identifier, String revision) {

		String html = null;

		String emlString = null;
		EmlObject emlObject = null;
		ArrayList<Title> titles = null;
		ArrayList<Creator> creators = null;

		String titleText = "";
		String creatorText = "";
		String orgText = "";
		String pubDateText = "";
		String citationId = "";
		String caveat = "";

		DataPackageManagerClient dpmClient = null;

		try {

			dpmClient = new DataPackageManagerClient(this.uid);

			emlString = dpmClient.readMetadata(scope, identifier, revision);
			emlObject = new EmlObject(emlString);

			titles = emlObject.getTitles();

			if (titles != null) {

				for (Title title : titles) {
					if (title.getTitleType().equals(Title.MAIN)) {
						titleText += title.getTite() + ". ";
					}
				}

			}

			creators = emlObject.getCreators();

			if (creators != null) {

				Integer personCount = emlObject.getPersonCount();
				Integer orgCount = emlObject.getOrgCount();
				Integer cnt = 0;

				// Citations should include only person names, if possible
				if (personCount != 0) {

					for (Creator creator : creators) {

						if (creator.getCreatorType().equals(Creator.PERSON)) {
							cnt++;
							if (cnt == personCount) {
								creatorText += creator.getCreatorName() + " ";
							} else {
								creatorText += creator.getCreatorName() + "; ";
							}
						}

					}

				} else if (orgCount != 0) { // otherwise, use organization names

					for (Creator creator : creators) {

						if (creator.getCreatorType().equals(Creator.ORGANIZATION)) {
							cnt++;
							if (cnt == orgCount) {
								creatorText += creator.getCreatorName() + " ";
							} else {
								creatorText += creator.getCreatorName() + "; ";
							}
						}

					}

				}

			}

			creators = emlObject.getCreators();

			if (creators != null) {

				Integer orgCount = emlObject.getOrgCount();
				Integer cnt = 0;

				if (orgCount != 0) {

					for (Creator creator : creators) {

						if (creator.getCreatorType().equals(Creator.ORGANIZATION)) {
							cnt++;
							if (cnt == orgCount) {
								orgText += creator.getCreatorName() + ". ";
							} else {
								orgText += creator.getCreatorName() + "; ";
							}
						}

					}

				}

			}

			try {
				citationId = dpmClient.readDataPackageDoi(scope, identifier, revision);
			} catch (Exception e) {
				logger.error(e.getMessage());
				e.printStackTrace();
				citationId = dpmClient.getPastaPackageUri(scope, identifier, revision);
				caveat = "<p>Note: DOIs are generated hourly for all data packages"
				    + " that are \"publicly\" accessible.</p>";
			}

			String pubDate = emlObject.getPubDate();

			if (pubDate != null) {
				pubDateText += "(" + pubDate + "): ";
			}

		} catch (PastaAuthenticationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			html = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
			return html;
		} catch (PastaConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			html = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
			return html;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			html = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
			return html;
		}

		html = "<p id=\"cite\">" + creatorText + pubDateText + titleText + orgText + PUBLISHER
		    + citationId + "</p>" + caveat;
		
		return html;

	}

}
