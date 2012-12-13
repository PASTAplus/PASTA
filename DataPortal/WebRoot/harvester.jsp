<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet"%>
<%@ page import="edu.lternet.pasta.portal.HarvestReportServlet"%>

<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String uid = (String) session.getAttribute("uid");

  if (uid == null || uid.isEmpty()) {
    request.setAttribute("from", "./harvester.jsp");
    String loginWarning = DataPortalServlet.getLoginWarning();
    request.setAttribute("message", loginWarning);
    RequestDispatcher requestDispatcher = request
        .getRequestDispatcher("./login.jsp");
    requestDispatcher.forward(request, response);
  }

  String warningMessage = (String) request.getAttribute("message");
  if (warningMessage == null) {
    warningMessage = "";
  }
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Evaluate/Upload Data Packages</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />
<script src="./js/toggle.js" type="text/javascript"></script>

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Evaluate/Upload Data Packages</h2>

			<%=warningMessage%>

			<p>Data packages may be evaluated without uploading them to the NIS
				by selecting "evaluate". Once you are satisfied that data packages
				are ready to be uploaded to the NIS, you may do so by selecting
				"upload". Several alternatives for supplying the EML metadata for
				your data packages are available below.</p>

            <fieldset>
                <legend>EML Metadata File</legend>
                <p>Add an EML metadata file to the form and then select "evaluate" or
                    "upload".</p>
                <div class="section">
                    <form id="emlFile" name="emlFile" method="post"
                        enctype="multipart/form-data" action="./harvester">
                        <table align="left" cellpadding="4em">
                            <tbody>
                                <tr>
                                    <td align="left"><label for="packageid">File: </label> <input
                                        type="file" name="emlfile" accept="application/xml" size="60" 
                                         required="required" />
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left"><input type="submit" name="submit"
                                        value="evaluate" /> <input type="submit" name="submit"
                                        value="upload" /> <input type="reset" name="reset"
                                        value="reset" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <input type="hidden" name="metadataSource" id="metadataSource"
                            value="emlFile" />
                    </form>
                </div>
            </fieldset>

            <fieldset>
                <legend>EML Document URLs</legend>
                <p>Enter a list of EML document URLs into the text area below,
                    one per line, and then select "evaluate" or "upload".</p>
                <div class="section">
                    <form id="urlList" name="urlList" method="post"
                        action="./harvester">
                        <table align="left" cellpadding="4em">
                            <tbody>
                                <tr>
                                    <td align="left"><textarea id="urlTextArea"
                                            name="urlTextArea" rows="8" cols="80"
                                             required="required"></textarea>
                                    </td>
                                </tr>
                                <tr>
                                    <td align="left"><input type="submit" name="submit"
                                        value="evaluate" /> <input type="submit" name="submit"
                                        value="upload" /> <input type="reset" name="reset"
                                        value="reset" />
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                        <input type="hidden" name="metadataSource" id="metadataSource"
                            value="urlList" />
                    </form>
                </div>
            </fieldset>

            <h4 id="toggleEntities" class="toggleButton"><button>+/-</button> More Options for Upload</h4>
            <div class="collapsible">

			<fieldset>
			    <legend>Copy EML Metadata Text</legend>
				<p>Copy the XML for a single EML metadata document into the text
					area below and then select "evaluate" or "upload".</p>
				<div class="section">
					<form id="emlText" name="emlText" method="post"
						action="./harvester">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left"><textarea id="emlTextArea"
											name="emlTextArea" rows="8" cols="100" required="required"></textarea></td>
								</tr>
								<tr>
									<td align="left"><input type="submit" name="submit"
										value="evaluate" /> <input type="submit" name="submit"
										value="upload" /> <input type="reset" name="reset"
										value="reset" /></td>
								</tr>
							</tbody>
						</table>
						<input type="hidden" name="metadataSource" id="metadataSource"
							value="emlText" />
					</form>
				</div>
			</fieldset>
            
			<fieldset>
				<legend>Metacat Harvest List URL</legend>
				<p>
					Enter the URL of a Metacat Harvest List and then select "evaluate"
					or "upload". All
					<code>documentURL</code>
					elements in the harvest list will be processed.
				</p>
				<div class="section">
					<form id="harvestList" name="harvestList" method="post"
						action="./harvester">
						<table align="left" cellpadding="4em">
							<tbody>
								<tr>
									<td align="left"><label for="harvestListURL">Metacat
											Harvest List URL:</label> <input type="text" name="harvestListURL"
										size="150"
										required="required" />
									</td>
								</tr>
								<tr>
									<td align="left"><input type="submit" name="submit"
										value="evaluate" /> <input type="submit" name="submit"
										value="upload" /> <input type="reset" name="reset"
										value="reset" />
									</td>
								</tr>
							</tbody>
						</table>
						<input type="hidden" name="metadataSource" id="metadataSource"
							value="harvestList" />
					</form>
				</div>
			</fieldset>
            </div>

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

	<script type="text/javascript">
		jQuery(document).ready(function() {
			jQuery(".toggleButton").click(function() {
				jQuery(this).next(".collapsible").slideToggle("fast");
			});
			jQuery(".collapsible").hide();
			jQuery("#toggleSummary").next(".collapsible").show();
		});
		jQuery("#showAll").click(function() {
			jQuery(".collapsible").show();
		});
		jQuery("#hideAll").click(function() {
			jQuery(".collapsible").hide();
		});
	</script>

</body>
</html>
