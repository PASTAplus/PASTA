<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName()
	    + ":" + request.getServerPort() + path + "/";

	HttpSession httpSession = request.getSession();
	String citationMessage = (String) httpSession
	    .getAttribute("citationmessage");
	String html = (String) httpSession.getAttribute("html");

	if (html == null) {
		html = "";
	}
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Package Citation</title>

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

			<fieldset>
				<legend>Data Package Citation</legend>

				<p>
					The LTER Network considers it a matter of professional ethics to
					acknowledge the work of other scientists. Thus, you, the <em>Data User</em>
					should properly cite the <em>Data Package</em> you utilize in any
					publication or in the metadata of any derived data product that is
					produced using the <em>Data Package</em>.
				<p>The citation should have the following general form:</p>

				<%
					if (citationMessage != null)
						out.println(citationMessage);
				%>

				<ul style="list-style: none;">
					<li><em> <%=html%> </em>
					</li>
				</ul>

				<p>
					Please refer to the LTER Network <a
						href="http://www.lternet.edu/policies/data-access" target="_blank">Data
						Access Policy</a> for additional information.
				</p>

			</fieldset>

		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>