<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
  final String pageTitle = "Provenance Generator";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String provenanceHTML = (String) request.getAttribute("provenanceHTML");
  String source = (String) request.getAttribute("source");
  String derived = (String) request.getAttribute("derived");
  
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

<!-- JS-GRAPH-IT (for provenance graph) -->
    <script type="text/javascript" src="js/js-graph-it.js"></script>
    <link rel="stylesheet" type="text/css" href="css/js-graph-it.css">
    <style>
      .canvas {
        font-family: tahoma;
      }
      .block {
        position: absolute;
        border: 1px solid #7DAB76;
        background-color: skyblue;
        padding: 3px;
      }
      .connector {
        background-color: #FF9900;
      }
      .source-label, .middle-label, .destination-label {
        padding: 5px;
      }
    </style>

</head>

<body onload="initPageObjects();">

<jsp:include page="header.jsp" />

<div class="row-fluid ">
	<div>
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h2>Provenance Graph</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->

    <div class="canvas" id="mainCanvas" style="width: 365px; height: 280px; border: 1px solid black;">
      <h3 class="block" id="derived" style="left: 15px; top: 15px;"><%= derived %></h3>
      <h3 class="block" id="source" style="left: 200px; top: 200px;"><%= source %></h3>
      <div class="connector derived source down_start down_end">
        <label class="source-label">derived</label>
        <label class="middle-label">is derived from</label>
        <label class="destination-label">source</label>
      </div>
    </div>

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
