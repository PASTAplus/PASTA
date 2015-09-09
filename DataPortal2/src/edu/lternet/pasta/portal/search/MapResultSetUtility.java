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

package edu.lternet.pasta.portal.search;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.xpath.CachedXPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * @author Duane Costa
 * @since June 5, 2015
 * 
 *        The MapResultSetUtility class parses XML search results and
 *        stores them in a JavaScript array that can be utilized by
 *        Google Maps and its marker clusterer component.
 * 
 */
public class MapResultSetUtility {

  /*
   * Class variables
   */

  private static final Logger logger = Logger
      .getLogger(edu.lternet.pasta.portal.search.MapResultSetUtility.class);
  
  public static final String PACKAGEID_PATH = "packageid";
  public static final String TITLE_PATH = "title";
  public static final String PUBDATE_PATH = "pubdate";
  public static final String COORDINATES_PATH = "spatialCoverage/coordinates";

  
  /*
   * Instance variables
   */

  private Integer numFound = 0;
  

  /*
   * Constructors
   */

  /*
   * Methods
   */
  
  
  	public Integer getNumFound() {
  		return numFound;
  	}
  
  
	/*
	dataPackages = 
    "[\n" +
	"{'packageId':'knb-lter-abc.1.2', 'title':'Rubber Soul', 'pubDate': '1965', 'location': {'Latitude':34.8, 'Longitude': 67.816667}},\n" +
	"{'packageId':'knb-lter-abc.2.2', 'title':'Rubber Soul', 'pubDate': '1966', 'location': {'Latitude':31.55, 'Longitude': 64.366667}},\n" +
	"{'packageId':'knb-lter-abc.3.2', 'title':'Rubber Soul', 'pubDate': '1967', 'location': {'Latitude':34.533333, 'Longitude': 65.266667}},\n" +
	"{'packageId':'knb-lter-abc.4.2', 'title':'Rubber Soul', 'pubDate': '1968', 'location': {'Latitude':38.466667, 'Longitude': 70.883333}},\n" +
	"{'packageId':'knb-lter-abc.5.2', 'title':'Rubber Soul', 'pubDate': '1969', 'location': {'Latitude':37.083333, 'Longitude': 70.5}},\n" +
	"{'packageId':'knb-lter-abc.6.2', 'title':'Rubber Soul', 'pubDate': '1970', 'location': {'Latitude':32.366667, 'Longitude': 62.166667}},\n" +
	"{'packageId':'knb-lter-abc.7.2', 'title':'Rubber Soul', 'pubDate': '1971', 'location': {'Latitude':33.616667, 'Longitude': 69.116667}},\n" +
	"{'packageId':'knb-lter-abc.8.2', 'title':'Rubber Soul', 'pubDate': '1972', 'location': {'Latitude':-18.966667, 'Longitude': 32.45}},\n" +
	"{'packageId':'knb-lter-abc.9.2', 'title':'Rubber Soul', 'pubDate': '1973', 'location': {'Latitude':-18.095833, 'Longitude': 25.833889}}\n" +
	"];\n";
	*/
  	
  	
  	/**
  	 * Using search results XML as input, compose a JavaScript array that contains
  	 * the data necessary to render the search results in a Google Maps script
  	 * that uses the Marker Clusterer component.
  	 * 
  	 * @param    xml  search resuts XML string
  	 * @return   a string representation of the JavaScript array
  	 */
  	public String parseResultSet(String xml) { 	   
  		String jsArray = "";
  		StringBuilder jsArrayBuilder = new StringBuilder("[\n");
  		
  		if (xml != null) {
  			InputStream inputStream = null;
  			try {
  				inputStream = IOUtils.toInputStream(xml, "UTF-8");
  				DocumentBuilder documentBuilder = 
  	              DocumentBuilderFactory.newInstance().newDocumentBuilder();
  				CachedXPathAPI xpathapi = new CachedXPathAPI();

  				Document document = null;
  				document = documentBuilder.parse(inputStream);
  	      
  				if (document != null) { 	        
  					Node numFoundNode = null;
  					numFoundNode = xpathapi.selectSingleNode(document, "//resultset/@numFound");
  					if (numFoundNode != null) {
  						String numFoundStr = numFoundNode.getNodeValue();
  						this.numFound = new Integer(numFoundStr);
  					}
  					
  					NodeList dataPackageNodeList = xpathapi.selectNodeList(document, "//resultset/document");
  			        
  					if (dataPackageNodeList != null) {
  						Set<String> coordinatesSet = new TreeSet<String>();
  						for (int i = 0; i < dataPackageNodeList.getLength(); i++) {
  							Node dataPackageNode = dataPackageNodeList.item(i);
  							
  							String packageIdField = null;
  							String titleField = null;
  							String titleLinkField = null;
  							String pubDateField = null;
  							String locationField = null;

  							NodeList coordinatesNodeList = xpathapi.selectNodeList(dataPackageNode, COORDINATES_PATH);

  							/*
  							 * If we can't determine the coordinates, we can't map the
  							 * data package, so just continue to the next data package
  							 */
  							if (coordinatesNodeList != null) {
  								if (coordinatesNodeList.getLength() > 0) {
  									for (int j = 0; j < coordinatesNodeList.getLength(); j++) {
  										if (j == 1) break;
  										Node coordinatesNode = coordinatesNodeList.item(j);
  										String coordinates = coordinatesNode.getTextContent();
  										boolean useOffset = false;
  										locationField = composeLocationField(coordinates, coordinatesSet, useOffset);
  										if (locationField == null) {
  											continue;
  										}
  										else {
  											coordinatesSet.add(locationField);
  										}
  									}
  								}
  								else {
  									continue;
  								}
  							}
  							else {
  								continue;
  							}
  							
  							String packageId = null;
  							Node packageIdNode = xpathapi.selectSingleNode(dataPackageNode, PACKAGEID_PATH);
  							if (packageIdNode != null) {
  								packageId = packageIdNode.getTextContent();
  								packageIdField = composePackageIdField(packageId);
  							}

  							Node titleNode = xpathapi.selectSingleNode(dataPackageNode, TITLE_PATH);
  							if (titleNode != null) {
  								String title = titleNode.getTextContent();
  								titleField = composeTitleField(title);
  								if (packageId != null) {
  									titleLinkField = composeTitleLinkField(title, packageId);
  								}
  							}
  							
  							Node pubDateNode = xpathapi.selectSingleNode(dataPackageNode, PUBDATE_PATH);
  							if (pubDateNode != null) {
  								String pubDate = pubDateNode.getTextContent();
  								pubDateField = composePubDateField(pubDate);
  							}
  							
  							String jsArrayElement = 
  									composeArrayElement(packageIdField, titleField, titleLinkField, pubDateField, locationField);
  							jsArrayBuilder.append(jsArrayElement);
  						}
  			        }
  				}
  			}
  			catch (Exception e) {
  		        logger.error("Error parsing search result set: " + e.getMessage());
  			}
  			finally {
  				if (inputStream != null) {
  					try {
  						inputStream.close();
  					}
  					catch (IOException e) {
  						;
  					}
  				}
  			}
  		}
  		
  		jsArrayBuilder.append("];\n");
  		jsArray = jsArrayBuilder.toString();
  		
  		return jsArray;
  	}
  	
  	
  	// Example:
	//"{'packageId':'knb-lter-abc.9.2', 'title':'Rubber Soul', 'pubDate': '1973', 'location': {'Latitude':-18.095833, 'Longitude': 25.833889}}\n" +
  	private String composeArrayElement(String packageId, String title, String titleLink, String pubDate, String location) {
  		String arrayElement = "";
  		
  		arrayElement = String.format("{%s, %s, %s, %s, %s},\n", packageId, title, titleLink, pubDate, location);
  		
  		return arrayElement;
  	}
  	
  	
  	private String composePackageIdField(String packageId) {
  		String field = "";
  		
  		String html = String.format("<a href=\"./mapbrowse?packageid=%s\">%s</a>", packageId, packageId);
  		field = String.format("'packageId':'%s'", html);
  		
  		return field;
  	}

  	
  	private String composeTitleField(String title) {
  		String field;
  		String markerTitle = "";
  		int titleLimit = 200;
  		
  		if (title != null) {
  			String escapedTitle = title.replace("'", "\\'");
  			escapedTitle = escapedTitle.replace("\n", "");
  			if (escapedTitle.length() > titleLimit) {
  				markerTitle = String.format("%s...", escapedTitle.substring(0, titleLimit));
  			}
  			else {
  				markerTitle = escapedTitle;
  			}
   		}
  		
 		field = String.format("'title':'%s'", markerTitle);
  		return field;
  	}

  	
  	private String composeTitleLinkField(String title, String packageId) {
  		String field;
  		String markerTitle = "";
  		int titleLimit = 200;
  		
  		if (title != null) {
  			String escapedTitle = title.replace("'", "\\'");
  			escapedTitle = escapedTitle.replace("\n", "");
  			if (escapedTitle.length() > titleLimit) {
  				markerTitle = String.format("%s...", escapedTitle.substring(0, titleLimit));
  			}
  			else {
  				markerTitle = escapedTitle;
  			}
   		}
  		
  		String titleLink = String.format("<a href=\"./mapbrowse?packageid=%s\">%s</a>", packageId, markerTitle);
 		field = String.format("'titleLink':'%s'", titleLink);
  		return field;
  	}

  	
  	private String composePubDateField(String pubDate) {
  		String field = "";
  		
  		field = String.format("'pubDate':'%s'", pubDate);
  		
  		return field;
  	}

  	
  	/*
  	 * Example coordinates argument string:
  	 *     "-124.3983126 43.625394 -121.3531372 45.5751826"
  	 *      W bound      S bound   E bound      N bound
  	 */
  	private String composeLocationField(String coordinates, Set<String> coordinatesSet, boolean useOffset) {
  		String field = null;
  		double lat = 0;
  		double lon = 0;
  		
  		if (coordinates != null && coordinates.contains(" ")) {
  			String[] tokens = coordinates.split(" ");
  			if (tokens.length == 4) {
  				String wStr = tokens[0];
  				String sStr = tokens[1];
  				String eStr = tokens[2];
  				String nStr = tokens[3];
				try {			
					if (wStr.equals(eStr)) {
						lon = Double.parseDouble(wStr);
					}
					else {
						double wLon = Double.parseDouble(wStr);
						double eLon = Double.parseDouble(eStr);
						lon = avg(wLon, eLon);
					}
					
					if (sStr.equals(nStr)) {
						lat = Double.parseDouble(sStr);
					}
					else {
						double sLat = Double.parseDouble(sStr);
						double nLat = Double.parseDouble(nStr);
						lat = avg(sLat, nLat);
					}
				}
				catch (NumberFormatException e) {
					e.printStackTrace();
					return null;
				}
				
				/*
				 * If we need to use an offset, tweak the values a little bit
				 * to make them unique
				 */
				if (useOffset) {
		            lat = lat + (Math.random() - 0.5) / 1500;
		            lon = lon + (Math.random() - 0.5) / 1500;
				}

				field = String.format("'location': {'latitude':%f, 'longitude': %f}", lat, lon);
				
		  		/*
		  		 * Ensure that we have a unique pair of coordinates. If we don't, call
		  		 * this method recursively using an offset.
		  		 */
		  		if (coordinatesSet.contains(field)) {
		  			useOffset = true;
		  			field = composeLocationField(coordinates, coordinatesSet, useOffset);
		  		}
  			}
  		}
  		
  		return field;
  	}
  	
  	
  	/*
  	 * Calculate the average of two double values
  	 */
  	private double avg(double d1, double d2) {
  		return (d1 + d2) / 2.0;
  	}

}
