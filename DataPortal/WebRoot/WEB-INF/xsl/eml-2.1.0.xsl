<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  $Date$
  $Author: mservilla	$
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

<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
  xmlns:eml="eml://ecoinformatics.org/eml-2.1.0">

  <xsl:output method="html" encoding="utf-8" indent="yes" />

  <xsl:variable name="prov-stmt"
    select="'This method step describes provenance-based metadata as specified in the LTER EML Best Practices.'"/>

  <xsl:template match="/">

    <xsl:variable name="packageid" select="/eml:eml/@packageId"/>
    
<xsl:text disable-output-escaping='yes'>&lt;!doctype html>&#x0A;</xsl:text>

<html>

<head>
    <title>Metadata Previewer</title>
    <link rel="stylesheet" type="text/css" href="./css/lter-nis.css"></link>
    <script src="./js/jquery-1.7.1.js" type="text/javascript"></script>
    <script src="./js/toggle.js" type="text/javascript"></script>
</head>

<body>
    <div class="eml">
      <h3 id="top" >
        <xsl:value-of select="/eml:eml/dataset/title"/>
      </h3>

      <fieldset>
        <legend>Summary Information</legend>
        <div class="section-table">
          <table>
            <tbody>
              <tr>
                <td class="title">Data Package Identifier:</td>
                <td class="data">
                  <xsl:value-of select="$packageid"/>
                </td>
              </tr>
              <tr>
                <td class="title">Title:</td>
                <td class="data">
                  <xsl:value-of select="/eml:eml/dataset/title"/>
                </td>
              </tr>
              <!-- Prefer individual name; otherwise, test for organization name -->
              <xsl:for-each select="/eml:eml/dataset/creator">
                <tr>
                  <td class="title">Creator:</td>
                  <td class="data">
                    <xsl:choose>
                      <xsl:when test="./individualName">
                        <xsl:value-of select="./individualName"/>
                      </xsl:when>
                      <xsl:when test="./organizationName">
                        <xsl:value-of select="./organizationName"/>
                      </xsl:when>
                      <xsl:when test="./positionName">
                        <xsl:value-of select="./positionName"/>
                      </xsl:when>
                    </xsl:choose>
                  </td>
                </tr>
              </xsl:for-each>
              <xsl:apply-templates select="/eml:eml/dataset/abstract"/>
              <xsl:apply-templates select="/eml:eml/dataset/coverage/temporalCoverage"
                mode="summary"/>
              <xsl:apply-templates
                select="/eml:eml/dataset/coverage/geographicCoverage/geographicDescription"/>
              <!-- Iterate over each of six possible data entity types -->
              <!-- Data Table -->
              <xsl:for-each select="/eml:eml/dataset/dataTable">
                <tr>
                  <td class="title">Data Table:</td>
                  <td class="data">
                    <xsl:value-of select="./entityName"/>
                  </td>
                </tr>
              </xsl:for-each>
              <!-- Spatial Raster -->
              <xsl:for-each select="/eml:eml/dataset/spatialRaster">
                <tr>
                  <td class="title">Spatial Raster:</td>
                  <td class="data">
                    <xsl:value-of select="./entityName"/>
                    <xsl:if test="./physical/size"> - <xsl:apply-templates select="./physical/size"
                      />
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
              <!-- Spatial Vector -->
              <xsl:for-each select="/eml:eml/dataset/spatialVector">
                <tr>
                  <td class="title">Spatial Vector:</td>
                  <td class="data">
                    <xsl:value-of select="./entityName"/>
                    <xsl:if test="./physical/size"> - <xsl:apply-templates select="./physical/size"
                      />
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
              <!-- Stored Procedure -->
              <xsl:for-each select="/eml:eml/dataset/storedProcedure">
                <tr>
                  <td class="title">Stored Procedure:</td>
                  <td class="data">
                    <xsl:value-of select="./entityName"/>
                    <xsl:if test="./physical/size"> - <xsl:apply-templates select="./physical/size"
                      />
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
              <!-- View -->
              <xsl:for-each select="/eml:eml/dataset/view">
                <tr>
                  <td class="title">View:</td>
                  <td class="data">
                    <xsl:value-of select="./entityName"/>
                    <xsl:if test="./physical/size"> - <xsl:apply-templates select="./physical/size"
                      />
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
              <!-- Other Entity -->
              <xsl:for-each select="/eml:eml/dataset/otherEntity">
                <tr>
                  <td class="title">Other Entity:</td>
                  <td class="data">
                    <xsl:value-of select="./entityName"/>
                    <xsl:if test="./physical/size"> - <xsl:apply-templates select="./physical/size"
                      />
                    </xsl:if>
                  </td>
                </tr>
              </xsl:for-each>
            </tbody>
          </table>
        </div>
        <!-- end of section-table -->
      </fieldset>

      <p id="toggle"> Show/hide full metadata <button>+/-</button>
      </p>
      <div class="collapsible">

        <!-- Table of contents -->
        <p style="margin-bottom: -0.5em;">Table of Contents:</p>
        <ul>
          <li>
            <a href="#org">Organization/Personnel Information</a>
          </li>
          <xsl:if test="/eml:eml/dataset/methods">
            <li>
              <a href="#methods">Data Set Methods</a>
            </li>
          </xsl:if>
          <!-- Iterate over each of six possible data entity types -->
          <!-- Data Table -->
          <xsl:if test="/eml:eml/dataset/dataTable">
            <li> Data Table(s) <ul>
                <xsl:for-each select="/eml:eml/dataset/dataTable">
                  <li>
                    <xsl:element name="a">
                      <xsl:attribute name="href">#<xsl:value-of select="./entityName"
                        /></xsl:attribute>
                      <xsl:value-of select="./entityName"/>
                    </xsl:element>
                  </li>
                </xsl:for-each>
              </ul>
            </li>
          </xsl:if>
          <!-- Spatial Raster -->
          <xsl:if test="/eml:eml/dataset/spatialRaster">
            <li> Spatial Raster(s) <ul>
                <xsl:for-each select="/eml:eml/dataset/spatialRaster">
                  <li>
                    <xsl:element name="a">
                      <xsl:attribute name="href">#<xsl:value-of select="./entityName"
                        /></xsl:attribute>
                      <xsl:value-of select="./entityName"/>
                    </xsl:element>
                  </li>
                </xsl:for-each>
              </ul>
            </li>
          </xsl:if>
          <!-- Spatial Vector -->
          <xsl:if test="/eml:eml/dataset/spatialVector">
            <li> Spatial Vector(s) <ul>
                <xsl:for-each select="/eml:eml/dataset/spatialVector">
                  <li>
                    <xsl:element name="a">
                      <xsl:attribute name="href">#<xsl:value-of select="./entityName"
                        /></xsl:attribute>
                      <xsl:value-of select="./entityName"/>
                    </xsl:element>
                  </li>
                </xsl:for-each>
              </ul>
            </li>
          </xsl:if>
          <!-- Stored Procedure -->
          <xsl:if test="/eml:eml/dataset/storedProcedure">
            <li> Stored Procedure(s) <ul>
                <xsl:for-each select="/eml:eml/dataset/storedProcedure">
                  <li>
                    <xsl:element name="a">
                      <xsl:attribute name="href">#<xsl:value-of select="./entityName"
                        /></xsl:attribute>
                      <xsl:value-of select="./entityName"/>
                    </xsl:element>
                  </li>
                </xsl:for-each>
              </ul>
            </li>
          </xsl:if>
          <!-- View -->
          <xsl:if test="/eml:eml/dataset/view">
            <li> View(s) <ul>
                <xsl:for-each select="/eml:eml/dataset/view">
                  <li>
                    <xsl:element name="a">
                      <xsl:attribute name="href">#<xsl:value-of select="./entityName"
                        /></xsl:attribute>
                      <xsl:value-of select="./entityName"/>
                    </xsl:element>
                  </li>
                </xsl:for-each>
              </ul>
            </li>
          </xsl:if>
          <!-- Other Entity -->
          <xsl:if test="/eml:eml/dataset/otherEntity">
            <li> Other Entity(s) <ul>
                <xsl:for-each select="/eml:eml/dataset/otherEntity">
                  <li>
                    <xsl:element name="a">
                      <xsl:attribute name="href">#<xsl:value-of select="./entityName"
                        /></xsl:attribute>
                      <xsl:value-of select="./entityName"/>
                    </xsl:element>
                  </li>
                </xsl:for-each>
              </ul>
            </li>
          </xsl:if>
        </ul>

        <!-- Organization/Personnel Information -->
        <fieldset>
          <legend id="org"> Organization/Personnel Information ( <a href="#top"
              >top</a> ) </legend>
          <div class="section-table">
            <table>
              <tbody>
                <xsl:apply-templates select="/eml:eml/dataset/creator" mode="org"/>
                <xsl:apply-templates select="/eml:eml/dataset/contact" mode="org"/>
                <xsl:apply-templates select="/eml:eml/dataset/metadataProvider" mode="org"/>
                <xsl:apply-templates select="/eml:eml/dataset/publisher" mode="org"/>
              </tbody>
            </table>
          </div>
        </fieldset>

        <!-- Data Set Methods -->
        <xsl:if test="/eml:eml/dataset/methods">
          <fieldset>
            <legend id="methods"> Data Set Methods ( <a href="#top">top</a> ) </legend>
            <div class="section-table">
              <table>
                <tbody>
                  <xsl:apply-templates select="/eml:eml/dataset/methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:if>

        <!-- Begin Data Entity Descriptions -->

        <!-- Data Table -->
        <xsl:for-each select="/eml:eml/dataset/dataTable">
          <fieldset>
            <xsl:element name="legend">
              <xsl:attribute name="id"><xsl:value-of select="./entityName"/></xsl:attribute>
              <xsl:value-of select="./entityName"/> ( <a href="#top">top</a> ) </xsl:element>
            <div class="section-table">
              <table>
                <tbody>
                  <tr>
                    <td class="header" colspan="2">Physical</td>
                  </tr>
                  <tr>
                    <td class="title">Type:</td>
                    <td class="data">Data Table</td>
                  </tr>
                  <tr>
                    <td class="title">Description:</td>
                    <td class="data">
                      <xsl:value-of select="./entityDescription"/>
                    </td>
                  </tr>
                  <xsl:apply-templates select="./physical"/>
                  <xsl:apply-templates select="./coverage/temporalCoverage" mode="dataset"/>
                  <xsl:apply-templates select="./coverage/geographicCoverage"/>
                  <xsl:apply-templates select="./methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:for-each>

        <!-- Spatial Raster -->
        <xsl:for-each select="/eml:eml/dataset/spatialRaster">
          <fieldset>
            <xsl:element name="legend">
              <xsl:attribute name="id"><xsl:value-of select="./entityName"/></xsl:attribute>
              <xsl:value-of select="./entityName"/> ( <a href="#top">top</a> ) </xsl:element>
            <div class="section-table">
              <table>
                <tbody>
                  <tr>
                    <td class="header" colspan="2">Physical</td>
                  </tr>
                  <tr>
                    <td class="title">Type:</td>
                    <td class="data">Spatial Raster</td>
                  </tr>
                  <tr>
                    <td class="title">Description:</td>
                    <td class="data">
                      <xsl:value-of select="./entityDescription"/>
                    </td>
                  </tr>
                  <xsl:apply-templates select="./physical"/>
                  <xsl:apply-templates select="./coverage/temporalCoverage" mode="dataset"/>
                  <xsl:apply-templates select="./coverage/geographicCoverage"/>
                  <xsl:apply-templates select="./methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:for-each>

        <!-- Spatial Vector -->
        <xsl:for-each select="/eml:eml/dataset/spatialVector">
          <fieldset>
            <xsl:element name="legend">
              <xsl:attribute name="id"><xsl:value-of select="./entityName"/></xsl:attribute>
              <xsl:value-of select="./entityName"/> ( <a href="#top">top</a> ) </xsl:element>
            <div class="section-table">
              <table>
                <tbody>
                  <tr>
                    <td class="header" colspan="2">Physical</td>
                  </tr>
                  <tr>
                    <td class="title">Type:</td>
                    <td class="data">Spatial Vector</td>
                  </tr>
                  <tr>
                    <td class="title">Description:</td>
                    <td class="data">
                      <xsl:value-of select="./entityDescription"/>
                    </td>
                  </tr>
                  <xsl:apply-templates select="./physical"/>
                  <xsl:apply-templates select="./coverage/temporalCoverage" mode="dataset"/>
                  <xsl:apply-templates select="./coverage/geographicCoverage"/>
                  <xsl:apply-templates select="./methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:for-each>

        <!-- Stored Procedure -->
        <xsl:for-each select="/eml:eml/dataset/storedProcedure">
          <fieldset>
            <xsl:element name="legend">
              <xsl:attribute name="id"><xsl:value-of select="./entityName"/></xsl:attribute>
              <xsl:value-of select="./entityName"/> ( <a href="#top">top</a> ) </xsl:element>
            <div class="section-table">
              <table>
                <tbody>
                  <tr>
                    <td class="header" colspan="2">Physical</td>
                  </tr>
                  <tr>
                    <td class="title">Type:</td>
                    <td class="data">Stored Procedure</td>
                  </tr>
                  <tr>
                    <td class="title">Description:</td>
                    <td class="data">
                      <xsl:value-of select="./entityDescription"/>
                    </td>
                  </tr>
                  <xsl:apply-templates select="./physical"/>
                  <xsl:apply-templates select="./coverage/temporalCoverage" mode="dataset"/>
                  <xsl:apply-templates select="./coverage/geographicCoverage"/>
                  <xsl:apply-templates select="./methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:for-each>

        <!-- View -->
        <xsl:for-each select="/eml:eml/dataset/view">
          <fieldset>
            <xsl:element name="legend">
              <xsl:attribute name="id"><xsl:value-of select="./entityName"/></xsl:attribute>
              <xsl:value-of select="./entityName"/> ( <a href="#top">top</a> )i </xsl:element>
            <div class="section-table">
              <table>
                <tbody>
                  <tr>
                    <td class="header" colspan="2">Physical</td>
                  </tr>
                  <tr>
                    <td class="title">Type:</td>
                    <td class="data">View</td>
                  </tr>
                  <tr>
                    <td class="title">Description:</td>
                    <td class="data">
                      <xsl:value-of select="./entityDescription"/>
                    </td>
                  </tr>
                  <xsl:apply-templates select="./physical"/>
                  <xsl:apply-templates select="./coverage/temporalCoverage" mode="dataset"/>
                  <xsl:apply-templates select="./coverage/geographicCoverage"/>
                  <xsl:apply-templates select="./methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:for-each>

        <!-- Other Entity -->
        <xsl:for-each select="/eml:eml/dataset/otherEntity">
          <fieldset>
            <xsl:element name="legend">
              <xsl:attribute name="id"><xsl:value-of select="./entityName"/></xsl:attribute>
              <xsl:value-of select="./entityName"/> ( <a href="#top">top</a> ) </xsl:element>
            <div class="section-table">
              <table>
                <tbody>
                  <tr>
                    <td class="header" colspan="2">Physical</td>
                  </tr>
                  <tr>
                    <td class="title">Type:</td>
                    <td class="data">Other Entity</td>
                  </tr>
                  <tr>
                    <td class="title">Description:</td>
                    <td class="data">
                      <xsl:value-of select="./entityDescription"/>
                    </td>
                  </tr>
                  <xsl:apply-templates select="./physical"/>
                  <xsl:apply-templates select="./coverage/temporalCoverage" mode="dataset"/>
                  <xsl:apply-templates select="./coverage/geographicCoverage"/>
                  <xsl:apply-templates select="./methods"/>
                </tbody>
              </table>
            </div>
          </fieldset>
        </xsl:for-each>

        <p> To view this document as XML, click <a
            href="./metadataviewer?packageid={$packageid}&#38;contentType=application/xml"
            target="_blank">here</a>. </p>

      </div>
      <!-- end of collapsible -->
    </div>
    <!-- end of eml -->

    <script type="text/javascript">

					jQuery(document).ready(function() {
					jQuery(".collapsible").hide();
					jQuery("#toggle").click(function()
					{
					jQuery(this).next(".collapsible").slideToggle("fast");
					});
					});

					jQuery("#show").click(function () {
					jQuery(".collapsible").show("fast"); });
					jQuery("#hide").click(function () {
					jQuery(".collapsible").hide("fast"); });

					function
					submitform(form_ref) {
					form_ref.submit();
					}

				</script>
				
