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
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  version="2.0"
>

  <xsl:output method="html"/>

  <xsl:variable name="documentCount">
      <xsl:value-of select="count(/resultset/document)"/>
  </xsl:variable>

  <xsl:template match="/">

      <p>Number of matches: <b><xsl:value-of select="$documentCount"/></b></p>

      <table width="100%">
        <tbody>
          <tr>
            <th class="nis" width="15%">Package Id</th>
            <th class="nis" width="50%">Title</th>
            <th class="nis" width="20%">Creators</th>
            <th class="nis" width="10%">Publication Date</th>
          </tr>
          <xsl:for-each select="/resultset/document">
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </tbody>
      </table>
  </xsl:template>

  <xsl:template match="document">
  
	<xsl:variable name="pid" select="./packageId"/>
	
    <tr>
      <td class="nis" align="center">
        <a class="searchsubcat" href="./mapbrowse?packageid={$pid}">
        <xsl:value-of select="$pid"/>
        </a>
      </td>
      <td class="nis" align="left">
        <xsl:value-of select="title"/>
      </td>
      <td class="nis" align="center">
        <xsl:apply-templates select="authors"/>
      </td>
      <td class="nis" align="center">
        <xsl:value-of select="pubDate"/>
      </td>
    </tr>
    
  </xsl:template>

  <xsl:template match="authors">
    <xsl:for-each select="author">
      <xsl:value-of select="." /><br/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
