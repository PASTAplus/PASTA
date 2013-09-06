<!DOCTYPE html>
<html>

<head>
<title>LTER :: Network Data Portal</title>

<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<!-- Google Fonts CSS -->
<link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

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
<script src="charts/assets/jquery.min.js" type="text/javascript"></script>
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
								<h2>Advanced Search Options</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="content span12 box_layout">
								<!-- Content -->
								<!--  -->
								<!--  -->
								<div class="spacer">
								</div>
								<div class="tabbable  " style="left: 0px; top: 0px">
									<ul class="nav nav-tabs">
										<li class="active">
										<a data-toggle="tab" href="#tab1">LTER Sites 
										/ Taxonomic Criteria / Search Options</a>
										</li>
										<li><a data-toggle="tab" href="#tab2">Subject 
										/ Creators-Organizations / Temporal Criteria</a>
										</li>
										<li><a data-toggle="tab" href="#tab3">Spatial 
										Criteria</a> </li>
									</ul>
									<div class="tab-content">
										<div id="tab1" class="tab-pane active ">
											<!-- <div class="row-fluid">
												<div class="span4 sc-col">
													<div class="row-fluid simple-content themeple_sc">
														<div class="header">
															<div class="icon_container img-circle">
																<span style="background-image: url('images/2012/09/njerez.png');">
																</span></div>
															<h2><a href="#">LTER 
															Sites</a> </h2>
															<div class="content">
																Lorem ipsum dolor 
																slo onsec</div>
														</div>
													</div>
												</div>
												<div class="span4 sc-col">
													<div class="row-fluid simple-content themeple_sc">
														<div class="header">
															<div class="icon_container img-circle">
																<span style="background-image: url('images/2012/09/njerez.png');">
																</span></div>
															<h2><a href="#">Responsive 
															Theme</a> </h2>
															<div class="content">
																Lorem ipsum dolor 
																slo onsec</div>
														</div>
													</div>
												</div>
												<div class="span4 sc-col">
													<div class="row-fluid simple-content themeple_sc">
														<div class="header">
															<div class="icon_container img-circle">
																<span style="background-image: url('images/2012/09/njerez.png');">
																</span></div>
															<h2><a href="#">World 
															Service</a> </h2>
															<div class="content">
																Lorem ipsum dolor 
																slo onsec</div>
														</div>
													</div>
												</div>
											</div> -->
											<form id="advancedSearchForm" action="./advancedSearch" method="post" name="advancedSearchForm" onsubmit="return submitRequest(this)">
												<table>
													<tr>
														<td valign="top">
														<h3 class="separator_border labelBolder span4" for="advancedsearch">
														LTER Sites</h3>
														</td>
														<td valign="top"></td>
														<td valign="top">
														<h3 class="separator_border labelBolder span4" for="advancedsearch">
														Taxonomic Criteria</h3>
														</td>
														<td valign="top"></td>
													</tr>
													<tr>
														<td class="spacersmh">
														</td>
													</tr>
													<tr>
														<td valign="top">
														<select multiple="multiple" name="siteValues" size="17">
														<option value="AND">Andrews 
														LTER</option>
														<option value="ARC">Arctic 
														LTER</option>
														<option value="BES">Baltimore 
														Ecosystem Study</option>
														<option value="BNZ">Bonanza 
														Creek LTER</option>
														<option value="CAP">Central 
														Arizona - Phoenix Urban 
														LTER</option>
														<option value="CCE">California 
														Current Ecosystem
														</option>
														<option value="CDR">Cedar 
														Creek Ecosystem Science 
														Reserve</option>
														<option value="CWT">Coweeta 
														LTER</option>
														<option value="FCE">Florida 
														Coastal Everglades LTER
														</option>
														<option value="GCE">Georgia 
														Coastal Ecosystems LTER
														</option>
														<option value="HBR">Hubbard 
														Brook LTER</option>
														<option value="HFR">Harvard 
														Forest LTER</option>
														<option value="JRN">Jornada 
														Basin LTER</option>
														<option value="KBS">Kellogg 
														Biological Station LTER
														</option>
														<option value="KNZ">Konza 
														Prairie LTER</option>
														<option value="LNO">LTER 
														Network Office</option>
														<option value="LUQ">Luquillo 
														LTER</option>
														<option value="MCM">McMurdo 
														Dry Valleys LTER
														</option>
														<option value="MCR">Moorea 
														Coral Reef LTER</option>
														<option value="NIN">North 
														Inlet LTER</option>
														<option value="NTL">North 
														Temperate Lakes LTER
														</option>
														<option value="NWT">Niwot 
														Ridge LTER</option>
														<option value="PAL">Palmer 
														Antarctica LTER</option>
														<option value="PIE">Plum 
														Island Ecosystems LTER
														</option>
														<option value="SBC">Santa 
														Barbara Coastal LTER
														</option>
														<option value="SEV">Sevilleta 
														LTER</option>
														<option value="SGS">Shortgrass 
														Steppe</option>
														<option value="VCR">Virginia 
														Coast Reserve LTER
														</option>
														</select> </td>
														<td class="spacerwd" valign="top">
														</td>
														<td valign="top">
														<table>
															<tr>
																<td>
																<h3 for="advancedsearchleft">
																Taxon: </h3>
																<select name="taxonQueryType">
																<option selected="selected" value="0">
																contains
																</option>
																<option value="1">
																matches exactly
																</option>
																<option value="2">
																starts with
																</option>
																<option value="3">
																ends with
																</option>
																</select> </td>
																<td></td>
															</tr>
															<tr>
																<td>
																<input name="taxon" style="width: 190px" type="text" value="" /></td>
															</tr>
														</table>
														</td>
														<td class="spacerwd" valign="top">
														</td>
													</tr>
												</table>
											</form>
											<!--  -->
											<div class="row-fluid text_bar_pattern themeple_sc">
											
												<!--  -->
												<!--  -->
												<!-- Search Options Table -->												
												<table style="float:right">
													<tr>
													
														<td colspan="5" valign="top">
														<h3 class="separator_border labelBolder span3" for="advancedsearch">
														Search Options</h3>
														</td>
													</tr>
													<tr>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
													</tr>
													<tr>
														<td>
															<ul class="checklist">
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Case Sensitive?</p>
																<a class="checkbox-select" href="#">
																Select</a>
																<a class="checkbox-deselect" href="#">
																Cancel</a>
																</li>
															</ul>
														</td>
														<td class="spacerwd">
														</td>
														<td valign="top">
															<input checked="checked" name="formAllAny" type="radio" value="0" /> 
															&quot;<strong>And</strong>&quot; 
															all search 
															criteria
														</td>
														<td class="spacerwd">
														</td>
														<td valign="top">
															<input name="formAllAny" type="radio" value="1" /> 
															&quot;<strong>Or</strong>&quot; 
															all search 
															criteria
														</td>
													</tr>
												</table>												
												<!-- /Search Options Table -->
												<!--  -->
												<!--  -->
												
												<div class="span12">
													<span class="row-fluid separator_border"></span>
												</div>
												<a class="btn btn-large btn-info btn-default" href="#" style="float: right">
												Clear</a>
												<a class="btn btn-large btn-info btn-default" href="#" style="float: right; margin-right: 5px">
												Submit</a>
										
											</div>
										</div>
										<div id="tab2" class="tab-pane  ">
											<div class="row-fluid text_bar_pattern themeple_sc">
												<!--  -->
												<div>
													<table>
														<tr>
															<td colspan="6">
															<h3 class="separator_border labelBolder span1" for="advancedsearch">
															Subject</h3>
															</td>
														</tr>
														<tr>
															<td class="spacersm2">
															</td>
														</tr>
														<tr>
															<td>
															<select name="subjectField">
															<option value="ALL">
															Subject</option>
															<option value="TITLE">
															Title Only</option>
															<option value="ABSTRACT">
															Abstract Only
															</option>
															<option selected="" value="KEYWORDS">
															Keywords Only
															</option>
															</select>
															<select name="subjectQueryType">
															<option selected="selected" value="0">
															contains</option>
															<option value="1">matches 
															exactly</option>
															<option value="2">starts 
															with</option>
															<option value="3">ends 
															with</option>
															</select>
															<input name="subjectValue" type="text" value="" />
															</td>
														</tr>
													</table>
													<table>
														<tr>
															<td>
															<input checked="checked" name="subjectAllAny" type="radio" value="0" /> 
															Match All Terms </td>
															<td class="spacerwd">
															</td>
															<td>
															<input name="subjectAllAny" type="radio" value="1" /> 
															Match Any Term </td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
													</table>
													<table>
														<tr>
															<td>
															<ul class="checklistLG">
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>More Specific 
																Terms</p>
																<a class="checkboxLG-select" href="#">
																Select</a>
																<a class="checkboxLG-deselect" href="#">
																Cancel</a> </li>
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Related Terms</p>
																<a class="checkboxLG-select" href="#">
																Select</a>
																<a class="checkboxLG-deselect" href="#">
																Cancel</a> </li>
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Related / More 
																Specific Terms</p>
																<a class="checkboxLG-select" href="#">
																Select</a>
																<a class="checkboxLG-deselect" href="#">
																Cancel</a> </li>
															</ul>
															</td>
														</tr>
													</table>
													<table>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
														<tr>
															<td>
															<h3 class="separator_border labelBold span1" for="advancedsearch">
															Creators / Organizations</h3>
															</td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
														<tr>
															<td>
															<h3 for="advancedsearchleft">
															Creator&#39;s Last Name:</h3>
															<select name="creatorSurnameQueryType">
															<option selected="selected" value="0">
															contains</option>
															<option value="1">matches 
															exactly</option>
															<option value="2">starts 
															with</option>
															<option value="3">ends 
															with</option>
															</select>
															<input name="creatorSurname" type="text" value="" />
															</td>
														</tr>
														<tr>
															<td>
															<h3 for="advancedsearchleft">
															Creator&#39;s Organization:</h3>
															<select name="creatorOrganizationQueryType">
															<option selected="selected" value="0">
															contains</option>
															<option value="1">matches 
															exactly</option>
															<option value="2">starts 
															with</option>
															<option value="3">ends 
															with</option>
															</select>
															<input name="creatorOrganization" type="text" value="" />
															</td>
														</tr>
													</table>
													<table>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
														<tr>
															<td colspan="12">
															<h3 class="separator_border labelBold span1" for="advancedsearch">
															Temporal Criteria</h3>
															</td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
														<tr>
															<td>
															<input checked="checked" name="dateField" type="radio" value="COLLECTION" /> 
															Collection Date </td>
															<td class="spacerwd">
															</td>
															<td>
															<input name="dateField" type="radio" value="PUBLICATION" /> 
															Publication Date
															</td>
															<td class="spacerwd">
															</td>
															<td>
															<input name="dateField" type="radio" value="ALL" /> 
															Either </td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
													</table>
													<table>
														<tr>
															<td>
															<label for="userId">
															Start Date</label>
															<input name="begin" placeholder="YYYY-MM-DD" size="15px" type="date" />
															</td>
															<td></td>
															<td>
															<label for="group">End 
															Date</label>
															<input name="end" placeholder="YYYY-MM-DD" size="15px" type="date" />
															</td>
														</tr>
													</table>
													<table>
														<tr>
															<td colspan="2">
															<ul class="checklistLG">
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Dataset is Contained 
																within Start / End 
																Dates</p>
																<a class="checkboxLG-select" href="#">
																Select</a>
																<a class="checkboxLG-deselect" href="#">
																Cancel</a> </li>
															</ul>
															</td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
														<tr>
															<td>
															<label for="advancedsearchleft">
															Named Time-scale:</label>
															</td>
														</tr>
														<tr>
															<td>
															<select name="namedTimescaleQueryType">
															<option selected="selected" value="0">
															contains</option>
															<option value="1">matches 
															exactly</option>
															<option value="2">starts 
															with</option>
															<option value="3">ends 
															with</option>
															</select> </td>
															<td></td>
															<td>
															<input name="namedTimescale" style="width: 190px" type="text" value="">
															</td>
														</tr>
													</table>
												</div>
												<!--  -->
												<!--  -->
												<!--  -->
												<!--  -->
												<!--  -->
												<div class="row-fluid text_bar_pattern themeple_sc">
												<!--  -->
												<!--  -->
												<!-- Search Options Table -->												
												<table style="float:right">
													<tr>
													
														<td colspan="5" valign="top">
														<h3 class="separator_border labelBolder span3" for="advancedsearch">
														Search Options</h3>
														</td>
													</tr>
													<tr>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
													</tr>
													<tr>
														<td>
															<ul class="checklist">
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Case Sensitive?</p>
																<a class="checkbox-select" href="#">
																Select</a>
																<a class="checkbox-deselect" href="#">
																Cancel</a>
																</li>
															</ul>
														</td>
														<td class="spacerwd">
														</td>
														<td valign="top">
															<input checked="checked" name="formAllAny" type="radio" value="0" /> 
															&quot;<strong>And</strong>&quot; 
															all search 
															criteria
														</td>
														<td class="spacerwd">
														</td>
														<td valign="top">
															<input name="formAllAny" type="radio" value="1" /> 
															&quot;<strong>Or</strong>&quot; 
															all search 
															criteria
														</td>
													</tr>
												</table>												
												<!-- /Search Options Table -->
												<!--  -->
												<!--  -->
												
												<div class="span12">
													<span class="row-fluid separator_border"></span>
												</div>
												<a class="btn btn-large btn-info btn-default" href="#" style="float: right">
												Clear</a>
												<a class="btn btn-large btn-info btn-default" href="#" style="float: right; margin-right: 5px">
												Submit</a>
										
											</div>
											<!--  -->
											<!--  -->
											<!--  -->
											<!--  -->
											<!--  -->
											</div>
										</div>
										<div id="tab3" class="tab-pane  ">
											<div class="row-fluid text_bar_pattern themeple_sc" style="left: 0px; top: 0px">
												<!--  -->
												<div class="figure floatleft span12">
													<h3 class="separator_border labelBolder span12" for="advancedsearch">
													Spatial Criteria</h3>
													<input name="boundsChangedCount" type="hidden" value="0" />
													<script src="https://maps.googleapis.com/maps/api/js?key=AIzaSyAcbgq4MRleYDjHPQoQazyHMAiavmj0s0U&amp;sensor=false" type="text/javascript">
            										</script>
													<script src="./js/map_functions.js" type="text/javascript"></script>
													<script type="text/javascript">google.maps.event.addDomListener(window, 'load', initialize);
            										</script>
													<div id="map-canvas" style="margin: 0 auto; width: 330px; height: 258px">
													</div>
													<div style="margin: 0 auto; width: 330px;">
														Zoom in to the region you&#39;d 
														like to search </div>
													<table width="100%">
														<tr>
															<td></td>
															<td align="center">
															<label for="advancedsearchleft">
															N:
															<input maxlength="12" name="northBound" onchange="boundsChanged()" size="12" type="text" value="90.0" /></label>
															</td>
															<td></td>
														</tr>
														<tr>
															<td align="left">
															<label for="advancedsearchleft">
															W:
															<input maxlength="12" name="westBound" onchange="boundsChanged()" size="12" type="text" value="-180.0" /></label>
															</td>
															<td></td>
															<td align="right">
															<label for="advancedsearchleft">
															E:
															<input maxlength="12" name="eastBound" onchange="boundsChanged()" size="12" type="text" value="180.0" /></label>
															</td>
														</tr>
														<tr>
															<td></td>
															<td align="center">
															<label for="advancedsearchleft">
															S:
															<input maxlength="12" name="southBound" onchange="boundsChanged()" size="12" type="text" value="-90.0" /></label>
															</td>
															<td></td>
														</tr>
														<tr>
															<td colspan="3">
															<ul class="checklistLG">
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Dataset is Contained 
																within Boundaries</p>
																<a class="checkboxLG-select" href="#">
																Select</a>
																<a class="checkboxLG-deselect" href="#">
																Cancel</a> </li>
															</ul>
															</td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
														<tr>
															<td colspan="2">
															<label for="advancedsearchleft">
															Geographic Place Name:</label>
															<input name="locationName" size="40" type="text" value="" /></td>
														</tr>
														<tr>
															<td class="spacersmh">
															</td>
														</tr>
													</table>
												</div>
												<!--  -->
												<!--  -->
												<!--  -->
												<!--  -->
												<!--  -->
												<div class="row-fluid text_bar_pattern themeple_sc">
												<!--  -->
												<!--  -->
												<!-- Search Options Table -->												
												<table style="float:right">
													<tr>
													
														<td colspan="5" valign="top">
														<h3 class="separator_border labelBolder span3" for="advancedsearch">
														Search Options</h3>
														</td>
													</tr>
													<tr>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
														<td class="spacer">
														</td>
													</tr>
													<tr>
														<td>
															<ul class="checklist">
																<li>
																<input name="jqdemo" type="checkbox" value="value1">
																<p>Case Sensitive?</p>
																<a class="checkbox-select" href="#">
																Select</a>
																<a class="checkbox-deselect" href="#">
																Cancel</a>
																</li>
															</ul>
														</td>
														<td class="spacerwd">
														</td>
														<td valign="top">
															<input checked="checked" name="formAllAny" type="radio" value="0" /> 
															&quot;<strong>And</strong>&quot; 
															all search 
															criteria
														</td>
														<td class="spacerwd">
														</td>
														<td valign="top">
															<input name="formAllAny" type="radio" value="1" /> 
															&quot;<strong>Or</strong>&quot; 
															all search 
															criteria
														</td>
													</tr>
												</table>												
												<!-- /Search Options Table -->
												<!--  -->
												<!--  -->
												
												<div class="span12">
													<span class="row-fluid separator_border"></span>
												</div>
												<a class="btn btn-large btn-info btn-default" href="#" style="float: right">
												Clear</a>
												<a class="btn btn-large btn-info btn-default" href="#" style="float: right; margin-right: 5px">
												Submit</a>
										
											</div>
											<!--  -->
											<!--  -->
											<!--  -->
											<!--  -->
											<!--  -->
											<!--  -->
											</div>
										</div>
									</div>
								</div>
								<!--  -->
								<!--  -->
								<!-- /Content --></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<jsp:include page="footer.jsp" />
		
</div>

</body>

</html>
