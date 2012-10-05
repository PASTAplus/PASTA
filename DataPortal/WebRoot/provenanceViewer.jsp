<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
      
  HttpSession httpSession = request.getSession();
  
    String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./provenanceViewer.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  
  String message = (String) request.getAttribute("message");
  String type = (String) request.getAttribute("type");
  String packageid = (String) request.getAttribute("packageid");

  if (type == null) {
    type = "";
  }
          
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>Provenance Metadata Viewer</title>

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Provenance Metadata Viewer</h2>

			<fieldset>
				<legend>View Provenance Metadata</legend>

				<p>View provenance metadata of a data package using the package identifier</p>

				<div class="section">
					<form id="provenanceviewer" name="provenanceviewer" method="post"
						action="./provenanceViewer">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left" width="130px"><label for="packageid">PackageId:</label>
									</td>
									<td align="left" width="200px"><input type="text"
										name="packageid" required="required" size="30" /></td>
									<td align="center" width="70px"><input type="submit"
										name="view" value="view" />
									</td>
									<td align="center" width="40px"><input type="reset"
										name="reset" value="reset" />
									</td>
								</tr>
							</tbody>
						</table>
					</form>

                </div>

					<%
						if (message != null && type.equals("warning")) {
						    out.println("<div class=\"section\">\n");
							out.println("<table align=\"left\" cellpadding=\"4em\">\n");
							out.println("<tbody>\n");
							out.println("<tr>\n");
							out.println("<td class=\"" + type + "\">\n");
							out.println(message + "\n");
							out.println("</td>\n");
							out.println("</tr>\n");
							out.println("</tbody>\n");
							out.println("</table>\n");
							out.println("</div>\n");
						}
					%>

			</fieldset>

			<%
				if (message != null && !type.equals("warning")) {
				    out.println("<fieldset>\n");
				    out.println("<legend>" + packageid + "</legend>\n");
					out.println("<div class=\"section\">\n");
					out.println("<table align=\"left\" cellpadding=\"4em\">\n");
					out.println("<tbody>\n");
					out.println("<tr>\n");
					out.println("<td " + type + ">\n");
					out.println(message + "\n");
					out.println("</td>\n");
					out.println("</tr>\n");
					out.println("</tbody>\n");
					out.println("</table>\n");
					out.println("</div>\n");
					out.println("</fieldset>\n");
				}
			%>


		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
