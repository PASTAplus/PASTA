<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
  final String pageTitle = "LTER Resources";
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
								<h2>LTER Resources</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<h3 align="left">Websites</h3>
								<ul>
									<li><a class="searchsubcat" href="http://www.lternet.edu">LTER Network</a></li>
									<!--
									<li><a class="searchsubcat" href="http://lno.lternet.edu">LTER Network Office</a></li>
									<li>
									  <a class="searchsubcat" href="https://nis.lternet.edu:8443/x/agBP">
								  	  <abbr title="Network Information System">NIS</abbr> Guides</a>
								    </li> 
								    -->
								</ul>
								<h3 align="left">Tools</h3>
								<ul>
									<li><a class="searchsubcat" href="http://vocab.lternet.edu/vocab/vocab/index.php">LTER Network Controlled Vocabulary</a></li>
									<li><a class="searchsubcat" href="http://unit.lternet.edu/unitregistry/">LTER Network Unit Registry</a></li>
									<li><a class="searchsubcat" href="https://lter.dev.gios.asu.edu/lter/html/index.php">LTER Network Information System Reporting Tool</a>
								  </li>
								</ul>
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
