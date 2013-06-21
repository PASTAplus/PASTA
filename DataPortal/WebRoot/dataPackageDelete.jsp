<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();

  String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./dataPackageDelete.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }

  String deleteMessage = (String) request.getAttribute("deletemessage");
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

<title>NIS Data Portal - Delete Data Packages</title>

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

			<h2 align="center">Delete Data Packages</h2>

			<fieldset>
				<legend>Delete</legend>

				<p>Delete a data package using the package scope and identifier
					(e.g. <code>myscope.100</code>). 
				<p><em>Please note: Deletion of a data package is permanent and should be given due consideration. Once deleted, no additional data packages can be uploaded with the specified combination of scope and identifier.</em></p>

				<div class="section">
					<form id="datapackagedelete" name="datapackagedelete" method="post"
						action="./dataPackageDelete">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="packageid">PackageId:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="packageid" required="required" />
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
          if (deleteMessage != null && type.equals("class=\"warning\"")) {
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
