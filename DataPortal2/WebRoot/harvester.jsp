<!DOCTYPE html>
<html lang="en">

<head>
<title>LTER :: Network Data Portal</title>

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
<script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

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
								<h2>Evaluate/Upload Data Packages</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<p>Data packages may be evaluated without uploading 
								them to the NIS by selecting <b>Evaluate</b>. Once you 
								are satisfied that data packages are ready to be 
								uploaded to the NIS, you may do so by selecting 
								<b>Upload</b>. Several alternatives for supplying the 
								EML metadata for your data packages are available below.</p>
								<fieldset>
								<legend>EML Metadata File</legend>
								<p>Select an Ecological Metadata Language (EML) file to evaluate or upload.</p>
								<div class="section">
                  <form id="emlFile" name="emlFile" method="post" enctype="multipart/form-data" action="./harvester">
										<table align="left" cellpadding="4em">
											<tr>
												<td align="left">
												<label class="labelBold">File:</label>
												<input accept="application/xml" name="emlfile" required="required" size="60" type="file" />
												</td>
											</tr>
											<tr>
												<td align="left">
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
										<input id="metadataSource" name="metadataSource" type="hidden" value="emlFile" />
									</form>
								</div>
								</fieldset>
								<fieldset>
								<legend>EML Document URLs</legend>
								<p>Enter a list of EML document URLs into the text 
								area below, one per line, and then select <b>Evaluate</b> or <b>Upload</b>.</p>
								<div class="section">
									<form id="urlList" action="./harvester" method="post" name="urlList">
										<table align="left" cellpadding="4em">
											<tr>
												<td align="left">
												<textarea id="urlTextArea" cols="80" name="urlTextArea" required="required" rows="8"></textarea>
												</td>
											</tr>
											<tr>
												<td align="left">
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
										<input id="metadataSource" name="metadataSource" type="hidden" value="urlList" />
									</form>
								</div>
								</fieldset>
								<!-- More Options for Upload -->
								<!-- <div class="spacer"></div> -->
								<div id="accordion1" class="accordion">
									<div class="accordion-group">
										<div class="accordion-heading ">
											<a class="accordion-toggle" data-parent="#accordion1" data-toggle="collapse" href="#Accordion_1">
											More Options for Upload</a> </div>
										<div id="Accordion_1" class="accordion-body  collapse">
											<div class="accordion-inner ">
												<!-- Content -->
												<fieldset>
												<legend>Copy EML Metadata Text</legend>
												<p>Copy the XML for a single EML metadata document into the text 
			                     area below and then select <b>Evaluate</b> or <b>Upload</b>.</p>
												<div class="section">
													<form id="emlText" action="./harvester" method="post" name="emlText">
														<table align="left" cellpadding="4em">
															<tr>
																<td align="left">
																<textarea id="emlTextArea" cols="100" name="emlTextArea" required="required" rows="8"></textarea></td>
															</tr>
															<tr>
																<td align="left">
																<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
																<input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
																<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" /></td>
															</tr>
														</table>
														<input id="metadataSource" name="metadataSource" type="hidden" value="emlText" />
													</form>
												</div>
												</fieldset>
												<fieldset>
												<legend>Metacat Harvest List URL</legend>
												<p>Enter the URL of a Metacat Harvest List and then select <b>Evaluate</b> 
												or <b>Upload</b>. All <var>documentURL</var> elements in the harvest list will be processed.</p>
												<div class="section">
													<form id="harvestList" action="./harvester" method="post" name="harvestList">
														<table align="left" cellpadding="4em">
															<tr>
																<td align="left">
																<label class="labelBold">Metacat Harvest List URL:</label>
																<input name="harvestListURL" required="required" size="150" type="text" />
																</td>
															</tr>
															<tr>
																<td align="left">
																<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
																<input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
																<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
																</td>
															</tr>
														</table>
														<input id="metadataSource" name="metadataSource" type="hidden" value="harvestList" />
													</form>
												</div>
												</fieldset>
												<!-- /Content -->
											</div>
										</div>
									</div>
								</div>
								<!-- /More Options for Upload -->
							</div>
							<!-- /Content -->
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

		<jsp:include page="footer.jsp" />
		
</div>

</body>

</html>
