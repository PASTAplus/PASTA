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

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Audit Report</title>

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

			<h2 align="center">Data Package Audit Report Viewer</h2>


			<fieldset>
				<legend>Data Package Audit Reports</legend>

				<p>Review a PASTA audit report by entering the Package Id value
				   (e.g., <em>knb-lter-xyz.1.1</em>) and, optionally, a user
				   name (e.g., <em>jdoe</em>) and date range, then select
				   "submit". Dates are formatted as <em>YYYY-MM-DD</em>.</p>

				<form id="dataPackageAudit" name="dataPackageAudit" method="post"
					action="./dataPackageAudit">
			       <div class="section">
					<table>
						<tbody>
							<tr>
								<td align="left">
								    <label for="packageId">Package Id:</label>
								</td>
								<td>
									<input type="text" name="packageId" size="50px"
									required="required" placeholder="knb-lter-xyz.1.1" autofocus />
								</td>
							</tr>
							<tr>
								<td align="left">
								    <label for="userId">User Name:</label>
								</td>
								<td>
								    <input type="text" name="userId" size="50px" />
								</td>
							</tr>
							<tr>
								<td align="left">Date Range:</td>
								<td>
								    <label for="begin">Begin </label>
								    <input type="text" name="begin" size="15px" placeholder="YYYY-MM-DD" />
						            <label for="end">End </label>
								    <input type="text" name="end" size="15px" placeholder="YYYY-MM-DD" />
								</td>
							</tr>
							<tr>
								<td></td>
								<td>
								    <input type="submit" name="submit" value="submit" />
								    <input type="reset" name="reset" value="reset" />
								</td>
							</tr>
						</tbody>
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

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
