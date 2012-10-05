<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.portal.HarvestReport"%>

<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String uid = (String) session.getAttribute("uid");
  
  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./harvestReport.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  
  String warningMessage = (String) request.getAttribute("message");
  if (warningMessage == null) {
    warningMessage = "";
  }

  HarvestReport harvestReport = new HarvestReport();
  String newestReportID = harvestReport.newestHarvestReport(uid);
  String harvestReportHTML = null;
  String harvestReportID = (String) session.getAttribute("harvestReportID");
  if (harvestReportID != null && harvestReportID.length() > 0) {
    harvestReportHTML = harvestReport.harvestReportHTML(harvestReportID);
  } else if (newestReportID != null && newestReportID.length() > 0) {
    harvestReportHTML = harvestReport.harvestReportHTML(newestReportID);
  }
  if (harvestReportHTML == null) {
    harvestReportHTML = "";
  }

  String newestReportLink = harvestReport.newestHarvestReportLink(uid);
  if (newestReportLink == null) {
    newestReportLink = "";
  }

  boolean removeNewestReport = true;
  String olderReports = harvestReport.composeHarvestReports(uid,
      removeNewestReport);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - View Reports</title>

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

			<h2 align="center">View Harvest Reports</h2>

			<%=warningMessage%>

			<table width="100%">
				<tbody>
					<tr>
						<td valign="top" width="30%"><b>Most recent report:</b>
							<ul>
								<%=newestReportLink%>
							</ul> <b>Older reports:</b> <%=olderReports%></td>
						<td valign="top" width="70%">
							<div class="section-table">
								<%=harvestReportHTML%>
							</div>
						</td>
					</tr>
				</tbody>
			</table>



		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
