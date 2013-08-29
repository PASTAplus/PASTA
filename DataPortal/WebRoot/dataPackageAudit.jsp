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

<title>NIS Data Portal - Access Report</title>

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

			<h2 align="center">Data Package Access Report Viewer</h2>


			<fieldset>
				<legend>Data Package Access Report</legend>

				<p>Review a PASTA Data Package access report by entering information into one or more
				of the filters below, then select "submit":</p>

				<form id="dataPackageAudit" name="dataPackageAudit" method="post"
					action="./dataPackageAudit">
			       <div class="section">
					<table>
						<tbody>
							<tr>
								<td align="right">PackageId:</td>
								<td>
                  <label for="scope">Scope </label>
									<input type="text" name="scope" size="30px" placeholder="knb-lter-xyz" autofocus />
								</td>
                <td>
                  <label for="identifier">Identifier </label>
                  <input type="number" name="identifier" size="5px" />
                </td>
                <td>
                  <label for="revision">Revision </label>
                  <input type="number" name="revision" size="5px" />
                </td>
							</tr>
							<tr>
							  <td align="right">Resource type:</td>
							  <td colspan="3">
							    <input type="checkbox" name="package" value="on" checked="checked" />
							    <label for="package">Package</label>&nbsp;&nbsp;&nbsp;
							    <input type="checkbox" name="metadata" value="on" />
							    <label for="metadata">Metadata</label>&nbsp;&nbsp;&nbsp;
							    <input type="checkbox" name="report" value="on" />
							    <label for="report]">Report</label>&nbsp;&nbsp;&nbsp;
							    <input type="checkbox" name="entity" value="on" />
							    <label for="entity">Data</label>
							  </td>
							</tr>
							<tr>
								<td align="right">Who:</td>
								<td colspan="3">
								  <label for="userId">User Name </label>
								  <input type="text" name="userId" size="30px" />
                  <label for="group">&nbspGroup </label>
                  <input type="text" name="group" size="30px" />
                </td>
							</tr>
							<tr>
								<td align="right">Date:</td>
								<td colspan="3">
								    <label for="begin">Begin </label>
								    <input type="date" name="begin" size="15px" placeholder="YYYY-MM-DD" />
						        <label for="end">&nbsp;End </label>
								    <input type="date" name="end" size="15px" placeholder="YYYY-MM-DD" />
								</td>
							</tr>
							<tr>
								<td></td>
								<td colspan="3">
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
