<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
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
	String type = (String) request.getAttribute("type");

	if (type == null) {
		type = "";
	} else {
		type = "class=\"" + type + "\"";
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

<!-- For Custom Checkboxes -->
<script src="charts/assets/jquery.min.js" type="text/javascript"></script>
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
								<!-- Content --><fieldset>
								<p>Review a PASTA Data Package access report by 
								entering information into one or more of the filters 
								below, then select &quot;Submit&quot;:</p>
								<form id="dataPackageAudit" action="./dataPackageAudit" method="post" name="dataPackageAudit">
									<div class="section">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="scope">
												Package Id:</label></td>
											</tr>
											<tr>
												<td><label for="scope">Scope</label>
												<input autofocus name="scope" placeholder="knb-lter-xyz" size="30px" type="text" />
												</td>
												<td><label for="identifier">Identifier</label>
												<input name="identifier" size="5px" type="number" />
												</td>
												<td><label for="revision">Revision</label>
												<input name="revision" size="5px" type="number" />
												</td>
											</tr>
											<tr>
												<td class="spacer"></td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">Resource 
												type:</label></td>
											</tr>
											<tr>
												<td class="spacersm"></td>
											</tr>
											<tr>
												<td>
												<form>
													<fieldset>
													<label for="choices">
													<ul class="checklist">
														<li>
														<input name="package" type="checkbox" value="value1" />
														<p>Package</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
														<li>
														<input name="metadata" type="checkbox" value="value2" />
														<p>Metadata</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
														<li>
														<input name="entity" type="checkbox" value="value4" />
														<p>Data</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
														<li>
														<input name="report" type="checkbox" value="value3" />
														<p>Report</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
													</ul>
													</label></fieldset>
												</form>
												</td>
											</tr>
											<tr>
												<td class="spacer"></td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">Who:</label></td>
											</tr>
											<tr>
												<td><label for="userId">User Name
												</label>
												<input name="userId" size="30px" type="text" />
												</td>
												<td><label for="group">Group
												</label>
												<input name="group" size="30px" type="text" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">Date:</label></td>
											</tr>
											<tr>
												<td><label for="userId">Begin</label>
												<input name="begin" placeholder="YYYY-MM-DD" size="15px" type="date" />
												</td>
												<td><label for="group">End</label>
												<input name="end" placeholder="YYYY-MM-DD" size="15px" type="date" />
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
										</table>
									</div>
									<!-- section -->
					<%
						if (reportMessage != null && type.equals("class=\"warning\"")) {
							out.println("<div class=\"section\">\n");
							out.println("<table align=\"left\" cellpadding=\"4em\">\n");
							out.println("<tbody>\n");
							out.println("<tr>\n");
							out.println("<td " + type + ">\n");
							out.println(reportMessage + "\n");
							out.println("</td>\n");
							out.println("</tr>\n");
							out.println("</tbody>\n");
							out.println("</table>\n");
						}
					%>
								</form>
								</fieldset>
			<%
				if (reportMessage != null && type.equals("class=\"info\"")) {
					out.println(reportMessage);
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

		<script src="charts/assets/effects.js"></script>

</body>

</html>
