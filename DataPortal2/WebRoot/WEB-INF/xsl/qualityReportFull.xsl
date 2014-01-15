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
                    <td class="header" align="center" width="180px"> Quality Check </td>
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
                        <td class="header" align="center" width="180px"> Quality Check </td>
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
                            <xsl:apply-templates select="qr:found"/>
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
        <td class="data" align="center" valign="top" title="identifier">
            <xsl:value-of select="."/>
        </td>
    </xsl:template>

    <xsl:template match="qr:status">
        <td class="data" align="center" valign="top" title="status">
            <xsl:variable name="status" select="."/>
            <!-- color code status value -->
            <xsl:if test="$status = 'info'">
                <div class="info">info</div>
            </xsl:if>
            <xsl:if test="$status = 'valid'">
                <div class="valid">valid</div>
            </xsl:if>
            <xsl:if test="$status = 'warn'">
                <div class="warn">warn</div>
            </xsl:if>
            <xsl:if test="$status = 'error'">
                <div class="error">error</div>
            </xsl:if>
        </td>
    </xsl:template>

    <xsl:template match="qr:qualityCheck">
        <td class="data" align="left" valign="top" title="qualitycheck">
        <pre> Type: <xsl:value-of select="@qualityType"/>
 System: <xsl:value-of select="@system"/>
 On Failure: <xsl:value-of select="@statusType"/></pre>
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
        <td class="data" align="left" valign="top" title="found">
            <xsl:variable name="found" select="."/>
            <xsl:if test="string-length($found) &gt; 200">
                <xsl:element name="a">
                    <xsl:attribute name="href">#</xsl:attribute>
                    <!-- flatten white space to single space character -->
                    <xsl:attribute name="onclick">confirm("<xsl:value-of
                            select="normalize-space($found)"/>");</xsl:attribute> click here to view
                    content </xsl:element>
            </xsl:if>
            <xsl:if test="string-length($found) &lt;= 200">
                <xsl:value-of select="$found"/>
            </xsl:if>
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
