<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";

	HttpSession httpSession = request.getSession();
	String citationMessage = (String) httpSession
	    .getAttribute("citationmessage");
	String html = (String) httpSession.getAttribute("html");

	if (html == null) {
		html = "";
	}
%>

<!DOCTYPE html>
<html>

<head>
<title>LTER :: Network Data Portal</title>

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

<!-- These Scripts are for my Chart Demo and can be removed at any time -->
<script src="charts/assets/Chart.js" type="text/javascript"></script>
<script src="charts/assets/Chart_Demo.js" type="text/javascript"></script>
<script src="charts/assets/jquery.min.js" type="text/javascript"></script>
<!-- /These Scripts are for my Chart Demo and can be removed at any time -->

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
								<h2>Data Package Citation</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								
				<p>
					The LTER Network considers it a matter of professional ethics to
					acknowledge the work of other scientists. Thus, you, the <em>Data User</em>
					should properly cite the <em>Data Package</em> you utilize in any
					publication or in the metadata of any derived data product that is
					produced using the <em>Data Package</em>.
				<p>The citation should have the following general form:</p>

				<%
					if (citationMessage != null)
						out.println(citationMessage);
				%>

				<%=html%>
				
				<p>
					Please refer to the LTER Network <a
						href="http://www.lternet.edu/policies/data-access" target="_blank">Data
						Access Policy</a> for additional information.
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

		<!-- Can be removed, loads charts demo -->
		<script src="charts/assets/effects.js"></script>
		<!-- /Can be removed, loads charts demo -->

</body>

</html>
