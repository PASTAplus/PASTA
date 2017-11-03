<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="edu.lternet.pasta.portal.DataPortalServlet" %>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms" %>

<%
  final String pageTitle = "Search Results";
  final String titleText = DataPortalServlet.getTitleText(pageTitle);
  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String termsListHTML = (String) session.getAttribute("termsListHTML");
  if (termsListHTML == null)
    termsListHTML = "";
  
  String queryText = (String) session.getAttribute("queryText");
  String queryURL = "";
  int textBoxSize = 100;
  String textBoxSizeStr = "100";
  if (!queryText.isEmpty()) {
    queryURL = String.format("%ssimpleSearch?%s", basePath, queryText);
    textBoxSize = Math.min(textBoxSize, queryURL.length());
    textBoxSizeStr = String.format("%d", textBoxSize);
  }

  String mapButtonHTML = (String) request.getAttribute("mapButtonHTML");
  if (mapButtonHTML == null)
    mapButtonHTML = "";

  String relevanceHTML = (String) request.getAttribute("relevanceHTML");
  if (relevanceHTML == null)
    relevanceHTML = "";

  String searchResult = (String) request.getAttribute("searchresult");

  if (searchResult == null)
    searchResult = "";

  //String jqueryString = LTERTerms.getJQueryString(); // for auto-complete using JQuery
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

<!-- JS 
<script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/isotope68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>-->
<script src="js/jquery-1.11.0.min.js" type="text/javascript"></script>
<script src="js/data-shelf-ajax.js" type="text/javascript"></script>
<script src="js/clipboard.min.js"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css">
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css">

</head>

<body>


<jsp:include page="header.jsp" />

    <script>
    var clipboard = new Clipboard('.btn');

    clipboard.on('success', function(e) {
        console.log(e);
    });

    clipboard.on('error', function(e) {
        console.log(e);
    });
    </script>
 
  <div class="row-fluid ">
		<div class="container">
			<div class="row-fluid distance_1">
				<div class="box_shadow box_layout">
					<div class="row-fluid">
						<div class="span12">
							<div class="recent_title">
								<h2>Search Results</h2>
							</div>
							<span class="row-fluid separator_border"></span>
						</div>
						
						<div class="row-fluid">
							<div class="span12">
								<!-- Content -->
	<table>
   		<tr>
			<td>			
				<%=mapButtonHTML%>
			</td>
			<td>
				<%=relevanceHTML%>
			</td>
  		</tr>
	</table>
    <table>
        <tr>
            <td>
              <table>
                <tr>
                  <td><button class="btn btn-info btn-default" data-clipboard-action="copy" data-clipboard-target="#queryURL">Copy Query URL</button></td>
                </tr>
                <tr>
                  <td>&nbsp;</td>
                </tr>
              </table>
            </td>
            <td>&nbsp;</td>
            <td>
              <input id="queryURL" type="url" value="<%=queryURL%>" size="<%=textBoxSizeStr%>">
            </td>
        </tr>
    </table>


                <%=termsListHTML%>
				<%=searchResult%>
			    
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
