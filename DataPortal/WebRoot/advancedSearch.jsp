<!--

 $Date$
 $Author$
 $Revision$
 
 Copyright 2011,2012 the University of New Mexico.
 
 This work was supported by National Science Foundation Cooperative
 Agreements #DEB-0832652 and #DEB-0936498.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0.
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 -->

<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.search.LTERSite" %>

<%
  String warningMessage = (String) request.getAttribute("message");
  if (warningMessage == null) {
    warningMessage = "";
  }

  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String searchResult = (String) request.getAttribute("searchresult");
  if (searchResult == null) { searchResult = ""; }
    
  /* 
   * Check to see whether a default 'site' setting was specified (e.g. 'ARC').
   */
  String siteParam = request.getParameter("site");
  final String indent = "              ";
  boolean includeNIN = true;      // include North Inlet in the options list
  String siteOptions = LTERSite.composeHTMLOptions(indent, includeNIN, siteParam);
  
  /* 
   * Check to see whether a default 'subjectValue' setting was specified
   * (e.g. "Primary Productivity")
   */
  String subjectValue = "";
  String subjectValueParam = request.getParameter("subjectValue");
  if (subjectValueParam != null) {
    subjectValue = subjectValueParam;
  }
  
  /* 
   * Check to see whether a default 'subjectField' setting was specified.
   * Possible values are 'ALL', 'TITLE', 'ABSTRACT', or 'KEYWORDS'.
   */
  final String SELECTED = "selected=\"selected\"";
  String subjectFieldAll = "";
  String subjectFieldTitle = "";
  String subjectFieldAbstract = "";
  String subjectFieldKeywords = "";
  String subjectFieldParam = request.getParameter("subjectField");
  if (subjectFieldParam != null) {
    if (subjectFieldParam.equalsIgnoreCase("ALL")) { 
      subjectFieldAll = SELECTED; 
    }
    else if (subjectFieldParam.equalsIgnoreCase("TITLE")) { 
      subjectFieldTitle = SELECTED; 
    }
    else if (subjectFieldParam.equalsIgnoreCase("ABSTRACT")) { 
      subjectFieldAbstract = SELECTED; 
    }
    else if (subjectFieldParam.equalsIgnoreCase("KEYWORDS")) { 
      subjectFieldKeywords = SELECTED; 
    }
  }

  /* 
   * Check to see whether a default 'creatorSurname' setting was specified
   * (e.g. "Smith")
   */
  String creatorSurname = "";
  String creatorSurnameParam = request.getParameter("creatorSurname");
  if (creatorSurnameParam != null) {
    creatorSurname = creatorSurnameParam;
  }
  
  String taxon = "";
  
%>

<!doctype html>

<html>

<head>
  <base href="<%=basePath%>">
  <title>Advanced Search</title>
  <meta http-equiv="pragma" content="no-cache">
  <meta http-equiv="cache-control" content="no-cache">
  <meta http-equiv="expires" content="0">
  <meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
  <meta http-equiv="description" content="This is my page">
  <link rel="stylesheet" type="text/css" href="./css/lter-nis.css">
  <jsp:include page="/WEB-INF/jsp/javascript.jsp" />
  <script type="text/javascript" src="./js/utilities.js"></script>
  <script type="text/javascript" src="./js/validation.js"></script>
  <script type="text/javascript">
  
      var boundsChangedCount = 0;

      function boundsChanged() {
          boundsChangedCount++;
          document.advancedSearchForm.boundsChangedCount.value = boundsChangedCount;
      }

      function submitRequest(form) {
        var canSearch = true;

        if (trim(form.subjectValue.value) == "" &&
            trim(form.creatorSurname.value) == "" &&
            trim(form.creatorOrganization.value) == "" &&
            trim(form.boundsChangedCount.value) == "1" &&
            trim(form.locationName.value) == "" &&
            trim(form.taxon.value) == "" &&
            howManySelected(form.siteValues) == 0 &&
            trim(form.startDate.value) == "" &&
            trim(form.endDate.value) == "" &&
            trim(form.namedTimescale.value) ==""
           ) {              
          //canSearch = confirm("Show *all* data in the catalog?\n(This may take some time!)");
          alert("Please enter a value to search.");
          canSearch = false;
        }

        if (canSearch) {        
          return(validateAdvancedSearchForm(form));
        }
        else {
          return false;
        }
      }
  </script>
  
</head>

