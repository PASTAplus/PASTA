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

<!DOCTYPE html>
<html>

<head>
<title>LTER :: Network Data Portal</title>

<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<!-- Google Fonts CSS -->
<link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

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
								<h2>Provenance Metadata Viewer</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<fieldset>
								<p>View provenance metadata of a data package using 
								the package identifier</p>
								<div class="section">
									<form id="provenanceviewer" action="provenanceViewer" method="post" name="provenanceviewer">
										<table>
											<tr>
												<td><label class="labelBold" for="packageid">Package 
												Id:</label> </td>
											</tr>
											<tr>
												<td>
												<input name="packageid" required="required" size="30" type="text" /></td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="view" type="submit" value="view" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="reset" />
												</td>
											</tr>
										</table>
									</form>
								</div>
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


								<!-- /Content --></div>
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
