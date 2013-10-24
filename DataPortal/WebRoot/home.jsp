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
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms"%>
<%@ page import="edu.lternet.pasta.portal.PastaStatistics"%>
<%
	HttpSession httpSession = request.getSession();
	httpSession.setAttribute("menuid", "home");

	String uid = (String) httpSession.getAttribute("uid");

	String path = request.getContextPath();
	String basePath = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort()
			+ path + "/";

	String jqueryString = LTERTerms.getJQueryString(); // for auto-complete using JQuery

	if (uid == null || uid.isEmpty()) {
		uid = "public";
	}

	// Generate PASTA data package statistics and store values in session.

	Integer numDataPackages = null;
	Integer numDataPackagesSites = null;
	String count = null;

	PastaStatistics pastaStats = new PastaStatistics("public");

	count = (String) httpSession.getAttribute("numDataPackages");
	if (count != null) {
		numDataPackages = Integer.valueOf(count);
	} else {
		numDataPackages = pastaStats.getNumDataPackages();
		httpSession.setAttribute("numDataPackages",
				numDataPackages.toString());
	}

	count = (String) httpSession.getAttribute("numDataPackagesSites");
	if (count != null) {
		numDataPackagesSites = Integer.valueOf(count);
	} else {
		numDataPackagesSites = pastaStats.getNumDataPackagesSites();
		httpSession.setAttribute("numDataPackagesSites",
				numDataPackagesSites.toString());
	}

 String hover = "New user registration for non-LTER members coming soon!";

%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Home</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">
<link rel="stylesheet" href="./css/jquery-ui-1.10.0.css" />

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

<script src="./js/jquery-ui-1.10.0.js"></script>
<script>
	$(function() {
		var availableTags = [
<%=jqueryString%>
	];

		$("#lterterms").autocomplete({
			source : availableTags
		});
	});
</script>

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
		data.addColumn('string', 'Week');
		data.addColumn('number', 'Packages');
		data.addColumn('number', 'Sites');
		data.addRows([ 
			['1', 0, 0],
			['2', 210, 5],
			['3', 281, 7],
			['4', 347, 7],
			['5', 374, 7],
			['6', 430, 8],
			['7', 436, 8],
			['8', 454, 9],
			['9', 657, 10],
			['10', 684, 10],
			['11', 708, 10],
			['12', 746, 10],
			['13', 763, 12],
			['14', 766, 12],
			['15', 829, 12],
			['16', 849, 12],
			['17', 894, 13],
			['18', 928, 13],
			['19', 936, 13],
			['20', 1142, 15],
			['21', 1274, 15],
			['22', 1346, 15],
			['23', 1404, 16],
			['24', 1430, 16],
			['25', 1451, 16],
			['26', 1461, 16],
			['27', 1466, 17],
			['28', 1484, 17],
			['29', 1717, 19],
			['30', 1729, 19],
			['31', 1734, 19],
			['32', 1777, 20],
			['33', 1787, 20],
			['34', 1810, 20],
			['35', 1824, 20],
			['36', 1955, 21],
			['37', 1966, 21],
			['38', 2025, 21],
			['39', 2055, 21],
			['40', 2504, 23],
			['41', 2620, 23],
			['42', 2640, 23],
			['43', 2692, 23]
		]);

		// Set chart options
		var options = {
			'title' : 'Site/Data Package Growth',
			'width' : 400,
			'height' : 200,
			'hAxis' : {
				title : 'Week'
			},
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
					targetAxisIndex : 0
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

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<div class="section">
				<table id="graph">
					<tbody>
						<tr>
							<td style="vertical-align: top;">
								<p>
									Data are one of the most valuable products of the Long Term
									Ecological Research (LTER) Network. Data and metadata derived
									from publicly funded research in the U.S. LTER Network are made
									available online with as few restrictions as possible, on a
									non-discriminatory basis. In return, the LTER Network expects
									data users to <strong>act ethically</strong> by contacting the
									investigator prior to the use of data for publication.
								</p>

								<p>
									The LTER Network Information System Data Portal contains
									ecological data packages contributed by past and present LTER
									sites. Please review the <a target="_top"
										href='http://www.lternet.edu/data/netpolicy.html'> LTER
										Data Policy</a> before downloading any data product. We request
									that you cite data sources in your published and unpublished
									works whenever possible. Digital object identifiers (DOI) are
									provided for each dataset to facilitate citation.
								</p>

								<p>
									Voluntary registration on this site will allow us to notify you
									of updates to data and metadata of interest and of corrections
									made to data. In addition, your validated login will gain you
									access to even more data where contributors have asked for
									additional information on use. <span name="<%=hover%>" 
									class="tooltip" style="color: blue;"><em>Click here</em></span>
									to register now.
								</p>

								<p>LTER Network scientists make every effort to release data in a
								timely fashion and with attention to accurate, well-designed and
								well-documented data.  To understand data fully, please read the
								associated metadata and contact data providers if you have any
								questions.  The LTER Network is not responsible for misinterpretation
								of data resulting from failure to consult metadata or data providers.</p>
								</td>
							<td align="left">
								<div id="chart_div"></div>
								<p align="center">
									Site contributed data packages: <em><%=numDataPackagesSites.toString()%></em><br />
									Total data packages: <em><%=numDataPackages.toString()%></em>
								</p>
							</td>
						</tr>
					</tbody>
				</table>
			</div>

			<div class="section">
				<p style="padding-left: 10px">Search for data packages using one
					or more terms separated by spaces</p>

				<form id="simplesearch" name="simplesearch" method="post"
					action="./simpleSearch">
        <p align="center">
          <label for="terms">Search Terms	(use * for any):</label>
					<input type="search" name="terms" required="required"
					  size="50" id="lterterms" style="font-size: 80%;" />
					&nbsp;&nbsp;&nbsp;
          <input type="submit" name="search" value="search" />
          <input type="reset" name="reset" value="reset" />
					(<a target="_top" href="./advancedSearch.jsp">Advanced Search</a>)
        </p>
				</form>
			</div>

		</div>
		<!-- end content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end wrapper -->

</body>
</html>
