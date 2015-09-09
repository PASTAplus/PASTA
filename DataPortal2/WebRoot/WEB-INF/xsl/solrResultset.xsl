<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  $Date$
  $Author: dcosta $
  $Revision$
  
	Copyright 2011-2015 the University of New Mexico.
	
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

  <xsl:param name="start"></xsl:param>
  <xsl:param name="rows"></xsl:param>
  <xsl:param name="isSavedDataPage"></xsl:param>
  <xsl:param name="savedDataList"></xsl:param>
  <xsl:param name="showSavedData"></xsl:param>

  <xsl:param name="titleSort"></xsl:param>
  <xsl:param name="creatorsSort"></xsl:param>
  <xsl:param name="pubDateSort"></xsl:param>
  <xsl:param name="packageIdSort"></xsl:param>
  
  <xsl:variable name="numFound">
      <xsl:value-of select="number(/resultset/@numFound)"/>
  </xsl:variable>

  <xsl:variable name="packageWord">
      <xsl:choose>
        <xsl:when test="($numFound > 1)">packages</xsl:when>
        <xsl:otherwise>package</xsl:otherwise>
      </xsl:choose>
  </xsl:variable>

  <xsl:variable name="displayCount">
      <xsl:choose>
        <xsl:when test="($numFound > number($rows))">
        	<xsl:value-of select="$rows"></xsl:value-of>
        </xsl:when>
        <xsl:otherwise>
        	<xsl:value-of select="$numFound"></xsl:value-of>
        </xsl:otherwise>
      </xsl:choose>
  </xsl:variable>
  
  <xsl:variable name="savedDataPage" select="boolean($isSavedDataPage = 'true')"></xsl:variable>
  <xsl:variable name="showSaved" select="boolean($showSavedData = 'true')"></xsl:variable>

  <xsl:template match="/">
  	<xsl:for-each select="/resultset/document">
		<xsl:apply-templates select="."/>
	</xsl:for-each>
  </xsl:template>

  <xsl:template match="document">
  
	<xsl:variable name="docid" select="./id"/>
	<xsl:variable name="pid" select="./packageid"/>
	
    <tr>
      <td class="nis" align="left">
        <a class="searchsubcat" href="./mapbrowse?packageid={$pid}">
        <xsl:value-of select="title"/>
        </a>
      </td>
      <td class="nis" align="center">
        <xsl:apply-templates select="organizations"/>
        <xsl:apply-templates select="authors"/>
      </td>
      <td class="nis" align="center">
        <xsl:value-of select="pubdate"/>
      </td>
      <td class="nis" align="center">
        <a class="searchsubcat" href="./mapbrowse?packageid={$pid}">
        <xsl:value-of select="$pid"/>
        </a>
	  </td>
	  <xsl:if test="$showSaved">
			<td id ="td-{$pid}" class="nis" align="center">
				<xsl:choose>
					<xsl:when test="$savedDataPage">
        				<form id="{$pid}" class="form-no-margin" name="savedDataForm" method="post" action="savedDataServlet" >
							<input type="hidden" name="operation" value="unsave"></input>
							<input type="hidden" name="packageId" value="{$pid}"></input>
							<input type="hidden" name="forward" value="savedData"></input>
                            <input type="image" name="submit" src="images/minus_blue_small.png" alt="Remove from your data shelf" title="Remove from your data shelf"/>	
		    				<div><small><em>On shelf</em></small><br/></div>
						</form>
					</xsl:when>
					<xsl:otherwise>
						<xsl:variable name="docidPlusComma" select="concat($docid, ',')"></xsl:variable>
						<xsl:variable name="containsDocid" select="boolean(contains($savedDataList, $docidPlusComma))"></xsl:variable>
						<xsl:choose>
							<xsl:when test="$containsDocid">
       							<form id="{$pid}" class="form-no-margin" name="savedDataForm" method="post" action="savedDataServlet" >
									<input type="hidden" name="operation" value="unsave"></input>
									<input type="hidden" name="packageId" value="{$pid}"></input>
									<input type="hidden" name="forward" value="simpleSearch"></input>
                                    <input type="image" name="submit" src="images/minus_blue_small.png" alt="Remove from your data shelf" title="Remove from your data shelf"/>
		    						<div><small><em>On shelf</em></small><br/></div>
								</form>
							</xsl:when>
							<xsl:otherwise>
       							<form id="{$pid}" class="form-no-margin" name="savedDataForm" method="post" action="savedDataServlet" >
									<input type="hidden" name="operation" value="save"></input>
									<input type="hidden" name="packageId" value="{$pid}"></input>
									<input type="hidden" name="forward" value="simpleSearch"></input>
                                    <input type="image" name="submit" src="images/plus_blue_small.png" alt="Add to your data shelf" title="Add to your data shelf"/>	
		    						<div></div>
                                </form>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
      	</td>
		</xsl:if>
    </tr>
    
  </xsl:template>

  <xsl:template match="authors">
    <xsl:for-each select="author">
      <xsl:value-of select="." /><br/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="organizations">
    <xsl:for-each select="organization">
      <xsl:value-of select="." /><br/>
    </xsl:for-each>
  </xsl:template>

</xsl:stylesheet>
