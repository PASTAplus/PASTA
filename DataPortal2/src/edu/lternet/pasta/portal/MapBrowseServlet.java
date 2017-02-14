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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EmlObject;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;
import edu.lternet.pasta.portal.codegeneration.CodeGenerationServlet;
import edu.lternet.pasta.portal.user.SavedData;


/**
 * Class to compose HTML for display in the Data Package Summary page.
 * 
 * @author dcosta
 *
 */
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
	 * Constructor of the object.
	 */
	public MapBrowseServlet() {
		super();
	}
	
	
	/*
	 * Class methods 
	 */
	
	/**
	 * Composes a relative URL to the mapbrowse servlet for the specified
	 * packageId.
	 * 
	 * @param packageId  the packageId value
	 * @return the URL for the specified packageId
	 */
	public static String getRelativeURL(String packageId) {
		String mapBrowseURL = null;
		
		if (packageId != null) {
			EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
			EmlPackageId emlPackageId = emlPackageIdFormat.parse(packageId);
			String scope = emlPackageId.getScope();
			Integer identifier = emlPackageId.getIdentifier();
			Integer revision = emlPackageId.getRevision();
			mapBrowseURL = String.format("./mapbrowse?scope=%s&identifier=%d&revision=%d", 
										scope, identifier, revision);
		}

		return mapBrowseURL;
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
		String titleHTML = "";
		String creatorsHTML = "";
		String abstractHTML = "";
		String publicationDateHTML = "";
		String spatialCoverageHTML = "";
		String googleMapHTML = "";
		String packageIdHTML = "";
		String resourcesHTML = "";
		String citationHTML = "";
		String provenanceHTML = "";
		String codeGenerationHTML = "";
		String digitalObjectIdentifier = "";
		String pastaDataObjectIdentifier = "";
		String savedDataHTML = "";
		boolean hasIntellectualRights = false;
		boolean showSaved = false;
		boolean isSaved = false;

		String uid = (String) httpSession.getAttribute("uid");

		if (uid == null || uid.isEmpty()) {
			uid = "public";
		}
		else {
			showSaved = true;
		}

		Integer id = null;
		boolean isPackageId = false;

		// Accept packageId by parts or whole
		String scope = request.getParameter("scope");
		String identifier = request.getParameter("identifier");
		String revision = request.getParameter("revision");
		String packageid = request.getParameter("packageid");

		try {
			if (scope != null && !(scope.isEmpty()) && identifier != null
					&& !(identifier.isEmpty())) {

				if (revision == null || revision.isEmpty()) {
					revision = "newest";
				}

				id = Integer.valueOf(identifier);
				isPackageId = true;
			}
			else
				if (packageid != null && !packageid.isEmpty()) {

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
				StringBuilder titleHTMLBuilder = new StringBuilder();
				StringBuilder creatorsHTMLBuilder = new StringBuilder();
				StringBuilder publicationDateHTMLBuilder = new StringBuilder();
				StringBuilder spatialCoverageHTMLBuilder = new StringBuilder();
				StringBuilder googleMapHTMLBuilder = new StringBuilder();
				StringBuilder packageIdHTMLBuilder = new StringBuilder();
				StringBuilder resourcesHTMLBuilder = new StringBuilder();
				StringBuilder citationHTMLBuilder = new StringBuilder();
				StringBuilder provenanceHTMLBuilder = new StringBuilder();
				StringBuilder codeGenerationHTMLBuilder = new StringBuilder();
				StringBuilder savedDataHTMLBuilder = new StringBuilder();

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

					String revisionList = dpmClient.listDataPackageRevisions(
							scope, id, null);
					revUtil = new RevisionUtility(revisionList);
					size = revUtil.getSize();

					if (revision.equals("newest"))
						revision = revUtil.getNewest().toString();

					packageId = scope + "." + id.toString() + "." + revision;
					predecessor = revUtil.getPredecessor(Integer
							.valueOf(revision));
					successor = revUtil.getSuccessor(Integer.valueOf(revision));

					emlString = dpmClient.readMetadata(scope, id, revision);
					emlObject = new EmlObject(emlString);
					titles = emlObject.getTitles();
					
					if (showSaved) {
						SavedData savedData = new SavedData(uid);
						Integer identifierInt = new Integer(identifier);
						isSaved = savedData.hasDocid(scope, identifierInt);
					}

					if (showSaved) {
						String operation = isSaved ? "unsave" : "save";
						String display = isSaved ? "Remove from your data shelf" : "Add to your data shelf";
						String imgName = isSaved ? "minus_blue_small.png" : "plus_blue_small.png";
						
						savedDataHTMLBuilder.append("<form style=\"display:inline-block\" id=\"savedData\" class=\"form-no-margin\" name=\"savedDataForm\" method=\"post\" action=\"./savedDataServlet\" >\n");
						savedDataHTMLBuilder.append("  <input type=\"hidden\" name=\"operation\" value=\""+ operation + "\" >\n");
						savedDataHTMLBuilder.append("  <input type=\"hidden\" name=\"packageId\" value=\""+ packageId + "\" >\n");
						savedDataHTMLBuilder.append("  <input type=\"hidden\" name=\"forward\" value=\"\" >\n");
						savedDataHTMLBuilder.append("  <sup><input type=\"image\" name=\"submit\" src=\"images/" + imgName +  "\" alt=\"" + display + "\" title=\"" + display + "\"></sup>");	
						savedDataHTMLBuilder.append("</form>\n");		
						savedDataHTML = savedDataHTMLBuilder.toString();
					}

					if (titles != null) {
						titleHTMLBuilder
								.append("<ul class=\"no-list-style\">\n");

						for (Title title : titles) {
							String listItem = "<li>" + title.getTitle() + "</li>\n";
							titleHTMLBuilder.append(listItem);
						}

						titleHTMLBuilder.append("</ul>\n");
						titleHTML = titleHTMLBuilder.toString();
					}

					creators = emlObject.getCreators();

					if (creators != null) {

						creatorsHTMLBuilder
								.append("<ul class=\"no-list-style\">\n");

						for (ResponsibleParty creator : creators) {
							creatorsHTMLBuilder.append("<li>");

							String individualName = creator.getIndividualName();
							String positionName = creator.getPositionName();
							String organizationName = creator
									.getOrganizationName();

							if (individualName != null) {
								creatorsHTMLBuilder.append(individualName);
							}

							if (positionName != null) {
								if (individualName != null) {
									creatorsHTMLBuilder.append("; "
											+ positionName);
								}
								else {
									creatorsHTMLBuilder.append(positionName);
								}
							}

							if (organizationName != null) {
								if (positionName != null
										|| individualName != null) {
									creatorsHTMLBuilder.append("; "
											+ organizationName);
								}
								else {
									creatorsHTMLBuilder
											.append(organizationName);
								}
							}

							creatorsHTMLBuilder.append("</li>\n");
						}

						creatorsHTMLBuilder.append("</ul>\n");
						creatorsHTML = creatorsHTMLBuilder.toString();
					}
					
					String abstractText = emlObject.getAbstractText();

					if (abstractText != null) {
						abstractHTML = toSingleLine(abstractText);
					}

					String pubDate = emlObject.getPubDate();

					if (pubDate != null) {
						publicationDateHTMLBuilder
								.append("<ul class=\"no-list-style\">\n");
						publicationDateHTMLBuilder.append("<li>" + pubDate
								+ "</li>");
						publicationDateHTMLBuilder.append("</ul>");
						publicationDateHTML = publicationDateHTMLBuilder
								.toString();
					}

					map = dpmClient.readDataPackage(scope, id, revision);
					
					String jsonCoordinates = emlObject.jsonSerializeCoordinates();
					String stringCoordinates = emlObject.stringSerializeCoordinates();
					
					request.setAttribute("jsonCoordinates", jsonCoordinates);
					if (stringCoordinates != null && !stringCoordinates.equals("")) {

						String[] coordinatesArray = stringCoordinates.split(":");

						/*
						 * If there are two or fewer sets of coordinates, then initially
						 * show them expanded, otherwise show them collapsed (to save
						 * screen space.)
						 */
						request.setAttribute("expandCoordinates", new Boolean((coordinatesArray.length <= 2)));

						// Only use the expander widget if there's more than one set of coordinates
						boolean useExpander = (coordinatesArray.length > 1) ? true : false;
						
						if (useExpander) {
							spatialCoverageHTMLBuilder.append("<div id='jqxWidget'>\n");
							spatialCoverageHTMLBuilder.append("    <div id='jqxExpander'>\n");
							spatialCoverageHTMLBuilder.append("        <div>Geographic Coordinates</div>\n");
							spatialCoverageHTMLBuilder.append("        <div>\n");
							spatialCoverageHTMLBuilder.append("            <ul class=\"no-list-style\">\n");
							boolean firstCoordinates = true;
							
							for (String coordinates : coordinatesArray) {
								String[] nsew = coordinates.split(",");
								Double northCoord = new Double(nsew[0]);
								Double southCoord = new Double(nsew[1]);
								Double eastCoord = new Double(nsew[2]);
								Double westCoord = new Double(nsew[3]);
								if (firstCoordinates) {
									request.setAttribute("northCoord", northCoord);
									request.setAttribute("southCoord", southCoord);
									request.setAttribute("eastCoord", eastCoord);
									request.setAttribute("westCoord", westCoord);
								}
								firstCoordinates = false;
								String spatial = String.format("N: %s,  S: %s,  E: %s,  W: %s",
								             northCoord, southCoord, eastCoord, westCoord);
								spatialCoverageHTMLBuilder.append(
										String.format("  <li>%s</li>\n", spatial));	
							}
							
							spatialCoverageHTMLBuilder.append("            </ul>\n");
							spatialCoverageHTMLBuilder.append("        </div>\n");
							spatialCoverageHTMLBuilder.append("    </div>\n");
							spatialCoverageHTMLBuilder.append("</div>\n");
						}
						else {
							String[] nsew = coordinatesArray[0].split(",");						
							Double northCoord = new Double(nsew[0]);
							Double southCoord = new Double(nsew[1]);
							Double eastCoord = new Double(nsew[2]);
							Double westCoord = new Double(nsew[3]);
							request.setAttribute("northCoord", northCoord);
							request.setAttribute("southCoord", southCoord);
							request.setAttribute("eastCoord", eastCoord);
							request.setAttribute("westCoord", westCoord);
							final String spacer = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
							spatialCoverageHTMLBuilder.append("<div>\n");
							String spatial = String.format("N: %s%sS: %s%sE: %s%sW: %s",
									          northCoord, spacer, southCoord, spacer, eastCoord, spacer, westCoord);
							spatialCoverageHTMLBuilder.append(String.format("%s\n", spatial));
							spatialCoverageHTMLBuilder.append("</div>\n");
						}
						
						spatialCoverageHTML = spatialCoverageHTMLBuilder.toString();

						googleMapHTMLBuilder.append("<ul class=\"no-list-style\">\n");
						googleMapHTMLBuilder.append("  <li><div id='map-canvas-summary'></div></li>");						
						googleMapHTMLBuilder.append("</ul>\n");
						googleMapHTML = googleMapHTMLBuilder.toString();
					}

				}
				catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
					throw (e);
				}

				tokens = new StrTokenizer(map);

				URLCodec urlCodec = new URLCodec();

				String packageIdListItem = null;
				String metadata = null;
				String report = null;
				String data = "";
				String doiId = null;
				String entityNames = dpmClient.readDataEntityNames(scope, id, revision);
				String entitySizes = dpmClient.readDataEntitySizes(scope, id, revision);

				while (tokens.hasNext()) {
					resource = tokens.nextToken();

					if (resource.contains(metadataUri)) {

						metadata = "<li><a class=\"searchsubcat\" href=\"./metadataviewer?packageid="
								+ packageId + "\">Metadata</a></li>\n";

					}
					else
						if (resource.contains(reportUri)) {

							report = "<li><a class=\"searchsubcat\" href=\"./reportviewer?packageid="
									+ packageId
									+ "\" target=\"_blank\">Report</a></li>\n";

						}
						else
							if (resource.contains(dataUri)) {
								uriTokens = resource.split("/");
								entityId = uriTokens[uriTokens.length - 1];
								String entityName = null;
								String entitySize = null;
								String entitySizeStr = "";

								entityName = findEntityName(entityNames, entityId);
								entitySize = findEntitySize(entitySizes, entityId);
								
								if (entitySize != null) {
									entitySizeStr = String.format("&nbsp;&nbsp;<small><em>(%s bytes)</em></small>", entitySize);
								}

								// Safe URL encoding of entity id
								try {
									entityId = urlCodec.encode(entityId);
								}
								catch (EncoderException e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}
								
								/*
								 * Entity name will only be returned for authorized data
								 * entities, so if it's non-null then we know the user is authorized.
								 */
								Boolean isAuthorized = false;

								if (entityName != null) {
									isAuthorized = true;
								}

								if (isAuthorized) {
									data += "<li><a class=\"searchsubcat\" href=\"./dataviewer?packageid="
											+ packageId
											+ "&entityid="
											+ entityId
											+ "\" target=\"_blank\">"
											+ entityName 
											+ "</a>" 
											+ entitySizeStr
											+ "</li>\n";
								}
								else {
									entityName = "Data object";
									String tooltip = null;
									if (uid.equals("public")) {
										tooltip = "You may need to log in before you can access this data object.";
									}
									else {
										tooltip = "You may not have permission to access this data object.";
									}
									data += String.format(
											  "<li>%s [<span name='%s' class='tooltip'><em>more info</em></span>]</li>\n", 
											  entityName, tooltip);
								}
							}
							else {

								try {
									doiId = dpmClient.readDataPackageDoi(scope,
											id, revision);
								}
								catch (Exception e) {
									logger.error(e.getMessage());
									e.printStackTrace();
								}

								pastaDataObjectIdentifier = dpmClient
										.getPastaPackageUri(scope, id, revision);

								packageIdListItem = 
										"<li>" + packageId  + "&nbsp;&nbsp;" + savedDataHTML + "</li>\n";

								if (predecessor != null) {
									previous = "<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope="
											+ scope
											+ "&identifier="
											+ identifier.toString()
											+ "&revision="
											+ predecessor.toString()
											+ "\">previous revision</a></li>\n";
								}

								if (successor != null) {
									next = "<li><a class=\"searchsubcat\" href=\"./mapbrowse?scope="
											+ scope
											+ "&identifier="
											+ identifier.toString()
											+ "&revision="
											+ successor.toString()
											+ "\">next revision</a></li>\n";
								}

								if (size > 1) {
									revisions = "<li><a class=\"searchsubcat\" href=\"./revisionbrowse?scope="
											+ scope
											+ "&identifier="
											+ identifier.toString()
											+ "\">all revisions</a></li>\n";
								}

							}

				}

				packageIdHTMLBuilder.append("<ul class=\"no-list-style\">\n");
				packageIdHTMLBuilder.append(packageIdListItem);
				packageIdHTMLBuilder.append(previous);
				packageIdHTMLBuilder.append(next);
				packageIdHTMLBuilder.append(revisions);
				packageIdHTMLBuilder.append("</ul>\n");
				packageIdHTML = packageIdHTMLBuilder.toString();

				resourcesHTMLBuilder.append("<ul class=\"no-list-style\">\n");
				resourcesHTMLBuilder.append(metadata);
				resourcesHTMLBuilder.append(report);
				resourcesHTMLBuilder
						.append("<li>Data <sup><strong>*</strong></sup>\n");
				resourcesHTMLBuilder.append("<ol>\n");
				resourcesHTMLBuilder.append(data);
				resourcesHTMLBuilder.append("</ol>\n");
				resourcesHTMLBuilder.append("</li>\n");

				resourcesHTMLBuilder.append("<li>&nbsp;</li>\n");

				resourcesHTMLBuilder.append("<li>\n");
				resourcesHTMLBuilder.append("<div>\n");				
				resourcesHTMLBuilder.append("<form id=\"archive\" name=\"archiveform\" method=\"post\" action=\"./archiveDownload\"	target=\"_top\">\n");
				resourcesHTMLBuilder.append("  <input type=\"hidden\" name=\"packageid\" value=\"" + packageId + "\" >\n");
				resourcesHTMLBuilder.append("  <input class=\"btn btn-info btn-default\" type=\"submit\" name=\"archive\" value=\"Download Zip Archive\" >\n");
				resourcesHTMLBuilder.append("</form>\n");
				resourcesHTMLBuilder.append("</div>\n");
				resourcesHTMLBuilder.append("</li>\n");

				hasIntellectualRights = emlObject.hasIntellectualRights();
				if (hasIntellectualRights) {
				resourcesHTMLBuilder.append("<li>\n");
				resourcesHTMLBuilder
						.append("<sup><strong>*</strong></sup> <em>By downloading any data you implicitly acknowledge the "
								+ "<a class=\"searchsubcat\" href=\"./metadataviewer?packageid="
								+ packageId + "#toggleDataSetUsageRights\">Data Package Usage Rights</a> detailed in the accompanying metadata.</em>");
				resourcesHTMLBuilder.append("</li>\n");

				resourcesHTMLBuilder.append("</ul>\n");
				}
				
				resourcesHTML = resourcesHTMLBuilder.toString();


				if (doiId != null) {
					digitalObjectIdentifier = doiId;
				}

				citationHTMLBuilder
						.append("<a class=\"searchsubcat\" href=\"./dataPackageCitation?scope="
								+ scope
								+ "&"
								+ "identifier="
								+ identifier.toString()
								+ "&"
								+ "revision="
								+ revision
								+ "\">How to cite this data package</a>\n");
				citationHTML = citationHTMLBuilder.toString();

				String dataSourcesStr = dpmClient.listDataSources(scope, id, revision);
				
				String source = null;
				String derived = null;
				
				if (dataSourcesStr != null &&
                    dataSourcesStr.length() > 0) {
					derived = packageId;
					String[] dataSources = dataSourcesStr.split("\n");
					if (dataSources.length > 0) {
						String dataSource = dataSources[0];
						if (dataSource != null && dataSource.length() > 0) {
							provenanceHTMLBuilder.append("This data package is derived from the following sources:<br/>");
							provenanceHTMLBuilder.append("<ol>\n");
							for (String uri : dataSources) {
								String mapbrowseURL = mapbrowseURL(uri);
								if (source == null) { source = packageIdFromPastaId(uri); }
								String listItem = String.format("<li>%s</li>", mapbrowseURL);
								provenanceHTMLBuilder.append(listItem);
							}
							provenanceHTMLBuilder.append("</ol>\n");
							provenanceHTMLBuilder.append("<br/>");
						}
					}
				}
				
				String dataDescendantsStr = dpmClient.listDataDescendants(scope, id, revision);
				
				if (dataDescendantsStr != null &&
						dataDescendantsStr.length() > 0) {
					source = packageId;
					String[] dataDescendants = dataDescendantsStr.split("\n");
					if (dataDescendants.length > 0) {
						String dataDescendant = dataDescendants[0];
						if (dataDescendant != null && dataDescendant.length() > 0) {
							provenanceHTMLBuilder.append("This data package is a source for the following derived data packages:<br/>");
							provenanceHTMLBuilder.append("<ol>\n");
							for (String uri : dataDescendants) {
								String mapbrowseURL = mapbrowseURL(uri);
								if (derived == null) { derived = packageIdFromPastaId(uri); }
								String listItem = String.format("<li>%s</li>", mapbrowseURL);
								provenanceHTMLBuilder.append(listItem);
							}
							provenanceHTMLBuilder.append("</ol>\n");
							provenanceHTMLBuilder.append("<br/>");
						}
					}
				}
				
				/*
				 * Provenance graph
				 */
				if ((source != null) && (derived != null)) {				
					String graphString = 
						String.format("View a <a class=\"searchsubcat\" href=\"./provenanceGraph?source=%s&derived=%s\">"
									+ "provenance graph</a> of this data package",
									source, derived);
					provenanceHTMLBuilder.append(graphString);
					provenanceHTMLBuilder.append("<br/><br/>");
				}
				
				/*
				 * Provenance metadata generator
				 */
				provenanceHTMLBuilder.append(
						String.format(
				"Generate <a class=\"searchsubcat\" href=\"./provenanceGenerator?packageid=%s\">" +
				"provenance metadata</a> for use within your derived data package", 
				packageId));
					
				provenanceHTML = provenanceHTMLBuilder.toString();				

				/*
				 * Add code generation section only if this data package has at
				 * least one entity that is a data table.
				 */
				DataPackage dataPackage = emlObject.getDataPackage();
				boolean hasDataTableEntity = dataPackage.hasDataTableEntity();
				if (hasDataTableEntity) {
					ArrayList<String> programLinks = CodeGenerationServlet
							.getProgramLinks(packageId);
					codeGenerationHTMLBuilder
							.append("Analyze this data package using ");
					for (String programLink : programLinks) {
						codeGenerationHTMLBuilder.append(String.format("%s, ",
								programLink));
					}
					codeGenerationHTML = codeGenerationHTMLBuilder.toString();
					codeGenerationHTML = codeGenerationHTML.substring(0,
							codeGenerationHTML.length() - 2); // trim the last
																// comma and
																// space
				}

			}

			else {
				String msg = "The 'scope', 'identifier', or 'revision' field of the packageId is empty.";
				throw new UserErrorException(msg);
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}

		request.setAttribute("dataPackageTitleHTML", titleHTML);
		request.setAttribute("dataPackageCreatorsHTML", creatorsHTML);
		request.setAttribute("abstractHTML", abstractHTML);
		request.setAttribute("dataPackagePublicationDateHTML",
				publicationDateHTML);
		request.setAttribute("spatialCoverageHTML", spatialCoverageHTML);
		request.setAttribute("googleMapHTML", googleMapHTML);
		request.setAttribute("dataPackageIdHTML", packageIdHTML);
		request.setAttribute("dataPackageResourcesHTML", resourcesHTML);
		request.setAttribute("dataPackageCitationHTML", citationHTML);
		request.setAttribute("digitalObjectIdentifier", digitalObjectIdentifier);
		request.setAttribute("pastaDataObjectIdentifier",
				pastaDataObjectIdentifier);
		request.setAttribute("provenanceHTML", provenanceHTML);
		request.setAttribute("codeGenerationHTML", codeGenerationHTML);

		RequestDispatcher requestDispatcher = request.getRequestDispatcher(forward);
		requestDispatcher.forward(request, response);
	}
	
	
	private String findEntityName(String entityNames, String entityId) {
		String entityName = null;
		if (entityNames != null && entityId != null) {
			String[] lines = entityNames.split("\n");
			for (String line : lines) {
				if (line.startsWith(entityId)) {
					entityName = line.substring(entityId.length() + 1);
				}
			}
		}
		
		return entityName;
	}
	
	
	private String findEntitySize(String entitySizes, String entityId) {
		String entitySize = null;
		if (entitySizes != null && entityId != null) {
			String[] lines = entitySizes.split("\n");
			for (String line : lines) {
				if (line.startsWith(entityId)) {
					return line.split(",")[1];
				}
			}
		}
		
		return entitySize;
	}
	
	
	/*
	 * Compose a relative URL to the mapbrowse servlet given a metadata resource identifier
	 * as input.
	 * 		Example input:  "https://pasta.lternet.edu/package/metadata/eml/lter-landsat/7/1"
	 * 		Example output: "mapbrowse?scope=lter-landsat&identifier=7&revision=1"
	 */
	private String mapbrowseURL(String uri) {
		String url = null;
		
		if (uri != null) {
			final String patternString = "^.*/package/metadata/eml/(\\S+)/(\\d+)/(\\d+)$";
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(uri);
			if (matcher.matches()) {
				String scope = matcher.group(1);
				String identifier = matcher.group(2);
				String revision = matcher.group(3);
				String displayURL = String.format("%s.%s.%s", scope, identifier, revision);
				String href = String.format("mapbrowse?scope=%s&identifier=%s&revision=%s", scope, identifier, revision);
				url = String.format("<a class=\"searchsubcat\" href=\"%s\">%s</a>", href, displayURL);
			}
		}
		
		return url;
	}
	
	
	/*
	 * Extract the package id value from a metadata resource identifier
	 * as input.
	 * 		Example input:  "https://pasta.lternet.edu/package/metadata/eml/lter-landsat/7/1"
	 * 		Example output: "lter-landsat.7.1"
	 */
	private String packageIdFromPastaId(String uri) {
		String packageId = null;
		
		if (uri != null) {
			final String patternString = "^.*/package/metadata/eml/(\\S+)/(\\d+)/(\\d+)$";
			Pattern pattern = Pattern.compile(patternString);
			Matcher matcher = pattern.matcher(uri);
			if (matcher.matches()) {
				String scope = matcher.group(1);
				String identifier = matcher.group(2);
				String revision = matcher.group(3);
				packageId = String.format("%s.%s.%s", scope, identifier, revision);
			}
		}
		
		return packageId;
	}
	
	
	/*
	 * Converts newline-separated text into a single line, so that we can display
	 * abstract text in a <textarea> HTML element without using an XLST stylesheet. 
	 * Without this conversion, the <textarea> displays the abstract in literal 
	 * layout format.
	 */
	private String toSingleLine(String text) {
		String singleLine = null;
		StringBuilder sb = new StringBuilder();
		
		String[] lines = text.split("\n");
		for (String line : lines) {
			sb.append(String.format("%s ", line.trim()));
		}
		
		singleLine = sb.toString().trim();
		return singleLine;
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

}
