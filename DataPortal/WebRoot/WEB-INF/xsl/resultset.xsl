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

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
  
  <xsl:output method="html"/>

  <xsl:template match="/">

    <xsl:variable name="results">
      <xsl:value-of select="count(/resultset/document)"/>
    </xsl:variable>

    <p>Number of results: <b><xsl:value-of select="$results"/></b></p>

    <xsl:if test="$results > 0">
      <table width="100%">
        <tbody>
          <tr>
            <td class="header" width="20%">Package Identifier</td>
            <td class="header" width="80%">Data Package Title</td>
          </tr>
          <xsl:for-each select="/resultset/document">
            <xsl:sort select="./packageId" data-type="text"/>
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </tbody>
      </table>
    </xsl:if>
  </xsl:template>

  <xsl:template match="document">
    <tr>
      <td class="data" align="center">
        <xsl:value-of select="./packageId"/>
      </td>
      <td class="data">
        <xsl:for-each select="./param">
          <xsl:if test="./@name = 'dataset/title'">
            <xsl:variable name="title" select="."/>
            <xsl:variable name="packageId" select="../packageId"/>
            <a href="./mapbrowse?packageid={$packageId}">
              <xsl:value-of select="$title"/>
            </a>
          </xsl:if>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
