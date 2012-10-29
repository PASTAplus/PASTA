<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Tools</title>

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

			<h4>Manage Your Data Packages</h4>
			<ul>
        <li><a href="./dataPackageEvaluate.jsp">Evaluate Data Packages</a>
        </li>
				<li><a href="./harvester.jsp">Harvest Data Packages</a>
				</li>
				<li><a href="./harvestReport.jsp">View Harvest Reports</a>
				</li>
				<li><a href="./dataPackageDelete.jsp">Delete Data Packages</a>
				</li>
			</ul>

			<h4>Manage Your Event Subscriptions</h4>
			<ul>
				<li><a href="./eventSubscribe.jsp">Subscribe/Review/Test/Delete</a>
				</li>
			</ul>

            <h4>View PASTA Provenance Metadata</h4>
            <ul>
                <li><a href="./provenanceViewer.jsp">Provenance Viewer</a>
            </ul>

			<h4>Review PASTA Audit Reports</h4>
			<ul>
				<li><a href="./dataPackageAudit.jsp">Data Package Audit Reports</a></li>
				<li><a href="./auditReport.jsp">Filtered Audit Reports</a></li>
			</ul>
			
		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
