<?xml version="1.0" encoding="UTF-8"?>

<!-- 
  $Date: 2012-05-03 19:43:59 -0700 (Thu, 03 May 2012) $
  $Author: mservilla	$
  $Revision: 2120 $
  
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

  <xsl:template match="/">

    <div class="section-table">
      <fieldset>
        <legend>PASTA Audit Report</legend>
        <table>
          <tbody>
            <tr>
              <td class="header">Date/Time</td>
              <td class="header">Method</td>
              <td class="header">Resource</td>
              <td class="header">User</td>
              <td class="header">Group(s)</td>
            </tr>
            <xsl:for-each select="/auditReport/auditRecord">
              <xsl:sort select="./oid" data-type="number" />
              <tr>
                <xsl:apply-templates select="./entryTime" />
                <xsl:apply-templates select="./serviceMethod" />
                <xsl:apply-templates select="./resourceId" />
                <xsl:apply-templates select="./user" />
                <xsl:apply-templates select="./groups" />
              </tr>
            </xsl:for-each>
          </tbody>
        </table>
      </fieldset>
    </div>
    <!-- section-table -->

  </xsl:template>
  
  <xsl:template match="entryTime">
    <td class="data">
      <xsl:value-of select="."/>
    </td>
  </xsl:template>
  
  <xsl:template match="serviceMethod">
    <td class="data">
      <xsl:value-of select="."/>
    </td>
  </xsl:template>
  
  <xsl:template match="resourceId">
    <td class="data">
      <xsl:value-of select="."/>
    </td>
  </xsl:template>
  
  <xsl:template match="user">
    <td class="data">
      <xsl:value-of select="."/>
    </td>
  </xsl:template>
  
  <xsl:template match="groups">
    <td class="data">
      <xsl:value-of select="."/>
    </td>
  </xsl:template>
  
</xsl:stylesheet>
