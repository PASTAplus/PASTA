<!DOCTYPE html>
<html>

<head>
<title>LTER :: Network Data Portal</title>

<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

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
								<h2>Evaluate Data Package</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<p>Select an Ecological Metadata Language (EML) file to evaluate.</p>
								<div class="section">
                  <form id="uploadevaluate" name="uploadevaluate" method="post" action="./uploadevaluate" enctype="multipart/form-data" target="_blank">
										<table align="left" cellpadding="4em">
											<tr>
												<td align="left">
												<label for="packageid">File:
												</label>
												<input accept="application/xml" name="emlfile" required="required" size="60" type="file" />
												</td>
											</tr>
											<tr>
												<td align="left">
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
										<input id="metadataSource" name="metadataSource" type="hidden" value="emlFile" />
									</form>
								</div>
								<!-- /Content --></div>
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