<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
      
  HttpSession httpSession = request.getSession();
        
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
<base href="<%=basePath%>">

<title>Data Package Viewer</title>

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Data Package Evaluate</h2>

      <fieldset>
        <legend>Evaluate Data Package</legend>

        <p>Evaluate a data package and view its quality report by uploading an Ecological
          Metadata Language file</p>

        <div class="section">
          <form id="uploadevaluate" name="uploadevaluate" method="post"
            action="./uploadevaluate" enctype="multipart/form-data"
            target="_blank">
            <table align="left" cellpadding="4em">
              <tbody>
                <tr>
                  <td align="right" width="130px"><label for="packageid">File:</label>
                  </td>
                  <td align="left" width="200px"><input type="file"
                    name="emlfile" accept="application/xml" size="60" 
                    required="required" /></td>
                </tr>
                <tr>
                  <td></td>
                  <td align="left" width="110px">
                    <input type="submit" name="upload" value="evaluate" />
                    <input type="reset" name="reset" value="reset" />
                  </td>
                </tr>
              </tbody>
            </table>
          </form>
        </div>
      </fieldset>


		</div>
		<!-- end of content -->

		<jsp:include page="/WEB-INF/jsp/foot.jsp" />

	</div>
	<!-- end of wrapper -->

</body>
</html>
