<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
%>

<%
  HttpSession httpSession = request.getSession();
  
  String message = (String) request.getAttribute("message");
  String from = (String) request.getAttribute("from");
  
  if (from != null && !from.isEmpty()) {
    httpSession.setAttribute("from", from);
  }

  if (message == null) {
    message = "";
  }
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal login</title>

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

				<p class="warning" align="center"><%=message%></p>

				<div class="login-form">
					<form id="login" name="loginform" method="post" action="./login"
						target="_top">
						<table align="center" width="270px">
							<tbody>
								<tr>
									<td><label for="uid">User Name:</label></td>
									<td align="right"><input type="text" name="uid"
										required="required" /></td>
								</tr>
								<tr>
									<td><label for="password">Password:</label></td>
									<td align="right"><input type="password" name="password"
										required="required" /></td>
								</tr>
								<tr>
									<td colspan="2" align="right"><input type="submit"
										name="login" value="login" /> <input type="reset"
										name="reset" value="reset" />
									</td>
								</tr>
							</tbody>
						</table>
					</form>
				</div>

			</div>
			<!-- end content -->

      <jsp:include page="/WEB-INF/jsp/foot.jsp" />

		</div>
		<!-- end wrapper -->

</body>
</html>
