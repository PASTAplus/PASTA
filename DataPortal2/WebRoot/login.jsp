<%
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
<title>LTER :: Network Data Portal</title>

<meta charset="UTF-8" />
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
								<h2>Login</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								
				<p class="warning" align="center"><%=message%></p>

					<form id="login" name="loginform" method="post" action="./login"
						target="_top">
						<table id="login-form">
							<tbody>
								<tr>
									<td align="right"><label for="uid">User Name:</label></td>
									<td align="right">
									   <input type="text" name="uid"
										size="25px" required="required" autocomplete="on" autofocus />
									</td>
								</tr>
								<tr>
									<td align="right"><label for="password">Password:</label></td>
									<td align="right">
									   <input type="password" name="password"
										size="25px" required="required" />
									</td>
								</tr>
								<tr>
									<td></td>
									<td>
										<input class="btn btn-info btn-default" name="login" type="submit" value="Login" />
										<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
									</td>
								</tr>
							</tbody>
						</table>
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

		<!-- Can be removed, loads charts demo -->
		<script src="charts/assets/effects.js"></script>
		<!-- /Can be removed, loads charts demo -->

</body>

</html>
