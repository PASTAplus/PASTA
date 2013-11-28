<!-- Header -->

<%

	HttpSession httpSession = request.getSession();
	String uid = (String) httpSession.getAttribute("uid");
	String identity = null;
	String uname = null;
	String welcomeBack = null;
	
	if ((uid == null) || (uid.equals(""))) {
		identity = "<a href='./login.jsp'>Login</a>";
		uname = "";
		welcomeBack = "";
	} else {
    identity = "<a id=\"login\" href=\"./logout\">Log Out</a>";
		uname = uid;
		welcomeBack = "Welcome Back";
	}

  final String currentClass = " class='current-menu-item current_page_item'";
  String aboutClass = "";
  String contactClass = "";
  String discoverClass = "";
  String helpClass = "";
  String homeClass = "";
  String loginClass = "";
  String toolsClass = "";
  String requestURI = request.getRequestURI();
  String pageName = requestURI.substring(requestURI.lastIndexOf("/") + 1, 
                                        requestURI.lastIndexOf(".")
                                       );
  if (pageName.equals("about")) {
    aboutClass = currentClass;
  }
  else if (pageName.equals("contact")) {
    contactClass = currentClass;
  }
  else if (pageName.equals("browse") ||
           pageName.equals("packageIdentifier") ||
           pageName.equals("advancedSearch")
          ) {
    discoverClass = currentClass;
  }
  else if (pageName.equals("help")) {
    helpClass = currentClass;
  }
  else if (pageName.equals("home") ||
           pageName.equals("tigerTeam")
          ) {
    homeClass = currentClass;
  }
  else if (pageName.equals("dataPackageEvaluate") ||
           pageName.equals("harvester") ||
           pageName.equals("harvestReport") ||
           pageName.equals("dataPackageDelete") ||
           pageName.equals("eventSubscribe") ||
           pageName.equals("provenanceViewer") ||
           pageName.equals("dataPackageAudit") ||
           pageName.equals("auditReport")
          ) {
    toolsClass = currentClass;
  }
  else if (pageName.equals("login")) {
    loginClass = currentClass;
  }
%>
 
<div class="row-fluid ">
	<div class="span12 page_top_header base_color_background">
	</div>
</div>
<div class="container">
	<div class="row-fluid header_container">
		<div class="span3">
			<a href="home.jsp">
			  <img alt="" src="images/non_st_logo.png" title="LTER : Network">
			</a>
	 </div>
			<div class="span9 menu">
			<ul id="menu-nav" class="menu">
				<li<%= homeClass %>><a href="home.jsp">Home</a></li>
				<li<%= discoverClass %>><a href="#">Discover</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left" style="margin-left: 2px; margin-top: 5px">
					<img style="margin-top: -1px;" alt="" src="images/mini_arrow.png" title="LTER : Network"> 
					Browse Data By:</p>
					<li><a href="browse.jsp">Keyword or LTER Site</a> </li>
					<li><a href="scopebrowse">Package Identifier</a> </li>
					<p class="smallmenu pull-left" style="margin-left: 2px; margin-top: 5px">
					  <img style="margin-top: -1px;" alt="" src="images/mini_arrow.png" title="LTER : Network"> 
					Search Data Using:</p>
					<li><a href="advancedSearch.jsp">Advanced Search</a> </li>
				</ul>
				</li>
				<li<%= toolsClass %>><a href="#">Tools</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left" style="margin-left: 2px; margin-top: 5px">
					<img alt="" src="images/mini_arrow.png" style="margin-top: -1px;" title="LTER : Network"> 
					Manage Data Packages:</p>
					<li><a href="dataPackageEvaluate.jsp">Evaluate Data Packages</a></li>
					<li><a href="harvester.jsp">Evaluate/Upload Data Packages</a></li>
					<li><a href="harvestReport.jsp">View Evaluate/Upload Results</a></li>
					<li><a href="dataPackageDelete.jsp">Delete Data Packages</a></li>
					<p class="smallmenu pull-left" style="margin-left: 2px; margin-top: 5px">
					<img alt="" src="images/mini_arrow.png" style="margin-top: -1px;" title="LTER : Network"> 
					Manage Event Subscriptions:</p>
					<li><a href="eventSubscribe.jsp">Event Subscriptions</a></li>
					<p class="smallmenu pull-left" style="margin-left: 2px; margin-top: 5px">
					<img alt="" src="images/mini_arrow.png" style="margin-top: -1px;" title="LTER : Network"> 
					PASTA Provenance Metadata:</p>
					<li><a href="provenanceViewer.jsp">Provenance Viewer</a></li>
					<p class="smallmenu pull-left" style="margin-left: 2px; margin-top: 5px">
					<img alt="" src="images/mini_arrow.png" style="margin-top: -1px;" title="LTER : Network"> 
					Review PASTA Audit Reports:</p>
					<li><a href="auditReport.jsp">Audit Reports</a></li>
					<li><a href="dataPackageAudit.jsp">Data Package Access Reports</a></li>
				</ul>
				</li>
				<li<%= aboutClass %>><a href="about.jsp">About</a></li>
				<li<%= helpClass %>><a href="help.jsp">Help</a> </li>
				<li<%= contactClass %>><a href="contact.jsp">Contact</a> </li>
				<li<%= loginClass %>><%= identity %></li>
			</ul>
		</div>
	</div>
</div>
<!-- /Header -->

<!-- Divider -->
<div class="row-fluid ">
	<div class="span12 page_top_header line-divider">
	</div>
</div>
<!-- /Divider -->

<!-- Search Section -->
<div class="row-fluid page_title">
	<div class="container">
		<div class="span8">
			<h2 class="title_size"><%= welcomeBack %></h2>
			<h2 class="title_desc loggedin"><%= uname %></h2>
		</div>
		<div class="span4">
			<div class="pull-right">
				<div id="search-3" class="widget title_widget widget_search">
				  <form id="searchform" action="./simpleSearch" class="form-inline" method="post" >
				    <label style="margin-left: 80px; background-color:transparent">Search Term</label>

						<input type="search" name="terms" id="lterterms" class="span11 search-query" size="25" required="required">

<!--  
	          <div class="ui-widget">
	            <input type="search" name="terms" required="required" size="50" id="lterterms" style="font-size: 80%;"/>
	          </div>
-->

						<button class="search_icon" style="margin-top: 25px;" type="submit"></button>
						<label style="margin-left: 80px; background-color:transparent; margin-top: 5px;">
						<img alt="" src="images/mini_arrow.png" style="margin-top: -3px;" title="Advanced Search">
						<a href="advancedSearch.jsp">ADVANCED SEARCH</a></label>
					</form>
					<span class="seperator extralight-border"></span></div>
			</div>
		</div>
	</div>
	<div class="row-fluid divider base_color_background">
		<div class="container">
			<span class="bottom_arrow"></span></div>
	</div>
</div>
<!-- /Search Section -->

<div class="container shadow">
	<span class="bottom_shadow_full"></span>
</div>
