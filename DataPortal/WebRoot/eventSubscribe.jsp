<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
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

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Event Subscription</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Event Subscription Management</h2>

			<fieldset>
				<legend>Subscribe</legend>

				<p>Subscribe to a NIS data package "insert-" or "update-event"
					using the full package identifier (scope-identifier-revision), the
					scope-identifier, or just the scope</p>

				<div class="section">
					<form id="eventsubscribe" name="eventsubscribe" method="post"
						action="./eventsubscribe">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="packageid">PackageId:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="packageid" required="required" />
									</td>
									<td align="left" width="100px"><label for="targeturl">Target
											URL:</label>
									</td>
									<td align="left" width="375px"><input type="text"
										name="targeturl" required="required" size="50" />
									</td>
									<td align="center" width="70px"><input type="submit"
										name="subscribe" value="subscribe" /></td>
									<td align="center" width="40px"><input type="reset"
										name="reset" value="reset" /></td>
								</tr>
							</tbody>
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

				<p>Review a subscription using the subscription identifier or
					leave empty to review "all" of your subscriptions</p>

				<div class="section">
					<form id="eventreview" name="eventreview" method="post"
						action="./eventreview">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="subscriptionid">SubscriptionId:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="subscriptionid" />
									</td>
									<td align="center" width="70px"><input type="submit"
										name="review" value="review" /></td>
									<td align="center" width="40px"><input type="reset"
										name="reset" value="reset" /></td>
								</tr>
							</tbody>
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
			</fieldset>

			<fieldset>
				<legend>Test</legend>

				<p>Test a subscription using the subscription identifier</p>

				<div class="section">
					<form id="eventtest" name="eventtest" method="post"
						action="./eventtest">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="subscriptionid">SubscriptionId:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="subscriptionid" required="required" />
									</td>
									<td align="center" width="70px"><input type="submit"
										name="test" value="test" /></td>
									<td align="center" width="40px"><input type="reset"
										name="reset" value="reset" /></td>
								</tr>
							</tbody>
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
			</fieldset>

			<fieldset>
				<legend>Delete</legend>

				<p>Delete a subscription using the subscription identifier</p>

				<div class="section">
					<form id="eventdelete" name="eventdelete" method="post"
						action="./eventdelete">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="subscriptionid">SubscriptionId:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="subscriptionid" required="required" />
									</td>
									<td align="center" width="70px"><input type="submit"
										name="delete" value="delete" /></td>
									<td align="center" width="40px"><input type="reset"
										name="reset" value="reset" /></td>
								</tr>
							</tbody>
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

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
