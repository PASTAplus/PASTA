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

import java.util.ArrayList;



/**
 * A BrowseGroup holds a set of browse terms, or, a set of other BrowseGroup
 * objects. (Thus, this class has a recursive aspect to it.)
 * 
 */
public class BrowseGroup {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  
  //List of browse groups
  private ArrayList<BrowseGroup> browseGroups = null;   
  
  //List of browse terms                                        
  private ArrayList<BrowseTerm> browseTerms = null;    
       
  //The name of this browse group, e.g. "Habitat"
  private final String value;              
                                           


  /*
   * Constructors
   */
  
  public BrowseGroup(String s) {
    browseGroups = new ArrayList<BrowseGroup>();
    browseTerms = new ArrayList<BrowseTerm>();
    value = s;
  }

  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
 
  /**
   * Adds a browse group to this browse group's list of browse groups.
   * 
   * @param browseGroup   the BrowseGroup object to be added
   */
  public void addBrowseGroup(BrowseGroup browseGroup) {
    browseGroups.add(browseGroup);
  }

  
  /**
   * Adds a browse term to this browse group's list of browse terms.
   * 
   * @param browseTerm    the BrowseTerm object to be added
   */
  public void addBrowseTerm(BrowseTerm browseTerm) {
    browseTerms.add(browseTerm);
  }
  
  
  /**
   * Get all browse terms that are descendants of this browse group.
   * 
   * @return   A list of all BrowseTerm objects that are descendants of this
   *           browse group.
   */
  ArrayList<BrowseTerm> getBrowseTerms() {
    ArrayList<BrowseTerm> arrayList = new ArrayList<BrowseTerm>();
    
    if (hasTerms()) {
      arrayList = browseTerms;
    }
    else {
      for (int i = 0; i < browseGroups.size(); i++) {
        BrowseGroup browseGroup = browseGroups.get(i);
        ArrayList<BrowseTerm> browseGroupTerms = browseGroup.getBrowseTerms();
        
        for (int j = 0; j < browseGroupTerms.size(); j++) {
          arrayList.add(browseGroupTerms.get(j));
        }
      }
    }
    
    return arrayList;
  }

  
  /**
   * Checks all the browse terms that are descendants of this browse group. If
   * one of them matches the browse value, returns the XML that is
   * stored in the browse cache for that browse value. Note that this method
   * will only be called on the top-most browse group, since it is guaranteed
   * to contain all the browse terms in the cache.
   * 
   * @param browseValue  The browse value, e.g. "bird".
   * @return resultSet   An XML string representing the search results for this browse value
   */
  public String getResultSet(String browseValue) {
    ArrayList<BrowseTerm> arrayList = getBrowseTerms();
    String resultSet = "<?xml version=\'1.0\'?><resultset></resultset>\n";
    
    for (int i = 0; i < arrayList.size(); i++) {
      BrowseTerm browseTerm = arrayList.get(i);
      String browseTermValue = browseTerm.getValue();
      if (browseValue.equalsIgnoreCase(browseTermValue)) {
        resultSet = browseTerm.readSearchResults();
        break;
      }
    }
    
    return resultSet;
  }

  
  /**
   * Getter method for 'value'.
   * 
   * @return   value
   */
  public String getValue() {
    return value;
  }

  
  /**
   * Boolean to determine whether this browse group contains browse term
   * children.
   * 
   * @return  true if there are immediate browse terms, false if this browse
   *          group is a container for other browse groups
   */
  public boolean hasTerms() {
    return (browseTerms.size() > 0);
  }

  
  /**
   * Counts the number of datasets matching the browse terms contained
   * within this browse group (whether directly or indirectly).
   * 
   * @return  matchCount, an int indicating the total number of dataset matches.
   */
  public int matchCount() {
    int matchCount = 0;
    
    if (hasTerms()) {
      for (int i = 0; i < browseTerms.size(); i++) {
        BrowseTerm browseTerm = browseTerms.get(i);
        matchCount += browseTerm.getMatchCount();
      }
    }
    else {
      for (int i = 0; i < browseGroups.size(); i++) {
        BrowseGroup browseGroup = browseGroups.get(i);
        matchCount += browseGroup.matchCount();
      }
    }
    
    return matchCount;
  }
  
 
  /**
   * Convert this browse group into a HTML <table> used for displaying the name
   * of this browse group, e.g. "Habitat".
   * 
   * @return     a HTML string holding a <table> element
   */
  public String toHTML() {
    StringBuffer stringBuffer = new StringBuffer("");
    
    if (hasTerms()) {
      stringBuffer.append("<tr>\n");
      stringBuffer.append("  <td class=\"searchcat\">" + getValue());
      stringBuffer.append("  </td>\n</tr>\n");
      stringBuffer.append("<tr>\n");
      stringBuffer.append("  <td class=\"searchsubcat\">\n");
      
      for (int i = 0; i < browseTerms.size(); i++) {
        BrowseTerm browseTerm = browseTerms.get(i);
        stringBuffer.append("    " + browseTerm.toHTML());
        
        if (i < browseTerms.size() - 1) {
          stringBuffer.append(", ");
        }
        
        stringBuffer.append("\n");
      }
      
      stringBuffer.append("  </td>\n");
      stringBuffer.append("</tr>\n");
    }
    else {
      for (int i = 0; i < browseGroups.size(); i++) {
        BrowseGroup browseGroup = browseGroups.get(i);
        stringBuffer.append(browseGroup.toHTML());
        
        if (i < browseGroups.size() - 1) {
          //stringBuffer.append("<tr><td>&nbsp;</td></tr>\n");
        }
      }
    }
    
    return stringBuffer.toString();
  }
  

  /**
   * Converts this browse group to a string. Used in writing out the browse
   * cache to disk.
   */
  public String toString() {
    String browseTermString;
    String cacheString;
    StringBuffer stringBuffer = new StringBuffer("");
    
    stringBuffer.append("  <group>\n");
    stringBuffer.append("    <value>" + value + "</value>\n");

    if (hasTerms()) {
      stringBuffer.append("    <terms>\n");
      for (int i = 0; i < browseTerms.size(); i++) {
        BrowseTerm browseTerm = browseTerms.get(i);
        browseTermString = browseTerm.generateCacheString();
        if (!browseTermString.equals("")) {
          stringBuffer.append(browseTermString);
        }
      }
      stringBuffer.append("    </terms>\n");
    }
    else {
      for (int i = 0; i < browseGroups.size(); i++) {
        BrowseGroup browseGroup = browseGroups.get(i);
        stringBuffer.append(browseGroup.toString());
      }
    }
    
    stringBuffer.append("</group>\n");
    cacheString = stringBuffer.toString();
    return cacheString;
  }

}
