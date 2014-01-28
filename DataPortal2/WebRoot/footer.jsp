<!--
  ~ Copyright 2011-2013 the University of New Mexico.
  ~
  ~ This work was supported by National Science Foundation Cooperative
  ~ Agreements #DEB-0832652 and #DEB-0936498.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~ http://www.apache.org/licenses/LICENSE-2.0.
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
  ~ either express or implied. See the License for the specific
  ~ language governing permissions and limitations under the License.
  -->
<%@ page import="edu.lternet.pasta.portal.DataPackageSurvey" pageEncoding="UTF-8" %>

<%

DataPackageSurvey dps = new DataPackageSurvey();
String[] popular = dps.surveyDataPackages("popular", 2);
//String[][] recent = DataPackageSurvey.surveyDataPackages("recent", 2);

//String popularScope1 = "knb-lter-mcr";
//String popularIdentifier1 = "1036";
//String popularTitle1 = "MCR LTER: Coral Reef: Bathymetry Grid for North Shore";
//String popularDate1 = "2013-12-05";

String popularScope1 = popular[0];
String popularIdentifier1 = popular[1];
String popularTitle1 = popular[2];
String popularDate1 = popular[3];

/*
String popularScope2 = popular[1][0];
String popularIdentifier2 = popular[1][1];
String popularTitle2 = popular[1][2];
String popularDate2 = popular[1][3];

String recentScope1 = recent[0][0];
String recentIdentifier1 = recent[0][1];
String recentTitle1 = recent[0][2];
String recentDate1 = recent[0][3];

String recentScope2 = recent[1][0];
String recentIdentifier2 = recent[1][1];
String recentTitle2 = recent[1][2];
String recentDate2 = recent[1][3];
*/

%>

	<!-- Divider -->
	<div class="footers row-fluid pull-left distance_1">
		<div class="row-fluid ">
			<div class="span12 page_top_header line-divider">
			</div>
		</div>

	<!-- /Footer -->
    <footer class="row-fluid ">
      <div class="row-fluid">
				<div class="span12">
					<div class="container">
						<div class="row-fluid">
							<div class="span4">
								<div class="widget widget_recent_posts">
									<div class="footer_title">
										<h2 class="widget-title">Most Popular</h2>
									</div>
									<dl>
										<dt><a href="./mapbrowse?scope=<%= popularScope1 %>&identifier=<%= popularIdentifier1 %>"><span class="post_icon"></span></a></dt>
										<dd class="without_avatar"><%= popularDate1 %>
										<a href="./mapbrowse?scope=knb-lter-mcr&identifier=1036"><%= popularTitle1 %></a> </dd>
									</dl>
									<dl>
										<dt><a href="./mapbrowse?scope=knb-lter-sbc&identifier=21"><span class="post_icon"></span></a></dt>
										<dd class="without_avatar">2013-07-07
										<a href="./mapbrowse?scope=knb-lter-sbc&identifier=21">SBCLTER: Reef: Net primary production, growth and standing crop of Macrocystis pyrifera in Southern California</a> </dd>
									</dl>
								</div>
							</div>
							<div class="span6">
								<div class="widget widget_recent_posts">
									<div class="footer_title">
										<h2 class="widget-title">Recent Uploads</h2>
									</div>
									<dl>
										<dt><a href="./mapbrowse?scope=knb-lter-nin&identifier=1"><span class="post_icon"></span></a></dt>
										<dd class="without_avatar">2014-01-13
										<a href="./mapbrowse?scope=knb-lter-nin&identifier=1">Daily Water Sample Nutrient Data for North Inlet Estuary, South Carolina, from 1978 to 1992, North Inlet LTER</a> </dd>
									</dl>
									<dl>
										<dt><a href="./mapbrowse?scope=knb-lter-nin&identifier=99"><span class="post_icon"></span></a></dt>
										<dd class="without_avatar">2014-01-15
										<a href="./mapbrowse?scope=knb-lter-nin&identifier=99">Meteorological data for North Inlet Estuary, South Carolina, from 1982 to 1985, North Inlet LTER</a> </dd>
									</dl>
								</div>
							</div>
						</div>
					</div>
				</div>
		  </div>
    </footer>
  <!-- /Footer -->
      
    <div class="row-fluid base_color_background footer_copyright">
			<div class="span12">
				<div class="container">
					<span class="arrow row-fluid"><span class="span12"></span>
					</span>
					<div class="row-fluid">
						<div class="span12 ">
							Copyright 2009-2013 <a href="http://www.lternet.edu">Long Term Ecological Research
							Network</a>. This material is based upon work supported 
							by the <a href="http://www.nsf.gov/">National Science 
							Foundation</a> under Cooperative Agreements
							<a href="http://www.fastlane.nsf.gov/servlet/showaward?award=0832652" target="_blank">
							#DEB-0832652</a> and
							<a href="http://www.fastlane.nsf.gov/servlet/showaward?award=0936498" target="_blank">
							#DEB-0936498</a>. Any opinions, findings, conclusions, 
							or recommendations expressed in the material are those 
							of the author(s) and do not necessarily reflect the 
							views of the National Science Foundation. Please
							<a href="http://www.LTERnet.edu/contact" target="_blank">
							contact us</a> with questions, comments, or for technical 
							assistance regarding this web site or the LTER Network.<br/><br/>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
  <!-- /Divider -->
		