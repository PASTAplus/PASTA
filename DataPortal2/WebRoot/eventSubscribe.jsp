<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.client.EventSubscriptionClient"%>

<%
  final String pageTitle = "Event Subscriptions";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  HttpSession httpSession = request.getSession();

  String displayDivOpen = "<div>";
  String displayDivClose = "</div>";
  String subscriptionTableHTML = "";
  String subscriptionOptionsHTML = "";
  String subscribeMessage = (String) request.getAttribute("subscribemessage");
  String deleteMessage = (String) request.getAttribute("deletemessage");
  String testMessage = (String) request.getAttribute("testmessage");
  String type = (String) request.getAttribute("type");

  String uid = (String) httpSession.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./eventSubscribe.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }
  else {
  	EventSubscriptionClient esc = new EventSubscriptionClient(uid);
  	int numberOfSubscriptions = esc.numberOfSubscriptions();
  	
  	if (numberOfSubscriptions == 0) {
    	displayDivOpen = "<div class='display-none'>";
  	}
  	
  	subscriptionTableHTML = esc.subscriptionTableHTML();
  	subscriptionOptionsHTML = esc.subscriptionOptionsHTML();

  	if (type == null) {
    	type = "";
  	} 
  	else {
    	type = "class=\"" + type + "\"";
  	}
  
  	if (subscribeMessage  == null) { subscribeMessage = ""; }
  	if (testMessage  == null) { testMessage = ""; }
  	if (deleteMessage  == null) { deleteMessage = ""; }
  }
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
								<h1>Event Subscriptions</h1>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
                <h2>Subscribe</h2>
								<p>Subscribe to <abbr title="Network Information System">NIS</abbr> data package <b>insert</b> or <b>update</b> events by entering a package identifier that matches:</p>
								<ol>
								  <li>a particular data package revision (e.g. <kbd class="nis">mypackages.1.1</kbd>); or,</li>
								  <li>any revision of a data package with a given scope and identifier (e.g. <kbd class="nis">mypackages.1</kbd>); or,</li>
								  <li>any data package with a given scope (e.g. <kbd class="nis">mypackages</kbd>).</li>
                </ol>
								<p>Then enter the URL of a workflow or other procedure for the <abbr title="Network Information System">NIS</abbr> to invoke whenever the data packages you specified are inserted or updated.</p>
								<div class="section">
									<form id="eventsubscribe" action="eventsubscribe" method="post" name="eventsubscribe">
										<table>
											<tr>
												<td>
												<label class="labelBold" for="packageid">Package Id:</label>
												</td>
												<td>
												<label class="labelBold" for="packageid">Target URL:</label>
												</td>
											</tr>
											<tr>
												<td>
												<input name="packageid" required="required" type="text" />
												</td>
												<td>
												<input name="targeturl" required="required" size="50" type="url" />
												</td>
											</tr>
											<tr>
												<td>
													<input class="btn btn-info btn-default" name="subscribe" type="submit" value="Subscribe" />
													<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
												</td>
											</tr>
										</table>
									</form>
								</div>
					<%= subscribeMessage %>
				        <hr/>

     <%= displayDivOpen %>
      <h2>Current subscriptions for <%= uid %></h2>
        <table>
          <tbody>
            <tr>
              <th class="nis">Subscription Id</th>
              <th class="nis">Package Id</th>
              <th class="nis">Target URL</th>
            </tr>
            <%= subscriptionTableHTML %>
          </tbody>
        </table>
							
								<h2>Test</h2>
								<p>Test a subscription using the subscription identifier.</p>
									<form id="eventtest" action="eventtest" method="post" name="eventtest">
										<table>
											<tr>
												<td>
												<label class="labelBold">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
                    <select class="select-width-auto" name="subscriptionid">
                      <%= subscriptionOptionsHTML %>
                    </select>									
												</td>
											</tr>
											<tr>
												<td>
										<input class="btn btn-info btn-default" name="test" type="submit" value="Test" />
												</td>
											</tr>
										</table>
									</form>
									<%= testMessage %>

								<h2>Delete</h2>
								<p>Delete a subscription using the subscription identifier.</p>
									<form id="eventdelete" action="eventdelete" method="post" name="eventdelete">
										<table>
											<tr>
												<td>
												<label class="labelBold">Subscription Id:</label>
												</td>
											</tr>
											<tr>
												<td>
                    <select class="select-width-auto" name="subscriptionid">
                      <%= subscriptionOptionsHTML %>
                    </select>									
												</td>
											</tr>
											<tr>
												<td>
												<input class="btn btn-info btn-default" name="delete" type="submit" value="Delete" />
												</td>
											</tr>
										</table>
									</form>
									<%= deleteMessage %>
					  <%= displayDivClose %>
									
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
