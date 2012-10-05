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
import java.util.TreeSet;

import org.apache.log4j.Logger;


/**
 * AdvancedSearchQueryGroup holds the data needed to produce a valid querygroup 
 * string. A querygroup is composed of one or more querygroups and/or 
 * queryterms.
 */
public class AdvancedSearchQueryGroup  {
  
  /*
   * Class fields
   */
  
  /*
   * Any search term that is this length or shorter will use a
   * search mode of "exact" instead of "contains".
   */
  public static final int EXACT_SEARCH_MAXIMUM_LENGTH = 3;
  
  
  /*
   * This array holds a list of indexed fields that should be used for a
   * quick search (as opposed to a full document search). These fields should
   * be a subset of those that appear in the indexPaths property in the
   * 'build.properties' file.
   */
  private static String[] INDEXED_FIELDS = {
      "@packageId",
      //"@system",
      "dataset/abstract",
      "dataset/abstract/para",
      "dataset/abstract/section/para",
      "dataset/title",
      //"geographicDescription",
      //"givenName",
      "keyword",
      //"organizationName",
      "surName",
      //"taxonRankValue"
  };
  
  private static final Logger logger = 
    Logger.getLogger(AdvancedSearchQueryGroup.class);
  
  
  /*
   * Instance fields
   */
  private boolean includeOuterQueryGroup = true;
  private String indent;                // String of spaces for indenting output
  private final int initialLength = 500; // Initial length of the stringBuffer
  private String operator;              // "INTERSECT" or "UNION" operator
  private StringBuffer stringBuffer;    // Holds the querygroup string
  private ArrayList<AdvancedSearchQueryGroup> queryGroupList = 
               new ArrayList<AdvancedSearchQueryGroup>(); // List of querygroups
  private ArrayList<AdvancedSearchQueryTerm> queryTermList = 
               new ArrayList<AdvancedSearchQueryTerm>();  // List of queryterms


  /*
   * Constructors
   */
  
  /**
   * Initializes the operator and the indent.
   * 
   * @param operator       Must be either "INTERSECT" or "UNION"
   * @param indent         A string of spaces for indenting the xml output
   */
  public AdvancedSearchQueryGroup(final String operator, final String indent) {
    this.operator = operator;
    this.indent = indent;
    
    if (!((operator.equals("INTERSECT")) || (operator.equals("UNION")))) {
      logger.error("Invalid AdvancedSearchQueryGroup operator: " + operator);
    }
  }

  
  /*
   * Class methods
   */

  /**
   * Factory method to make an AdvancedSearchQueryGroup object that has
   * a <pathexpr> value for a sub-set of the indexed fields that are considered
   * important. This can be used by the simple search page (when user has the
   * "quick search" radio button selected), by the browse search page, etc.
   * 
   * @param  caseSensitive    the 'caseSensitive' setting placed in each query
   *                          term that is passed to the Metacat squery
   * @param  valueSet         a set of search values, for example:
   *                          ['birds', 'fishes']
   * @param  indent           a string of blank characters for pretty-printing
   *                          the query group with an appropriate indent level
   */
  public static AdvancedSearchQueryGroup createIndexedQuery(
                                           String caseSensitive, 
                                           TreeSet<String> valueSet,
                                           String indent) {
    String emlField;
    String operator = "UNION";
    AdvancedSearchQueryGroup qg =new AdvancedSearchQueryGroup(operator, indent);
    AdvancedSearchQueryTerm qt;

    indent = indent + "  ";
    
    for (String value : valueSet) {
      if ((value != null) && (!value.equals(""))) {
        String searchMode = (value.length() <= EXACT_SEARCH_MAXIMUM_LENGTH)
                            ? "equals" : "contains";
      
        for (int i = 0; i < INDEXED_FIELDS.length; i++) {
          emlField = INDEXED_FIELDS[i];
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       value, indent);
          qg.addQueryTerm(qt);
        }
      }
    }
    
    return qg;
  }
  

  /**
   * Gets the array of index field names that are considered important for
   * doing a "quick search".
   * 
   * @return     The string array of indexed fields used for a quick search.
   */
  public static String[] getIndexedFields() {
    return INDEXED_FIELDS;
  }

  
  /*
   * Instance methods
   */

  /**
   * Adds a AdvancedSearchQueryGroup to this AdvancedSearchQueryGroup's list of 
   * querygroups.
   * 
   * @param queryGroup   The AdvancedSearchQueryGroup object to be added to 
   *                     the list.
   */
  public void addQueryGroup(final AdvancedSearchQueryGroup queryGroup) {
    queryGroupList.add(queryGroup);
  }
  

  /**
   * Adds a AdvancedSearchQueryTerm to this AdvancedSearchQueryGroup's list of 
   * queryterms.
   * 
   * @param queryTerm   The AdvancedSearchQueryTerm object to be added to the 
   *                    list.
   */
  public void addQueryTerm(final AdvancedSearchQueryTerm queryTerm) {
    queryTermList.add(queryTerm);
  }
  
 
  /**
   * Sets the boolean value of includeOuterQueryGroup. This enables an
   * optimization. If the user enter search values for only one part of the
   * advanced search form, then includeOuterQueryGroup can be set to false.
   * When false, the QueryGroup object will omit the outer query group from
   * the PathQuery, resulting in a less nested SQL statement.
   * 
   * @param b  When false, allows the outer QueryGroup to be stripped off,
   *           resulting in a less nested SQL statement.
   */
  public void setIncludeOuterQueryGroup(boolean b) {
    this.includeOuterQueryGroup = b;
  }
  

  /**
   * Creates the XML string that represents this AdvancedSearchQueryGroup, 
   * including the querygroups and queryterms that are descendants of this 
   * querygroup.
   * 
   * @return    A XML string fragment representing this querygroup.
   */
  public String toString() {
    AdvancedSearchQueryGroup queryGroup;
    AdvancedSearchQueryTerm queryTerm;
    
    stringBuffer = new StringBuffer(initialLength);
    
    if (includeOuterQueryGroup == true) {
      stringBuffer.append(indent + 
                          "<querygroup operator=\"" + operator + "\">\n");
    }
    
    for (int i = 0; i < queryGroupList.size(); i++) {
      queryGroup = queryGroupList.get(i);
      stringBuffer.append(queryGroup.toString());
    }

    for (int i = 0; i < queryTermList.size(); i++) {
      queryTerm = queryTermList.get(i);
      stringBuffer.append(queryTerm.toString());
    }
    
    if (includeOuterQueryGroup == true) {
      stringBuffer.append(indent + "</querygroup>\n");
    }

    return stringBuffer.toString();
  }
  
}