</body>

</html>

  </xsl:template>

  <!-- begin template definitions -->

  <xsl:template match="abstract">
    <tr>
      <td class="title">Abstract:</td>
      <td class="data">
        <xsl:choose>
          <xsl:when test="boolean(./section) or boolean(./para)">
            <xsl:apply-templates select="./section"/>
            <xsl:apply-templates select="./para"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="."/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="individualName" mode="summary">
    <tr>
      <td class="title">Creator:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="organizationName" mode="summary">
    <tr>
      <td class="title">Creator:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="temporalCoverage" mode="summary">
    <tr>
      <td class="title">Temporal Coverage:</td>
      <td class="data">
        <xsl:if test="./singleDateTime">
          <xsl:value-of select="./singleDateTime"/>
        </xsl:if>
        <xsl:if test="./rangeOfDates">
          <xsl:value-of select="./rangeOfDates/beginDate"/> to <xsl:value-of
            select="./rangeOfDates/endDate"/>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="temporalCoverage" mode="dataset">
    <tr>
      <td class="header" colspan="2">Temporal Coverage</td>
    </tr>
    <xsl:choose>
      <xsl:when test="./singleDateTime">
        <tr>
          <td class="title">Singe Date/Time:</td>
          <td class="data">
            <xsl:value-of select="./singleDateTime"/>
          </td>
        </tr>
      </xsl:when>
      <xsl:when test="./rangeOfDates">
        <tr>
          <td class="title">Begin Date/Time:</td>
          <td class="data">
            <xsl:value-of select="./rangeOfDates/beginDate"/>
          </td>
        </tr>
        <tr>
          <td class="title">End Date/Time:</td>
          <td class="data">
            <xsl:value-of select="./rangeOfDates/endDate"/>
          </td>
        </tr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="geographicCoverage">
    <tr>
      <td class="header" colspan="2">Geographic Coverage</td>
    </tr>
    <xsl:apply-templates select="geographicDescription"/>
    <xsl:apply-templates select="boundingCoordinates"/>
  </xsl:template>

  <xsl:template match="geographicDescription">
    <tr>
      <td class="title">Geographic Description:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="boundingCoordinates">
    <tr>
      <td class="title">Bounding Coordinates:</td>
      <td class="data">
        <xsl:value-of select="./westBoundingCoordinate"/> W, <xsl:value-of
          select="./eastBoundingCoordinate"/> E, <xsl:value-of select="./northBoundingCoordinate"/>
        N, <xsl:value-of select="./southBoundingCoordinate"/> S </td>
    </tr>
    <xsl:if test="./boundingAltitudes">
      <tr>
        <td class="title">Bounding Altitudes:</td>
        <td class="data">
          <xsl:value-of select="./boundingAltitudes/altitudeMinimum"/> to <xsl:value-of
            select="./boundingAltitudes/altitudeMaximum"/> &#160; <xsl:value-of
            select="./boundingAltitudes/altitudeUnits"/>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="size">
    <xsl:value-of select="."/> &#160; <xsl:if test="@unit">
      <xsl:value-of select="@unit"/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="physical">
    <!-- Determine parent node value -->
    <xsl:variable name="data-type" select="parent::node()"/>
    <tr>
      <td class="title">Object Name:</td>
      <td class="data">
        <xsl:value-of select="./objectName"/>
      </td>
    </tr>
    <xsl:if test="./size">
      <tr>
        <td class="title">Size:</td>
        <td class="data">
          <xsl:apply-templates select="./size"/>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="not(./distribution/online/url/@function = 'information')">
      <tr>
        <td class="title">Download URI:</td>
        <td class="data">
          <xsl:value-of select="./distribution/online/url"/>
        </td>
      </tr>
    </xsl:if>
    <!-- Data type specific information -->
    <xsl:choose>
      <xsl:when test="local-name($data-type) = 'dataTable'">
        <tr>
          <td class="title">Table Information:</td>
          <td class="data"> This location will hold a link to detailed entity information. </td>
        </tr>
      </xsl:when>
      <xsl:when test="local-name($data-type) = 'spatialRaster'">
        <tr>
          <td class="title">Spatial Raster Information:</td>
          <td class="data"> This location will hold a link to detailed entity information. </td>
        </tr>
      </xsl:when>
      <xsl:when test="local-name($data-type) = 'spatialVector'">
        <tr>
          <td class="title">Spatial Vector Information:</td>
          <td class="data"> This location will hold a link to detailed entity information. </td>
        </tr>
      </xsl:when>
      <xsl:when test="local-name($data-type) = 'storedProcedure'">
        <tr>
          <td class="title">Stored Procedure Information:</td>
          <td class="data"> This location will hold a link to detailed entity information. </td>
        </tr>
      </xsl:when>
      <xsl:when test="local-name($data-type) = 'view'">
        <tr>
          <td class="title">View Information:</td>
          <td class="data"> This location will hold a link to detailed entity information. </td>
        </tr>
      </xsl:when>
      <xsl:when test="local-name($data-type) = 'otherEntity'">
        <tr>
          <td class="title">Other Entity Type:</td>
          <td class="data">
            <xsl:value-of select="$data-type/entityType"/>
          </td>
        </tr>
      </xsl:when>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="methods">
    <tr>
      <td class="header" colspan="2">Methods</td>
    </tr>
    <xsl:for-each select="./methodStep">
      <tr>
        <td class="title"> Method Step <xsl:value-of select="position()"/> : </td>
        <td class="data">
          <xsl:choose>
            <xsl:when test="./description/para/literalLayout[1] = $prov-stmt">
              <xsl:variable name="url" select="dataSource/distribution/online/url"/>
              <p>
                <b>The following data package was used in the creation of this product:</b>
              </p>
              <p><xsl:value-of select="./dataSource/title"/> (<a href="./metadataviewer?url={$url}"
                  >Click here to view metadata</a>)</p>
            </xsl:when>
            <xsl:when test="./description/para[1] = $prov-stmt">
              <xsl:variable name="url" select="dataSource/distribution/online/url"/>
              <p>
                <b>The following data package was used in the creation of this product:</b>
              </p>
              <p><xsl:value-of select="./dataSource/title"/> (<a href="./metadataviewer?url={$url}"
                >Click here to view metadata</a>)</p>
            </xsl:when>
            <xsl:otherwise>
              <xsl:apply-templates select="./description"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="description">
    <xsl:choose>
      <xsl:when test="boolean(./section) or boolean(./para)">
        <xsl:apply-templates select="./section"/>
        <xsl:apply-templates select="./para"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="."/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="section">
    <xsl:if test="./title">
      <p>
        <xsl:value-of select="./title"/>
      </p>
    </xsl:if>
    <xsl:for-each select="./para">
      <p>
        <xsl:value-of select="."/>
      </p>
    </xsl:for-each>
    <xsl:for-each select="./section">
      <xsl:apply-templates select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="para">
    <p>
      <xsl:value-of select="."/>
    </p>
  </xsl:template>

  <xsl:template match="creator" mode="org">
    <tr>
      <td class="header" colspan="2">Creator</td>
    </tr>
    <xsl:apply-templates select="./individualName" mode="org"/>
    <xsl:apply-templates select="./organizationName" mode="org"/>
    <xsl:apply-templates select="./positionName" mode="org"/>
    <xsl:apply-templates select="./address" mode="org"/>
    <xsl:apply-templates select="./phone" mode="org"/>
    <xsl:apply-templates select="./electronicMailAddress" mode="org"/>
    <xsl:apply-templates select="./onlineUrl" mode="org"/>
    <xsl:apply-templates select="./userId" mode="org"/>
  </xsl:template>

  <xsl:template match="contact" mode="org">
    <tr>
      <td class="header" colspan="2">Contact</td>
    </tr>
    <xsl:apply-templates select="./individualName" mode="org"/>
    <xsl:apply-templates select="./organizationName" mode="org"/>
    <xsl:apply-templates select="./positionName" mode="org"/>
    <xsl:apply-templates select="./address" mode="org"/>
    <xsl:apply-templates select="./phone" mode="org"/>
    <xsl:apply-templates select="./electronicMailAddress" mode="org"/>
    <xsl:apply-templates select="./onlineUrl" mode="org"/>
    <xsl:apply-templates select="./userId" mode="org"/>
  </xsl:template>

  <xsl:template match="metadataProvider" mode="org">
    <tr>
      <td class="header" colspan="2">Metadata Provider</td>
    </tr>
    <xsl:apply-templates select="./individualName" mode="org"/>
    <xsl:apply-templates select="./organizationName" mode="org"/>
    <xsl:apply-templates select="./positionName" mode="org"/>
    <xsl:apply-templates select="./address" mode="org"/>
    <xsl:apply-templates select="./phone" mode="org"/>
    <xsl:apply-templates select="./electronicMailAddress" mode="org"/>
    <xsl:apply-templates select="./onlineUrl" mode="org"/>
    <xsl:apply-templates select="./userId" mode="org"/>
  </xsl:template>

  <xsl:template match="publisher" mode="org">
    <tr>
      <td class="header" colspan="2">Publisher</td>
    </tr>
    <xsl:apply-templates select="./individualName" mode="org"/>
    <xsl:apply-templates select="./organizationName" mode="org"/>
    <xsl:apply-templates select="./positionName" mode="org"/>
    <xsl:apply-templates select="./address" mode="org"/>
    <xsl:apply-templates select="./phone" mode="org"/>
    <xsl:apply-templates select="./electronicMailAddress" mode="org"/>
    <xsl:apply-templates select="./onlineUrl" mode="org"/>
    <xsl:apply-templates select="./userId" mode="org"/>
    <xsl:if test="/eml:eml/dataset/pubDate">
      <tr>
        <td class="title">Publication Date:</td>
        <td class="data">
          <xsl:value-of select="/eml:eml/dataset/pubDate"/>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="/eml:eml/dataset/pubPlace">
      <tr>
        <td class="title">Place of Publication:</td>
        <td class="data">
          <xsl:value-of select="/eml:eml/dataset/pubPlace"/>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="individualName" mode="org">
    <tr>
      <td class="title">Individual Name:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="organizationName" mode="org">
    <tr>
      <td class="title">Organization Name:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="positionName" mode="org">
    <tr>
      <td class="title">Position Name:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="address" mode="org">
    <tr>
      <td class="title">Address:</td>
      <td class="data">
        <xsl:for-each select="./deliveryPoint">
          <xsl:value-of select="."/>
          <br/>
        </xsl:for-each>
        <xsl:if test="./city">
          <xsl:value-of select="./city"/>
          <br/>
        </xsl:if>
        <xsl:if test="./administrativeArea">
          <xsl:value-of select="./administrativeArea"/>
          <br/>
        </xsl:if>
        <xsl:if test="./postalCode">
          <xsl:value-of select="./postalCode"/>
          <br/>
        </xsl:if>
        <xsl:if test="./country">
          <xsl:value-of select="./country"/>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="phone" mode="org">
    <tr>
      <td class="title"> Phone - <xsl:choose>
          <xsl:when test="./@phonetype"> ( <xsl:value-of select="./@phonetype"/> ): </xsl:when>
          <xsl:otherwise> (voice): </xsl:otherwise>
        </xsl:choose>
      </td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="electronicMailAddress" mode="org">
    <tr>
      <td class="title">Email:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="onlineUrl" mode="org">
    <tr>
      <td class="title">Online URL:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="userId" mode="org">
    <tr>
      <td class="title">User Id:</td>
      <td class="data">
        <xsl:value-of select="."/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
