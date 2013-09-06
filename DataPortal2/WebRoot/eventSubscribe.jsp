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
								<h2>Event Subscription Management</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
								<fieldset>
								<legend>Subscribe</legend>
								<p>Subscribe to a NIS data package &quot;insert-&quot; or 
								&quot;update-event&quot; using the full package identifier 
								(scope-identifier-revision), the scope-identifier, 
								or just the scope.</p>
								<div class="section">
									<form id="eventsubscribe" action="eventsubscribe" method="post" name="eventsubscribe">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="packageid">Package Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="packageid" required="required" type="text" />
												</td>
												<td>
												<label style="padding-left:10px; padding-right:10px;" for="targeturl">Target URL :</label>
												</td>
												<td>
												<input name="targeturl" required="required" size="50" type="text" />
												</td>
											</tr>
											<tr>
												<td>
													<input class="btn btn-info btn-default" name="subscribe" type="submit" value="subscribe" />
													<input class="btn btn-info btn-default" name="reset" type="reset" value="reset" />
												</td>
											</tr>
										</table>
									</form>
								</div>
								</fieldset>
								<fieldset>
								<legend>Review</legend>
								<p>Review a subscription using the subscription 
								identifier or leave empty to review &quot;all&quot; of your 
								subscriptions.</p>
								<div class="section">
									<form id="eventreview" action="eventreview" method="post" name="eventreview">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="subscriptionid">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="subscriptionid" type="text" />
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="review" type="submit" value="review" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="reset" />
												</td>
											</tr>
										</table>
									</form>
								</div>
								</fieldset> <fieldset>
								<legend>Test</legend>
								<p>Test a subscription using the subscription identifier.</p>
								<div class="section">
									<form id="eventtest" action="eventtest" method="post" name="eventtest">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="subscriptionid">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="subscriptionid" required="required" type="text" />
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="test" type="submit" value="test" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="reset" /></td>
											</tr>
										</table>
									</form>
								</div>
								</fieldset> <fieldset>
								<legend>Delete</legend>
								<p>Delete a subscription using the subscription 
								identifier.</p>
								<div class="section">
									<form id="eventdelete" action="eventdelete" method="post" name="eventdelete">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="subscriptionid">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="subscriptionid" required="required" type="text" />
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="delete" type="submit" value="delete" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="reset" />
												</td>
											</tr>
										</table>
									</form>
								</div>
								</fieldset>
								<!-- /Content --></div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

		<jsp:include page="footer.jsp" />

</div>

		<script src="charts/assets/effects.js"></script>

</body>

</html>
