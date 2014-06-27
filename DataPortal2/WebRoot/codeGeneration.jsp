<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
  final String pageTitle = "Code Generation";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
        
  String filename = (String) request.getAttribute("filename");
  String statisticalFileType = (String) request.getAttribute("statisticalFileType");
  String statisticalPackageName = (String) request.getAttribute("statisticalPackageName");
  String packageId = (String) request.getAttribute("packageId");
  String mapBrowseURL = (String) request.getAttribute("mapBrowseURL");
  String instructions = (String) request.getAttribute("instructions");
  String programCode = (String) request.getAttribute("programCode");
  session.setAttribute("programCode", programCode);
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
								<h2><%= statisticalPackageName %> Code</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
									<div class="display-table">										
										<div class="table-row">										
											<div class="table-cell">
												<label class="labelBold text-align-right margin-right-15">Package ID:</label>
											</div>											
											<div class="table-cell">
												<a class='searchsubcat' href="<%= mapBrowseURL %>"><%= packageId %></a>
											</div>											
										</div>
										<div class="table-row">										
											<div class="table-cell">
												<label class="labelBold text-align-right margin-right-15">File Download:</label>
											</div>											
											<div class="table-cell">
												<a  class="searchsubcat" href="codegenerationdownload?filename=<%= filename %>"><%= filename %></a>
											</div>							
										</div>
										<div class="table-row">										
											<div class="table-cell">
												<label class="labelBold text-align-right margin-right-15">Instructions:</label>
											</div>											
											<div class="table-cell"><%= instructions %></div>							
										</div>
										<div class="table-row">										
											<div class="table-cell">
												<label class="labelBold text-align-right margin-right-15">Code:</label>
											</div>											
											<div class="table-cell">						
												<textarea id="programCodeTextArea" name="programCodeTextArea" cols="124" rows="12"><%= programCode %></textarea>
											</div>
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
