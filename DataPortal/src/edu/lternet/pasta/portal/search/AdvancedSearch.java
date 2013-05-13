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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import edu.lternet.pasta.client.DataPackageManagerClient;
import edu.lternet.pasta.client.PastaAuthenticationException;
import edu.lternet.pasta.client.PastaConfigurationException;
import edu.lternet.pasta.portal.search.LTERSite;


/**
 * AdvancedSearch class constructs queries that use the pathquery feature of 
 * Metacat. It can execute either an advanced search, where the user fills in
 * fields in a web form, or a simple search on a string.
 */
public class AdvancedSearch  {
  
  /*
   * Class fields
   */
  private static final Logger logger = Logger.getLogger(AdvancedSearch.class);

  
  /*
   * Instance fields
   */
  
  /*
   * Form parameters
   */
  private String caseSensitive;
  private final String creatorOrganization;
  private final String creatorOrganizationQueryType;
  private final String creatorSurname;
  private final String creatorSurnameQueryType;
  private final String dateField;
  private final String startDate;
  private final String endDate;
  private final String globalOperator;
  private boolean hasAuthorSearch = false;
  private boolean hasGeographicDescriptionSearch = false;
  private boolean hasSiteFilter = false;
  private boolean hasSpatialSearch = false;
  private boolean hasSubjectSearch = false;
  private boolean hasTaxonomicSearch = false;
  private boolean hasTemporalSearch = false;
  private int INDENT_LEVEL = 2;
  private boolean isDatesContainedChecked;
  private String namedTimescale;
  private String namedTimescaleQueryType;
  private String[] siteValues = null;
  private String subjectAllAny;
  private String subjectField;
  private String subjectQueryType;
  private String subjectValue;
  private String taxon;
  private String taxonQueryType;
  
  private boolean isBoundaryContainedChecked;
  private String northBound;
  private String southBound;
  private String eastBound;
  private String westBound;
  private String locationName;
  
  private AdvancedSearchPathQuery pathQuery;
  private AdvancedSearchQueryGroup queryGroup;
  private String queryString;
  private TermsList termsList;
  private final String title = "Advanced Search";

  // Controlled vocabulary settings
  private boolean hasExact = false;
  private boolean hasNarrow = false;
  private boolean hasRelated = false;
  private boolean hasNarrowRelated = false;
  private boolean hasAll = false;

  
  /*
   * Constructors
   */

  /**
   * Constructor. Passes in a large set of form field parameters.
   */
  public AdvancedSearch(
      boolean isCaseSensitive,
      String creatorOrganization,
      String creatorOrganizationQueryType,
      String creatorSurname,
      String creatorSurnameQueryType,
      String dateField,
      String startDate,
      String endDate,
      boolean matchAll,
      String namedTimescale,
      String namedTimescaleQueryType,
      String[] siteValues,
      String subjectAllAny,
      String subjectField,
      String subjectQueryType,
      String subjectValue,
      boolean isDatesContainedChecked,
      boolean isSpecificChecked,
      boolean isRelatedChecked,
      boolean isRelatedSpecificChecked,
      String taxon,
      String taxonQueryType,   
      boolean isBoundaryContainedChecked,
      String northBound,
      String southBound,
      String eastBound,
      String westBound,
      String locationName
                       ) { 
    this.creatorOrganization = creatorOrganization;
    this.creatorOrganizationQueryType = creatorOrganizationQueryType;
    this.creatorSurname = creatorSurname;
    this.creatorSurnameQueryType = creatorSurnameQueryType;
    this.dateField = dateField;
    this.startDate = startDate;
    this.endDate = endDate;
    this.isDatesContainedChecked = isDatesContainedChecked;
    this.namedTimescale = namedTimescale;
    this.namedTimescaleQueryType = namedTimescaleQueryType;
    this.siteValues = siteValues;
    this.subjectAllAny = subjectAllAny;
    this.subjectField = subjectField;
    this.subjectQueryType = subjectQueryType;
    this.subjectValue = subjectValue;
    this.taxon = taxon;
    this.taxonQueryType = taxonQueryType;
    
    this.isBoundaryContainedChecked = isBoundaryContainedChecked;
    this.northBound = northBound;
    this.southBound = southBound;
    this.eastBound = eastBound;
    this.westBound = westBound;
    this.locationName = locationName;
    
    this.hasExact = !isSpecificChecked && !isRelatedChecked && !isRelatedSpecificChecked;
    this.hasNarrow = isSpecificChecked && !isRelatedChecked && !isRelatedSpecificChecked;
    this.hasRelated = !isSpecificChecked && isRelatedChecked && !isRelatedSpecificChecked;
    this.hasNarrowRelated = isSpecificChecked && isRelatedChecked && !isRelatedSpecificChecked;
    this.hasAll = isRelatedSpecificChecked;
    
    String indent = getIndent(INDENT_LEVEL * 1);
    
    if (matchAll) {
      globalOperator = "INTERSECT";     
    }
    else {
      globalOperator = "UNION";
    }
    
    this.caseSensitive = isCaseSensitive ? "true" : "false";
    this.queryGroup = new AdvancedSearchQueryGroup(globalOperator, indent);
    this.pathQuery = new AdvancedSearchPathQuery(title, queryGroup, indent);
    this.termsList = new TermsList();
  }

  
  /*
   * Class methods
   */

  
  /*
   * Instance methods
   */

