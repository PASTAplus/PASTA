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

package edu.lternet.pasta.portal.search;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;


/** 
 * The BrowseSearch class supports browse-based search operations. This is equivalent
 * to a simple search, but the user clicks on a link to determine the search 
 * term. Browse search uses the search results stored in a browse cache (as
 * implemented by the BrowseGroup class) for high performance.
 */
public class BrowseSearch {

  /*
   * Class fields
   */
  
  /*
   * Default value of the browse cache directory is below, but it can be set to a
   * different value in the dataportal.properties file
   */
  public static String browseCacheDir = "/home/pasta/local/browse";     
  private static final String browseKeywordFilename = "browseKeyword.xml";
  public static String browseKeywordPath = browseCacheDir + "/" + browseKeywordFilename;
  private static final Logger logger = Logger.getLogger(BrowseSearch.class);
  static final long serialVersionUID = 0;  // Needed for Eclipse warning.


  /*
   * Instance fields
   */
  
  
  /*
   * Constructors
   */
  


  /*
   * Class methods
   */
  
  /**
   * Sets the value of the browse directory and related path value
   * @param  directoryPath   the browse directory path value to set
   */
  public static void setBrowseCacheDir(String directoryPath) {
    BrowseSearch.browseCacheDir = directoryPath;
    BrowseSearch.browseKeywordPath = String.format("%s/%s", directoryPath, browseKeywordFilename);
  }
  

  /*
   * Instance methods
   */
 
  /**
   * Reads the browse cache file into a XML DOM tree and parses it.
   * 
   * @param  browseCacheFile     the browse cache File object
   * @return topBrowseGroup, a BrowseGroup object holding the browse cache
   */
  public BrowseGroup readBrowseCache(File browseCacheFile) {
    Document document;
    DocumentBuilder documentBuilder;
    DocumentBuilderFactory documentBuilderFactory =
                                           DocumentBuilderFactory.newInstance(); 
    Element documentElement;
    Node documentNode;
    NodeList documentNodeList;
    Element groupElement;
    Node groupNode;
    NodeList groupNodeList;
    Text text;
    BrowseGroup topBrowseGroup = null;     // The BrowseGroup that is returned

    Element topElement;

    try {
      documentBuilder = documentBuilderFactory.newDocumentBuilder();
      document = documentBuilder.parse(browseCacheFile);
      documentElement = document.getDocumentElement();
      documentNodeList = documentElement.getChildNodes();
      
      for (int i = 0; i < documentNodeList.getLength(); i++) {
        documentNode = documentNodeList.item(i);
        
        if (documentNode instanceof Element) {
          topElement = (Element) documentNode;
          
          if (topElement.getTagName().equals("group")) {
            groupNodeList = topElement.getChildNodes();

            for (int j = 0; j < groupNodeList.getLength(); j++) {
              String value = null;
              groupNode = groupNodeList.item(j);
              
              if (groupNode instanceof Element) {
                groupElement = (Element) groupNode;

                /*
                 * If we encounter a value element, use the text of the value
                 * element to construct the top BrowseGroup, which will be
                 * the return value of this method.
                 */
                if (groupElement.getTagName().equals("value")) {
                  text = (Text) groupElement.getFirstChild();
                  value = text.getData().trim();
                  topBrowseGroup = new BrowseGroup(value);
                }
                /*
                 * If we encounter a group element, parse the group element,
                 * passing this group element as the parent Element,
                 * and the top BrowseGroup object as the parent BrowseGroup.
                 */
                else if (groupElement.getTagName().equals("group")) {
                  parseGroupElement(groupElement, topBrowseGroup);
                }
              }
            }
          }
        }
      }
    }
    catch (Exception e) {
      logger.error("Exception:\n" + e.getMessage());
      e.printStackTrace();
    }
 
    return topBrowseGroup;
  }
  

  /**
   * Recursive method that parses a <group> element. A group element may contain
   * other <group> elements, or a <terms> elements containing a list of <term>
   * elements.
   *  
   * @param parentGroupElement  a <group> element in the DOM tree
   * @param parentBrowseGroup   a BrowseGroup object representing the parent
   *                            group
   */
  private void parseGroupElement(Element parentGroupElement, 
                                 BrowseGroup parentBrowseGroup) {
    BrowseGroup browseGroup = null;
    BrowseTerm browseTerm = null;

    Element groupElement;
    Node groupNode;
    NodeList groupNodeList;

    Element termsElement;
    Node termsNode;
    NodeList termsNodeList;

    Element termElement;
    Node termNode;
    NodeList termNodeList;

    Text text;
    String textString;
    
    groupNodeList = parentGroupElement.getChildNodes();
    
    for (int i = 0; i < groupNodeList.getLength(); i++) {
      groupNode = groupNodeList.item(i);
      
      if (groupNode instanceof Element) {
        groupElement = (Element) groupNode;
 
        /* 
         * If we encounter a value element, use the text of the value element
         * to create a new BrowseGroup, then add the new BrowseGroup to the
         * parent BrowseGroup
         */
        if (groupElement.getTagName().equals("value")) {
          text = (Text) groupElement.getFirstChild();
          textString = text.getData().trim();
          if (parentBrowseGroup instanceof LTERSiteBrowseGroup) {
        	  browseGroup = new LTERSiteBrowseGroup(textString);
          }
          else {
        	  browseGroup = new BrowseGroup(textString);
          }
          parentBrowseGroup.addBrowseGroup(browseGroup);
        }
        /*
         * If we encounter a group element, parse the group element by calling
         * this method recursively. The current BrowseGroup object is passed in
         * as the parent BrowseGroup.
         */
        else if (groupElement.getTagName().equals("group")) {
          parseGroupElement(groupElement, browseGroup);         // Recursive!
        }
        /*
         * If we encounter a terms element, then this is a terminal browse
         * group. Parse each term element within the terms element.
         */
        else if (groupElement.getTagName().equals("terms")) {
          termsNodeList = groupElement.getChildNodes();
                
          for (int j = 0; j < termsNodeList.getLength(); j++) {
            termsNode = termsNodeList.item(j);
                  
            if (termsNode instanceof Element) {
              termsElement = (Element) termsNode;
                    
              if (termsElement.getTagName().equals("term")) {
                termNodeList = termsNode.getChildNodes();
                      
                for (int k = 0; k < termNodeList.getLength(); k++) {
                  termNode = termNodeList.item(k);
                        
                  if (termNode instanceof Element) {
                    termElement = (Element) termNode;
                          
                    if (termElement.getTagName().equals("value")) {
                      text = (Text) termElement.getFirstChild();
                      textString = text.getData().trim();
                      browseTerm = new BrowseTerm(textString);
                      browseGroup.addBrowseTerm(browseTerm);
                    }
                    else if (termElement.getTagName().equals("matchCount")) {
                      text = (Text) termElement.getFirstChild();
                      textString = text.getData().trim();
                      Integer integer = new Integer(textString);
                      int matchCount = integer.intValue();
                      browseTerm.setMatchCount(matchCount);
                    }
                  } 
                }
              }
            }
          }
        }
      }
    }
  }

}
