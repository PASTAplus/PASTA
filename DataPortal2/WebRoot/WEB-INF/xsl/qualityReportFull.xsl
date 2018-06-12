<?xml version="1.0" encoding="UTF-8"?>
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

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:qr="eml://ecoinformatics.org/qualityReport">

    <xsl:output method="html"/>

    <xsl:template match="/">
      
<head>

<meta charset="UTF-8" />
<meta content="width=device-width, initial-scale=1, maximum-scale=1" name="viewport"/>

<link rel="shortcut icon" href="./images/favicon.ico" type="image/x-icon" />

<!-- Google Fonts CSS -->
<link href="https://fonts.googleapis.com/css?family=Open+Sans:400,300,600,300italic" rel="stylesheet" type="text/css"/>

<!-- Page Layout CSS MUST LOAD BEFORE bootstap.css -->
<link href="css/style_slate.css" media="all" rel="stylesheet" type="text/css"/>

<!-- JS -->
<script src="js/jqueryba3a.js?ver=1.7.2" type="text/javascript"></script>
<script src="bootstrap/js/bootstrap68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.easing.1.368b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.flexslider-min68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/themeple68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.pixel68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/jquery.mobilemenu68b368b3.js?ver=1" type="text/javascript"></script>
<script src="js/mediaelement-and-player.min68b368b3.js?ver=1" type="text/javascript"></script>

<!-- Mobile Device CSS -->
<link href="bootstrap/css/bootstrap.css" media="screen" rel="stylesheet" type="text/css"/>
<link href="bootstrap/css/bootstrap-responsive.css" media="screen" rel="stylesheet" type="text/css"/>

