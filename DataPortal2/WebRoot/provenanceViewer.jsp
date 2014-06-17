<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%
  final String pageTitle = "Provenance Viewer";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
      
  HttpSession httpSession = request.getSession();
  
    String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./provenanceViewer.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  
  String message = (String) request.getAttribute("message");
  String type = (String) request.getAttribute("type");
  String packageid = (String) request.getAttribute("packageid");

  if (type == null) {
    type = "";
  }
          
	boolean showProvenance = false;	
	if (message != null && !type.equals("warning")) {
		showProvenance = true;
	}
			
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
								<h2>Provenance Viewer</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->

							<c:set var="showProv" value="<%= showProvenance %>"/>						
							<c:choose>
							
								<c:when test="${showProv}">
									<div class="display-table">										
										<div class="table-row">										
											<div class="table-cell">
												<label class="labelBold">Package Identifier:</label>
											</div>											
											<div class="table-cell"><%= packageid %></div>											
										</div>
									</div>
									<p></p>
									<pre><%= message %></pre>							
								</c:when>
								
								<c:otherwise>

								<p>View provenance metadata of a data package using the package identifier.</p>
								<div class="section">
									<form id="provenanceviewer" action="provenanceViewer" method="post" name="provenanceviewer">
						        <div class="display-table">
											<div class="table-row">
												<div class="table-cell">
												  <label class="labelBold" for="packageid">Package Id:</label>
												</div>
										  </div>
											<div class="table-row">
												<div class="table-cell">
												  <input name="packageid" required="required" autofocus size="20" type="text" placeholder="e.g., knb-lter-nin.1.3" />
												</div>
											</div>
											<div class="table-row">
												<div class="table-cell">
												  <input class="btn btn-info btn-default" name="view" type="submit" value="View" />
												  <input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</div>
											</div>
										</div>
									</form>
								</div>

								</c:otherwise>
							</c:choose>

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
