<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="java.io.File" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms" %>
<%@ page import="edu.lternet.pasta.portal.search.BrowseSearch" %>
<%@ page import="edu.lternet.pasta.portal.search.BrowseGroup" %>

<%
  final String pageTitle = "Browse Data Packages";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String attributeName = "browseKeywordHTML";
  String browseHTML = (String) application.getAttribute(attributeName);
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

<!-- jqWidgets CSS for jqxTree widget -->
<link rel="stylesheet" href="./js/jqwidgets/styles/jqx.base.css"  type="text/css" />
<link rel="stylesheet" href="./js/jqwidgets/styles/jqx.energyblue.css" type="text/css" />

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

<!-- jqWidgets JavaScript for jqxTree widget -->
    <script type="text/javascript" src="./js/jquery-1.11.0.min.js"></script>
    <script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxcore.js"></script>
    <script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxbuttons.js"></script>
    <script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxscrollbar.js"></script>
    <script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxpanel.js"></script>
    <script type="text/javascript" src="./js/jqwidgets-ver3.2.1/jqxtree.js"></script>

    <script type="text/javascript">
        $(document).ready(function () {
            // Create jqxTree
            $('#jqxTree').jqxTree(
            { height: '600px', 
              theme: 'energyblue',
              toggleMode: 'click'
            });
            
            $('#jqxTree').bind('select', function (event) {
                var htmlElement = event.args.element;
                var item = $('#jqxTree').jqxTree('getItem', htmlElement);
             });
        });
    </script>

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
								<h2>Browse Data by Keyword or LTER Site</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="content span12 box_layout">
								<p>
									Browse data packages by keyword or LTER site using the links
									below. The number of matching data packages is shown in
									parentheses.<sup>*</sup> <sup>**</sup>
								</p>

								<div id='jqxTree'>
									<%=browseHTML%>
								</div>
								<p>
									<sup>*</sup>
									<small><em>Only public documents are accessible from this page.</em></small>
								</p>
								<p>
									<sup>**</sup>
									<small><em>Search results are refreshed nightly.</em></small>
								</p>
							</div>
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
