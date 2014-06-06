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
import edu.lternet.pasta.common.UserErrorException;
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
	private static final String forward = "./dataPackageSummary.jsp";

	/**
	 * Instance variables
	 */
	
	private String titleHTML = "";
	private String creatorsHTML = "";
	private String publicationDateHTML = "";
	private String packageIdHTML = "";
	private String resourcesHTML = "";
	private String citationHTML = "";
	private String digitalObjectIdentifier = "";
	private String pastaDataObjectIdentifier = "";


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
		titleHTML = "";
		creatorsHTML = "";
		publicationDateHTML = "";
		packageIdHTML = "";
		resourcesHTML = "";
		citationHTML = "";

		String uid = (String) httpSession.getAttribute("uid");

		if (uid == null || uid.isEmpty()) uid = "public";

		Integer id = null;
		boolean isPackageId = false;

		// Accept packageId by parts or whole
		String scope = request.getParameter("scope");
		String identifier = request.getParameter("identifier");
		String revision = request.getParameter("revision");
		String packageid = request.getParameter("packageid");

		try {
			if (scope != null && 
				!(scope.isEmpty()) && 
				identifier != null && 
				!(identifier.isEmpty())
			   ) {

				if (revision == null || revision.isEmpty()) {
					revision = "newest";
				}

				id = Integer.valueOf(identifier);
				isPackageId = true;
			}
			else
				if (packageid != null && 
				    !packageid.isEmpty()
				   ) {

					String[] tokens = packageid.split("\\.");

					if (tokens.length == 3) {
						scope = tokens[0];
						identifier = tokens[1];
						id = Integer.valueOf(identifier);
						revision = tokens[2];
						isPackageId = true;
					}
				}
				else {
					String msg = "A well-formed packageId was not found.";
					throw new UserErrorException(msg);
				}

			if (isPackageId) {
				this.mapFormatter(uid, scope, id, revision);
			}
			else {
				String msg = "The 'scope', 'identifier', or 'revision' field of the packageId is empty.";
				throw new UserErrorException(msg);
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}

		httpSession.setAttribute("browsemessage", null);
		request.setAttribute("dataPackageTitleHTML", titleHTML);
		request.setAttribute("dataPackageCreatorsHTML", creatorsHTML);
		request.setAttribute("dataPackagePublicationDateHTML", publicationDateHTML);
		request.setAttribute("dataPackageIdHTML", packageIdHTML);
		request.setAttribute("dataPackageResourcesHTML", resourcesHTML);
		request.setAttribute("dataPackageCitationHTML", citationHTML);
		request.setAttribute("digitalObjectIdentifier", digitalObjectIdentifier);
		request.setAttribute("pastaDataObjectIdentifier", pastaDataObjectIdentifier);
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
	 */
	private void mapFormatter(String uid, String scope, Integer identifier, String revision) 
		throws Exception {
		StringBuilder titleHTMLBuilder = new StringBuilder();
		StringBuilder creatorsHTMLBuilder = new StringBuilder();
		StringBuilder publicationDateHTMLBuilder = new StringBuilder();
		StringBuilder packageIdHTMLBuilder = new StringBuilder();
		StringBuilder resourcesHTMLBuilder = new StringBuilder();
		StringBuilder citationHTMLBuilder = new StringBuilder();

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
		Boolean isAuthorized = false;

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
			
			String revisionList = dpmClient.listDataPackageRevisions(scope, identifier, null);
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
				titleHTMLBuilder.append("<ul class=\"no-list-style\">\n");

				for (Title title : titles) {
					String listItem = "<li>" + title.getTitle() + "</li>\n";
					titleHTMLBuilder.append(listItem);
				}

				titleHTMLBuilder.append("</ul>\n");
				this.titleHTML = titleHTMLBuilder.toString();
			}

			creators = emlObject.getCreators();

			if (creators != null) {

				creatorsHTMLBuilder.append("<ul class=\"no-list-style\">\n");

				for (ResponsibleParty creator : creators) {
					creatorsHTMLBuilder.append("<li>");
					
					String individualName = creator.getIndividualName();
					String positionName = creator.getPositionName();
					String organizationName = creator.getOrganizationName();
					
					if (individualName != null) {
						creatorsHTMLBuilder.append(individualName);
					}
					
					if (positionName != null) {
						if (individualName != null) {
							creatorsHTMLBuilder.append("; " + positionName);
						} else {
							creatorsHTMLBuilder.append(positionName);
						}
					}
					
					if (organizationName != null) {
						if (positionName != null || individualName != null) {
							creatorsHTMLBuilder.append("; " + organizationName);
						} else {
							creatorsHTMLBuilder.append(organizationName);
						}
					}
					
					creatorsHTMLBuilder.append("</li>\n");
				}

				creatorsHTMLBuilder.append("</ul>\n");
				this.creatorsHTML = creatorsHTMLBuilder.toString();
			}
			
			String pubDate = emlObject.getPubDate();
			
			if (pubDate != null) {			
				publicationDateHTMLBuilder.append("<ul class=\"no-list-style\">\n");
				publicationDateHTMLBuilder.append("<li>" + pubDate + "</li>");
				publicationDateHTMLBuilder.append("</ul>");				
				this.publicationDateHTML = publicationDateHTMLBuilder.toString();
			}

			map = dpmClient.readDataPackage(scope, identifier, revision);
			
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			throw(e);
		}

		tokens = new StrTokenizer(map);

		URLCodec urlCodec = new URLCodec();
		
		String dataPackage = null;
		String metadata = null;
		String report = null;
		String data = null;
		String doiId = null;

		while (tokens.hasNext()) {
			resource = tokens.nextToken();

			if (resource.contains(metadataUri)) {
				
				metadata = "<li><a class=\"searchsubcat\" href=\"./metadataviewer?packageid=" + packageId
				    + "\">Metadata</a></li>\n";
				
			} else if (resource.contains(reportUri)) {
				
				report = "<li><a class=\"searchsubcat\" href=\"./reportviewer?packageid=" + packageId
				    + "\" target=\"_blank\">Report</a></li>\n";
				
			} else if (resource.contains(dataUri)) {

				try {
	        isAuthorized = dpmClient.isAuthorized(resource);
        } catch (Exception e2) {
	        logger.error(e2.getMessage());
	        isAuthorized = false;  // Fall on side of restriction
        }
				
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

				if (isAuthorized) {
					if (data == null) {
						data = "<li><a class=\"searchsubcat\" href=\"./dataviewer?packageid=" + packageId
						    + "&entityid=" + entityId + "\" target=\"_blank\">"
						    + entityName + "</a></li>\n";
					} else {					
						data += "<li><a class=\"searchsubcat\" href=\"./dataviewer?packageid=" + packageId
						    + "&entityid=" + entityId + "\" target=\"_blank\">"
						    + entityName + "</a></li>\n";
					}
				} else {
					String hover = null;
					if (uid.equals("public")) {
						hover = "If this data entity is not linked, you may need to log in before you can access it.";
					}
					else {
						hover = "If this data entity is not linked, you may not have permission to access it.";
					}
					if (data == null) {
						data = "<li>" + entityName + " [<span name=\"" + hover
						    + "\" class=\"tooltip\">"
						    + "<em>more info</em>" + "</span>]</li>\n";
					} else {
						data += "<li>" + entityName + " [<span name=\"" + hover
						    + "\" class=\"tooltip\">"
						    + "<em>more info</em>" + "</span>]</li>\n";
					}
					
				}

			} else {
				
				try {
					doiId = dpmClient.readDataPackageDoi(scope,
									identifier, revision);
				}
				catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}

				pastaDataObjectIdentifier = dpmClient.getPastaPackageUri(scope, identifier, revision);

				dataPackage = "<li>" + packageId + "</li>\n";
				
				if (predecessor != null) {
					previous = "<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope=" + scope + "&identifier="
							+ identifier.toString() + "&revision=" + predecessor.toString()
							+ "\">previous revision</a></li>\n";
				}
				
				if (successor != null) {
					next = "<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope=" + scope + "&identifier="
							+ identifier.toString() + "&revision=" + successor.toString()
							+ "\">next revision</a></li>\n";
				}
				
				if (size > 1) {
					revisions = "<li><a class=\"searchsubcat\" href=\"./revisionbrowse?scope=" + scope
							+ "&identifier=" + identifier.toString()
							+ "\">all revisions</a></li>\n";
				}
				
			}
				
		}

		packageIdHTMLBuilder.append("<ul class=\"no-list-style\">\n");
		packageIdHTMLBuilder.append(dataPackage);
		//packageIdHTMLBuilder.append("<ul>");
		packageIdHTMLBuilder.append(previous);
		packageIdHTMLBuilder.append(next);
		packageIdHTMLBuilder.append(revisions);
		//packageIdHTMLBuilder.append("</ul>");
		packageIdHTMLBuilder.append("</ul>\n");
		packageIdHTML = packageIdHTMLBuilder.toString();
				
		resourcesHTMLBuilder.append("<ul class=\"no-list-style\">\n");
		resourcesHTMLBuilder.append(metadata);
		resourcesHTMLBuilder.append(report);
		resourcesHTMLBuilder.append("<li>Data *\n");
		resourcesHTMLBuilder.append("<ol>\n");
		resourcesHTMLBuilder.append(data);
		resourcesHTMLBuilder.append("</ol>\n");
		resourcesHTMLBuilder.append("</li>\n");

		resourcesHTMLBuilder.append("<li>\n");
		resourcesHTMLBuilder.append("<div class=\"zip\">");
		resourcesHTMLBuilder.append("<form id=\"archive\" name=\"archiveform\" method=\"post\" action=\"./archiveDownload\"	target=\"_top\">");
		resourcesHTMLBuilder.append("<input type=\"hidden\" name=\"packageid\" value=\"" + packageId + "\" />");
		resourcesHTMLBuilder.append("<input class=\"btn btn-info btn-default\" type=\"submit\" name=\"archive\" value=\"Download Zip Archive\" />");
		resourcesHTMLBuilder.append("</form>");
		resourcesHTMLBuilder.append("</div>");
		resourcesHTMLBuilder.append("</li>\n");
		
		resourcesHTMLBuilder.append("<li>\n");
		resourcesHTMLBuilder.append(
			"<em>* By downloading any data you implicitly acknowledge the " +
			"<a class=\"searchsubcat\" href=\"http://www.lternet.edu/data/netpolicy.html\">" +
			"LTER Data Policy</a></em>");
		resourcesHTMLBuilder.append("</li>\n");
		
		resourcesHTMLBuilder.append("</ul>\n");
		resourcesHTML = resourcesHTMLBuilder.toString();
		
		citationHTMLBuilder.append("<a class=\"searchsubcat\" href=\"./dataPackageCitation?scope=" + scope + "&"
				+ "identifier=" + identifier.toString() + "&"
				+ "revision=" + revision
		    + "\">How to cite this data package</a>\n");
		
		if (doiId != null) {
			digitalObjectIdentifier = doiId;
		}
		
		citationHTML = citationHTMLBuilder.toString();
	}

}
