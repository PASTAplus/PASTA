<%@ page language="java" import="java.util.*" pageEncoding="UTF-8" %>
<%@ page import="edu.lternet.pasta.common.CalendarUtility" %>
<%@ page import="edu.lternet.pasta.portal.ConfigurationListener" %>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.Tooltip" %>

<%
    final String pageTitle = "Evaluate/Upload Data Packages";
    final String titleText = DataPortalServlet.getTitleText(pageTitle);
    final String downtime = (String) ConfigurationListener.getOptions().getProperty("dataportal.downtime.dayOfWeek");
    HttpSession httpSession = request.getSession();
    String downtimeHTML = "";
    
    if (downtime != null && !downtime.isEmpty()) {
        String today = CalendarUtility.todaysDayOfWeek();
        if (today != null && today.equalsIgnoreCase(downtime)) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("The Data Portal and PASTA+ services will be unavailable on %s evening from 7-9 pm Mountain Time for scheduled weekly maintenance. ",
                                    downtime));
            sb.append("Processing of data packages submitted prior to this time may be interrupted.");
            downtimeHTML = String.format("<em>Please Note: </em>%s",
                                         sb.toString());
        }
    }
    
    String uid = (String) httpSession.getAttribute("uid");
    String warningMessage = (String) request.getAttribute("message");

    if (uid == null || uid.isEmpty()) {
		request.setAttribute("from", "./harvester.jsp");
		String loginWarning = DataPortalServlet.getLoginWarning();
		request.setAttribute("message", loginWarning);
		RequestDispatcher requestDispatcher = request
		    .getRequestDispatcher("./login.jsp");
		requestDispatcher.forward(request, response);
	}
	else if (warningMessage == null) {
      warningMessage = "";
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
								<h2>Evaluate/Upload Data Packages</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						<div class="row-fluid">
							<div class="span12">

								<!-- Content -->
								<p class="nis-warn"><%= warningMessage %></p>
                                <p class="nis-warn"><%= downtimeHTML %></p>                               

								<p>Data packages may be evaluated without uploading 
								them to the repository by selecting <b>Evaluate</b>. Once you 
								are satisfied that data packages are ready to be 
								uploaded to the repository, you may do so by selecting 
								<b>Upload</b>. Several methods for supplying the 
								EML metadata for your data packages are available below.</p>

								<fieldset>
								<legend>EML Metadata File</legend>
								<p>Select an Ecological Metadata Language (EML) file to evaluate or upload.</p>
								
                  <form id="emlFile" name="emlFile" method="post" enctype="multipart/form-data" action="./harvester">
						<div class="display-table">
								<div class="table-row">
									<div class="table-cell">
										<label class="labelBold">EML Metadata File:</label>
									</div>
								</div>
								<div class="table-row">
									<div class="table-cell">
										<input accept="application/xml" name="emlfile" required="required" size="60" type="file" />
									</div>
								</div>
                                <div class="table-row">
									<div class="table-cell">
                                        <label class="labelBold">Data Upload Options:</label>
                                    </div>
                                </div>
                                <div class="table-row">
                                    <div class="table-cell">
                                        <input name="useChecksum" type="checkbox" value="useChecksum" />
                                        Allow PASTA to skip upload of a data entity if it has a matching copy
                                    </div>
                                    <div class="table-cell">
                                    <span name='<%= Tooltip.USE_CHECKSUM %>'
                                          class="tooltip">
                                        <img src="images/hand.png" />
                                        <dfn>Please note</dfn>
                                    </span>
                                    </div>
                                </div>
								<div class="table-row">
									<div class="table-cell">
								    	<input name="desktopUpload" type="checkbox" value="desktopUpload" /> 
										I want to manually upload the data by selecting files on my local system
									</div>
									<div class="table-cell">
									<span name='<%= Tooltip.DESKTOP_HARVEST %>'
									      class="tooltip">
									    <img src="images/hand.png" />
									    <dfn>Please note</dfn>
								    </span>
									</div>
								</div>
                                <div class="table-row">
                                    <div class="table-cell"><br/></div>
                                </div>
								<div class="table-row">
									<div class="table-cell">
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
												<input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
												<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
								  </div>
								</div>
						</div>
										<input id="metadataSource" name="metadataSource" type="hidden" value="emlFile" />
									</form>
								
								</fieldset>
								
								<fieldset>
								<legend>EML Document URLs</legend>
								<p>Enter a list of EML document URLs into the text 
								area below, one per line, and then select <b>Evaluate</b> or <b>Upload</b>.</p>

									<form id="urlList" action="./harvester" method="post" name="urlList">
						<div class="display-table">
								<div class="table-row">
									<div class="table-cell">
										<label class="labelBold">URLs:</label>
								    </div>
								</div>
								<div class="table-row">
									<div class="table-cell">
										<textarea id="urlTextArea" cols="80" name="urlTextArea" required="required" rows="8"></textarea>
								    </div>
								</div>
                                <div class="table-row">
                                    <div class="table-cell">
                                        <label class="labelBold">Data Upload Options:</label>
                                    </div>
                                </div>
                                <div class="table-row">
                                    <div class="table-cell">
                                        <input name="useChecksum" type="checkbox" value="useChecksum" />
                                        Allow PASTA to skip upload of a data entity if it has a matching copy&nbsp;&nbsp;
                                    <span name='<%= Tooltip.USE_CHECKSUM %>'
                                          class="tooltip">
                                        <img src="images/hand.png" />
                                        <dfn>Please note</dfn>
                                    </span>
                                    </div>
                                </div>
                                <div class="table-row">
                                    <div class="table-cell"><br/></div>
                                </div>
                                <div class="table-row">
									<div class="table-cell">
										<input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
										<input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
										<input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
									</div>
								</div>
								<div class="table-row">
								  <div class="table-cell nis-warn">
								    <%=warningMessage%>
								  </div>
								</div>
						</div>
										<input id="metadataSource" name="metadataSource" type="hidden" value="urlList" />
									</form>
								</fieldset>
								
								<!-- More Options for Upload
								<div id="accordion1" class="accordion">
									<div class="accordion-group">
										<div class="accordion-heading ">
											<a class="accordion-toggle" data-parent="#accordion1" data-toggle="collapse" href="#Accordion_1">
											More Options for Upload</a> </div>
										<div id="Accordion_1" class="accordion-body  collapse">
											<div class="accordion-inner ">
											
												<!-- Content 
												
												<fieldset>
												<legend>Copy EML Metadata Text</legend>
												<p>Copy the XML for a single EML metadata document into the text 
			                     area below and then select <b>Evaluate</b> or <b>Upload</b>.</p>
													<form id="emlText" action="./harvester" method="post" name="emlText">
														<table>
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
												</fieldset>
												-->
												
								<fieldset>
									<legend>Metacat Harvest List URL</legend>
									<p>Enter the URL of a Metacat Harvest List and then select <b>Evaluate</b> 
									   or <b>Upload</b>. All <var>documentURL</var> elements in the harvest list will be processed.</p>
									<form id="harvestList" action="./harvester" method="post" name="harvestList">
										<div class="disply-table">
											<div class="table-row">
												<div class="table-cell">
													<label class="labelBold">Metacat Harvest List URL:</label>
													<input name="harvestListURL" required="required" size="80" type="url" />
												</div>
											</div>
											<div class="table-row">
											    <div class="table-cell">
                                                    <label class="labelBold">Data Upload Options:</label>
                                                </div>
                                            </div>
                                            <div class="table-row">
                                                <div class="table-cell">
                                                     <input name="useChecksum" type="checkbox" value="useChecksum" />
                                                     Allow PASTA to skip upload of a data entity if it has a matching copy&nbsp;&nbsp;
                                                     <span name='<%= Tooltip.USE_CHECKSUM %>'
                                                         class="tooltip">
                                                         <img src="images/hand.png" />
                                                         <dfn>Please note</dfn>
                                                    </span>
                                                </div>
                                            </div>
                                            <div class="table-row">
                                                <div class="table-cell"><br/></div>
                                            </div>
                                            <div class="table-row">
											    <div class="table-cell">
													 <input class="btn btn-info btn-default" name="submit" type="submit" value="Evaluate" />
													 <input class="btn btn-info btn-default" name="submit" type="submit" value="Upload" />
													 <input class="btn btn-info btn-default" name="reset" type="reset" value="Clear" />
											    </div>
										    </div>
									    </div>
								        <input id="metadataSource" name="metadataSource" type="hidden" value="harvestList" />
								    </form>
							    </fieldset>
												
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
