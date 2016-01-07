<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  $Date$
  $Author: dcosta	$
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

  <xsl:template match="/">
	<xsl:apply-templates select="methodStep"/>
  </xsl:template>

  <xsl:template match="methodStep">
    <h1>Method Step</h1>
	<xsl:apply-templates select="description"/>
	<xsl:apply-templates select="dataSource"/>
  </xsl:template>

  <xsl:template match="description">
    <h2>Description</h2>
	<xsl:apply-templates select="para" />
  </xsl:template>

  <xsl:template match="para">
	<p><xsl:value-of select="."></xsl:value-of></p>
  </xsl:template>

  <xsl:template match="dataSource">
    <h2>Data Source</h2>
  	<xsl:for-each select="title">
	   <xsl:apply-templates select="." />
  	</xsl:for-each>
  	<xsl:for-each select="creator">
	   <xsl:apply-templates select="." />
  	</xsl:for-each>
  	<xsl:for-each select="distribution">
	   <xsl:apply-templates select="." />
  	</xsl:for-each>
  	<xsl:for-each select="contact">
	   <xsl:apply-templates select="." />
  	</xsl:for-each>
  </xsl:template>

  <xsl:template match="title">
    <table>
    <tr>
      <th align="left">Title</th>
    </tr>
    <tr>
      <td><xsl:value-of select="."/></td>
    </tr>
    </table>
  </xsl:template>

  <xsl:template match="distribution">
    <br/>
    <table>
    <tr>
      <th align="left" width="150px">Distribution</th>
      <th></th>
    </tr>
        <xsl:apply-templates select="online"/>
    </table>
  </xsl:template>

  <xsl:template match="online">
    <xsl:apply-templates select="onlineDescription"/>
    <xsl:apply-templates select="url"/>
  </xsl:template>

  <xsl:template match="onlineDescription">
    <tr>
      <td class="labelBold nis-provenance-label">Online Description:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="url">
    <tr>
      <td class="labelBold nis-provenance-label">URL:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="creator">
    <br/>
    <table>
    <tr>
      <th align="left" width="150px">Creator</th>
      <th></th>
    </tr>
    <xsl:apply-templates select="./individualName"/>
    <xsl:apply-templates select="./organizationName"/>
    <xsl:apply-templates select="./positionName"/>
    <xsl:apply-templates select="./address"/>
    <xsl:apply-templates select="./phone"/>
    <xsl:apply-templates select="./electronicMailAddress"/>
    <xsl:apply-templates select="./onlineUrl"/>
    <xsl:apply-templates select="./userId"/>
    </table>
  </xsl:template>

  <xsl:template match="contact">
    <br/>
    <table>
    <tr>
      <th align="left" width="150px">Contact</th>
      <th></th>
    </tr>
    <xsl:apply-templates select="./individualName"/>
    <xsl:apply-templates select="./organizationName"/>
    <xsl:apply-templates select="./positionName"/>
    <xsl:apply-templates select="./address"/>
    <xsl:apply-templates select="./phone"/>
    <xsl:apply-templates select="./electronicMailAddress"/>
    <xsl:apply-templates select="./onlineUrl"/>
    <xsl:apply-templates select="./userId"/>
    </table>
  </xsl:template>

  <xsl:template match="metadataProvider">
    <br/>
    <table>
    <tr>
      <th align="left" width="150px">Metadata Provider</th>
      <th></th>
    </tr>
    <xsl:apply-templates select="./individualName"/>
    <xsl:apply-templates select="./organizationName"/>
    <xsl:apply-templates select="./positionName"/>
    <xsl:apply-templates select="./address"/>
    <xsl:apply-templates select="./phone"/>
    <xsl:apply-templates select="./electronicMailAddress"/>
    <xsl:apply-templates select="./onlineUrl"/>
    <xsl:apply-templates select="./userId"/>
    </table>
  </xsl:template>

  <xsl:template match="publisher">
    <br/>
    <table>
    <tr>
      <th align="left" width="150px">Publisher</th>
      <th></th>
    </tr>
    <xsl:apply-templates select="./individualName"/>
    <xsl:apply-templates select="./organizationName"/>
    <xsl:apply-templates select="./positionName"/>
    <xsl:apply-templates select="./address"/>
    <xsl:apply-templates select="./phone"/>
    <xsl:apply-templates select="./electronicMailAddress"/>
    <xsl:apply-templates select="./onlineUrl"/>
    <xsl:apply-templates select="./userId"/>
    </table>
  </xsl:template>

  <xsl:template match="individualName">
    <tr>
      <td class="labelBold nis-provenance-label">Individual Name:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="organizationName">
    <tr>
      <td class="labelBold nis-provenance-label">Organization Name:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="positionName">
    <tr>
      <td class="labelBold nis-provenance-label">Position Name:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="address">
    <tr>
      <td class="labelBold nis-provenance-label">Address:</td>
      <td>
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

  <xsl:template match="phone">
    <tr>
      <td class="labelBold nis-provenance-label"> Phone - <xsl:choose>
          <xsl:when test="./@phonetype"> (<xsl:value-of select="./@phonetype"/>): </xsl:when>
          <xsl:otherwise> (voice): </xsl:otherwise>
        </xsl:choose>
      </td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="electronicMailAddress">
    <tr>
      <td class="labelBold nis-provenance-label">Email:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="onlineUrl">
    <tr>
      <td class="labelBold nis-provenance-label">Online URL:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="userId">
    <tr>
      <td class="labelBold nis-provenance-label">User Id:</td>
      <td><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
