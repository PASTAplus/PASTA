<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.Tooltip" %>
<%@ page import="edu.lternet.pasta.portal.search.LTERSite" %>

<%
  final String pageTitle = "Advanced Search";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);

  final String indent = "              ";
  boolean includeNIN = true;      // include North Inlet in the options list
  String siteOptions = LTERSite.composeHTMLOptions(indent, includeNIN, null);
%>

<!DOCTYPE html>
<html lang="en">

<head>
<title><%= titleText %></title>

<meta charset="UTF-8" />

<link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon" />

<!--  <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1" > -->
<!-- Google Fonts CSS -->
<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

<!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
<link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css">

<!-- JS -->
<script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

<!-- For Custom Checkboxes -->
<script src="js/jquery-1.8.3.min.js" type="text/javascript"></script>
<script type="text/javascript">

	$(document).ready(function() {
		$(".checklist .checkbox-select").click(
			function(event) {
				event.preventDefault();
				$(this).parent().addClass("selected");
				$(this).parent().find(":checkbox").attr("checked","checked");
				
			}
		);
		
		$(".checklist .checkbox-deselect").click(
			function(event) {
				event.preventDefault();
				$(this).parent().removeClass("selected");
				$(this).parent().find(":checkbox").removeAttr("checked");
				
			}
		);
		
	});

</script>

<script type="text/javascript">

	$(document).ready(function() {
		$(".checklistLG .checkboxLG-select").click(
			function(event) {
				event.preventDefault();
				$(this).parent().addClass("selected");
				$(this).parent().find(":checkbox").attr("checked","checked");
				
			}
		);
		
		$(".checklistLG .checkboxLG-deselect").click(
			function(event) {
				event.preventDefault();
				$(this).parent().removeClass("selected");
				$(this).parent().find(":checkbox").removeAttr("checked");
				
			}
		);
		
	});

</script>
<!-- /For Custom Checkboxes -->

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

<jsp:include page="header.jsp" />

