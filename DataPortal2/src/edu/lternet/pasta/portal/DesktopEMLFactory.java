/*
*
* $Date: 2014-04-21 13:03:20 -0700 (Fri, 29 Apr 2011) $
* $Author: dcosta $
* $Revision: 1026 $
*
* Copyright 2014 the University of New Mexico.
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

import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.lternet.pasta.common.eml.Entity;

/**
 * Class for processing a desktop data harvest. 
 */
public final class DesktopEMLFactory {

	/*
	 * Class variables and constants
	 */
	private static final Logger logger = Logger
			.getLogger(DesktopEMLFactory.class);

	private static final String ENTITY_NAME = "entityName";
	private static final String ENTITY_PATH_PARENT = "//dataset/";
	private static final String OBJECT_NAME = "physical/objectName";
	private static final String ONLINE_URL = "physical/distribution/online/url";
	private static final String OTHER_ENTITY = "otherEntity";
	private static final String PHYSICAL = "physical";
	private static final String SPATIAL_RASTER_ENTITY = "spatialRaster";
	private static final String SPATIAL_VECTOR_ENTITY = "spatialVector";
	private static final String STORED_PROCEDURE_ENTITY = "storedProcedure";
	private static final String TABLE_ENTITY = "dataTable";
	private static final String VIEW_ENTITY = "view";

	
	/*
	 * Instance variables
	 */

	private String urlHead = null;

	String[] ENTITY_TYPES = { OTHER_ENTITY, SPATIAL_RASTER_ENTITY,
			SPATIAL_VECTOR_ENTITY, STORED_PROCEDURE_ENTITY, TABLE_ENTITY,
			VIEW_ENTITY };


	/*
	 * Constructors
	 */

	/**
	 * Constructs a DesktopEMLFactory object for processing a desktop data
	 * harvest.
	 * 
	 * @param urlHead
	 *            The head of the portal URL that will be used for composing
	 *            PASTA-ready distribution online URLs from which the data can
	 *            be accessed by PASTA.
	 */
	public DesktopEMLFactory(String urlHead) {
		this.urlHead = urlHead;
	}


	/*
	 * Class methods
	 */

 
	/*
	 * Instance methods
	 */

	/**
	 * Makes a PASTA-ready, Level-0 EML document from a "desktop" EML document.
	 * 
	 * @param desktopEMLDocument
	 *            the pre-Level-0 EML Document uploaded from the user's desktop
	 * @param entityList
	 *            a list of Entity objects
	 * @return the PASTA-ready Level-0 EML Document object
	 * @throws TransformerException
	 */
	public Document makePastaReady(Document desktopEMLDocument,
			ArrayList<Entity> entityList) throws TransformerException {
		if (desktopEMLDocument == null) {
			throw new TransformerException(
					"A null Document object was passed to the makePastaReady() method");
		}
		
		modifyDataURLs(desktopEMLDocument);

		return desktopEMLDocument;
	}


	/*
	 * Modify any existing data URLs in the EML document, changing them to data
	 * URLs hosted on the LTER NIS Data Portal.
	 */
	private void modifyDataURLs(Document emlDocument)
			throws TransformerException {
		CachedXPathAPI xpathapi = new CachedXPathAPI();

		for (int j = 0; j < ENTITY_TYPES.length; j++) {
			final String entityPath = ENTITY_PATH_PARENT + ENTITY_TYPES[j];

			// Parse the entity
			NodeList entityNodeList = xpathapi.selectNodeList(emlDocument,
					entityPath);

			if (entityNodeList != null) {
				for (int i = 0; i < entityNodeList.getLength(); i++) {
					String entityName = null;
					Node entityNode = entityNodeList.item(i);

					// Get the entityName
					NodeList entityNameNodeList = xpathapi.selectNodeList(
							entityNode, ENTITY_NAME);

					if (entityNameNodeList != null && 
						entityNameNodeList.getLength() > 0) 
					{
						entityName = entityNameNodeList.item(0).getTextContent();
					}
					else {
						throw new TransformerException(
								String.format(
										"No entityName specified for %s entity", 
										ENTITY_TYPES[j]));
					}

					// Get the objectName
					NodeList objectNameNodeList = 
						xpathapi.selectNodeList(entityNode, OBJECT_NAME);
					if (objectNameNodeList != null && 
					    objectNameNodeList.getLength() > 0) 
					{
						final String objectName = 
							objectNameNodeList.item(0).getTextContent();
						final String portalUrl = String.format("%s/%s",
								                               urlHead, objectName);
						// Get the distribution online url information
						NodeList urlNodeList = 
								xpathapi.selectNodeList(entityNode, ONLINE_URL);
						if (urlNodeList != null && urlNodeList.getLength() > 0) {
							final String url = urlNodeList.item(0).getTextContent();
							logger.info(String
									.format("Changing data URL:\n  from: %s\n    to: %s",
											url, portalUrl));
							urlNodeList.item(0).setTextContent(portalUrl);
						}
						else {
							// Get the physical node
							NodeList physicalNodeList = 
									xpathapi.selectNodeList(entityNode, PHYSICAL);
							if (physicalNodeList != null &&
									physicalNodeList.getLength() > 0) 
							{
							    Element physical = (Element) physicalNodeList.item(0);
							    Element distribution = emlDocument.createElement("distribution");
							    Element online = emlDocument.createElement("online");
							    Element url = emlDocument.createElement("url");			
								physical.appendChild(distribution);
							    distribution.appendChild(online);
							    online.appendChild(url);							
							    url.appendChild(emlDocument.createTextNode(portalUrl));
							}
							else {
								throw new TransformerException(
								"A 'physical' element was not specified for entity: " + entityName);
							}
						}
					}
					else {
						throw new TransformerException(
							"No 'objectName' was specified for entity: " + entityName);
					}
				}
			}
			else {
				throw new TransformerException(
						"No entities were found in the EML document");
			}
		}
	}

}
