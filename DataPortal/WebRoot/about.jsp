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

				<h3 align="center">
					Welcome to the US Long-Term Ecological Research (LTER) <br />
					Network Information System (NIS) Data Portal
				</h3>


				<h4>For Scientists, Researchers, Students, and the General
					Public</h4>

				<div class="section">

					<p>The LTER is able to support high-level analysis and
						synthesis of complex ecosystem data across the
						science-policy-management continuum, which in turn helps advance
						ecosystem research. By providing the means to share data sets and
						develop collaborations as part of our data sharing processes, the
						LTER seeks to improve:</p>

					<ol>
						<li>the availability and quality of data from the varied LTER
							sites,</li>
						<li>the timeliness and quantity of LTER derived data
							products, and</li>
						<li>the knowledge gained from the synthesis of LTER data.</li>
					</ol>

					<p>The NIS Data Portal is the cumulative effort of the NIS
						Development Team to provide a public facing information management and
						technology interface. The NIS Data Portal is the main path for
						input and retrieval of the varied data products from the NIS PASTA
						(Provenance Aware Synthesis Tracking Architecture).</p>

				</div>

				<h4>For LTER Site Information Managers, Programmers, and Other
					Interested Parties</h4>

				<div class="section">

					<p>
						The PASTA framework is comprised of the following application
						programming interfaces (APIs) of the LTER Network Information
						System (<b><i>user authentication</i> </b> is required for all
						data input to the PASTA system):
					</p>

					<ol>
						<li>The Provenance Factory API</li>
						<li>The Audit Service API</li>
						<li>The Event Manager API</li>
						<li>The Data Package Manager (including the Data Manager and
							the Data Catalog) API</li>
						<li>The Metadata Manager (including the Metadata Catalog) API</li>
					</ol>

					<p>Identity Management Services (Gatekeeper) and Persistent
						Identifier Services have been adopted from community and industry
						standards.</p>

					<p>The Audit Services API supports and complies with the LTER
						Data Policy to track LTER data access and usage.</p>

					<p>LTER Site Information Managers can choose to interact
						directly with the Data Package Manager component to configure and
						schedule metadata harvests into the Data Catalog and to identify
						"PASTA-compliant" data (i.e., data that are made available to
						PASTA and conform to the necessary metadata standards).</p>

					<p>
						Information about the structure and functions of the APIs and the
						overall source-code documentation for PASTA is available at the <a
							href="https://nis.lternet.edu:8443/display/pasta/Home"
							target="_blank">LTER NIS developer's documentation wiki.</a>
					</p>

				</div>

				<table>
					<tbody>
						<tr>
							<td>Partial funding for the development of PASTA and the
								LTER NIS is provided under the American Recovery and
								Reinvestment Act of 2009 and is administered by the National
								Science Foundation.</td>
							<td><img id="arra-img" src="./images/ARRA-Small.gif"
								alt="US ARRA logo" />
							</td>
						</tr>
					</tbody>
				</table>

			</div>
			<!-- end content -->

			<jsp:include page="/WEB-INF/jsp/foot.jsp" />

		</div>
		<!-- end wrapper -->

</body>
</html>
