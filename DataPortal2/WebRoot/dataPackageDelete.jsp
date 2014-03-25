<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>

<%
  final String pageTitle = "Delete Data Packages";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();

  String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./dataPackageDelete.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }

  String deleteMessage = (String) request.getAttribute("deletemessage");
  String deleteMessageHTML = "";
  if (deleteMessage != null) {
    deleteMessageHTML = String.format("<p class=\"nis-info\">%s</p>", deleteMessage);
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
								<h2>Delete Data Packages</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<p>Delete a data package using the package <b>scope</b> and <b>identifier</b>.<sup>*</sup>
								</p>
								<div class="section">
									<form id="datapackagedelete" action="./dataPackageDelete" method="post" name="datapackagedelete">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="packageid">Package Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="packageid" required="required" type="text" autofocus  placeholder="e.g., myscope.100"/>
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="delete" type="submit" value="Delete" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
									</form>
								<p><sup>*</sup><strong><em>Please note</em></strong>: Deletion of a data package is 
								permanent and should be given careful consideration. 
								Once deleted, <em>no additional data packages</em> can be 
								uploaded with the specified combination of <var>scope</var> 
								and <var>identifier</var>.</p>
								</div>
                <%= deleteMessageHTML %>
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
