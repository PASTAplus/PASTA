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

/** 
 * The AdvancedSearchQueryTerm class holds the data needed to produce a xml 
 * string fragment representing a single queryterm in a querygroup.
 */
public class AdvancedSearchQueryTerm  {
  
  /*
   * Class fields
   */

  
  /*
   * Instance fields
   */
  private final String caseSensitive;//Case sensitive setting, "true" or "false"
  private final String indent;       // String of spaces for indenting output
  private final int initialLength = 100; // Initial length of the stringBuffer
  private final String pathExpr;         // The search field, e.g. "keyword"
  private final String searchMode;   // The search mode, e.g. "less-than"
  private StringBuffer stringBuffer; // Holds the queryterm xml string
  private final String value;        // The search value to match, e.g. "35"


  /*
   * Constructors
   */
  
  /**
   * Initializes searchMode, caseSensitive, value, and indent.
   * 
   * @param searchMode       The search mode, e.g. "less-than-equals"
   * @param caseSensitive    Case sensitive setting, "true" or "false"
   * @param pathExpr         The search field, e.g. "northBoundingCoordinate"
   * @param value            The search value to match, e.g. "35"
   * @param indent           String of spaces for indenting output
   */
  public AdvancedSearchQueryTerm(final String searchMode, 
                                 final String caseSensitive, 
                                 final String pathExpr, 
                                 final String value, 
                                 final String indent
                         ) {
    this.searchMode = searchMode;
    this.caseSensitive = caseSensitive;
    this.pathExpr = pathExpr;
    this.value = value;
    this.indent = indent;
    stringBuffer = new StringBuffer(initialLength);
  }
  
  
  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */

  /**
   * Produce a xml string fragment that represents this queryterm.
   * 
   * @return    A xml string fragment that represents this queryterm.
   */
  public String toString() {
    stringBuffer.append(indent + 
                        "<queryterm searchmode=\"" + 
                        searchMode + 
                        "\" casesensitive=\"" + 
                        caseSensitive + 
                        "\">\n"
                       );

    stringBuffer.append(indent + "  <value>" + value + "</value>\n");

    // For a simple search or a browse search, the pathExpr string will be "".
    if (!pathExpr.equals("")) {
      stringBuffer.append(indent + "  <pathexpr>" + pathExpr + "</pathexpr>\n");
    }
    
    stringBuffer.append(indent + "</queryterm>\n");

    return stringBuffer.toString();
  }

}