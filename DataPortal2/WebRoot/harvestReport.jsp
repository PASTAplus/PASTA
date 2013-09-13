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

<!-- These Scripts are for my Chart Demo and can be removed at any time -->
<script src="charts/assets/Chart.js" type="text/javascript"></script>
<script src="charts/assets/Chart_Demo.js" type="text/javascript"></script>
<script src="charts/assets/jquery.min.js" type="text/javascript"></script>
<!-- /These Scripts are for my Chart Demo and can be removed at any time -->

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
								<h2>View Upload Reports</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								
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

		<!-- Can be removed, loads charts demo -->
		<script src="charts/assets/effects.js"></script>
		<!-- /Can be removed, loads charts demo -->

</body>

</html>
