<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.ConfigurationListener"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
  final String pageTitle = "Data Package Access Reports";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";

	HttpSession httpSession = request.getSession();

	String uid = (String) httpSession.getAttribute("uid");

	if (uid == null || uid.isEmpty()) {
		request.setAttribute("from", "./dataPackageAudit.jsp");
		String loginWarning = DataPortalServlet.getLoginWarning();
		request.setAttribute("message", loginWarning);
		RequestDispatcher requestDispatcher = request
		    .getRequestDispatcher("./login.jsp");
		requestDispatcher.forward(request, response);
	}

	String reportMessage = (String) request.getAttribute("reportMessage");

	String limitHTML = "";
	String auditRecordLimit = (String) ConfigurationListener.getOptions().getProperty("auditreport.limit");
	if (auditRecordLimit != null && !auditRecordLimit.equals("")) {
	  limitHTML = "<p><small><sup>*</sup><em>Only the first " + auditRecordLimit + " matching audit records will be displayed.</em></small></p>";
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

<!-- For Custom Checkboxes -->
<script src="js/jquery-1.8.3.min.js" type="text/javascript"></script>
<script type="text/javascript">

	$(document).ready(function() {
		$(".checklist .checkbox-select").click(
			function(event) {
				event.preventDefault();
				$(this).parent().addClass("selected");
				$(this).parent().find(":checkbox").attr("checked","checked");
				
			}
		);
		
		$(".checklist .checkbox-deselect").click(
			function(event) {
				event.preventDefault();
				$(this).parent().removeClass("selected");
				$(this).parent().find(":checkbox").removeAttr("checked");
				
			}
		);
		
	});

</script>
<!-- /For Custom Checkboxes -->

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
								<h2>Data Package Access Reports</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<p>Review a Data Package access report<sup>*</sup> by entering information into one or more of the filters 
								   below.
								</p>
								<form id="dataPackageAudit" action="./dataPackageAudit" method="post" name="dataPackageAudit">
									<div class="section">
										<table>
											<tr>
												<td>
												  <label class="labelBold">Scope:</label>
												  <input autofocus required name="scope" size="15" type="text" placeholder="e.g., knb-lter-nin" />
												</td>
												<td>
												  <label class="labelBold">Identifier:</label>
												  <input name="identifier" size="5" type="number"  placeholder="e.g., 1"/>
												</td>
												<td>
												  <label class="labelBold">Revision:</label>
												  <input name="revision" size="5" type="number"  placeholder="e.g., 3"/>
												</td>
											</tr>
											<tr>
												<td class="spacer"></td>
											</tr>
										</table>
										
										<table>
											<tr>
												<td><label class="labelBold">Resource Type:</label></td>
											</tr>
											<tr>
												<td class="spacersm"></td>
											</tr>
											<tr>
												<td>
												  <form>
													    <label for="choices">
													    <ul class="checklist">
														    <li>
														      <input name="package" type="checkbox" value="value1" />
														      <p>Package</p>
														      <a class="checkbox-select" href="#">Select</a>
														      <a class="checkbox-deselect" href="#">Cancel</a> 
														    </li>
														    <li>
														      <input name="metadata" type="checkbox" value="value2" />
														      <p>Metadata</p>
														      <a class="checkbox-select" href="#">Select</a>
														      <a class="checkbox-deselect" href="#">Cancel</a>
														    </li>
														    <li>
														      <input name="entity" type="checkbox" value="value4" />
														      <p>Data</p>
														      <a class="checkbox-select" href="#">Select</a>
														      <a class="checkbox-deselect" href="#">Cancel</a>
														    </li>
													    	<li>
														      <input name="report" type="checkbox" value="value3" />
														      <p>Report</p>
														      <a class="checkbox-select" href="#">Select</a>
														      <a class="checkbox-deselect" href="#">Cancel</a>
														    </li>
													    </ul>
													    </label>
												  </form>
												</td>
											</tr>
											<tr>
												<td class="spacer"></td>
											</tr>
										</table>
										<table>
											<tr>
												<td>
												  <label class="labelBold">User Name:</label>
												  <input name="userId" size="15" type="text" />
												</td>
												<td>
												  <label class="labelBold">Group:</label>
												  <input name="group" size="15" type="text" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td>
												  <label class="labelBold">Begin Date:</label>
												  <input name="begin" placeholder="YYYY-MM-DD" type="date" />
												</td>
												<td>
												  <label class="labelBold">End Date:</label>
												  <input name="end" placeholder="YYYY-MM-DD" type="date" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td></td>
												<td colspan="3">
												  <input class="btn btn-info btn-default" name="submit" type="submit" value="Submit" />
												  <input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
							        <tr>
								        <td colspan="3"><%= limitHTML %></td>
							        </tr>
										</table>
									</div>
									<!-- section -->
					<%
						if (reportMessage != null) {
							out.println(String.format("<p class=\"nis-warn\">%s</p>", reportMessage));
						}
					%>
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
