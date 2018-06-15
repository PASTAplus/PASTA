<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ page import="java.io.File"%>
<%@ page import="java.util.*"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.portal.Harvester"%>

<%
	HttpSession httpSession = request.getSession();
	String uid = (String) httpSession.getAttribute("uid");
	if (uid == null || uid.isEmpty()) {
		request.setAttribute("from", "./desktopHarvester.jsp");
		String loginWarning = DataPortalServlet.getLoginWarning();
		request.setAttribute("message", loginWarning);
		RequestDispatcher requestDispatcher = request
		    .getRequestDispatcher("./login.jsp");
		requestDispatcher.forward(request, response);
	}
	
	String desktopUploadHTML = (String) httpSession.getAttribute("desktopUploadHTML");
	File emlFile = (File) httpSession.getAttribute("emlFile");
	String emlFileName = emlFile.getName();
	Boolean isEvaluate = (Boolean) httpSession.getAttribute("isEvaluate");
	String buttonVerb = isEvaluate ? "Evaluate" : "Upload";
    Boolean useChecksum = (Boolean) httpSession.getAttribute("useChecksum");
	
  final String pageTitle = buttonVerb + " Data Package";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
%>

<!DOCTYPE html>
<html lang="en">

<head>
<title><%= titleText %></title>

<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1"
	name="viewport">

<link rel="shortcut icon" href="./images/favicon.ico"
	type="image/x-icon" />

<!-- Google Fonts CSS -->
<link
	href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic"
	rel="stylesheet" type="text/css">

<!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
<link href="css/style_slate.css" media="all" rel="stylesheet"
	type="text/css">

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
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet"
	type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen"
	rel="stylesheet" type="text/css">

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
									<h2><%= pageTitle %>:
										<%= emlFileName %></h2>
								</div>
								<span class="row-fluid separator_border"></span>
							</div>
							<div class="row-fluid">
								<div class="span12">

									<!-- Content -->
									<p>For each of the data entities documented in your EML,
										please select the data file from your desktop's
										file system corresponding to the <b>objectName</b>
										value documented in the EML file.</p>
									<p><strong>Please note</strong>:<em> An object name containing
									spaces cannot be processed and will result in an error.</em></p>
									<div class="section">
										<form id="desktopUpload" name="desktopUpload" method="post"
											enctype="multipart/form-data" action="./harvester">

											<table>
												<tr style="background:#ababff">
													<th class="nis">Entity Name</th>
													<th class="nis">Object Name</th>
													<th class="nis">Data File<br/><small>(<em>The file name you select should match the <b>object name</b>.</em>)</small></th>
												</tr>
									<c:forEach items="${sessionScope.entityList}" var="entity"
										varStatus="status">
										<c:choose>
											<c:when test="${status.count % 2 == 0}">
												<tr id="row-${status.count}"
													style="background: lightgray">
											</c:when>
											<c:otherwise>
												<tr id="row-${status.count}">
											</c:otherwise>
										</c:choose>
													<td class="nis">${entity.name}</td>
													<td class="nis"
														id="object-name-${entity.entityId}">${entity.objectName}
														</td>
													<td class="nis">
													
													    <input
													    	name="object-name-${entity.entityId}"
													    	type="hidden"
															value="${entity.objectName}" />
						
														<input 
															name="entity-${entity.entityId}" 
															id="entity-${entity.entityId}" 
															required="required"
															size="60" type="file" />
													</td>
												</tr>
									</c:forEach>
												<tr>
													<td colspan="3"></td>
												</tr>
												<tr>
													<td colspan="3"></td>
												</tr>
											</table>

											<div class="display-table">
												<div class="table-row">
													<div class="table-cell">
														<input 
															class="btn btn-info btn-default" 
															name="submit"
															type="submit" 
															value="<%= buttonVerb %>" 
															/> 
														<input class="btn btn-info btn-default" name="reset"
															type="reset" value="Clear" />
													</div>
												</div>
											</div>

											<input id="metadataSource" name="metadataSource"
												type="hidden" value="desktopHarvester" />
                                            <c:if test="${useChecksum}">
                                                <input id="useChecksum" name="useChecksum"
                                                    type="hidden" value="useChecksum" />
                                            </c:if>
										</form>
									</div>
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
