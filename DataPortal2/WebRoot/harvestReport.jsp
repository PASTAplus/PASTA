<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.portal.HarvestReport"%>
<%@ page import="edu.lternet.pasta.portal.HarvestReportServlet"%>

<%
  final String pageTitle = "View Evaluate/Upload Results";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
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
  } 
  else if (newestReportID != null && newestReportID.length() > 0) {
    harvestReportHTML = harvestReport.harvestReportHTML(newestReportID);
    harvestReportID = newestReportID;
  }
  
  if (harvestReportHTML == null) {
    harvestReportHTML = "";
  }

  String harvestReportList = harvestReport.composeHarvestReports(uid, harvestReportID);
  long daysToLive = HarvestReportServlet.harvesterReportDaysToLive;
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
								<h2>View Evaluate/Upload Results</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								
			<%=warningMessage%>

			<form id="harvestReport" action="./harvestReport" method="post" name="harvestReport" >
			  <label>Select the <b>Evaluate</b> or <b>Upload</b> results to view (results are stored for <%= daysToLive %> days):</label>
				<select class="select-width-auto" name="reportId" size="1" onchange="submitform()" >
				  <%= harvestReportList %>
			  </select>					
				<%=harvestReportHTML%>
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

    <script type="text/javascript">
        jQuery(document).ready(function() {
            jQuery(".dataset-content").hide();
            jQuery(".dataset-title").click(function()
            {
                jQuery(this).next(".dataset-content").slideToggle("fast");
            });
         });
         
         /* jQuery(".dataset-content").click(function () { jQuery.get("docInfoServlet", function(data) { alert(data); }); }); */
         /* jQuery(".fetch").click(function (docid) { alert(docid); }); */
         /* jQuery(".fetch").load("docInfoServlet"); */
         jQuery("#show").click(function () { jQuery(".dataset-content").show("fast"); });
         jQuery("#hide").click(function () { jQuery(".dataset-content").hide("fast"); });
                           
         function submitform() {
              document.getElementById('harvestReport').submit();
         }
          
    </script>
    
</body>

</html>
