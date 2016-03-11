<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
  final String pageTitle = "Contact Us";
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
								<h2>Contact Us</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<h3 align="left">People</h3>
								<ul>
									<li>
									<a class="searchsubcat" href="http://search.lternet.edu/directory_view.php?personid=13823">
									Mark Servilla</a>, <abbr title="Network Information System">NIS</abbr> Lead Scientist</li>
									<li>
									<a class="searchsubcat" href="http://search.lternet.edu/directory_view.php?personid=13757">
									Duane Costa</a>, <abbr title="Network Information System">NIS</abbr> Analyst/Programmer III</li>
									<li>
									<a class="searchsubcat" href="http://search.lternet.edu/directory_view.php?personid=10391">
									James Brunt</a>, Information Management Specialist</li>
								</ul>
								<h3 align="left">Physical Address</h3>
								<ul class="no-list-style">
									<li>LTER Network Office<br />
									Suite 320, CERIA Bldg #83,<br />
									University of New Mexico (Main Campus)<br />
									Albuquerque, New Mexico, USA<br />
									Phone: 505 277-2597<br />
									Fax: 505 277-2541<br />
                </li>
								</ul>
								<h3 align="left">Mailing Address</h3>
								<ul class="no-list-style">
									<li>LTER Network Office<br />
									UNM Dept of Biology, MSC03 2020<br />
									1 University of New Mexico<br />
									Albuquerque, New Mexico, USA<br />
									87131-0001</li>
								</ul>
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
