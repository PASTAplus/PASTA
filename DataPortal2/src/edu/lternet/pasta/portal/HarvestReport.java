/*
 *
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
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;


public class HarvestReport {
  
  /*
   * Class variables
   */
  
  private static final Logger logger = Logger
  .getLogger(edu.lternet.pasta.portal.HarvestReport.class);
  
  /*
   * Instance variables
   */
  
  
  /*
   * Constructors
   */
  

  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  /**
   * Composes harvest report HTML list for use inside a JSP. A list
   * of reports is generated based on the user id.
   * 
   * @param uid  the user id, e.g. "ucarroll"
   * @return  the HTML string, a "<ul>" element
   */
  public String composeHarvestReports(String uid, String reportId) {
    StringBuffer htmlStringBuffer = new StringBuffer(""); 
    ArrayList<String> harvestDirs = getHarvestDirs(uid);  
    
    for (String harvestDir : harvestDirs) {
      String selected = (harvestDir.equals(reportId)) ? " selected='selected'" : "";
      String formattedReportId = reportIdFormatter(harvestDir);
      htmlStringBuffer.append(String.format("<option value='%s'%s>%s</option>\n",
      				                        harvestDir, selected, formattedReportId)
      				         );
    }
    
    return htmlStringBuffer.toString();
  }
  
  
  /**
   * Generates HTML for a specific harvest report based on the
   * specified reportId value.
   * 
   * @param reportId  the report ID, e.g. "ucarroll-evaluate-2012-04-16-13346200183"
   * @return  the harvest report HTML string, a "<table>" element
   */
  public String harvestReportHTML(String reportId) {
    String reportPath = HarvestReportServlet.getHarvesterPath() + "/" + reportId;
    boolean isEvaluate = (reportId != null && reportId.contains("-evaluate-"));
    String verb = isEvaluate ? "evaluated" : "uploaded";
    StringBuffer stringBuffer = new StringBuffer("");
    
    stringBuffer.append("<table>\n");
    stringBuffer.append("<thead>\n");
    stringBuffer.append("<tr>\n");
    stringBuffer.append("<th class=\"nis\">Package Id</th>\n");
    stringBuffer.append("<th class=\"nis\">Was<br/>" + verb + "</th>\n");
    stringBuffer.append("<th class=\"nis\">Report</th>\n");
    stringBuffer.append("<th class=\"nis\">Total<br/>Quality<br/>Checks</th>\n");
    stringBuffer.append("<th class=\"nis\">Valid</th>\n");
    stringBuffer.append("<th class=\"nis\">Info</th>\n");
    stringBuffer.append("<th class=\"nis\">Warn</th>\n");
    stringBuffer.append("<th class=\"nis\">Error</th>\n");
    stringBuffer.append("<th class=\"nis\">System Message</th>\n");
    stringBuffer.append("</tr>\n");
    stringBuffer.append("</thead>\n");
    stringBuffer.append("<tbody>\n");
    
    String urlMessagesPath = reportPath + "/urlMessages.txt";
    ArrayList<String> urlMessages = getUrlMessages(urlMessagesPath);
    for (String urlMessage : urlMessages) {
      stringBuffer.append(urlMessageHTML(urlMessage));
    }
    
    ArrayList<String> packageIds = getPackageIds(reportPath, reportId);
    for (String packageId : packageIds) {
      stringBuffer.append(dataPackageHTML(reportPath, reportId, packageId, isEvaluate));
    }
      
    stringBuffer.append("</tbody>\n");
    stringBuffer.append("</table>\n");
    
    String htmlString = stringBuffer.toString();
    return htmlString;
  }
  
  
  /*
   * Get a list of URL error messages, if any, that were generated
   * during the evaluate/insert processing.
   */
  private ArrayList<String> getUrlMessages(String urlMessagesPath) {
    ArrayList<String> urlMessages = new ArrayList<String>();

    try {
      if (urlMessagesPath != null && !urlMessagesPath.equals("")) {
        File urlMessagesFile = new File(urlMessagesPath);
        if (urlMessagesFile != null && urlMessagesFile.exists()) {
          String urlMessagesText = FileUtils.readFileToString(urlMessagesFile);
          if (urlMessagesText != null && urlMessagesText.length() > 0) {
            String[] messages = urlMessagesText.split("\n");
            for (int i = 0; i < messages.length; i++) {
              String message = messages[i];
              if (message != null && message.length() > 0) {
                urlMessages.add(message);
              }
            }
          }
        }
      }
    }
    catch (IOException e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }

    return urlMessages;
  }

  
  /*
   * Generate the HTML <tr> element for a URL error message. This
   * message occurs when a document URL (specified either in the
   * harvester window or in a legacy Metacat harvest list) generates
   * an error. In these cases, the EML cannot be evaluated and even
   * the packageId value is unknown.
   */
  private String urlMessageHTML(String urlMessage) {
    String html = "";
    
    if (urlMessage != null && urlMessage.length() > 0) {
      html = "<tr>" + "<td class=\"data\" align=\"center\">n/a</td>"
          + "<td class=\"data\" align=\"center\">No</td>"
          + "<td class=\"data\" align=\"center\">n/a</td>"
          + "<td class=\"data\" align=\"center\">0</td>"
          + "<td class=\"data\" align=\"center\">0</td>"
          + "<td class=\"data\" align=\"center\">0</td>"
          + "<td class=\"data\" align=\"center\">0</td>"
          + "<td class=\"data\" align=\"center\">0</td><"
          + "/td><td class=\"data\" align=\"center\">" + urlMessage
          + "</td></tr>";
    }
    else {
      html = "<tr><td class=\"data\" colspan=\"9\"></td></tr>";
    }
    
    return html;
  }
  
  
  /*
   * Get all packageId values for the specified harvest report ID.
   */
  private ArrayList<String> getPackageIds(String reportPath, String reportId) {
    ArrayList<String> packageIds = new ArrayList<String>();
    
    if (reportId != null && !reportId.equals("")) {
      File dir = new File(reportPath);
      File[] fileArray = dirListByAscendingDate(dir);
      
      for (int i = 0; i < fileArray.length; i++) {
        File aFile = fileArray[i];
        String fileName = aFile.getName();
        if (fileName != null && !fileName.equals("eml")) {
          packageIds.add(fileName);
        }
      }
    }
    
    return packageIds;
  }
  
  
  /*
   * Sorts directories by date in ascending order.
   */
  private File[] dirListByAscendingDate(File folder) {
    if (!folder.isDirectory()) {
      return null;
    }
    
    File files[] = folder.listFiles();
    
    Arrays.sort( files, new Comparator<Object>() {
      public int compare(final Object o1, final Object o2) {
        return new Long(((File)o1).lastModified()).compareTo
             (new Long(((File) o2).lastModified()));
      }
    }
    ); 
    
    return files;
  }
  
  
  /*
   * Generates the data package HTML for a single row in the
   * harvest report table.
   */
  private String dataPackageHTML(String harvestReportPath, String reportId, 
                                 String packageId, boolean isEvaluate) {
    String dataPackageLink = packageId;
    String qualityReportLink = "n/a";
    StringBuffer stringBuffer = new StringBuffer("<tr>\n");
    String packageIdPath = harvestReportPath + "/" + packageId;
    String qualityReportPath = packageIdPath + "/qualityReport.xml";
    File qualityReportFile = new File(qualityReportPath);
    
    boolean wasInserted = wasInserted(packageIdPath, isEvaluate);
    String inserted = wasInserted ? "Yes" : "No";
    
    if (wasInserted && !isEvaluate) {
      dataPackageLink = getDataPackageLink(packageId);
    }

    boolean qualityReportExists = qualityReportFile != null && 
                                  qualityReportFile.exists();
    if (qualityReportExists) {
      qualityReportLink = getQualityReportLink(packageId, qualityReportPath);
    }
    else if (wasInserted) {
      qualityReportLink = getQualityReportLink(packageId, null);
    }

    String serviceMessage = serviceMessage(packageIdPath);
    int[] statusCounts = getStatusCounts(qualityReportFile);
    
    stringBuffer.append("<td class=\"nis\">" + dataPackageLink + "</td>\n");
    stringBuffer.append("<td class=\"nis\">" + inserted + "</td>\n");
    stringBuffer.append("<td class=\"nis\">" + qualityReportLink + "</td>\n");

    for (int i = 0; i < statusCounts.length; i++) {
      stringBuffer.append("<td class=\"nis\">" + statusCounts[i] + "</td>\n");
    }

    stringBuffer.append("<td class=\"nis\" align=\"left\" >" + serviceMessage + "</td>\n");
    
    stringBuffer.append("</tr>\n");
    String html = stringBuffer.toString();
    return html;
  }
  
  
  /*
   * Generates the data package link to the mapbrowse servlet
   * for a data package based on its packageId value.
   */
  private String getDataPackageLink(String packageId) {
    String link = "";
    
    if (packageId != null && packageId.length() > 0) {
      EmlPackageIdFormat epf = new EmlPackageIdFormat();
      EmlPackageId emlPackageId = epf.parse(packageId);
      String scope = emlPackageId.getScope();
      Integer identifier = emlPackageId.getIdentifier();
      Integer revision = emlPackageId.getRevision();
      link = "<a class=\"searchsubcat\" href=\"./mapbrowse?scope=" + scope +
             "&identifier=" + identifier.toString() +
             "&revision=" + revision.toString() +
             "\">" + packageId + "</a>";
    }

    return link;
  }

  
  /*
   * Generates the quality report link for an entry in the
   * harvest report table. If localPath is a non-null value,
   * it will pass the value to the localPath query parameter
   * of the reportviewer servlet.
   */
  private String getQualityReportLink(String packageId, String localPath) {
    String link = "";
    URLCodec urlCodec = new URLCodec();

    try {
      if (packageId != null && packageId.length() > 0) {
        link = "<a class=\"searchsubcat\" href=\"./reportviewer?packageid=" + packageId;
        
        if (localPath != null) {
          String encodedPath = urlCodec.encode(localPath);
          link += "&localPath=" + encodedPath; 
        }
        
        link += "\" target=\"_blank\">view</a>";
      }
    }
    catch (EncoderException e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }

    return link;
  }

  
  /*
   * Generates an array of five int values representing status counts
   * for the quality checks in the specified quality report file.
   */
  private int[] getStatusCounts(File qualityReportFile) {
    int[] statusCounts = new int[5];
    int total = 0;
    int valid = 0;
    int info = 0;
    int warn = 0;
    int error = 0;
    
    try {
      if (qualityReportFile != null && qualityReportFile.exists()) {
        FileInputStream fis = new FileInputStream(qualityReportFile);

        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder();
        Document document = documentBuilder.parse(fis);
        NodeList nodeList = document.getElementsByTagName("status");
        for (int i = 0; i < nodeList.getLength(); i++) {
          Node node = nodeList.item(i);
          String status = node.getTextContent();
          if (status != null && status.length() > 0) {
            if (status.equals("valid")) { valid++; total++; }
            if (status.equals("info")) { info++; total++; }
            if (status.equals("warn")) { warn++; total++; }
            if (status.equals("error")) { error++; total++; }
          }
        }
        statusCounts[0] = total;
        statusCounts[1] = valid;
        statusCounts[2] = info;
        statusCounts[3] = warn;
        statusCounts[4] = error;
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      logger.error(e.getMessage());
    }

    return statusCounts;
  }
  
  
  /*
   * Boolean to determine whether a given data package was either
   * evaluated or inserted.
   */
  private boolean wasInserted(String packageIdPath, boolean isEvaluate) {
    boolean wasInserted = false;
    String fileName = isEvaluate ? "qualityReport.xml" : "resourceMap.txt";
    String path = packageIdPath + "/" + fileName;
    File file = new File(path);
    wasInserted = file.exists();
    return wasInserted;
  }
  
  
  /*
   * Reads text from the service message file for a data package and
   * returns it as a String value. Returns an empty string if no
   * service message file exists for the data package.
   */
  private String serviceMessage(String packageIdPath) {
    String serviceMessage = "";
    String fileName = "serviceMessage.txt";
    String path = packageIdPath + "/" + fileName;
    File file = new File(path);

    try {
      if (file.exists()) {
        serviceMessage = FileUtils.readFileToString(file);
      }
    }
    catch (IOException e) {
      logger.error(e.getMessage());
    }

    return serviceMessage;
  }
 
  
  /**
   * Returns the report identifier for the newest harvest report
   * that has been generated for the specified user.
   * 
   * @param uid    the user id, e.g. "ucarroll"
   * @return  a report identifier value for the newest report
   */
  public String newestHarvestReport(String uid) {
    String newestReport = null;
    
    if (uid != null && uid.length() > 0) {
      ArrayList<String> harvestDirs = getHarvestDirs(uid);
      newestReport = getNewestReport(harvestDirs);    
    }
    
    return newestReport;
  }
  
  
  /*
   * Gets a list of harvest report directory names for the specified 
   * user id.
   */
  private ArrayList<String> getHarvestDirs(String uid) {
    ArrayList<String> harvestDirs = new ArrayList<String>();
    
    if (uid != null && !uid.equals("")) {
      String harvesterPath = HarvestReportServlet.getHarvesterPath();
      File dir = new File(harvesterPath);
      String[] fileNames = dir.list( DirectoryFileFilter.INSTANCE );
      if (fileNames != null) {
        for (int i = 0; i < fileNames.length; i++) {
          String fileName = fileNames[i];
          if (fileName != null && 
               (fileName.startsWith(uid + "-evaluate-") || 
                fileName.startsWith(uid + "-upload-")
               )
             ) {
            harvestDirs.add(fileName);
          }
        }
      }
    }
    
    Comparator<String> reportComparator = new Comparator<String>() {
      public int compare(String a, String b) {
        Long aLong = parseEpoch(a);
        Long bLong = parseEpoch(b);
        Long diff = aLong - bLong;
        int result = (diff < 0L ? 1 : -1);
        return result;
      }
    };
    Collections.sort(harvestDirs, reportComparator);
    
    return harvestDirs;
  }
 
  
  /*
   * Gets the newest harvest report identifier from a list of 
   * harvest report identifiers.
   */
  private String getNewestReport(ArrayList<String> reports) {
    String newest = null;
    Long maxLong = new Long(0L);
    
    for (String report : reports) {
      String[] tokens = report.split("-");
      Long newLong = new Long(tokens[5]);
      if (newLong > maxLong) {
        maxLong = newLong;
        newest = report;
      }
    }
    
    return newest;
  }
  
  
  /*
   * Parse the epoch value from a harvest report identifier and
   * return it as a Long.
   */
  private Long parseEpoch(String reportId) {
    Long epochValue = null;
    
    if (reportId != null && reportId.length() > 0) {
      String tokens[] = reportId.split("-");
      if (tokens != null) {
        Integer length = tokens.length;
        epochValue = Long.valueOf(tokens[length - 1]);
      } 
    }
    
    return epochValue;
  }
  
  
  private String reportIdFormatter(String reportId) {
    
    String newId;
    
    if (reportId != null) {
      String tokens[] = reportId.split("-");
      Integer length = tokens.length;

      Long epoch = Long.valueOf(tokens[length - 1]);

      Time time = new Time(epoch);
      Date date = new Date(epoch);

      newId = date.toString();

      return newId + " (" + tokens[1] + ")";
    }
    else {
      return "";
    }
  }

}
