<%@ page import="edu.lternet.pasta.portal.PastaStatistics"%>

<%
	// Generate PASTA data package statistics and store values in session.

	Integer numDataPackages = null;
	Integer numDataPackagesSites = null;
	String count = null;

	PastaStatistics pastaStats = new PastaStatistics("public");

	count = (String) session.getAttribute("numDataPackages");
	if (count != null) {
		numDataPackages = Integer.valueOf(count);
	} else {
		numDataPackages = pastaStats.getNumDataPackages();
		session.setAttribute("numDataPackages",
				numDataPackages.toString());
	}

	count = (String) session.getAttribute("numDataPackagesSites");
	if (count != null) {
		numDataPackagesSites = Integer.valueOf(count);
	} else {
		numDataPackagesSites = pastaStats.getNumDataPackagesSites();
		session.setAttribute("numDataPackagesSites",
				numDataPackagesSites.toString());
	}

 String hover = "New user registration for non-LTER members coming soon!";

%>

<!DOCTYPE html>
<html lang="en">

<head>

<title>LTER :: Network Data Portal</title>

<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

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

<!-- These Scripts are for my Chart Demo and can be removed at any time 
<script src="charts/assets/Chart.js" type="text/javascript"></script>
<script src="charts/assets/jquery.min.js" type="text/javascript"></script>
<!-- /These Scripts are for my Chart Demo and can be removed at any time -->

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
			['38', 1971, 21]
		]);

		// Set chart options
		var options = {
			'title' : '',
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
							to <strong>act ethically</strong> by contacting the 
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
							<p>Voluntary registration on this site will allow us 
							to notify you of updates to data and metadata of interest 
							and of corrections made to data. In addition, your validated 
							login will gain you access to even more data where contributors 
							have asked for additional information on use.
							<span name="New user registration for non-LTER members coming soon!" 
							class="tooltip" class="searchsubcat">Click here</span> to register now.
							</p>
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
						<div class="span12">
							<div class="recent_title">
								<h2>Site / Data Package Growth</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<!-- My Chart Code. Can be removed anytime -->
							<section id="examples">
								<article id="lineChart" class=" ">
									<div>
										<!-- 
										  <canvas id="introChart" width="320" height="250"></canvas> 
											<p>Site contributed data packages: <strong><i>1473</i></strong></p>
									  -->
										<br />
										<br />
								<div id="chart_div"></div>
								<p>Site contributed data packages: 
								  <strong><em><%=numDataPackagesSites.toString()%></em></strong><br />
									Total data packages: <strong><em><%=numDataPackages.toString()%></em></strong>
								</p>
											<!--   Total data packages: <strong><i>16888</i></strong> -->
									</div>
								</article>
							</section>
							<!-- /My Chart Code. Can be removed anytime -->
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
        
		<jsp:include page="footer.jsp" />
		
</div>

  <script src="charts/assets/effects.js"></script>
  
</body>

</html>
