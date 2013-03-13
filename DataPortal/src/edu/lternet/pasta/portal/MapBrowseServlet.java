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

import org.apache.commons.lang3.text.StrTokenizer;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.common.eml.EmlObject;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;


public class MapBrowseServlet extends DataPortalServlet {

	/**
	 * Class variables
	 */

	private static final Logger logger = Logger
	    .getLogger(edu.lternet.pasta.portal.MapBrowseServlet.class);
	private static final long serialVersionUID = 1L;
	
	private static String pastaUriHead;
	private static final String forward = "./dataPackageBrowser.jsp";

	/**
	 * Instance variables
	 */


	/**
	 * Constructor of the object.
	 */
	public MapBrowseServlet() {
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

		if (uid == null || uid.isEmpty()) uid = "public";

		String html = null;
		Integer id = null;
		boolean isPackageId = false;

		// Accept packageId by parts or whole
		String scope = request.getParameter("scope");
		String identifier = request.getParameter("identifier");
		String revision = request.getParameter("revision");
		String packageid = request.getParameter("packageid");

		if (scope != null && !(scope.isEmpty()) && identifier != null
		    && !(identifier.isEmpty())) {
			
			if (revision == null || revision.isEmpty()) {
				revision = "newest";
			}

			id = Integer.valueOf(identifier);
			isPackageId = true;

		} else if (packageid != null && !packageid.isEmpty()) {

			String[] tokens = packageid.split("\\.");

			if (tokens.length == 3) {
				scope = tokens[0];
				identifier = tokens[1];
				id = Integer.valueOf(identifier);
				revision = tokens[2];
				isPackageId = true;
			}

		} else {
			html = "<p class=\"warning\">Error: a well-formed packageId was not found.</p>\n";
		}

		if (isPackageId) {

			html = this.mapFormatter(uid, scope, id, revision);

		} else {
			html = "<p class=\"warning\"> Error: \"scope\" and or \"identifier\" and or \"revision\" field(s) empty</p>\n";
		}

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
	private String mapFormatter(String uid, String scope, Integer identifier, String revision) {

		String html = "";

		String packageId = null;

		Integer size = null;
		Integer predecessor = null;
		Integer successor = null;
		String previous = "";
		String next = "";
		String revisions = "";

		String metadataUri = pastaUriHead + "metadata/eml";
		String reportUri = pastaUriHead + "report";
		String dataUri = pastaUriHead + "data/eml";

		String[] uriTokens = null;
		String entityId = null;
		String resource = null;

		String map = null;
		StrTokenizer tokens = null;
		String emlString = null;
		EmlObject emlObject = null;
		ArrayList<Title> titles = null;
		ArrayList<ResponsibleParty> creators = null;

		DataPackageManagerClient dpmClient = null;
		RevisionUtility revUtil = null;

		try {

			dpmClient = new DataPackageManagerClient(uid);
			
			String revisionList = dpmClient.listDataPackageRevisions(scope, identifier);
			revUtil = new RevisionUtility(revisionList);
			size = revUtil.getSize();
			
			if (revision.equals("newest")) revision = revUtil.getNewest().toString();
			
			packageId = scope + "." + identifier.toString() + "." + revision;
			predecessor = revUtil.getPredecessor(Integer.valueOf(revision));
			successor = revUtil.getSuccessor(Integer.valueOf(revision));

			emlString = dpmClient.readMetadata(scope, identifier, revision);
			emlObject = new EmlObject(emlString);

			titles = emlObject.getTitles();

			if (titles != null) {

				html += "<h4 align=\"left\">Title</h4>\n";
				html += "<ul style=\"list-style: none;\">\n";

				for (Title title : titles) {
					html += "<li>" + title.getTite() + "</li>\n";
				}

				html += "</ul>\n";

			}

			creators = emlObject.getCreators();

			if (creators != null) {

				html += "<h4 align=\"left\">Creators</h4>\n";
				html += "<ul style=\"list-style: none;\">\n";

				for (ResponsibleParty creator : creators) {
					html += "<li>";
					
					String individualName = creator.getIndividualName();
					String positionName = creator.getPositionName();
					String organizationName = creator.getOrganizationName();
					
					if (individualName != null) {
						html += individualName;
					}
					
					if (positionName != null) {
						if (individualName != null) {
							html += "; " + positionName;
						} else {
							html += positionName;
						}
					}
					
					if (organizationName != null) {
						if (positionName != null || individualName != null) {
							html += "; " + organizationName;
						} else {
							html += organizationName;
						}
					}
					
					html += "</li>\n";
				}

				html += "</ul>\n";

			}
			
			String pubDate = emlObject.getPubDate();
			
			if (pubDate != null) {
				
				html += "<h4 align=\"left\">Publication Date</h4>\n";
				html += "<ul style=\"list-style: none;\">\n";
				html += "<li>" + pubDate + "</li>";
				html += "</ul>";				

			}

			map = dpmClient.readDataPackage(scope, identifier, revision);
			
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

		tokens = new StrTokenizer(map);

		URLCodec urlCodec = new URLCodec();
		
		String dataPackage = null;
		String metadata = null;
		String report = null;
		String data = null;

		while (tokens.hasNext()) {
			resource = tokens.nextToken();

			if (resource.contains(metadataUri)) {
				
				metadata = "<li><a href=\"./metadataviewer?packageid=" + packageId
				    + "\" target=\"_blank\">Metadata</a></li>\n";
				
			} else if (resource.contains(reportUri)) {
				
				report = "<li><a href=\"./reportviewer?packageid=" + packageId
				    + "\" target=\"_blank\">Report</a></li>\n";
				
			} else if (resource.contains(dataUri)) {

				uriTokens = resource.split("/");

				entityId = uriTokens[uriTokens.length - 1];
				
				String entityName = null;
				
				try {
	        entityName = dpmClient.readDataEntityName(scope, identifier, revision, entityId);
        } catch (Exception e1) {
	        logger.error(e1.getMessage());
	        e1.printStackTrace();
	        entityName = entityId;
        }

				// Safe URL encoding of entity name
				try {
					entityId = urlCodec.encode(entityId);
				} catch (EncoderException e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}

				if (data == null) {
					data = "<li><a href=\"./dataviewer?packageid=" + packageId
					    + "&entityid=" + entityId + "\" target=\"_blank\">"
					    + entityName + "</a></li>\n";
				} else {					
					data += "<li><a href=\"./dataviewer?packageid=" + packageId
					    + "&entityid=" + entityId + "\" target=\"_blank\">"
					    + entityName + "</a></li>\n";
				}

			} else {
				
				dataPackage = "<li>" + packageId + "</li>\n";
				
				if (predecessor != null) {
					previous = "<li><a href=\"./mapbrowse?scope=" + scope + "&identifier="
							+ identifier.toString() + "&revision=" + predecessor.toString()
							+ "\">previous revision</a></li>\n";
				}
				
				if (successor != null) {
					next = "<li><a href=\"./mapbrowse?scope=" + scope + "&identifier="
							+ identifier.toString() + "&revision=" + successor.toString()
							+ "\">next revision</a></li>\n";
				}
				
				if (size > 1) {
					revisions = "<li><a href=\"./revisionbrowse?scope=" + scope
							+ "&identifier=" + identifier.toString()
							+ "\">all revisions</a></li>\n";
				}
				
			}
				
		}

		html += "<h4 align=\"left\">Package Identification</h4>\n";
		html += "<ul style=\"list-style: none;\">\n";
		html += dataPackage;
		html += "<ul>";
		html += previous;
		html += next;
		html += revisions;
		html += "</ul>";
		html += "</ul>\n";
		html += "<h4 align=\"left\">Resources</h4>\n";
		html += "<ul>\n";
		html += metadata;
		html += report;
		html += "<li>Data\n";
		html += "<ol>\n";
		html += data;
		html += "</ol>\n";
		html += "</li>\n";
		html += "</ul>\n";
		
		html += "<p><br/><a href=\"./dataPackageCitation?scope=" + scope + "&"
				+ "identifier=" + identifier.toString() + "&"
				+ "revision=" + revision
		    + "\">How to cite this data package...</a></p>";

		return html;

	}

}