  /**
   * Adds a string to an ArrayList of terms. An auxiliary method to the
   * parseTermsAdvanced() method.
   * 
   * @param terms      ArrayList of strings.
   * @param term       the new string to add to the ArrayList, but only if
   *                   it isn't an empty string.
   */
  private void addTerm(ArrayList<String> terms, final StringBuffer term) {
    final String s = term.toString().trim();
      
    if (s.length() > 0) {
      terms.add(s);
    }
  }


  /**
   * A full subject query searches the title, abstract, and keyword sections of
   * the document. Individual searches on these sections is also supported.
   */
  private void buildQuerySubject(TermsList termsList) {
    String emlField;
    String indent;
    AdvancedSearchQueryGroup innerQuery = null;
    final String outerOperator;
    AdvancedSearchQueryGroup outerQuery;
    AdvancedSearchQueryTerm qt;
    String searchMode;
    ArrayList<String> terms;
 
    if ((this.subjectValue != null) && (!(this.subjectValue.equals("")))) {
      hasSubjectSearch = true;
      
      if (this.subjectAllAny != null && 
          this.subjectAllAny.equals("0")
         ) {
        outerOperator = "INTERSECT";
      }
      else {
        outerOperator = "UNION";
      }

      indent = getIndent(INDENT_LEVEL * 2);
      outerQuery = new AdvancedSearchQueryGroup(outerOperator, indent);
      terms = parseTermsAdvanced(this.subjectValue);
      searchMode = metacatSearchMode(subjectQueryType);
      
      for (String term : terms) {
        indent = getIndent(INDENT_LEVEL * 3);
        innerQuery = new AdvancedSearchQueryGroup("UNION", indent);
        indent = getIndent(INDENT_LEVEL * 4);
        
        TreeSet<String> derivedTerms = new TreeSet<String>();
        TreeSet<String> webTerms = 
            ControlledVocabularyClient.webServiceSearchValues(
                term, hasExact, hasNarrow, 
                hasRelated, hasNarrowRelated, hasAll);
        webTerms = optimizeTermList(webTerms);
      
        for (String webValue : webTerms) {
          derivedTerms.add(webValue);
        }
      
        /*
         * Sometimes the original search term (e.g. "fishes") doesn't need to be 
         * included in the set of search values because it is covered by a
         * substring term returned by the vocabulary web service (e.g. "fish"). 
         * However, if the web service failed to return any values, then we need 
         * to add back the original search term.
         */
        if (webTerms.size() < 1) {
          derivedTerms.add(term);
        }
        
        for (String derivedTerm : derivedTerms) {
          
          termsList.addTerm(derivedTerm);
          
          if (subjectField.equals("ALL") || subjectField.equals("TITLE")) {
            emlField = "dataset/title";
            qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                             derivedTerm, indent);
            innerQuery.addQueryTerm(qt);
          }

          if (subjectField.equals("ALL") || subjectField.equals("ABSTRACT")) {
            emlField = "dataset/abstract/para";
            qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                             derivedTerm, indent);
            innerQuery.addQueryTerm(qt);

            emlField = "dataset/abstract/section/para";
            qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                             derivedTerm, indent);
            innerQuery.addQueryTerm(qt);
          }

          if (subjectField.equals("ALL") || subjectField.equals("KEYWORDS")) {
            emlField = "keyword";
            qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                            derivedTerm, indent);
            innerQuery.addQueryTerm(qt);
          }
        }
      
        outerQuery.addQueryGroup(innerQuery);
      }

      // Minimize the number of query groups that get created, depending on
      // which criteria the user specified.
      //
      if (terms.size() > 1) {
        queryGroup.addQueryGroup(outerQuery);
      }
      else if (terms.size() == 1){
        queryGroup.addQueryGroup(innerQuery);
      }       
    }

  }
  

  /**
   * An author query will search the creator/individualName/surName field, the
   * creator/organizationName field, or an intersection of both fields.
   */
  private void buildQueryAuthor(TermsList termsList) {
    boolean addQueryGroup = false;
    String emlField;
    String indent = getIndent(INDENT_LEVEL * 2);
    AdvancedSearchQueryGroup qg = 
                           new AdvancedSearchQueryGroup(globalOperator, indent);
    AdvancedSearchQueryTerm qt;
    String searchMode;
    String value = this.creatorSurname;

    indent = getIndent(INDENT_LEVEL * 3);
    if ((value != null) && (!(value.equals("")))) {
      emlField = "dataset/creator/individualName/surName";
      searchMode = metacatSearchMode(this.creatorSurnameQueryType);
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       value, indent);
      qg.addQueryTerm(qt);        
      addQueryGroup = true;
      termsList.addTerm(value);
    }

    value = this.creatorOrganization;
      
    if ((value != null) && (!(value.equals("")))) {
      emlField = "creator/organizationName";
      searchMode = metacatSearchMode(this.creatorOrganizationQueryType);
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       value, indent);
      qg.addQueryTerm(qt);        
      addQueryGroup = true;      
      termsList.addTerm(value);
    }
    
    if (addQueryGroup) {      
      hasAuthorSearch = true;
      queryGroup.addQueryGroup(qg);
    }
  }
  

  /**
   * Builds query group for a search on a specific named location.
   */
  private void buildQueryGeographicDescription(String locationName, TermsList termsList) {
    String emlField;
    String indent = getIndent(INDENT_LEVEL * 2);
    final String operator = "INTERSECT";
    AdvancedSearchQueryGroup qgGeo;
    AdvancedSearchQueryTerm qt;
    String searchMode;

    qgGeo = new AdvancedSearchQueryGroup(operator, indent);
    indent = getIndent(INDENT_LEVEL * 3);

    if ((locationName != null) && (!(locationName.equals("")))) {
      searchMode = "contains";
      emlField = "geographicDescription";
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       locationName, indent);
      qgGeo.addQueryTerm(qt);
      queryGroup.addQueryGroup(qgGeo);
      hasGeographicDescriptionSearch = true;  
      termsList.addTerm(locationName);
    }
  }
  
  
  /**
   * Builds query group for spatial search on north/south/east/west bounding
   * coordinates. Includes logic to handle queries across the international
   * date line.
   */
  private void buildQuerySpatial(String northValue, String southValue, 
                                 String eastValue, String westValue,
                                 boolean boundaryContained) {
    boolean crosses;
    AdvancedSearchQueryGroup datelineGroup = null;
    String emlField;
    AdvancedSearchQueryGroup leftOfDateline = null;
    final String operator = "INTERSECT";
    String indent = getIndent(INDENT_LEVEL * 2);
    AdvancedSearchQueryGroup qgSpatial;
    AdvancedSearchQueryTerm qt;
    AdvancedSearchQueryGroup rightOfDateline = null;
    String searchMode;

    qgSpatial = new AdvancedSearchQueryGroup(operator, indent);
    indent = getIndent(INDENT_LEVEL * 3);
    
    northValue = validateGeographicCoordinate(northValue, -90.0F, 90.0F, "90.0");
    southValue = validateGeographicCoordinate(southValue, -90.0F, 90.0F, "-90.0");
    eastValue = validateGeographicCoordinate(eastValue, -180.0F, 180.0F, "180.0");
    westValue = validateGeographicCoordinate(westValue, -180.0F, 180.0F, "-180.0");

    /* Check that all four coordinates has values before attempting spatial
     * search.
     */
    if ((northValue != null) && 
        (!(northValue.equals(""))) &&
        (southValue != null) && 
        (!(southValue.equals(""))) &&
        (eastValue != null) && 
        (!(eastValue.equals(""))) &&
        (westValue != null) && 
        (!(westValue.equals("")))
       ) {
      
      if (northValue.equals("90.0") && southValue.equals("-90.0") &&
          eastValue.equals("180.0") && westValue.equals("-180.0")
         ) {
        hasSpatialSearch = false;
        return;
      }
      
      hasSpatialSearch = true;   

      /*
       * Check whether the east/west coordinates cross over the international
       * dateline. If the search crosses the dateline, special handling will
       * be needed in forming the query.
       */
      crosses = crossesInternationalDateline(eastValue, westValue);
      
      if (crosses) {
        datelineGroup = new AdvancedSearchQueryGroup("UNION", indent);
        indent = getIndent(INDENT_LEVEL * 4);
        leftOfDateline = new AdvancedSearchQueryGroup("INTERSECT", indent);
        rightOfDateline = new AdvancedSearchQueryGroup("INTERSECT", indent);
        datelineGroup.addQueryGroup(leftOfDateline);
        datelineGroup.addQueryGroup(rightOfDateline);
      }

      /*
       * If the user selects the boundaryContained checkbox, use the following
       * logical expression. N, S, E, and W are the boundaries of the bounding
       * box, while N', S', E', and W' are the boundaries specified in a given
       * EML document:
       *              (N' <= N) && (S' >= S) && (E' <= E) && (W' >= W)
       */
      if (boundaryContained) {

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/northBoundingCoordinate";
        searchMode = "less-than-equals";
        qt=new AdvancedSearchQueryTerm(searchMode,caseSensitive,emlField, 
                                       northValue, indent);
        qgSpatial.addQueryTerm(qt);        

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/southBoundingCoordinate";
        searchMode = "greater-than-equals";
        qt=new AdvancedSearchQueryTerm(searchMode,caseSensitive,emlField, 
                                       southValue, indent);
        qgSpatial.addQueryTerm(qt);        

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/eastBoundingCoordinate";
        searchMode = "less-than-equals";
        
        if (crosses) {
          indent = getIndent(INDENT_LEVEL * 5);
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           "180.0", indent);
          leftOfDateline.addQueryTerm(qt);
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           eastValue, indent);
          rightOfDateline.addQueryTerm(qt);
        }
        else {
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           eastValue, indent);
          qgSpatial.addQueryTerm(qt);
        }

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/westBoundingCoordinate";
        searchMode = "greater-than-equals";
        
        if (crosses) {
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           westValue, indent);
          leftOfDateline.addQueryTerm(qt);
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           "-180.0", indent);
          rightOfDateline.addQueryTerm(qt);
        }
        else {
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           westValue, indent);
          qgSpatial.addQueryTerm(qt);        
        }
      }
     /*
      * Else, if the user does not select the boundaryContained checkbox, use 
      * the following logical expression. N, S, E, and W are the boundaries of 
      * the bounding box, while N', S', E', and W' are the boundaries specified 
      * in a given EML document:
      *              (N' > S) && (S' < N) && (E' > W) && (W' < E)
      */
      else {     

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/southBoundingCoordinate";
        searchMode = "less-than";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         northValue, indent);
        qgSpatial.addQueryTerm(qt);        

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/northBoundingCoordinate";
        searchMode = "greater-than";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         southValue, indent);
        qgSpatial.addQueryTerm(qt);        

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/westBoundingCoordinate";
        searchMode = "less-than";
        
        if (crosses) {
          indent = getIndent(INDENT_LEVEL * 5);
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           "180.0", indent);
          leftOfDateline.addQueryTerm(qt);
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           eastValue, indent);
          rightOfDateline.addQueryTerm(qt);
        }
        else {
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           eastValue, indent);
          qgSpatial.addQueryTerm(qt);   
        }

        emlField = "dataset/coverage/geographicCoverage/boundingCoordinates/eastBoundingCoordinate";
        searchMode = "greater-than";
        
        if (crosses) {
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           westValue, indent);
          leftOfDateline.addQueryTerm(qt);
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           "-180.0", indent);
          rightOfDateline.addQueryTerm(qt);
        }
        else {
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                           westValue, indent);
          qgSpatial.addQueryTerm(qt);
        }
      }

      if (crosses) {
        qgSpatial.addQueryGroup(datelineGroup);
      }

      queryGroup.addQueryGroup(qgSpatial);
    }
  }
  

  /*
   * Helper method to implement server-side validation of geographic coordinate values.
   */
  private String validateGeographicCoordinate(String stringValue, 
                                              float minValue, 
                                              float maxValue, 
                                              String defaultValue) {
    String returnValue = defaultValue;
    String warning = "Illegal geographic coordinate specified: '" + stringValue +
                     "'. Expected a value between " + minValue + " and " + maxValue;

    if (stringValue == null || stringValue.equals("")) {
      return returnValue;
    }
    else {
      try {
        Float floatValue = new Float(stringValue);

        if ((floatValue != null) &&
            (floatValue >= minValue) && 
            (floatValue <= maxValue)) {
          returnValue = stringValue;
        }
        else {
          logger.warn(warning);
        }
      }
      catch (NumberFormatException e) {
        logger.warn(warning);
      }
    }
    
    return returnValue;
  }
  

  /**
   * Two kinds of temporal searches are supported. The first is on a named
   * time scale. The second is on a specific start date and/or end date.
   */
  private void buildQueryTemporalCriteria(String dateField,
                                          String startDate,
                                          String endDate,
                                          boolean isDatesContainedChecked,
                                          String namedTimescale, 
                                          String namedTimescaleQueryType,
                                          TermsList termsList
                                         ) {
    boolean addQueryGroup = false;
    boolean addQueryGroupDates = false;
    boolean addQueryGroupNamed = false;
    String emlField;
    final String operator = "INTERSECT";
    String indent = getIndent(INDENT_LEVEL * 2);
    AdvancedSearchQueryGroup qg= new AdvancedSearchQueryGroup(operator, indent);
    AdvancedSearchQueryGroup qgNamed, qgDates, qgDatesStart, qgDatesEnd;
    AdvancedSearchQueryTerm qt;
    String searchMode;
    String xDate;     // Will hold either "beginDate" or "endDate"

    indent = getIndent(INDENT_LEVEL * 3);

    /* If the user specified a named timescale, check to see whether it occurs
     * in any of three possible places: singleDateTime, beginDate, or endDate.
     */
    qgNamed = new AdvancedSearchQueryGroup("UNION", indent);
    if ((namedTimescale != null) && (!(namedTimescale.equals("")))) {
      indent = getIndent(INDENT_LEVEL * 4);
      searchMode = metacatSearchMode(namedTimescaleQueryType);
      
      emlField = 
           "temporalCoverage/singleDateTime/alternativeTimeScale/timeScaleName";
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       namedTimescale, indent);
      qgNamed.addQueryTerm(qt);
      
      emlField = 
   "temporalCoverage/rangeOfDates/beginDate/alternativeTimeScale/timeScaleName";
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       namedTimescale, indent);
      qgNamed.addQueryTerm(qt);
      
      emlField = 
     "temporalCoverage/rangeOfDates/endDate/alternativeTimeScale/timeScaleName";
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       namedTimescale, indent);
      qgNamed.addQueryTerm(qt);
      termsList.addTerm(namedTimescale);
      
      addQueryGroupNamed = true;
    }
    
    qgDates = new AdvancedSearchQueryGroup("INTERSECT", indent);
    
    startDate = validateDateString(startDate);
    endDate = validateDateString(endDate);
    validateDateRange(startDate, endDate);

    // If a start date was specified, search for temporal coverage and/or a
    // pubDate with 'endDate' greater than or equal to the specified start date.
    //
    if ((startDate != null) && (!(startDate.equals("")))) {
      indent = getIndent(INDENT_LEVEL * 4);
      qgDatesStart = new AdvancedSearchQueryGroup("UNION", indent);
      indent = getIndent(INDENT_LEVEL * 5);
      searchMode = "greater-than-equals";

      if (dateField.equals("ALL") || dateField.equals("COLLECTION")) {
        xDate = isDatesContainedChecked ? "beginDate" : "endDate";
        emlField = "temporalCoverage/rangeOfDates/" + xDate + "/calendarDate";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         startDate, indent);
        qgDatesStart.addQueryTerm(qt);        

        emlField = "temporalCoverage/singleDateTime/calendarDate";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         startDate, indent);
        qgDatesStart.addQueryTerm(qt);
      }
      
      if (dateField.equals("ALL") || dateField.equals("PUBLICATION")) {
        emlField = "dataset/pubDate";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         startDate, indent);
        qgDatesStart.addQueryTerm(qt);        
      }
      
      qgDates.addQueryGroup(qgDatesStart);
      addQueryGroupDates = true;
    }

    // If an end date was specified, search for temporal coverage and/or a
    // pubDate with 'beginDate' less than or equal to the end date.
    //
    if ((endDate != null) && (!(endDate.equals("")))) {
      indent = getIndent(INDENT_LEVEL * 4);
      qgDatesEnd = new AdvancedSearchQueryGroup("UNION", indent);
      indent = getIndent(INDENT_LEVEL * 5);
      searchMode = "less-than-equals";

      if (dateField.equals("ALL") || dateField.equals("COLLECTION")) {
        xDate = isDatesContainedChecked ? "endDate" : "beginDate";
        emlField = "temporalCoverage/rangeOfDates/" + xDate + "/calendarDate";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         endDate, indent);
        qgDatesEnd.addQueryTerm(qt);        

        emlField = "temporalCoverage/singleDateTime/calendarDate";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         endDate, indent);
        qgDatesEnd.addQueryTerm(qt);
      }
      
      if (dateField.equals("ALL") || dateField.equals("PUBLICATION")) {
        emlField = "dataset/pubDate";
        qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                         endDate, indent);
        qgDatesEnd.addQueryTerm(qt);        
      }      

      qgDates.addQueryGroup(qgDatesEnd);
      addQueryGroupDates = true;
    }
    
    if (addQueryGroupNamed) {
      qg.addQueryGroup(qgNamed);
      addQueryGroup = true;
    }
    
    if (addQueryGroupDates) {
      qg.addQueryGroup(qgDates);
      addQueryGroup = true;
    }
    
    if (addQueryGroup) {
      hasTemporalSearch = true;
      queryGroup.addQueryGroup(qg);
    }

  }
  
  
  /*
   * Check whether a user's input date string conforms with one of the
   * allowed formats as specified on the web form.
   */
  private String validateDateString(String dateString) {
    String returnValue = null;
    DateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM-dd");
    DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM");
    DateFormat dateFormat3 = new SimpleDateFormat("yyyy");
    Date date = null;
    
    if (dateString == null || dateString.equals("")) {
      return dateString;
    }
    else {
      try {
        date = dateFormat1.parse(dateString);
        returnValue = dateFormat1.format(date);
      }
      catch (ParseException e1) {
        try {
          date = dateFormat2.parse(dateString);
          returnValue = dateFormat2.format(date);
        }
        catch (ParseException e2) {
          try {
            date = dateFormat3.parse(dateString);
            returnValue = dateFormat3.format(date);
          }
          catch (ParseException e3) {
            logger.warn("Couldn't parse date string using any of the recognized formats: " + dateString);
          }    
        }
      }
    }
    
    return returnValue;
  }
  
  
  /*
   * Check whether a user's input date range is valid.
   */
  private void validateDateRange(String startDateStr, String endDateStr) {
    Date startDate = null;
    Date endDate = null;
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    
    try {
      if ((startDateStr != null) && (endDateStr != null)) {
        startDate = dateFormat.parse(startDateStr);
        endDate = dateFormat.parse(endDateStr);
      
        if ((startDate != null) && (endDate != null) && (startDate.after(endDate))) {
          throw new IllegalArgumentException(
            "The date range is invalid. Start date ('" + startDateStr + 
            "') should be less than end date ('" + endDateStr + "').");   	
        }
      }
    }
    catch (ParseException e) {
      logger.warn("Couldn't parse date string: " + e.getMessage());
    }
    
  }


  /**
   * A taxon query searches the taxonRankValue field,
   * matching the field if the user-specified value is contained in the field.
   */
  private void buildQueryTaxon(TermsList termsList) {
    boolean addQueryGroup = false;
    final String emlField;
    String indent = getIndent(INDENT_LEVEL * 2);
    final String operator = "INTERSECT";
    AdvancedSearchQueryGroup qg= new AdvancedSearchQueryGroup(operator, indent);
    AdvancedSearchQueryTerm qt;
    final String searchMode;
    String taxonQueryType = this.taxonQueryType;
    final String value = this.taxon;
      
    indent = getIndent(INDENT_LEVEL * 3);

    if ((value != null) && (!(value.equals("")))) {
      emlField = "taxonRankValue";
      searchMode = metacatSearchMode(taxonQueryType);
      qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       value, indent);
      qg.addQueryTerm(qt);        
      addQueryGroup = true;     
      termsList.addTerm(value);
    }

    if (addQueryGroup) {      
      hasTaxonomicSearch = true;
      queryGroup.addQueryGroup(qg);
    }
  }
  

  /**
   * Build a site filter. If the AdvancedSearch's site value is non-null, add a
   * query group that limits the results to a particular LTER site. Do this
   * by searching for a packageId attribute that starts with "knb-lter-xyz"
   * where "xyz" is the three-letter site acronym, or for a site keyword
   * phrase (e.g. "Kellogg Biological Station") anywhere in the documment.
   */
  private void buildSiteFilter(TermsList termsList) {
    String attributeValue = "";
    String emlField = "";
    String indent = getIndent(INDENT_LEVEL * 2);
    final String operator = "UNION";
    AdvancedSearchQueryGroup qg= new AdvancedSearchQueryGroup(operator, indent);
    AdvancedSearchQueryTerm qt;
    String searchMode;
   
    indent = getIndent(INDENT_LEVEL * 3);

    if (this.siteValues != null) {
      for (int i = 0; i < siteValues.length; i++) {  
        String site = siteValues[i];
        LTERSite lterSite = new LTERSite(site);
        if (lterSite.isValidSite()) {
          hasSiteFilter = true;
      
          emlField = "@packageId";
          attributeValue = lterSite.getPackageId();              
          searchMode = "starts-with";
          qt = new AdvancedSearchQueryTerm(searchMode, caseSensitive, emlField, 
                                       attributeValue, indent);
          qg.addQueryTerm(qt);
          String siteName = lterSite.getSiteName();
          if ((siteName != null) && (!siteName.equals(""))) {
            termsList.addTerm(siteName);
          }
        }
      }

      queryGroup.addQueryGroup(qg); 
    }
  }


  /**
   * Counts the number of search types on the form. For example, if the user
   * has filled in values for both a subject search and a spatial search,
   * would return 2.
   * 
   * @return  searchFields  An integer representing the total number of search
   *                        fields that the user has filled in for this advanced 
   *                        search.
   */
  private int countSearchFields() {
    int searchFields = 0;
    
    if (hasSubjectSearch == true)   { searchFields++; }
    if (hasAuthorSearch == true)    { searchFields++; }
    if (hasGeographicDescriptionSearch == true)   { searchFields++; }
    if (hasSpatialSearch == true)   { searchFields++; }
    if (hasTaxonomicSearch == true) { searchFields++; }
    if (hasTemporalSearch == true)  { searchFields++; }
    if (hasSiteFilter == true)      { searchFields++; }
    
    return searchFields;
  }
  

  /**
   * Boolean to determine whether the east/west coordinates of a spatial search
   * cross over the international date line.
   * 
   * @param eastValue
   * @param westValue
   * @return true if the values cross the data line, else false.
   */
  private boolean crossesInternationalDateline(String eastValue, 
                                               String westValue
                                               ) {
    boolean crosses = false;
    
    if ((eastValue != null) &&
        (eastValue != "") &&
        (westValue != null) &&
        (westValue != "")
       ) {
      Double eastInteger = new Double(eastValue);
      Double westInteger = new Double(westValue);
      
      crosses = westInteger > eastInteger;
    }
    
    logger.debug("crosses International Dateline: " + crosses);
    
    return crosses;
  }

  
  /**
   * Builds and runs a search, returning the result XML string.
   * 
   * @param request     the servlet request object
   * @param uid         the user id
   */
  public String executeSearch(final HttpServletRequest request,
                              final String uid) {
    int searchFields;
    buildQuerySubject(this.termsList);
    buildQueryAuthor(this.termsList);
    buildQueryTaxon(this.termsList);
    buildQueryGeographicDescription(this.locationName, this.termsList);
    buildQuerySpatial(this.northBound, this.southBound, 
                      this.eastBound, this.westBound,
                      this.isBoundaryContainedChecked);
    buildQueryTemporalCriteria(this.dateField,
                               this.startDate,
                               this.endDate,
                               this.isDatesContainedChecked,
                               this.namedTimescale, 
                               this.namedTimescaleQueryType,
                               this.termsList);
    buildSiteFilter(termsList);

    // Count the number of search types the user has entered.
    searchFields = countSearchFields();

    // If the user has entered values for only one type of search criteria,
    // then optimize the search by setting the QueryGroup object's
    // includeOuterQueryGroup to false. This will strip off the outer query
    // group and result in a more simplified SQL statement.
    //
    if (searchFields == 1) {
      queryGroup.setIncludeOuterQueryGroup(false);
    }

    queryString = pathQuery.toString();
    logger.info(queryString);
    String resultsetXML = runQuery(request, uid);

    return resultsetXML;
  }
  

  /**
   * Returns a string of spaces that corresponds to the current indent level.
   * 
   * @param indentLevel   The number of spaces to be indented.
   * @return              A string containing indentLevel number of spaces.
   */
  private String getIndent(final int indentLevel) {
    StringBuffer indent = new StringBuffer(12);
    
    for (int i = 0; i < indentLevel; i++) {
      indent.append(" ");
    }
    
    return indent.toString();
  }
  
  
  /*
   * Accessor method for the termsList instance variable.
   * 
   * @return  termsList, a TermsList object
   */
  public TermsList getTermsList() {
    return termsList;
  }
  
  
  /**
   * Given a query type value, return the corresponding Metacat
   * searchmode string.
   * 
   * @param queryType  A string value indicating the query type:
   *                     "0" --> "contains"
   *                     "1" --> "equals"
   *                     "2" --> "starts-with"
   *                     "3" --> "ends-with"
   * @return A string, the Metacat search mode value.
   */ 
  private String metacatSearchMode(final String queryType) {   
    if (queryType == null) return "contains";
    if (queryType.equals("0")) return "contains";
    if (queryType.equals("1")) return "equals";
    if (queryType.equals("2")) return "starts-with";
    if (queryType.equals("3")) return "ends-with";
    return "containts";  
  }
  
  
  /*
   * Optimize the set of search terms returned from the controlled
   * vocabulary web service by removing terms that are already
   * covered by substring terms. For example, "fishes" can be
   * removed if "fish" is already present.
   * 
   * An exception to this rule is if the substring term is short
   * enough for using "exact" instead of "contains" (i.e. less than
   * or equal to AdvancedSearchQueryGroup.EXACT_SEARCH_MAXIMUM_LENGTH).
   */
  private TreeSet<String> optimizeTermList(TreeSet<String> webValues) {
    TreeSet<String> optimizedWebValues = new TreeSet<String>();
    
    for (String value1 : webValues) {
      if (value1 != null) {
        boolean keepThisValue = true;
        for (String value2 : webValues) {
          if ((!value1.equalsIgnoreCase(value2)) &&
              (value2.length() > 
               AdvancedSearchQueryGroup.EXACT_SEARCH_MAXIMUM_LENGTH)
             ) {
            if (value1.contains(value2)) {
              keepThisValue = false; // covered by a substring term
            }
          }
        }
        
        /*
         * This is a workaround for a Metacat bug #5443
         * 
         * http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5443
         * 
         * Metacat structured pathquery is bugged. It does not support
         * searchmode of 'equals' or 'matches-exactly' the way it should.
         * So we need to filter out any terms that are two characters
         * or less in length because they match too many documents on
         * a 'contains' search.
         */
        if (value1.length() <= 3) {
          keepThisValue = false;
        }
      
        if (keepThisValue) {
          optimizedWebValues.add(value1);
        }
      }
    }
    
    return optimizedWebValues;
  }
  

  /**
   * Parses search terms from a string. In this simple implementation, the 
   * string is considered to be a list of tokens separated by spaces. The more 
   * advanced implementation (parserTermsAdvanced) parses quoted strings 
   * containing spaces as a term. This method can be eliminated if we are
   * satisfied that parseTermsAdvanced() is working properly.
   * 
   * @param  value    The string value as entered by the user.
   * 
   * @return terms    An ArrayList of String objects. Each space-separated 
   *                  token is a single term.
   *
  private ArrayList parseTerms(final String value) {
    StringTokenizer st;
    ArrayList terms = new ArrayList();
    String token;
    final int tokenCount;
    
    st = new StringTokenizer(value, " ");
    tokenCount = st.countTokens();
    
    for (int i = 0; i < tokenCount; i++) {
      token = st.nextToken();
      terms.add(token);
    }
    
    return terms;
  }
  */

  
  /**
   * Parses search terms from a string. In this advanced implementation,
   * double-quoted strings that contain spaces are considered a single term.
   * 
   * @param  value     The string value as entered by the user.
   * 
   * @return terms    An ArrayList of String objects. Each string is a term.
   */
  private ArrayList<String> parseTermsAdvanced(String value) {
    char c;
    StringBuffer currentTerm = new StringBuffer(100);
    boolean keepSpaces = false;
    final int stringLength;
    ArrayList<String> terms = new ArrayList<String>();

    value = value.trim();
    stringLength = value.length();
    
    for (int i = 0; i < stringLength; i++) {
      c = value.charAt(i);
  
      if (c == '\"') {
        // Termination of a quote-enclosed term. Add the current term to the
        // list and start a new term.
        if (keepSpaces) {
          addTerm(terms, currentTerm);
          currentTerm = new StringBuffer(100);
        }
      
        keepSpaces = !(keepSpaces); // Toggle keepSpaces to its opposite value.
      }
      else if (c == ' ') {
        // If we are inside a quote-enclosed term, append the space.
        if (keepSpaces) {
          currentTerm.append(c);
        }
        // Else, add the current term to the list and start a new term.
        else {
          addTerm(terms, currentTerm);
          currentTerm = new StringBuffer(100);
        }
      }
      else {
        // Append any non-quote, non-space characters to the current term.
        currentTerm.append(c);
      }
    }

    // Add the final term to the list.
    addTerm(terms, currentTerm);

    return terms;
  }


  /**
   * Runs the Metacat query for a browse search, simple search, or advanced
   * search. Stores the search results in a "resultSet" session attribute as a 
   * ResultSet object.
   * 
   * @param request     the servlet request object
   * @param uid         user id
   * @param metacat     A metacat client object, possible null.
   * @param qformat     The qformat (skin) to use when displaying results.
   * @param xslPath     File path to the resultset.xsl stylesheet.
   * 
   * @return resultsetXML  XML search results from Metacat.
   */
  private String runQuery(HttpServletRequest request,
                          String uid
                         ) {
    String resultsetXML = null;
    String htmlMessage = null;
    
    try {  
      DataPackageManagerClient dpmClient = new DataPackageManagerClient(uid);
      resultsetXML = dpmClient.searchDataPackages(queryString);    
    } 
    catch (PastaAuthenticationException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      htmlMessage = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
    } 
    catch (PastaConfigurationException e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      htmlMessage = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
    } 
    catch (Exception e) {
      logger.error(e.getMessage());
      e.printStackTrace();
      htmlMessage = "<p class=\"warning\">" + e.getMessage() + "</p>\n";
    }

    request.setAttribute("searchresult", htmlMessage);

    return resultsetXML;
  }

}
