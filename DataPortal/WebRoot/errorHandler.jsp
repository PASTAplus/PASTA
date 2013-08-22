<!--

 $Date$
 $Author$
 $Revision$
 
 Copyright 2011,2012 the University of New Mexico.
 
 This work was supported by National Science Foundation Cooperative
 Agreements #DEB-0832652 and #DEB-0936498.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0.
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

-->

<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page isErrorPage="true"%>
<%
  HttpSession httpSession = request.getSession();
  httpSession.setAttribute("menuid", "about");

  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - About</title>

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
        <p class="warning">An error has occurred. Error message:</p>
        <p class="warning">
<%
    out.println(exception.toString());
%>
        </p>
			</div>
		  <!-- end content -->

			<jsp:include page="/WEB-INF/jsp/foot.jsp" />

		</div>
		<!-- end wrapper -->

</body>
</html>