<body>
	<div class="wrapper">
		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />
		<div class="content">
			<h2 align="center">Search for Data Packages</h2>
			<fieldset>
				<legend>Advanced Search</legend>

		  <div class="section advancedsearch">
			  <%=warningMessage%>
        <form id="advancedSearchForm" 
              name="advancedSearchForm" 
              method="post"
              action="./advancedSearch"
              onsubmit="return submitRequest(this)"
        >

        <div class="section">

          <div class="figure floatleft">
            <label for="advancedsearch">LTER Sites</label>
            <select name="siteValues" multiple="multiple" size="17">
              <%= siteOptions %>
            </select>
            <label for="advancedsearch">Taxonomic Criteria</label>
            <table>
              <tr>
                <td>
                  <label for="advancedsearchleft">Taxon:
                    <select name="taxonQueryType">
                      <option value="0" selected="selected">contains</option>
                      <option value="1">matches exactly</option>
                      <option value="2">starts with</option>
                      <option value="3">ends with</option>
                    </select>
                  </label>
                  <input type="text" name="taxon" value='<%=taxon%>' />
                </td>
              </tr>
            </table>
            <label for="advancedsearch">Search Options</label>
            <input type="radio" name="formAllAny" value="0" checked="checked" />"And" all search criteria&nbsp;
            <input type="radio" name="formAllAny" value="1" />"Or" all search criteria&nbsp;<br/>
            <input type="checkbox" name="caseSensitive" value="on" />Case sensitive
          </div>
          
          <div class="figure floatleft">
            <label for="advancedsearch">Subject</label>
            <table>
              <tr>
                <td>
                  <select name="subjectField">
                    <option value="ALL" <%= subjectFieldAll %>>Subject</option>
                    <option value="TITLE" <%= subjectFieldTitle %>>Title Only</option>
                    <option value="ABSTRACT" <%= subjectFieldAbstract %>>Abstract Only</option>
                    <option value="KEYWORDS" <%= subjectFieldKeywords %> selected>Keywords Only</option>
                  </select>
                  <select name="subjectQueryType">
                    <option value="0" selected="selected">contains</option>
                    <option value="1">matches exactly</option>
                    <option value="2">starts with</option>
                    <option value="3">ends with</option>
                  </select>
                  <input type="text" name="subjectValue" value='<%= subjectValue %>' />
                </td>
              </tr>
              <tr>
                <td>
                  <input type="radio" name="subjectAllAny" value="0" checked="checked" />Match All Terms
                  <input type="radio" name="subjectAllAny" value="1" />Match Any Term
                </td>
              </tr>
              <tr>
                <td>
                  <input type="checkbox" name="specific" value="on"  checked="checked" />Add more specific terms
                  <br/><input type="checkbox" name="related" value="on" />Add related terms
                  <br/><input type="checkbox" name="relatedSpecific" value="on" />Add related terms and their more specific terms
                </td>
              </tr>
            </table>
            <label for="advancedsearch">Creators/Organizations</label>
            <table>
              <tr>
                <td><label for="advancedsearchleft">Creator's Last Name:</label>
                  <select name="creatorSurnameQueryType">
                    <option value="0" selected="selected">contains</option>
                    <option value="1">matches exactly</option>
                    <option value="2">starts with</option>
                    <option value="3">ends with</option>
                  </select>
                  <input type="text" name="creatorSurname" value='<%=creatorSurname%>' />
                </td>
              </tr>
              <tr>
                <td><label for="advancedsearchleft">Creator's Organization:</label>
                  <select name="creatorOrganizationQueryType">
                    <option value="0" selected="selected">contains</option>
                    <option value="1">matches exactly</option>
                    <option value="2">starts with</option>
                    <option value="3">ends with</option>
                  </select>
                  <input type="text" name="creatorOrganization" value="" />
                </td>
              </tr>
            </table>   
            <label for="advancedsearch">Temporal Criteria</label>
            <table>
              <tr>
                <td colspan="2" align="center">
                  <input type="radio" name="dateField" value="COLLECTION" checked="checked" />Collection Date&nbsp;
                  <input type="radio" name="dateField" value="PUBLICATION" />Publication Date&nbsp;
                  <input type="radio" name="dateField" value="ALL" />Either&nbsp;
                </td>
              </tr>
              <tr>
                <td>
                  <label for="advancedsearchleft">Start Date:<input type="date" name="startDate" value="" placeholder="YYYY-MM-DD" /></label>                        
                </td>
                <td>
                  <label for="advancedsearchleft">End Date:<input type="date" name="endDate" value="" placeholder="YYYY-MM-DD" /></label>
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <input type="checkbox" name="datesContained" />
                  Dataset is fully contained within start and end dates
                </td>
              </tr>
              <tr>
                <td colspan="2">
                  <label for="advancedsearchleft">Named&nbsp;Timescale:
                  <select name="namedTimescaleQueryType">
                    <option value="0" selected="selected">contains</option>
                    <option value="1">matches exactly</option>
                    <option value="2">starts with</option>
                    <option value="3">ends with</option>
                  </select></label>
                  <input type="text" name="namedTimescale" value="">
                </td>
              </tr>
            </table>
          </div>

          <div class="figure floatleft">
            <label for="advancedsearch">Spatial Criteria</label>
            <input type="hidden" name="boundsChangedCount" value="0" />
            <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAcbgq4MRleYDjHPQoQazyHMAiavmj0s0U&sensor=false"
                    type="text/javascript">
            </script>
            <script type="text/javascript" src="./js/map_functions.js" ></script>      
            <script type="text/javascript">
              google.maps.event.addDomListener(window, 'load', initialize);
            </script>
            <div id="map-canvas" style="margin: 0 auto; width: 330px; height: 258px"></div>
            <div style="margin: 0 auto; width: 330px;">
            Zoom in to the region you'd like to search
            </div>
            <table width="100%">
              <tr>
                <td></td>
                <td align="center">
                  <label for="advancedsearchleft">N:&nbsp;<input type="text" name="northBound" size="12" maxlength="12" value="90.0" onchange="boundsChanged()" /></label>
                </td>
                <td></td>
              </tr>
              <tr>
                <td align="left">
                  <label for="advancedsearchleft">W:&nbsp;<input type="text" name="westBound" size="12" maxlength="12" value="-180.0" onchange="boundsChanged()" /></label>
                </td>
                <td></td>
                <td align="right">
                  <label for="advancedsearchleft">E:&nbsp;<input type="text" name="eastBound" size="12" maxlength="12" value="180.0" onchange="boundsChanged()" /></label>
                </td>
              </tr>
              <tr>
                <td></td>
                <td align="center">
                  <label for="advancedsearchleft">S:&nbsp;<input type="text" name="southBound" size="12" maxlength="12" value="-90.0" onchange="boundsChanged()" /></label>
                </td>
                <td></td>
              </tr>
              <tr>
                <td colspan="3">
                  <input type="checkbox" name="boundaryContained" value="on" />      
                  Dataset is fully contained within boundaries
                </td>   
              </tr>
              <tr>
                <td colspan="2"><label for="advancedsearchleft">Geographic Place Name:</label><br/><input type="text" name="locationName" size="40" value=""/></td>
              </tr>
            </table>    
         </div>

         <div class="section">
            <br/>
            <p align="center">
             <input type="submit" value="Search" /> <input type="reset" value="Reset" />
           </p>
         </div>
         
       </div> <!-- end section -->
      
    </form>
    
    </div> <!-- end section advancedsearch -->
    
			</fieldset>

    

    <div class="section-table">
        <%=searchResult%>
    </div>
	  <!-- end of section-table -->

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

  <script type="text/javascript"> 

    <!-- // JavaScript input validation checking code for advanced search form
    var bCancel = false; 

    function validateAdvancedSearchForm(form) {                                                                   
        if (bCancel) 
            return true; 
        else 
            var formValidationResult;
            formValidationResult = validateFloat(form) && 
                                   validateFloatRange(form) &&
                                   validateDate(form);
            return (formValidationResult == 1);
    } 

    function advancedSearchForm_FloatValidations () { 
      this.a0 = new Array("westBound", "West Boundary must be a number.", new Function ("varName", "this.min='-180.0'; this.max='180.0';  return this[varName];"));
      this.a1 = new Array("eastBound", "East Boundary must be a number.", new Function ("varName", "this.min='-180.0'; this.max='180.0';  return this[varName];"));
      this.a2 = new Array("northBound", "North Boundary must be a number.", new Function ("varName", "this.min='-90.0'; this.max='90.0';  return this[varName];"));
      this.a3 = new Array("southBound", "South Boundary must be a number.", new Function ("varName", "this.min='-90.0'; this.max='90.0';  return this[varName];"));
    } 

    function advancedSearchForm_DateValidations () { 
      this.a0 = new Array("startDate", "Start Date must be a date (YYYY-MM-DD).", new Function ("varName", "this.datePattern='yyyy-MM-dd';  return this[varName];"));
      this.a1 = new Array("endDate", "End Date must be a date (YYYY-MM-DD).", new Function ("varName", "this.datePattern='yyyy-MM-dd';  return this[varName];"));
    } 

    function advancedSearchForm_floatRange () { 
      this.a0 = new Array("westBound", "West Boundary must be in the range -180.0 through 180.0.", new Function ("varName", "this.min='-180.0'; this.max='180.0';  return this[varName];"));
      this.a1 = new Array("eastBound", "East Boundary must be in the range -180.0 through 180.0.", new Function ("varName", "this.min='-180.0'; this.max='180.0';  return this[varName];"));
      this.a2 = new Array("northBound", "North Boundary must be in the range -90.0 through 90.0.", new Function ("varName", "this.min='-90.0'; this.max='90.0';  return this[varName];"));
      this.a3 = new Array("southBound", "South Boundary must be in the range -90.0 through 90.0.", new Function ("varName", "this.min='-90.0'; this.max='90.0';  return this[varName];"));
    } 
  </script>
  <!-- End  JavaScript input validation checking code. --> 

</body>
</html>
