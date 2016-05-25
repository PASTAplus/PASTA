<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>

<%
  final String pageTitle = "About";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
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
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

</head>

<body>

<%@ include file="header.jsp" %>

<div class="row-fluid ">
	<div>
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h2>The US Long-Term Ecological Research (LTER) Network Information System (<abbr title="Network Information System">NIS</abbr>) Data Portal</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="content span12 box_layout">
								<p>The <abbr title="Network Information System">NIS</abbr> Data Portal is the public facing information 
								management and technology interface to the Provenance Aware Synthesis Tracking Architecture (<abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr>). The 
								<abbr title="Network Information System">NIS</abbr> Data Portal is the main path for uploading and 
								discovery of varied LTER data products that are 
								in <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr>.</p>
								<!-- <p>Visit the
								<a href="https://nis.lternet.edu:8443/x/agBP" class="searchsubcat" target="_blank">
								<abbr title="Network Information System">NIS</abbr> Community Website</a> for information about 
								upcoming updates to the LTER Network Portal.</p> -->
								
								<!-- For Scientists, Researchers, Students, and the General Public -->
								<!-- <div class="spacer"></div> -->
								<div id="accordion1" class="accordion">
									<div class="accordion-group">
										<div class="accordion-heading ">
											<a class="accordion-toggle" data-parent="#accordion1" data-toggle="collapse" href="#Accordion_1">
											For Scientists, Researchers, Students, and the General Public</a> </div>
										<div id="Accordion_1" class="accordion-body  collapse">
											<div class="accordion-inner ">
												<p>The LTER is able to support high-level 
												analysis and synthesis of complex 
												ecosystem data across the science-policy-management 
												continuum, which in turn helps advance 
												ecosystem research. By providing 
												the means to share data sets and 
												develop collaborations as part of 
												our data sharing processes, the 
												LTER seeks to improve:</p>
												<ol>
													<li>the availability and quality 
													of data from the varied LTER 
													sites,</li>
													<li>the timeliness and quantity 
													of LTER derived data products, 
													and</li>
													<li>the knowledge gained from 
													the synthesis of LTER data.</li>
												</ol>
												<p>The <abbr title="Network Information System">NIS</abbr> Data Portal uses a <i>rolling-update</i>
												approach to continuously release 
												improved versions as they are ready 
												for the community.</p>
												<!--   <p>Visit the
												<a href="https://nis.lternet.edu:8443/x/BIBH" class="searchsubcat" target="_blank">
												<abbr title="Network Information System">NIS</abbr> User&#39;s Guide</a> for detailed 
												information on how to best utilize 
												the LTER Network Data Portal.</p>
												
												<p>Any questions not answered by 
												the <abbr title="Network Information System">NIS</abbr> User&#39;s Guide may be addressed 
												in either the comments section (at 
												the bottom of every page in the 
												User Guide) or by emailing
												<a href="mailto:tech-support@lternet.edu" class="searchsubcat">
												tech-support@lternet.edu</a>.</p>-->
											</div>
										</div>
									</div>
								</div>
								<!-- /For Scientists, Researchers, Students, and the General Public -->
												
								<!-- For LTER Site Information Managers,... -->
								<!-- <div class="spacer"></div> -->
								<div id="accordion2" class="accordion">
									<div class="accordion-group">
										<div class="accordion-heading ">
											<a class="accordion-toggle" data-parent="#accordion2" data-toggle="collapse" href="#Accordion_2">
											For LTER Site Information Managers, Software Developers, and Other Interested Parties</a> </div>
										<div id="Accordion_2" class="accordion-body  collapse">
											<div class="accordion-inner ">
												<p>The <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> framework is comprised of the Gatekeeper identity authentication 
												service<sup>*</sup> and the following application programming interfaces (APIs) of 
												the LTER Network Information System:
												<ol>
													<li>The Audit Service API</li>
													<li>The Event Manager API</li>
													<li>The Data Package Manager 
													API, includes:
													<ul>
														<li>Data Manager</li>
														<li>Metadata Manager</li>
														<li>Provenance Factory</li>
													</ul>
													</li>
												</ol>
												<p>The Gatekeeper is a reverse proxy 
												service that performs user identity 
												verification and service forwarding; 
												it does not perform any direct <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> 
												function and does not have a web-service 
												API.</p>
												<p>The Audit Manager collects information 
												about operations that are executed 
												within the <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> environment and 
												provides an API for searching and 
												viewing recorded events.</p>
												<p>The Event Manager is an extended 
												feature of <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> and allows users 
												to subscribe their own workflows 
												to <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> data package upload (insert 
												and or update) events.</p>
												<p>The Data Package Manager is designed 
												for users to configure and schedule 
												data package uploads into <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> 
												and to search for data packages 
												that reside in <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr>.</p>
												<p>Like the <abbr title="Network Information System">NIS</abbr> Data Portal, all 
												of <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr>&#39;s services use a <i>rolling-update</i>
												approach to adding bug fixes, improvements, 
												and new features to each of the 
												services.</p>
												<!-- 
												<p>Information about the structure 
												and functions of the APIs and the 
												overall source-code documentation 
												for <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> is available at the
												<a href="https://nis.lternet.edu:8443/x/BAAF" class="searchsubcat" target="_blank">
												<abbr title="Network Information System">NIS</abbr> Software Developer&#39;s Guide</a>.
												</p>
												<p>Any questions not answered by 
												the <abbr title="Network Information System">NIS</abbr> Software Developer&#39;s Guide 
												may be addressed in either the comments 
												section (at the bottom of every 
												page in the Software Developer&#39;s 
												Guide) or by emailing
												<a href="mailto:tech-support@lternet.edu" class="searchsubcat">
												tech-support@lternet.edu</a>.</p>
												 -->
									<p><sup>*</sup>User authentication is <em>required</em> for all data input to the <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> system.</p>
											</div>
										</div>
									</div>
								</div>
								<!-- /For LTER Site Information Managers,... -->
												
												
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
	<!-- Banner -->
	<div>
		<div class="container">
			<div class="row-fluid distance_2">
				<div class="row-fluid text_bar_pattern">
					<span class="right_arrow"></span>
					<div id="div-arra-img">
						<h1 class="banner_font">Thank You!</h1>
						<p></p>
						<p>Partial funding for the development 
						of <abbr title="Provenance Aware Synthesis Tracking Architecture">PASTA</abbr> and the LTER <abbr title="Network Information System">NIS</abbr> is provided under the American 
						Recovery and Reinvestment Act of 2009 and is administered 
						by the National Science Foundation.</p>
						<p id="p-arra-img">
						  <img id="arra-img" alt="US ARRA logo" src="images/ARRA-Small.png" title="US ARRA logo">
						</p>
					</div>
					<span class="bottom_shadow_full"></span></div>
			</div>
		</div>
	</div>
	<!-- /Banner -->

	  <jsp:include page="footer.jsp" />

</body>

</html>
