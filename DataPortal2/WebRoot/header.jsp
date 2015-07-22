<%@ page import="edu.lternet.pasta.portal.Tooltip" %>

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
  String dataClass = "";
  String helpClass = "";
  String homeClass = "";
  String loginClass = "";
  String toolsClass = "";
  String requestURI = request.getRequestURI();
  String pageName = requestURI.substring(requestURI.lastIndexOf("/") + 1, 
                                        requestURI.lastIndexOf(".")
                                       );
  if (pageName.equals("browse") ||
           pageName.equals("packageIdentifier") ||
           pageName.equals("advancedSearch") ||
           pageName.equals("savedData")
          ) {
    dataClass = currentClass;
  }
  else if (pageName.equals("help") ||
           pageName.equals("resources")
          ) {
    helpClass = currentClass;
  }
  else if (pageName.equals("home")) {
    homeClass = currentClass;
  }
  else if (pageName.equals("dataPackageEvaluate") ||
           pageName.equals("harvester") ||
           pageName.equals("harvestReport") ||
           //pageName.equals("dataPackageDelete") ||
           pageName.equals("eventSubscribe") ||
           pageName.equals("provenanceGenerator") ||
           pageName.equals("dataPackageAudit") ||
           pageName.equals("auditReport")
          ) {
    toolsClass = currentClass;
  }
  else if (pageName.equals("login")) {
    loginClass = currentClass;
  }
%>
 
<header role="banner">
<div class="row-fluid ">
	<div class="span12 page_top_header base_color_background">
	</div>
</div>
<div class="container">
	<div class="row-fluid header_container">
		<div class="span3">
			<a href="home.jsp">
			  <img alt="LTER Network Data Portal logo" src="images/nis_logo.png" title="LTER : Network">
			</a>
	 </div>
			<div class="span9 menu">
			<nav role="navigation">
			<ul id="menu-nav" class="menu">
				<li<%= homeClass %>><a href="home.jsp">Home</a></li>
				<li<%= dataClass %>><a href="#">Data</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="LTER : Network"> 
					Browse Data By:</p>
					<li><a href="browse.jsp">Keyword or LTER Site</a> </li>
					<li><a href="scopebrowse">Package Identifier</a> </li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					  <img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="LTER : Network"> 
					Search Data:</p>
					<li><a href="advancedSearch.jsp">Advanced Search</a> </li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					  <img class="mini-arrow-margin" alt="" src="images/mini_arrow.png" title="LTER : Network"> 
					Store Data:</p>
					<li><a href="savedDataServlet">Your Data Shelf</a> </li>
				</ul>
				</li>
				<li<%= toolsClass %>><a href="#">Tools</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					Data Packages:</p>
					<!-- <li><a href="dataPackageEvaluate.jsp">Evaluate Data Packages</a></li> -->
					<li><a href="harvester.jsp">Evaluate/Upload Data Packages</a></li>
					<li><a href="harvestReport.jsp">View Evaluate/Upload Results</a></li>
					<!--  <li><a href="dataPackageDelete.jsp">Delete Data Packages</a></li> -->
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					Events:</p>
					<li><a href="eventSubscribe.jsp">Event Subscriptions</a></li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					Provenance:</p>
					<li><a href="provenanceGenerator.jsp">Provenance Generator</a></li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					Reports:</p>
					<li><a href="auditReport.jsp">Audit Reports</a></li>
					<li><a href="dataPackageAudit.jsp">Data Package Access Reports</a></li>
				</ul>
				</li>
				<li<%= helpClass %>><a href="#">Help</a>
				<ul class="sub-menu">
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					Support:</p>
				  <!-- <li><a href="help.jsp">How Do I...</a></li> -->
				  <li><a href="contact.jsp">Contact Us</a></li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					Resources:</p>
				  <li><a href="resources.jsp">LTER Resources</a></li>
					<p class="smallmenu pull-left nis-navigation-submenu">
					<img alt="" src="images/mini_arrow.png" class="mini-arrow-margin" title="LTER : Network"> 
					About:</p>
				  <li><a href="about.jsp">About the LTER Network Data Portal</a></li>
				</ul>
				</li>
				<li<%= loginClass %>><%= identity %></li>
			</ul>
			</nav>
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
				    <!-- <label class="nis-search-label">Search Terms</label> -->
					<!-- <span name='<%= Tooltip.SEARCH_TERMS %>'
						  class="tooltip"> -->
						<input type="search" 
							name="terms" 
							id="lterterms" 
							class="span11 search-query"
							placeholder="enter search terms" 
							size="25" required="required">
					<!-- </span> -->
						<button class="search_icon" type="submit"></button>
						<label id="advanced-search-label" class="nis-search-label">
						  <img id="advanced-search-arrow" alt="" src="images/mini_arrow.png" title="Advanced Search">
						  <a href="advancedSearch.jsp">ADVANCED SEARCH</a>
						</label>
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
</header>
