<!DOCTYPE html>
<html>

<head>
<title>LTER :: Network Data Portal</title>

<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport">

<!-- Google Fonts CSS -->
<link href="http://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css">

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

<!-- For Custom Checkboxes -->
<script src="charts/assets/jquery.min.js" type="text/javascript"></script>
<script type="text/javascript">

	$(document).ready(function() {
		$(".checklist .checkbox-select").click(
			function(event) {
				event.preventDefault();
				$(this).parent().addClass("selected");
				$(this).parent().find(":checkbox").attr("checked","checked");
				
			}
		);
		
		$(".checklist .checkbox-deselect").click(
			function(event) {
				event.preventDefault();
				$(this).parent().removeClass("selected");
				$(this).parent().find(":checkbox").removeAttr("checked");
				
			}
		);
		
	});

</script>
<!-- /For Custom Checkboxes -->

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
								<h2>Audit Report</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content --><fieldset>
								<p>Review a PASTA audit report by entering information
								 into one or more of the filters below, or see all entries
								  by leaving the defaults, then select "submit":</p>
								<form id="dataPackageAudit" action="./dataPackageAudit" method="post" name="dataPackageAudit">
									<div class="section">
										<table>
											<tr>
												<td><label class="labelBold">Begin Date-Time:</label></td>
											</tr>
											<tr>
												<td><label for="userId">Date</label>
												<input name="begin" placeholder="YYYY-MM-DD" size="15px" type="date" />
												<label style="margin-top:-16px;">&nbsp;</label>
												</td>
												<td><label for="group">Time</label>
												<input name="end" placeholder="HH:MM:SS" size="15px" type="time" />
												<label style="margin-top:-16px;"> Values are Mountain TZ (default 00:00:00)</label>
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">End Date-Time:</label></td>
											</tr>
											<tr>
												<td><label for="userId">Date</label>
												<input name="begin" placeholder="YYYY-MM-DD" size="15px" type="date" />
												<label style="margin-top:-16px;">&nbsp;</label>
												</td>
												<td><label for="group">Time</label>
												<input name="end" placeholder="HH:MM:SS" size="15px" type="time" />
												<label style="margin-top:-16px;"> Values are Mountain TZ (default 00:00:00)</label>
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">Category Status:</label></td>
											</tr>
											<tr>
												<td class="spacersm"></td>
											</tr>
											<tr>
												<td>
												<form>
													<fieldset>
													<label for="choices">
													<ul class="checklist">
														<li>
														<input name="jqdemo" type="checkbox" value="value1" />
														<p>Debug</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
														<li>
														<input name="jqdemo" type="checkbox" value="value2" />
														<p>Info</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
														<li>
														<input name="jqdemo" type="checkbox" value="value3" />
														<p>Warn</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
														<li>
														<input name="jqdemo" type="checkbox" value="value4" />
														<p>Error</p>
														<a class="checkbox-select" href="#">
														Select</a>
														<a class="checkbox-deselect" href="#">
														Cancel</a> </li>
													</ul>
													</label></fieldset>
												</form>
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label style="margin-top:20px" class="labelBold">HTTP Code:</label></td>
											</tr>
											<tr>
												<td>
												<input name="begin" placeholder=" " size="15px" type="text" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">User Name:</label></td>
											</tr>
											<tr>
												<td>
												<input name="begin" placeholder=" " size="15px" type="text" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td><label class="labelBold">Group:</label></td>
											</tr>
											<tr>
												<td>
												<input name="begin" placeholder=" " size="15px" type="text" />
												</td>
											</tr>
										</table>
										<table>
											<tr>
												<td></td>
												<td>
												<input style="margin-top:10px" class="btn btn-info btn-default" name="submit" type="submit" value="submit" />
												<input style="margin-top:10px" class="btn btn-info btn-default" name="reset" type="reset" value="reset" />
												</td>
											</tr>
										</table>
									</div>
									<!-- section -->
								</form>
								</fieldset>
								<!-- /Content -->
							</div>
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
