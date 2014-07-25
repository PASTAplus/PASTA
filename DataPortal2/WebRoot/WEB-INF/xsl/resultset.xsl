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

  <xsl:param name="includeEcotrends"></xsl:param>
  <xsl:param name="includeLandsat5"></xsl:param>
  
  <xsl:variable name="documentCount">
      <xsl:value-of select="count(/resultset/document)"/>
  </xsl:variable>

  <xsl:variable name="ecotrendsCount" >
  	<xsl:value-of select="count(/resultset/document/packageId[@scope = 'ecotrends'])" />
  </xsl:variable>

  <xsl:variable name="landsat5Count" >
  	<xsl:value-of select="count(/resultset/document/packageId[starts-with(@scope, 'lter-landsat')])" />
  </xsl:variable>

  <xsl:template match="/">

  <xsl:choose>
    <xsl:when test="(($includeEcotrends = 'true') and ($includeLandsat5 = 'true'))">
		<xsl:variable name="results" >
  			<xsl:value-of select="$documentCount" />
  		</xsl:variable>
        <p>Number of results displayed: <b><xsl:value-of select="$results"/></b></p>
    </xsl:when>
    <xsl:when test="$includeEcotrends = 'true'">
		<xsl:variable name="results" >
  			<xsl:value-of select="$documentCount - $landsat5Count" />
  		</xsl:variable>
        <p>Number of results displayed: <b><xsl:value-of select="$results"/></b></p>
    </xsl:when>
    <xsl:when test="$includeLandsat5 = 'true'">
		<xsl:variable name="results" >
  			<xsl:value-of select="$documentCount - $ecotrendsCount" />
  		</xsl:variable>
        <p>Number of results displayed: <b><xsl:value-of select="$results"/></b></p>
    </xsl:when>
    <xsl:otherwise>
		<xsl:variable name="results" >
  			<xsl:value-of select="$documentCount - $landsat5Count - $ecotrendsCount" />
  		</xsl:variable>
        <p>Number of matches: <b><xsl:value-of select="$results"/></b></p>
    </xsl:otherwise>    
  </xsl:choose>

<!-- for debugging
    <p>Number of documents matched: <b><xsl:value-of select="$documentCount"/></b></p>
    <p>Number of ecotrends: <b><xsl:value-of select="$ecotrendsCount"/></b></p>
    <p>Number of landsat5: <b><xsl:value-of select="$landsat5Count"/></b></p>
-->

      <table width="100%">
        <tbody>
          <tr>
            <th class="nis" width="15%">Package Id</th>
            <th class="nis" width="10%">Creators</th>
            <th class="nis" width="10%">Publication Date</th>
            <th class="nis" width="65%">Title</th>
          </tr>
          <xsl:for-each select="/resultset/document">
            <xsl:sort select="./packageId/@scope" data-type="text"/>
            <xsl:sort select="./packageId/@identifier" data-type="number"/>
            <xsl:apply-templates select="."/>
          </xsl:for-each>
        </tbody>
      </table>
  </xsl:template>

  <xsl:template match="document">
  
	<xsl:variable name="pid" select="./packageId"/>
	
	<xsl:choose>
    <xsl:when test="(
                      ((not(starts-with($pid, 'ecotrends'))) and (not(starts-with($pid, 'lter-landsat')))) or 
                      (starts-with($pid, 'ecotrends') and ($includeEcotrends = 'true')) or
                      (starts-with($pid, 'lter-landsat') and ($includeLandsat5 = 'true'))
                    )">
      
    <tr>
      <td class="nis" align="center">
        <a class="searchsubcat" href="./mapbrowse?packageid={$pid}">
        <xsl:value-of select="$pid"/>
        </a>
      </td>
      <td class="nis" align="center">
        <xsl:apply-templates select="./param" mode="creator"/>
      </td>
      <td class="nis" align="center">
        <xsl:apply-templates select="./param" mode="pubdate"/>
      </td>
      <td class="nis" align="left">
        <xsl:apply-templates select="./param" mode="title"/>
      </td>
    </tr>
    
    </xsl:when>
    <xsl:otherwise></xsl:otherwise>
    </xsl:choose>
    
  </xsl:template>

  <xsl:template match="param" mode="creator">
    <xsl:if test="./@name = 'dataset/creator/individualName/surName'">
      <xsl:value-of select="."/><br/>
    </xsl:if>
  </xsl:template>

  <xsl:template match="param" mode="title">
    <xsl:if test="./@name = 'dataset/title'">
      <xsl:variable name="title" select="."/>
      <xsl:value-of select="$title"/>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="param" mode="pubdate">
    <xsl:if test="./@name = 'dataset/pubDate'">
      <xsl:value-of select="."/>
    </xsl:if>    
  </xsl:template>

</xsl:stylesheet>
