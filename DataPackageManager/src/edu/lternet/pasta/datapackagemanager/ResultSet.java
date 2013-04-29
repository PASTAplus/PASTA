/**
 *
 * $Date$
 * $Author: dcosta $
 * $Revision$
 *
 * Copyright 2011 the University of New Mexico.
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

package edu.lternet.pasta.datapackagemanager;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.ecoinformatics.datamanager.quality.QualityReport;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import edu.lternet.pasta.common.EmlPackageId;
import edu.lternet.pasta.common.EmlPackageIdFormat;
import edu.lternet.pasta.common.security.authorization.Rule;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.datapackagemanager.DataPackageManager.ResourceType;
import edu.ucsb.nceas.utilities.Options;
import edu.ucsb.nceas.utilities.XMLUtilities;


/**
 * @author dcosta
 * 
 * The ResultSet class stores result set XML from Metacat searches and
 * provides methods for PASTA customization.
 * 
 * Please note: The current implementation relies on the fact that
 * <document> elements in the Metacat result set XML appear one to
 * a line. If this should ever change, the implementation will need
 * to be changed as well. For prototyping purposes, however, the current
 * implementation is adequate.
 */
public class ResultSet {
  
  /*
   * Class fields
   */

  private static final String PACKAGEID_PATH = "//resultset/document/packageId";
  private static String dbDriver = null;
  private static String dbUser = null;
  private static String dbPassword = null;
  private static String dbURL = null;
  
  /*
   * Instance fields
   */
  
  private Logger logger = Logger.getLogger(ResultSet.class);
  
  private String resultSetXML = null;
  private Authorizer authorizer = null;

  
  /*
   * Constructors
   */
  
  ResultSet(String xmlString) throws Exception {
    
  	this.resultSetXML = xmlString;
    
    loadOptions();
    DataPackageRegistry dataPackageRegistry = new DataPackageRegistry(dbDriver,
		    dbURL, dbUser, dbPassword);
    authorizer = new Authorizer(dataPackageRegistry);
    
  }

  
  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */
  
	/**
	 * Loads Data Manager options from a configuration file.
	 */
	private void loadOptions() throws Exception {
		try {
			// Load database connection options
			Options options = ConfigurationListener.getOptions();
			dbDriver = options.getOption("dbDriver");
			dbURL = options.getOption("dbURL");
			dbUser = options.getOption("dbUser");
			dbPassword = options.getOption("dbPassword");

		} catch (Exception e) {
			logger.error("Error in loading options: " + e.getMessage());
			e.printStackTrace();
			throw (e);
		}
	}

  
  private String packageIdToResourceId(String packageId) {
    String resourceId = null;
    
    if (packageId != null) {    
      EmlPackageIdFormat emlPackageIdFormat = new EmlPackageIdFormat();
      EmlPackageId emlPackageId = emlPackageIdFormat.parse(packageId);
      String scope = emlPackageId.getScope();
      Integer identifier = emlPackageId.getIdentifier();
      Integer revision = emlPackageId.getRevision();
      resourceId = 
          DataPackageManager.composeResourceId(ResourceType.dataPackage, 
                                               scope, identifier, 
                                               revision, null);
    }
    
    return resourceId;
  }


  /**
   * This code does the same as toPastaFormat() but uses a less
   * efficient implemenation. Keeping it commented out for future
   * reference.
   * 
   * Converts this result set to PASTA format.
   * 
   * @return   the result set XML in PASTA format
   *
  public String toPastaFormat2() {
    String pastaFormatXML = this.resultSetXML;
    
    boolean continueOn = true;
    int lastBeginIndex = 0;
    int lastEndIndex = 0;
    final String beginTag = "<docid>";
    final String endTag = "</docid>";

    if (this.resultSetXML != null) {
      while (continueOn) {
        lastBeginIndex = this.resultSetXML.indexOf(beginTag, lastBeginIndex);     
        if (lastBeginIndex != -1) {
          lastEndIndex = this.resultSetXML.indexOf(endTag, lastBeginIndex);
          String docid = this.resultSetXML.substring(lastBeginIndex + beginTag.length(), lastEndIndex);
          String resourceId = packageIdToResourceId(docid);
          String originalString = "<docid>" + docid + "</docid>";
          String replacementString = "\n<packageId>" + docid + "</packageId>\n<resourceId>" + resourceId + "</resourceId>\n";
          pastaFormatXML = pastaFormatXML.replace(originalString, replacementString);
          lastBeginIndex += beginTag.length();
        }
        else {
          continueOn = false;
        }
      }
    }
    
    return pastaFormatXML;
  } */
  
  
  /**
   * Converts this result set to PASTA format.
   * 
   * @return   the result set XML in PASTA format
   */
  public String toPastaFormat(AuthToken authToken) {
    String pastaFormatXML = this.resultSetXML;
    
    if (pastaFormatXML != null) {
      pastaFormatXML = pastaFormatXML.replace("<docid>", "<packageId>");
      pastaFormatXML = pastaFormatXML.replace("</docid>", "</packageId>");
      
      InputStream is = new ByteArrayInputStream(pastaFormatXML.getBytes());

      try {
        DocumentBuilder documentBuilder = 
          DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = documentBuilder.parse(is);
        Node documentElement = document.getDocumentElement();
        
        NodeList packageIdNodeList = null;
        CachedXPathAPI xpathapi = new CachedXPathAPI();

        // Process <packageId> elements
        packageIdNodeList = xpathapi.selectNodeList(documentElement, PACKAGEID_PATH);
        int nNodes = packageIdNodeList.getLength();
        
				for (int i = 0; i < nNodes; i++) {
					Node packageIdNode = packageIdNodeList.item(i);
					String packageId = packageIdNode.getTextContent();
					String resourceId = packageIdToResourceId(packageId);
					Node parentNode = packageIdNode.getParentNode();
					
					// Remove package from result set if not authorized; otherwise add
					// resourceId
					if (!authorizer.isAuthorized(authToken, resourceId,
					    Rule.Permission.read)) {
						Node documentNode = parentNode.getParentNode();
						documentNode.removeChild(parentNode);
					} else {
						Element resourceIdElement = document.createElement("resourceId");
						Text resourceIdText = document.createTextNode(resourceId);
						resourceIdElement.appendChild(resourceIdText);
						parentNode.insertBefore(resourceIdElement, packageIdNode);
					}
					
				}

        pastaFormatXML = XMLUtilities.getDOMTreeAsString(documentElement);
        logger.debug(pastaFormatXML);
      }
      catch (Exception e) {
        logger.error("Error parsing search results: " + e.getMessage());
        e.printStackTrace();
      }
    }
    
    return pastaFormatXML;
  }
    
}
