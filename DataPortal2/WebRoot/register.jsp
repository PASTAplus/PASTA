<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";

	HttpSession httpSession = request.getSession();

	String uid = (String) httpSession.getAttribute("uid");

%>

<!DOCTYPE html>
<html lang="en">
<head>
<title>LTER :: Network Data Portal</title>

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

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />

		<div class="content">

			<h2 align="center">New User Registration</h2>


			<div class="section">

      <p>The contributors of these data have requested additional user
        information prior to granting access. If you have an LTER login,
        please select the "login" link above. If not, you can register here
        for access to these data.  Clicking "Submit" will send a confirmation
        email to the address provided.  Thank you!</p>

				<form id="" name="" method="post" action="">

					<fieldset>
						<legend>User Information</legend>
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="givenName">First
											Name:</label></td>
									<td align="left" width="200px"><input type="text"
										name="givenName" required="required" size="30" /></td>
								</tr>
								<tr>
									<td align="left" width="130px"><label for="surName">Last
											Name:</label></td>
									<td align="left" width="200px"><input type="text"
										name="surName" required="required" size="30" /></td>
								</tr>
								<tr>
									<td align="left" width="130px"><label for="email">Email:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="email" required="required" size="30" /></td>
								</tr>
								<tr>
									<td align="left" width="130px"><label for="institution">Institution:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="institution" required="required" size="30" /></td>
								</tr>
							</tbody>
						</table>
					</fieldset>

					<fieldset>
						<legend>Data Use Purpose/Intent</legend>
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;Research
										with intent on publication</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;Research
										for background information</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;K-12
										student project</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;College
										student project</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;K-12
										teaching</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;College
										teaching</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;Resource
										management</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;Media</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;Information
										manager</td>
								</tr>
								<tr>
									<td><input type="radio" name="intent" />&nbsp;Other</td>
								</tr>

							</tbody>
						</table>
				</form>
				</fieldset>

				<p>
					<input type="checkbox" name="acknowledgement" />&nbsp; I
					acknowledge having read the <a
						href='http://www.lternet.edu/data/netpolicy.html'> LTER Data
						Policy</a> <br />
				</p>
				<p>
					<input type="submit" name="submit" value="Submit" /> <input
						type="reset" name="reset" value="Clear" />
				</p>
	
			</div>

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
