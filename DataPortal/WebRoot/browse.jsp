<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
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
  String type = (String) request.getParameter("type");
  
  String attributeName = "browseKeywordHTML";
  String subcaption = "Category and Keyword";
  String introText = "category and keyword";
  if ((type != null) && (type.equals("ltersite"))) {
    attributeName = "browseLterSiteHTML";
    subcaption = "LTER Site";
    introText = "LTER site";
  }
  
  String browseHTML = (String) servletContext.getAttribute(attributeName);
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>Browse Data Packages</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">
<link rel="stylesheet" href="./css/jquery-ui-1.10.0.css" />

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

<script src="./js/jquery-ui-1.10.0.js"></script>
<script src="./js/toggle.js" type="text/javascript"></script>

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Browse Data Packages</h2>
			<h3 align="center"><%= subcaption %></h3>

			<fieldset>
				<p>Browse data packages by <%= introText %> using the links below. The number of matching data packages is shown in parentheses.&#42;&nbsp;&#42;&#42;</p>

        <!-- <p><strong>Alternative:</strong> <a href="http://vocab.lternet.edu" target="new">Multi-level Browse</a></p> -->
        
				<div class="section">
          <%= browseHTML %>
				</div>
				<p><small>&#42; <em>Only public documents are accessible from this page.</em></small><br/>
				   <small>&#42;&#42; <em>Search results are refreshed nightly.</em></small></p>
			</fieldset>

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

	<script type="text/javascript">
		jQuery(document).ready(function() {
			jQuery(".toggleButton").click(function() {
				jQuery(this).next(".collapsible").slideToggle("fast");
			});
			jQuery(".collapsible").hide();
		});
		jQuery("#showAll").click(function() {
			jQuery(".collapsible").show();
		});
		jQuery("#hideAll").click(function() {
			jQuery(".collapsible").hide();
		});
	</script>

</body>
</html>
