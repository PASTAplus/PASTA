<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="java.io.File" %>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms" %>
<%@ page import="edu.lternet.pasta.portal.search.BrowseSearch" %>
<%@ page import="edu.lternet.pasta.portal.search.BrowseGroup" %>

<%
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String searchResult = (String) request.getAttribute("searchresult");

  if (searchResult == null)
    searchResult = "";
    
  String browseHTML = "";
  
  HttpSession httpSession = request.getSession();
  ServletContext servletContext = httpSession.getServletContext();
  browseHTML = (String) servletContext.getAttribute("browseHTML");

  /* File browseCacheFile = new File(BrowseSearch.browseCachePath);

  if (browseCacheFile.exists()) {
      BrowseSearch browseSearch = new BrowseSearch();
      BrowseGroup browseGroup = browseSearch.readBrowseCache(browseCacheFile);
      
      ServletContext servletContext = getServletContext();

      /* Lock the servlet context object to guarantee that only one thread at a
       * time can be getting or setting the context attribute. 
       *
      browseHTML = browseGroup.toHTML();
   } */
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>Browse Data Packages</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">
<link rel="stylesheet" href="./css/jquery-ui-1.10.0.css" />

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

<script src="./js/jquery-ui-1.10.0.js"></script>
<script src="./js/toggle.js" type="text/javascript"></script>

    <script language="javascript" type="text/javascript">
    
      function keywordSearch(formObj, searchKeyword) {
        var searchString = trim(searchKeyword);
        alert("searchString: " + searchString);
        formObj.browseValue.value=searchString;
        formObj.submit();
        return true;
      }
      
    </script>

</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<h2 align="center">Browse Data Packages</h2>

			<fieldset>
				<p>Browse by category using the links below. The number of matching data sets is shown in parentheses.&#42;</p>

        <!-- <p><strong>Alternative:</strong> <a href="http://vocab.lternet.edu" target="new">Multi-level Browse</a></p> -->
        
				<div class="section">
					<form id="browsesearch" name="browsesearch" method="post" action="./browseServlet">
	          <table id="browseSearch">
	            <tbody>
	            <tr>
	              <th align="left">Categories &amp; Terms</th>
	            </tr>
                <%= browseHTML %>
              </tbody>
            </table>
            <input type="hidden" name="browseValue" value="" />
          </form>
				</div>
				<p><small>&#42; <em>Please note: (1) Only public documents are accessible from this page; (2) Search results are refreshed nightly.</em></small></p>
			</fieldset>

			<div class="section-table">
				<%=searchResult%>
			</div>
			<!-- end of section-table -->

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
