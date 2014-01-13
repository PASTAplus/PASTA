<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="java.io.File" %>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms" %>
<%@ page import="edu.lternet.pasta.portal.search.BrowseSearch" %>
<%@ page import="edu.lternet.pasta.portal.search.BrowseGroup" %>

<%
  HttpSession httpSession = request.getSession();
  ServletContext servletContext = httpSession.getServletContext();
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
  
  String attributeName = "browseKeywordHTML";
  String browseHTML = (String) servletContext.getAttribute(attributeName);
%>

<!DOCTYPE html>
<html lang="en">

<head>
<title>LTER :: Network Data Portal</title>
<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon" />

<!-- Google Fonts CSS -->
<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

<link rel="stylesheet"        href="./js/jqwidgets/styles/jqx.base.css" type="text/css" />
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

    <script type="text/javascript" src="./js/jquery-1.8.3.min.js"></script>
    <script type="text/javascript" src="./js/jqwidgets/jqxcore.js"></script>
    <script type="text/javascript" src="./js/jqwidgets/jqxbuttons.js"></script>
    <script type="text/javascript" src="./js/jqwidgets/jqxscrollbar.js"></script>
    <script type="text/javascript" src="./js/jqwidgets/jqxpanel.js"></script>
    <script type="text/javascript" src="./js/jqwidgets/jqxtree.js"></script>
    <script type="text/javascript">
        $(document).ready(function () {
            // Create jqxTree
            $('#jqxTree').jqxTree({ height: '600px'});
            $('#jqxTree').bind('select', function (event) {
                var htmlElement = event.args.element;
                var item = $('#jqxTree').jqxTree('getItem', htmlElement);
                //alert(item.label);
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
								<h2>Browse Data Packages</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="content span12 box_layout">
			<fieldset>
				<p>Browse data packages by keyword or LTER site using the links below. The number of matching data packages is shown in parentheses.&#42;&nbsp;&#42;&#42;</p>

        <!-- <p><strong>Alternative:</strong> <a href="http://vocab.lternet.edu" target="new">Multi-level Browse</a></p> -->
        
				<div class="section">
          <div id='jqxTree'>
            <%= browseHTML %>
				  </div>
				</div>
				<p>
				   <small>&#42; <em>Only public documents are accessible from this page.</em></small><br/>
				   <small>&#42;&#42; <em>Search results are refreshed nightly.</em></small>
				</p>
			</fieldset>
			        </div>
							<!-- end of content --></div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<jsp:include page="footer.jsp" />

</div>

</body>

</html>
