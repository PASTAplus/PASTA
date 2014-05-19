<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.ConfigurationListener"%>
<%@ page import="edu.lternet.pasta.portal.AuditReportServlet"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
  final String pageTitle = "Audit Reports";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String serviceMethodsHTML = "";

	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";

	HttpSession httpSession = request.getSession();

	String uid = (String) httpSession.getAttribute("uid");

	if (uid == null || uid.isEmpty()) {
		request.setAttribute("from", "./auditReport.jsp");
		String loginWarning = DataPortalServlet.getLoginWarning();
		request.setAttribute("message", loginWarning);
		RequestDispatcher requestDispatcher = request
		    .getRequestDispatcher("./login.jsp");
		requestDispatcher.forward(request, response);
	}
	else {
	  AuditReportServlet ars = new AuditReportServlet();
	  serviceMethodsHTML = ars.serviceMethodsHTML(uid);
	}

	String reportMessage = (String) request.getAttribute("reportMessage");
	
	String limitHTML = "";
	String auditRecordLimit = (String) ConfigurationListener.getOptions().getProperty("auditreport.limit");
	if (auditRecordLimit != null && !auditRecordLimit.equals("")) {
	  limitHTML = "<sup>*</sup><small><em>Only the first " + auditRecordLimit + " matching audit records will be displayed.</em></small>";
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
								<h2>Audit Reports</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<p>Review an audit report<sup>*</sup> by entering your criteria into one or more of the filters below.</p>
								<hr/>
								<form id="auditReport" action="./auditReport" method="post" name="auditReport">
									<div class="section">
										<table>
											<tr>
												<td>
												  <label class="labelBold">Data Package Manager Service Method:</label>
												</td>
											</tr>
											<tr>
												<td class="spacersm"></td>
											</tr>
													<tr>
														<td valign="top">
                              <select class="select-width-auto" name="serviceMethod">
                                <%= serviceMethodsHTML %>
                              </select>
                            </td>
													</tr>
										</table>
										<table>
											<tr>
												<td>
												  <label class="labelBold">Category Status:</label>
												</td>
												<td>
												  <label class="labelBold">HTTP Code:</label>
												</td>
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
														        <input name="debug" type="checkbox" value="debug" />
														        <p>Debug</p>
														        <a class="checkbox-select" href="#">Select</a>
														        <a class="checkbox-deselect" href="#">Cancel</a>
														      </li>
														      <li>
														        <input name="info" type="checkbox" value="info" />
														        <p>Info</p>
														        <a class="checkbox-select" href="#">Select</a>
														        <a class="checkbox-deselect" href="#">Cancel</a>
														      </li>
														      <li>
														        <input name="warn" type="checkbox" value="warn" />
														        <p>Warn</p>
														        <a class="checkbox-select" href="#">Select</a>
														        <a class="checkbox-deselect" href="#">Cancel</a>
														      </li>
														      <li>
														        <input name="error" type="checkbox" value="error" />
														        <p>Error</p>
														        <a class="checkbox-select" href="#">Select</a>
														        <a class="checkbox-deselect" href="#">Cancel</a>
														      </li>
													      </ul>
													    </label>
												  </form>
												</td>
												<td>
                              <select class="select-width-auto" name="code">
<option value="all">All HTTP Codes</option>
<option value="100">100 Continue</option>
<option value="101">101 Switching Protocols</option>
<option value="102">102 Processing</option>
<option value="200">200 OK</option>
<option value="201">201 Created</option>
<option value="202">202 Accepted</option>
<option value="203">203 Non-Authoritative Information</option>
<option value="204">204 No Content</option>
<option value="205">205 Reset Content</option>
<option value="206">206 Partial Content</option>
<option value="207">207 Multi-Status</option>
<option value="208">208 Already Reported</option>
<option value="226">226 IM Used</option>
<option value="300">300 Multiple Choices</option>
<option value="301">301 Moved Permanently</option>
<option value="302">302 Found</option>
<option value="303">303 See Other</option>
<option value="304">304 Not Modified</option>
<option value="305">305 Use Proxy</option>
<option value="306">306 Switch Proxy</option>
<option value="307">307 Temporary Redirect</option>
<option value="308">308 Permanent Redirect</option>
<option value="400">400 Bad Request</option>
<option value="401">401 Unauthorized</option>
<option value="402">402 Payment Required</option>
<option value="403">403 Forbidden</option>
<option value="404">404 Not Found</option>
<option value="405">405 Method Not Allowed</option>
<option value="406">406 Not Acceptable</option>
<option value="407">407 Proxy Authentication Required</option>
<option value="408">408 Request Timeout</option>
<option value="409">409 Conflict</option>
<option value="410">410 Gone</option>
<option value="411">411 Length Required</option>
<option value="412">412 Precondition Failed</option>
<option value="413">413 Request Entity Too Large</option>
<option value="414">414 Request-URI Too Long</option>
<option value="415">415 Unsupported Media Type</option>
<option value="416">416 Requested Range Not Satisfiable</option>
<option value="417">417 Expectation Failed</option>
<option value="418">418 I'm a teapot</option>
<option value="419">419 Authentication Timeout</option>
<option value="420">420 Method Failure</option>
<option value="420">420 Enhance Your Calm</option>
<option value="422">422 Unprocessable Entity</option>
<option value="423">423 Locked</option>
<option value="424">424 Failed Dependency</option>
<option value="424">424 Method Failure</option>
<option value="425">425 Unordered Collection</option>
<option value="426">426 Upgrade Required</option>
<option value="428">428 Precondition Required</option>
<option value="429">429 Too Many Requests</option>
<option value="431">431 Request Header Fields Too Large</option>
<option value="440">440 Login Timeout</option>
<option value="444">444 No Response</option>
<option value="449">449 Retry With</option>
<option value="450">450 Blocked by Windows Parental Controls</option>
<option value="451">451 Unavailable For Legal Reasons</option>
<option value="451">451 Redirect</option>
<option value="494">494 Request Header Too Large</option>
<option value="495">495 Cert Error</option>
<option value="496">496 No Cert</option>
<option value="497">497 HTTP to HTTPS</option>
<option value="499">499 Client Closed Request</option>
<option value="500">500 Internal Server Error</option>
<option value="501">501 Not Implemented</option>
<option value="502">502 Bad Gateway</option>
<option value="503">503 Service Unavailable</option>
<option value="504">504 Gateway Timeout</option>
<option value="505">505 HTTP Version Not Supported</option>
<option value="506">506 Variant Also Negotiates</option>
<option value="507">507 Insufficient Storage</option>
<option value="508">508 Loop Detected</option>
<option value="509">509 Bandwidth Limit Exceeded</option>
<option value="510">510 Not Extended</option>
<option value="511">511 Network Authentication Required</option>
<option value="520">520 Origin Error</option>
<option value="522">522 Connection timed out</option>
<option value="523">523 Proxy Declined Request</option>
<option value="524">524 A timeout occurred</option>
<option value="598">598 Network read timeout error</option>
<option value="599">599 Network connect timeout error</option>
</select>
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td>
												  <label class="labelBold">User Name:</label>
												</td>
												<td>
												  <label class="labelBold">Group:</label>
												</td>
											</tr>
											<tr>
												<td>
												  <input name="userId" size="15" type="text" />
												</td>
												<td>
												  <input name="group" size="15" type="text" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td>
												  <label class="labelBold">Begin Date:</label>
												  <input name="beginDate" placeholder="YYYY-MM-DD" type="date" />
												</td>
												<td>
												  <label class="labelBold">Begin Time:<sup>**</sup></label>
												  <input name="beginTime" placeholder="HH:MM:SS" type="time" />
												</td>
												<td>
												  <label class="labelBold">End Date:</label>
												  <input name="endDate" placeholder="YYYY-MM-DD" type="date" />
												</td>
												<td>
												  <label class="labelBold">End Time:<sup>**</sup></label>
												  <input name="endTime" placeholder="HH:MM:SS" type="time" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td></td>
												<td>
												  <input class="btn btn-info btn-default" name="submit" type="submit" value="Submit" />
												  <input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
							 <tr>
												<td class="spacersm"></td>
							</tr>
								<tr>
									<td colspan="3"><%= limitHTML %></td>
								</tr>
								<tr>
								  <td colspan="3"><sup>**</sup><small><em>Time values are Mountain TZ (default 00:00:00).</em></small></td>
								</tr>
										</table>
									</div>
									<!-- section -->
					<%
						if (reportMessage != null) {
							out.println(String.format("<p class=\"nis-warn\">%s</p>", reportMessage));
						}
					%>

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
