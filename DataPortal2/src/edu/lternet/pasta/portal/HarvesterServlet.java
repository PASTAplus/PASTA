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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.common.UserErrorException;
import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.eml.DataPackage;
import edu.lternet.pasta.common.eml.EMLParser;
import edu.lternet.pasta.common.eml.Entity;
import edu.ucsb.nceas.utilities.IOUtil;
import edu.ucsb.nceas.utilities.XMLUtilities;

/**
 * 
 * @author dcosta
 *
 */
@MultipartConfig
public class HarvesterServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static String desktopUrlHead = null;
  private static String harvesterPath = null;
  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.HarvesterServlet.class);
  private static final long serialVersionUID = 1L;
  public static final String DESKTOP_DATA_DIR = "data";
  
  
  /*
   * Instance variables
   */
  
  private final String CHECK_BACK_LATER =
      "This may take a few minutes. Please check the " + 
      "<a href=\"./harvestReport.jsp\">View Evaluate/Upload Results</a> page " +
      "for available reports.";
      
  private final String DESKTOP_FILE_NAME = "DESKTOP-EML.xml";
  private final String PASTA_READY_FILE_NAME = "PASTA-READY-EML.xml";
  

  /*
   * Constructors
   */
  

  /*
   * Class methods
   */
  
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

    // Pass request on to "doPost".
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
		ServletContext servletContext = httpSession.getServletContext();
		ArrayList<String> documentURLs = null;
		File emlFile = null;
		String emlTextArea = null;
		Harvester harvester = null;
		String harvestId = null;
		String harvestListURL = null;
		String harvestReportId = null;
		boolean isDesktopUpload = false;
		boolean isEvaluate = false;
		String uid = (String) httpSession.getAttribute("uid");
		String urlTextArea = null;
		String warningMessage = "";
		boolean useChecksum = false;

		try {
			if (uid == null) {
				throw new PastaAuthenticationException(LOGIN_WARNING);
			}
			else {
				/*
				 * The "metadataSource" request parameter can have a value of
				 * "emlText", "emlFile", "urlList", "harvestList", or
				 * "desktopHarvester". It is set as a hidden input field in 
				 * each of the harvester forms.
				 */
				String metadataSource = request.getParameter("metadataSource");

				/*
				 * "mode" can have a value of "evaluate" or "upgrade". It is set
				 * as the value of the submit button in each of the harvester
				 * forms.
				 */
				String mode = request.getParameter("submit");
				if ((mode != null) && 
					(mode.equalsIgnoreCase("evaluate"))
				   ) {
					isEvaluate = true;
				}

				if ((metadataSource != null) && 
					(!metadataSource.equals("desktopHarvester"))
				   ) {
					harvestId = generateHarvestId();
					if (isEvaluate) {
						harvestReportId = uid + "-evaluate-" + harvestId;
					}
					else {
						harvestReportId = uid + "-upload-" + harvestId;
					}
				}
				
				if (metadataSource != null) {
					if (metadataSource.equals("emlText")) {
						emlTextArea = request.getParameter("emlTextArea");
						if (emlTextArea == null || emlTextArea.trim().isEmpty()) {
							warningMessage = "<p class=\"warning\">Please enter the text of an EML document into the text area.</p>";
						}
					}
					else if (metadataSource.equals("emlFile")) {
						Collection<Part> parts = request.getParts();
						for (Part part : parts) {
							if (part.getContentType() != null) {
								// save EML file to disk
								emlFile = processUploadedFile(part);
							}
							else {
								/*
								 * Parse the request parameters.
								 */
								String fieldName = part.getName();
								String fieldValue = request
											.getParameter(fieldName);
								if (fieldName != null && fieldValue != null) {
									if (fieldName.equals("submit") && 
										fieldValue.equalsIgnoreCase("evaluate")
									   ) {
										isEvaluate = true;
									}
									else if (fieldName.equals("desktopUpload") && 
											 fieldValue.equalsIgnoreCase("desktopUpload")
											) {
										isDesktopUpload = true;
									}
									else if (fieldName.equals("useChecksum") && 
											 fieldValue.equalsIgnoreCase("useChecksum")
											) {
										useChecksum = true;
									}
								}
							}
						}
					}
					else if (metadataSource.equals("urlList")) {
						urlTextArea = request.getParameter("urlTextArea");
						String useChecksumParam = request.getParameter("useChecksum");
						useChecksum = (useChecksumParam != null);
						if (urlTextArea == null || 
							urlTextArea.trim().isEmpty()
						   ) {
							warningMessage = "<p class=\"warning\">Please enter one or more EML document URLs into the text area.</p>";
						}
						else {
							documentURLs = parseDocumentURLsFromTextArea(urlTextArea);
							warningMessage = CHECK_BACK_LATER;
						}
					} 
					else if (metadataSource.equals("harvestList")) {
						String useChecksumParam = request.getParameter("useChecksum");
						useChecksum = (useChecksumParam != null);
						harvestListURL = request.getParameter("harvestListURL");
						if (harvestListURL == null || 
							harvestListURL.trim().isEmpty()
						) {
							warningMessage = "<p class=\"warning\">Please enter the URL to a Metacat Harvest List.</p>";
						}
						else {
							documentURLs = parseDocumentURLsFromHarvestList(harvestListURL);
							warningMessage = CHECK_BACK_LATER;
						}
					}
					/*
					 * If the metadata source is "desktopHarvester", we already have the
					 * EML file stored in a session attribute. Now we need to retrieve
					 * the data files from the brower's form fields and write the
					 * data files to a URL accessible location.
					 */
					else if (metadataSource.equals("desktopHarvester")) {
						String useChecksumParam = request.getParameter("useChecksum");
						useChecksum = (useChecksumParam != null);
						emlFile = (File) httpSession.getAttribute("emlFile");
						ArrayList<Entity> entityList = parseEntityList(emlFile);
						harvestReportId = (String) httpSession.getAttribute("harvestReportId");
						String dataPath = servletContext.getRealPath(DESKTOP_DATA_DIR);
				        String harvestPath = String.format("%s/%s", dataPath, harvestReportId);
						
						Collection<Part> parts = request.getParts();
						String objectName = null;
						Part filePart = null;

						for (Part part : parts) {
							if (part.getContentType() != null) {
								// save data file to disk
								//processDataFile(part, harvestPath);
								filePart = part;
							}
							else {
								/*
								 * Parse the request parameters.
								 */
								String fieldName = part.getName();
								String fieldValue = request.getParameter(fieldName);
								if (fieldName != null && fieldValue != null) {
									if (fieldName.equals("submit") && 
										fieldValue.equalsIgnoreCase("evaluate")
									   ) {
										isEvaluate = true;
									}
									else if (fieldName.startsWith("object-name-")
										   ) {
										objectName = fieldValue;
									}
								}
							}
							
							if (filePart != null && objectName != null) {
								processDataFile(filePart, harvestPath, objectName);
								objectName = null;
								filePart = null;
							}
							
						}
						
						emlFile = transformDesktopEML(harvestPath, emlFile, harvestReportId, entityList);
					}
				}
				else {
					throw new IllegalStateException(
							"No value specified for request parameter 'metadataSource'");
				}

				if (harvester == null) {
					harvester = new Harvester(harvesterPath,
						harvestReportId, uid, isEvaluate, useChecksum);
				}

				if (emlTextArea != null) {
					harvester.processSingleDocument(emlTextArea);
				}
				else if (emlFile != null) {
					if (isDesktopUpload) {
						ArrayList<Entity> entityList = parseEntityList(emlFile);
						httpSession.setAttribute("entityList", entityList);
						httpSession.setAttribute("emlFile", emlFile);
						httpSession.setAttribute("harvestReportId", harvestReportId);
						httpSession.setAttribute("isEvaluate", new Boolean(isEvaluate));
						httpSession.setAttribute("useChecksum", new Boolean(useChecksum));
					}
					else {
						harvester.processSingleDocument(emlFile);
					}
				}
				else if (documentURLs != null) {
					harvester.setDocumentURLs(documentURLs);
					ExecutorService executorService = Executors
									.newCachedThreadPool();
					executorService.execute(harvester);
					executorService.shutdown();
				}
			}
		}
		catch (Exception e) {
			handleDataPortalError(logger, e);
		}

		request.setAttribute("message", warningMessage);

		/*
		 * If we have a new reportId, and either there is no warning message or
		 * it's the "Check back later" message, set the harvestReportID session
		 * attribute to the new reportId value.
		 */
		if (harvestReportId != null
				&& harvestReportId.length() > 0
				&& (warningMessage.length() == 0 || warningMessage
						.equals(CHECK_BACK_LATER))) {
			httpSession.setAttribute("harvestReportID", harvestReportId);
		}

		if (isDesktopUpload) {
			RequestDispatcher requestDispatcher = request
					.getRequestDispatcher("./desktopHarvester.jsp");
			requestDispatcher.forward(request, response);
		}
		else if (warningMessage.length() == 0) {
				response.sendRedirect("./harvestReport.jsp");
			}
		else {
			RequestDispatcher requestDispatcher = request
						.getRequestDispatcher("./harvester.jsp");
			requestDispatcher.forward(request, response);
		}

	}
  
  
    /*
     * Parses the EML file and returns a list of Entity objects
     */
	private ArrayList<Entity> parseEntityList(File emlFile)
			throws IOException {
		String eml = FileUtils.readFileToString(emlFile);
		EMLParser emlParser = new EMLParser();
		DataPackage dataPackage = emlParser.parseDocument(eml);		
		ArrayList<Entity> entityList = dataPackage.getEntityList();

		return entityList;
	}
  
  
  /*
   * Generates a numeric harvest ID based on the current timestamp.
   */
  private String generateHarvestId() {
    String harvestId = null;
    
    Date now = new Date();
    Long epochTime = now.getTime();
    String epochTimeString = epochTime.toString();
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateString = dateFormat.format(now);
    String numberString = epochTimeString;
    harvestId = dateString + "-" + numberString;
    logger.debug("HARVESTID: " + harvestId);
    
    return harvestId;
  }
  
  
  private String getFilename(Part part) {
      String contentDispositionHeader =
              part.getHeader("content-disposition");
      String[] elements = contentDispositionHeader.split(";");
      for (String element : elements) {
          if (element.trim().startsWith("filename")) {
              return element.substring(element.indexOf('=') + 1)
                      .trim().replace("\"", "");
          }
      }
      return null;
  }


  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    harvesterPath = options.getString("harvester.path");
    desktopUrlHead = options.getString("dataportal.desktopUrlHead");
}
  
  
	/**
	 * Stores a local copy of EML metadata on the file system.
	 * 
	 * @param   xmlContent   the XML string content
	 * @return  the EML file that was created
	 */
	 public File storeMetadata(String dirPath, String xmlContent, boolean isPastaReady) 
	         throws IOException {
	     File emlFile = null;
	   
	     File dirFile = new File(dirPath);
	     if (dirFile != null && !dirFile.exists()) { dirFile.mkdirs(); }
	     String filename = isPastaReady ? PASTA_READY_FILE_NAME : DESKTOP_FILE_NAME;
	     emlFile = new File(dirPath, filename);
	     FileWriter fileWriter = null;
	     
	     StringBuffer stringBuffer = new StringBuffer(xmlContent);
	     try {
	       fileWriter = new FileWriter(emlFile);
	       IOUtil.writeToWriter(stringBuffer, fileWriter, true);
	     }
	     catch (IOException e) {
	       logger.error("IOException storing PASTA-ready metadata file:\n" + 
	                    e.getMessage());
	       e.printStackTrace();
	       throw(e);
	     }
	     finally {
	       if (fileWriter != null) { fileWriter.close(); }
	     }
	    
	   return emlFile;
	 }

	 
  /*
   * Transforms the user's desktop EML to PASTA-ready EML.
   */
  private File transformDesktopEML(String harvestPath, File desktopEMLFile, String harvestReportId, ArrayList<Entity> entityList)
	  throws IOException, TransformerException, SAXException, ParserConfigurationException {
      boolean isPastaReady = false;
	  File pastaReadyEMLFile = null;
      Document desktopEMLDocument = XmlUtility.xmlFileToDocument(desktopEMLFile);
      Node documentElement = desktopEMLDocument.getDocumentElement();
      String desktopEMLString = FileUtils.readFileToString(desktopEMLFile);
      storeMetadata(harvestPath, desktopEMLString, isPastaReady);
      String harvestUrlHead = String.format("%s/%s", desktopUrlHead, harvestReportId);
      DesktopEMLFactory desktopEMLFactory = new DesktopEMLFactory(harvestUrlHead);
      Document pastaReadyEMLDocument = desktopEMLFactory.makePastaReady(desktopEMLDocument, entityList);
      documentElement = pastaReadyEMLDocument.getDocumentElement();
      String pastaReadyEMLString = XMLUtilities.getDOMTreeAsString(documentElement);
      isPastaReady = true;
      pastaReadyEMLFile = storeMetadata(harvestPath, pastaReadyEMLString, isPastaReady);      

      return pastaReadyEMLFile;
    }
  
  
  /*
   * Parses a list of document URLs from a legacy Metacat Harvester
   * harvest list.
   */
  private ArrayList<String> parseDocumentURLsFromHarvestList(String harvestListURL) 
          throws Exception {
    ArrayList<String> urlList = new ArrayList<String>();
    
    if (harvestListURL != null) {
      DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
          .newDocumentBuilder();
      CachedXPathAPI xpathapi = new CachedXPathAPI();
      Document document = documentBuilder.parse(harvestListURL);

      if (document != null) {
        NodeList documentURLNodeList = xpathapi.selectNodeList(document, "//documentURL");
        for (int i = 0; i < documentURLNodeList.getLength(); i++) {
          Node aNode = documentURLNodeList.item(i);
          String url = aNode.getTextContent();
          urlList.add(url);
        }
      }
    }

    return urlList;
  }


  /*
   * Parses a list of document URLs from a user input text area.
   */
  private ArrayList<String> parseDocumentURLsFromTextArea(String textArea) {
    ArrayList<String> urlList = new ArrayList<String>();
    
    if (textArea != null) {
      String[] lines = textArea.split("\n");
      for (String line : lines) {
        line = line.trim();
        if (line != null && 
            (line.startsWith("http://") ||
             line.startsWith("https://") ||
             line.startsWith("ftp://")
            )
           ) {
          urlList.add(line);
        }
      }
    }

    return urlList;
  }

  
  /**
   * Process the uploaded EML file
   * 
   * @param part The multipart form file data.
   * 
   * @return The uploaded EML file as File object.
   * 
   * @throws Exception
   */
  private File processUploadedFile(Part part) throws Exception {

    File eml = null;

    if (part.getContentType() != null) {
        // save file Part to disk
        String fileName = getFilename(part);
        if (fileName != null && !fileName.isEmpty()) {
            long timestamp = new Date().getTime();
            String tmpDir = String.format("%s/%d", System.getProperty("java.io.tmpdir"), timestamp);
            Harvester.createDirectory(tmpDir);
            String tmpPath = String.format("%s/%s", tmpDir, fileName);
            part.write(tmpPath);
            eml = new File(tmpPath);
        }
    }
    
    return eml;
  }

  
  /**
   * Process the uploaded data file
   * 
   * @param part The multipart form file data.
   * 
   * @return The uploaded data file as File object.
   * 
   * @throws Exception
   */
  private String processDataFile(Part part, String harvestDir, String objectName)
		  throws Exception {
    String fileName = null;
    
    if (part.getContentType() != null) {
        // save the data file to disk where it can be harvested
        fileName = getFilename(part);
        
        if (fileName != null && !fileName.isEmpty()) {
        	
        	if (!fileName.equals(objectName)) {
        		String msg = String.format("Filename \"%s\" does not match objectName \"%s\".", fileName, objectName);
        		throw new UserErrorException(msg);
        	}
        	
            Harvester.createDirectory(harvestDir);
            String filePath = String.format("%s/%s", harvestDir, fileName);
            part.write(filePath);
        }
    }
    
    return fileName;
  }

}
