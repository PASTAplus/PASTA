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

				<h3 align="center">
					Welcome to the US Long-Term Ecological Research (LTER) <br />
					Network Information System (NIS) Data Portal
				</h3>

				<p>The NIS Data Portal is the cumulative effort of the NIS
					Development Team to provide a public facing information management and
					technology interface. The NIS Data Portal is the main path for
					input and retrieval of the varied data products from the NIS PASTA
					(Provenance Aware Synthesis Tracking Architecture).</p>
					
			    <p>Visit the <a href="https://nis.lternet.edu:8443/x/agBP" target="_blank">
			        NIS Community Website</a> for information about
			        upcoming updates to the LTER Network Portal.</p>

                <p>What is a DOI and what is its purpose? Find the answer to
                    this question and others in the
                    <a href="https://nis.lternet.edu:8443/x/swBP" target="_blank">
                    Frequently Asked Questions</a>
                    section of the NIS Community Website.</p>
					
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

				    <p>The NIS Data Portal uses a "rolling-update" approach to
				       continuously release improved versions as they are ready for
				       the community.</p>
				       
				    <p>Visit the <a href="https://nis.lternet.edu:8443/x/BIBH" target="_blank">
				        NIS User's Guide</a> for detailed information on
				        how to best utilize the LTER Network Data Portal.</p>
				        
				    <p>Any questions not answered by the
				       NIS User's Guide may be addressed in either the comments
				       section (at the bottom of every page in the User Guide) or
				       by emailing <a href="mailto:tech-support@lternet.edu">
				       tech-support@lternet.edu</a>.

				</div>

				<h4>For LTER Site Information Managers, Software Developers, and Other
					Interested Parties</h4>

				<div class="section">

					<p>
						The PASTA framework is comprised of the Gatekeeper identity
						authentication service and the following application
						programming interfaces (APIs) of the LTER Network Information
						System (<b><i>user authentication</i> </b> is required for all
						data input to the PASTA system):
					</p>

					<ol>
						<li>The Audit Service API</li>
						<li>The Event Manager API</li>
						<li>The Data Package Manager API, includes:
						  <ul>
						      <li>Data Manager</li>
						      <li>Metadata Manager</li>
						      <li>Provenance Factory</li>
						  </ul>
						</li>
					</ol>
					
					<p>The Gatekeeper is a reverse proxy service that performs user
					   identity verification and service forwarding; it does not
					   perform any direct PASTA function and does not have a web-service
					   API.</p>
					
					<p>The Audit Manager collects information about operations that
					   are executed within the PASTA environment and provides an
					   API for searching and viewing recorded events.</p>

					<p>The Event Manager is an extended feature of PASTA and allows
						users to subscribe their own workflows to PASTA data package upload
						(insert and or update) events.</p>
					
					<p>The Data Package Manager is designed for users to configure
						and schedule data package uploads into PASTA and to
						search for data packages that reside in PASTA.</p>
						
				    <p>Like the NIS Data Portal, all of PASTA's services use a
				       "rolling-update" approach to adding bug fixes, improvements,
				       and new features to each of the services.</p>

					<p>
						Information about the structure and functions of the APIs and the
						overall source-code documentation for PASTA is available at the
						<a href="https://nis.lternet.edu:8443/x/BAAF"
					    target="_blank">NIS Software Developer's Guide</a>.
					</p>
					
					<p>Any questions not answered by the NIS Software Developer's Guide
					   may be addressed in either the comments
				       section (at the bottom of every page in the Software Developer's Guide) or
				       by emailing <a href="mailto:tech-support@lternet.edu">
				       tech-support@lternet.edu</a>.

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
