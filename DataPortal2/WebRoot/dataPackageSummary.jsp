<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
  final String pageTitle = "Data Package Summary";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);

  String titleHTML = (String) request.getAttribute("dataPackageTitleHTML");
  String creatorsHTML = (String) request.getAttribute("dataPackageCreatorsHTML");
  String publicationDateHTML = (String) request.getAttribute("dataPackagePublicationDateHTML");
  String packageIdHTML = (String) request.getAttribute("dataPackageIdHTML");
  String resourcesHTML = (String) request.getAttribute("dataPackageResourcesHTML");
  String citationHTML = (String) request.getAttribute("dataPackageCitationHTML");
  String digitalObjectIdentifier = (String) request.getAttribute("digitalObjectIdentifier");
  String pastaDataObjectIdentifier = (String) request.getAttribute("pastaDataObjectIdentifier");
  String provenanceHTML = (String) request.getAttribute("provenanceHTML");
  String codeGenerationHTML = (String) request.getAttribute("codeGenerationHTML");

  String uid = (String) session.getAttribute("uid");
  boolean showPubDate = !(publicationDateHTML == null || publicationDateHTML.isEmpty());
  boolean showCodeGeneration = !(codeGenerationHTML == null || codeGenerationHTML.isEmpty());
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
								<h2>Data Package Summary</h2>
							</div>		
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<div class="display-table">

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Title:</label>
										</div>
										<div class="table-cell">
											<%= titleHTML %>
										</div>
									</div>
											
									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Creators:</label>
										</div>
										<div class="table-cell">
											<%= creatorsHTML %>
										</div>											
									</div>

							<c:set var="showDate" value="<%= showPubDate %>"/>
							<c:choose>
								<c:when test="${showDate}">
									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Publication Date:</label>
										</div>
										<div class="table-cell">
											<%= publicationDateHTML %>
										</div>											
									</div>
								</c:when>
							</c:choose>

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Package ID:</label>
										</div>
										<div class="table-cell">
											<%= packageIdHTML %>
										</div>											
									</div>

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Resources:</label>
										</div>
										<div class="table-cell">
											<%= resourcesHTML %>
										</div>											
									</div>

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Digital Object Identifier:</label>
										</div>
										<div class="table-cell">
											<ul class="no-list-style">
												<li><%= digitalObjectIdentifier %></li>
											</ul>
										</div>											
									</div>

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">PASTA Identifier:</label>
										</div>
										<div class="table-cell">
											<ul class="no-list-style">
												<li><%= pastaDataObjectIdentifier %></li>
											</ul>
										</div>											
									</div>

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Citation:</label>
										</div>
										<div class="table-cell">
											<ul class="no-list-style">
												<li><%= citationHTML %></li>
											</ul>
										</div>											
									</div>

									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Provenance:</label>
										</div>
										<div class="table-cell">
											<ul class="no-list-style">
												<li><%= provenanceHTML %></li>
											</ul>
										</div>											
									</div>

							<c:set var="showCodeGen" value="<%= showCodeGeneration %>"/>
							<c:if test="${showCodeGen}">
									<div class="table-row">										
										<div class="table-cell text-align-right">
											<label class="labelBold">Code Generation:</label>
										</div>
										<div class="table-cell">
											<ul class="no-list-style">
												<li><%= codeGenerationHTML %></li>
											</ul>
										</div>											
									</div>
							</c:if>
									
								</div>
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