<div class="row-fluid ">
	<div>
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h2>Advanced Search</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="content span12 box_layout">
								<!-- Content -->
								<div class="spacer">
								</div>
								<div class="tabbable">
									<ul class="nav nav-tabs">
										<li class="active">
										  <a data-toggle="tab" href="#tab3">Spatial / Place Name</a> 
										</li>
										<li>
										  <a data-toggle="tab" href="#tab1">LTER Sites</a>
										</li>
										<li>
										  <a data-toggle="tab" href="#tab2">Subject</a>
										</li>
										<li>
										  <a data-toggle="tab" href="#tab6">Creator / Organization</a>
										</li>
										<li>
										  <a data-toggle="tab" href="#tab4">Temporal</a> 
										</li>
										<li>
										  <a data-toggle="tab" href="#tab7">Taxonomic</a> 
										</li>
										<li>
										  <a data-toggle="tab" href="#tab5">Search Options</a> 
										</li>
									</ul>
								  <form id="advancedSearchForm" action="./advancedSearch" method="post" name="advancedSearchForm" onsubmit="return submitRequest(this)">
									  <div class="tab-content">
									  
										  <div id="tab3" class="tab-pane active">
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div>
													  <!-- <h3 class="separator_border labelBolder span12">Spatial Criteria</h3> -->
													  <input name="boundsChangedCount" type="hidden" value="0" />
													  <script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAcbgq4MRleYDjHPQoQazyHMAiavmj0s0U&amp;sensor=false" type="text/javascript"></script>
													  <script src="./js/map_functions.js" type="text/javascript"></script>
													  <script type="text/javascript">google.maps.event.addDomListener(window, 'load', initialize);</script>
														<table>														
													  	<tr>										
														    <td>
														      <label>Zoom in to the region you&#39;d like to search:</label>											
                                  <div id="map-canvas"></div>
													      </td>
													      <td>
													        <table id="nis-coordinates">
														        <tr>
															        <td></td>
															        <td>
															          <label>North:</label>
															            <input name="northBound" onchange="boundsChanged()" 
															                  size="8" type="text" value="90.0" />
															        </td>
															        <td></td>
		  												      </tr>
			  											      <tr>
				  											      <td>
					  										        <label>West:</label>
						  									          <input name="westBound" onchange="boundsChanged()" 
							  								                 size="8" type="text" value="-180.0" />						  							        
									  						      </td>
										  					      <td></td>
											  				      <td>
												  			        <label>East:</label>
													  		          <input name="eastBound" onchange="boundsChanged()" 
														  	                 size="8" type="text" value="180.0" />									          
															        </td>
						  								      </tr>
							  							      <tr>
								  							      <td></td>
									  						      <td>
										  					        <label>South:</label>
											  				          <input name="southBound" onchange="boundsChanged()" 
												  			                 size="8" type="text" value="-90.0" />
														  	      </td>
															        <td></td>
	  													      </tr>
		  												    </table>
			  											  </td>														
													      <td></td>
														    <td>
															    <ul class="checklistLG">
																    <li>
																      <input name="boundaryContained" type="checkbox" value="value1">
																      <p>Dataset is Contained within Boundaries</p>
																      <a class="checkboxLG-select" href="#">Select</a>
																      <a class="checkboxLG-deselect" href="#">Cancel</a>
																    </li>
															    </ul>
													      </td>								
				  										</tr>
														</table>
														
														<table>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
														  <tr>
															  <td>
															    <!-- <h3 class="separator_border labelBold span1" for="advancedsearch">Geographic Place Name</h3> -->
															    <label class="labelBold">Geographic Place Name:</label>
															  </td>
														  </tr>
														<tr>
														<td>													  
															    <input name="locationName" size="40" type="text" />
													  </td>
													  </tr>
													  </table>
													  
												  </div>
												  
												  <div class="row-fluid text_bar_pattern themeple_sc">
												    <div class="span12">
													    <span class="row-fluid separator_border"></span>
												    </div>
											    </div>
											    
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab3 -->
										
																  
										  <div id="tab1" class="tab-pane  ">
												<table>
												  <!--
													<tr>
														<td valign="top">
														  <h3 class="separator_border labelBolder span4" for="advancedsearch">LTER Sites</h3>
														</td>
													</tr>
													<tr>
														<td class="spacersmh"></td>
													</tr>
													-->
													<tr>
														<td valign="top">
                              <select class="select-width-auto" name="siteValues" multiple="multiple" size="10">
                                <%= siteOptions %>
                              </select>
                            </td>
													</tr>
												</table>
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div class="span12">
													  <span class="row-fluid separator_border"></span>
												  </div>
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab1 -->
										
										
										  <div id="tab2" class="tab-pane  ">
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div>
													  <table>
													    <!-- 
														  <tr>
															  <td colspan="6">
															    <h3 class="separator_border labelBolder span1">Subject</h3>
															  </td>
														  </tr>
														  <tr>
															  <td class="spacersm2"></td>
														  </tr>
														  -->
														  <tr>
															  <td>
															    <select name="subjectField">
															      <option value="ALL">Subject</option>
															      <option value="TITLE">Title only</option>
															      <option value="ABSTRACT">Abstract only</option>
															      <option value="KEYWORDS">Keywords only</option>
															    </select>
															  </td>
															  <td class="spacerwd"></td>
															  <td>
															    <select name="subjectQueryType">
															      <option selected="selected" value="0">contains</option>
															      <option value="1">matches exactly</option>
															      <option value="2">starts with</option>
															      <option value="3">ends with</option>
															    </select>
															  </td>
															  <td class="spacerwd"></td>														  
															  <td>
															  	<span name='<%= Tooltip.SEARCH_TERMS %>'
															  		  class="tooltip">
															  		<input name="subjectValue" type="search" placeholder="enter search terms" />
															  	</span>
															  </td>
															</tr>
															<tr>
															  <td class="spacerwd"></td>
															  <td class="spacerwd"></td>
															  <td class="spacerwd"></td>
															  <td class="spacerwd"></td>
															  <td>
															    <input checked="checked" name="subjectAllAny" type="radio" value="0" />
															    Match All Terms
															    &nbsp;&nbsp;&nbsp;
															    <input name="subjectAllAny" type="radio" value="1" /> 
															    Match Any Term
															  </td>
														  </tr>
												  </table>
													  <table>
														  <tr>
														  </tr>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
													  </table>
													  <table>
														  <tr>
															  <td>
															    <ul class="checklistLG">
																    <li>
																      <input name="specific" type="checkbox" value="value1">
																      <p>More Specific Terms</p>
																      <a class="checkboxLG-select" href="#">Select</a>
																      <a class="checkboxLG-deselect" href="#">Cancel</a> </li>
																    <li>
																      <input name="related" type="checkbox" value="value1">
																      <p>Related Terms</p>
																      <a class="checkboxLG-select" href="#">Select</a>
																      <a class="checkboxLG-deselect" href="#">Cancel</a>
																    </li>
																    <li>
																      <input name="relatedSpecific" type="checkbox" value="value1">
																      <p>Related / More Specific Terms</p>
																      <a class="checkboxLG-select" href="#">Select</a>
																      <a class="checkboxLG-deselect" href="#">Cancel</a>
																    </li>
															    </ul>
															  </td>
														  </tr>
													  </table>													  
												  </div>
												  <div class="row-fluid text_bar_pattern themeple_sc">												
												    <div class="span12">
													    <span class="row-fluid separator_border"></span>
												    </div>
											    </div>
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab2 -->
										
										
										  <div id="tab6" class="tab-pane  ">
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div>
													  <table>
													    <!--
														  <tr>
															  <td>
															    <h3 class="separator_border labelBold span1">Creator / Organization</h3>
															  </td>
														  </tr>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
														  -->
														  <tr>
															  <td>
															    <label class="labelBold">Creator&#39;s Last Name:</label>
															    <select name="creatorSurnameQueryType">
															      <option selected="selected" value="0">contains</option>
															      <option value="1">matches exactly</option>
															      <option value="2">starts with</option>
															      <option value="3">ends with</option>
															    </select>
															    <input name="creatorSurname" type="text" />
															  </td>
														  </tr>
														  <tr>
															  <td>
															    <label class="labelBold">Creator&#39;s Organization:</label>
															    <select name="creatorOrganizationQueryType">
															      <option selected="selected" value="0">contains</option>
															      <option value="1">matches exactly</option>
															      <option value="2">starts with</option>
															      <option value="3">ends with</option>
															    </select>
															    <input name="creatorOrganization" type="text" />
															  </td>
														  </tr>
													  </table>
												  </div>
												  <div class="row-fluid text_bar_pattern themeple_sc">												
												    <div class="span12">
													    <span class="row-fluid separator_border"></span>
												    </div>
											    </div>
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab6 -->
										
										
										  <div id="tab4" class="tab-pane  ">
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div>
													  <table>
													    <!-- 
														  <tr>
															  <td colspan="12">
															    <h3 class="separator_border labelBold span1">Temporal Criteria</h3>
															  </td>
														  </tr>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
														  -->
														  <tr>
															  <td>
															    <input checked="checked" name="dateField" type="radio" value="COLLECTION" /> 
															    Collection Date
															  </td>
															  <td class="spacerwd"></td>
															  <td>
															    <input name="dateField" type="radio" value="PUBLICATION" /> 
															    Publication Date
															  </td>
															  <td class="spacerwd"></td>
															  <td>
															    <input name="dateField" type="radio" value="ALL" /> 
															    Either
															  </td>
														  </tr>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
													  </table>
													  <table>
														  <tr>
															  <td>
															    <label class="labelBold" for="userId">Start Date:</label>
															    <input name="startDate" placeholder="YYYY-MM-DD" type="date" />
															  </td>
															  <td></td>
															  <td>
															    <label class="labelBold" for="group">End Date:</label>
															    <input name="endDate" placeholder="YYYY-MM-DD" type="date" />
															  </td>
															  <td></td>
															  <td></td>
															  <td>
															    <ul class="checklistLG">
																    <li>
																      <input name="datesContained" type="checkbox" value="value1">
																      <p>Dataset is Contained within Start / End Dates</p>
																      <a class="checkboxLG-select" href="#">Select</a>
																      <a class="checkboxLG-deselect" href="#">Cancel</a>
																    </li>
															    </ul>
															  </td>
														  </tr>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
														  <tr>
															  <td>
															    <label class="labelBold">Named Time-scale:</label>
															  </td>
														  </tr>
														  <tr>
															  <td>
															    <select name="namedTimescaleQueryType">
															      <option selected="selected" value="0">contains</option>
															      <option value="1">matches exactly</option>
															      <option value="2">starts with</option>
															      <option value="3">ends with</option>
															    </select>
															  </td>
															  <td></td>
															  <td>
															    <input name="namedTimescale" type="text" >
															  </td>
														  </tr>
													  </table>
												  </div>
												  <div class="row-fluid text_bar_pattern themeple_sc">
												    <div class="span12">
													    <span class="row-fluid separator_border"></span>
												    </div>
											    </div>
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab4 -->
										
										
										  <div id="tab7" class="tab-pane  ">
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div>
														<table>
														  <!--
														  <tr>
														   <td colspan="12">
														     <h3 class="separator_border labelBold span1">Taxonomic Criteria</h3>
														   </td>
														  </tr>
														  <tr>
														   <td class="spacersmh"></td>
														  </tr>
														  -->
														  <tr>
														    <td>
														      <label class="labelBold">Taxon:</label>
														    </td>
														  </tr>
															<tr>
															  <td>
																  <select name="taxonQueryType">
																    <option selected="selected" value="0">contains</option>
																    <option value="1">matches exactly</option>
																    <option value="2">starts with</option>
																    <option value="3">ends with</option>
																  </select> 
																</td>
																<td></td>
															  <td>
															    <input name="taxon" type="text" />
															  </td>
															</tr>
														</table>
												  </div>
												  <div class="row-fluid text_bar_pattern themeple_sc">
												    <div class="span12">
													    <span class="row-fluid separator_border"></span>
												    </div>
											    </div>
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab7 -->
										
										
										  <div id="tab5" class="tab-pane  ">
											  <div class="row-fluid text_bar_pattern themeple_sc">
												  <div class="row-fluid text_bar_pattern themeple_sc">
												    <!-- Search Options Table -->												
												    <table>
														  <tr>
														    <td>
														      <label class="labelBold">Case Sensitivity:</label>
														    </td>
														  </tr>
													    <tr>
														    <td>
															    <ul class="checklist">
																    <li>
																      <input name="caseSensitive" type="checkbox" value="value1">
																      <p>Case Sensitive?</p>
																      <a class="checkbox-select" href="#">Select</a>
																      <a class="checkbox-deselect" href="#">Cancel</a>
																    </li>
															    </ul>
														    </td>
														  </tr>
														  <tr>
															  <td class="spacersmh"></td>
														  </tr>
														  <tr>
														    <td>
														      <label class="labelBold">Match Options:</label>
														    </td>
														  </tr>
														  <tr>
														    <td valign="top">
															    <input checked="checked" name="formAllAny" type="radio" value="0" /> 
															    Match <b>ALL</b> search criteria
														    </td>
														  </tr>
														  <tr>
														    <td valign="top">
															    <input name="formAllAny" type="radio" value="1" /> 
															    Match <b>ANY</b> search criteria
														    </td>
													    </tr>
												    </table>												
												    <!-- /Search Options Table -->
												    <div class="span12">
													    <span class="row-fluid separator_border"></span>
												    </div>
											    </div>
										      <table>
											      <tr>
												      <td align="left">
												        <input class="btn btn-large btn-info btn-default" name="submit" type="submit" value="Submit" />
												        <input class="btn btn-large btn-info btn-default" name="reset" type="reset" value="Clear All" />
												      </td>
											      </tr>
										      </table>
											  </div>
										  </div>
										  <!-- /#tab4 -->

										
									  </div>
							    </form>
								</div>
						  </div>
						  <!-- /Content -->
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<jsp:include page="footer.jsp" />
		
</div>

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
