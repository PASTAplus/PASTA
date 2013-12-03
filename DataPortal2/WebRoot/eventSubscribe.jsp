<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();

  String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./eventSubscribe.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }

  String subscribeMessage = (String) request
      .getAttribute("subscribemessage");
  String deleteMessage = (String) request.getAttribute("deletemessage");
  String reviewMessage = (String) request.getAttribute("reviewmessage");
  String testMessage = (String) request.getAttribute("testmessage");
  String type = (String) request.getAttribute("type");

  if (type == null) {
    type = "";
  } else {
    type = "class=\"" + type + "\"";
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
								<h2>Event Subscriptions</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<fieldset>
								<legend>Subscribe</legend>
								<p>Subscribe to a NIS data package &quot;insert-&quot; or 
								&quot;update-event&quot; using the full package identifier 
								(scope-identifier-revision), the scope-identifier, 
								or just the scope.</p>
								<div class="section">
									<form id="eventsubscribe" action="eventsubscribe" method="post" name="eventsubscribe">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="packageid">Package Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="packageid" required="required" type="text" />
												</td>
												<td>
												<label style="padding-left:10px; padding-right:10px;" for="targeturl">Target URL :</label>
												</td>
												<td>
												<input name="targeturl" required="required" size="50" type="text" />
												</td>
											</tr>
											<tr>
												<td>
													<input class="btn btn-info btn-default" name="subscribe" type="submit" value="Subscribe" />
													<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
									</form>
								</div>
				<%
				  if (subscribeMessage != null) {
				    out.println("<div class=\"section\">\n");
				    out.println("<table align=\"left\" cellpadding=\"4em\">\n");
				    out.println("<tbody>\n");
				    out.println("<tr>\n");
				    out.println("<td " + type + ">\n");
				    out.println(subscribeMessage + "\n");
				    out.println("</td>\n");
				    out.println("</tr>\n");
				    out.println("</tbody>\n");
				    out.println("</table>\n");
				  }
				%>
								</fieldset>
								<fieldset>
								<legend>Review</legend>
								<p>Review a subscription using the subscription 
								identifier or leave empty to review &quot;all&quot; of your 
								subscriptions.</p>
								<div class="section">
									<form id="eventreview" action="eventreview" method="post" name="eventreview">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="subscriptionid">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="subscriptionid" type="text" />
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="review" type="submit" value="Review" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
									</form>
								</div>
        <%
          if (reviewMessage != null) {
            out.println("<div class=\"section-table\">\n");
            out.println("<table align=\"left\" cellpadding=\"4em\">\n");
            out.println("<tbody>\n");
            out.println("<tr>\n");
            out.println("<td " + type + ">\n");
            out.println(reviewMessage + "\n");
            out.println("</td>\n");
            out.println("</tr>\n");
            out.println("</tbody>\n");
            out.println("</table>\n");
          }
        %>
								</fieldset> <fieldset>
								<legend>Test</legend>
								<p>Test a subscription using the subscription identifier.</p>
								<div class="section">
									<form id="eventtest" action="eventtest" method="post" name="eventtest">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="subscriptionid">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="subscriptionid" required="required" type="text" />
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="test" type="submit" value="Test" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" /></td>
											</tr>
										</table>
									</form>
								</div>
        <%
          if (testMessage != null) {
            out.println("<div class=\"section-table\">\n");
            out.println("<table align=\"left\" cellpadding=\"4em\">\n");
            out.println("<tbody>\n");
            out.println("<tr>\n");
            out.println("<td " + type + ">\n");
            out.println(testMessage + "\n");
            out.println("</td>\n");
            out.println("</tr>\n");
            out.println("</tbody>\n");
            out.println("</table>\n");
          }
        %>
								</fieldset> <fieldset>
								<legend>Delete</legend>
								<p>Delete a subscription using the subscription 
								identifier.</p>
								<div class="section">
									<form id="eventdelete" action="eventdelete" method="post" name="eventdelete">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="subscriptionid">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="subscriptionid" required="required" type="text" />
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
								</div>
        <%
          if (deleteMessage != null) {
            out.println("<div class=\"section\">\n");
            out.println("<table align=\"left\" cellpadding=\"4em\">\n");
            out.println("<tbody>\n");
            out.println("<tr>\n");
            out.println("<td " + type + ">\n");
            out.println(deleteMessage + "\n");
            out.println("</td>\n");
            out.println("</tr>\n");
            out.println("</tbody>\n");
            out.println("</table>\n");
          }
        %>
								</fieldset>
								<!-- /Content --></div>
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
