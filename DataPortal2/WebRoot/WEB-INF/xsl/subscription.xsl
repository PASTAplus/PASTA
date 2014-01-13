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

    <div class="section-table">
      <h3 align="center">Subscription(s) for <xsl:choose>
            <xsl:when test="/subscription">
              <xsl:value-of select="/subscription/creator"/>
            </xsl:when>
            <xsl:when test="/subscriptions">
              <xsl:value-of select="/subscriptions/subscription/creator"/>
            </xsl:when>
          </xsl:choose></h3>
        <table>
          <tbody>
            <tr>
              <th class="nis">Subscription Id</th>
              <th class="nis">Package Id</th>
              <th class="nis">Target URL</th>
            </tr>

            <xsl:choose>
              <xsl:when test="/subscription">
                <xsl:apply-templates select="/subscription"/>
              </xsl:when>
              <xsl:when test="/subscriptions">
                <xsl:for-each select="/subscriptions/subscription">
                  <xsl:sort select="./id" data-type="number"/>
                  <xsl:apply-templates select="."/>
                </xsl:for-each>
              </xsl:when>
            </xsl:choose>

          </tbody>
        </table>
    </div>


  </xsl:template>

  <xsl:template match="subscription">
    <tr>
      <td class="nis" align="center">
        <xsl:value-of select="./id"/>
      </td>
      <td class="nis" align="center">
        <xsl:value-of select="./packageId"/>
      </td>
      <td class="nis">
        <xsl:value-of select="./url"/>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>
