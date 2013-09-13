<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";
      
  HttpSession httpSession = request.getSession();
        
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>Metadata Previewer</title>

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Metadata Previewer</h2>

      <fieldset>
        <legend>Preview Metadata Rendering</legend>

        <p>Preview a rendered version of EML in HTML format by uploading your
           EML file</p>

        <div class="section">
          <form id="metadataPreviewer" name="metadataPreviewer" method="post"
            action="./metadataPreviewer" enctype="multipart/form-data"
            target="_blank">
            <table align="left" cellpadding="4em">
              <tbody>
                <tr>
                  <td align="left" width="130px"><label for="packageid">File:</label>
                  </td>
                  <td align="left" width="200px"><input type="file"
                    name="emlfile" accept="application/xml" size="60" 
                    required="required" /></td>
                  <td align="center" width="70px">
                    <input type="submit" name="upload" value="Upload" />
                  </td>
                  <td align="center" width="40px">
                    <input type="reset" name="reset" value="Clear" />
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
