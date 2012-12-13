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
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Contact</title>

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

			<h3 align="center">Contact Us</h3>

			<fieldset>
				<legend>People</legend>

				<ul>
					<li><a
						href="http://search.lternet.edu/directory_view.php?personid=10391">James
							Brunt</a>, LTER Chief Information Officer</li>
					<li><a
						href="http://search.lternet.edu/directory_view.php?personid=13823">Mark
							Servilla</a>, NIS Lead Scientist</li>
					<li><a
						href="http://search.lternet.edu/directory_view.php?personid=13757">Duane
							Costa</a>, NIS Analyst/Programmer III</li>
				</ul>

			</fieldset>

			<fieldset>
				<legend>Websites</legend>

				<ul>
					<li><a href="https://www.lternet.edu">LTER Network</a>
					</li>
					<li><a href="https://lno.lternet.edu">LTER Network Office</a>
					</li>
					<li><a href="https://nis.lternet.edu:8443/x/agBP">NIS
							Guides</a>
					</li>
				</ul>

			</fieldset>

			<fieldset>
				<legend>Physical Address</legend>

				<ul style="list-style: none;">
					<li>LTER Network Office<br /> Suite 320, CERIA Bldg #83,<br />
						University of New Mexico (Main Campus)<br /> Albuquerque, New
						Mexico, USA<br /> Phone: 505 277-2597<br /> Fax: 505 277-2541<br />
						Email: <a href="mailto:tech-support@lternet.edu">tech-support@lternet.edu</a><br />
						URL: <a href="https://lno.lternet.edu">https://lno.lternet.edu</a>
					</li>
				</ul>
			</fieldset>

			<fieldset>
				<legend>Mailing Address</legend>

				<ul style="list-style: none;">
					<li>LTER Network Office<br /> UNM Dept of Biology, MSC03 2020<br />
						1 University of New Mexico<br /> Albuquerque, New Mexico, USA<br />
						87131-0001</li>
				</ul>
			</fieldset>

		</div>
		<!-- end content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end wrapper -->

</body>
</html>
