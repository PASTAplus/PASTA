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
<%
  HttpSession httpSession = request.getSession();
  httpSession.setAttribute("menuid", "help");
  
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Help</title>

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

			<h3 align="center">Need Help, Have Questions?</h3>

			<h4 align="left">How do I ...</h4>

			<ul>
				<li>Learn about the PASTA Software Developer's Application
					Programming Interface (API) - <a
					href="https://nis.lternet.edu:8443/x/BAAF" target="_blank">NIS
						Software Developer's Guide</a>
				</li>
				<li>Search for LTER data - <a
					href="https://nis.lternet.edu:8443/x/IoBH" target="_blank">NIS
						Users' Guide</a>
				</li>
				<li>Find out more about the LTER Network Information System
					(NIS) and its Mission - <a
					href="https://nis.lternet.edu:8443/x/agBP" target="_blank">NIS
						Community Website</a>
				</li>
				<li>Use the NIS Data Portal to create synthetic data - <a
					href="https://nis.lternet.edu:8443/x/NQFZ" target="_blank">Synthesis</a>
				</li>
				<li>Find out who has been downloading my data - <a
					href="https://nis.lternet.edu:8443/x/OwFZ" target="_blank">Reports</a>
				</li>
			</ul>

			<p>
				Have more questions, go to the <a
					href="https://nis.lternet.edu:8443/x/swFZ" target="_blank">Frequently
					Asked Questions</a> page, enter your question as a comment in either
				the NIS User's Guide or the NIS Software Developer's Guide, or you
				can always email <a href="mailto:tech-support@lternet.edu"
					target="_blank">tech-support@lternet.edu</a>.
			</p>

		</div>
		<!-- end content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end wrapper -->

</body>
</html>
