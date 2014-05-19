<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
  final String pageTitle = "Help";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
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
								<h2>How do I...</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->

			<dl>

				<dt>Search for LTER data?</dt>
				<dd><a href="https://nis.lternet.edu:8443/x/IoBH" class="searchsubcat">Searching for data</a></dd>
			  
				<dt>Find out more about the LTER Network Information System (<abbr title="Network Information System">NIS</abbr>) and its mission?</dt>
			  <dd><a href="https://nis.lternet.edu:8443/x/agBP" class="searchsubcat">LTER Network Information System Community Website</a></dd>
			  
				<dt>Use the <abbr title="Network Information System">NIS</abbr> Data Portal to create synthetic data?</dt>
				<dd><a href="https://nis.lternet.edu:8443/x/NQFZ" class="searchsubcat">How does the <abbr title="Network Information System">NIS</abbr> enable Network Synthesis?</a></dd>
				
				<dt>Find out who has been downloading my data?</dt> 
			  <dd><a href="https://nis.lternet.edu:8443/x/OwFZ" class="searchsubcat">Who is using my data?</a></dd>

				<dt>Learn about the <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> Software Developer's Application Programming Interface (API)?</dt>
			  <dd><a href="https://nis.lternet.edu:8443/x/BAAF" class="searchsubcat"><abbr title="Network Information System">NIS</abbr> Software Developer's Guide</a></dd>				

			</dl>

			<p>
				Have more questions? Go to the 
				<a href="https://nis.lternet.edu:8443/x/swFZ" class="searchsubcat">Frequently Asked Questions</a> page, 
				enter your question as a comment in either the <cite><abbr title="Network Information System">NIS</abbr> User's Guide</cite> or the 
				<cite><abbr title="Network Information System">NIS</abbr> Software Developer's Guide</cite>, or you can always email 
				<a href="mailto:tech-support@lternet.edu" class="searchsubcat">tech-support@lternet.edu</a>.
			</p>
								<!-- /Content -->
							</div>
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
