<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
  final String pageTitle = "Login";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  HttpSession httpSession = request.getSession();
  
  String message = (String) request.getAttribute("message");
  String from = (String) request.getAttribute("from");
  
  if (from != null && !from.isEmpty()) {
    httpSession.setAttribute("from", from);
  }

  if (message == null) {
    message = "";
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
								<h2>Login</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								
				<p class="nis-warn"><%=message%></p>

					<form id="login" name="loginform" method="post" action="./login"
						target="_top">
						<div class="display-table">
								<div class="table-row">
									<div class="table-cell">
									  <label class="labelBold text-align-right">User Name:</label>
									</div>
									<div class="table-cell">
									   <input type="text" name="uid" size="25px" required="required" autocomplete="on" autofocus />
									</div>
								</div>
								<div class="table-row">
									<div class="table-cell">
									  <label class="labelBold text-align-right">Password:</label>
									</div>
									<div class="table-cell">
									  <input type="password" name="password" size="25px" required="required" />
									</div>
							  </div>
								<div class="table-row">
									<div class="table-cell">
									</div>
									<div class="table-cell">
										<input class="btn btn-info btn-default" name="login" type="submit" value="Login" />
										<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
									</div>
							  </div>
						</div>
					</form>
									
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
