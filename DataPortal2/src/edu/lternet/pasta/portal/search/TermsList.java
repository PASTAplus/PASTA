/*
 *
 * $Date: 2012-06-22 12:23:25 -0700 (Fri, 22 Jun 2012) $
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

import java.util.TreeSet;


/**
 * Generates HTML for displaying a list of terms used in a search.
 * 
 * @author dcosta
 *
 */
public class TermsList {
  
  /*
   * Class variables
   */
  
  
  /*
   * Instance variables
   */
  
  private TreeSet<String> terms;
  
  /*
   * Constructors
   */
  
  public TermsList() {
    terms = new TreeSet<String>();
  }
  
  
  /*
   * Class methods
   */
  
  
  /*
   * Instance methods
   */
  
  /**
   * Adds a term to the terms list.
   */
  public void addTerm(String term) {
    if (term != null) {
      terms.add(term);
    }
  }
  
  
  /**
   * Returns the size of the terms list
   * 
   * @return  the size value
   */
  public int size() {
	  return terms.size();
  }


  /**
   * Returns an HTML string for presentation of the terms list
   */
  public String toHTML() {
    String termsListHTML = null;
    StringBuilder stringBuilder = new StringBuilder("");
    stringBuilder.append("<p>Terms used in this search: ");
    for (String term : terms) {
      stringBuilder.append("<b>" + term + "</b>, ");
    }
    termsListHTML = stringBuilder.toString();
    if (termsListHTML.length() > 2) {
      termsListHTML = termsListHTML.substring(0, termsListHTML.length() - 2);
    }
    termsListHTML += "</p>";   
    
    return termsListHTML;
  }
  
}
