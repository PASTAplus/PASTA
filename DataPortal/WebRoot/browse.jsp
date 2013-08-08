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
  
  String attributeName = "browseKeywordHTML";
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

    <link rel="stylesheet"        href="./js/jqwidgets/styles/jqx.base.css" type="text/css" />
    <link rel="stylesheet" type="text/css" href="./css/lter-nis.css">

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

<body class='default'>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Browse Data Packages</h2>

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
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
