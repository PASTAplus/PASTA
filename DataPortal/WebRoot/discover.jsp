<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
  HttpSession httpSession = request.getSession();
  httpSession.setAttribute("menuid", "discover");

  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Discover</title>

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

			<h4>Discover Data Packages</h4>
			<ul>
				<li>Browse Data Packages by
				    <ul>
                <li><a href="./browse.jsp">Keyword or LTER Site</a></li>
				        <li><a href="./scopebrowse">Package Identifier</a></li>
				    </ul>
				</li>
				<li>Search for Data Packages using
				    <ul>
				        <li><a href="./simpleSearch.jsp">Basic Search</a></li>
				        <li><a href="./advancedSearch.jsp">Advanced Search</a></li>	        
				    </ul>
				</li>
			</ul>

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
