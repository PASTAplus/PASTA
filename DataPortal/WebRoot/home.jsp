<!--

 $Date$
 $Author$
 $Revision$
 
 Copyright 2011,2012 the University of New Mexico.
 
 This work was supported by National Science Foundation Cooperative
 Agreements #DEB-0832652 and #DEB-0936498.
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 http://www.apache.org/licenses/LICENSE-2.0.
 
 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 either express or implied. See the License for the specific
 language governing permissions and limitations under the License.

 -->

<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%@ page import="edu.lternet.pasta.portal.search.LTERTerms" %>
<%
  HttpSession httpSession = request.getSession();
  httpSession.setAttribute("menuid", "home");

  String path = request.getContextPath();
  String basePath = request.getScheme() + "://" + request.getServerName()
      + ":" + request.getServerPort() + path + "/";

  String jqueryString = LTERTerms.getJQueryString(); // for auto-complete using JQuery
%>

<!doctype html>
<html>
<head>
<base href="<%=basePath%>">

<title>NIS Data Portal - Home</title>

<meta http-equiv="pragma" content="no-cache">
<meta http-equiv="cache-control" content="no-cache">
<meta http-equiv="expires" content="0">
<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
<meta http-equiv="description" content="This is my page">

<link rel="stylesheet" type="text/css" href="./css/lter-nis.css">
<link rel="stylesheet" href="http://code.jquery.com/ui/1.10.0/themes/base/jquery-ui.css" />

<jsp:include page="/WEB-INF/jsp/javascript.jsp" />

<script src="http://code.jquery.com/ui/1.10.0/jquery-ui.js"></script>
<script>
  $(function() {
    var availableTags = [ <%=jqueryString%> ];
    
    $( "#lterterms" ).autocomplete({
        source: availableTags
    });
  });
</script>  
</head>

<body>

	<div class="wrapper">

		<jsp:include page="/WEB-INF/jsp/head.jsp" />
		<jsp:include page="/WEB-INF/jsp/menuTopLevel.jsp" />

		<div class="content">

			<div class="section">
				<p>
					Data are one of the most valuable products of the Long-Term
					Ecological Research (LTER) Network program. The LTER Network seeks
					to inform the broader scientific community by providing open access
					to well designed and well documented databases via a Network-wide
					information system. The LTER Network Data Portal contains 
					ecological data packages contributed by 27 past and present LTER
					sites. Please review the <a target="_top" 
					href='http://www.lternet.edu/data/netpolicy.html'> LTER Data 
				    Policy</a> before downloading any data product.
				</p>
			</div>

			<div class="section">
				<p>Search for data packages using one or more terms separated by
					spaces</p>

				<form id="simplesearch" name="simplesearch" method="post"
					action="./simpleSearch">
					<table align="center" cellpadding="4em">
						<tbody>
							<tr>
								<td align="left" width="260px">
								  <label for="terms">Search Terms (use * for any):</label>
								</td>
								<td align="left" width="200px">
								  <div class="ui-widget">
									  <input type="search" name="terms" required="required" size="60" id="lterterms"/>
									</div>
								</td>
								<td align="center" width="70px"><input type="submit"
									name="search" value="search" />
								</td>
								<td align="center" width="40px"><input type="reset"
									name="reset" value="reset" />
								</td>
								<td>(<a target="_top" href="./advancedSearch.jsp">Advanced Search</a>)</td>
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
