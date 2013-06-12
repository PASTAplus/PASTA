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
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.client.PastaAuthenticationException;

/**
 * 
 * @author dcosta
 *
 */
public class HarvesterServlet extends DataPortalServlet {

  /*
   * Class variables
   */

  private static String harvesterPath = null;
  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.HarvesterServlet.class);
  private static final long serialVersionUID = 1L;
  
  
  /*
   * Instance variables
   */
  
  private final String CHECK_BACK_LATER =
      "<p class=\"warning\">This may take a few minutes. Please check the <a href=\"./harvestReport.jsp\">View Upload Reports</a> page for available reports.</p>";
  
  
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
    String uid = (String) httpSession.getAttribute("uid");
    String warningMessage = "";
    
    ArrayList<String> documentURLs = null;
    boolean isEvaluate = false;
    boolean isMultipart = ServletFileUpload.isMultipartContent(request);
    String emlTextArea = null;
    File emlFile = null;
    String urlTextArea = null;
    String harvestListURL = null;
    String reportId = null;
    
    try {
      if (uid == null) {
        warningMessage = "<p class=\"warning\">" + LOGIN_WARNING + "</p>";
      } 
      else {
        /*
         * "metadataSource" can have a value of "emlText", "emlFile",
         * "urlList", or "harvestList". It is set as a hidden input field 
         * in each of the harvester forms except the file upload form,
         * which is detected by the above call to:
         *   ServletFileUpload.isMultipartContent(request)
         */
        String metadataSource = request.getParameter("metadataSource");
        if (isMultipart) metadataSource = "emlFile";
    
        if (metadataSource != null) {
          if (metadataSource.equals("emlText")) {
            emlTextArea = request.getParameter("emlTextArea");
            if (emlTextArea == null || emlTextArea.trim().isEmpty()) {
              warningMessage = "<p class=\"warning\">Please enter the text of an EML document into the text area.</p>";
            }
          }
          else if (metadataSource.equals("emlFile")) {

            if (isMultipart) {
              // Create a factory for disk-based file items
              FileItemFactory factory = new DiskFileItemFactory();
              
              // Create a new file upload handler
              ServletFileUpload upload = new ServletFileUpload(factory);

              // Parse the request
              List /* FileItem */items = upload.parseRequest(request);

              // Process the uploaded items
              Iterator iter = items.iterator();
                
              while (iter.hasNext()) {                 
                FileItem item = (FileItem) iter.next();
                if (!(item.isFormField())) {
                  String fileName = item.getName();
                  if (fileName != null && !fileName.isEmpty()) {
                    emlFile = processUploadedFile(item);
                  }
                  else {
                    warningMessage = "<p class=\"warning\">Please enter an EML file.</p>";
                  }
                } 
                else {                  
                  String fieldName = item.getFieldName();
                  String itemString = item.getString();
                  if (fieldName != null && 
                      fieldName.equals("submit") &&
                      itemString != null &&
                      itemString.equals("evaluate")
                     ) {
                    isEvaluate = true;                   
                  }
                }
              }
            }
          }
          else if (metadataSource.equals("urlList")) {
            urlTextArea = request.getParameter("urlTextArea");
            if (urlTextArea == null || urlTextArea.trim().isEmpty()) {
              warningMessage = "<p class=\"warning\">Please enter one or more EML document URLs into the text area.</p>";
            }
            else {
              documentURLs = parseDocumentURLsFromTextArea(urlTextArea);
              warningMessage = CHECK_BACK_LATER;
            }
          }
          else if (metadataSource.equals("harvestList")) {
            harvestListURL = request.getParameter("harvestListURL");
            if (harvestListURL == null || harvestListURL.trim().isEmpty()) {
              warningMessage = "<p class=\"warning\">Please enter the URL to a Metacat Harvest List.</p>";
            }
            else {
              documentURLs = parseDocumentURLsFromHarvestList(harvestListURL);
              warningMessage = CHECK_BACK_LATER;
            }
          }
        }
    
        /*
         * "mode" can have a value of "evaluate" or "upgrade". It is set as the
         * value of the submit button in each of the harvester forms.
         */
        String mode = request.getParameter("submit");
        if (mode != null && mode.equals("evaluate")) {
          isEvaluate = true;
        }
        
        String harvestId = generateHarvestId();
        
        if (isEvaluate) {
          reportId = uid + "-evaluate-" + harvestId;
        }
        else {
          reportId = uid + "-upload-" + harvestId;
        }
        
        String harvestDirPath = harvesterPath + "/" + reportId;
        Harvester harvester = new Harvester(harvestDirPath, uid, isEvaluate);
        
        if (emlTextArea != null) {
          harvester.processSingleDocument(emlTextArea);
        }
        else if (emlFile != null) {
          harvester.processSingleDocument(emlFile);
        }
        else if (documentURLs != null) {
          harvester.setDocumentURLs(documentURLs);
          ExecutorService executorService = Executors.newCachedThreadPool();
          executorService.execute(harvester);
          executorService.shutdown();
        }
      }
    }
    catch (PastaAuthenticationException e) {
      warningMessage = "<p class=\"warning\">" + LOGIN_WARNING + "</p>";
      logger.error(e.getMessage());
    }
    catch (Exception e) {
      String eMessage = e.getMessage();
      warningMessage = "<p class=\"warning\">A problem occurred while processing your request: " + 
                eMessage + "</p>";
      logger.error(eMessage);
    }
    finally {
      request.setAttribute("message", warningMessage);
      
      /*
       * If we have a new reportId, and either there is no warning message
       * or it's the "Check back later" message, set the harvestReportID
       * session attribute to the new reportId value.
       */
      if (reportId != null && 
          reportId.length() > 0 &&
          (warningMessage.length() == 0 || 
           warningMessage.equals(CHECK_BACK_LATER)
          )
         ) {
        httpSession.setAttribute("harvestReportID", reportId);
      }
      
      if (warningMessage.length() == 0) {
        response.sendRedirect("./harvestReport.jsp");
      }
      else {
        RequestDispatcher requestDispatcher = request.getRequestDispatcher("./harvester.jsp");
        requestDispatcher.forward(request, response);
      }
    }
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
  
  
  /**
   * Initialization of the servlet. <br>
   * 
   * @throws ServletException
   *           if an error occurs
   */
  public void init() throws ServletException {
    PropertiesConfiguration options = ConfigurationListener.getOptions();
    harvesterPath = options.getString("harvester.path");
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
   * Process the uploaded file
   * 
   * @param item The multipart form file data.
   * 
   * @return The uploaded file as File object.
   * 
   * @throws Exception
   */
  private File processUploadedFile(FileItem item) throws Exception {

    File eml = null;

    // Process a file upload
    if (!item.isFormField()) {
      // Get object information
      String fieldName = item.getFieldName();
      String fileName = item.getName();
      String contentType = item.getContentType();
      boolean isInMemory = item.isInMemory();
      long sizeInBytes = item.getSize();
      String tmpdir = System.getProperty("java.io.tmpdir");
      logger.debug("FILE: " + tmpdir + "/" + fileName);
      eml = new File(tmpdir + "/" + fileName);
      item.write(eml);
    }

    return eml;
  }

}
