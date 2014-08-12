<!--
  ~ Copyright 2011-2014 the University of New Mexico.
  ~
  ~ This work was supported by National Science Foundation Cooperative
  ~ Agreements #DEB-0832652 and #DEB-0936498.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~ http://www.apache.org/licenses/LICENSE-2.0.
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  ~ either express or implied. See the License for the specific
  ~ language governing permissions and limitations under the License.
  -->

<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.PastaStatistics"%>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms"%>
<%@ page import="edu.lternet.pasta.portal.statistics.GrowthStats"%>

<%
	final String pageTitle = "Home";
	final String titleText = DataPortalServlet.getTitleText(pageTitle);
	session.setAttribute("menuid", "home");

	String uid = (String) session.getAttribute("uid");
	if (uid == null || uid.isEmpty()) {
		uid = "public";
	}

	//String jqueryString = LTERTerms.getJQueryString(); // for auto-complete using JQuery

	// Generate PASTA data package statistics and store values in session.

	String numDataPackages = null;
	String numDataPackagesSites = null;
	PastaStatistics pastaStats = new PastaStatistics("public");

	numDataPackages = (String) application.getAttribute("numDataPackages");
	if (numDataPackages == null) {
		numDataPackages = pastaStats.getNumDataPackages().toString();
		application.setAttribute("numDataPackages", numDataPackages);
	}

	numDataPackagesSites = (String) application.getAttribute("numDataPackagesSites");
	if (numDataPackagesSites == null) {
		numDataPackagesSites = pastaStats.getNumDataPackagesSites().toString();
		application.setAttribute("numDataPackagesSites", numDataPackagesSites);
	}

    GregorianCalendar now = new GregorianCalendar();

    String googleChartJson = (String) application.getAttribute("googleChartJson");
    if (googleChartJson == null) {
        GrowthStats gs = new GrowthStats();
        googleChartJson = gs.getGoogleChartJson(now, Calendar.MONTH);
        application.setAttribute("googleChartJson", googleChartJson);
    }

    String hover = "New user registration for non-LTER members coming soon!";

%>

<!DOCTYPE html>
<html lang="en">

<head>
<title><%= titleText %></title>

<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon" />

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

<!-- Google Chart for NIS Data Package and Site Growth -->
<script type="text/javascript" src="https://www.google.com/jsapi"></script>
<script type="text/javascript">
	// Load the Visualization API and the piechart package.
	google.load('visualization', '1.0', {
		'packages' : [ 'corechart' ]
	});

	// Set a callback to run when the Google Visualization API is loaded.
	google.setOnLoadCallback(drawChart);

	// Callback that creates and populates a data table,
	// instantiates the pie chart, passes in the data and
	// draws it.
	function drawChart() {

		// Create the data table.
		var data = new google.visualization.DataTable();
		data.addColumn('string', 'Month');
		data.addColumn('number', 'Packages');
		data.addColumn('number', 'Sites');
		data.addRows([
            <%=googleChartJson%>
		]);

		// Set chart options
		var options = {
			'title' : 'Site/Data Package Growth',
			'width' :  400,
			'height' : 250,

			'vAxes' : {
				0 : {
					logScale : false
				},
				1 : {
					logScale : false,
					maxValue : 27
				}
			},
			'series' : {
				0 : {
					targetAxisIndex : 0,
                    type : "line"
				},
				1 : {
					targetAxisIndex : 1
				}
			}
		};

		// Instantiate and draw our chart, passing in some options.
		var chart = new google.visualization.ColumnChart(document
				.getElementById('chart_div'));
		chart.draw(data, options);
	}
</script>

</head>

<body>

<jsp:include page="header.jsp" />

<div class="row-fluid">
	<div class="span12">
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="span8 box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h2>Welcome to the LTER Network Data Portal</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<p>Data are one of the most valuable products of the 
							Long Term Ecological Research (LTER) Network. Data and 
							metadata derived from publicly funded research in the 
							U.S. LTER Network are made available online with as 
							few restrictions as possible, on a non-discriminatory 
							basis. In return, the LTER Network expects data users 
							to <em>act ethically</em> by contacting the 
							investigator prior to the use of data for publication.
							</p>
							<p>The LTER Network Information System Data Portal contains 
							ecological data packages contributed by past and present 
							LTER sites. Please review the
							<a class="searchsubcat" href="http://www.lternet.edu/data/netpolicy.html" target="_blank">
							LTER Data Policy</a> before downloading any data product. 
							We request that you cite data sources in your published 
							and unpublished works whenever possible. Digital object 
							identifiers (DOI) are provided for each dataset to facilitate 
							citation. </p>
							<!--  
							<p>Voluntary registration on this site will allow us 
							to notify you of updates to data and metadata of interest 
							and of corrections made to data. In addition, your validated 
							login will gain you access to even more data where contributors 
							have asked for additional information on use.
							<span name="New user registration for non-LTER members coming soon!" 
							class="tooltip">Register now</span>!
							</p>
							-->
							<p>LTER Network scientists make every effort to release 
							data in a timely fashion and with attention to accurate, 
							well-designed and well-documented data. To understand 
							data fully, please read the associated metadata and 
							contact data providers if you have any questions. The 
							LTER Network is not responsible for misinterpretation 
							of data resulting from failure to consult metadata or 
							data providers.</p>
						</div>
					</div>
				</div>
				<div class="span4 box_shadow box_layout">
					<div class="row-fluid">
						<div class="row-fluid">
								    <div id="chart_div"></div>
								    <p id="nis-growth">Site contributed data packages: <b><%= numDataPackagesSites %></b><br />
									     Total data packages: <b><%= numDataPackages %></b>
								    </p>
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