</head>

        <!-- tabular framework for each quality report -->
        <table>
            <tbody>
                <tr>
                    <td class="header" colspan="11" align="center">Data Package Quality Report</td>
                </tr>
                <tr>
                    <td class="header" colspan="11" align="left">PackageId: <xsl:apply-templates
                            select="/qr:qualityReport/qr:packageId"/></td>
                </tr>
                <tr>
                    <td class="header" colspan="11" align="left">Report Date/Time:
                            <xsl:apply-templates select="/qr:qualityReport/qr:creationDate"/></td>
                </tr>
                <tr>
                    <td class="header" colspan="11" align="center">Dataset Report</td>
                </tr>
                <tr>
                    <td class="header" align="center"> # </td>
                    <td class="header" align="center"> Identifier </td>
                    <td class="header" align="center"> Status </td>
                    <td class="header" align="center"> Quality Check </td>
                    <td class="header" align="center"> Name </td>
                    <td class="header" align="center"> Description </td>
                    <td class="header" align="center"> Expected </td>
                    <td class="header" align="center"> Found </td>
                    <td class="header" align="center"> Explanation </td>
                    <td class="header" align="center"> Suggestion </td>
                    <td class="header" align="center"> Reference </td>
                </tr>
                <xsl:for-each select="qr:qualityReport/qr:datasetReport/qr:qualityCheck">
                    <tr>
                        <td class="data" align="center" valign="top" title="position">
                            <xsl:value-of select="position()"/>
                        </td>
                        <xsl:apply-templates select="qr:identifier"/>
                        <xsl:apply-templates select="qr:status"/>
                        <xsl:apply-templates select="."/>
                        <xsl:apply-templates select="qr:name"/>
                        <xsl:apply-templates select="qr:description"/>
                        <xsl:apply-templates select="qr:expected"/>
                        <xsl:apply-templates select="qr:found"/>
                        <xsl:apply-templates select="qr:explanation"/>
                        <xsl:apply-templates select="qr:suggestion"/>
                        <xsl:apply-templates select="qr:reference"/>
                    </tr>
                </xsl:for-each>
                <xsl:for-each select="/qr:qualityReport/qr:entityReport">
                   <xsl:variable name="entityNumber" select="position()"/>
                   <tr>
                        <td class="header" colspan="11" align="center">Entity Report</td>
                    </tr>
                    <tr>
                        <td class="header" colspan="11" align="left">Entity Name:
                                <xsl:apply-templates select="qr:entityName"/>
                        </td>
                    </tr>
                    <tr>
                        <td class="header" colspan="11" align="left">Entity Identifier:
                                <xsl:apply-templates select="qr:entityId"/></td>
                    </tr>
                    <tr>
                        <td class="header" align="center"> # </td>
                        <td class="header" align="center"> Identifier </td>
                        <td class="header" align="center"> Status </td>
                        <td class="header" align="center"> Quality Check </td>
                        <td class="header" align="center"> Name </td>
                        <td class="header" align="center"> Description </td>
                        <td class="header" align="center"> Expected </td>
                        <td class="header" align="center"> Found </td>
                        <td class="header" align="center"> Explanation </td>
                        <td class="header" align="center"> Suggestion </td>
                        <td class="header" align="center"> Reference </td>
                    </tr>
                    <xsl:for-each select="qr:qualityCheck">
                        <tr>
                            <td class="data" align="center" valign="top" title="position">
                                <xsl:value-of select="position()"/>
                            </td>
                            <xsl:apply-templates select="qr:identifier"/>
                            <xsl:apply-templates select="qr:status"/>
                            <xsl:apply-templates select="."/>
                            <xsl:apply-templates select="qr:name"/>
                            <xsl:apply-templates select="qr:description"/>
                            <xsl:apply-templates select="qr:expected"/>
                            <xsl:apply-templates select="qr:found">
                                <xsl:with-param name="entityNumber" select="$entityNumber"></xsl:with-param>
                            </xsl:apply-templates>
                            <xsl:apply-templates select="qr:explanation"/>
                            <xsl:apply-templates select="qr:suggestion"/>
                            <xsl:apply-templates select="qr:reference"/>
                        </tr>
                    </xsl:for-each>
                </xsl:for-each>
            </tbody>
        </table>

    </xsl:template>
  
    <!-- begin template definitions -->

    <xsl:template match="qr:packageId">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="qr:creationDate">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="qr:entityName">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="qr:entityId">
        <xsl:value-of select="."/>
    </xsl:template>

    <xsl:template match="qr:identifier">
        <td class="data identifier" align="center" valign="top" title="identifier">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:status">
        <xsl:variable name="status" select="."/>
        <td class="data {$status}" align="center" valign="top" title="status">
            <!-- color code status value -->
            <xsl:choose>
                <xsl:when test="$status='info'"><span class='info'>info</span></xsl:when>
                <xsl:when test="$status='valid'"><span class='valid'>valid</span></xsl:when>
                <xsl:when test="$status='warn'"><span class='warn'>warn</span></xsl:when>
                <xsl:when test="$status='error'"><span class='error'>error</span></xsl:when>
            </xsl:choose>
        </td>
    </xsl:template>

    <xsl:template match="qr:qualityCheck">
        <td class="data" align="left" valign="top" title="qualitycheck">
        <table class="inner">
          <tr><td>Type:</td><td><xsl:value-of select="@qualityType"/></td></tr> 
          <tr><td>System:</td><td><xsl:value-of select="@system"/></td></tr> 
          <tr><td>On failure:</td><td><xsl:value-of select="@statusType"/></td></tr> 
        </table>
        </td>
    </xsl:template>

    <xsl:template match="qr:name">
        <td class="data" align="left" valign="top" title="name">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:description">
        <td class="data" align="left" valign="top" title="description">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:expected">
        <td class="data" align="left" valign="top" title="expected">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:found">
        <xsl:param name="entityNumber"></xsl:param>
        <td class="data" align="left" valign="top" title="found">
            <xsl:variable name="identifier" select="../qr:identifier"/>
            <xsl:variable name="modalId" select="concat($identifier, $entityNumber)" />
            <xsl:variable name="foundContent" select="."/>
            <xsl:choose>
            <xsl:when test="$identifier = 'headerRowAttributeNames' or string-length($foundContent) &gt; 200">
                <!-- Trigger the modal with a button -->
                <button type="button" 
                        class="btn btn-info btn-lg" 
                        data-toggle="modal" 
                        data-target="#{$modalId}">Click to view content</button>
                <!-- Modal -->
                <div id="{$modalId}" class="modal fade" role="dialog">
                    <div class="modal-dialog">
                        <!-- Modal content-->
                        <div class="modal-content">
                            <div class="modal-header">
                                <h4 class="modal-title">Found for <xsl:value-of select="../qr:identifier"/> check</h4>
                            </div>
                            <div class="modal-body">
                                <pre>
                                <xsl:value-of select="$foundContent"/>
                                </pre>
                            </div>
                            <div class="modal-footer">
                                <button type="button" 
                                        class="btn btn-default" 
                                        data-dismiss="modal">
                                    Close
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </xsl:when>
            <xsl:when test="string-length($foundContent) &lt;= 200">
                <xsl:value-of select="$foundContent"/>
            </xsl:when>
            </xsl:choose>
        </td>
    </xsl:template>

    <xsl:template match="qr:explanation">
        <td class="data" align="left" valign="top" title="explanation">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:suggestion">
        <td class="data" align="left" valign="top" title="suggestion">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:reference">
        <td class="data" align="left" valign="top" title="suggestion">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

</xsl:stylesheet>
