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
 * The AdvancedSearchPathQuery class holds the data needed to produce a 
 * valid PathQuery XML string.
 */
public class AdvancedSearchPathQuery  {
  
  /*
   * Class fields
   */
 
  
  /*
   * Instance fields
   */
  private String indent;                       // String of spaces
  private final int initialLength = 500;       // Initial length of stringBuffer
  private StringBuffer stringBuffer;           // Holds the pathquery xml
  private ArrayList<String> returnFieldList = new ArrayList<String>(); 
                                              // List of returnfields
  private AdvancedSearchQueryGroup queryGroup;         // The outer query group
  private String title;                                // The pathquery title
  

  /*
   * Constructors
   */
  
  /**
   * Initializes the pathquery title, the main query group, and the
   * indent string.
   * 
   * @param title         the title of the pathquery
   * @param queryGroup    the main query group
   * @param indent        a string of spaces used for indenting output
   */
  public AdvancedSearchPathQuery(final String title, 
                                 final AdvancedSearchQueryGroup queryGroup, 
                                 final String indent) {
    this.title = title;
    this.queryGroup = queryGroup;
    this.indent = indent;
    addReturnField("dataset/title");
    addReturnField("originator/individualName/surName");
    addReturnField("dataset/creator/individualName/surName");
    addReturnField("originator/organizationName");
    addReturnField("dataset/creator/organizationName");
    addReturnField("keyword");
    addReturnField("entityName");
    addReturnField("dataset/distribution/online/url");
    addReturnField("physical/distribution/online/url");
    addReturnField("dataset/abstract/para");
    addReturnField("dataset/abstract/section/para");
    addReturnField("dataset/abstract/para/literalLayout");
  }
  
  
  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */

  /**
   * Adds a returnfield to the pathquery xml.
   * 
   * @param s       the name of the returnfield to add
   */
  public void addReturnField(final String s) {
    returnFieldList.add(s);
  }
  

  /**
   * Set the queryGroup instance field to a new value
   * 
   * @param qg   the new value for the queryGroup instance field
   */
  public void setQueryGroup(AdvancedSearchQueryGroup qg) {
    this.queryGroup = qg;
  }
  

  /**
   * Creates the pathquery xml string.
   * 
   * @return  a string holding the PathQuery XML.
   */
  public String toString() {
    String returnField;

    stringBuffer = new StringBuffer(initialLength);
    stringBuffer.append("<?xml version=\"1.0\"?>\n");
    stringBuffer.append("<pathquery version=\"1.2\">\n");
    stringBuffer.append(indent + "<querytitle>" + 
                        title + "</querytitle>\n");

    for (int i = 0; i < returnFieldList.size(); i++) {
      returnField = returnFieldList.get(i);
      stringBuffer.append(indent + "<returnfield>" + 
                          returnField + "</returnfield>\n");
    }
    
    stringBuffer.append(queryGroup.toString());
    stringBuffer.append("</pathquery>\n");

    return stringBuffer.toString();
  }
  
}