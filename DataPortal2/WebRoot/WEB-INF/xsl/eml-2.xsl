<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  $Date$
  $Author: dcosta	$
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
  
  <!-- mob: 2010-03-19 
    note on encoding: These stylesheets use &#160; for a non-breaking space, which is utf-8. 
    So setting encoding to iso-8859 renders these incorrectly. Metacat seems to map between the two
    character sets (ie, no unreadable characters appear when rendered by Metacat, but other 
    transformers do not. Would it be most polite to settle on one encoding?  or is character-set mapping 
    expected of all transformers or applications?
  -->
  
  <!-- *** Output *** -->
  <xsl:output method="html" encoding="utf-8" indent="yes" /> 
  
  <!-- *** Variables *** -->
  <xsl:variable name="packageID" select="*/@packageId"/>
  <xsl:variable name="docid" select="$packageID"/>
  <xsl:variable name="resourcetitle" select="*/dataset/title"/>
  <!-- global variables to store id node set in case to be referenced -->
  <xsl:variable name="ids" select="//*[@id != '']"/>
  <xsl:variable name="prov-stmt" select="'This method step describes provenance-based metadata as specified in the LTER EML Best Practices.'"/>
  
  <!-- *** Parameters ***
       Note that the default values specified below may be overridden by passing parameters to
       the XSLT processor programatically, although the procedure for doing so is vendor-specific.
  -->
  <!-- change debugmessages value to 1 to enable debugging output -->
  <xsl:param name="debugmessages">0</xsl:param>
  <xsl:param name="entitytype"></xsl:param>
  <xsl:param name="entityindex">1</xsl:param>
  <xsl:param name="resourceId"></xsl:param>
  <xsl:param name="dataPackageDOI"></xsl:param>
  <xsl:param name="cgi-prefix"></xsl:param>  
  <!-- To show the links for the Entities in the dataset display module -->
  <xsl:param name="withEntityLinks">1</xsl:param>
  <!-- To show the link for the Original XML in the dataset display module -->
  <xsl:param name="withOriginalXMLLink">1</xsl:param>
  <!-- To show the Attributes table in the entity display -->
  <xsl:param name="withAttributes">1</xsl:param>
  <!-- the index of physical part in entity part-->
  <xsl:param name="physicalindex"/>
  <!-- the index of distribution in physical part  -->
  <xsl:param name="distributionindex"/>
  <!-- the level of distribution -->
  <xsl:param name="distributionlevel"/>
  <!-- the index of attribute in attribute list-->
  <xsl:param name="attributeindex"/>
  <!-- attribute set to get rid of cell spacing-->
  <xsl:attribute-set name="cellspacing">
    <xsl:attribute name="cellpadding">0</xsl:attribute>
    <xsl:attribute name="cellspacing">0</xsl:attribute>
  </xsl:attribute-set>
  <!-- URL for the app which supplies the xslt with their parameters  -->
  <xsl:param name="useridDirectoryApp1_URI"><xsl:value-of select="$cgi-prefix" /><![CDATA[/ldapweb2012.cgi?stage=showindividual&lter_id=]]></xsl:param>
  <xsl:param name="useridDirectory1">sbclter-directory</xsl:param>
  <xsl:param name="useridDirectoryLabel1">SBC LTER</xsl:param>
  <!--
    *   Most of the html pages are currently laid out as a 2-column table, with
    *   highlights for more-major rows containing subsection titles etc.
    *   The following parameters are used within the
    *           <td width="whateverWidth" class="whateverClass">
    *   tags to define the column widths and (css) styles.
    *   The values of the "xxxColWidth" parameters can be percentages (need to
    *   include % sign) or pixels (number only). Note that if a width is defined
    *   in the CSS stylesheet (see next paragraph), it will override this local
    *   width setting in browsers newer than NN4
  -->
  <!-- the style for major rows containing subsection titles etc. -->
  <xsl:param name="subHeaderStyle" select="'tablehead'"/>
  <!-- the style for major rows containing links, such as additional metadata, original xml file, etc. -->
  <xsl:param name="linkedHeaderStyle" select="'linkedHeaderStyle'"/>  
  <!-- the width for the first column (but see note above) -->
  <xsl:param name="firstColWidth" select="''"/>
  <!-- the style for the first column -->
  <xsl:param name="firstColStyle" select="'rowodd'"/>
  <!-- the width for the second column (but see note above) -->
  <xsl:param name="secondColWidth" select="''"/>
  <!-- the style for the second column -->
  <xsl:param name="secondColStyle" select="'roweven'"/>
  <!-- the style for the attribute table -->
  <xsl:param name="tableattributeStyle" select="'tableattribute'"/>
  <!-- the style for the even col in attributes table -->
  <xsl:param name="colevenStyle" select="'coleven'"/>
  <!-- the style for the inner even col in attributes table -->
  <xsl:param name="innercolevenStyle" select="'innercoleven'"/>
  <!-- the style for the odd col in attributes table -->
  <xsl:param name="coloddStyle" select="'colodd'"/>
  <!-- the style for the inner odd col in attributes table -->
  <xsl:param name="innercoloddStyle" select="'innercolodd'"/>
  <!-- the default style for all other tables -->
  <xsl:param name="tabledefaultStyle" select="'subGroup onehundred_percent'"/>
  <!-- the style for table party -->
  <xsl:param name="tablepartyStyle" select="'tableparty'"/> 
  <!-- Some html pages use a nested table in the second column.
       Some of these nested tables set their first column to the following width: -->
  <xsl:param name="secondColIndent" select="'10%'"/>
  <!-- the first column width of attribute table-->
  <xsl:param name="attributefirstColWidth" select="'15%'"/>
  
  <xsl:template match="/">
    <xsl:param name="docid" select="$docid"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: /</xsl:text></xsl:message></xsl:if>
    <!-- HTML5 DOCTYPE declaration -->
    <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>&#x0A;</xsl:text>
        <!-- begin the content area -->
        <xsl:element name="div">
          <xsl:apply-templates select="*[local-name()='eml']"/>              
        </xsl:element> <!-- closes the div element around the page. -->      
        <!-- mob 2010-03-24 mob added to catch error msgs. -->
        <div> 
          <xsl:apply-templates select="error"/>
        </div>
        <!-- end the content area -->
        <xsl:text>&#x0A;</xsl:text> 
        <xsl:text>&#x0A;</xsl:text>
  </xsl:template>
  
  <xsl:template match="error">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: error</xsl:text></xsl:message></xsl:if>
    <xsl:value-of select="."/>
  </xsl:template>

  <xsl:template match="*[local-name()='eml']">
    <!-- hang onto first title to pass to child pages -->
    <xsl:param name="resourcetitle"><xsl:value-of select="*/title"/> </xsl:param> 
    <xsl:param name="packageID"><xsl:value-of select="@packageId"/></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: *[local-name()='eml']</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="dataset">
      <xsl:call-template name="emldataset">
        <xsl:with-param name="resourcetitle" select="$resourcetitle"></xsl:with-param>
        <xsl:with-param name="packageID" select="$packageID"></xsl:with-param>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="citation">
      <xsl:call-template name="emlcitation"/>
    </xsl:for-each>
    <xsl:for-each select="software">
      <xsl:call-template name="emlsoftware"/>
    </xsl:for-each>
    <xsl:for-each select="protocol">
      <xsl:call-template name="emlprotocol"/>
    </xsl:for-each>
    <fieldset>
      <legend>Other Metadata</legend>
      <xsl:for-each select="additionalMetadata">
        <xsl:call-template name="additionalmetadata"/>
      </xsl:for-each> 
    </fieldset>
    <xsl:if test="$withOriginalXMLLink='1'">
      <xsl:call-template name="xml"/>
    </xsl:if>
  </xsl:template>

  <!-- dataset part -->
  <xsl:template name="emldataset">
    <xsl:param name="resourcetitle" select="$resourcetitle"/>
    <xsl:param name="entitytype" select="$entitytype"/>
    <xsl:param name="entityindex" select="$entityindex"/> 
    <xsl:param name="packageID"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: emldataset</xsl:text></xsl:message></xsl:if>   
    <xsl:call-template name="datasettitle">
      <xsl:with-param name="packageID" select="$packageID"/> 
    </xsl:call-template>

    <fieldset>
      <legend>Summary Information</legend>
      <xsl:call-template name="datasetpart">
        <xsl:with-param name="packageID" select="$packageID"></xsl:with-param>
      </xsl:call-template>
    </fieldset> 

    <button id="showAll">Show Details</button><button id="hideAll">Hide Details</button>
    <fieldset>
      <legend>Detailed Metadata</legend>

      <h3 id="toggleEntities" class="toggleButton"><button>+/-</button> Data Entities</h3>
      <div class="collapsible">
        <xsl:call-template name="entitypart"/>
      </div> <!-- end collapsible --> 

      <xsl:if test="intellectualRights">
      <h3 id="toggleDataSetUsageRights" class="toggleButton"><button>+/-</button> Data Package Usage Rights</h3>
      <div class="collapsible">
        <!-- add in the intellectual rights info -->
        <table class="subGroup onehundred_percent">  
          <tr>
            <td>
                <xsl:for-each select="intellectualRights">
                  <xsl:call-template name="resourceintellectualRights">
                    <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
            </td>
          </tr>
        </table>
      </div> <!-- end collapsible -->
      </xsl:if>

      <xsl:if test="keywordSet">
      <h3 id="toggleKeywords" class="toggleButton"><button>+/-</button> Keywords</h3>
      <div class="collapsible">
        <!-- the keywords table. -->
          <table class="{$tabledefaultStyle}"> 
            <tr>
              <th colspan="2">By Thesaurus:</th>
            </tr>
                <xsl:for-each select="keywordSet">
                     <tr>
                      <xsl:choose>
                        <xsl:when test="keywordThesaurus"> 
                          <td class="{$firstColStyle}"><xsl:value-of select="keywordThesaurus" /></td>
                        </xsl:when>
                        <xsl:otherwise>      
                          <td class="{$firstColStyle}">(No thesaurus)</td>                 
                        </xsl:otherwise>
                      </xsl:choose>
                      <td class="{$secondColStyle}">
                        <xsl:call-template name="resourcekeywordsAsPara" ></xsl:call-template>
                      </td>
                    </tr>
                </xsl:for-each>
          </table>
      </div> <!-- end collapsible -->
      </xsl:if>

      <h3 id="toggleMethods" class="toggleButton"><button>+/-</button> Methods and Protocols</h3>
      <div class="collapsible">
        <!-- mob added 2010-03-26  -->
        <xsl:call-template name="ifmethods">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="packageID" select="$packageID"/>
        </xsl:call-template>
      </div> <!-- end collapsible -->

      <h3 id="togglePeople" class="toggleButton"><button>+/-</button> People and Organizations</h3>
      <div class="collapsible">
        <!-- Organization/Personnel Information -->
        <!-- mob added 2010-03-26 -->
        <xsl:call-template name="responsiblepartiespart">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="resourcetitle" select="$resourcetitle"/>
          <xsl:with-param name="packageID" select="$packageID"/> 
        </xsl:call-template>
      </div> <!-- end collapsible -->
      <!-- mob added 2010-03-26. this one only used by attribute-level coverage  -->
      <!-- <xsl:if test="$displaymodule='coverage' "> -->     
      <xsl:if test="boolean(0)">
        <xsl:call-template name="coveragepart">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="resourcetitle" select="$resourcetitle"/>
        </xsl:call-template>
      </xsl:if>

      <h3 id="toggleCoverage" class="toggleButton"><button>+/-</button> Temporal, Geographic and Taxonomic Coverage</h3>
      <div class="collapsible">
        <!-- mob added 2010-03-26  -->
        <xsl:call-template name="ifcoverage">
          <xsl:with-param name="packageID" select="$packageID"/>
        </xsl:call-template>
      </div> <!-- end collapsible -->

      <h3 id="toggleMethods" class="toggleButton"><button>+/-</button> Project</h3>
      <div class="collapsible">
         <xsl:call-template name="datasetproject">
         </xsl:call-template>
      </div> <!-- end collapsible -->
      
      <xsl:if test="maintenance">
      <h3 id="toggleMaintenance" class="toggleButton"><button>+/-</button> Maintenance</h3>
      <div class="collapsible">
        <!-- add in the maintenance info -->
        <table class="subGroup onehundred_percent">  
          <tr>
            <td>
                <xsl:for-each select="maintenance">
                  <xsl:call-template name="datasetmaintenance">
                    <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
            </td>
          </tr>
        </table>
      </div> <!-- end collapsible -->
      </xsl:if>

      <xsl:if test="additionalInfo">
      <h3 id="toggleAdditionalInfo" class="toggleButton"><button>+/-</button> Additional Info</h3>
      <div class="collapsible">
        <!-- add in the maintenance info -->
        <table class="subGroup onehundred_percent">  
          <tr>
            <td>
                <xsl:for-each select="additionalInfo">
                  <xsl:call-template name="resourceadditionalInfo">
                    <xsl:with-param name="ressubHeaderStyle" select="$secondColStyle"/>
                    <xsl:with-param name="resfirstColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
            </td>
          </tr>
        </table>
      </div> <!-- end collapsible -->
      </xsl:if>

    </fieldset> 
    <!-- end Detailed Metadata -->

    <!-- <xsl:if test="$displaymodule='attribute'"> -->
    <xsl:if test="boolean(0)">
      <xsl:call-template name="attributepart"/>
    </xsl:if>
    <!-- <xsl:if test="$displaymodule='attributedomain'"> -->
    <xsl:if test="boolean(0)">
      <xsl:call-template name="datasetattributedomain"/>
    </xsl:if>
    <!-- <xsl:if test="$displaymodule='attributecoverage'"> -->
    <xsl:if test="boolean(0)">
      <xsl:call-template name="datasetattributecoverage">
        <xsl:with-param name="entitytype" select="$entitytype"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
      </xsl:call-template>
    </xsl:if>
    <!-- <xsl:if test="$displaymodule='attributemethod'"> -->
    <xsl:if test="boolean(0)">
      <xsl:call-template name="datasetattributemethod"/>
    </xsl:if>
    <!-- <xsl:if test="$displaymodule='inlinedata'"> -->
    <xsl:if test="boolean(0)">
      <xsl:call-template name="emlinlinedata"/>
    </xsl:if>
    <!-- <xsl:if test="$displaymodule='attributedetail'"> -->
    <xsl:if test="boolean(0)">
      <xsl:call-template name="entityparam"/>
    </xsl:if>
  </xsl:template>

  <!-- *************** Data set display *************-->
  <xsl:template name="datasetpart">
    <xsl:param name="packageID"/>  
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetpart</xsl:text></xsl:message></xsl:if>
    <xsl:apply-templates select="." mode="dataset">
      <xsl:with-param name="packageID" select="$packageID"></xsl:with-param> 
    </xsl:apply-templates> 
  </xsl:template>

  <!--************ Entity diplay *****************-->
  <xsl:template name="entitypart">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entitypart</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <tr>
            <td colspan="2"><xsl:call-template name="entityparam"/></td>
          </tr>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td colspan="2"><xsl:call-template name="entityparam"/></td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!--************ Responsible Parties display *****************-->
  <xsl:template name="responsiblepartiespart">
    <xsl:param name="docid" select="$docid"></xsl:param>
    <xsl:param name="resourcetitle" select="$resourcetitle"></xsl:param>
    <xsl:param name="packageID" select="$packageID"/> 
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: responsiblepartiespart</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <xsl:call-template name="responsibleparties">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="resourcetitle" select="$resourcetitle"/>     
            <xsl:with-param name="packageID" select="$packageID"/> 
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="responsibleparties">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="resourcetitle" select="$resourcetitle"/>
          <xsl:with-param name="packageID" select="$packageID"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="responsibleparties">
    <xsl:param name="docid" select="$docid"></xsl:param>
    <xsl:param name="resourcetitle" select="$resourcetitle"></xsl:param>
    <xsl:param name="packageID" select="$packageID"/> 
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: responsibleparties</xsl:text></xsl:message></xsl:if>
    <table class="onehundred_percent">            
      <tr>
        <td>
          <table class="subGroup onehundred_percent">
            <xsl:if test="publisher">
              <th>Publishers:</th>
              <xsl:for-each select="publisher">
                <tr>
                  <td>
                    <xsl:call-template name="party">
                      <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
                      <xsl:with-param name="partysecondColStyle" select="$secondColStyle"/>
                    </xsl:call-template>
                  </td>
                </tr>
              </xsl:for-each>
            </xsl:if>     
            <xsl:if test="creator">
              <th>Creators:</th>
              <xsl:for-each select="creator">
                <tr>
                  <td>
                    <xsl:call-template name="party">
                      <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
                      <xsl:with-param name="partysecondColStyle" select="$secondColStyle"/>
                    </xsl:call-template>
                  </td>
                </tr>
              </xsl:for-each>
            </xsl:if>      
            <xsl:if test="contact">
              <th>Contacts:</th>
              <xsl:for-each select="contact">
                <tr>
                  <td>
                    <xsl:call-template name="party">
                      <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
                      <xsl:with-param name="partysecondColStyle" select="$secondColStyle"/>
                    </xsl:call-template>
                  </td>
                </tr>
              </xsl:for-each>
            </xsl:if>     
            <xsl:if test="associatedParty">
              <th>Associated Parties:</th>
                <xsl:for-each select="associatedParty">
                  <tr>
                    <td>
                      <xsl:call-template name="party">
                        <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
                        <xsl:with-param name="partysecondColStyle" select="$secondColStyle"/>
                      </xsl:call-template>
                    </td>
                </tr>
              </xsl:for-each>
            </xsl:if>     
            <xsl:if test="metadataProvider">
              <th>Metadata Providers:</th>
              <xsl:for-each select="metadataProvider">
                <tr>
                  <td>
                    <xsl:call-template name="party">
                      <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
                      <xsl:with-param name="partysecondColStyle" select="$secondColStyle"/>
                    </xsl:call-template>
                  </td>
                </tr>
              </xsl:for-each>
            </xsl:if>
          </table>
        </td>
      </tr>
    </table> <!-- closes the table wrapping the dataset-menu  -->
  </xsl:template>
  
  <xsl:template name="coveragepart">
    <xsl:param name="docid" select="$docid"></xsl:param>
    <xsl:param name="resourcetitle" select="$resourcetitle"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: coveragepart</xsl:text></xsl:message></xsl:if>
    <h3>Data Package Coverage</h3>
    <!-- add in the coverage info -->
    <table class="subGroup onehundred_percent">  
      <tr>
        <!-- add in the geographic coverage info -->
        <td>
          <xsl:if test="./coverage/geographicCoverage">
            <xsl:for-each select="./coverage/geographicCoverage">
              <xsl:call-template name="geographicCoverage">
                <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if>
        </td>
        <!-- mob 2010-03-24: moved up to general information area -->
        <!-- add in the temporal coverage info
          <td>
          <xsl:if test="./coverage/temporalCoverage">
          <xsl:for-each select="./coverage/temporalCoverage">
          <xsl:call-template name="temporalCoverage">
          <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
          <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
          </xsl:call-template>
          </xsl:for-each>
          </xsl:if>
          </td> -->
      </tr>
      <tr>
        <!-- add in the taxonomic coverage info -->
        <td colspan="2" class="onehundred_percent">
          <xsl:if test="./coverage/taxonomicCoverage">
            <xsl:for-each select="./coverage/taxonomicCoverage">
              <xsl:call-template name="taxonomicCoverage">
                <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if>
        </td>
      </tr>
    </table>
  </xsl:template>
  
  <!-- Template to show comprehensive coverage info from resource, entity and 
       attribute modules. Not from project tree. Added by mob 2010-apr.
  -->
  <xsl:template name="coverageall">
    <xsl:param name="docid" select="$docid"></xsl:param>
    <xsl:param name="resourcetitle" select="$resourcetitle"></xsl:param>
    <xsl:param name="packageID" select="$packageID"/>  
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: coverageall</xsl:text></xsl:message></xsl:if>
    <table>            
      <tr>
        <th colspan="2"><xsl:text>Temporal, Geographic and/or Taxonomic information that applies to all data in this dataset:</xsl:text></th>
      </tr>
      <tr>
        <td>
          <xsl:if test="./coverage">
            <!-- print the type of parent element, and title or description -->
            <xsl:for-each select="./coverage/temporalCoverage">
              <xsl:call-template name="temporalCoverage">
                <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
              </xsl:call-template>
            </xsl:for-each>
            <!-- mob: wrap all the geocov in a table to be treated as a unit. -->
            <xsl:if test="./coverage/geographicCoverage">
              <xsl:for-each select="./coverage/geographicCoverage">
                <xsl:call-template name="geographicCoverage">
                  <xsl:with-param  name="firstColStyle" select="$firstColStyle"/>
                  <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                </xsl:call-template>
              </xsl:for-each>  
              <!-- Disable the google map until we figure out why it's not rendering well -->
              <!--
              <td>          
                <div class="eml_map">
                  <div id="map_canvas" style="width: 400px; height: 300px;"></div>
                </div>    
                <xsl:call-template name="geoCovMap">
                  <xsl:with-param name="currentmodule">coverageall</xsl:with-param>
                </xsl:call-template>
              </td> -->
            </xsl:if>
            <xsl:for-each select="./coverage/taxonomicCoverage">
              <xsl:call-template name="taxonomicCoverage">
                <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if> 
        </td>
      </tr>
    </table>
    <table>
      <tr>
        <td>
          <!-- next comes the entity level coverages. attribute-level stuff under its entity name --> 
          <!--  TO DO: this needs to work for all entity types. choose label based on element name  -->
          <xsl:for-each select="dataTable">
            <xsl:if test="coverage or *//attribute/coverage">
              <h3>
                <xsl:text>Temporal, Geographic and/or Taxonomic information that applies to Data Table: </xsl:text>
                <xsl:value-of select="entityName"/> 
              </h3>
              <xsl:if test="coverage"> <!-- if an entity-level cov tree -->
                <xsl:for-each select="./coverage/temporalCoverage">
                  <xsl:call-template name="temporalCoverage">
                    <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="./coverage/geographicCoverage">
                  <xsl:call-template name="geographicCoverage">
                    <xsl:with-param  name="firstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
                <xsl:for-each select="./coverage/taxonomicCoverage">
                  <xsl:call-template name="taxonomicCoverage">
                    <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
              </xsl:if>      
              <xsl:if test=".//attribute/coverage"> <!-- an attribute descendant has a cov tree -->
                <xsl:for-each select=".//attribute/coverage">
                  <table  class="subGroup">
                    <tr>
                      <th>
                        <!-- create a label for that attribute's coverage info. use the orientation and attr label if it has one -->                 
                        <xsl:choose>
                          <xsl:when test="ancestor::dataTable/*//attributeOrientation ='column' ">
                            <xsl:text>Temporal, Geographic and/or Taxonomic information that applies to the data table column:&#160;</xsl:text>
                          </xsl:when>
                          <xsl:when test="ancestor::dataTable/*//attributeOrientation ='row' ">
                            <xsl:text>Temporal, Geographic and/or Taxonomic information that applies to the data table row:&#160;</xsl:text>
                          </xsl:when>
                        </xsl:choose>
                        <xsl:choose>
                          <xsl:when test="../attributeLabel">
                            <xsl:value-of select="../attributeLabel"/>
                            <xsl:text>&#160;(</xsl:text><xsl:value-of select="../attributeName"/><xsl:text>)</xsl:text>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="../attributeName"/>
                          </xsl:otherwise>
                        </xsl:choose> <!-- end of cov info label  -->
                      </th>
                    </tr>
                    <tr>
                      <td>
                        <xsl:for-each select="temporalCoverage">            
                          <xsl:call-template name="temporalCoverage">
                            <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                            <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                          </xsl:call-template>
                        </xsl:for-each>
                        <xsl:for-each select="geographicCoverage">            
                          <xsl:call-template name="geographicCoverage">
                            <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                            <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                          </xsl:call-template>
                        </xsl:for-each>
                        <xsl:for-each select="taxonomicCoverage">            
                          <xsl:call-template name="taxonomicCoverage">
                            <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                            <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                          </xsl:call-template>
                        </xsl:for-each>
                      </td>
                    </tr>
                  </table>    <!-- closes the table for the attribute -->
                </xsl:for-each> 
              </xsl:if>
            </xsl:if>
          </xsl:for-each> 
        </td>
      </tr>
    </table> <!-- closes the table wrapping the dataset-menu  -->
  </xsl:template>
  
  <!-- 
    template to show comprehensive methods info from resource, entity and attribute modules.
    not from project tree. added by mob 2010-apr
  --> 
  <xsl:template name="ifmethods">
    <xsl:param name="packageID"></xsl:param>
    <xsl:param name="docid"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: ifmethods</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="(//method) or (//methods)">
        <xsl:call-template name="methodsall">
          <xsl:with-param name="docid"/>
          <xsl:with-param name="resourcetitle"/>
          <xsl:with-param name="packageID" select="$packageID"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="nodemissing">
          <xsl:with-param name="nodemissing_message">No methods information available</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="ifcoverage">
    <xsl:param name="packageID"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: ifcoverage</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="(//coverage) or (//coverage)">
        <xsl:call-template name="coverageall">
          <xsl:with-param name="docid"/>
          <xsl:with-param name="resourcetitle"/>
          <xsl:with-param name="packageID" select="$packageID"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="nodemissing">
          <xsl:with-param name="nodemissing_message">No coverage information available</xsl:with-param>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="nodemissing">
    <xsl:param name="nodemissing_message"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: nodemissing</xsl:text></xsl:message></xsl:if>
    <table class="onehundred_percent">            
      <tr>
        <td align="center"><h4><xsl:value-of select="$nodemissing_message"/></h4></td>
      </tr>
    </table>
  </xsl:template>
  
  <xsl:template name="methodsall">
    <xsl:param name="docid" select="$docid"></xsl:param>
    <xsl:param name="resourcetitle" select="$resourcetitle"></xsl:param>
    <xsl:param name="packageID" select="$packageID"/> 
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: methodsall</xsl:text></xsl:message></xsl:if>
    <table  class="onehundred_percent">            
      <tr>
        <td>  
        <xsl:if test="./methods">
          <h4><xsl:text>These methods, instrumentation and/or protocols apply to all data in this dataset:</xsl:text></h4>
          <table class="subGroup onehundred_percent">  
            <tr>
              <td>
                <!-- print the type of parent element, and title or description -->
                <xsl:for-each select="./methods">
                  <xsl:call-template name="datasetmethod">
                    <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
              </td>
            </tr>
          </table>
        </xsl:if> 
        <!-- Next comes the entity level coverages. attribute-level stuff under its entity name -->
        <xsl:for-each select="dataTable | spatialRaster | spatialVector | storedProcedure | view | otherEntity">
             
            <xsl:if test="(./methods) or (*//attribute/methods) or (./method) or (*//attribute/method)">
              <h4>
                <xsl:text>These methods, instrumentation, and/or protocols apply to the</xsl:text>
                  <xsl:choose>
                      <xsl:when test="../dataTable"> data table </xsl:when>
                      <xsl:when test="../spatialRaster"> spatial raster </xsl:when>
                      <xsl:when test="../spatialVector"> spatial vector </xsl:when>
                      <xsl:when test="../storedProcedure"> stored procedure </xsl:when>
                      <xsl:when test="../view"> view </xsl:when>
                      <xsl:when test="../otherEntity"> non-categorized data resource </xsl:when>
                  </xsl:choose>
                  <em><xsl:value-of select="entityName"/></em>
                <xsl:text>:</xsl:text>
              </h4>  
              <xsl:if test="(./method) or (./methods)"> <!-- first find an entity-level methods tree -->
                <!--  this becomes METHODS in eml 2.1 -->
                <xsl:for-each select="method | methods">     
                  <xsl:call-template name="datasetmethod">
                    <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                    <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                  </xsl:call-template>
                </xsl:for-each>
              </xsl:if>
              <xsl:if test="(*//attribute/methods)  or (*//attribute/method)"> <!-- an attribute descendant has a method tree -->  
                <xsl:for-each select="*//attribute/method | *//attribute/methods"> <!-- mob fixed 2011-12-23 - missing 'or' -->
                  <table class="subGroup">
                    <tr>
                      <th>
                        <!-- create a label for that attribute's coverage info. use the orientation and attr label if it has one -->                 
                        <xsl:choose>
                          <xsl:when test="ancestor::dataTable/*//attributeOrientation ='column' ">
                            <xsl:text>These methods, instrumentation and/or protocols apply to the data table column:&#160;</xsl:text>
                          </xsl:when>
                          <xsl:when test="ancestor::dataTable/*//attributeOrientation ='row' ">
                            <xsl:text>These methods, instrumentation and/or protocols apply to the data table row:&#160;</xsl:text>
                          </xsl:when>
                        </xsl:choose>
                        <xsl:choose>
                          <xsl:when test="../attributeLabel">
                            <xsl:value-of select="../attributeLabel"/>
                            <xsl:text>&#160;(</xsl:text><xsl:value-of select="../attributeName"/>
                            <xsl:text>)</xsl:text>
                          </xsl:when>
                          <xsl:otherwise>
                            <xsl:value-of select="../attributeName"/>
                          </xsl:otherwise>
                        </xsl:choose> <!-- end of cov info label  -->
                      </th>
                    </tr>
                    <tr>
                      <td>
                        <xsl:for-each select=".">            
                          <xsl:call-template name="datasetmethod">
                            <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
                            <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
                          </xsl:call-template>
                        </xsl:for-each>      
                      </td>
                    </tr>
                  </table>    <!-- closes the table for the attribute -->
                </xsl:for-each> 
              </xsl:if>
            </xsl:if>
          </xsl:for-each>
        </td>
      </tr>
    </table> <!-- closes the table wrapping the dataset-menu  -->
  </xsl:template>
  
  <!--************ Attribute display *****************-->
  <xsl:template name="attributedetailpart">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributedetailpart</xsl:text></xsl:message></xsl:if>
  </xsl:template>
  
  <xsl:template name="attributepart">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributepart</xsl:text></xsl:message></xsl:if>
    <tr>
      <td><h3>Attributes Description</h3></td>
    </tr>
    <tr>
      <td>
        <!-- find the subtree to process -->
        <xsl:if test="$entitytype='dataTable'">
          <xsl:for-each select="dataTable">
            <xsl:if test="position()=$entityindex">
              <xsl:for-each select="attributeList">
                <xsl:call-template name="attributelist">
                  <xsl:with-param name="docid" select="$docid"/>
                  <xsl:with-param name="entitytype" select="$entitytype"/>
                  <xsl:with-param name="entityindex" select="$entityindex"/>
                </xsl:call-template>
              </xsl:for-each>
            </xsl:if>
          </xsl:for-each>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <!-- Attribute Domain display module -->
  <xsl:template name="datasetattributedomain">
    <!-- 
     these params are used to construct links back and to provide the 
     attribute name or label as a variable
    -->
    <xsl:param name="entityindex" select="$entityindex"/>
    <xsl:param name="entitytype" select="$entitytype"/>
    <xsl:param name="attributeindex" select="$attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetattributedomain</xsl:text></xsl:message></xsl:if>
    <xsl:variable name="attribute_label">
      <xsl:choose>
        <xsl:when test="*/attributeList/attribute[number($attributeindex)]/attributeLabel">
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeLabel"/>
          <xsl:text>&#160;(</xsl:text>
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeName"/>
          <xsl:text>)</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeName"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- begin the display -->
    <tr>
      <td>
        <table class="dataset-entity-part">
          <tr>
            <td class="dataset-entity-part-header">
              <h3>Codes and Definitions for: <xsl:value-of select="$attribute_label"/></h3>
            </td>
          </tr>
        </table>       
      </td>
    </tr>
    <tr>
      <!-- find the subtree to process -->
      <td><xsl:call-template name="entityparam"/></td>
    </tr>
  </xsl:template>

  <!-- Attribute Method display module -->
  <xsl:template name="datasetattributemethod">
    <!-- 
      these params are used to construct links back and to provide the 
      attribute name or label as a variable 
    -->
    <xsl:param name="entityindex" select="$entityindex"/>
    <xsl:param name="entitytype" select="$entitytype"/>
    <xsl:param name="attributeindex" select="$attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetattributemethod</xsl:text></xsl:message></xsl:if>
    <xsl:variable name="attribute_label">
      <xsl:choose>
        <xsl:when test="*/attributeList/attribute[number($attributeindex)]/attributeLabel ">
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeLabel "/>
          <xsl:text>&#160;(</xsl:text>
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeName "/>
          <xsl:text>)</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeName "/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- begin the display -->
    <tr>
      <td>
        <table class="dataset-entity-part">
          <tr>
            <td class="dataset-entity-part-header">
              <h3>Method for Attribute: <xsl:value-of select="$attribute_label"/></h3>
            </td>
          </tr>
        </table>  
      </td>
    </tr>
    <tr>
      <!-- find the subtree to process -->
      <td><xsl:call-template name="entityparam"/></td>
    </tr>
  </xsl:template>

  <!-- Attribute Coverage display module -->
  <xsl:template name="datasetattributecoverage">
    <!-- These params are used to provide the attribute name or label as a variable -->
    <xsl:param name="entityindex" select="$entityindex"/>
    <xsl:param name="entitytype" select="$entitytype"/>
    <xsl:param name="attributeindex" select="$attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetattributecoverage</xsl:text></xsl:message></xsl:if>
    <xsl:variable name="attribute_label">
      <xsl:choose>
        <xsl:when test="*/attributeList/attribute[number($attributeindex)]/attributeLabel ">
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeLabel "/>
          <xsl:text>&#160;(</xsl:text>
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeName "/>
          <xsl:text>)</xsl:text>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="*/attributeList/attribute[number($attributeindex)]/attributeName "/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <!-- begin the display -->
    <tr>
      <td>
        <table class="dataset-entity-part">
          <tr>
            <td class="dataset-entity-part-header">
              <h3>Coverage for Attribute: <xsl:value-of select="$attribute_label"/></h3>
            </td>
          </tr>
        </table>  
      </td>
    </tr>
    <tr>
      <!-- find the subtree to process -->
      <td><xsl:call-template name="entityparam"/></td>
    </tr>
  </xsl:template>

  <xsl:template name="entityparam">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entityparam</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="$entitytype=''">
        <xsl:variable name="dataTableCount" select="0"/>
        <xsl:variable name="spatialRasterCount" select="0"/>
        <xsl:variable name="spatialVectorCount" select="0"/>
        <xsl:variable name="storedProcedureCount" select="0"/>
        <xsl:variable name="viewCount" select="0"/>
        <xsl:variable name="otherEntityCount" select="0"/>
        <xsl:for-each select="dataTable|spatialRaster|spatialVector|storedProcedure|view|otherEntity">
          <xsl:if test="'dataTable' = name()">          
            <xsl:variable name="currentNode" select="."/>          
            <xsl:variable name="dataTableCount">
              <xsl:for-each select="../dataTable">
                <xsl:if test=". = $currentNode">
                  <xsl:value-of select="position()"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>          
            <xsl:if test="boolean(1)">
            <!-- <xsl:if test="position() = number($entityindex)"> -->
              <xsl:choose>
                <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
                <xsl:when test="boolean(0)">
                  <xsl:for-each select="attributeList">
                    <xsl:call-template name="singleattribute">
                      <xsl:with-param name="attributeindex" select="$attributeindex"/>
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="'dataTable'"/>
                      <xsl:with-param name="entityindex" select="$dataTableCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="../.">
                    <xsl:call-template name="chooseentity">
                      <xsl:with-param name="entitytype" select="'dataTable'"/>
                      <xsl:with-param name="entityindex" select="$dataTableCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:if>
          <xsl:if test="'spatialRaster' = name()">
            <xsl:variable name="currentNode" select="."/>
            <xsl:variable name="spatialRasterCount">
              <xsl:for-each select="../spatialRaster">
                <xsl:if test=". = $currentNode">
                  <xsl:value-of select="position()"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:if test="boolean(1)">
            <!-- <xsl:if test="position() = number($entityindex)"> -->
              <xsl:choose>
                <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
                <xsl:when test="boolean(0)">
                  <xsl:for-each select="attributeList">
                    <xsl:call-template name="singleattribute">
                      <xsl:with-param name="attributeindex" select="$attributeindex"/>
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="'spatialRaster'"/>
                      <xsl:with-param name="entityindex" select="$spatialRasterCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="../.">
                    <xsl:call-template name="chooseentity">
                     <xsl:with-param name="entitytype" select="'spatialRaster'"/>
                     <xsl:with-param name="entityindex" select="$spatialRasterCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:if>
          <xsl:if test="'spatialVector' = name()">
            <xsl:variable name="currentNode" select="."/>
            <xsl:variable name="spatialVectorCount">
              <xsl:for-each select="../spatialVector">
                <xsl:if test=". = $currentNode">
                  <xsl:value-of select="position()"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:if test="boolean(1)">
            <!-- <xsl:if test="position() = number($entityindex)"> -->
              <xsl:choose>
                <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
                <xsl:when test="boolean(0)">
                  <xsl:for-each select="attributeList">
                    <xsl:call-template name="singleattribute">
                      <xsl:with-param name="attributeindex" select="$attributeindex"/>
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="'spatialVector'"/>
                      <xsl:with-param name="entityindex" select="$spatialVectorCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="../.">
                    <xsl:call-template name="chooseentity">
                      <xsl:with-param name="entitytype" select="'spatialVector'"/>
                      <xsl:with-param name="entityindex" select="$spatialVectorCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:if>
          <xsl:if test="'storedProcedure' = name()">
            <xsl:variable name="currentNode" select="."/>
            <xsl:variable name="storedProcedureCount">
              <xsl:for-each select="../storedProcedure">
                <xsl:if test=". = $currentNode">
                  <xsl:value-of select="position()"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:if test="boolean(1)">
            <!-- <xsl:if test="position() = number($entityindex)"> -->
              <xsl:choose>
                <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
                <xsl:when test="boolean(0)">
                  <xsl:for-each select="attributeList">
                    <xsl:call-template name="singleattribute">
                      <xsl:with-param name="attributeindex" select="$attributeindex"/>
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="'storedProcedure'"/>
                      <xsl:with-param name="entityindex" select="$storedProcedureCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="../.">
                    <xsl:call-template name="chooseentity">
                      <xsl:with-param name="entitytype" select="'storedProcedure'"/>
                      <xsl:with-param name="entityindex" select="$storedProcedureCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:if>
          <xsl:if test="'view' = name()">
            <xsl:variable name="currentNode" select="."/>
            <xsl:variable name="viewCount">
              <xsl:for-each select="../view">
                <xsl:if test=". = $currentNode">
                  <xsl:value-of select="position()"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:if test="boolean(1)">
            <!-- <xsl:if test="position() = number($entityindex)"> -->
              <xsl:choose>
                <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
                <xsl:when test="boolean(0)">
                  <xsl:for-each select="attributeList">
                    <xsl:call-template name="singleattribute">
                      <xsl:with-param name="attributeindex" select="$attributeindex"/>
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="'view'"/>
                      <xsl:with-param name="entityindex" select="$viewCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="../.">
                    <xsl:call-template name="chooseentity">
                      <xsl:with-param name="entitytype" select="'view'"/>
                      <xsl:with-param name="entityindex" select="$viewCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:if>
          <xsl:if test="'otherEntity' = name()">
            <xsl:variable name="currentNode" select="."/>
            <xsl:variable name="otherEntityCount">
              <xsl:for-each select="../otherEntity">
                <xsl:if test=". = $currentNode">
                  <xsl:value-of select="position()"/>
                </xsl:if>
              </xsl:for-each>
            </xsl:variable>
            <xsl:if test="boolean(1)">
            <!-- <xsl:if test="position() = number($entityindex)"> -->
              <xsl:choose>
                <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
                <xsl:when test="boolean(0)">
                  <xsl:for-each select="attributeList">
                    <xsl:call-template name="singleattribute">
                      <xsl:with-param name="attributeindex" select="$attributeindex"/>
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="'otherEntity'"/>
                      <xsl:with-param name="entityindex" select="$otherEntityCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="../.">
                    <xsl:call-template name="chooseentity">
                      <xsl:with-param name="entitytype" select="'otherEntity'"/>
                      <xsl:with-param name="entityindex" select="$otherEntityCount"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:if>   
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <!-- <xsl:when test="$displaymodule='attributedetail'"> -->
          <xsl:when test="boolean(0)">
            <xsl:for-each select="attributeList">
              <xsl:call-template name="singleattribute">
                <xsl:with-param name="attributeindex" select="$attributeindex"/>
                <xsl:with-param name="docid" select="$docid"/>
                <xsl:with-param name="entitytype" select="$entitytype"/>
                <xsl:with-param name="entityindex" select="$entityindex"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="chooseentity">
              <xsl:with-param name="entitytype" select="$entitytype"/>
              <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="chooseentity" match='dataset'>
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: chooseentity</xsl:text></xsl:message></xsl:if>
    <xsl:if test="$entitytype='dataTable'">
      <xsl:for-each select="dataTable">
        <xsl:if test="position()=$entityindex">
          <xsl:choose>
                     <xsl:when test="references!=''">
                        <xsl:variable name="ref_id" select="references"/>
                        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                          <xsl:for-each select="$references">
                              <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="dataTable">
                                        <xsl:with-param name="datatablefirstColStyle" select="$firstColStyle"/>
                                        <xsl:with-param name="datatablesubHeaderStyle" select="$subHeaderStyle"/>
                                        <xsl:with-param name="docid" select="$docid"/>
                                        <xsl:with-param name="entitytype" select="$entitytype"/>
                                        <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="dataTable">
                                        <xsl:with-param name="datatablefirstColStyle" select="$firstColStyle"/>
                                        <xsl:with-param name="datatablesubHeaderStyle" select="$subHeaderStyle"/>
                                        <xsl:with-param name="docid" select="$docid"/>
                                        <xsl:with-param name="entitytype" select="$entitytype"/>
                                        <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <!-- <xsl:otherwise> -->
                       <!-- </xsl:otherwise> -->
                       </xsl:choose>
                       <xsl:call-template name="chooseattributelist"/>
                   </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="$entitytype='spatialRaster'">
        <xsl:for-each select="spatialRaster">
            <xsl:if test="position()=$entityindex">
                   <xsl:choose>
                     <xsl:when test="references!=''">
                        <xsl:variable name="ref_id" select="references"/>
                        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                          <xsl:for-each select="$references">
                              <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="spatialRaster">
                                        <xsl:with-param name="spatialrasterfirstColStyle" select="$firstColStyle"/>
                                        <xsl:with-param name="spatialrastersubHeaderStyle" select="$subHeaderStyle"/>
                                        <xsl:with-param name="docid" select="$docid"/>
                                        <xsl:with-param name="entitytype" select="$entitytype"/>
                                        <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="spatialRaster">
                                        <xsl:with-param name="spatialrasterfirstColStyle" select="$firstColStyle"/>
                                        <xsl:with-param name="spatialrastersubHeaderStyle" select="$subHeaderStyle"/>
                                        <xsl:with-param name="docid" select="$docid"/>
                                        <xsl:with-param name="entitytype" select="$entitytype"/>
                                        <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                       </xsl:choose>
                   </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="$entitytype='spatialVector'">
        <xsl:for-each select="spatialVector">
            <xsl:if test="position()=$entityindex">
                   <xsl:choose>
                     <xsl:when test="references!=''">
                        <xsl:variable name="ref_id" select="references"/>
                        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                          <xsl:for-each select="$references">
                              <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="spatialVector">
                                       <xsl:with-param name="spatialvectorfirstColStyle" select="$firstColStyle"/>
                                        <xsl:with-param name="spatialvectorsubHeaderStyle" select="$subHeaderStyle"/>
                                        <xsl:with-param name="docid" select="$docid"/>
                                        <xsl:with-param name="entitytype" select="$entitytype"/>
                                        <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="spatialVector">
                                        <xsl:with-param name="spatialvectorfirstColStyle" select="$firstColStyle"/>
                                        <xsl:with-param name="spatialvectorsubHeaderStyle" select="$subHeaderStyle"/>
                                        <xsl:with-param name="docid" select="$docid"/>
                                        <xsl:with-param name="entitytype" select="$entitytype"/>
                                        <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                       </xsl:choose>
                   </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="$entitytype='storedProcedure'">
        <xsl:for-each select="storedProcedure">
            <xsl:if test="position()=$entityindex">
                   <xsl:choose>
                     <xsl:when test="references!=''">
                        <xsl:variable name="ref_id" select="references"/>
                        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                          <xsl:for-each select="$references">
                              <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="storedProcedure">
                                       <xsl:with-param name="storedprocedurefirstColStyle" select="$firstColStyle"/>
                                       <xsl:with-param name="storedproceduresubHeaderStyle" select="$subHeaderStyle"/>
                                       <xsl:with-param name="docid" select="$docid"/>
                                       <xsl:with-param name="entitytype" select="$entitytype"/>
                                       <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="storedProcedure">
                                       <xsl:with-param name="storedprocedurefirstColStyle" select="$firstColStyle"/>
                                       <xsl:with-param name="storedproceduresubHeaderStyle" select="$subHeaderStyle"/>
                                       <xsl:with-param name="docid" select="$docid"/>
                                       <xsl:with-param name="entitytype" select="$entitytype"/>
                                       <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                       </xsl:choose>
                   </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="$entitytype='view'">
        <xsl:for-each select="view">
            <xsl:if test="position()=$entityindex">
                   <xsl:choose>
                     <xsl:when test="references!=''">
                        <xsl:variable name="ref_id" select="references"/>
                        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                          <xsl:for-each select="$references">
                              <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="view">
                                       <xsl:with-param name="viewfirstColStyle" select="$firstColStyle"/>
                                       <xsl:with-param name="viewsubHeaderStyle" select="$subHeaderStyle"/>
                                       <xsl:with-param name="docid" select="$docid"/>
                                       <xsl:with-param name="entitytype" select="$entitytype"/>
                                       <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="view">
                                       <xsl:with-param name="viewfirstColStyle" select="$firstColStyle"/>
                                       <xsl:with-param name="viewsubHeaderStyle" select="$subHeaderStyle"/>
                                       <xsl:with-param name="docid" select="$docid"/>
                                       <xsl:with-param name="entitytype" select="$entitytype"/>
                                       <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                       </xsl:choose>
                   </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
      </xsl:if>
      <xsl:if test="$entitytype='otherEntity'">
        <xsl:for-each select="otherEntity">
            <xsl:if test="position()=$entityindex">
                   <xsl:choose>
                     <xsl:when test="references!=''">
                        <xsl:variable name="ref_id" select="references"/>
                        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                          <xsl:for-each select="$references">
                              <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="otherEntity">
                                       <xsl:with-param name="otherentityfirstColStyle" select="$firstColStyle"/>
                                       <xsl:with-param name="otherentitysubHeaderStyle" select="$subHeaderStyle"/>
                                       <xsl:with-param name="docid" select="$docid"/>
                                       <xsl:with-param name="entitytype" select="$entitytype"/>
                                       <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                              </xsl:choose>
                          </xsl:for-each>
                     </xsl:when>
                     <xsl:otherwise>
                       <xsl:choose>
                                 <xsl:when test="boolean(1)">
                                    <xsl:call-template name="otherEntity">
                                       <xsl:with-param name="otherentityfirstColStyle" select="$firstColStyle"/>
                                       <xsl:with-param name="otherentitysubHeaderStyle" select="$subHeaderStyle"/>
                                       <xsl:with-param name="docid" select="$docid"/>
                                       <xsl:with-param name="entitytype" select="$entitytype"/>
                                       <xsl:with-param name="entityindex" select="$entityindex"/>
                                    </xsl:call-template>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <xsl:call-template name="chooseattributelist"/>
                                 </xsl:otherwise>
                       </xsl:choose>
                   </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:for-each>
      </xsl:if>
   </xsl:template>

  <xsl:template name="chooseattributelist">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: chooseattributelist</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="attributeList">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="chooseattribute"/>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="chooseattribute"/>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="chooseattribute">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: chooseattribute</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="attribute">
      <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>  Processing attribute: </xsl:text><xsl:value-of select="./attributeName"/></xsl:message></xsl:if>
      <xsl:if test="boolean(0)">
        <xsl:if test="boolean(1)">
          <xsl:for-each select="measurementScale/*/*">
            <xsl:call-template name="nonNumericDomain">
              <xsl:with-param name="nondomainfirstColStyle" select="$firstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:if>
        <xsl:if test="boolean(1)">
          <xsl:for-each select="coverage">
            <xsl:call-template name="coverage">
              <xsl:with-param name="coveragefirstColStyle" select="$firstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:if>
        <xsl:if test="boolean(1)">
          <xsl:for-each select="method | methods"> <!-- mob kludge for eml2.1 -->
            <xsl:call-template name="method">
                <xsl:with-param name="methodfirstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="methodsubHeaderStyle" select="$firstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:if>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <!-- Distribution Inline Data display module -->
  <xsl:template name="emlinlinedata">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: emlinlinedata</xsl:text></xsl:message></xsl:if>
    <tr>
      <td><h3>Data (inline):</h3></td>
    </tr>
    <tr>
      <td>
        <xsl:if test="$distributionlevel='toplevel'">
          <xsl:for-each select="distribution">
            <xsl:if test="position()=$distributionindex">
              <xsl:choose>
                <xsl:when test="references!=''">
                  <xsl:variable name="ref_id1" select="references"/>
                  <xsl:variable name="references1" select="$ids[@id=$ref_id1]" />
                  <xsl:for-each select="$references1">
                    <xsl:for-each select="inline">
                      <pre>
                        <xsl:value-of  select="." xml:space="preserve"/>
                      </pre>
                      <!--   <xsl:value-of select="."/> -->
                    </xsl:for-each>
                  </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:for-each select="inline">
                    <pre>
                      <xsl:value-of  select="." xml:space="preserve"/>
                    </pre>
                    <!-- <xsl:value-of select="."/> -->
                  </xsl:for-each>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:if>
          </xsl:for-each>
        </xsl:if>
        <xsl:if test="$distributionlevel='entitylevel'">
          <xsl:if test="$entitytype='dataTable'">
            <xsl:for-each select="dataTable">
              <xsl:if test="position()=$entityindex">
                <xsl:choose>
                  <xsl:when test="references!=''">
                    <xsl:variable name="ref_id2" select="references"/>
                    <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                    <xsl:for-each select="$references2">
                      <xsl:call-template name="choosephysical"/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="choosephysical"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
          <xsl:if test="$entitytype='spatialRaster'">
            <xsl:for-each select="spatialRaster">
              <xsl:if test="position()=$entityindex">
                <xsl:choose>
                  <xsl:when test="references!=''">
                    <xsl:variable name="ref_id2" select="references"/>
                    <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                    <xsl:for-each select="$references2">
                      <xsl:call-template name="choosephysical"/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="choosephysical"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
          <xsl:if test="$entitytype='spatialVector'">
            <xsl:for-each select="spatialVector">
              <xsl:if test="position()=$entityindex">
                <xsl:choose>
                  <xsl:when test="references!=''">
                    <xsl:variable name="ref_id2" select="references"/>
                    <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                    <xsl:for-each select="$references2">
                      <xsl:call-template name="choosephysical"/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="choosephysical"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
          <xsl:if test="$entitytype='storedProcedure'">
            <xsl:for-each select="storedProcedure">
              <xsl:if test="position()=$entityindex">
                <xsl:choose>
                  <xsl:when test="references!=''">
                    <xsl:variable name="ref_id2" select="references"/>
                    <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                    <xsl:for-each select="$references2">
                      <xsl:call-template name="choosephysical"/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="choosephysical"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
          <xsl:if test="$entitytype='view'">
            <xsl:for-each select="view">
              <xsl:if test="position()=$entityindex">
                <xsl:choose>
                  <xsl:when test="references!=''">
                    <xsl:variable name="ref_id2" select="references"/>
                    <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                    <xsl:for-each select="$references2">
                      <xsl:call-template name="choosephysical"/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="choosephysical"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
          <xsl:if test="$entitytype='otherEntity'">
            <xsl:for-each select="otherEntity">
              <xsl:if test="position()=$entityindex">
                <xsl:choose>
                  <xsl:when test="references!=''">
                    <xsl:variable name="ref_id2" select="references"/>
                    <xsl:variable name="references2" select="$ids[@id=$ref_id2]" />
                    <xsl:for-each select="$references2">
                      <xsl:call-template name="choosephysical"/>
                    </xsl:for-each>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:call-template name="choosephysical"/>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:if>
            </xsl:for-each>
          </xsl:if>
        </xsl:if>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="choosephysical">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: choosephysical</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="physical">
      <xsl:if test="position()=$physicalindex">
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]" />
            <xsl:for-each select="$references">
              <xsl:call-template name="choosedistribution"/>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="choosedistribution"/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="choosedistribution">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: choosedistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <xsl:if test="$distributionindex=position()">
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]" />
            <xsl:for-each select="$references">
              <xsl:for-each select="inline">
                <pre>
                  <xsl:value-of select="." xml:space="preserve"/>
                </pre>
                <!--  <xsl:value-of select="."/> -->
              </xsl:for-each>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:for-each select="inline">
              <pre>
                <xsl:value-of select="." xml:space="preserve"/>
              </pre>
              <!--  <xsl:value-of select="."/> -->
            </xsl:for-each>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>
  <!-- Citation part -->
  <xsl:template name="emlcitation">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: emlcitation</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <!-- <xsl:when test="$displaymodule='inlinedata'"> -->
      <xsl:when test="boolean(0)">
          <xsl:call-template name="emlinlinedata"/>
      </xsl:when>
      <xsl:otherwise>
      <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}">
        <tr>
          <td colspan="2"><h3>Citation Description</h3></td>
        </tr>
        <xsl:call-template name="identifier">
          <xsl:with-param name="packageID" select="../@packageId"/>
          <xsl:with-param name="system" select="../@system"/>
        </xsl:call-template>
        <tr>
          <td colspan="2">
            <xsl:call-template name="citation">
              <xsl:with-param name="citationfirstColStyle" select="$firstColStyle"/>
              <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  <!-- Software part -->
  <xsl:template name="emlsoftware">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: emlsoftware</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <!-- <xsl:when test="$displaymodule='inlinedata'"> -->
      <xsl:when test="boolean(0)">
        <xsl:call-template name="emlinlinedata"/>
      </xsl:when>
      <xsl:otherwise>
        <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}">
          <tr>
            <td colspan="2"><h3>Software Description</h3></td>
          </tr>
          <xsl:call-template name="identifier">
            <xsl:with-param name="packageID" select="../@packageId"/>
            <xsl:with-param name="system" select="../@system"/>
          </xsl:call-template>
          <tr>
            <td colspan="2">
              <xsl:call-template name="software">
                <xsl:with-param name="softwarefirstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="softwaresubHeaderStyle" select="$subHeaderStyle"/>
              </xsl:call-template>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- Protocol part -->
  <xsl:template name="emlprotocol">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: emlprotocol</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <!-- <xsl:when test="$displaymodule='inlinedata'"> -->
      <xsl:when test="boolean(0)">
        <xsl:call-template name="emlinlinedata"/>
      </xsl:when>
      <xsl:otherwise>
        <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}">
          <tr>
            <td colspan="2"><h3>Protocal Description</h3></td>
          </tr>
          <xsl:call-template name="identifier">
            <xsl:with-param name="packageID" select="../@packageId"/>
            <xsl:with-param name="system" select="../@system"/>
          </xsl:call-template>
          <tr>
            <td colspan="2">
              <xsl:call-template name="protocol">
                <xsl:with-param name="protocolfirstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="protocolsubHeaderStyle" select="$subHeaderStyle"/>
              </xsl:call-template>
            </td>
          </tr>
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- download XML part -->
  <xsl:template name="xml">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: xml</xsl:text></xsl:message></xsl:if>
    <br/><a target="_blank" href="./metadataviewer?packageid={$packageID}&#38;contentType=application/xml">Download as XML</a> (in Ecological Metadata Language)
  </xsl:template>
  
  <!-- This module is for dataset -->
  <xsl:template match="dataset" mode="dataset">
    <xsl:param name="packageID" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: dataset</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references != ''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <xsl:call-template name="datasetmixed"/>
        </xsl:for-each>    
      </xsl:when>
      <xsl:otherwise>
              <xsl:call-template name="datasetmixed">
                <xsl:with-param name="packageID" select="$packageID"></xsl:with-param>
              </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="datasettitle">
    <xsl:param name="packageID" ></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasettitle</xsl:text></xsl:message></xsl:if>
    <h2>
      <xsl:for-each select="./title">
        <xsl:value-of select="."/>
      </xsl:for-each>
    </h2>
  </xsl:template>
  
  <xsl:template name="datasetmixed">
    <xsl:param name="packageID"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetmixed</xsl:text></xsl:message></xsl:if>
    <table class="subGroup onehundred_percent">
      <tr>
        <th colspan="2">Data Package General Information:</th>
      </tr>
      <!-- put in the identifier and system that the ID belongs to -->
      <xsl:if test="../@packageId">
        <xsl:for-each select="../@packageId">
          <xsl:call-template name="identifier">
            <xsl:with-param name="packageID" select="../@packageId"/>
            <xsl:with-param name="system" select="../@system"/>
            <xsl:with-param name="IDfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="IDsecondColStyle" select="$secondColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the title -->            
      <xsl:if test="./title">
        <xsl:for-each select="./title">
          <xsl:call-template name="resourcetitle">
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the alternate identifiers -->
      <xsl:if test="alternateIdentifier">
        <xsl:for-each select="alternateIdentifier">
          <xsl:call-template name="resourcealternateIdentifier">
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the text of the abstract-->
      <xsl:if test="./abstract">
        <xsl:for-each select="./abstract">
          <xsl:call-template name="resourceabstract">
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the purpose of the dataset-->
      <xsl:if test="./purpose">
        <xsl:for-each select="./purpose">
          <xsl:call-template name="datasetpurpose">
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the short name -->
      <xsl:if test="shortName">
        <xsl:for-each select="./shortName">
          <xsl:call-template name="resourceshortName">
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
            <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
         </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the publication date -->
      <xsl:if test="./pubDate">
        <xsl:for-each select="pubDate">
          <xsl:call-template name="resourcepubDate" >
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the language -->
      <xsl:if test="./language">
        <xsl:for-each select="language">
          <xsl:call-template name="resourcelanguage" >
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- put in the series -->
      <xsl:if test="./series">
        <xsl:for-each select="series">
          <xsl:call-template name="resourceseries" >
            <xsl:with-param name="resfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:if>
      <!-- the dataset-level distribution tag. for LTER, it should be 
           function=information. the link to the data entity itself 
           will be in the entity's tree.  -->
      <xsl:if test="distribution/@id">
        <tr>
          <td colspan="2">
            <table class="subGroup onehundred_percent">
              <tr>
                <td>
                  <table class="{$tabledefaultStyle}">
                    <th colspan="2">For more information:</th>
                    <xsl:for-each select="distribution">
                      <tr>
                        <td class="{$firstColStyle}"><xsl:text>Visit: </xsl:text></td>
                        <td class="{$secondColStyle}">
                          <a><xsl:attribute name="href"><xsl:value-of select="online/url"/></xsl:attribute><xsl:value-of select="online/url"/></a>
                        </td>
                      </tr>
                    </xsl:for-each>
                  </table>
                </td>
              </tr>
            </table>
          </td>
        </tr>
      </xsl:if>      
    </table>
    <!-- Add in the temporal coverage info -->
    <xsl:if test="./coverage/temporalCoverage">
      <xsl:for-each select="./coverage/temporalCoverage">
        <xsl:call-template name="temporalCoverage">
          <xsl:with-param name="firstColStyle" select="$firstColStyle"/>
          <xsl:with-param name="secondColStyle" select="$secondColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <table class="subgroup onehundred_percent">
      <tr>
        <th colspan="2"><br/>People and Organizations</th>
      </tr>
      <!-- Put the contacts first -->
      <xsl:for-each select="contact">
      <tr>
        <td class="{$firstColStyle}">Contact:</td>
          <td class="{$secondColStyle}">
            <xsl:choose>
              <xsl:when test="individualName">
                <!--  if creator has an individual, show it and make creator's with other labels subordinate  -->
                <xsl:for-each select="individualName">
                  <xsl:value-of select="surName"/>
                  <xsl:if test="givenName">
                    <xsl:text>,&#160;</xsl:text>
                    <xsl:for-each select="givenName">
                      <xsl:value-of select="."/>
                      <xsl:text>&#160;</xsl:text>
                    </xsl:for-each>
                  </xsl:if> 
                  <xsl:if test="../organizationName or ../positionName">
                    <xsl:text>(</xsl:text>
                    <xsl:choose>
                      <xsl:when test="../organizationName and ../positionName">
                        <xsl:value-of select="../organizationName"/>
                        <xsl:text>,&#160;</xsl:text>
                        <xsl:value-of select="../positionName"/>
                      </xsl:when>
                      <xsl:when test="../organizationName and not(../positionName)">
                        <xsl:value-of select="../organizationName"/>
                      </xsl:when>
                      <xsl:when test="not(../organizationName) and ../positionName">
                        <xsl:value-of select="../positionName"/>
                      </xsl:when>
                    </xsl:choose>
                    <xsl:text>)</xsl:text>
                  </xsl:if>
                </xsl:for-each>                        
              </xsl:when> 
              <xsl:otherwise>
                <!--  the contact has no individual.   -->
                <xsl:choose>
                  <xsl:when test="positionName">
                    <!-- next most important is a position, with org subordinate -->
                    <xsl:value-of select="positionName"/>
                    <xsl:if test="organizationName">
                      <xsl:text>&#160;(</xsl:text>
                      <xsl:value-of select="organizationName"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                    <xsl:if test="organizationName and not(positionName)">
                      <!-- Organization appears alone if alone  -->
                      <xsl:value-of select="organizationName"/>
                    </xsl:if>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#160;</xsl:text>
            <xsl:if test="electronicMailAddress">[&#160;
              <a><xsl:attribute name="href"><xsl:text>mailto:</xsl:text><xsl:value-of select="electronicMailAddress"/></xsl:attribute>email</a>&#160;]
            </xsl:if>
          </td>
        </tr>
      </xsl:for-each>                 
      <!-- second is creators -->
      <xsl:for-each select="creator">
        <tr>
          <xsl:choose>
            <xsl:when test="individualName">
              <!--  if creator has an individual, so it and make creator's  with other labels subordinate  -->
              <td class="{$firstColStyle}">Creator:</td>
              <td class="{$secondColStyle}">
                <xsl:for-each select="individualName">
                  <xsl:value-of select="surName"/>
                  <xsl:if test="givenName">
                    <xsl:text>,&#160;</xsl:text>
                    <xsl:for-each select="givenName">
                      <xsl:value-of select="."/>
                      <xsl:text>&#160;</xsl:text>
                    </xsl:for-each>
                  </xsl:if> 
                  <xsl:if test="../organizationName or ../positionName">
                    <xsl:text>(</xsl:text>
                    <xsl:choose>
                      <xsl:when test="../organizationName and ../positionName">
                        <xsl:value-of select="../organizationName"/>
                        <xsl:text>,&#160;</xsl:text>
                        <xsl:value-of select="../positionName"/>
                      </xsl:when>
                      <xsl:when test="../organizationName and not(../positionName)">
                        <xsl:value-of select="../organizationName"/>
                      </xsl:when>
                      <xsl:when test="not(../organizationName) and ../positionName">
                        <xsl:value-of select="../positionName"/>
                      </xsl:when>
                    </xsl:choose>
                    <xsl:text>)</xsl:text>
                  </xsl:if>
                </xsl:for-each>
              </td>
            </xsl:when> 
            <xsl:otherwise>
              <!--  the creator has no individual.   -->
              <xsl:if test="positionName">
                <!-- next most important is a position, with org subordinate -->
                <td class="{$firstColStyle}">Position:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="positionName"/>
                  <xsl:if test="../organizationName">
                    <xsl:text>(</xsl:text>
                      <xsl:value-of select="../organizationName"/>
                    <xsl:text>)</xsl:text>
                  </xsl:if>
                </td>
              </xsl:if>
              <xsl:if test="organizationName">
                <!-- Organization appears alone if alone under creator -->
                <td class="{$firstColStyle}">Organization:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="organizationName"/></td>
              </xsl:if>
            </xsl:otherwise>
          </xsl:choose>
        </tr>
      </xsl:for-each> <!-- end creators  --> 
      <!-- then everyone else -->            
      <xsl:if test="associatedParty">
        <xsl:for-each select="associatedParty">
          <tr>
            <xsl:choose>
              <xsl:when test="individualName">
                <!--  associatedParty has an individual, so  other labels are subordinate  -->
                <td class="{$firstColStyle}">Associate:</td>
                <td class="{$secondColStyle}">
                  <xsl:for-each select="individualName">
                    <xsl:value-of select="surName"/>
                    <xsl:if test="givenName">
                      <xsl:text>,&#160;</xsl:text>
                      <xsl:for-each select="givenName">
                        <xsl:value-of select="."/>
                        <xsl:text>&#160;</xsl:text>
                      </xsl:for-each>
                    </xsl:if> 
                    <xsl:if test="../organizationName or ../positionName">
                      <xsl:text>(</xsl:text>
                      <xsl:choose>
                        <xsl:when test="../organizationName and ../positionName">
                          <xsl:value-of select="../organizationName"/>
                          <xsl:text>,&#160;</xsl:text>
                          <xsl:value-of select="../positionName"/>
                        </xsl:when>
                        <xsl:when test="../organizationName and not(../positionName)">
                          <xsl:value-of select="../organizationName"/>
                        </xsl:when>
                        <xsl:when test="not(../organizationName) and ../positionName">
                          <xsl:value-of select="../positionName"/>
                        </xsl:when>
                      </xsl:choose>
                      <xsl:if test="../role">
                        <xsl:text>,&#160;</xsl:text>
                        <xsl:value-of select="../role"/>
                      </xsl:if>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                  </xsl:for-each>
                </td>
              </xsl:when> 
              <xsl:otherwise>
                <!--  the party has no individual.   -->
                <xsl:if test="positionName">
                  <!-- next most important is a position, with org subordinate -->
                  <td>Position</td>
                  <td>
                    <xsl:value-of select="positionName"/>
                    <xsl:if test="../organizationName">
                      <xsl:text>(</xsl:text>
                      <xsl:value-of select="../organizationName"/>
                      <xsl:text>)</xsl:text>
                    </xsl:if>
                  </td>
                </xsl:if>
                <xsl:if test="organizationName">
                  <!-- Organization appears alone if alone under party -->
                  <td>Organization:</td>
                  <td><xsl:value-of select="organizationName"/></td>
                </xsl:if>
              </xsl:otherwise>
            </xsl:choose>
          </tr>
        </xsl:for-each> <!-- end associatedParty -->
      </xsl:if>
    </table>   
    <!-- create a table listing the dataset entities -->
    <xsl:if test="dataTable|spatialRaster|spatialVector|storedProcedure|view|otherEntity">
      <table class="{$tabledefaultStyle}">
        <xsl:if test="dataTable or spatialRaster or spatialVector or storedProcedures or view or otherEntity">
          <tr>
            <th colspan="2"><br/><xsl:text>Data Entities</xsl:text></th>
          </tr>
        </xsl:if>
        <!--  when you call the entityurl template, include a label for type of entity  -->
        <xsl:for-each select="dataTable">
          <xsl:call-template name="entityurl">
            <xsl:with-param name="type">dataTable</xsl:with-param>
            <xsl:with-param name="showtype">Data Table</xsl:with-param>
            <xsl:with-param name="index" select="position()"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="spatialRaster">
          <xsl:call-template name="entityurl">
            <xsl:with-param name="type">spatialRaster</xsl:with-param>
            <xsl:with-param name="showtype">Spatial Raster</xsl:with-param>
            <xsl:with-param name="index" select="position()"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="spatialVector">
          <xsl:call-template name="entityurl">
            <xsl:with-param name="type">spatialVector</xsl:with-param>
            <xsl:with-param name="showtype">Spatial Vector</xsl:with-param>
            <xsl:with-param name="index" select="position()"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="storedProcedure">
          <xsl:call-template name="entityurl">
            <xsl:with-param name="type">storedProcedure</xsl:with-param>
            <xsl:with-param name="showtype">Stored Procedure</xsl:with-param>
            <xsl:with-param name="index" select="position()"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="view">
          <xsl:call-template name="entityurl">
            <xsl:with-param name="type">view</xsl:with-param>
            <xsl:with-param name="showtype">View</xsl:with-param>
            <xsl:with-param name="index" select="position()"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:for-each select="otherEntity">
          <xsl:call-template name="entityurl">
            <xsl:with-param name="type">otherEntity</xsl:with-param>
            <xsl:with-param name="showtype">Other</xsl:with-param>
            <xsl:with-param name="index" select="position()"/>
          </xsl:call-template>
        </xsl:for-each>
      </table>
    </xsl:if>           
    <!-- dataset citation  
    <table class="{$tabledefaultStyle}">
      <tr>
        <th colspan="2">Data Package Citation:</th>
      </tr>
      <xsl:call-template name="howtoCite">
        <xsl:with-param name="citetabledefaultStyle"  select="$tabledefaultStyle"/>
        <xsl:with-param name="citefirstColStyle"  select="$firstColStyle"/>
        <xsl:with-param name="citesecondColStyle"  select="$secondColStyle"/>
      </xsl:call-template>   
    </table> --> 
    <!-- add in the method info
    <h3>Sampling, Processing and Quality Control Methods</h3>
    <table class="subGroup onehundred_percent">  
      <tr>
        <td colspan="2" class="onehundred_percent">
          <xsl:if test="./methods">
            <xsl:for-each select="./methods">
              <xsl:call-template name="datasetmethod">
                <xsl:with-param name="methodfirstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="methodsecondColStyle" select="$secondColStyle"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if>
        </td>
      </tr>
    </table> -->
    <xsl:if test="boolean(0)">
      <!-- add in the access control info -->
      <table class="subGroup onehundred_percent">  
        <tr>
          <td>
            <xsl:if test="access">
              <xsl:for-each select="access">
                <xsl:call-template name="access">
                  <xsl:with-param name="accessfirstColStyle" select="$firstColStyle"/>
                  <!-- <xsl:with-param name="accesssecondColStyle" select="$secondColStyle"/> -->
                </xsl:call-template>
              </xsl:for-each>
            </xsl:if>
          </td>
        </tr>
      </table>
    </xsl:if>
  </xsl:template>

  <xsl:template name="datasetpurpose">
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetpurpose</xsl:text></xsl:message></xsl:if>
      <tr>
        <td class="{$resfirstColStyle}"><xsl:text>Purpose:</xsl:text></td>
        <td>
          <xsl:call-template name="text">
           <xsl:with-param name="textfirstColStyle" select="$resfirstColStyle"/>
           <xsl:with-param name="textsecondColStyle" select="$ressecondColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
  </xsl:template>

  <xsl:template name="datasetmaintenance">
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetmaintenance</xsl:text></xsl:message></xsl:if>
      <tr>
        <th colspan="2"><xsl:text>Maintenance:</xsl:text></th>
      </tr>
      <xsl:call-template name="mantenancedescription"/>
      <tr>
        <td class="{$firstColStyle}">Frequency:</td>
        <td class="{$secondColStyle}" ><xsl:value-of select="maintenanceUpdateFrequency"/></td>
      </tr>
      <xsl:call-template name="datasetchangehistory"/>
  </xsl:template>

  <xsl:template name="mantenancedescription">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: mantenancedescription</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="description">
      <tr>
        <td class="{$firstColStyle}">Description:</td>
        <td>
          <xsl:call-template name="text">
            <xsl:with-param name="textfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datasetchangehistory">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetchangehistory</xsl:text></xsl:message></xsl:if>
    <xsl:if test="changeHistory">
      <tr>
        <td class="{$firstColStyle}">History:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:for-each select="changeHistory">
              <xsl:call-template name="historydetails"/>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="historydetails">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: historydetails</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">scope:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="changeScope"/></td>
    </tr>
    <tr>
      <td class="{$firstColStyle}">old value:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="oldValue"/></td>
    </tr>
    <tr>
      <td class="{$firstColStyle}">change date:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="changeDate"/></td>
    </tr>
    <xsl:if test="comment and normalize-space(comment) != ''">
      <tr>
        <td class="{$firstColStyle}">comment:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="comment"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="datasetcontact">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetcontact</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2"><xsl:text>Contact:</xsl:text></td>
    </tr>
    <xsl:for-each select="contact">
      <tr>
        <td colspan="2">
          <xsl:call-template name="party">
            <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datasetpublisher">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetpublisher</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="publisher">
      <tr>
        <td colspan="2"><xsl:text>Publisher:</xsl:text></td>
      </tr>
      <tr>
        <td colspan="2">
          <xsl:call-template name="party">
            <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datasetpubplace">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetpubplace</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="pubPlace">
      <tr>
        <td class="{$firstColStyle}">Publish Place:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datasetmethod">
    <xsl:param name="firstColStyle"></xsl:param>
    <xsl:param name="secondColStyle"></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetmethod</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select=".">
      <xsl:call-template name="method">
        <xsl:with-param name="methodfirstColStyle" select="$firstColStyle"/>
	    <xsl:with-param name="methodsubHeaderStyle" select="$subHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datasetproject">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetproject</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="project">
      <tr>
        <td colspan="2"><h3><xsl:text>Parent Project Information:</xsl:text></h3></td>
      </tr>
      <tr>
        <td colspan="2">
          <xsl:call-template name="project">
            <xsl:with-param name="projectfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- 2 col structure, maybe a link to dataTable in col2-->
  <xsl:template name="entityurl">
    <xsl:param name="showtype"/>
    <xsl:param name="type"/>
    <xsl:param name="index"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entityurl</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references != ''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <tr>
            <td class="{$firstColStyle}">
              <!--  this is the simple label-only version, instead of the form button. -->
              <xsl:value-of select="$showtype"/> Name<xsl:text>:</xsl:text>
            </td>
            <td class="{$secondColStyle}">
              <strong><xsl:value-of select="./entityName"/></strong>
            </td>
          </tr>
          <tr>
            <td class="{$firstColStyle}">
              <!--  this is the simple label-only version, instead of the form button. -->
              Description<xsl:text>:</xsl:text>
            </td>
            <td class="{$secondColStyle}">
              <xsl:value-of select="./entityDescription"/>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td class="{$firstColStyle}">
            <xsl:value-of select="$showtype"/> Name<xsl:text>:</xsl:text><br></br>
          </td>
          <td class="{$secondColStyle}">
            <strong><xsl:value-of select="./entityName"/></strong><br></br>
          </td>
        </tr>
        <tr>
          <td class="{$firstColStyle}">Description<xsl:text>:</xsl:text><br></br></td>
          <td class="{$secondColStyle}"><xsl:value-of select="./entityDescription"/></td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template match="text()" mode="dataset" />

  <xsl:template match="text()" mode="resource" />

  <!-- This module is for party member and it is self contained -->
  <xsl:template name="party">
    <!-- added these params so that the display of a persons profile page can use different apps -->
    <xsl:param name="useridDirectory1"/>
    <xsl:param name="useridDirectoryApp1"/>
    <xsl:param name="useridDirectoryLabel1"/>
    <xsl:param name="partyfirstColStyle"/>
    <xsl:param name="partysecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: party</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:apply-templates mode="party">
              <xsl:with-param name="partyfirstColStyle" select="$partyfirstColStyle"/>
            </xsl:apply-templates>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:apply-templates mode="party">
            <xsl:with-param name="partyfirstColStyle" select="$partyfirstColStyle"/>
          </xsl:apply-templates>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>
  
  <xsl:template match="individualName" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: individualName</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}" >Individual:</td>
        <td class="{$secondColStyle}" >
          <strong>
            <xsl:value-of select="./salutation"/><xsl:text> </xsl:text>
            <xsl:value-of select="./givenName"/><xsl:text> </xsl:text>
            <xsl:value-of select="./surName"/>
          </strong>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="organizationName" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: organizationName</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}" >Organization:</td>
        <td class="{$secondColStyle}"><strong><xsl:value-of select="."/></strong></td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="positionName" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: positionName</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}">Position:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="address" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: address; mode: party</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <xsl:call-template name="addressCommon">
        <xsl:with-param name="partyfirstColStyle" select="$partyfirstColStyle"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <!-- This template will be call by other place-->
  <xsl:template name="address">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: address</xsl:text></xsl:message></xsl:if>
    <table class="{$tablepartyStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="addressCommon">
              <xsl:with-param name="partyfirstColStyle" select="$partyfirstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="addressCommon">
            <xsl:with-param name="partyfirstColStyle" select="$partyfirstColStyle"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>
  
  <xsl:template name="addressCommon">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: addressCommon</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}">Address:</td>
        <td>
          <table class="{$tablepartyStyle}">
            <xsl:for-each select="deliveryPoint">
              <tr>
                <td class="{$secondColStyle}"><xsl:value-of select="."/><xsl:text>, </xsl:text></td>
              </tr>
            </xsl:for-each>
            <!-- only include comma if city exists... -->
            <tr>
              <td class="{$secondColStyle}" >
                <xsl:if test="normalize-space(city)!=''">
                  <xsl:value-of select="city"/><xsl:text>, </xsl:text>
                </xsl:if>
                <xsl:if test="normalize-space(administrativeArea)!= '' or normalize-space(postalCode) != ''">
                  <xsl:value-of select="administrativeArea"/><xsl:text> </xsl:text><xsl:value-of select="postalCode"/><xsl:text> </xsl:text>
                </xsl:if>
                <xsl:if test="normalize-space(country)!=''">
                  <xsl:value-of select="country"/>
                </xsl:if>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="phone" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: phone</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$partyfirstColStyle}" >Phone:</td>
      <td>
        <table class="{$tablepartyStyle}">
          <tr>
            <td class="{$secondColStyle}">
              <xsl:value-of select="."/>
              <xsl:if test="normalize-space(./@phonetype)!=''">
                <xsl:text> (</xsl:text><xsl:value-of select="./@phonetype"/><xsl:text>)</xsl:text>
              </xsl:if>
            </td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>
  
  <xsl:template match="electronicMailAddress" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: electronicMailAddress</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}" >Email Address:</td>
        <td>
          <table class="{$tablepartyStyle}">
            <tr>
              <td class="{$secondColStyle}">
                <a>
                  <xsl:attribute name="href">mailto:<xsl:value-of select="."/></xsl:attribute>
                  <xsl:value-of select="./entityName"/>
                  <xsl:value-of select="."/>
                </a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="onlineUrl" mode="party">
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: onlineUrl</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}" >Web Address:</td>
        <td>
          <table class="{$tablepartyStyle}">
            <tr>
              <td class="{$secondColStyle}">
                <a>
                  <xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute>
                  <xsl:value-of select="./entityName"/>
                  <xsl:value-of select="."/>
                </a>
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <!-- 
  2010: started using the userId field to display a link to the user's profile page.
  This could also be done with the creator id attribute, but only one of those is allowed. The userId
  element is repeatable, and an anchor tag can be constructed with params based on the content of 
  the element's @directory attribute.
  -->
  <xsl:template match="userId" mode="party">
    <xsl:param name="useridDirectory1" select="$useridDirectory1" />
    <xsl:param name="useridDirectoryApp1_URI" select="$useridDirectoryApp1_URI"/>
    <xsl:param name="useridDirectoryLabel1" select="$useridDirectoryLabel1"/>
    <xsl:param name="partyfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: userId</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test=" @directory=$useridDirectory1 ">
        <xsl:if test="normalize-space(.)!=''">
          <tr>
            <td class="{$partyfirstColStyle}" >Profile:</td>
            <td class="{$secondColStyle}">
              <xsl:element name="a">
                <xsl:attribute name="href">
                  <xsl:value-of select="$useridDirectoryApp1_URI"/><xsl:value-of select="."/>
                </xsl:attribute>
                <xsl:value-of select="$useridDirectoryLabel1"/>
                <xsl:text> Profile for </xsl:text>
                <xsl:value-of select="../individualName/surName"/>
              </xsl:element>
            </td>
          </tr>
        </xsl:if>
      </xsl:when>
      <xsl:when test=" @directory='LTERnetwork-directory' ">
        <!-- finish when lter dir available by ID.
        <xsl:if test="normalize-space(.)!=''">
          <tr>
            <td class="{$partyfirstColStyle}" >Link to Profile:</td>
            <td class="{$secondColStyle}"><xsl:value-of select="."/>LTER Network Personnel Directory</td>
          </tr>
        </xsl:if> -->
      </xsl:when>
      <xsl:otherwise>
        <xsl:if test="normalize-space(.) != ''">
          <tr>
            <td class="{$partyfirstColStyle}" >Id:</td>
            <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
          </tr>
        </xsl:if>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template match="role" mode="party">
    <xsl:param name="partyfirstColStyle" select="$firstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: role</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$partyfirstColStyle}" >Role:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:if>
  </xsl:template>
  
  <xsl:template match="text()" mode="party" />

  <!-- This module is for coverage and it is self contained. (It is a table
       and will handle reference by it self)-->
  <!-- mob took out the table elements in the coverage template. they are 
       also in geo, temporal and taxonomic templates -->
  <xsl:template name="coverage">
    <xsl:param name="coveragefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: coverage</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
			<!--	  <table class="{$tabledefaultStyle}">  -->           
            <xsl:for-each select="geographicCoverage">
                <xsl:call-template name="geographicCoverage">
                </xsl:call-template>
            </xsl:for-each>
       <!--  </table>  -->
        <!--  <table class="{$tabledefaultStyle}">  -->
             <xsl:for-each select="temporalCoverage">
                <xsl:call-template name="temporalCoverage">
                </xsl:call-template>
            </xsl:for-each>
         <!-- </table>  -->
         <!-- <table class="{$tabledefaultStyle}">  -->
            <xsl:for-each select="taxonomicCoverage">
                <xsl:call-template name="taxonomicCoverage">
                </xsl:call-template>
            </xsl:for-each>
        <!--  </table>  -->
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
      <!--   <table class="{$tabledefaultStyle}">  -->
            <xsl:for-each select="geographicCoverage">             
                <xsl:call-template name="geographicCoverage">
                </xsl:call-template>
            </xsl:for-each>
       <!--  </table>  -->
        <!--   <table class="{$tabledefaultStyle}"> -->
            <xsl:for-each select="temporalCoverage">
                <xsl:call-template name="temporalCoverage">
                </xsl:call-template>
            </xsl:for-each>
         <!-- </table> -->
        <!--  <table class="{$tabledefaultStyle}"> -->
            <xsl:for-each select="taxonomicCoverage">
                <xsl:call-template name="taxonomicCoverage">
                </xsl:call-template>
            </xsl:for-each>
        <!--  </table>  -->
        </xsl:otherwise>
      </xsl:choose>
  </xsl:template>

 <!-- ********************************************************************* -->
 <!-- **************  G E O G R A P H I C   C O V E R A G E  ************** -->
 <!-- ********************************************************************* -->
  <xsl:template name="geographicCoverage">
    <xsl:param name="firstColStyle" ></xsl:param>
    <xsl:param name="secondColStyle" ></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: geographicCoverage</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <!-- <xsl:for-each select="geographicCoverage"> -->
          <!-- letting the foreach select the current node instead of geographicCoverage lets this work for
          either dataset/coverage or attribute/coverage, but I do not know why (oh no!).  -->
          <xsl:for-each select=".">
            <table class="subgroup onehundred_percent">
              <xsl:call-template name="geographicCovCommon" />
            </table>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <table class="subgroup onehundred_percent">
            <xsl:call-template name="geographicCovCommon" />
          </table>
      </xsl:otherwise>
    </xsl:choose>  
  </xsl:template>

  <xsl:template name="geographicCovCommon">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: geographicCovCommon</xsl:text></xsl:message></xsl:if>
    <!-- use the bounding coordinates to determine if  lats and longs are alike. 
         set a boolean  that can be used to choose labels -->
    <xsl:variable name="west" select="./boundingCoordinates/westBoundingCoordinate"/>
    <xsl:variable name="east" select="./boundingCoordinates/eastBoundingCoordinate"/>
    <xsl:variable name="north" select="./boundingCoordinates/northBoundingCoordinate"/>
    <xsl:variable name="south" select="./boundingCoordinates/southBoundingCoordinate"/>
    <xsl:variable name="lat-lon-identical">
      <xsl:choose>
        <xsl:when test="($west = $east) and ($north = $south)">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <tr>  
      <th colspan="2"><!-- label for geoCov group chosen based on lat-lon boolean -->
        <xsl:choose>
          <xsl:when test="$lat-lon-identical = 'true' ">
            <xsl:text>Sampling Site:&#160;</xsl:text>
            <xsl:if test="(contains(@system, 'sbclter' ) ) and (not(contains(@id, 'boilerplate' ) ) ) ">
              <xsl:value-of select="@id"/>
            </xsl:if>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Geographic Region:</xsl:text>              
          </xsl:otherwise>
        </xsl:choose>
      </th>
    </tr>
    <xsl:apply-templates select="geographicDescription"/>
    <tr>
      <td>
        <xsl:apply-templates select="boundingCoordinates">
          <xsl:with-param name="lat-lon-identical" select="$lat-lon-identical"/>
        </xsl:apply-templates>
      </td>
    </tr>
    <xsl:for-each select="datasetGPolygon">
      <xsl:if test="datasetGPolygonOuterGRing">
        <xsl:apply-templates select="datasetGPolygonOuterGRing"/>
      </xsl:if>
      <xsl:if test="datasetGPolygonExclusionGRing">
        <xsl:apply-templates select="datasetGPolygonExclusionGRing"/>
      </xsl:if>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="geographicDescription">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: geographicDescription</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Description:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="boundingCoordinates">
    <xsl:param name="lat-lon-identical"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: boundingCoordinates</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">
        <xsl:choose>
          <xsl:when test="$lat-lon-identical= 'true' ">Site Coordinates:</xsl:when>
          <xsl:otherwise>Bounding Coordinates:</xsl:otherwise>
        </xsl:choose>
      </td>
      <td>
        <xsl:choose>
          <xsl:when test="$lat-lon-identical = 'true' ">
            <xsl:call-template name="boundingCoordinatesSingleLatLon"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="boundingCoordinatesBox"/>
          </xsl:otherwise>
        </xsl:choose>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="boundingCoordinatesSingleLatLon">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: boundingCoordinatesSingleLatLon</xsl:text></xsl:message></xsl:if>
    <table>
      <tr> 
        <td class="{$firstColStyle}"><xsl:text>Longitude (degree):&#160;</xsl:text></td>
        <td><xsl:value-of select="westBoundingCoordinate"/></td>
        <td class="{$firstColStyle}"><xsl:text>Latitude (degree):&#160;</xsl:text></td>
        <td><xsl:value-of select="northBoundingCoordinate"/></td>
      </tr>
      <xsl:apply-templates select="boundingAltitudes"/>
    </table>
  </xsl:template>
  
  <xsl:template name="boundingCoordinatesBox">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: boundingCoordinatesBox</xsl:text></xsl:message></xsl:if>
    <table>
      <tr>
        <td class="{$firstColStyle}"><xsl:text>Northern: &#160;</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="northBoundingCoordinate"/> </td>
        <td class="{$firstColStyle}"><xsl:text>Southern: &#160;</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="southBoundingCoordinate"/></td>
      </tr>
      <tr>
        <td class="{$firstColStyle}"><xsl:text>Western: &#160;</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="westBoundingCoordinate"/></td>
        <td class="{$firstColStyle}"><xsl:text>Eastern: &#160;</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="eastBoundingCoordinate"/></td>
      </tr>
      <xsl:apply-templates select="boundingAltitudes"/>
    </table>
  </xsl:template>

  <!-- 
  <xsl:template match="westBoundingCoordinate">
   <td class="{$firstColStyle}"><xsl:text>Western: &#160;</xsl:text></td>
   <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
  </xsl:template>
  
  <xsl:template match="eastBoundingCoordinate">
    <td class="{$firstColStyle}"><xsl:text>Eastern: &#160;</xsl:text></td>
    <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
  </xsl:template>
  
  <xsl:template match="northBoundingCoordinate">  
    <td class="{$firstColStyle}"><xsl:text>Northern: &#160;</xsl:text></td>
    <td class="{$secondColStyle}"><xsl:value-of select="."/> </td>
  </xsl:template>
  
  <xsl:template match="southBoundingCoordinate">
    <td class="{$firstColStyle}"><xsl:text>Southern: &#160;</xsl:text></td>
     <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
  </xsl:template>
  -->

  <xsl:template match="boundingAltitudes">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: boundingAltitudes</xsl:text></xsl:message></xsl:if>
    <xsl:variable name="altitude-minimum" select="./altitudeMinimum"/>
    <xsl:variable name="altitude-maximum" select="./altitudeMaximum"/>
    <xsl:variable name="min-max-identical">
      <xsl:choose>
        <xsl:when test="$altitude-minimum = $altitude-maximum">true</xsl:when>
        <xsl:otherwise>false</xsl:otherwise>
      </xsl:choose>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$min-max-identical = 'true' ">
        <td class="{$firstColStyle}">Altitude (<xsl:value-of select="altitudeUnits"/>):</td>
        <td class="{$secondColStyle}"><xsl:value-of select="altitudeMinimum"/></td>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td class="{$firstColStyle}">Altitude Minimum:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="altitudeMinimum"/></td>
          <td class="${firstColStyle}">Altitude Maximum:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="altitudeMaximum"/></td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- 
  <xsl:template match="altitudeMinimum">
     <xsl:value-of select="."/> &#160;<xsl:value-of select="../altitudeUnits"/>
  </xsl:template>

  <xsl:template match="altitudeMaximum">
    <xsl:value-of select="."/> &#160;<xsl:value-of select="../altitudeUnits"/>
  </xsl:template>
  -->
  
  <xsl:template match="datasetGPolygonOuterGRing">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetGPolygonOuterGRing</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>G-Ploygon(Outer Ring): </xsl:text></td>
      <td class="{$secondColStyle}">
        <xsl:apply-templates select="gRingPoint"/>
        <xsl:apply-templates select="gRing"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="datasetGPolygonExclusionGRing">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datasetGPolygonExclusionGRing</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>G-Ploygon(Exclusion Ring): </xsl:text></td>
      <td class="{$secondColStyle}">
        <xsl:apply-templates select="gRingPoint"/>
        <xsl:apply-templates select="gRing"/>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="gRing">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: gRing</xsl:text></xsl:message></xsl:if>
    <xsl:text>(GRing) &#160;</xsl:text>
    <xsl:text>Latitude: </xsl:text>
    <xsl:value-of select="gRingLatitude"/>,
    <xsl:text>Longitude: </xsl:text>
    <xsl:value-of select="gRingLongitude"/><br/>
  </xsl:template>

  <xsl:template match="gRingPoint">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: gRingPoint</xsl:text></xsl:message></xsl:if>
    <xsl:text>Latitude: </xsl:text>
    <xsl:value-of select="gRingLatitude"/>,
    <xsl:text>Longitude: </xsl:text>
    <xsl:value-of select="gRingLongitude"/><br/>
  </xsl:template>

  <!-- ****************  T E M P O R A L   C O V E R A G E  **************** -->
  <xsl:template name="temporalCoverage">
    <xsl:param name="firstColStyle" ></xsl:param>
    <xsl:param name="secondColStyle" ></xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: temporalCoverage</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <table class="subgroup onehundred_percent">
            <xsl:call-template name="temporalCovCommon" />
          </table>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <table class="subgroup onehundred_percent">
            <xsl:call-template name="temporalCovCommon" />
          </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="temporalCovCommon">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: temporalCovCommon</xsl:text></xsl:message></xsl:if>
    <tr>
      <th colspan="2"><br/><xsl:text>Time Period</xsl:text></th>
    </tr>
    <xsl:apply-templates select="singleDateTime"/>
    <xsl:apply-templates select="rangeOfDates"/>
  </xsl:template>

  <xsl:template match="singleDateTime">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: singleDateTime</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Date:</td>
      <td><xsl:call-template name="singleDateType" /></td>
    </tr>
  </xsl:template>

  <xsl:template match="rangeOfDates">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: rangeOfDates</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Begin:</td>
      <td><xsl:apply-templates select="beginDate"/></td>
    </tr>
    <tr>
      <td class="{$firstColStyle}">End:</td>
      <td><xsl:apply-templates select="endDate"/></td>
    </tr>
  </xsl:template>

  <xsl:template match="beginDate">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: beginDate</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="singleDateType"/>
  </xsl:template>

  <xsl:template match="endDate">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: endDate</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="singleDateType"/>
  </xsl:template>

  <xsl:template name="singleDateType">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: singleDateType</xsl:text></xsl:message></xsl:if>
    <table>
      <xsl:if test="calendarDate">
        <tr>
          <td colspan="2" class="{$secondColStyle}">
            <xsl:value-of select="calendarDate"/>
            <xsl:if test="./time and normalize-space(./time)!=''">
              <xsl:text>&#160; at &#160;</xsl:text><xsl:apply-templates select="time"/>
            </xsl:if>
          </td>
        </tr>
      </xsl:if>
      <xsl:if test="alternativeTimeScale">
        <xsl:apply-templates select="alternativeTimeScale"/>
      </xsl:if>
    </table>
  </xsl:template>

  <xsl:template match="alternativeTimeScale">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: alternativeTimeScale</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Timescale:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="timeScaleName"/></td>
    </tr>
    <tr>
      <td class="{$firstColStyle}">Time estimate:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="timeScaleAgeEstimate"/></td>
    </tr>
    <xsl:if test="timeScaleAgeUncertainty and normalize-space(timeScaleAgeUncertainty) != ''">
      <tr>
        <td class="{$firstColStyle}">Time uncertainty:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="timeScaleAgeUncertainty"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="timeScaleAgeExplanation and normalize-space(timeScaleAgeExplanation) != ''">
      <tr>
        <td class="{$firstColStyle}">Time explanation:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="timeScaleAgeExplanation"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="timeScaleCitation and normalize-space(timeScaleCitation) != ''">
      <tr>
        <td class="{$firstColStyle}">Citation:</td>
        <td class="{$secondColStyle}"><xsl:apply-templates select="timeScaleCitation"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="timeScaleCitation">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: timeScaleCitation</xsl:text></xsl:message></xsl:if>
    <!-- Using citation module here -->
     <xsl:call-template name="citation">
       <xsl:with-param name="citationfirstColStyle" select="$firstColStyle"/>
       <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
     </xsl:call-template>
  </xsl:template>

  <!-- ***************  T A X O N O M I C   C O V E R A G E  *************** -->
  <xsl:template name="taxonomicCoverage">
    <xsl:param name="firstColStyle" ></xsl:param>
    <xsl:param name="secondColStyle" ></xsl:param>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <table class="{$tabledefaultStyle}">
            <xsl:call-template name="taxonomicCovCommon" />
          </table>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <table class="{$tabledefaultStyle}">
          <xsl:call-template name="taxonomicCovCommon" />
        </table>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="taxonomicCovCommon">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonomicCovCommon</xsl:text></xsl:message></xsl:if>
    <tr>
      <th colspan="2"><xsl:text>Taxonomic Range:</xsl:text></th>
    </tr>
    <xsl:apply-templates select="taxonomicSystem"/>
    <xsl:apply-templates select="generalTaxonomicCoverage"/>
    <xsl:for-each select="taxonomicClassification">
      <xsl:apply-templates select="."/>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="taxonomicSystem">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonomicSystem</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Taxonomic System:</xsl:text></td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates select="./*"/>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="classificationSystem">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: classificationSystem</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="classificationSystemCitation">
        <tr><td class="{$firstColStyle}">Classification Citation:</td>
          <td>
           <xsl:call-template name="citation">
             <xsl:with-param name="citationfirstColStyle" select="$firstColStyle"/>
             <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
           </xsl:call-template>
         </td>
        </tr>
     </xsl:for-each>
     <xsl:if test="classificationSystemModifications and normalize-space(classificationSystemModifications)!=''">
      <tr><td class="{$firstColStyle}">Modification:</td>
        <td class="{$secondColStyle}">
          <xsl:value-of select="classificationSystemModifications"/>
        </td>
      </tr>
     </xsl:if>
  </xsl:template>

  <xsl:template match="identificationReference">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: identificationReference</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$firstColStyle}">ID Reference:</td>
          <td>
             <xsl:call-template name="citation">
                <xsl:with-param name="citationfirstColStyle" select="$firstColStyle"/>
                <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
             </xsl:call-template>
          </td>
     </tr>
  </xsl:template>

  <xsl:template match="identifierName">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: identifierName</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$firstColStyle}">ID Name:</td>
          <td>
             <xsl:call-template name="party">
               <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
             </xsl:call-template>
          </td>
      </tr>
  </xsl:template>

  <xsl:template match="taxonomicProcedures">
    <tr><td class="{$firstColStyle}">
        <xsl:text>Procedures:</xsl:text></td><td class="{$secondColStyle}">
        <xsl:value-of select="."/></td></tr>
  </xsl:template>

  <xsl:template match="taxonomicCompleteness">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonomicCompleteness</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Completeness:</xsl:text></td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="vouchers">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: vouchers</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Vouchers:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates select="specimen"/>
          <xsl:apply-templates select="repository"/>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="specimen">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: specimen</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Specimen:</xsl:text></td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="repository">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: repository</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Repository:</td>
      <td>
        <xsl:for-each select="originator">
          <xsl:call-template name="party">
            <xsl:with-param name="partyfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="generalTaxonomicCoverage">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: generalTaxonomicCoverage</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$firstColStyle}">
             <xsl:text>General Coverage:</xsl:text></td>
           <td class="{$secondColStyle}">
             <xsl:value-of select="."/>
          </td>
      </tr>
  </xsl:template>

  <xsl:template match="taxonomicClassification">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonomicClassification</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Classification:</xsl:text></td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates select="./*" mode="nest"/>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="taxonRankName" mode="nest" >
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonRankName</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Rank Name:</xsl:text></td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="taxonRankValue" mode="nest">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonRankValue</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Rank Value:</xsl:text></td>
      <td class="{$secondColStyle}"><i><xsl:value-of select="."/></i></td>
    </tr>
  </xsl:template>

  <xsl:template match="commonName" mode="nest">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: commonName</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Common Name:</xsl:text></td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="taxonomicClassification" mode="nest">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: taxonomicClassification</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}"><xsl:text>Classification:</xsl:text></td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates select="./*" mode="nest"/>
        </table>
      </td>
    </tr>
  </xsl:template>

  <!-- This module is for datatable module-->
  <!-- change the "Entity Description" and "Identifier" to a complete 
       citation for dataset. in eml.xsl -->
  <xsl:template name="dataTable">
    <xsl:param name="datatablefirstColStyle"/>
    <xsl:param name="datatablesubHeaderStyle"/>
    <xsl:param name="docid"/> 
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <!-- mob added this -->
    <xsl:param name="numberOfColumns">
      <xsl:if test="$withAttributes='1'"> 
        <xsl:value-of select="count(attributeList/attribute/attributeName)"/>
      </xsl:if>
    </xsl:param>  
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: dataTable</xsl:text></xsl:message></xsl:if>
    <hr></hr>
    <h2>Data Table</h2>
    <table class="subGroup onehundred_percent">
      <tr>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:choose>
              <xsl:when test="references!=''">
                <xsl:variable name="ref_id" select="references"/>
                <xsl:variable name="references" select="$ids[@id=$ref_id]" />
                <xsl:for-each select="$references">
                  <xsl:call-template name="datatablecommon">
                    <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
                    <xsl:with-param name="datatablesubHeaderStyle" select="$datatablesubHeaderStyle"/>
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entityindex" select="$entityindex"/>
                    <xsl:with-param name="numberOfColumns" select="$numberOfColumns"/>
                  </xsl:call-template>
                </xsl:for-each>
              </xsl:when>
              <xsl:otherwise>
                <xsl:call-template name="datatablecommon">
                  <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
                  <xsl:with-param name="datatablesubHeaderStyle" select="$datatablesubHeaderStyle"/>
                  <xsl:with-param name="docid" select="$docid"/>
                  <xsl:with-param name="entityindex" select="$entityindex"/>
                  <xsl:with-param name="numberOfColumns" select="$numberOfColumns"/>
                </xsl:call-template>
              </xsl:otherwise>
            </xsl:choose>
          </table>
        </td>
      </tr>
      <tr>
        <td>
          <table class="{$tabledefaultStyle}">
            <!-- moved this out of datatablecommon, to break up linear arrangment  -->
            <xsl:if test="physical">
              <tr>
                <th colspan="2"><br/>Table Structure</th>
              </tr>
              <!-- distrubution is still under datatablecommon 
              <xsl:for-each select="physical">
                <xsl:call-template name="showdistribution">
                  <xsl:with-param name="docid" select="$docid"/>
                  <xsl:with-param name="entityindex" select="$entityindex"/>
                  <xsl:with-param name="physicalindex" select="position()"/>
                  <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
                  <xsl:with-param name="datatablesubHeaderStyle" select="$datatablesubHeaderStyle"/>
                </xsl:call-template>
              </xsl:for-each>-->
            </xsl:if>
            <xsl:for-each select="physical">
              <tr>
                <td colspan="2">
                  <xsl:call-template name="physical">
                    <xsl:with-param name="physicalfirstColStyle" select="$datatablefirstColStyle"/>
                    <xsl:with-param name="notshowdistribution">yes</xsl:with-param>
                  </xsl:call-template>
                </td>
              </tr>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </table>
    <!-- a second table for the attributeList -->
    <table class="{$tabledefaultStyle}">
      <tr>
        <th colspan="2"><br/>Table Column Descriptions</th>
      </tr>
      <tr>
        <td>
          <xsl:if test="$withAttributes='1'">
            <xsl:for-each select="attributeList">
              <xsl:call-template name="datatableattributeList">
                <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
                <xsl:with-param name="datatablesubHeaderStyle" select="$datatablesubHeaderStyle"/>
                <xsl:with-param name="docid" select="$docid"/>
                <xsl:with-param name="entityindex" select="$entityindex"/>
              </xsl:call-template>
            </xsl:for-each>
          </xsl:if>
        </td>
      </tr>  
    </table>
  </xsl:template>

  <xsl:template name="datatablecommon">
    <xsl:param name="datatablefirstColStyle"/>
    <xsl:param name="datatablesubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="numberOfColumns"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datatablecommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="physical">
      <xsl:call-template name="showdistribution">
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
        <xsl:with-param name="physicalindex" select="position()"/>
        <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
        <xsl:with-param name="datatablesubHeaderStyle" select="$datatablesubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>   
    <!-- only one entityName is allowed, so is foreach superfluous?  -->
    <xsl:for-each select="entityName">
       <xsl:call-template name="entityName">
          <xsl:with-param name="entityfirstColStyle" select="$datatablefirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="alternateIdentifier">
       <xsl:call-template name="entityalternateIdentifier">
          <xsl:with-param name="entityfirstColStyle" select="$datatablefirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityDescription">
       <xsl:call-template name="entityDescription">
          <xsl:with-param name="entityfirstColStyle" select="$datatablefirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="additionalInfo">
       <xsl:call-template name="entityadditionalInfo">
          <xsl:with-param name="entityfirstColStyle" select="$datatablefirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each> 
    <xsl:for-each select="numberOfRecords">
       <xsl:call-template name="datatablenumberOfRecords">
          <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <!-- show the number of columns, too -->
    <xsl:call-template name="datatablenumberOfColumns">
      <xsl:with-param name="datatablefirstColStyle" select="$datatablefirstColStyle"/>
      <xsl:with-param name="numberOfColumns" select="$numberOfColumns"/>
    </xsl:call-template>
    <xsl:if test="coverage">
       <tr>
         <td class="{$datatablesubHeaderStyle}" colspan="2"><!-- label removed by mob, 16apr2010 Coverage Description: --></td>
       </tr>
    </xsl:if>
    <xsl:for-each select="coverage">
      <tr>
        <td colspan="2">
          <xsl:call-template name="coverage"></xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="method | methods">
      <tr>
        <td class="{$datatablesubHeaderStyle}" colspan="2"><!-- label removed by mob, 16apr2010 Method Description: --></td>
      </tr>
    </xsl:if>
    <xsl:for-each select="method | methods">
      <tr>
        <td colspan="2">
          <xsl:call-template name="method">
            <xsl:with-param name="methodfirstColStyle" select="$datatablefirstColStyle"/>
            <xsl:with-param name="methodsubHeaderStyle" select="$datatablesubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="constraint">
       <tr>
         <td class="{$datatablesubHeaderStyle}" colspan="2">Constraint:</td>
       </tr>
    </xsl:if>
    <xsl:for-each select="constraint">
      <tr>
        <td colspan="2">
          <xsl:call-template name="constraint">
            <xsl:with-param name="constraintfirstColStyle" select="$datatablefirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datatablecaseSensitive">
    <xsl:param name="datatablefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datatablecaseSensitive</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$datatablefirstColStyle}">Case Sensitive?</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template name="datatablenumberOfRecords">
    <xsl:param name="datatablefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datatablenumberOfRecords</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$datatablefirstColStyle}">Number of Records:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template name="datatablenumberOfColumns">
     <xsl:param name="numberOfColumns"/>      
     <xsl:param name="datatablefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datatablenumberOfColumns</xsl:text></xsl:message></xsl:if>
    <tr>
       <td class="{$datatablefirstColStyle}">Number of Columns:</td>
       <td class="{$secondColStyle}"><xsl:value-of select="$numberOfColumns"/></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="showdistribution">
    <xsl:param name="datatablefirstColStyle"/>
    <xsl:param name="datatablesubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entitylevel</xsl:param>
    <xsl:param name="entitytype">dataTable</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: showdistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <tr>
        <td colspan="2">
          <xsl:call-template name="distribution">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="physicalindex" select="$physicalindex"/>
            <xsl:with-param name="distributionindex" select="position()"/>
            <xsl:with-param name="disfirstColStyle" select="$datatablefirstColStyle"/>
            <xsl:with-param name="dissubHeaderStyle" select="$datatablesubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="datatableattributeList">
    <xsl:param name="datatablefirstColStyle"/>
    <xsl:param name="datatablesubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype">dataTable</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datatableattributeList</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$datatablesubHeaderStyle}" colspan="2"><!-- <xsl:text>Attribute(s) Info:</xsl:text> --></td>
    </tr>
    <tr>
      <td colspan="2">
        <xsl:call-template name="attributelist">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="access">
    <xsl:param name="accessfirstColStyle"/>
    <xsl:param name="accesssubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: access</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="accessCommon">
              <xsl:with-param name="accessfirstColStyle" select="$accessfirstColStyle"/>
              <xsl:with-param name="accesssubHeaderStyle" select="$accesssubHeaderStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="accessCommon">
            <xsl:with-param name="accessfirstColStyle" select="$accessfirstColStyle"/>
            <xsl:with-param name="accesssubHeaderStyle" select="$accesssubHeaderStyle"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>
  
  <xsl:template name="accessCommon">
    <xsl:param name="accessfirstColStyle" />
    <xsl:param name="accesssubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: accessCommon</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="accesssystem">
      <xsl:with-param name="accessfirstColStyle" select="$accessfirstColStyle"/>
      <!-- <xsl:with-param name="accesssubHeaderStyle" select="$accesssubHeaderStyle"/> -->
    </xsl:call-template>
    <xsl:if test="normalize-space(./@order)='allowFirst' and (allow)">
      <xsl:call-template name="allow_deny">
        <xsl:with-param name="permission" select="'allow'"/>
        <xsl:with-param name="accessfirstColStyle" select="$accessfirstColStyle"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="(deny)">
      <xsl:call-template name="allow_deny">
        <xsl:with-param name="permission" select="'deny'"/>
        <xsl:with-param name="accessfirstColStyle" select="$accessfirstColStyle"/>
      </xsl:call-template>
    </xsl:if>
    <xsl:if test="normalize-space(acl/@order)='denyFirst' and (allow)">
      <xsl:call-template name="allow_deny">
        <xsl:with-param name="permission" select="'allow'"/>
        <xsl:with-param name="accessfirstColStyle" select="$accessfirstColStyle"/>
      </xsl:call-template>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="allow_deny">
    <xsl:param name="permission"/>
    <xsl:param name="accessfirstColStyle" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: allow_deny</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="$permission='allow'">
        <xsl:for-each select="allow">
          <tr>
            <td class="{$accessfirstColStyle}">Allow:</td>
            <td class="{$accessfirstColStyle}">
              <xsl:for-each select="./permission">
                <xsl:text>[</xsl:text><xsl:value-of select="."/><xsl:text>] </xsl:text>
              </xsl:for-each>
            </td>
            <td class="{$accessfirstColStyle}" >
              <xsl:for-each select="principal">
                <xsl:value-of select="."/><br/>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="deny">
          <tr>
            <td class="{$accessfirstColStyle}">Deny:</td>
            <td class="{$accessfirstColStyle}">
              <xsl:for-each select="./permission">
                <xsl:text>[</xsl:text><xsl:value-of select="."/><xsl:text>] </xsl:text>
              </xsl:for-each>
            </td>
            <td class="{$accessfirstColStyle}" >
              <xsl:for-each select="principal">
                <xsl:value-of select="."/><br/>
              </xsl:for-each>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <xsl:template name="accesssystem">
    <xsl:param name="accessfirstColStyle" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: accesssystem</xsl:text></xsl:message></xsl:if>
    <tr>
      <th colspan="3"><xsl:text>Access Control:</xsl:text></th>
    </tr>
    <tr>
      <td class="{$accessfirstColStyle}">Auth System:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="./@authSystem"/></td>
    </tr>
    <tr>
      <td class="{$accessfirstColStyle}">Order:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="./@order"/></td>
    </tr>
  </xsl:template>
  
  <!-- Used for additional metadata.
  
  ASCII XML Tree Viewer 1.0 (13 Feb 2001)
  An XPath/XSLT visualisation tool for XML documents

  Written by Jeni Tennison and Mike J. Brown
  No license; use freely, but please credit the authors if republishing elsewhere.

  Use this stylesheet to produce an ASCII art representation of an XML document's
  node tree, as exposed by the XML parser and interpreted by the XSLT processor.
  Note that the parser may not expose comments to the XSLT processor.

  Usage notes
  ===========
  By default, this stylesheet will not show namespace nodes. If the XSLT processor
  supports the namespace axis and you want to see namespace nodes, just pass a
  non-empty "show_ns" parameter to the stylesheet. Example using Instant Saxon:

    saxon somefile.xml ascii-treeview.xsl show_ns=yes

  If you want to ignore whitespace-only text nodes, uncomment the xsl:strip-space
  instruction below.
-->
  
  <!-- uncomment the following to ignore whitespace-only text nodes -->
  <!-- xsl:strip-space elements="*" -->

  <!-- pass a non-empty show_ns parameter to the stylesheet to show namespace nodes -->
  <xsl:param name="show_ns"/>

  <xsl:variable name="apos">'</xsl:variable>

  <xsl:template name="additionalmetadata">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: additionalmetadata</xsl:text></xsl:message></xsl:if>
    <h3 class="toggleButton"><button>+/-</button> Additional Metadata</h3>
    <div class="collapsible">
<pre>
   <xsl:text>additionalMetadata&#xA;</xsl:text>
   <xsl:apply-templates mode="ascii-art" />
</pre>
    </div>
  </xsl:template>

  <xsl:template match="*" mode="ascii-art">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: *; mode: ascii-art</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="ascii-art-hierarchy" />
    <xsl:text />___element '<xsl:value-of select="local-name()" />'<xsl:text />
    <xsl:if test="namespace-uri()"> in ns '<xsl:value-of select="namespace-uri()"/>' ('<xsl:value-of select="name()"/>')</xsl:if>
    <xsl:text>&#xA;</xsl:text>
    <xsl:apply-templates select="@*" mode="ascii-art" />
    <xsl:if test="$show_ns">
      <xsl:for-each select="namespace::*">
        <xsl:call-template name="ascii-art-hierarchy" />
        <xsl:text />  \___namespace '<xsl:value-of select="name()" />' = '<xsl:value-of select="." />'&#xA;<xsl:text />
      </xsl:for-each>
    </xsl:if>
    <xsl:apply-templates mode="ascii-art" />
  </xsl:template>

  <xsl:template match="@*" mode="ascii-art">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: @*; mode: ascii-art</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="ascii-art-hierarchy" />
    <xsl:text />  \___attribute '<xsl:value-of select="local-name()" />'<xsl:text />
    <xsl:if test="namespace-uri()"> in ns '<xsl:value-of select="namespace-uri()"/>' ('<xsl:value-of select="name()"/>')</xsl:if>
    <xsl:text /> = '<xsl:text />
    <xsl:call-template name="escape-ws">
      <xsl:with-param name="text" select="." />
    </xsl:call-template>
    <xsl:text />'&#xA;<xsl:text />
  </xsl:template>

  <xsl:template match="text()" mode="ascii-art">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: text(); mode: ascii-art</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="ascii-art-hierarchy" />
    <xsl:text>___text '</xsl:text>
    <xsl:call-template name="escape-ws">
      <xsl:with-param name="text" select="." />
    </xsl:call-template>
    <xsl:text>'&#xA;</xsl:text>
  </xsl:template>

  <xsl:template match="comment()" mode="ascii-art">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: comment()</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="ascii-art-hierarchy" />
    <xsl:text />___comment '<xsl:value-of select="." />'&#xA;<xsl:text />
  </xsl:template>

  <xsl:template match="processing-instruction()" mode="ascii-art">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: processing-instruction()</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="ascii-art-hierarchy" />
    <xsl:text />___processing instruction target='<xsl:value-of select="name()" />' instruction='<xsl:value-of select="." />'&#xA;<xsl:text />
  </xsl:template>

  <xsl:template name="ascii-art-hierarchy">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: ascii-art-hierarchy</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="ancestor::*">
      <xsl:choose>
        <xsl:when test="local-name() != 'additionalMetadata'">
          <xsl:choose>
            <xsl:when test="following-sibling::node()">  |   </xsl:when>
            <xsl:otherwise><xsl:text>      </xsl:text></xsl:otherwise>
          </xsl:choose>
        </xsl:when>
      </xsl:choose>
    </xsl:for-each>
    <xsl:choose>
        <xsl:when test="parent::node() and ../child::node()">  |</xsl:when>
        <xsl:otherwise><xsl:text>   </xsl:text></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- recursive template to escape backslashes, apostrophes, newlines and tabs -->
  <xsl:template name="escape-ws">
    <xsl:param name="text" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: escape-ws</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
        <xsl:when test="contains($text, '\')">
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-before($text, '\')" />
            </xsl:call-template>
            <xsl:text>\\</xsl:text>
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-after($text, '\')" />
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="contains($text, $apos)">
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-before($text, $apos)" />
            </xsl:call-template>
            <xsl:text>\'</xsl:text>
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-after($text, $apos)" />
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="contains($text, '&#xA;')">
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-before($text, '&#xA;')" />
            </xsl:call-template>
            <xsl:text>\n</xsl:text>
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-after($text, '&#xA;')" />
            </xsl:call-template>
        </xsl:when>
        <xsl:when test="contains($text, '&#x9;')">
            <xsl:value-of select="substring-before($text, '&#x9;')" />
            <xsl:text>\t</xsl:text>
            <xsl:call-template name="escape-ws">
                <xsl:with-param name="text" select="substring-after($text, '&#x9;')" />
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise><xsl:value-of select="$text" /></xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- eml-attribute-2.0.0.xsl -->
  <xsl:template name="attributelist">
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributelist</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
          <xsl:for-each select="$references">
            <xsl:call-template name="attributecommon">
              <xsl:with-param name="docid" select="$docid"/>
              <xsl:with-param name="entitytype" select="$entitytype"/>
              <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="attributecommon">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="attributecommon">
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributecommon</xsl:text></xsl:message></xsl:if>
    <!-- First row for headers (attributeLabel) the pretty one. Element is optional,
         so could be empty. -->
    <!-- upper left cell has nbsp -->
    <tr>
      <th>&#160;</th>
      <xsl:for-each select="attribute">
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <th><xsl:value-of select="attributeLabel"/></th>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <th><xsl:value-of select="attributeLabel"/></th>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- Second row for attribute label, the ugly one, element required -->
    <tr>
      <td class="rowodd">Column Name:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="attributeName!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="attributeName">
                      <xsl:value-of select="."/> &#160;<br/>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160;<br/>
                  </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="attributeName!=''">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="attributeName">
                    <xsl:value-of select="."/> &#160;<br/>
                  </xsl:for-each>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160;<br/>
                </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- Third row for attribute defination-->
    <tr>
      <td class="rowodd">Definition:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <td colspan="1" align="center" class="{$stripes}"><xsl:value-of select="attributeDefinition"/></td>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <td colspan="1" align="center" class="{$stripes}"><xsl:value-of select="attributeDefinition"/></td>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The fourth row for attribute storage type-->
    <tr>
      <td class="rowodd">Storage Type:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="storageType!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="storageType">
                      <xsl:value-of select="."/> &#160;<br/>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="storageType!=''">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="storageType">
                    <xsl:value-of select="."/> &#160;<br/>
                  </xsl:for-each>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The fifth row for meaturement type-->
    <tr>
      <td class="rowodd">Measurement Type:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <td colspan="1" align="center" class="{$stripes}">
                <xsl:for-each select="measurementScale">
                  <xsl:value-of select="local-name(./*)"/>
                </xsl:for-each>
              </td>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <td colspan="1" align="center" class="{$stripes}">
              <xsl:for-each select="measurementScale">
                <xsl:value-of select="local-name(./*)"/>
              </xsl:for-each>
            </td>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The sixth row for meaturement domain-->
    <tr>
   <!--    <th class="rowodd">Description of Allowed Values</th>-->
      <td class="rowodd">Measurement Values Domain:</td> 
      <xsl:for-each select="attribute">
        <!-- mob added, pass this index to measurementscale, not position. should move this earlier? -->
        <xsl:variable name="attributeindex" select="position()"/>
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="innerstripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$innercolevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$innercoloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <td colspan="1" align="center" class="{$stripes}">
                <xsl:for-each select="measurementScale">
                  <xsl:call-template name="measurementscale">
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="$entitytype"/>
                    <xsl:with-param name="entityindex" select="$entityindex"/>
                    <xsl:with-param name="attributeindex" select="$attributeindex"/>
                    <xsl:with-param name="stripes" select="$innerstripes"/>
                  </xsl:call-template>
                </xsl:for-each>
              </td>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <td colspan="1" align="center" class="{$stripes}">
              <xsl:for-each select="measurementScale">
                <xsl:call-template name="measurementscale">
                  <xsl:with-param name="docid" select="$docid"/>
                  <xsl:with-param name="entitytype" select="$entitytype"/>
                  <xsl:with-param name="entityindex" select="$entityindex"/>
                  <xsl:with-param name="attributeindex" select="$attributeindex"/>
                  <xsl:with-param name="stripes" select="$innerstripes"/>
                </xsl:call-template>
              </xsl:for-each>
            </td>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The seventh row for missing value code-->
    <tr>
      <td class="rowodd">Missing Value Code:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="innerstripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$innercolevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$innercoloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="missingValueCode!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <table>
                      <xsl:for-each select="missingValueCode">
                        <tr>
                          <td class="{$innerstripes}"><b>Code</b></td>
                          <td class="{$innerstripes}"><xsl:value-of select="code"/></td>
                        </tr>
                        <tr>
                          <td class="{$innerstripes}"><b>Expl</b></td>
                          <td class="{$innerstripes}"><xsl:value-of select="codeExplanation"/></td>
                        </tr>
                      </xsl:for-each>
                    </table>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="missingValueCode!=''">
                <td colspan="1" align="center" class="{$stripes}">
                  <table>
                    <xsl:for-each select="missingValueCode">
                      <tr>
                        <td class="{$innerstripes}"><b>Code</b></td>
                        <td class="{$innerstripes}"><xsl:value-of select="code"/></td>
                      </tr>
                      <tr>
                        <td class="{$innerstripes}"><b>Expl</b></td>
                        <td class="{$innerstripes}"><xsl:value-of select="codeExplanation"/></td>
                      </tr>
                    </xsl:for-each>
                  </table>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The eighth row for accuracy report-->
    <tr>
      <td class="rowodd">Accuracy Report:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="accuracy!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="accuracy">
                      <xsl:value-of select="attributeAccuracyReport"/>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="accuracy!=''">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="accuracy">
                    <xsl:value-of select="attributeAccuracyReport"/>
                  </xsl:for-each>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The ninth row for quality accuracy accessment -->
    <tr>
      <td class="rowodd">Accuracy Assessment:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:variable name="innerstripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$innercolevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$innercoloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="accuracy/quantitativeAttributeAccuracyAssessment!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="accuracy">
                      <table>
                        <xsl:for-each select="quantitativeAttributeAccuracyAssessment">
                          <tr>
                            <td class="{$innerstripes}"><b>Value</b></td>
                            <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyValue"/></td>
                          </tr>
                          <tr>
                            <td class="{$innerstripes}"><b>Expl</b></td>
                            <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyExplanation"/></td>
                          </tr>
                        </xsl:for-each>
                      </table>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="accuracy/quantitativeAttributeAccuracyAssessment!=''">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="accuracy">
                    <table>
                      <xsl:for-each select="quantitativeAttributeAccuracyAssessment">
                        <tr>
                          <td class="{$innerstripes}"><b>Value</b></td>
                          <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyValue"/></td>
                        </tr>
                        <tr>
                          <td class="{$innerstripes}"><b>Expl</b></td>
                          <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyExplanation"/></td>
                        </tr>
                      </xsl:for-each>
                    </table>
                  </xsl:for-each>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The tenth row for coverage-->
    <tr>
      <td class="rowodd">Coverage:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="index" select="position()"/>
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="coverage!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="coverage">
                      <xsl:call-template name="attributecoverage">
                        <xsl:with-param name="docid" select="$docid"/>
                        <xsl:with-param name="entitytype" select="$entitytype"/>
                        <xsl:with-param name="entityindex" select="$entityindex"/>
                        <xsl:with-param name="attributeindex" select="$index"/>
                      </xsl:call-template>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise>
            <xsl:choose>
              <xsl:when test="coverage!=''">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="coverage">
                    <xsl:call-template name="attributecoverage">
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="$entitytype"/>
                      <xsl:with-param name="entityindex" select="$entityindex"/>
                      <xsl:with-param name="attributeindex" select="$index"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
    <!-- The eleventh row for method-->
    <tr>
      <td class="rowodd">Methods:</td>
      <xsl:for-each select="attribute">
        <xsl:variable name="index" select="position()"/>
        <xsl:variable name="stripes">
          <xsl:choose>
            <xsl:when test="position() mod 2 = 0">
              <xsl:value-of select="$colevenStyle"/>
            </xsl:when>
            <xsl:when test="position() mod 2 = 1">
              <xsl:value-of select="$coloddStyle"/>
            </xsl:when>
          </xsl:choose>
        </xsl:variable>
        <xsl:choose>
          <xsl:when test="references!=''">
            <xsl:variable name="ref_id" select="references"/>
            <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
            <xsl:for-each select="$references">
              <xsl:choose>
                <xsl:when test="method!='' or methods!=''"> <!-- another mob kludge for eml 2.1 -->
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="method | methods">                      
                      <xsl:call-template name="attributemethod">
                        <xsl:with-param name="docid" select="$docid"/>
                        <xsl:with-param name="entitytype" select="$entitytype"/>
                        <xsl:with-param name="entityindex" select="$entityindex"/>
                        <xsl:with-param name="attributeindex" select="$index"/>
                      </xsl:call-template>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:for-each>
          </xsl:when>
          <xsl:otherwise> 
            <!-- Most SBC datasets end up here. No references, so test method!='' -->
            <!-- gotcha! in a test, using pipe '|' for 'or'  does not work for testing for content. but
             ONLY the '|' works in a select! ! ouch! 
             Also works: when test="method | methods", but used test-for-content for consistency.
            -->
            <xsl:choose> 
              <xsl:when test="method!='' or methods!='' "> <!-- another mob kludge for eml 2.1 -->
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="method | methods">
                    <xsl:call-template name="attributemethod">
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="$entitytype"/>
                      <xsl:with-param name="entityindex" select="$entityindex"/>
                      <xsl:with-param name="attributeindex" select="$index"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </td>
              </xsl:when>
              <xsl:otherwise>
                <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
              </xsl:otherwise>
            </xsl:choose>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </tr>
  </xsl:template>

  <xsl:template name="singleattribute">
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: singleattribute</xsl:text></xsl:message></xsl:if>
    <table class="{$tableattributeStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
          <xsl:for-each select="$references">
            <xsl:call-template name="singleattributecommon">
              <xsl:with-param name="docid" select="$docid"/>
              <xsl:with-param name="entitytype" select="$entitytype"/>
              <xsl:with-param name="entityindex" select="$entityindex"/>
              <xsl:with-param name="attributeindex" select="$attributeindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="singleattributecommon">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="attributeindex" select="$attributeindex"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="singleattributecommon">
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: singleattributecommon</xsl:text></xsl:message></xsl:if>
    <!-- First row for attribute name-->
    <tr>
      <th class="rowodd">Column Name</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <th><xsl:value-of select="attributeName"/></th>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <th><xsl:value-of select="attributeName"/></th>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- Second row for attribute label-->
    <tr>
      <th class="rowodd">Column Label</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="attributeLabel!=''">
                    <td colspan="1" align="center" class="{$stripes}">
                      <xsl:for-each select="attributeLabel">
                        <xsl:value-of select="."/> &#160;<br/>
                      </xsl:for-each>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" align="center" class="{$stripes}"> &#160;<br/></td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="attributeLabel!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="attributeLabel">
                      <xsl:value-of select="."/> &#160;<br/>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160;<br/></td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- Third row for attribute defination-->
    <tr>
      <th class="rowodd">Definition</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <td colspan="1" align="center" class="{$stripes}"><xsl:value-of select="attributeDefinition"/></td>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <td colspan="1" align="center" class="{$stripes}"><xsl:value-of select="attributeDefinition"/></td>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The fourth row for attribute storage type-->
    <tr>
      <th class="rowodd">Type of Value</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="storageType!=''">
                    <td colspan="1" align="center" class="{$stripes}">
                      <xsl:for-each select="storageType">
                        <xsl:value-of select="."/> &#160;<br/>
                      </xsl:for-each>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="storageType!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="storageType">
                      <xsl:value-of select="."/> &#160;<br/>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The fifth row for meaturement type-->
    <tr>
      <th class="rowodd">Measurement Type</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="measurementScale">
                    <xsl:value-of select="local-name(./*)"/>
                  </xsl:for-each>
                </td>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <td colspan="1" align="center" class="{$stripes}">
                <xsl:for-each select="measurementScale">
                  <xsl:value-of select="local-name(./*)"/>
                </xsl:for-each>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The sixth row for meaturement domain-->
    <tr>
      <th class="rowodd">Measurement Domain</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="innerstripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$innercolevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$innercoloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <td colspan="1" align="center" class="{$stripes}">
                  <xsl:for-each select="measurementScale">
                    <xsl:call-template name="measurementscale">
                      <xsl:with-param name="docid" select="$docid"/>
                      <xsl:with-param name="entitytype" select="$entitytype"/>
                      <xsl:with-param name="entityindex" select="$entityindex"/>
                      <xsl:with-param name="attributeindex" select="position()"/>
                      <xsl:with-param name="stripes" select="$innerstripes"/>
                    </xsl:call-template>
                  </xsl:for-each>
                </td>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <td colspan="1" align="center" class="{$stripes}">
                <xsl:for-each select="measurementScale">
                  <xsl:call-template name="measurementscale">
                    <xsl:with-param name="docid" select="$docid"/>
                    <xsl:with-param name="entitytype" select="$entitytype"/>
                    <xsl:with-param name="entityindex" select="$entityindex"/>
                    <xsl:with-param name="attributeindex" select="position()"/>
                    <xsl:with-param name="stripes" select="$innerstripes"/>
                  </xsl:call-template>
                </xsl:for-each>
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The seventh row for missing value code-->
    <tr>
      <th class="rowodd">Missing Value Code</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="innerstripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$innercolevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$innercoloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="missingValueCode!=''">
                    <td colspan="1" align="center" class="{$stripes}">
                      <table>
                        <xsl:for-each select="missingValueCode">
                          <tr>
                            <td class="{$innerstripes}"><b>Code</b></td>
                            <td class="{$innerstripes}"><xsl:value-of select="code"/></td>
                          </tr>
                          <tr>
                            <td class="{$innerstripes}"><b>Expl</b></td>
                            <td class="{$innerstripes}"><xsl:value-of select="codeExplanation"/></td>
                          </tr>
                        </xsl:for-each>
                      </table>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" class="{$stripes}"> &#160; </td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="missingValueCode!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <table>
                      <xsl:for-each select="missingValueCode">
                        <tr>
                          <td class="{$innerstripes}"><b>Code</b></td>
                          <td class="{$innerstripes}"><xsl:value-of select="code"/></td>
                        </tr>
                        <tr>
                          <td class="{$innerstripes}"><b>Expl</b></td>
                          <td class="{$innerstripes}"><xsl:value-of select="codeExplanation"/></td>
                        </tr>
                      </xsl:for-each>
                    </table>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The eighth row for accuracy report-->
    <tr>
      <th class="rowodd">Accuracy Report</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="accuracy!=''">
                    <td colspan="1" align="center" class="{$stripes}">
                      <xsl:for-each select="accuracy">
                        <xsl:value-of select="attributeAccuracyReport"/>
                      </xsl:for-each>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="accuracy!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="accuracy">
                      <xsl:value-of select="attributeAccuracyReport"/>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The nineth row for quality accuracy accessment -->
    <tr>
      <th class="rowodd">Accuracy Assessment</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:variable name="innerstripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$innercolevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$innercoloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="accuracy/quantitativeAttributeAccuracyAssessment!=''">
                    <td colspan="1" align="center" class="{$stripes}">
                      <xsl:for-each select="accuracy">
                        <table>
                          <xsl:for-each select="quantitativeAttributeAccuracyAssessment">
                            <tr>
                              <td class="{$innerstripes}"><b>Value</b></td>
                              <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyValue"/></td>
                            </tr>
                            <tr>
                              <td class="{$innerstripes}"><b>Expl</b></td>
                              <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyExplanation"/></td>
                            </tr>
                          </xsl:for-each>
                        </table>
                      </xsl:for-each>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="accuracy/quantitativeAttributeAccuracyAssessment!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="accuracy">
                      <table>
                        <xsl:for-each select="quantitativeAttributeAccuracyAssessment">
                          <tr>
                            <td class="{$innerstripes}"><b>Value</b></td>
                            <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyValue"/></td>
                          </tr>
                          <tr>
                            <td class="{$innerstripes}"><b>Expl</b></td>
                            <td class="{$innerstripes}"><xsl:value-of select="attributeAccuracyExplanation"/></td>
                          </tr>
                        </xsl:for-each>
                      </table>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The tenth row for coverage-->
    <tr>
      <th class="rowodd">Coverage</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="index" select="position()"/>
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="coverage!=''">
                    <td colspan="1" align="center" class="{$stripes}">
                      <xsl:for-each select="coverage">
                        <xsl:call-template name="attributecoverage">
                          <xsl:with-param name="docid" select="$docid"/>
                          <xsl:with-param name="entitytype" select="$entitytype"/>
                          <xsl:with-param name="entityindex" select="$entityindex"/>
                          <xsl:with-param name="attributeindex" select="$index"/>
                        </xsl:call-template>
                      </xsl:for-each>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="coverage!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="coverage">
                      <xsl:call-template name="attributecoverage">
                        <xsl:with-param name="docid" select="$docid"/>
                        <xsl:with-param name="entitytype" select="$entitytype"/>
                        <xsl:with-param name="entityindex" select="$entityindex"/>
                        <xsl:with-param name="attributeindex" select="$index"/>
                      </xsl:call-template>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
    <!-- The eleventh row for method-->
    <tr>
      <th class="rowodd">Methods</th>
      <xsl:for-each select="attribute">
        <xsl:if test="position() = number($attributeindex)">
          <xsl:variable name="index" select="position()"/>
          <xsl:variable name="stripes">
            <xsl:choose>
              <xsl:when test="position() mod 2 = 0">
                <xsl:value-of select="$colevenStyle"/>
              </xsl:when>
              <xsl:when test="position() mod 2 = 1">
                <xsl:value-of select="$coloddStyle"/>
              </xsl:when>
            </xsl:choose>
          </xsl:variable>
          <xsl:choose>
            <xsl:when test="references!=''">
              <xsl:variable name="ref_id" select="references"/>
              <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
              <xsl:for-each select="$references">
                <xsl:choose>
                  <xsl:when test="method!='' or methods!='' "> <!-- another mob kludge for eml2.1 -->
                    <td colspan="1" align="center" class="{$stripes}">
                      <xsl:for-each select="method | methods">
                        <xsl:call-template name="attributemethod">
                          <xsl:with-param name="docid" select="$docid"/>
                          <xsl:with-param name="entitytype" select="$entitytype"/>
                          <xsl:with-param name="entityindex" select="$entityindex"/>
                          <xsl:with-param name="attributeindex" select="$index"/>
                        </xsl:call-template>
                      </xsl:for-each>
                    </td>
                  </xsl:when>
                  <xsl:otherwise>
                    <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                  </xsl:otherwise>
                </xsl:choose>
              </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
              <xsl:choose>
                <xsl:when test="method!='' or methods!=''">
                  <td colspan="1" align="center" class="{$stripes}">
                    <xsl:for-each select="method | methods">
                      <xsl:call-template name="attributemethod">
                        <xsl:with-param name="docid" select="$docid"/>
                        <xsl:with-param name="entitytype" select="$entitytype"/>
                        <xsl:with-param name="entityindex" select="$entityindex"/>
                        <xsl:with-param name="attributeindex" select="$index"/>
                      </xsl:call-template>
                    </xsl:for-each>
                  </td>
                </xsl:when>
                <xsl:otherwise>
                  <td colspan="1" align="center" class="{$stripes}"> &#160; </td>
                </xsl:otherwise>
              </xsl:choose>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
      </xsl:for-each>
    </tr>
  </xsl:template>

  <xsl:template name="measurementscale">
    <xsl:param name="stripes"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: measurementscale</xsl:text></xsl:message></xsl:if>
    <table>
      <xsl:for-each select="nominal">
        <xsl:call-template name="attributenonnumericdomain">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
          <xsl:with-param name="attributeindex" select="$attributeindex"/>
          <xsl:with-param name="stripes" select="$stripes"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="ordinal">
        <xsl:call-template name="attributenonnumericdomain">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
          <xsl:with-param name="attributeindex" select="$attributeindex"/>
          <xsl:with-param name="stripes" select="$stripes"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="interval">
        <xsl:call-template name="intervalratio">
          <xsl:with-param name="stripes" select="$stripes"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="ratio">
        <xsl:call-template name="intervalratio">
          <xsl:with-param name="stripes" select="$stripes"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="datetime | dateTime">
        <xsl:call-template name="datetime">
          <xsl:with-param name="stripes" select="$stripes"/>
        </xsl:call-template>
      </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template name="attributenonnumericdomain">
    <xsl:param name="stripes"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributenonnumericdomain</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="nonNumericDomain">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
          <xsl:for-each select="$references">
            <xsl:call-template name="attributenonnumericdomaincommon">
              <xsl:with-param name="docid" select="$docid"/>
              <xsl:with-param name="entitytype" select="$entitytype"/>
              <xsl:with-param name="entityindex" select="$entityindex"/>
              <xsl:with-param name="attributeindex" select="$attributeindex"/>
              <xsl:with-param name="stripes" select="$stripes"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="attributenonnumericdomaincommon">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="attributeindex" select="$attributeindex"/>
            <xsl:with-param name="stripes" select="$stripes"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="attributenonnumericdomaincommon">
    <xsl:param name="stripes"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributenonnumericdomaincommon</xsl:text></xsl:message></xsl:if>
    <!-- if numericdomain only has one test domain,
        it will be displayed inline otherwith will be show a link-->
    <xsl:choose>
      <xsl:when test="count(textDomain)=1 and not(enumeratedDomain)">
        <tr>
          <td class="{$stripes}"><strong>Definition</strong></td>
          <td class="{$stripes}"><xsl:value-of select="textDomain/definition"/></td>
        </tr>
        <xsl:if test="textDomain/pattern"> <!-- if added by mob. -->
          <xsl:for-each select="textDomain/pattern">
            <tr>
              <td class="{$stripes}"><strong>Pattern</strong></td>
              <td class="{$stripes}"><xsl:value-of select="."/></td>
            </tr>
          </xsl:for-each>
        </xsl:if>
        <xsl:if test="textDomain/source">  <!-- if added by mob. -->
          <xsl:for-each select=".">
            <tr> 
              <td class="{$stripes}"><strong>Source</strong></td>
              <td class="{$stripes}"><xsl:value-of select="textDomain/source"/></td>
            </tr>
          </xsl:for-each>
        </xsl:if>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td colspan="2" align="center" class="{$stripes}">
          <xsl:call-template name="nonNumericDomain">
              <xsl:with-param name="nondomainfirstColStyle" select="$firstColStyle"/>
          </xsl:call-template>
          </td>
        </tr>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="intervalratio">
    <xsl:param name="stripes"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: intervalratio</xsl:text></xsl:message></xsl:if>
    <xsl:if test="unit/standardUnit">
      <tr>
        <td class="{$stripes}"><b>Unit</b></td>
        <td class="{$stripes}"><xsl:value-of select="unit/standardUnit"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="unit/customUnit">
      <tr>
        <td class="{$stripes}"><b>Unit</b></td>
        <td class="{$stripes}"><xsl:value-of select="unit/customUnit"/></td>
      </tr>
    </xsl:if>
    <xsl:for-each select="precision">
      <tr>
        <td class="{$stripes}"><b>Precision</b></td>
        <td class="{$stripes}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="numericDomain">
      <xsl:call-template name="numericDomain">
        <xsl:with-param name="stripes" select="$stripes"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="numericDomain">
    <xsl:param name="stripes"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: numericDomain</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
        <xsl:for-each select="$references">
          <tr>
            <td class="{$stripes}"><b>Type</b></td>
            <td class="{$stripes}"><xsl:value-of select="numberType"/></td>
          </tr>
          <xsl:for-each select="bounds">
            <tr>
              <td class="{$stripes}"><b>Min</b></td>
              <td class="{$stripes}">
                <xsl:for-each select="minimum">
                  <xsl:value-of select="."/>&#160; </xsl:for-each>
              </td>
            </tr>
            <tr>
              <td class="{$stripes}"><b>Max</b></td>
              <td class="{$stripes}">
                <xsl:for-each select="maximum">
                  <xsl:value-of select="."/>&#160; </xsl:for-each>
              </td>
            </tr>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td class="{$stripes}"><b>Type</b></td>
          <td class="{$stripes}"><xsl:value-of select="numberType"/></td>
        </tr>
        <xsl:for-each select="bounds">
          <tr>
            <td class="{$stripes}"><b>Min</b></td>
            <td class="{$stripes}">
              <xsl:for-each select="minimum">
                <xsl:value-of select="."/>&#160; </xsl:for-each>
            </td>
          </tr>
          <tr>
            <td class="{$stripes}"><b>Max</b></td>
            <td class="{$stripes}">
              <xsl:for-each select="maximum">
                <xsl:value-of select="."/>&#160; </xsl:for-each>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="datetime">
    <xsl:param name="stripes"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: datetime</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$stripes}"><b>Format</b></td>
      <td class="{$stripes}"><xsl:value-of select="formatString"/></td>
    </tr>
    <tr>
      <td class="{$stripes}"><b>Precision</b></td>
      <td class="{$stripes}"><xsl:value-of select="dateTimePrecision"/></td>
    </tr>
    <xsl:call-template name="timedomain"/>
  </xsl:template>

  <xsl:template name="timedomain">
    <xsl:param name="stripes"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: timedomain</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]"/>
        <xsl:for-each select="$references">
          <xsl:for-each select="bounds">
            <tr>
              <td class="{$stripes}"><strong>Min</strong></td>
              <td class="{$stripes}">
                <xsl:for-each select="minimum">
                  <xsl:value-of select="."/>&#160; </xsl:for-each>
              </td>
            </tr>
            <tr>
              <td class="{$stripes}"><strong>Max</strong></td>
              <td class="{$stripes}">
                <xsl:for-each select="maximum">
                  <xsl:value-of select="."/>&#160; </xsl:for-each>
              </td>
            </tr>
          </xsl:for-each>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:for-each select="bounds">
          <tr>
            <td class="{$stripes}"><strong>Min</strong></td>
            <td class="{$stripes}">
              <xsl:for-each select="minimum">
                <xsl:value-of select="."/>&#160; </xsl:for-each>
            </td>
          </tr>
          <tr>
            <td class="{$stripes}"><strong>Max</strong></td>
            <td class="{$stripes}">
              <xsl:for-each select="maximum">
                <xsl:value-of select="."/>&#160; </xsl:for-each>
            </td>
          </tr>
        </xsl:for-each>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="attributecoverage">
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributecoverage</xsl:text></xsl:message></xsl:if>
    <strong>Coverage Info</strong>
  </xsl:template>

  <xsl:template name="attributemethod">
    <xsl:param name="docid"/>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="attributeindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributemethod</xsl:text></xsl:message></xsl:if>
    <strong>Method Info</strong>
  </xsl:template>

  <!-- eml-attribute-enumeratedDomain-2.0.0.xsl -->
   <xsl:template name="nonNumericDomain">
     <xsl:param name="nondomainfirstColStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: nonNumericDomain</xsl:text></xsl:message></xsl:if>
      <strong id="toggleNonNumericDomain" class="toggleButton"><button>+/-</button>Allowed Values and Definitions</strong>
      <div class="collapsible">
     <table class="{$tabledefaultStyle}">
        <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="nonNumericDomainCommon">
             <xsl:with-param name="nondomainfirstColStyle" select="$nondomainfirstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="nonNumericDomainCommon">
             <xsl:with-param name="nondomainfirstColStyle" select="$nondomainfirstColStyle"/>
           </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
      </div>
  </xsl:template>

  <xsl:template name="nonNumericDomainCommon">
    <xsl:param name="nondomainfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: nonNumericDomainCommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="enumeratedDomain">
      <xsl:call-template name="enumeratedDomain">
        <xsl:with-param name="nondomainfirstColStyle" select="$nondomainfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="textDomain">
      <xsl:call-template name="enumeratedDomain">
        <xsl:with-param name="nondomainfirstColStyle" select="$nondomainfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="textDomain">
    <xsl:param name="nondomainfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: textDomain</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$nondomainfirstColStyle}"><strong>Text Domain</strong></td>
      <td class="{$secondColStyle}">&#160;</td>
    </tr>
    <tr>
      <td class="{$nondomainfirstColStyle}">Definition</td>
      <td class="{$secondColStyle}"><xsl:value-of select="definition"/></td>
    </tr>
    <xsl:for-each select="parttern">
      <tr>
        <td class="{$nondomainfirstColStyle}">Pattern</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:if test="source">
      <tr>
        <td class="{$nondomainfirstColStyle}">Source</td>
        <td class="{$secondColStyle}"><xsl:value-of select="source"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="enumeratedDomain">
    <xsl:param name="nondomainfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: enumeratedDomain</xsl:text></xsl:message></xsl:if>
    <xsl:if test="codeDefinition">
      <tr>
        <td class="{$nondomainfirstColStyle}"><strong>Enumerated Domain</strong></td>
        <td class="{$secondColStyle}">&#160;</td>
      </tr>
      <xsl:for-each select="codeDefinition">
        <tr>
          <td class="{$nondomainfirstColStyle}">Code Definition</td>
          <td>
            <table class="{$tabledefaultStyle}">
              <tr>
                <td class="{$nondomainfirstColStyle}">Code</td>
                <td class="{$secondColStyle}"><xsl:value-of select="code"/></td>
              </tr>
              <tr>
                <td class="{$nondomainfirstColStyle}">Definition</td>
                <td class="{$secondColStyle}"><xsl:value-of select="definition"/></td>
              </tr>
              <tr>
                <td class="{$nondomainfirstColStyle}">Source</td>
                <td class="{$secondColStyle}"><xsl:value-of select="source"/></td>
              </tr>
            </table>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="externalCodeSet">
      <tr>
        <td class="{$nondomainfirstColStyle}"><strong>Enumerated Domain(External Set)</strong></td>
        <td>&#160;</td>
      </tr>
      <tr>
        <td class="{$nondomainfirstColStyle}">Set Name:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="externalCodeSet/codesetName"/></td>
      </tr>
      <xsl:for-each select="externalCodeSet/citation">
        <tr>
          <td class="{$nondomainfirstColStyle}">Citation:</td>
          <td>
            <xsl:call-template name="citation">
              <xsl:with-param name="citationfirstColStyle" select="$nondomainfirstColStyle"/>
              <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="externalCodeSet/codesetURL">
        <tr>
          <td class="{$nondomainfirstColStyle}">URL</td>
          <td class="{$secondColStyle}">
            <a><xsl:attribute name="href"><xsl:value-of select="."/></xsl:attribute><xsl:value-of select="."/></a>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
    <!-- 
     <xsl:if test="entityCodeList">
        <tr><td class="{$nondomainfirstColStyle}"><b>Enumerated Domain (Entity)</b></td>
            <td class="{$secondColStyle}">&#160;
            </td>
       </tr>
        <tr><td class="{$nondomainfirstColStyle}">Entity Reference</td>
            <td class="{$secondColStyle}"><xsl:value-of select="entityCodeList/entityReference"/>
            </td>
       </tr>
       <tr><td class="{$nondomainfirstColStyle}">Attribute Value Reference</td>
            <td class="{$secondColStyle}"><xsl:value-of select="entityCodeList/valueAttributeReference"/>
            </td>
       </tr>
       <tr><td class="{$nondomainfirstColStyle}">Attribute Definition Reference</td>
            <td class="{$secondColStyle}"><xsl:value-of select="entityCodeList/definitionAttributeReference"/>
            </td>
       </tr>
     </xsl:if>
-->
    <!-- mob, 2012-06-04 -->
    <xsl:if test="entityCodeList">
      <tr>
        <td class="{$nondomainfirstColStyle}" colspan="2">
          <strong>The allowed values and their definitions can be found in another data entity in this package. 
                  Please follow link to description, then download:
          </strong>
        </td>
        <!-- <td class="{$secondColStyle}">&#160;</td> -->
      </tr> 
      <tr>
        <td class="{$nondomainfirstColStyle}">Data link:</td>
        <td>
          <table  class="subGroup onehundred_percent {$tabledefaultStyle}">
          <!-- when you call the entityurl template, include a param to label the type of entity  -->
          <!-- also, need 
          http://stackoverflow.com/questions/4449810/using-position-function-in-xslt
          http://www.w3schools.com/xsl/el_number.asp
          remember: The <xsl:number> element is used to determine the integer position 
          of the current node in the source. It is also used to format a number.
          -->
          <!-- one more note: tested with dataTable only
          -->
          <xsl:variable name="entity_ref" select="entityCodeList/entityReference"/>
          <xsl:for-each select="//dataTable[@id=$entity_ref]">
            <xsl:variable name="entity_position"><xsl:number/></xsl:variable>
            <xsl:call-template name="entityurl">
              <xsl:with-param name="type">dataTable</xsl:with-param>
              <xsl:with-param name="showtype">Data Table</xsl:with-param>
              <xsl:with-param name="index" select="$entity_position"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:for-each select="//spatialRaster[@id=$entity_ref]">
            <xsl:variable name="entity_position"><xsl:number/></xsl:variable>
            <xsl:call-template name="entityurl">
              <xsl:with-param name="type">spatialRaster</xsl:with-param>
              <xsl:with-param name="showtype">Spatial Raster</xsl:with-param>
              <xsl:with-param name="index" select="$entity_position"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:for-each select="//spatialVector[@id=$entity_ref]">
            <xsl:variable name="entity_position"><xsl:number/></xsl:variable>
            <xsl:call-template name="entityurl">
              <xsl:with-param name="type">spatialVector</xsl:with-param>
              <xsl:with-param name="showtype">Spatial Vector</xsl:with-param>
              <xsl:with-param name="index" select="$entity_position"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:for-each select="//storedProcedure[@id=$entity_ref]">
            <xsl:variable name="entity_position"><xsl:number/></xsl:variable>
            <xsl:call-template name="entityurl">
              <xsl:with-param name="type">storedProcedure</xsl:with-param>
              <xsl:with-param name="showtype">Stored Procedure</xsl:with-param>
              <xsl:with-param name="index" select="$entity_position"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:for-each select="//view[@id=$entity_ref]">
            <xsl:variable name="entity_position"><xsl:number/></xsl:variable>
            <xsl:call-template name="entityurl">
              <xsl:with-param name="type">view</xsl:with-param>
              <xsl:with-param name="showtype">View</xsl:with-param>
              <xsl:with-param name="index" select="$entity_position"/>
            </xsl:call-template>
          </xsl:for-each>
          <xsl:for-each select="//otherEntity[@id=$entity_ref]">
            <xsl:variable name="entity_position"><xsl:number/></xsl:variable>
            <xsl:call-template name="entityurl">
              <xsl:with-param name="type">otherEntity</xsl:with-param>
              <xsl:with-param name="showtype">Other</xsl:with-param>
              <xsl:with-param name="index" select="$entity_position"/>
            </xsl:call-template>
          </xsl:for-each>  
        </table>
        </td>
      </tr>
      <tr><td class="{$nondomainfirstColStyle}">Code value can be found in:</td>
        <td class="{$secondColStyle}">
           <xsl:variable name="attribute_val_ref" select="entityCodeList/valueAttributeReference"/>
          <xsl:choose>
            <xsl:when test="//*/attributeLabel[../@id=$attribute_val_ref]">
          <xsl:value-of select="//*/attributeLabel[../@id=$attribute_val_ref]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="//*/attributeName[../@id=$attribute_val_ref]"/>
            </xsl:otherwise>
          </xsl:choose>
        </td>
      </tr>
      <tr><td class="{$nondomainfirstColStyle}">Code definition can be found in</td>
        <td class="{$secondColStyle}">
          <xsl:variable name="attribute_def_ref" select="entityCodeList/definitionAttributeReference"/>
          <xsl:choose>
            <xsl:when test="//*/attributeLabel[../@id=$attribute_def_ref]">
              <xsl:value-of select="//*/attributeLabel[../@id=$attribute_def_ref]"/>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="//*/attributeName[../@id=$attribute_def_ref]"/>
            </xsl:otherwise>
          </xsl:choose>          
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- eml-constraint-2.0.0.xsl -->
  <!-- This module is for constraint. And it is self contained-->
  <xsl:template name="constraint">
     <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: constraint</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
        <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="constraintCommon">
             <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
           <xsl:call-template name="constraintCommon">
             <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
            </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="constraintCommon">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: constraintCommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="primaryKey">
       <xsl:call-template name="primaryKey">
          <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="uniqueKey">
       <xsl:call-template name="uniqueKey">
          <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="checkConstraint">
       <xsl:call-template name="checkConstraint">
          <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="foreignKey">
       <xsl:call-template name="foreignKey">
          <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="joinCondition">
       <xsl:call-template name="joinCondition">
          <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="notNullConstraint">
       <xsl:call-template name="notNullConstraint">
          <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!--Keys part-->
  <xsl:template name="primaryKey">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: primaryKey</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$constraintfirstColStyle}">
          Primary Key:</td>
          <td>
            <table class="{$tabledefaultStyle}">
                 <xsl:call-template name="constraintBaseGroup">
                    <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
                 </xsl:call-template>
                 <xsl:for-each select="key/attributeReference">
                      <tr><td class="{$constraintfirstColStyle}">
                            <xsl:text>Key:</xsl:text></td>
                          <td class="{$secondColStyle}">
                            <xsl:value-of select="."/></td>
                      </tr>
                 </xsl:for-each>
            </table>
          </td>
     </tr>
  </xsl:template>

  <xsl:template name="uniqueKey">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: uniqueKey</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$constraintfirstColStyle}">Unique Key:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:call-template name="constraintBaseGroup">
            <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
          </xsl:call-template>
          <xsl:for-each select="key/attributeReference">
            <tr>
              <td class="{$constraintfirstColStyle}"><xsl:text>Key:</xsl:text></td>
              <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
            </tr>
          </xsl:for-each>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="checkConstraint">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: checkConstraint</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$constraintfirstColStyle}">Checking Constraint: </td><td>
        <table class="{$tabledefaultStyle}">
          <xsl:call-template name="constraintBaseGroup">
            <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
          </xsl:call-template>
          <xsl:for-each select="checkCondition">
            <tr>
              <td class="{$constraintfirstColStyle}"><xsl:text>Check Condition:</xsl:text></td>
              <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
            </tr>
          </xsl:for-each>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="foreignKey">
     <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: foreignKey</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$constraintfirstColStyle}">
          Foreign Key:</td>
          <td>
              <table class="{$tabledefaultStyle}">
                  <xsl:call-template name="constraintBaseGroup">
                        <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
                   </xsl:call-template>
                   <xsl:for-each select="key/attributeReference">
                      <tr><td class="{$constraintfirstColStyle}">
                             <xsl:text>Key:</xsl:text></td>
                          <td class="{$secondColStyle}">
                             <xsl:value-of select="."/></td>
                      </tr>
                  </xsl:for-each>
                  <tr><td class="{$constraintfirstColStyle}">
                          <xsl:text>Entity Reference:</xsl:text></td>
                       <td class="{$secondColStyle}">
                           <xsl:value-of select="entityReference"/></td>
                   </tr>
                   <xsl:if test="relationshipType and normalize-space(relationshipType)!=''">
                        <tr><td class="{$constraintfirstColStyle}">
                                <xsl:text>Relationship:</xsl:text></td>
                             <td class="{$secondColStyle}">
                                 <xsl:value-of select="relationshipType"/></td>
                         </tr>
                    </xsl:if>
                    <xsl:if test="cardinality and normalize-space(cardinality)!=''">
                          <tr><td class="{$constraintfirstColStyle}">
                                 <xsl:text>Cardinality:</xsl:text></td>
                              <td>
                                  <table class="{$tabledefaultStyle}">
                                        <tr><td class="{$constraintfirstColStyle}">
                                                <xsl:text>Parent:</xsl:text></td>
                                             <td class="{$secondColStyle}">
                                                 <xsl:value-of select="cardinality/parentOccurences"/></td>
                                        </tr>
                                        <tr><td class="{$constraintfirstColStyle}">
                                                <xsl:text>Children</xsl:text></td>
                                            <td class="{$secondColStyle}">
                                                 <xsl:value-of select="cardinality/childOccurences"/></td>
                                         </tr>
                                   </table>
                               </td>
                          </tr>
                   </xsl:if>
             </table>
          </td>
     </tr>

  </xsl:template>

  <xsl:template name="joinCondition">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: joinCondition</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$constraintfirstColStyle}">
          Join Condition:</td>
          <td>
              <table class="{$tabledefaultStyle}">
                   <xsl:call-template name="foreignKey">
                        <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
                   </xsl:call-template>
                   <xsl:for-each select="referencedKey/attributeReference">
                      <tr><td class="{$constraintfirstColStyle}">
                             <xsl:text>Referenced Key:</xsl:text></td>
                          <td class="{$secondColStyle}">
                              <xsl:value-of select="."/></td>
                       </tr>
                   </xsl:for-each>
             </table>
          </td>
    </tr>
  </xsl:template>

  <xsl:template name="notNullConstraint">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: notNullConstraint</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$constraintfirstColStyle}">
          Not Null Constraint:</td>
          <td>
              <table class="{$tabledefaultStyle}">
                   <xsl:call-template name="constraintBaseGroup">
                       <xsl:with-param name="constraintfirstColStyle" select="$constraintfirstColStyle"/>
                   </xsl:call-template>
                   <xsl:for-each select="key/attributeReference">
                        <tr><td class="{$constraintfirstColStyle}">
                                 <xsl:text>Key:</xsl:text></td>
                            <td class="{$secondColStyle}">
                                 <xsl:value-of select="."/></td>
                        </tr>
                   </xsl:for-each>
              </table>
          </td>
     </tr>
   </xsl:template>

  <xsl:template name="constraintBaseGroup">
    <xsl:param name="constraintfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: constraintBaseGroup</xsl:text></xsl:message></xsl:if>
    <tr><td class="{$constraintfirstColStyle}">
          <xsl:text>Name:</xsl:text></td>
          <td class="{$secondColStyle}">
         <xsl:value-of select="constraintName"/></td>
     </tr>
     <xsl:if test="constraintDescription and normalize-space(constraintDescription)!=''">
       <tr><td class="{$constraintfirstColStyle}">
          <xsl:text>Description:</xsl:text></td>
          <td class="{$secondColStyle}">
          <xsl:value-of select="constraintDescription"/></td>
      </tr>
     </xsl:if>
  </xsl:template>

  <!-- eml-distribution-2.0.0.xsl -->
  <!-- This module is for distribution and it is self-contained-->
  <xsl:template name="distribution">
      <xsl:param name="disfirstColStyle"/>
      <xsl:param name="dissubHeaderStyle"/>
      <xsl:param name="docid"/>
      <xsl:param name="level">entitylevel</xsl:param>
      <xsl:param name="entitytype"/>
      <xsl:param name="entityindex"/>
      <xsl:param name="physicalindex"/>
      <xsl:param name="distributionindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: distribution</xsl:text></xsl:message></xsl:if>
       <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:apply-templates select="online">
              <xsl:with-param name="dissubHeaderStyle" select="$dissubHeaderStyle"/>
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
            </xsl:apply-templates>
            <xsl:apply-templates select="offline">
              <xsl:with-param name="dissubHeaderStyle" select="$dissubHeaderStyle"/>
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
            </xsl:apply-templates>
            <xsl:apply-templates select="inline">
              <xsl:with-param name="dissubHeaderStyle" select="$dissubHeaderStyle"/>
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
               <xsl:with-param name="docid" select="$docid"/>
               <xsl:with-param name="level" select="$level"/>
               <xsl:with-param name="entitytype" select="$entitytype"/>
               <xsl:with-param name="entityindex" select="$entityindex"/>
               <xsl:with-param name="physicalindex" select="$physicalindex"/>
               <xsl:with-param name="distributionindex" select="$distributionindex"/>
             </xsl:apply-templates>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
            <xsl:apply-templates select="online">
              <xsl:with-param name="dissubHeaderStyle" select="$dissubHeaderStyle"/>
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
            </xsl:apply-templates>
            <xsl:apply-templates select="offline">
              <xsl:with-param name="dissubHeaderStyle" select="$dissubHeaderStyle"/>
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
            </xsl:apply-templates>
            <xsl:apply-templates select="inline">
              <xsl:with-param name="dissubHeaderStyle" select="$dissubHeaderStyle"/>
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
               <xsl:with-param name="docid" select="$docid"/>
               <xsl:with-param name="level" select="$level"/>
               <xsl:with-param name="entitytype" select="$entitytype"/>
               <xsl:with-param name="entityindex" select="$entityindex"/>
               <xsl:with-param name="physicalindex" select="$physicalindex"/>
               <xsl:with-param name="distributionindex" select="$distributionindex"/>
            </xsl:apply-templates>
        </xsl:otherwise>
       </xsl:choose>
  </xsl:template>

  <!-- *******************************  Online data  *********************** -->
  <xsl:template match="online">
    <xsl:param name="disfirstColStyle"/>
    <xsl:param name="dissubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: online</xsl:text></xsl:message></xsl:if>
    <xsl:apply-templates select="url">
      <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
    </xsl:apply-templates>
    <xsl:apply-templates select="connection">
      <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
    </xsl:apply-templates>
    <xsl:apply-templates select="connectionDefinition">
      <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="url">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: url</xsl:text></xsl:message></xsl:if>
    <xsl:variable name="URL" select="."/>
    <xsl:variable name="entity_name">
      <xsl:value-of select="../../../../entityName"/>
    </xsl:variable>
    <!-- Parse out entityId from URL and encode for URI safe use as URL HREF value -->
    <xsl:variable name="tokens" select="tokenize($URL, '/')"></xsl:variable>
    <xsl:variable name="entity_identifier" select="string($tokens[10])"></xsl:variable>
    <xsl:variable name="entity_identifier_encd" select="encode-for-uri($entity_identifier)"></xsl:variable>
    <xsl:choose>
      <!-- Assume a PASTA data entity if ancestor path stems from "physical" -->
      <xsl:when test="ancestor::physical/distribution/online/url">
        <tr>
          <td class="{$firstColStyle}">Data:</td>
          <td class="{$secondColStyle}">
            <a>
              <xsl:attribute name="href">/nis/dataviewer?packageid=<xsl:value-of select="$packageID" />&amp;entityid=<xsl:value-of select="$entity_identifier_encd" /></xsl:attribute>
              <xsl:attribute name="target">_blank</xsl:attribute>
              <xsl:value-of select="."/>
            </a>
          </td>
        </tr>         
      </xsl:when>
      <xsl:otherwise>
        <tr>
          <td class="{$firstColStyle}">Url:</td>
          <td class="{$secondColStyle}">
            <a>
              <xsl:attribute name="href"><xsl:value-of select="$URL" /></xsl:attribute>
              <xsl:attribute name="target">_blank</xsl:attribute>
              <xsl:value-of select="."/>
            </a>
          </td>
        </tr>         
      </xsl:otherwise>
    </xsl:choose>
    <xsl:if test="ancestor::otherEntity[physical/dataFormat/externallyDefinedFormat/formatName='KML']">
      <tr>
        <td>View KML content with Google Maps:</td>
        <td>
          <xsl:element name="a">
            <xsl:attribute name="href">
              <xsl:text>http://maps.google.com/?q=</xsl:text><xsl:value-of select="$URL"/>
            </xsl:attribute>
            CLICK HERE FOR MAP
          </xsl:element>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="connection">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: connection</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <xsl:call-template name="connectionCommon">
            <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="connectionCommon">
          <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- A template shared by connection references and connection in line-->
  <xsl:template name="connectionCommon">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: connectionCommon</xsl:text></xsl:message></xsl:if>
    <xsl:if test="parameter">
      <tr>
        <td class="{$disfirstColStyle}">
          <xsl:text>Parameter(s):</xsl:text>
        </td>
        <td class="{$secondColStyle}"><xsl:text>&#160;</xsl:text>
        </td>
      </tr>
      <xsl:call-template name="renderParameters">
        <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
      </xsl:call-template>
    </xsl:if>
    <xsl:apply-templates select="connectionDefinition">
      <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template name="renderParameters">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: renderParameters</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="parameter" >
      <tr>
        <td class="{$disfirstColStyle}">
          <xsl:text>&#160;&#160;&#160;&#160;&#160;</xsl:text><xsl:value-of select="name" />
        </td>
        <td class="{$secondColStyle}">
         <xsl:value-of select="value" />
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="connectionDefinition">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: connectionDefinition</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <xsl:call-template name="connectionDefinitionCommon">
            <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="connectionDefinitionCommon">
          <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

   <!-- This template will be shared by both reference and inline connectionDefinition-->
   <xsl:template name="connectionDefinitionCommon">
     <xsl:param name="disfirstColStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: connectionDefinitionCommon</xsl:text></xsl:message></xsl:if>
      <tr>
          <td class="{$disfirstColStyle}">
            <xsl:text>Schema Name:</xsl:text>
          </td>
          <td class="{$secondColStyle}">
            <xsl:value-of select="schemeName" />
          </td>
       </tr>
       <tr>
          <td class="{$disfirstColStyle}">
            <xsl:text>Description:</xsl:text>
          </td>
          <td>
           <xsl:apply-templates select="description">
              <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
            </xsl:apply-templates>
          </td>
       </tr>
       <xsl:for-each select="parameterDefinition">
          <xsl:call-template name="renderParameterDefinition">
            <xsl:with-param name="disfirstColStyle" select="$disfirstColStyle" />
          </xsl:call-template>
       </xsl:for-each>
   </xsl:template>

  <xsl:template match="description">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: description</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="text">
      <xsl:with-param name="textfirstColStyle" select="$secondColStyle" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="renderParameterDefinition">
    <xsl:param name="disfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: renderParameterDefinition</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$disfirstColStyle}">
        <xsl:text>&#160;&#160;&#160;&#160;&#160;</xsl:text><xsl:value-of select="name"/><xsl:text>:</xsl:text>
      </td>
      <td>
        <table class="{$tabledefaultStyle}">
          <tr>
            <td class="{$disfirstColStyle}">
              <xsl:choose>
                <xsl:when test="defaultValue">
                  <xsl:value-of select="defaultValue" />
                </xsl:when>
                <xsl:otherwise>&#160;</xsl:otherwise>
              </xsl:choose>
            </td>
            <td class="{$secondColStyle}"><xsl:value-of select="definition"/></td>
         </tr>
       </table>
     </td>
   </tr>
  </xsl:template>

  <!-- *******************************  Offline data  ********************** -->
  <xsl:template match="offline">
    <xsl:param name="disfirstColStyle"/>
    <xsl:param name="dissubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: offline</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$dissubHeaderStyle}" colspan="2">
        <xsl:text>Data are Offline:</xsl:text>
      </td>
    </tr>
    <xsl:if test="(mediumName) and normalize-space(mediumName)!=''">
      <tr>
        <td class="{$disfirstColStyle}"><xsl:text>Medium:</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="mediumName"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="(mediumDensity) and normalize-space(mediumDensity)!=''">
      <tr>
        <td class="{$disfirstColStyle}"><xsl:text>Medium Density:</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="mediumDensity"/>
          <xsl:if test="(mediumDensityUnits) and normalize-space(mediumDensityUnits)!=''">
            <xsl:text> (</xsl:text><xsl:value-of select="mediumDensityUnits"/><xsl:text>)</xsl:text>
          </xsl:if>
        </td>
      </tr>
    </xsl:if>
    <xsl:if test="(mediumVol) and normalize-space(mediumVol)!=''">
    <tr>
      <td class="{$disfirstColStyle}"><xsl:text>Volume:</xsl:text></td>
      <td class="{$secondColStyle}"><xsl:value-of select="mediumVol"/></td></tr>
    </xsl:if>
    <xsl:if test="(mediumFormat) and normalize-space(mediumFormat)!=''">
      <tr>
        <td class="{$disfirstColStyle}"><xsl:text>Format:</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="mediumFormat"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="(mediumNote) and normalize-space(mediumNote)!=''">
      <tr>
        <td class="{$disfirstColStyle}"><xsl:text>Notes:</xsl:text></td>
        <td class="{$secondColStyle}"><xsl:value-of select="mediumNote"/></td>
      </tr>
    </xsl:if>
    <!-- added the request-data button oct 2012 mob -->
    <xsl:variable name="email1" select="//dataset/contact[1]/electronicMailAddress"/>
    <!-- Exclude email to the now obsolete tech-support@lternet.edu -->
    <xsl:if test="$email1 != 'tech-support@lternet.edu'">
    <tr>
      <td class="{$disfirstColStyle}"><xsl:text>Request data:</xsl:text></td>
      <td class="{$secondColStyle}">   
        <form method="GET" >
          <xsl:attribute name="action">mailto:<xsl:value-of select="//dataset/contact[1]/electronicMailAddress"/></xsl:attribute>          
          <input type="hidden">
            <xsl:attribute name="value"><xsl:value-of select="../../../entityName"/></xsl:attribute>       
            <xsl:attribute name="name" >subject</xsl:attribute>        
          </input>   
          <input type="submit">
            <xsl:attribute name="value">Request data via email to <xsl:value-of select="//dataset/contact[1]/electronicMailAddress" /></xsl:attribute>       
          </input> 
        </form>
      </td>
    </tr>
    </xsl:if>
    <xsl:variable name="email2" select="//dataset/contact[2]/electronicMailAddress"/>
    <!-- Exclude email to the now obsolete tech-support@lternet.edu -->
    <xsl:if test="$email2 and $email2 != 'tech-support@lternet.edu'">
    <tr>
      <td class="{$disfirstColStyle}"><xsl:text>Request data:</xsl:text></td>
      <td class="{$secondColStyle}">   
        <form method="GET" >
          <xsl:attribute name="action">mailto:<xsl:value-of select="//dataset/contact[2]/electronicMailAddress"/></xsl:attribute>          
          <input type="hidden">
            <xsl:attribute name="value"><xsl:value-of select="../../../entityName"/></xsl:attribute>       
            <xsl:attribute name="name" >subject</xsl:attribute>        
          </input>   
          <input type="submit">
            <xsl:attribute name="value">Request data via email to <xsl:value-of select="//dataset/contact[2]/electronicMailAddress" /></xsl:attribute>       
          </input> 
        </form>
      </td>
    </tr>
    </xsl:if>
  </xsl:template>

  <!-- *******************************  Inline data  *********************** -->
  <xsl:template match="inline">
    <xsl:param name="disfirstColStyle"/>
    <xsl:param name="dissubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entity</xsl:param>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:param name="distributionindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: inline</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$dissubHeaderStyle}" colspan="2"><xsl:text>Data:</xsl:text></td>
    </tr>
    <tr><td class="{$disfirstColStyle}">
      <xsl:text>&#160;</xsl:text></td>
      <td class="{$secondColStyle}">
      <!-- for top top distribution-->
      <xsl:if test="$level='toplevel'">
        <strong>Inline Data</strong>
      </xsl:if>
      <xsl:if test="$level='entitylevel'">
        <strong>Inline Data</strong>
      </xsl:if>
     </td></tr>
  </xsl:template>
  
  <!-- eml-entity-2.0.0.xsl-->
  <xsl:template name="entityName">
    <xsl:param name="entityfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entityName</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$entityfirstColStyle}">Name:</td>
      <td class="{$secondColStyle}"><strong><xsl:value-of select="."/></strong></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="entityalternateIdentifier">
    <xsl:param name="entityfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entityalternateIdentifier</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$entityfirstColStyle}">Identifier:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="entityDescription">
    <xsl:param name="entityfirstColStyle"/> 
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entityDescription</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$entityfirstColStyle}">Description:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>
  
  <xsl:template name="entityadditionalInfo">
    <xsl:param name="entityfirstColStyle"/> 
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: entityadditionalInfo</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$entityfirstColStyle}">Additional Info:</td>
      <td><xsl:call-template name="text"/></td>
    </tr>
  </xsl:template>
  
  <!-- Add a google map using geoCov info mob 10Oct2012 -->
  <!-- 
       JavaScript example:  
       http://jamestombs.co.uk/2011-05-05/creating-markers-info-windows-using-google-maps-javascript-api-v3 
  -->
  <!-- 
       As of Oct 2012, handles only bounding boxes at the dataset level. 
       If N=S and E=W, a marker is displayed. otherwise, a polygon.
       Still to add: 
         (1) datasetGPolygonOuterRing and ExclusionRing (for MCR). EML does not seem to handle lines 
         (2) the KML file that some people (GCE) add via references. might be a different template.
  -->
  <!-- style the identifier and system -->
  <xsl:template name="geoCovMap">
    <xsl:param name="index"/>
    <xsl:param name="maptabledefaultStyle"/>
    <xsl:param name="mapfirstColStyle"/>
    <xsl:param name="mapsecondColStyle"/>
    <xsl:param name="currentmodule"/>
    <xsl:param name="packageID"><xsl:value-of select="../@packageId"/></xsl:param>   
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: geoCovMap</xsl:text></xsl:message></xsl:if>
    
    <script type="text/javascript"
            src="http://maps.googleapis.com/maps/api/js?sensor=false">
    </script>

    <script type="text/javascript">
  
  var map; 
  var infowindow;  
  var curr_infowindow;

  var marker;
  var markers = [];
  var pt_locations = [];
  var pt_titles = [];
  var pt_info =[];
  
  var boundingPolygon;
  var boundingPolygons = [];
  var poly_locations = [];
  var poly_titles = [];
  var poly_info =[];

  // get the data into arrays.
  <xsl:for-each select="coverage/geographicCoverage">
    var nbc = <xsl:value-of select="boundingCoordinates/northBoundingCoordinate"/>;
    var sbc = <xsl:value-of select="boundingCoordinates/southBoundingCoordinate"/>;
    var wbc = <xsl:value-of select="boundingCoordinates/westBoundingCoordinate"/>;
    var ebc = <xsl:value-of select="boundingCoordinates/eastBoundingCoordinate"/>;
    
    var gc_id = '<xsl:value-of select="@id"/>';
    var gc_descr = "<xsl:value-of select="normalize-space(geographicDescription)"/>";
    
    //logic to determine if we are dealing with a single point or a polygon
    if (nbc == sbc &amp;&amp; wbc == ebc) {
      // single point, will use a marker
      myLat = nbc; 
      myLon = wbc;
      var point = new google.maps.LatLng(myLat,myLon);
      pt_locations.push(point);
      
      // the id if there is one - id is optional
      if( gc_id ) {
      pt_titles.push(gc_id);
      } else {
      // if no id, titles[i] will be null 
      pt_titles.push(' ');
      }
      
      // the description for the info bubble
      pt_info.push(gc_descr);

    } else {
     // not a point, will use a polygon
    var boundingCoordinates = [
      new google.maps.LatLng(nbc,wbc),
      new google.maps.LatLng(nbc,ebc),
      new google.maps.LatLng(sbc,ebc),
      new google.maps.LatLng(sbc,wbc)
    ];
    poly_locations.push(boundingCoordinates);
    
      // the id if there is one - id is optional
      if( gc_id ) {
      poly_titles.push(gc_id);
      } else {
      // if no id, titles[i] will be null 
      poly_titles.push(' ');
      }
      
      // the description for the info bubble
      poly_info.push(gc_descr);
    
    } // closes if n=s and w=s 

  </xsl:for-each>

  
  // MAP FUNCTIONS BELOW HERE.
  function initialize_map() {
    var myCenter = new google.maps.LatLng(34.25,-120.00);
    var myOptions = {
      zoom: 8,
      center: myCenter,
      mapTypeId: google.maps.MapTypeId.TERRAIN,
      streetViewControl: false 
    };
    
    map = new google.maps.Map(document.getElementById("map_canvas"), myOptions);
  
    // add the site-markers 
    for (var i = 0; i &lt; pt_locations.length; i++) {
      markers[i] = createMarker(pt_locations[i], pt_info[i], pt_titles[i], map);      
    } 
    
    // add the polygons
    for (var j = 0; j &lt; poly_locations.length; j++) {
      boundingPolygons[j] = createPolygon(poly_locations[j], poly_info[j], poly_titles[j], map);      
    } 
    
  } 
        
        
   function createMarker(point, pt_info, pt_title, map) {
     var marker = new google.maps.Marker({
       position: point,
       map: map,         
       title: pt_title       
     });
             
     var infowindow = new google.maps.InfoWindow({
       content: pt_info
     });
        
     google.maps.event.addListener(marker, "click", function() {
       if (curr_infowindow) {curr_infowindow.close(); }
       curr_infowindow = infowindow;
       infowindow.open(map, marker); 
     });
     
     return marker;
   }   
   
   
   function createPolygon (poly_coords, poly_info, poly_title, map) {      
     var boundingPolygon = new google.maps.Polygon({
       paths: poly_coords,
       strokeColor: "#FF0000",
       strokeOpacity: 0.8,
       strokeWeight: 2,
       fillColor: "#FF0000",
       fillOpacity: 0.1
     });
  
     var infowindow = new google.maps.InfoWindow({
       content: poly_info
     });
  
     // add a listener 
     google.maps.event.addListener(boundingPolygon, 'click', function(event) {
       if ( curr_infowindow) {curr_infowindow.close(); }
       curr_infowindow = infowindow;
       infowindow.setPosition(event.latLng);
       infowindow.open(map);
     });
    
     boundingPolygon.setMap(map); 
   }
   
    </script> 

  </xsl:template>
    
    
  <!-- eml-identifier-2.0.0.xsl -->
  <!-- How to cite a dataset  mgb 31May2011. style the identifier and system -->
  <xsl:template name="howtoCite">
    <xsl:param name="index"/>
    <xsl:param name="citetabledefaultStyle"/>
    <xsl:param name="citefirstColStyle"/>
    <xsl:param name="citesecondColStyle"/>
    <xsl:param name="packageID"><xsl:value-of select="../@packageId"/></xsl:param>
    <xsl:param name="datasetTitle"><xsl:value-of select="title"/></xsl:param>
    <xsl:param name="publisherOrganizationName"><xsl:value-of select="publisher/organizationName"/></xsl:param>
    <xsl:param name="organizationName"><xsl:value-of select="creator/organizationName"/></xsl:param>
    <xsl:param name="pubDate"><xsl:value-of select="pubDate"/></xsl:param>
    <xsl:param name="givenName"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: howtoCite</xsl:text></xsl:message></xsl:if>
    <table class="{$citetabledefaultStyle}" id="howToCite">
      <tr>
        <td class="{$citefirstColStyle}">How to cite this data package:</td>
        <td class="{$citesecondColStyle}">
          <!-- count the creators, set a var -->
          <xsl:variable name="creator_count" select="count(creator/individualName)"/>
          <xsl:for-each select="creator/individualName">
            <!-- if this is the last author in a list, and not the first, then prefix an "and" -->
            <xsl:if test="position() = last() and not(position() = 1)">
              and
            </xsl:if>
            <xsl:if test="not(position() = last()) and not(position() = 1)">
              <xsl:text>,&#160;</xsl:text>
            </xsl:if>           
            <xsl:if test="position()=1"> <!-- for first author, put surname before initial(s) -->
              <xsl:value-of select="surName"/>
              <xsl:if test="givenName">
                <xsl:text>,&#160;</xsl:text>
                <xsl:for-each select="givenName">                
                  <xsl:value-of select="substring(., 1, 1)"/><xsl:text>.&#160;</xsl:text><!-- first initial followed by period -->
                </xsl:for-each>
              </xsl:if>
            </xsl:if>
            <xsl:if test="not(position()=1)"> <!-- for any except first author, put initial(s) before surname -->
              <xsl:if test="givenName">               
                <xsl:for-each select="givenName"> <!-- the dot in the substring arg below is the givenName value -->     
                  <xsl:value-of select="substring(., 1, 1)"/> <!-- first initial -->
                </xsl:for-each>
                <xsl:text>.&#160;</xsl:text>
                <xsl:value-of select="surName"/>
              </xsl:if>
            </xsl:if>
          </xsl:for-each>
          <!-- GET LOGIC FROM YOUR PUBS DISPLAY!!!   -->
          <xsl:choose>
            <xsl:when test="creator_count='1'">
              <xsl:choose>
                <xsl:when test="creator/individualName/givenName">
                  <!-- do nothing. the period following the initial will suffice.--> 
                </xsl:when>
                <xsl:otherwise>
                  <!-- there is one creator, and no given name. eew. ugly, but oh well. add a period. -->
                  <xsl:text>.&#160;</xsl:text>
                </xsl:otherwise>
              </xsl:choose>             
            </xsl:when>
            <xsl:otherwise>
              <!-- more than one creator. -->
              <xsl:text>.&#160;</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <xsl:value-of select="substring($pubDate,1,4)"/>. 
          <xsl:value-of select="$datasetTitle"/><xsl:text>.&#160;</xsl:text>
          <xsl:choose>
            <xsl:when test="string-length($publisherOrganizationName) = 0">
              <xsl:value-of select="$organizationName"/><xsl:text>.</xsl:text>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="$publisherOrganizationName"/>
              <xsl:text>.&#160;</xsl:text>
            </xsl:otherwise>
          </xsl:choose>
          <br/>
          <xsl:choose>
            <xsl:when test="string-length($dataPackageDOI) > 0">
              <label>Identifier:</label><xsl:value-of select="$dataPackageDOI"/><br/>
            </xsl:when>
          </xsl:choose>     
          <label>Alternate Identifier:</label><xsl:value-of select="$resourceId"/>
        </td>
      </tr>
    </table>
  </xsl:template>

  <!-- Style the identifier and system -->
  <xsl:template name="identifier">
    <xsl:param name="IDfirstColStyle"/>
    <xsl:param name="IDsecondColStyle"/>
    <xsl:param name="packageID"/>
    <xsl:param name="system"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: identifier</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)">
      <tr>
        <td class="{$firstColStyle}">Local&#160;Identifier:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="$packageID"/>
          <!-- 
          <xsl:if test="normalize-space(../@system) != ''">
            <xsl:text> (in the </xsl:text><em><xsl:value-of select="$system"/></em><xsl:text> catalog system)</xsl:text>
          </xsl:if> 
          -->
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- eml-literature-2.0.0.xsl '-->
  <xsl:template name="citation">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citation</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <xsl:call-template name="citationCommon">
            <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
            <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="citationCommon">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="citationCommon">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationCommon</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2">
      <xsl:call-template name="resource">
        <xsl:with-param name="resfirstColStyle" select="$citationfirstColStyle"/>
        <xsl:with-param name="ressubHeaderStyle" select="$citationsubHeaderStyle"/>
        <xsl:with-param name="creator">Author(s):</xsl:with-param>
      </xsl:call-template>
      </td>
    </tr>
    <xsl:for-each select="article">
       <xsl:call-template name="citationarticle">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="book">
       <xsl:call-template name="citationbook">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="chapter">
       <xsl:call-template name="citationchapter">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="editedBook">
       <xsl:call-template name="citationeditedBook">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="manuscript">
       <xsl:call-template name="citationmanuscript">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="report">
       <xsl:call-template name="citationreport">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="thesis">
       <xsl:call-template name="citationthesis">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="conferenceProceedings">
       <xsl:call-template name="citationconferenceProceedings">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="personalCommunication">
       <xsl:call-template name="citationpersonalCommunication">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="map">
       <xsl:call-template name="citationmap">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="generic">
       <xsl:call-template name="citationgeneric">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="audioVisual">
       <xsl:call-template name="citationaudioVisual">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="presentation">
       <xsl:call-template name="citationpresentation">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:if test="access and normalize-space(access)!=''">
      <tr><td colspan="2">
        <xsl:for-each select="access">
          <xsl:call-template name="access">
            <xsl:with-param name="accessfirstColStyle" select="$citationfirstColStyle"/>
            <xsl:with-param name="accesssubHeaderStyle" select="$citationsubHeaderStyle"/>
         </xsl:call-template>
        </xsl:for-each>
      </td>
     </tr>
   </xsl:if>
  </xsl:template>

  <xsl:template name="citationarticle">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationarticle</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$citationsubHeaderStyle}" colspan="2"><xsl:text>Article:</xsl:text></td>
    </tr>
    <tr>
      <td class="{$citationfirstColStyle}">Journal:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="journal"/></td>
    </tr>
    <tr>
      <td class="{$citationfirstColStyle}">Volume:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="volume"/></td>
    </tr>
    <xsl:if test="issue and normalize-space(issue)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Issue:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="issue"/></td>
      </tr>
    </xsl:if>
    <tr>
      <td class="{$citationfirstColStyle}">Page Range:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="pageRange"/></td>
    </tr>
    <xsl:if test="publisher and normalize-space(publisher)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Publisher:</td>
        <td class="{$secondColStyle}">&#160;</td>
      </tr>
      <xsl:for-each select="publisher">
        <tr>
          <td colspan="2">
            <xsl:call-template name="party">
              <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="publicationPlace and normalize-space(publicationPlace)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Publication Place:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="publicationPlace"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="ISSN and normalize-space(ISSN)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">ISSN:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="ISSN"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="citationbook">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:param name="notshow" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationbook</xsl:text></xsl:message></xsl:if>
    <xsl:if test="$notshow =''">
          <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Book:</xsl:text></td></tr>
        </xsl:if>
        <tr><td class="{$citationfirstColStyle}">
            Publisher:</td><td>
           <xsl:for-each select="publisher">
             <xsl:call-template name="party">
                <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
              </xsl:call-template>
          </xsl:for-each>
        </td></tr>
        <xsl:if test="publicationPlace and normalize-space(publicationPlace)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Publication Place:</td><td class="{$secondColStyle}">
            <xsl:value-of select="publicationPlace"/></td></tr>
        </xsl:if>
        <xsl:if test="edition and normalize-space(edition)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Edition:</td><td class="{$secondColStyle}">
            <xsl:value-of select="edition"/></td></tr>
        </xsl:if>
        <xsl:if test="volume and normalize-space(volume)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Volume:</td><td class="{$secondColStyle}">
            <xsl:value-of select="volume"/></td></tr>
        </xsl:if>
         <xsl:if test="numberOfVolumes and normalize-space(numberOfVolumes)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Number of Volumes:</td><td class="{$secondColStyle}">
            <xsl:value-of select="numberOfVolumes"/></td></tr>
        </xsl:if>
        <xsl:if test="totalPages and normalize-space(totalPages)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Total Pages:</td><td class="{$secondColStyle}">
            <xsl:value-of select="totalPages"/></td></tr>
        </xsl:if>
        <xsl:if test="totalFigures and normalize-space(totalFigures)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Total Figures:</td><td class="{$secondColStyle}">
            <xsl:value-of select="totalFigures"/></td></tr>
        </xsl:if>
        <xsl:if test="totalTables and normalize-space(totalTables)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Total Tables:</td><td class="{$secondColStyle}">
            <xsl:value-of select="totalTables"/></td></tr>
        </xsl:if>
        <xsl:if test="ISBN and normalize-space(ISBN)!=''">
           <tr><td class="{$citationfirstColStyle}">
            ISBN:</td><td class="{$secondColStyle}">
            <xsl:value-of select="ISBN"/></td></tr>
        </xsl:if>
   </xsl:template>

   <xsl:template name="citationchapter">
      <xsl:param name="citationfirstColStyle"/>
      <xsl:param name="citationsubHeaderStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationchapter</xsl:text></xsl:message></xsl:if>
     <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Chapter:</xsl:text></td></tr>
        <xsl:if test="chapterNumber and normalize-space(chapterNumber)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Chapter Number:</td><td class="{$secondColStyle}">
            <xsl:value-of select="chapterNumber"/></td></tr>
        </xsl:if>
       <tr><td class="{$citationfirstColStyle}">
        Book Editor:</td><td class="{$secondColStyle}">
        &#160;</td></tr>
        <xsl:for-each select="editor">
          <tr><td colspan="2">
            <xsl:call-template name="party">
              <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
          </td></tr>
        </xsl:for-each>
       <tr><td class="{$citationfirstColStyle}">
        Book Title:</td><td class="{$secondColStyle}">
        <xsl:value-of select="bookTitle"/></td></tr>
        <xsl:if test="pageRange and normalize-space(pageRange)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Page Range:</td><td class="{$secondColStyle}">
            <xsl:value-of select="pageRange"/></td></tr>
        </xsl:if>
        <xsl:call-template name="citationbook">
          <xsl:with-param name="notshow" select="yes"/>
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
        </xsl:call-template>
   </xsl:template>

   <xsl:template name="citationeditedBook">
      <xsl:param name="citationfirstColStyle"/>
      <xsl:param name="citationsubHeaderStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationeditedBook</xsl:text></xsl:message></xsl:if>
     <xsl:call-template name="citationbook">
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
        </xsl:call-template>
   </xsl:template>

   <xsl:template name="citationmanuscript">
     <xsl:param name="citationfirstColStyle"/>
     <xsl:param name="citationsubHeaderStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationmanuscript</xsl:text></xsl:message></xsl:if>
     <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Manuscript:</xsl:text></td></tr>
       <tr><td class="{$citationfirstColStyle}">
            Institution:
            </td>
            <td class="{$secondColStyle}">
              &#160;
            </td>
       </tr>
       <xsl:for-each select="institution">
        <tr><td colspan="2">
              <xsl:call-template name="party">
                <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
              </xsl:call-template>
           </td>
        </tr>
       </xsl:for-each>
       <xsl:if test="totalPages and normalize-space(totalPages)!=''">
         <tr><td class="{$citationfirstColStyle}">
            Total Pages:</td><td class="{$secondColStyle}">
            <xsl:value-of select="totalPages"/></td></tr>
       </xsl:if>
   </xsl:template>

   <xsl:template name="citationreport">
     <xsl:param name="citationfirstColStyle"/>
     <xsl:param name="citationsubHeaderStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationreport</xsl:text></xsl:message></xsl:if>
     <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Report:</xsl:text></td></tr>
       <xsl:if test="reportNumber and normalize-space(reportNumber)!=''">
          <tr><td class="{$citationfirstColStyle}">
            Report Number:</td><td class="{$secondColStyle}">
          <xsl:value-of select="reportNumber"/></td></tr>
       </xsl:if>
       <xsl:if test="publisher and normalize-space(publisher)!=''">
          <tr><td class="{$citationfirstColStyle}">
            Publisher:</td><td class="{$secondColStyle}">
            &#160;</td></tr>
          <xsl:for-each select="publisher">
           <tr><td colspan="2">
              <xsl:call-template name="party">
                <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
              </xsl:call-template>
           </td></tr>
          </xsl:for-each>
       </xsl:if>
       <xsl:if test="publicationPlace and normalize-space(publicationPlace)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Publication Place:</td><td class="{$secondColStyle}">
            <xsl:value-of select="publicationPlace"/></td></tr>
       </xsl:if>
       <xsl:if test="totalPages and normalize-space(totalPages)!=''">
         <tr><td class="{$citationfirstColStyle}">
            Total Pages:</td><td class="{$secondColStyle}">
            <xsl:value-of select="totalPages"/></td></tr>
       </xsl:if>
   </xsl:template>

   <xsl:template name="citationthesis">
     <xsl:param name="citationfirstColStyle"/>
     <xsl:param name="citationsubHeaderStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationthesis</xsl:text></xsl:message></xsl:if>
     <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Thesis:</xsl:text></td></tr>
        <tr><td class="{$citationfirstColStyle}">
        Degree:</td><td class="{$secondColStyle}">
        <xsl:value-of select="degree"/></td></tr>
       <tr><td class="{$citationfirstColStyle}">
        Degree Institution:</td><td class="{$secondColStyle}">
        &#160;</td></tr>
        <xsl:for-each select="institution">
          <tr><td colspan="2">
              <xsl:call-template name="party">
                <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
                <!-- <xsl:with-param name="partysubHeaderStyle" select="$citationsubHeaderStyle"/> -->
              </xsl:call-template>
          </td></tr>
        </xsl:for-each>
       <xsl:if test="totalPages and normalize-space(totalPages)!=''">
         <tr><td class="{$citationfirstColStyle}">
         Total Pages:</td><td class="{$secondColStyle}">
         <xsl:value-of select="totalPages"/></td></tr>
       </xsl:if>
   </xsl:template>

   <xsl:template name="citationconferenceProceedings">
     <xsl:param name="citationfirstColStyle"/>
     <xsl:param name="citationsubHeaderStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationconferenceProceedings</xsl:text></xsl:message></xsl:if>
     <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Conference Proceedings:</xsl:text></td></tr>
      <xsl:if test="conferenceName and normalize-space(conferenceName)!=''">
         <tr><td class="{$citationfirstColStyle}">
         Conference Name:</td><td class="{$secondColStyle}">
         <xsl:value-of select="conferenceName"/></td></tr>
       </xsl:if>
       <xsl:if test="conferenceDate and normalize-space(conferenceDate)!=''">
         <tr><td class="{$citationfirstColStyle}">
         Date:</td><td class="{$secondColStyle}">
         <xsl:value-of select="conferenceDate"/></td></tr>
       </xsl:if>
       <xsl:if test="conferenceLocation and normalize-space(conferenceLocation)!=''">
        <tr><td class="{$citationfirstColStyle}">
         Location:</td><td class="{$secondColStyle}">
         &#160;</td></tr>
         <tr><td colspan="2">
           <xsl:for-each select="conferenceLocation">
            <xsl:call-template name="party">
             <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
           </xsl:for-each>
          </td>
        </tr>
       </xsl:if>
       <xsl:call-template name="citationchapter">
          <!-- <xsl:with-param name="notshow" select="yes"/> -->
          <xsl:with-param name="citationfirstColStyle" select="$citationfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$citationsubHeaderStyle"/>
       </xsl:call-template>
  </xsl:template>

  <xsl:template name="citationpersonalCommunication">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationpersonalCommunication</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Personal Communication:</xsl:text></td>
    </tr>
    <xsl:if test="publisher and normalize-space(publisher)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Publisher:</td>
        <td class="{$secondColStyle}">&#160;</td>
      </tr>
      <xsl:for-each select="publisher">
        <tr>
          <td colspan="2">
            <xsl:call-template name="party">
              <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="publicationPlace and normalize-space(publicationPlace)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Publication Place:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="publicationPlace"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="communicationType and normalize-space(communicationType)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Communication Type:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="communicationType"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="recipient and normalize-space(recipient)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Recipient:</td>
        <td class="{$secondColStyle}">&#160;</td>
      </tr>
      <xsl:for-each select="recipient">
        <tr>
          <td colspan="2">
            <xsl:call-template name="party">
              <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="citationmap">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationmap</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Map:</xsl:text></td>
    </tr>
    <xsl:if test="publisher and normalize-space(publisher)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Publisher:</td>
        <td class="{$secondColStyle}">&#160;</td>
      </tr>
      <xsl:for-each select="publisher">
        <tr>
          <td colspan="2">
            <xsl:call-template name="party">
              <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="edition and normalize-space(edition)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Edition:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="edition"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="geographicCoverage and normalize-space(geographicCoverage)!=''">
      <xsl:for-each select="geographicCoverage">
        <xsl:call-template name="geographicCoverage"></xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="scale and normalize-space(scale)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Scale:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="scale"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="citationgeneric">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationgeneric</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Generic Citation:</xsl:text></td>
    </tr>
    <tr>
      <td class="{$citationfirstColStyle}">Publisher:</td>
      <td class="{$secondColStyle}">&#160;</td>
    </tr>
    <xsl:for-each select="publisher">
      <tr>
        <td colspan="2">
          <xsl:call-template name="party">
            <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="publicationPlace and normalize-space(publicationPlace)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Publication Place:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="publicationPlace"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="referenceType and normalize-space(referenceType)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Reference Type:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="referenceType"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="volume and normalize-space(volume)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Volume:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="volume"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="numberOfVolumes and normalize-space(numberOfVolumes)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Number of Volumes:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="numberOfVolumes"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="totalPages and normalize-space(totalPages)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Total Pages:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="totalPages"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="totalFigures and normalize-space(totalFigures)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Total Figures:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="totalFigures"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="totalTables and normalize-space(totalTables)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Total Tables:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="totalTables"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="edition and normalize-space(edition)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Edition:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="edition"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="originalPublication and normalize-space(originalPublication)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Supplemental Info for Original Publication:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="originalPublication"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="reprintEdition and normalize-space(reprintEdition)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Reprint Edition:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="reprintEdition"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="reviewedItem and normalize-space(reviewedItem)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">Review Item:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="reviewedItem"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="ISBN and normalize-space(ISBN)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">ISBN:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="ISBN"/></td>
      </tr>
    </xsl:if>
    <xsl:if test="ISSN and normalize-space(ISSN)!=''">
      <tr>
        <td class="{$citationfirstColStyle}">ISSN:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="ISSN"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="citationaudioVisual">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationaudioVisual</xsl:text></xsl:message></xsl:if>
    <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Media Citation:</xsl:text></td></tr>
      <tr><td class="{$citationfirstColStyle}">
            Publisher:</td><td class="{$secondColStyle}">
            &#160;
      </td></tr>
       <xsl:for-each select="publisher">
         <tr><td colspan="2">
              <xsl:call-template name="party">
                <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
              </xsl:call-template>
         </td></tr>
      </xsl:for-each>
      <xsl:if test="publicationPlace and normalize-space(publicationPlace)!=''">
           <tr><td class="{$citationfirstColStyle}">
            Publication Place:</td><td class="{$secondColStyle}">
            &#160;</td></tr>
            <xsl:for-each select="publicationPlace">
                <tr><td class="{$citationfirstColStyle}">
                    &#160;</td>
                    <td class="{$secondColStyle}">
                    <xsl:value-of select="."/>
                </td></tr>
            </xsl:for-each>
      </xsl:if>
      <xsl:if test="performer and normalize-space(performer)!=''">
            <tr><td class="{$citationfirstColStyle}">
            Performer:</td><td class="{$secondColStyle}">
            &#160;</td></tr>
            <xsl:for-each select="performer">
                <tr><td colspan="2">
                   <xsl:call-template name="party">
                     <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
                   </xsl:call-template>
                </td></tr>
            </xsl:for-each>
      </xsl:if>
      <xsl:if test="ISBN and normalize-space(ISBN)!=''">
           <tr><td class="{$citationfirstColStyle}">
            ISBN:</td><td class="{$secondColStyle}">
            <xsl:value-of select="ISBN"/></td></tr>
      </xsl:if>
  </xsl:template>

  <xsl:template name="citationpresentation">
    <xsl:param name="citationfirstColStyle"/>
    <xsl:param name="citationsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citationpresentation</xsl:text></xsl:message></xsl:if>
    <tr><td colspan="2" class="{$citationsubHeaderStyle}"><xsl:text>Presentation:</xsl:text></td></tr>
      <xsl:if test="conferenceName and normalize-space(conferenceName)!=''">
         <tr><td class="{$citationfirstColStyle}">
         Conference Name:</td><td class="{$secondColStyle}">
         <xsl:value-of select="conferenceName"/></td></tr>
       </xsl:if>
       <xsl:if test="conferenceDate and normalize-space(conferenceDate)!=''">
         <tr><td class="{$citationfirstColStyle}">
         Date:</td><td class="{$secondColStyle}">
         <xsl:value-of select="conferenceDate"/></td></tr>
       </xsl:if>
         <tr><td class="{$citationfirstColStyle}">
         Location:</td><td class="{$secondColStyle}">
         &#160;</td></tr>
         <tr><td colspan="2">
           <xsl:for-each select="conferenceLocation">
            <xsl:call-template name="party">
             <xsl:with-param name="partyfirstColStyle" select="$citationfirstColStyle"/>
            </xsl:call-template>
           </xsl:for-each>
          </td>
        </tr>
  </xsl:template>

  <!-- eml-method-2.0.0.xsl -->
  <!-- changes labeled "mob 2005-12-xx to clean up protocols. not sure of
       impact on other elements. arghh. -->
  <xsl:template name="method">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: method</xsl:text></xsl:message></xsl:if>
    <!-- <table class="{$tabledefaultStyle}">  use this class to unbox the table  -->
    <table class="subGroup onehundred_percent">
      <tr>
	      <!-- changed table title. usually protocol refs, sometimes procedural steps -->
        <!-- Step by Step Procedures  -->
        <th colspan="2">Protocols and/or Procedures</th>
      </tr>
      <xsl:for-each select="methodStep">
		    <!-- methodStep (defined below) calls step (defined in protocol.xsl).  -->
		    <!-- mob added a table element to the step template so that each methodStep 
		         is boxed, but without position labels. Could add step labels back in, or just 
		         for subSteps with 'if test=substep'? proceduralStep? -->
		    <tr>
		 	    <td>
				    <xsl:call-template name="methodStep">
				      <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
				      <xsl:with-param name="methodsubHeaderStyle" select="$methodsubHeaderStyle"/>
			      </xsl:call-template>
			    </td>
		    </tr>
      </xsl:for-each>
      <!-- SAMPLING descr, extent -->
      <xsl:if test="sampling">   
		    <xsl:for-each select="sampling">
		      <!-- <table class="{$tabledefaultStyle}">  use this class to unbox the table  -->
			    <table class="subGroup onehundred_percent">
            <tr>
				      <th colspan="2">Sampling Area and Study Extent</th>
				    </tr>
				    <tr>
				      <td>
				        <xsl:call-template name="sampling">
						      <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
						      <xsl:with-param name="methodsubHeaderStyle" select="$methodsubHeaderStyle"/>
					      </xsl:call-template>
				      </td>
				    </tr>
			    </table>
		    </xsl:for-each> 
      </xsl:if>
      <!-- QUALITY CONTROL -->
		  <!-- dont have any files to test this on yet, working? -->
      <xsl:if test="qualityControl">
        <table class="{$tabledefaultStyle}">
				  <tr>
					  <th colspan="2">Quality Control</th>
			    </tr>
			    <xsl:for-each select="qualityControl">
				    <tr>
				      <td class="{$methodfirstColStyle}">
					      <strong>Quality Control Step<xsl:text> </xsl:text><xsl:value-of select="position()"/>:</strong>
				      </td>
				      <td width="${secondColWidth}" class="{$secondColStyle}">&#160;</td>
				    </tr> 
				    <xsl:call-template name="qualityControl">
					    <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
					    <xsl:with-param name="methodsubHeaderStyle" select="$methodsubHeaderStyle"/>
				    </xsl:call-template>
		      </xsl:for-each>
		    </table>
		  </xsl:if>
    </table>   <!-- matches table onehundredpercent, entire methodStep-->
  </xsl:template>

  <!-- Method step -->
  <xsl:template name="methodStep">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: methodStep</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="step">
      <xsl:with-param name="protocolfirstColStyle" select="$methodfirstColStyle"/>
      <xsl:with-param name="protocolsubHeaderStyle" select="$methodsubHeaderStyle"/>
    </xsl:call-template>
    <xsl:for-each select="dataSource">
      <tr>
        <td colspan="2"><xsl:apply-templates mode="dataset"></xsl:apply-templates></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- Sampling -->
  <xsl:template name="sampling">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: sampling</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="samplingDescription">
		  <table class="{$tabledefaultStyle}">
     	  <tr>
				  <td class="{$methodfirstColStyle}">Sampling Description:</td>
          <td width="${secondColWidth}">
            <xsl:call-template name="text">
              <xsl:with-param name="textfirstColStyle" select="$methodfirstColStyle"/>
            </xsl:call-template>
          </td>
        </tr>
		  </table>
    </xsl:for-each>
    <xsl:for-each select="studyExtent">
      <xsl:call-template name="studyExtent">
        <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each> 
    <xsl:for-each select="spatialSamplingUnits">
      <xsl:call-template name="spatialSamplingUnits">
        <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="citation">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Citation:</td>
        <td width="${secondColWidth}">
          <xsl:call-template name="citation">
            <xsl:with-param name="citationfirstColStyle" select="$methodfirstColStyle"/>
            <xsl:with-param name="citationsubHeaderStyle" select="$methodsubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="studyExtent">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: studyExtent</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="coverage">
		  <!-- this table call puts each coverage node in a box -->
		  <table class="{$tabledefaultStyle}">
        <tr>
          <td class="{$methodfirstColStyle}">Sampling Extent:</td>
          <td width="${secondColWidth}">
            <xsl:call-template name="coverage">
            </xsl:call-template>
          </td>
        </tr>
		  </table>
    </xsl:for-each>
    <xsl:for-each select="description">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Area And Frequency:</td>
        <td width="${secondColWidth}" >
          <xsl:call-template name="text">
            <xsl:with-param name="textfirstColStyle" select="$methodfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spatialSamplingUnits">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialSamplingUnits</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="referenceEntityId">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Unit Reference:</td>
        <td width="${secondColWidth}" class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="coverage">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Unit Location:</td>
        <td width="${secondColWidth}">
          <xsl:call-template name="geographicCoverage"></xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- quality control -->
  <xsl:template name="qualityControl">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: qualityControl</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="step">
      <xsl:with-param name="protocolfirstColStyle" select="$methodfirstColStyle"/>
      <xsl:with-param name="protocolsubHeaderStyle" select="$methodsubHeaderStyle"/>
    </xsl:call-template>
  </xsl:template>

  <!-- eml-method-2.0.0.xsl -->
  <xsl:template name="method-original">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: method-original</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <tr>
        <th colspan="2">Step by Step Procedures</th>
      </tr>
      <xsl:for-each select="methodStep">
        <tr>
          <td class="{$methodfirstColStyle}"><strong>Step<xsl:text> </xsl:text><xsl:value-of select="position()"/>:</strong></td>
          <td width="${secondColWidth}" class="{$secondColStyle}">&#160;</td>
        </tr>
        <xsl:call-template name="methodStep">
          <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
          <xsl:with-param name="methodsubHeaderStyle" select="$methodsubHeaderStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="sampling">
        <xsl:call-template name="sampling">
          <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
          <xsl:with-param name="methodsubHeaderStyle" select="$methodsubHeaderStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="qualityControl">
        <tr>
          <td class="{$methodfirstColStyle}"><strong>Quality Control Step<xsl:text> </xsl:text><xsl:value-of select="position()"/>:</strong></td>
          <td width="${secondColWidth}" class="{$secondColStyle}">&#160;</td>
        </tr>
        <xsl:call-template name="qualityControl">
          <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
          <xsl:with-param name="methodsubHeaderStyle" select="$methodsubHeaderStyle"/>
        </xsl:call-template>
      </xsl:for-each>
    </table>
  </xsl:template>

  <!-- Method step -->
  <xsl:template name="methodStep-original">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: methodStep-original</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="step">
      <xsl:with-param name="protocolfirstColStyle" select="$methodfirstColStyle"/>
      <xsl:with-param name="protocolsubHeaderStyle" select="$methodsubHeaderStyle"/>
    </xsl:call-template>
    <xsl:for-each select="dataSource">
      <tr>
        <td colspan="2"><xsl:apply-templates mode="dataset"></xsl:apply-templates></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- Sampling -->
  <xsl:template name="sampling-original">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: sampling-original</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="studyExtent">
      <xsl:call-template name="studyExtent">
        <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="samplingDescription">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Description:</td>
        <td width="${secondColWidth}">
          <xsl:call-template name="text">
            <xsl:with-param name="textfirstColStyle" select="$methodfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="spatialSamplingUnits">
      <xsl:call-template name="spatialSamplingUnits">
        <xsl:with-param name="methodfirstColStyle" select="$methodfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="citation">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Citation:</td>
        <td width="${secondColWidth}">
          <xsl:call-template name="citation">
            <xsl:with-param name="citationfirstColStyle" select="$methodfirstColStyle"/>
            <xsl:with-param name="citationsubHeaderStyle" select="$methodsubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="studyExtent-original">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: studyExtent-original</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="coverage">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Coverage:</td>
        <td width="${secondColWidth}"><xsl:call-template name="coverage"></xsl:call-template></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="description">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Area And Frequency:</td>
        <td width="${secondColWidth}" >
          <xsl:call-template name="text">
            <xsl:with-param name="textfirstColStyle" select="$methodfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spatialSamplingUnits-original">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialSamplingUnits-original</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="referenceEntityId">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Unit Reference:</td>
        <td width="${secondColWidth}" class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="coverage">
      <tr>
        <td class="{$methodfirstColStyle}">Sampling Unit Location:</td>
        <td width="${secondColWidth}"><xsl:call-template name="geographicCoverage"></xsl:call-template></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- quality control -->
  <xsl:template name="qualityControl-original">
    <xsl:param name="methodfirstColStyle"/>
    <xsl:param name="methodsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: qualityControl-original</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="step">
      <xsl:with-param name="protocolfirstColStyle" select="$methodfirstColStyle"/>
      <xsl:with-param name="protocolsubHeaderStyle" select="$methodsubHeaderStyle"/>
    </xsl:call-template>
  </xsl:template>

  <!-- eml-otherentity-2.0.0.xsl -->
  <!-- This module is for datatable module-->
  <xsl:template name="otherEntity">
    <xsl:param name="otherentityfirstColStyle"/>
    <xsl:param name="otherentitysubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: otherEntity</xsl:text></xsl:message></xsl:if>
    <hr></hr>
    <h2>Non-Categorized Data Resource</h2>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="otherEntityCommon">
              <xsl:with-param name="otherentityfirstColStyle" select="$otherentityfirstColStyle"/>
              <xsl:with-param name="otherentitysubHeaderStyle" select="$otherentitysubHeaderStyle"/>
              <xsl:with-param name="docid" select="$docid"/>
              <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="otherEntityCommon">
            <xsl:with-param name="otherentityfirstColStyle" select="$otherentityfirstColStyle"/>
            <xsl:with-param name="otherentitysubHeaderStyle" select="$otherentitysubHeaderStyle"/>
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="otherEntityCommon">
    <xsl:param name="otherentityfirstColStyle"/>
    <xsl:param name="otherentitysubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: otherEntityCommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="entityName">
      <xsl:call-template name="entityName">
        <xsl:with-param name="entityfirstColStyle" select="$otherentityfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityType">
      <tr>
        <td class="{$otherentityfirstColStyle}">Entity Type:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="alternateIdentifier">
      <xsl:call-template name="entityalternateIdentifier">
        <xsl:with-param name="entityfirstColStyle" select="$otherentityfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityDescription">
      <xsl:call-template name="entityDescription">
        <xsl:with-param name="entityfirstColStyle" select="$otherentityfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="additionalInfo">
      <xsl:call-template name="entityadditionalInfo">
        <xsl:with-param name="entityfirstColStyle" select="$otherentityfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <!-- call physical moduel without show distribution(we want see it later)-->
    <xsl:if test="physical">
      <tr>
        <td class="{$otherentitysubHeaderStyle}" colspan="2"></td>
      </tr>
      <tr>
        <td class="{$otherentitysubHeaderStyle}" colspan="2">Physical Structure Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="physical">
      <tr>
        <td colspan="2">
          <xsl:call-template name="physical">
            <xsl:with-param name="physicalfirstColStyle" select="$otherentityfirstColStyle"/>
            <xsl:with-param name="notshowdistribution">yes</xsl:with-param>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="coverage">
      <tr>
        <td class="{$otherentitysubHeaderStyle}" colspan="2">Coverage Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="coverage">
      <tr>
        <td colspan="2"><xsl:call-template name="coverage"></xsl:call-template></td>
      </tr>
    </xsl:for-each>
    <xsl:if test="method | methods">
      <tr>
        <td class="{$otherentitysubHeaderStyle}" colspan="2">Method Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="method | methods">
      <tr>
        <td colspan="2">
          <xsl:call-template name="method">
            <xsl:with-param name="methodfirstColStyle" select="$otherentityfirstColStyle"/>
            <xsl:with-param name="methodsubHeaderStyle" select="$otherentitysubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="constraint">
      <tr>
        <td class="{$otherentitysubHeaderStyle}" colspan="2">Constraint:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="constraint">
      <tr>
        <td colspan="2">
          <xsl:call-template name="constraint">
            <xsl:with-param name="constraintfirstColStyle" select="$otherentityfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="$withAttributes='1'">
    <xsl:for-each select="attributeList">
      <xsl:call-template name="otherEntityAttributeList">
        <xsl:with-param name="otherentityfirstColStyle" select="$otherentityfirstColStyle"/>
        <xsl:with-param name="otherentitysubHeaderStyle" select="$otherentitysubHeaderStyle"/>
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
      </xsl:call-template>
    </xsl:for-each>
    </xsl:if>
    <!-- Here to display distribution info-->
    <xsl:for-each select="physical">
      <xsl:call-template name="otherEntityShowDistribution">
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
        <xsl:with-param name="physicalindex" select="position()"/>
        <xsl:with-param name="otherentityfirstColStyle" select="$otherentityfirstColStyle"/>
        <xsl:with-param name="otherentitysubHeaderStyle" select="$otherentitysubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="otherEntityShowDistribution">
    <xsl:param name="otherentityfirstColStyle"/>
    <xsl:param name="otherentitysubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entitylevel</xsl:param>
    <xsl:param name="entitytype">otherEntity</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: otherEntityShowDistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <xsl:call-template name="distribution">
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="level" select="$level"/>
        <xsl:with-param name="entitytype" select="$entitytype"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
        <xsl:with-param name="physicalindex" select="$physicalindex"/>
        <xsl:with-param name="distributionindex" select="position()"/>
        <xsl:with-param name="disfirstColStyle" select="$otherentityfirstColStyle"/>
        <xsl:with-param name="dissubHeaderStyle" select="$otherentitysubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="otherEntityAttributeList">
    <xsl:param name="otherentityfirstColStyle"/>
    <xsl:param name="otherentitysubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype">otherEntity</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: otherEntityAttributeList</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$otherentitysubHeaderStyle}" colspan="2"><xsl:text>Attribute(s) Info:</xsl:text></td>
    </tr>
    <tr>
      <td colspan="2">
        <xsl:call-template name="attributelist">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <!-- eml-physical-2.0.0.xsl -->
  <xsl:template name="physical">
    <xsl:param name="docid"/>
    <xsl:param name="level">entity</xsl:param>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:param name="distributionindex"/>
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:param name="notshowdistribution"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physical</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="physicalcommon">
              <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
              <xsl:with-param name="notshowdistribution" select="$notshowdistribution"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="physicalcommon">
            <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
            <xsl:with-param name="notshowdistribution" select="$notshowdistribution"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="physicalcommon">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:param name="notshowdistribution"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entity</xsl:param>
    <xsl:param name="entitytype"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:param name="distributionindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalcommon</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="physicalobjectName">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalsize">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalauthentication">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalcompressionMethod">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalencodingMethod">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalcharacterEncoding">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicaltextFormat">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalexternallyDefinedFormat">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="physicalbinaryRasterFormat">
      <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
    </xsl:call-template>
    <xsl:if test="$notshowdistribution=''">
      <xsl:for-each select="distribution">
        <xsl:call-template name="distribution">
          <xsl:with-param name="disfirstColStyle" select="$physicalfirstColStyle"/>
          <xsl:with-param name="dissubHeaderStyle" select="$subHeaderStyle"/>
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="level">entitylevel</xsl:with-param>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
          <xsl:with-param name="physicalindex" select="$physicalindex"/>
          <xsl:with-param name="distributionindex" select="position()"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="physicalobjectName">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalobjectName</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="objectName">
      <tr>
        <td class="{$physicalfirstColStyle}">Object Name:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="physicalsize">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalsize</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="size">
      <tr>
        <td class="{$physicalfirstColStyle}">Size:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/><xsl:text> </xsl:text><xsl:value-of select="./@unit"/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="physicalauthentication">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalauthentication</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="authentication">
      <tr>
        <td class="{$physicalfirstColStyle}">Authentication:</td>
        <td class="{$secondColStyle}">
          <xsl:value-of select="."/><xsl:text> </xsl:text>
          <xsl:if test="./@method">
            Calculated By<xsl:text> </xsl:text><xsl:value-of select="./@method"/>
          </xsl:if>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="physicalcompressionMethod">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalcompressionMethod</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="compressionMethod">
      <tr>
        <td class="{$physicalfirstColStyle}">Compression Method:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="physicalencodingMethod">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalencodingMethod</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="encodingMethod">
      <tr>
        <td class="{$physicalfirstColStyle}">Encoding Method:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="physicalcharacterEncoding">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalcharacterEncoding</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="characterEncoding">
      <tr>
        <td class="{$physicalfirstColStyle}">Character Encoding:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- TextFormat templates -->
  <xsl:template name="physicaltextFormat">
   <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicaltextFormat</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="dataFormat/textFormat">
      <tr>
        <td class="{$physicalfirstColStyle}">Text Format:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:apply-templates>
              <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
            </xsl:apply-templates>
          </table>
        </td>
      </tr>
   </xsl:for-each>
  </xsl:template>

  <xsl:template match="numHeaderLines">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: numHeaderLines</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Number of Header Lines:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="numFooterLines">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: numFooterLines</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Number of Foot Lines:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="recordDelimiter">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: recordDelimiter</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Record Delimiter:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="physicalLineDelimiter">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalLineDelimiter</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Line Delimiter:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="numPhysicalLinesPerRecord">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: numPhysicalLinesPerRecord</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Line Number For One Record:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="maxRecordLength">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: maxRecordLength</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Maximum Record Length:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="attributeOrientation">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: attributeOrientation</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Orientation:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="simpleDelimited">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: simpleDelimited</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Simple Delimited:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates>
            <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
          </xsl:apply-templates>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="complex">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: complex</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Complex Delimited:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:call-template name="textFixed">
            <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
          </xsl:call-template>
          <xsl:call-template name="textDelimited">
            <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
          </xsl:call-template>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="textFixed">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: textFixed</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Text Fixed:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates>
            <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
          </xsl:apply-templates>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="textDelimited">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: textDelimited</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Text Delimited:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <xsl:apply-templates>
            <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
          </xsl:apply-templates>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="quoteCharacter">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: quoteCharacter</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Quote Character:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="literalCharacter">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: literalCharacter</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Literal Character:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="fieldDelimiter">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: fieldDelimiter</xsl:text></xsl:message></xsl:if>
    <td class="{$firstColStyle}">Field Delimiter:</td>
    <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
  </xsl:template>

  <xsl:template match="fieldWidth">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: fieldWidth</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Field Width:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="lineNumber">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: lineNumber</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Line Number:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="fieldStartColumn">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: fieldStartColumn</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Field Start Column:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <!-- externallyDefinedFormat templates -->
  <xsl:template name="physicalexternallyDefinedFormat">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalexternallyDefinedFormat</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="dataFormat/externallyDefinedFormat">
      <tr>
        <td class="{$physicalfirstColStyle}">Externally Defined Format:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:apply-templates>
              <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
            </xsl:apply-templates>
          </table>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>
  
  <xsl:template match="formatName">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalfirstColStyle</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$firstColStyle}">Format Name:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template match="formatVersion">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: formatVersion</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Format Version:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="citation">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: citation (match)</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$physicalfirstColStyle}">Citation:</td>
      <td>
        <xsl:call-template name="citation">
          <xsl:with-param name="citationfirstColStyle" select="$physicalfirstColStyle"/>
          <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <!-- binaryRasterFormat templates -->
  <xsl:template name="physicalbinaryRasterFormat">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: physicalbinaryRasterFormat</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="dataFormat/binaryRasterFormat">
      <tr>
        <td class="{$physicalfirstColStyle}">Binary Raster Format:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:apply-templates>
              <xsl:with-param name="physicalfirstColStyle" select="$physicalfirstColStyle"/>
            </xsl:apply-templates>
          </table>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template match="rowColumnOrientation">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: rowColumnOrientation</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Orientation:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="multiBand">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: multiBand</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Multiple Bands:</td>
      <td>
        <table class="{$tabledefaultStyle}">
          <tr>
            <td class="{$firstColStyle}">Number of Spectral Bands:</td>
            <td class="{$secondColStyle}"><xsl:value-of select="./nbands"/></td>
          </tr>
          <tr>
            <td class="{$firstColStyle}">Layout:</td>
            <td class="{$secondColStyle}"><xsl:value-of select="./layout"/></td>
          </tr>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template match="nbits">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: nbits</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Number of Bits (/pixel/band):</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="byteorder">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: byteorder</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Byte Order:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="skipbytes">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: skipbytes</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Skipped Bytes:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="bandrowbytes">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: bandrowbytes</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Number of Bytes (/band/row):</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="totalrowbytes">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: totalrowbytes</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Total Number of Byte (/row):</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <xsl:template match="bandgapbytes">
    <xsl:param name="physicalfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: bandgapbytes</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$firstColStyle}">Number of Bytes between Bands:</td>
      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
    </tr>
  </xsl:template>

  <!-- eml-project-2.0.0.xsl -->
  <xsl:template name="project">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: project</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="projectcommon">
              <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="projectcommon">
            <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="projectcommon">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectcommon</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="projecttitle">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="projectpersonnel">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="projectabstract">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="projectfunding">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
   <xsl:call-template name="projectstudyareadescription">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="projectdesigndescription">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
    <xsl:call-template name="projectrelatedproject">
      <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="projecttitle">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projecttitle</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="title">
      <tr>
        <td class="{$projectfirstColStyle}">Title:</td>
        <td class="{$secondColStyle}" ><xsl:value-of select="../title"/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="projectpersonnel">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectpersonnel</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$projectfirstColStyle}">Personnel:</td>
      <td>
        <table>
          <xsl:for-each select="personnel">
            <tr>
              <td colspan="2">
                <xsl:call-template name="party">
                  <xsl:with-param name="partyfirstColStyle" select="$projectfirstColStyle"/>
                </xsl:call-template>
              </td>
            </tr>
            <xsl:for-each select="role">
              <tr>
                <td class="{$projectfirstColStyle}">Role:</td>
                <td>
                  <table class="{$tablepartyStyle}">
                    <tr>
                      <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
                    </tr>
                  </table>
                </td>
              </tr>
            </xsl:for-each>
          </xsl:for-each>
        </table>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="projectabstract">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectabstract</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="abstract">
      <tr>
        <td class="{$projectfirstColStyle}">Abstract:</td>
        <td>
          <xsl:call-template name="text">
            <xsl:with-param name="textfirstColStyle" select="$projectfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="projectfunding">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectfunding</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="funding">
       <tr><td class="{$projectfirstColStyle}">
          Funding:
          </td>
          <td>
              <xsl:call-template name="text">
                 <xsl:with-param name="textfirstColStyle" select="$projectfirstColStyle"/>
              </xsl:call-template>
         </td>
       </tr>
    </xsl:for-each>
  </xsl:template>

   <xsl:template name="projectstudyareadescription">
     <xsl:param name="projectfirstColStyle"/>
     <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectstudyareadescription</xsl:text></xsl:message></xsl:if>
     <xsl:for-each select="studyAreaDescription">
       <tr><td class="{$projectfirstColStyle}">
           <xsl:text>Study Area:</xsl:text>
          </td>
          <td>
              <table class="{$tabledefaultStyle}">
                  <xsl:for-each select="descriptor">
                      <xsl:for-each select="descriptorValue">
                      <tr><td class="{$projectfirstColStyle}">
                            <xsl:value-of select="../@name"/>
                          </td>
                          <td class="{$secondColStyle}">
                             <xsl:choose>
                                <xsl:when test="./@citableClassificationSystem">
                                  <xsl:value-of select="."/>&#160;<xsl:value-of select="./@name_or_id"/>
                                </xsl:when>
                                <xsl:otherwise>
                                  <xsl:value-of select="."/>&#160;<xsl:value-of select="./@name_or_id"/>&#160;(No Citable Classification System)
                                </xsl:otherwise>
                              </xsl:choose>
                          </td>
                      </tr>
                      </xsl:for-each>
                      <xsl:for-each select="citation">
                        <tr><td class="{$projectfirstColStyle}">
                              Citation:
                            </td>
                            <td>
                             <xsl:call-template name="citation">
                                  <xsl:with-param name="citationfirstColStyle" select="$projectfirstColStyle"/>
                                  <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
                             </xsl:call-template>
                           </td>
                       </tr>
                    </xsl:for-each>
               </xsl:for-each>
            </table>
         </td>
       </tr>
       <xsl:for-each select="citation">
         <tr><td class="{$projectfirstColStyle}">
          Study Area Citation:
          </td>
          <td>
              <xsl:call-template name="citation">
                   <xsl:with-param name="citationfirstColStyle" select="$projectfirstColStyle"/>
                   <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
               </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
       <xsl:for-each select="coverage">
        <tr><td class="{$projectfirstColStyle}">
          Study Area Coverage:
          </td>
          <td>
             <xsl:call-template name="coverage"/>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:for-each>
   </xsl:template>

  <xsl:template name="projectdesigndescription">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectdesigndescription</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="designDescription">
       <xsl:for-each select="description">
        <tr><td class="{$projectfirstColStyle}">
          Design Description:
          </td>
          <td>
             <xsl:call-template name="text"/>
         </td>
       </tr>
      </xsl:for-each>
      <xsl:for-each select="citation">
        <tr><td class="{$projectfirstColStyle}">
          Design Citation:
          </td>
          <td >
             <xsl:call-template name="citation">
               <xsl:with-param name="citationfirstColStyle" select="$projectfirstColStyle"/>
               <xsl:with-param name="citationsubHeaderStyle" select="$subHeaderStyle"/>
             </xsl:call-template>
         </td>
       </tr>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="projectrelatedproject">
    <xsl:param name="projectfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: projectrelatedproject</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="relatedProject">
       <tr><td class="{$projectfirstColStyle}">
          Related Project:
          </td>
          <td>
            <xsl:call-template name="project">
              <xsl:with-param name="projectfirstColStyle" select="$projectfirstColStyle"/>
            </xsl:call-template>
         </td>
       </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- eml-protocol-2.0.0.xsl -->
  <!-- 
    Here, protocol is more/less synonomous with ProcedureStepType, 
    which is most of what composes methodStep 
  -->
  <xsl:template name="protocol">
    <xsl:param name="protocolfirstColStyle"/>
    <xsl:param name="protocolsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: protocol</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
        <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="protocolcommon">
              <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
              <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
           <xsl:call-template name="protocolcommon">
              <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
              <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
           </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
     </table>
  </xsl:template>

  <xsl:template name="protocolcommon">
    <xsl:param name="protocolfirstColStyle"/>
    <xsl:param name="protocolsubHeaderStyle"/> 
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: protocolcommon</xsl:text></xsl:message></xsl:if>
    <!-- template for protocol shows minimum elements (author, title, dist) -->
    <xsl:call-template name="protocol_simple">
      <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
      <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
    </xsl:call-template>
    <xsl:for-each select="proceduralStep">
      <tr>
        <td colspan="2" class="{$protocolsubHeaderStyle}">Step<xsl:text> </xsl:text><xsl:value-of select="position()"/>:</td>
      </tr>
      <xsl:call-template name="step">
        <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
        <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:call-template name="protocolAccess">
      <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
      <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
    </xsl:call-template>
  </xsl:template>

  <!-- this template creates a small table for a protocol tree with minimum required 
       content (title/creator/distribution). Only called in this stylesheet. It would be
       better to reuse the resource templates? but those currently are written for 
       toplevel, and that style is too prominent for this location. use modes? 
       but all calls to resource templates would be affected.
  -->
  <xsl:template name="protocol_simple">
    <xsl:param name="protocolfirstColStyle"/>
    <xsl:param name="protocolsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: protocol_simple</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="creator/individualName/surName">
      <tr>	
				<td class="{$protocolfirstColStyle}"><xsl:text>Author: </xsl:text></td>
				<td><xsl:value-of select="."/></td>
      </tr>
  	</xsl:for-each>
    <xsl:for-each select="title">
      <tr>
        <td class="{$protocolfirstColStyle}"><xsl:text>Title: </xsl:text></td>
        <td><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="distribution">
      <!--the template 'distribution' in eml-distribution.2.0.0.xsl. seems to be for
			  	data tables. use the resourcedistribution template instead (eml-resource.2.0.0.xsl) -->
      <xsl:call-template name="resourcedistribution">
        <xsl:with-param name="resfirstColStyle" select="$protocolfirstColStyle"/>
        <xsl:with-param name="ressubHeaderStyle" select="$protocolsubHeaderStyle"/> 
	    </xsl:call-template>
    </xsl:for-each>
	</xsl:template>

  <!-- 'step' refers to ProcedureStepType, i.e., methodStep (w/o optional dataSource)
	     (called from method.xsl, and here, from nested subStep)
	     mob added the table element to box each step -->
  <xsl:template name="step">
    <xsl:param name="protocolfirstColStyle"/>
    <xsl:param name="protocolsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: step</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:for-each select="description">
        <xsl:variable name="title" select="../dataSource/title"/>
        <tr>
          <td class="{$protocolfirstColStyle}">Description:</td>
          <td>
          <xsl:if test="($title) and normalize-space($title[1]) != ''">
            <h4>Provenance Metadata - The following data source was used in the creation of this product:</h4>
          </xsl:if>
          <xsl:variable name="url" select="../dataSource/distribution/online/url"/>
          <xsl:choose>
            <xsl:when test="(./para/literalLayout[1] = $prov-stmt) or (./para[1] = $prov-stmt)">
               <p class="eml"><xsl:value-of select="../dataSource/title"/> (<a href="./metadataviewer?url={$url}" target="_blank">Click here to view metadata</a>)</p>
            </xsl:when>
            <xsl:when test="($url) and normalize-space($url[1]) != ''">
               <p class="eml"><xsl:value-of select="../dataSource/title"/> (<a href="{$url}" target="_blank">Click here to view data source</a>)</p>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="text">
                <xsl:with-param name="textfirstColStyle" select="$protocolfirstColStyle"/>
              </xsl:call-template>
            </xsl:otherwise>
          </xsl:choose>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="citation">
        <tr>
          <td class="{$protocolfirstColStyle}">Citation:</td>
          <td colspan="2">
            <xsl:call-template name="citation">
              <xsl:with-param name="citationfirstColStyle" select="$protocolfirstColStyle"/>
              <xsl:with-param name="citationsubHeaderStyle" select="$protocolsubHeaderStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="protocol">
        <tr>
          <td class="{$protocolfirstColStyle}">Protocol:</td>
          <td class="{$secondColStyle}">    
					<!-- mob nested this table in col2, instead of new row. -->
	          <xsl:call-template name="protocol">
              <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
              <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
            </xsl:call-template>
          </td>
	      </tr>  
      </xsl:for-each>
      <xsl:for-each select="instrumentation">
        <tr>
          <td class="{$protocolfirstColStyle}">Instrument(s):</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>        
      </xsl:for-each>	
      <xsl:for-each select="software">
        <tr>
          <td colspan="2">
            <xsl:call-template name="software">
              <xsl:with-param name="softwarefirstColStyle" select="$protocolfirstColStyle"/>
              <xsl:with-param name="softwaresubHeaderStyle" select="$protocolsubHeaderStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="subStep">
        <tr>
          <td class="{$protocolfirstColStyle}">Substep<xsl:text> </xsl:text><xsl:value-of select="position()"/></td>
          <td class="{$secondColStyle}">&#160;</td>
					<td>  <!-- correct? was outside of table -->
            <xsl:call-template name="step">
						  <xsl:with-param name="protocolfirstColStyle" select="$protocolfirstColStyle"/>
						  <xsl:with-param name="protocolsubHeaderStyle" select="$protocolsubHeaderStyle"/>
						</xsl:call-template>
					</td>
		    </tr>						
      </xsl:for-each>
		</table>
  </xsl:template> 
  
	<!-- ? needed? no access elements here. -->
  <xsl:template name="protocolAccess">
    <xsl:param name="protocolfirstColStyle"/>
    <xsl:param name="protocolsubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: protocolAccess</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="access">
      <tr>
        <td colspan="2">
          <xsl:call-template name="access">
            <xsl:with-param name="accessfirstColStyle" select="$protocolfirstColStyle"/>
            <xsl:with-param name="accesssubHeaderStyle" select="$protocolsubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- eml-resource-2.0.0.xsl -->
  <!-- This module is for resouce and it is self-contained (it is table)-->
  <xsl:template name="resource">
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressubHeaderStyle"/>
    <xsl:param name="creator">Data Package Creator(s):</xsl:param>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resource</xsl:text></xsl:message></xsl:if>
    
      <xsl:for-each select="alternateIdentifier">
        <xsl:call-template name="resourcealternateIdentifier">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="shortName">
        <xsl:call-template name="resourceshortName">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
         </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="title">
        <xsl:call-template name="resourcetitle">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
       <xsl:for-each select="pubDate">
        <xsl:call-template name="resourcepubDate" >
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
         </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="language">
        <xsl:call-template name="resourcelanguage" >
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
         </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="series">
        <xsl:call-template name="resourceseries" >
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:if test="creator">
        <tr>
          <td class="{$ressubHeaderStyle}" colspan="2">
            <h3><xsl:value-of select="$creator"/></h3>
          </td>
        </tr>
      </xsl:if>
      <xsl:for-each select="creator">
        <xsl:call-template name="resourcecreator">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:if test="metadataProvider">
        <tr><td class="{$ressubHeaderStyle}" colspan="2">
        <xsl:text>Metadata Provider(s):</xsl:text>
      </td></tr>
      </xsl:if>
       <xsl:for-each select="metadataProvider">
        <xsl:call-template name="resourcemetadataProvider">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:if test="associatedParty">
        <tr>
          <td class="{$ressubHeaderStyle}" colspan="2">
            <h3><xsl:text>Associated Parties:</xsl:text></h3>
          </td>
        </tr>
      </xsl:if>
      <xsl:for-each select="associatedParty">
        <xsl:call-template name="resourceassociatedParty">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="abstract">
        <xsl:call-template name="resourceabstract" >
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:if test="keywordSet">
        <tr><td class="{$ressubHeaderStyle}" colspan="2">
             <xsl:text>Keywords:</xsl:text></td></tr>
      </xsl:if>
      <xsl:for-each select="keywordSet">
        <xsl:call-template name="resourcekeywordSet" >
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="additionalInfo">
        <xsl:call-template name="resourceadditionalInfo" >
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="ressubHeaderStyle" select="$ressubHeaderStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="intellectualRights">
        <xsl:call-template name="resourceintellectualRights" >
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="ressecondColStyle" select="$secondColStyle"/>
        </xsl:call-template>
      </xsl:for-each>
      <xsl:for-each select="distribution">
        <xsl:call-template name="resourcedistribution">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="ressubHeaderStyle" select="$ressubHeaderStyle"/>
	        <xsl:with-param name="index" select="position()"/>
          <xsl:with-param name="docid" select="$docid"/>
        </xsl:call-template>
      </xsl:for-each>
    <xsl:for-each select="coverage">
      <xsl:call-template name="resourcecoverage">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="ressubHeaderStyle" select="$ressubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>

  </xsl:template>

  <!-- style the alternate identifier elements -->
  <xsl:template name="resourcealternateIdentifier" >
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcealternateIdentifier</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$resfirstColStyle}">Alternate Identifier:</td>
        <td class="{$ressecondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- style the short name elements -->
  <xsl:template name="resourceshortName">
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourceshortName</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$resfirstColStyle}">Short Name:</td>
        <td class="{$ressecondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- style the title element -->
  <xsl:template name="resourcetitle" >
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcetitle</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <tr>
        <td class="{$resfirstColStyle}">Title:</td>
        <td class="{$ressecondColStyle}"><cite><xsl:value-of select="."/></cite></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="resourcecreator" >
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcecreator</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2">
        <xsl:call-template name="party">
          <xsl:with-param name="partyfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="resourcemetadataProvider" >
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcemetadataProvider</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2">
        <xsl:call-template name="party">
          <xsl:with-param name="partyfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="resourceassociatedParty">
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourceassociatedParty</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2">
        <xsl:call-template name="party">
          <xsl:with-param name="partyfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="resourcepubDate">
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcepubDate</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(../pubDate)!=''">
      <tr>
        <td class="{$resfirstColStyle}">Publication&#160;Date:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="../pubDate"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="resourcelanguage">
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcelanguage</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.) != ''">
      <tr>
        <td class="{$resfirstColStyle}">Language:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="resourceseries">
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourceseries</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(../series)!=''">
      <tr>
        <td class="{$resfirstColStyle}">Series:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="../series"/></td>
      </tr>
    </xsl:if>
  </xsl:template>

  <xsl:template name="resourceabstract">
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourceabstract</xsl:text></xsl:message></xsl:if>
    <tr>
       <td class="{$resfirstColStyle}"><xsl:text>Abstract:</xsl:text></td>
       <td>
         <xsl:call-template name="abstracttext">
           <xsl:with-param name="textfirstColStyle" select="$resfirstColStyle"/>
           <xsl:with-param name="textsecondColStyle" select="$ressecondColStyle"/>
         </xsl:call-template>
       </td>
     </tr>
  </xsl:template>

  <!--<xsl:template match="keywordSet[1]" mode="resource">
        <xsl:param name="ressubHeaderStyle"/>
        <xsl:param name="resfirstColStyle"/>
        <tr><td class="{$ressubHeaderStyle}" colspan="2">
        <xsl:text>Keywords:</xsl:text></td></tr>
        <xsl:call-template name="renderKeywordSet">
          <xsl:with-param name="resfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
  </xsl:template>-->

  <xsl:template name="resourcekeywordSet">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcekeywordSet</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="keywordThesaurus">
      <!--
	    <xsl:if test="normalize-space(.)!=''">
        <xsl:value-of select="."/>
        <xsl:text>: </xsl:text>
      </xsl:if>
      -->
      <xsl:if test="normalize-space(keyword)!=''">
        <ul>
          <xsl:for-each select="keyword">
            <li><xsl:value-of select="."/>
            <!--
            <xsl:if test="./@keywordType and normalize-space(./@keywordType)!=''">
              (<xsl:value-of select="./@keywordType"/>)
            </xsl:if>
            -->
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>
    </xsl:for-each>
    <xsl:if test="normalize-space(keyword)!=''">
      <ul>
        <xsl:for-each select="keyword">
          <li><xsl:value-of select="."/>
          <!--            <xsl:if test="./@keywordType and normalize-space(./@keywordType)!=''">
            (<xsl:value-of select="./@keywordType"/>)
          </xsl:if>
          -->
          </li>
        </xsl:for-each>
      </ul>
    </xsl:if>
  </xsl:template>

  <xsl:template name="resourcekeywordsAsPara">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcekeywordsAsPara</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(keyword[1])!=''">
      <xsl:for-each select="keyword">
        <xsl:value-of select="."/>
        <xsl:if test="position() != last()">
          <xsl:text>,&#160;</xsl:text>
        </xsl:if>
        <!-- don't print the icky-looking attribute!            
        <xsl:if test="./@keywordType and normalize-space(./@keywordType)!=''">
          (<xsl:value-of select="./@keywordType"/>)
        </xsl:if>
        -->      
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="resourceadditionalInfo">
    <xsl:param name="ressubHeaderStyle"/>
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourceadditionalInfo</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$ressubHeaderStyle}" colspan="2"><xsl:text>Additional Information:</xsl:text></td>
    </tr>
    <tr>
      <td class="{$resfirstColStyle}">&#160;</td>
      <td>
        <xsl:call-template name="text">
          <xsl:with-param name="textfirstColStyle" select="$resfirstColStyle"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <xsl:template name="resourceintellectualRights">
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="ressecondColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourceintellectualRights</xsl:text></xsl:message></xsl:if>
    <xsl:call-template name="text">
      <xsl:with-param name="textsecondColStyle" select="$ressecondColStyle"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="resourcedistribution">
    <xsl:param name="ressubHeaderStyle"/>
    <xsl:param name="resfirstColStyle"/>
    <xsl:param name="index"/>
    <xsl:param name="docid"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcedistribution</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2">
        <xsl:call-template name="distribution">
          <xsl:with-param name="disfirstColStyle" select="$resfirstColStyle"/>
          <xsl:with-param name="dissubHeaderStyle" select="$ressubHeaderStyle"/>
          <xsl:with-param name="level">toplevel</xsl:with-param>
          <xsl:with-param name="distributionindex" select="$index"/>
          <xsl:with-param name="docid" select="$docid"/>
        </xsl:call-template>
     </td>
    </tr>
  </xsl:template>

  <xsl:template name="resourcecoverage">
    <xsl:param name="ressubHeaderStyle"/>
    <xsl:param name="resfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: resourcecoverage</xsl:text></xsl:message></xsl:if>
    <tr>
      <td colspan="2">
        <xsl:call-template name="coverage"></xsl:call-template>
     </td>
    </tr>
  </xsl:template>

  <!-- eml-software-2.0.0.xsl -->
  <xsl:template name="software">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:param name="softwaresubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: software</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
        <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="softwarecommon">
              <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
              <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
            <xsl:call-template name="softwarecommon">
              <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
              <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
            </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="softwarecommon">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:param name="softwaresubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: softwarecommon</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$softwaresubHeaderStyle}" colspan="2">
        <xsl:text>Software:</xsl:text></td></tr>
        <xsl:call-template name="resource">
          <xsl:with-param name="resfirstColStyle" select="$softwarefirstColStyle"/>
          <xsl:with-param name="ressubHeaderStyle" select="$softwaresubHeaderStyle"/>
          <xsl:with-param name="creator">Author(s):</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="implementation">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
        </xsl:call-template>
        <xsl:for-each select="dependency">
          <tr>
            <td class="{$softwarefirstColStyle}">Dependency</td>
            <td class="{$secondColStyle}">&#160;</td>
          </tr>
          <xsl:call-template name="dependency">
            <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
            <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
          </xsl:call-template>
        </xsl:for-each>
        <xsl:call-template name="licenseURL">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <!-- <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/> -->
        </xsl:call-template>
        <xsl:call-template name="license">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <!-- <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/> -->
        </xsl:call-template>
        <xsl:call-template name="version">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <!-- <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/> -->
        </xsl:call-template>
        <xsl:call-template name="softwareAccess">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
        </xsl:call-template>
        <xsl:call-template name="softwareProject">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
        </xsl:call-template>
  </xsl:template>

  <xsl:template name="implementation">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:param name="softwaresubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: implementation</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="implementation">
      <tr>
        <td colspan="2" class="{$softwaresubHeaderStyle}">Implementation Info:</td>
      </tr>
      <xsl:for-each select="distribution">
        <tr>
          <td class="{$softwarefirstColStyle}">Distribution:</td>
          <td>
            <xsl:call-template name="distribution">
              <xsl:with-param name="disfirstColStyle" select="$softwarefirstColStyle"/>
              <xsl:with-param name="dissubHeaderStyle" select="$softwaresubHeaderStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="size">
        <tr>
          <td class="{$softwarefirstColStyle}">Size:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="language">
        <tr>
          <td class="{$softwarefirstColStyle}">Language:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="LanguageValue"/></td>
        </tr>
        <xsl:if test="LanguageCodeStandard">
          <tr>
            <td class="{$softwarefirstColStyle}">Language Code Standard:</td>
            <td class="{$secondColStyle}"><xsl:value-of select="LanguageValue"/></td>
          </tr>
        </xsl:if>
      </xsl:for-each>
      <xsl:for-each select="operatingSystem">
        <tr>
          <td class="{$softwarefirstColStyle}">Operating System:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="machineProcessor">
        <tr>
          <td class="{$softwarefirstColStyle}">Operating System:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="virtualMachine">
        <tr>
          <td class="{$softwarefirstColStyle}">Virtual Machine:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="diskUsage">
        <tr>
          <td class="{$softwarefirstColStyle}">Disk Usage:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="runtimeMemoryUsage">
        <tr>
          <td class="{$softwarefirstColStyle}">Run Time Memory Usage:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="programmingLanguage">
        <tr>
          <td class="{$softwarefirstColStyle}">Programming Language:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="checksum">
        <tr>
          <td class="{$softwarefirstColStyle}">Check Sum:</td>
          <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="dependency">
        <tr>
          <td class="{$softwarefirstColStyle}">Dependency:</td>
          <td class="{$secondColStyle}">&#160;</td>
        </tr>
        <xsl:call-template name="dependency">
          <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
          <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="dependency">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:param name="softwaresubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: dependency</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="../dependency">
      <tr>
        <td class="{$softwarefirstColStyle}"><strong><xsl:value-of select="action"/></strong> <xsl:text> Depend on</xsl:text></td>
        <td>
          <xsl:for-each select="software">
            <xsl:call-template name="software">
              <xsl:with-param name="softwarefirstColStyle" select="$softwarefirstColStyle"/>
              <xsl:with-param name="softwaresubHeaderStyle" select="$softwaresubHeaderStyle"/>
            </xsl:call-template>
          </xsl:for-each>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="version">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: version</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="version">
      <tr>
        <td class="{$firstColStyle}">Version Number:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="licenseURL">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: licenseURL</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="licenseURL">
      <tr>
        <td class="{$firstColStyle}">License URL:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="license">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: license</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="license">
      <tr>
        <td class="{$firstColStyle}">License:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="softwareAccess">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:param name="softwaresubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: softwareAccess</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="access">
      <tr>
        <td colspan="2">
          <xsl:call-template name="access">
            <xsl:with-param name="accessfirstColStyle" select="$softwarefirstColStyle"/>
            <xsl:with-param name="accesssubHeaderStyle" select="$softwaresubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="softwareProject">
    <xsl:param name="softwarefirstColStyle"/>
    <xsl:param name="softwaresubHeaderStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: softwareProject</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="project">
      <tr>
        <td class="{$softwaresubHeaderStyle}" colspan="2"><xsl:text>Project Info:</xsl:text></td>
      </tr>
      <tr>
        <td colspan="2">
          <xsl:call-template name="project">
            <xsl:with-param name="projectfirstColStyle" select="$softwarefirstColStyle"/>
            <!-- <xsl:with-param name="projectsubHeaderStyle" select="$softwaresubHeaderStyle"/> -->
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- eml-spatialraster-2.0.0.xsl -->
  <!-- This module is for datatable module-->
  <xsl:template name="spatialRaster">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:param name="spatialrastersubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialRaster</xsl:text></xsl:message></xsl:if>
    <hr></hr>
    <h2>Spatial Raster</h2>
    <table class="{$tabledefaultStyle}">
        <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="spatialRastercommon">
             <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
             <xsl:with-param name="spatialrastersubHeaderStyle" select="$spatialrastersubHeaderStyle"/>
             <xsl:with-param name="docid" select="$docid"/>
             <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
           <xsl:call-template name="spatialRastercommon">
             <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
             <xsl:with-param name="spatialrastersubHeaderStyle" select="$spatialrastersubHeaderStyle"/>
             <xsl:with-param name="docid" select="$docid"/>
             <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
      </table>
  </xsl:template>

  <xsl:template name="spatialRastercommon">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:param name="spatialrastersubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialRastercommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="entityName">
      <xsl:call-template name="entityName">
        <xsl:with-param name="entityfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="alternateIdentifier">
      <xsl:call-template name="entityalternateIdentifier">
        <xsl:with-param name="entityfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityDescription">
      <xsl:call-template name="entityDescription">
        <xsl:with-param name="entityfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="additionalInfo">
      <xsl:call-template name="entityadditionalInfo">
        <xsl:with-param name="entityfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <!-- call physical moduel without show distribution(we want see it later)-->
    <xsl:if test="physical">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Physical Structure Description:</td>
      </tr>
      <xsl:for-each select="physical">
        <tr>
          <td colspan="2">
            <xsl:call-template name="physical">
              <xsl:with-param name="physicalfirstColStyle" select="$spatialrasterfirstColStyle"/>
              <xsl:with-param name="notshowdistribution">yes</xsl:with-param>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="coverage">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Coverage Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="coverage">
      <tr>
        <td colspan="2">
          <xsl:call-template name="coverage">
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="method | methods">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Method Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="method | methods">
      <tr>
        <td colspan="2">
          <xsl:call-template name="method">
            <xsl:with-param name="methodfirstColStyle" select="$spatialrasterfirstColStyle"/>
            <xsl:with-param name="methodsubHeaderStyle" select="$spatialrastersubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="constraint">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Constraint:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="constraint">
      <tr>
        <td colspan="2">
          <xsl:call-template name="constraint">
            <xsl:with-param name="constraintfirstColStyle" select="$spatialrasterfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="spatialReference">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Spatial Reference:</td>
      </tr>
      <xsl:call-template name="spatialReference">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="georeferenceInfo">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Grid Postion:</td>
      </tr>
      <xsl:call-template name="georeferenceInfo">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="horizontalAccuracy">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Horizontal Accuracy:</td>
      </tr>
      <xsl:call-template name="dataQuality">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="verticalAccuracy">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Vertical Accuracy:</td>
      </tr>
      <xsl:call-template name="dataQuality">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="cellSizeXDirection">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Cell Size(X):</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="cellSizeYDirection">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Cell Size(Y):</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="numberOfBands">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Number of Bands:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="rasterOrigin">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Origin:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="columns">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Max Raster Objects(X):</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="rows">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Max Raster Objects(Y):</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="verticals">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Max Raster Objects(Z):</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="cellGeometry">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Cell Geometry:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="toneGradation">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Number of Colors:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="scaleFactor">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Scale Factor:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="offset">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Offset:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="imageDescription">
      <tr>
        <td class="{$spatialrastersubHeaderStyle}" colspan="2">Image Info:</td>
      </tr>
      <xsl:call-template name="imageDescription">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:if test="$withAttributes='1'">
      <xsl:for-each select="attributeList">
        <xsl:call-template name="spatialRasterAttributeList">
          <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
          <xsl:with-param name="spatialrastersubHeaderStyle" select="$spatialrastersubHeaderStyle"/>
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <!-- Here to display distribution info-->
    <xsl:for-each select="physical">
      <xsl:call-template name="spatialRasterShowDistribution">
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
        <xsl:with-param name="physicalindex" select="position()"/>
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
        <xsl:with-param name="spatialrastersubHeaderStyle" select="$spatialrastersubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <!-- spatial reference -->
  <xsl:template name="spatialReference">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialReference</xsl:text></xsl:message></xsl:if>
    <xsl:choose>
      <xsl:when test="references!=''">
        <xsl:variable name="ref_id" select="references"/>
        <xsl:variable name="references" select="$ids[@id=$ref_id]" />
        <xsl:for-each select="$references">
          <xsl:call-template name="spatialReferenceCommon">
            <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
          </xsl:call-template>
        </xsl:for-each>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="spatialReferenceCommon">
          <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="spatialReferenceCommon">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialReferenceCommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="horizCoordSysName">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Name of Coordinate System:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="horizCoordSysDef/geogCoordSys">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">
          Definition of <xsl:text> </xsl:text><xsl:value-of select="../@name"/> <xsl:text> </xsl:text> (Geographic Coordinate System):
        </td>
        <td>
          <xsl:call-template name="geogCoordSysType">
            <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="horizCoordSysDef/projCoordSys">
      <xsl:for-each select="geogCoordSys">
        <tr>
          <td class="{$spatialrasterfirstColStyle}">
            Definition of<xsl:text> </xsl:text><xsl:value-of select="../../@name"/><xsl:text> </xsl:text>(Geographic Coordinate System):
          </td>
          <td>
            <xsl:call-template name="geogCoordSysType">
              <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
            </xsl:call-template>
          </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="projection">
        <tr>
          <td class="{$spatialrasterfirstColStyle}">Projection in Geo Coord. System:</td>
          <td>
            <table class="{$tabledefaultStyle}">
              <xsl:for-each select="parameter">
                <tr>
                  <td class="{$spatialrasterfirstColStyle}"><xsl:value-of select="./@name"/>:</td>
                  <td>
                    <table class="{$tabledefaultStyle}">
                      <tr>
                        <td class="{$secondColStyle}"><xsl:value-of select="./@value"/></td>
                        <td class="{$secondColStyle}"><xsl:value-of select="./@description"/></td>
                      </tr>
                    </table>
                  </td>
                </tr>
              </xsl:for-each>
              <xsl:for-each select="unit">
                <tr>
                  <td class="{$spatialrasterfirstColStyle}">Unit:</td>
                  <td class="{$secondColStyle}"><xsl:value-of select="./@name"/></td>
                </tr>
              </xsl:for-each>
            </table>
          </td>
        </tr>
      </xsl:for-each>
    </xsl:for-each>
    <xsl:for-each select="vertCoordSys/altitudeSysDef">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Altitude System Definition:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:for-each select="altitudeDatumName">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Datum:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="altitudeResolution">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Resolution:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="altitudeDistanceUnits">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Distance Unit:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="altitudeEncodingMethod">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Encoding Method:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="vertCoordSys/depthSysDef">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Depth System Definition:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:for-each select="depthDatumName">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Datum:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="depthResolution">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Resolution:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="depthDistanceUnits">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Distance Unit:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="depthEncodingMethod">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Encoding Method:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="geogCoordSysType">
   <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: geogCoordSysType</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:for-each select="datum">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Datum:
            </td>
            <td class="{$secondColStyle}">
              <xsl:value-of select="./@name"/>
            </td>
        </tr>
      </xsl:for-each>
      <xsl:for-each select="spheroid">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Spheroid:
            </td>
            <td>
               <table class="{$tabledefaultStyle}">
                  <tr><td class="{$spatialrasterfirstColStyle}">
                       Name:
                       </td>
                       <td class="{$secondColStyle}">
                        <xsl:value-of select="./@name"/>
                       </td>
                   </tr>
                   <tr><td class="{$spatialrasterfirstColStyle}">
                       Semi Axis Major:
                       </td>
                       <td class="{$secondColStyle}">
                        <xsl:value-of select="./@semiAxisMajor"/>
                       </td>
                   </tr>
                   <tr><td class="{$spatialrasterfirstColStyle}">
                       Denom Flat Ratio:
                       </td>
                       <td class="{$secondColStyle}">
                        <xsl:value-of select="./@denomFlatRatio"/>
                       </td>
                   </tr>
               </table>
            </td>
        </tr>
      </xsl:for-each>
       <xsl:for-each select="primeMeridian">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Prime Meridian:
            </td>
            <td>
               <table class="{$tabledefaultStyle}">
                  <tr><td class="{$spatialrasterfirstColStyle}">
                       Name:
                       </td>
                       <td class="{$secondColStyle}">
                        <xsl:value-of select="./@name"/>
                       </td>
                   </tr>
                   <tr><td class="{$spatialrasterfirstColStyle}">
                       Longitude:
                       </td>
                       <td class="{$secondColStyle}">
                        <xsl:value-of select="./@longitude"/>
                       </td>
                   </tr>
               </table>
            </td>
        </tr>
      </xsl:for-each>
     <xsl:for-each select="unit">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Unit:
            </td>
            <td class="{$secondColStyle}">
              <xsl:value-of select="./@name"/>
            </td>
        </tr>
      </xsl:for-each>
   </table>
  </xsl:template>

  <!-- georeferenceinfo -->
  <xsl:template name="georeferenceInfo">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: georeferenceInfo</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="cornerPoint">
        <tr><td class="{$spatialrasterfirstColStyle}">
            Corner Point:
            </td>
            <td>
               <table class="{$tabledefaultStyle}">
                 <xsl:for-each select="corner">
                     <tr><td class="{$spatialrasterfirstColStyle}">
                          Corner:
                         </td>
                         <td class="{$secondColStyle}">
                            <xsl:value-of select="."/>
                          </td>
                      </tr>
                 </xsl:for-each>
                 <xsl:for-each select="xCoordinate">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          xCoordinate:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
                <xsl:for-each select="yCoordinate">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          yCoordinate:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
                <xsl:for-each select="pointInPixel">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          Point in Pixel:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
              </table>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="controlPoint">
       <tr><td class="{$spatialrasterfirstColStyle}">
            Control Point:
            </td>
            <td>
               <table class="{$tabledefaultStyle}">
                 <xsl:for-each select="column">
                     <tr><td class="{$spatialrasterfirstColStyle}">
                          Column Location:
                         </td>
                         <td class="{$secondColStyle}">
                            <xsl:value-of select="."/>
                          </td>
                      </tr>
                 </xsl:for-each>
                 <xsl:for-each select="row">
                     <tr><td class="{$spatialrasterfirstColStyle}">
                          Row Location:
                         </td>
                         <td class="{$secondColStyle}">
                            <xsl:value-of select="."/>
                          </td>
                      </tr>
                 </xsl:for-each>
                 <xsl:for-each select="xCoordinate">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          xCoordinate:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
                <xsl:for-each select="yCoordinate">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          yCoordinate:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
                <xsl:for-each select="pointInPixel">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          Point in Pixel:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
              </table>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="bilinearFit">
       <tr><td class="{$spatialrasterfirstColStyle}">
            Bilinear Fit:
            </td>
            <td>
               <table class="{$tabledefaultStyle}">
                 <xsl:for-each select="xIntercept">
                     <tr><td class="{$spatialrasterfirstColStyle}">
                          X Intercept:
                         </td>
                         <td class="{$secondColStyle}">
                            <xsl:value-of select="."/>
                          </td>
                      </tr>
                 </xsl:for-each>
                 <xsl:for-each select="xSlope">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          X Slope:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
                <xsl:for-each select="yIntercept">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          Y Intercept:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
                <xsl:for-each select="ySlope">
                    <tr><td class="{$spatialrasterfirstColStyle}">
                          Y Slope:
                        </td>
                        <td class="{$secondColStyle}">
                           <xsl:value-of select="."/>
                        </td>
                   </tr>
                </xsl:for-each>
              </table>
            </td>
       </tr>
    </xsl:for-each>
 </xsl:template>

  <!-- data quality -->
  <xsl:template name="dataQuality">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: dataQuality</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="accuracyReport">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Report:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:if test="quantitativeAccuracyReport">
      <tr>
        <td class="{$spatialrasterfirstColStyle}">Quantitative Report:</td>
        <td>
          <table class="{$tabledefaultStyle}">
            <xsl:for-each select="quantitativeAccuracyReport">
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Accuracy Value:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="quantitativeAccuracyValue"/></td>
              </tr>
              <tr>
                <td class="{$spatialrasterfirstColStyle}">Method:</td>
                <td class="{$secondColStyle}"><xsl:value-of select="quantitativeAccuracyMethod"/></td>
              </tr>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </xsl:if>
  </xsl:template>

  <!-- imageDescription -->
  <xsl:template name="imageDescription">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: imageDescription</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="illuminationElevationAngle">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Illumination Elevation:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="illuminationAzimuthAngle">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Illumination Azimuth:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="imageOrientationAngle">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Image Orientation:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="imagingCondition">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Code Affectting Quality of Image:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="imageQualityCode">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Quality:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="cloudCoverPercentage">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Cloud Coverage:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="preProcessingTypeCode">
        <tr><td class="{$spatialrasterfirstColStyle}">
             PreProcessing:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="compressionGenerationQuality">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Compression Quality:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="triangulationIndicator">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Triangulation Indicator:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="radionmetricDataAvailability">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Availability of Radionmetric Data:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="cameraCalibrationInformationAvailability">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Availability of Camera Calibration Correction:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="filmDistortionInformationAvailability">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Availability of Calibration Reseau:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="lensDistortionInformationAvailability">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Availability of Lens Aberration Correction:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="bandDescription">
     <tr><td class="{$spatialrasterfirstColStyle}">
             Availability of Lens Aberration Correction:
            </td>
            <td>
               <xsl:call-template name="bandDescription">
                  <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialrasterfirstColStyle"/>
               </xsl:call-template>
            </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <!-- band description -->
  <xsl:template name="bandDescription">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: bandDescription</xsl:text></xsl:message></xsl:if>
    <table class="{$tabledefaultStyle}">
      <xsl:for-each select="sequenceIdentifier">
        <tr><td class="{$spatialrasterfirstColStyle}">
            Sequence Identifier:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
        </tr>
     </xsl:for-each>
     <xsl:for-each select="highWavelength">
        <tr><td class="{$spatialrasterfirstColStyle}">
             High Wavelength:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
        </tr>
     </xsl:for-each>
     <xsl:for-each select="lowWaveLength">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Low Wavelength:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
        </tr>
     </xsl:for-each>
     <xsl:for-each select="waveLengthUnits">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Wavelength Units:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
        </tr>
     </xsl:for-each>
     <xsl:for-each select="peakResponse">
        <tr><td class="{$spatialrasterfirstColStyle}">
             Peak Response:
            </td>
            <td class="{$secondColStyle}">
                <xsl:value-of select="."/>
            </td>
        </tr>
     </xsl:for-each>
    </table>
  </xsl:template>

  <xsl:template name="spatialRasterShowDistribution">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:param name="spatialrastersubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entitylevel</xsl:param>
    <xsl:param name="entitytype">spatialRaster</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialRasterShowDistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <tr>
        <td colspan="2">
          <xsl:call-template name="distribution">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="physicalindex" select="$physicalindex"/>
            <xsl:with-param name="distributionindex" select="position()"/>
            <xsl:with-param name="disfirstColStyle" select="$spatialrasterfirstColStyle"/>
            <xsl:with-param name="dissubHeaderStyle" select="$spatialrastersubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spatialRasterAttributeList">
    <xsl:param name="spatialrasterfirstColStyle"/>
    <xsl:param name="spatialrastersubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype">spatialRaster</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialRasterAttributeList</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$spatialrastersubHeaderStyle}" colspan="2"><xsl:text>Attribute(s) Info:</xsl:text></td>
    </tr>
    <tr>
      <td colspan="2">
        <xsl:call-template name="attributelist">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <!-- eml-spatialvector-2.0.0.xsl -->
  <!-- This module is for datatable module-->
  <xsl:template name="spatialVector">
    <xsl:param name="spatialvectorfirstColStyle"/>
    <xsl:param name="spatialvectorsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialVector</xsl:text></xsl:message></xsl:if>
    <hr></hr>
    <h2>Spatial Vector</h2>
    <table class="{$tabledefaultStyle}">
        <xsl:choose>
         <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="spatialVectorcommon">
             <xsl:with-param name="spatialvectorfirstColStyle" select="$spatialvectorfirstColStyle"/>
             <xsl:with-param name="spatialvectorsubHeaderStyle" select="$spatialvectorsubHeaderStyle"/>
             <xsl:with-param name="docid" select="$docid"/>
             <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
           <xsl:call-template name="spatialVectorcommon">
             <xsl:with-param name="spatialvectorfirstColStyle" select="$spatialvectorfirstColStyle"/>
             <xsl:with-param name="spatialvectorsubHeaderStyle" select="$spatialvectorsubHeaderStyle"/>
             <xsl:with-param name="docid" select="$docid"/>
             <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
      </table>
  </xsl:template>

  <xsl:template name="spatialVectorcommon">
    <xsl:param name="spatialvectorfirstColStyle"/>
    <xsl:param name="spatialvectorsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialVectorcommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="entityName">
       <xsl:call-template name="entityName">
          <xsl:with-param name="entityfirstColStyle" select="$spatialvectorfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="alternateIdentifier">
       <xsl:call-template name="entityalternateIdentifier">
          <xsl:with-param name="entityfirstColStyle" select="$spatialvectorfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityDescription">
       <xsl:call-template name="entityDescription">
          <xsl:with-param name="entityfirstColStyle" select="$spatialvectorfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="additionalInfo">
       <xsl:call-template name="entityadditionalInfo">
          <xsl:with-param name="entityfirstColStyle" select="$spatialvectorfirstColStyle"/>
       </xsl:call-template>
    </xsl:for-each>
    <!-- call physical moduel without show distribution(we want see it later)-->
    <xsl:if test="physical">
       <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Physical Structure Description:
      </td></tr>
    </xsl:if>
    <xsl:for-each select="physical">
      <tr><td colspan="2">
        <xsl:call-template name="physical">
         <xsl:with-param name="physicalfirstColStyle" select="$spatialvectorfirstColStyle"/>
         <xsl:with-param name="notshowdistribution">yes</xsl:with-param>
        </xsl:call-template>
        </td></tr>
     </xsl:for-each>
    <xsl:if test="coverage">
       <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Coverage Description:
      </td></tr>
    </xsl:if>
    <xsl:for-each select="coverage">
      <tr><td colspan="2">
        <xsl:call-template name="coverage">
        </xsl:call-template>
      </td></tr>
    </xsl:for-each>
      <xsl:if test="method | methods">
       <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Method Description:
      </td></tr>
    </xsl:if>
    <xsl:for-each select="method | methods">
      <tr><td colspan="2">
        <xsl:call-template name="method">
          <xsl:with-param name="methodfirstColStyle" select="$spatialvectorfirstColStyle"/>
          <xsl:with-param name="methodsubHeaderStyle" select="$spatialvectorsubHeaderStyle"/>
        </xsl:call-template>
      </td></tr>
    </xsl:for-each>
    <xsl:if test="constraint">
       <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Constraint:
      </td></tr>
    </xsl:if>
    <xsl:for-each select="constraint">
      <tr><td colspan="2">
        <xsl:call-template name="constraint">
          <xsl:with-param name="constraintfirstColStyle" select="$spatialvectorfirstColStyle"/>
        </xsl:call-template>
      </td></tr>
    </xsl:for-each>
    <xsl:for-each select="geometry">
       <tr><td class="{$spatialvectorfirstColStyle}">
            Geometry:
            </td>
            <td class="{$secondColStyle}">
              <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="geometricObjectCount">
       <tr><td class="{$spatialvectorfirstColStyle}">
            Number of Geometric Objects:
            </td>
            <td class="{$secondColStyle}">
              <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="topologyLevel">
       <tr><td class="{$spatialvectorfirstColStyle}">
           Topolgy Level:
            </td>
            <td class="{$secondColStyle}">
              <xsl:value-of select="."/>
            </td>
       </tr>
    </xsl:for-each>
    <xsl:for-each select="spatialReference">
       <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Spatial Reference:
      </td></tr>
      <xsl:call-template name="spatialReference">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialvectorfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="horizontalAccuracy">
      <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Horizontal Accuracy:
      </td></tr>
      <xsl:call-template name="dataQuality">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialvectorfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="verticalAccuracy">
      <tr><td class="{$spatialvectorsubHeaderStyle}" colspan="2">
        Vertical Accuracy:
      </td></tr>
      <xsl:call-template name="dataQuality">
        <xsl:with-param name="spatialrasterfirstColStyle" select="$spatialvectorfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:if test="$withAttributes='1'">
    <xsl:for-each select="attributeList">
      <xsl:call-template name="spatialVectorAttributeList">
        <xsl:with-param name="spatialvectorfirstColStyle" select="$spatialvectorfirstColStyle"/>
        <xsl:with-param name="spatialvectorsubHeaderStyle" select="$spatialvectorsubHeaderStyle"/>
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
      </xsl:call-template>
    </xsl:for-each>
    </xsl:if>
     <!-- Here to display distribution info-->
    <xsl:for-each select="physical">
       <xsl:call-template name="spatialVectorShowDistribution">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
          <xsl:with-param name="physicalindex" select="position()"/>
          <xsl:with-param name="spatialvectorfirstColStyle" select="$spatialvectorfirstColStyle"/>
          <xsl:with-param name="spatialvectorsubHeaderStyle" select="$spatialvectorsubHeaderStyle"/>
       </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spatialVectorShowDistribution">
    <xsl:param name="spatialvectorfirstColStyle"/>
    <xsl:param name="spatialvectorsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entitylevel</xsl:param>
    <xsl:param name="entitytype">spatialVector</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialVectorShowDistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <tr>
        <td colspan="2">
          <xsl:call-template name="distribution">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="physicalindex" select="$physicalindex"/>
            <xsl:with-param name="distributionindex" select="position()"/>
            <xsl:with-param name="disfirstColStyle" select="$spatialvectorfirstColStyle"/>
            <xsl:with-param name="dissubHeaderStyle" select="$spatialvectorsubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="spatialVectorAttributeList">
    <xsl:param name="spatialvectorfirstColStyle"/>
    <xsl:param name="spatialvectorsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype">spatialVector</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: spatialVectorAttributeList</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$spatialvectorsubHeaderStyle}" colspan="2"><xsl:text>Attribute(s) Info:</xsl:text></td>
    </tr>
    <tr>
      <td colspan="2">
        <xsl:call-template name="attributelist">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <!-- eml-storedprocedure-2.0.0.xsl -->
  <!-- This module is for datatable module-->
  <xsl:template name="storedProcedure">
    <xsl:param name="storedprocedurefirstColStyle"/>
    <xsl:param name="storedproceduresubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: storedProcedure</xsl:text></xsl:message></xsl:if>
    <hr></hr>
    <h2>Stored Procedure</h2>
    <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="storedProcedureCommon">
              <xsl:with-param name="storedprocedurefirstColStyle" select="$storedprocedurefirstColStyle"/>
              <xsl:with-param name="storedproceduresubHeaderStyle" select="$storedproceduresubHeaderStyle"/>
              <xsl:with-param name="docid" select="$docid"/>
              <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <xsl:call-template name="storedProcedureCommon">
            <xsl:with-param name="storedprocedurefirstColStyle" select="$storedprocedurefirstColStyle"/>
            <xsl:with-param name="storedproceduresubHeaderStyle" select="$storedproceduresubHeaderStyle"/>
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
   </table>
  </xsl:template>

  <xsl:template name="storedProcedureCommon">
    <xsl:param name="storedprocedurefirstColStyle"/>
    <xsl:param name="storedproceduresubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: storedProcedureCommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="entityName">
      <xsl:call-template name="entityName">
         <xsl:with-param name="entityfirstColStyle" select="$storedprocedurefirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="alternateIdentifier">
      <xsl:call-template name="entityalternateIdentifier">
        <xsl:with-param name="entityfirstColStyle" select="$storedprocedurefirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityDescription">
      <xsl:call-template name="entityDescription">
        <xsl:with-param name="entityfirstColStyle" select="$storedprocedurefirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="additionalInfo">
      <xsl:call-template name="entityadditionalInfo">
        <xsl:with-param name="entityfirstColStyle" select="$storedprocedurefirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <!-- call physical moduel without show distribution(we want see it later)-->
    <xsl:if test="physical">
      <tr>
        <td class="{$storedproceduresubHeaderStyle}" colspan="2">Physical Structure Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="physical">
      <tr>
        <td colspan="2">
          <xsl:call-template name="physical">
            <xsl:with-param name="physicalfirstColStyle" select="$storedprocedurefirstColStyle"/>
            <xsl:with-param name="notshowdistribution">yes</xsl:with-param>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="coverage">
       <tr>
         <td class="{$storedproceduresubHeaderStyle}" colspan="2">Coverage Description:</td>
       </tr>
    </xsl:if>
    <xsl:for-each select="coverage">
      <tr>
        <td colspan="2"><xsl:call-template name="coverage"></xsl:call-template></td>
      </tr>
    </xsl:for-each>
    <xsl:if test="method | methods">
       <tr>
         <td class="{$storedproceduresubHeaderStyle}" colspan="2">Method Description:</td>
       </tr>
    </xsl:if>
    <xsl:for-each select="method | methods">
      <tr>
        <td colspan="2">
          <xsl:call-template name="method">
            <xsl:with-param name="methodfirstColStyle" select="$storedprocedurefirstColStyle"/>
            <xsl:with-param name="methodsubHeaderStyle" select="$storedproceduresubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="constraint">
      <tr>
        <td class="{$storedproceduresubHeaderStyle}" colspan="2">Constraint:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="constraint">
      <tr>
        <td colspan="2">
          <xsl:call-template name="constraint">
            <xsl:with-param name="constraintfirstColStyle" select="$storedprocedurefirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="parameter">
      <tr>
        <td width="{$firstColWidth}" class="{$storedprocedurefirstColStyle}">Parameter:</td>
        <td width="{$secondColWidth}">
          <table xsl:use-attribute-sets="cellspacing" class="{$tabledefaultStyle}">
            <xsl:for-each select="name">
              <tr>
                <td width="{$firstColWidth}" class="{$storedprocedurefirstColStyle}">Name:</td>
                <td width="{$secondColWidth}" class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="domainDescription">
              <tr>
                <td width="{$firstColWidth}" class="{$storedprocedurefirstColStyle}">Domain:</td>
                <td width="{$secondColWidth}" class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="required">
              <tr>
                <td width="{$firstColWidth}" class="{$storedprocedurefirstColStyle}">Required:</td>
                <td width="{$secondColWidth}" class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
            <xsl:for-each select="repeats">
              <tr>
                <td width="{$firstColWidth}" class="{$storedprocedurefirstColStyle}">Repeatable:</td>
                <td width="{$secondColWidth}" class="{$secondColStyle}"><xsl:value-of select="."/></td>
              </tr>
            </xsl:for-each>
          </table>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="$withAttributes='1'">
      <xsl:for-each select="attributeList">
        <xsl:call-template name="storedProcedureAttributeList">
          <xsl:with-param name="storedprocedurefirstColStyle" select="$storedprocedurefirstColStyle"/>
          <xsl:with-param name="storedproceduresubHeaderStyle" select="$storedproceduresubHeaderStyle"/>
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <!-- Here to display distribution info-->
    <xsl:for-each select="physical">
      <xsl:call-template name="storedProcedureShowDistribution">
        <xsl:with-param name="docid" select="$docid"/>
        <xsl:with-param name="entityindex" select="$entityindex"/>
        <xsl:with-param name="physicalindex" select="position()"/>
        <xsl:with-param name="storedprocedurefirstColStyle" select="$storedprocedurefirstColStyle"/>
        <xsl:with-param name="storedproceduresubHeaderStyle" select="$storedproceduresubHeaderStyle"/>
      </xsl:call-template>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="storedProcedureShowDistribution">
    <xsl:param name="storedprocedurefirstColStyle"/>
    <xsl:param name="storedproceduresubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entitylevel</xsl:param>
    <xsl:param name="entitytype">storedProcedure</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: storedProcedureShowDistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <tr>
        <td colspan="2">
          <xsl:call-template name="distribution">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="physicalindex" select="$physicalindex"/>
            <xsl:with-param name="distributionindex" select="position()"/>
            <xsl:with-param name="disfirstColStyle" select="$storedprocedurefirstColStyle"/>
            <xsl:with-param name="dissubHeaderStyle" select="$storedproceduresubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="storedProcedureAttributeList">
    <xsl:param name="storedprocedurefirstColStyle"/>
    <xsl:param name="storedproceduresubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype">storedProcedure</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: storedProcedureAttributeList</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$storedproceduresubHeaderStyle}" colspan="2"><xsl:text>Attribute(s) Info:</xsl:text></td>
    </tr>
    <tr>
      <td colspan="2">
         <xsl:call-template name="attributelist">
           <xsl:with-param name="docid" select="$docid"/>
           <xsl:with-param name="entitytype" select="$entitytype"/>
           <xsl:with-param name="entityindex" select="$entityindex"/>
         </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

  <!-- eml-text-2.0.0.xsl -->
  <xsl:preserve-space elements="literalLayout"/>
  
  <!-- This module is for text module in eml2 document. It is a table and self contained-->
  <xsl:template name="text">
    <xsl:param name="textfirstColStyle" />
    <xsl:param name="textsecondColStyle" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: text</xsl:text></xsl:message></xsl:if>
    <xsl:if test="(section and normalize-space(section[1]) != '') or (para and normalize-space(para[1]) != '') or (. != '')">
      <xsl:apply-templates mode="text">
        <xsl:with-param name="textfirstColStyle" select="$textfirstColStyle"/>
        <xsl:with-param name="textsecondColStyle" select="$textsecondColStyle" />
      </xsl:apply-templates>
    </xsl:if>
  </xsl:template>
  
  <xsl:template name="abstracttext">
    <xsl:param name="textfirstColStyle" />
    <xsl:param name="textsecondColStyle" />
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: abstracttext</xsl:text></xsl:message></xsl:if>
    <xsl:if test="(section and normalize-space(section[1]) != '') or (para and normalize-space(para[1]) != '') or (. != '')">
      <!-- was <xsl:apply-templates mode="text"> (mgb 7Jun2011) use mode="lowlevel" to make abstract use p for para -->
      <div>
        <xsl:apply-templates mode="text">
          <xsl:with-param name="textfirstColStyle" select="$textfirstColStyle"/>
          <xsl:with-param name="textsecondColStyle" select="$textsecondColStyle" />
        </xsl:apply-templates>
      </div>  
    </xsl:if>
  </xsl:template>

  <!-- Template for section-->
  <xsl:template match="section" mode="text">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: section</xsl:text></xsl:message></xsl:if>
    <xsl:if test="normalize-space(.)!=''">
      <xsl:if test="title and normalize-space(title[1])!=''">
	      <h4><xsl:value-of select="title"/></h4>
      </xsl:if>
      <xsl:if test="para and normalize-space(para[1])!=''">
        <xsl:apply-templates select="para" mode="text"/>
      </xsl:if>
      <xsl:if test="section and normalize-space(section[1])!=''">
        <xsl:apply-templates select="section" mode="text"/>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <!-- Section template for low level. Create a nested table and second column -->
  <xsl:template match="section" mode="lowlevel">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: section; mode: lowlevel</xsl:text></xsl:message></xsl:if>
    <div>
      <xsl:if test="title and normalize-space(title[1]) != ''">
        <h5><xsl:value-of select="title"/></h5>
      </xsl:if>
      <xsl:if test="para and normalize-space(para[1]) != ''">
        <xsl:apply-templates select="para" mode="lowlevelvel"/>
      </xsl:if>
      <xsl:if test="section and normalize-space(section[1]) != ''">
        <xsl:apply-templates select="section" mode="lowlevel"/>
      </xsl:if>
    </div>
  </xsl:template>

  <!-- para template for text mode-->
  <xsl:template match="para" mode="text">
    <xsl:param name="textfirstColStyle"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: para</xsl:text></xsl:message></xsl:if>
    <p class="eml"><xsl:apply-templates/></p>
    <!-- <xsl:apply-templates mode="lowlevel"/> -->
  </xsl:template>

  <!-- para template without any other structure. It does actually transfer.
       Currently, only get the text and it need more revision-->
  <xsl:template match="para" mode="lowlevel">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: para; mode: lowlevel</xsl:text></xsl:message></xsl:if>
    <p class="eml"><xsl:value-of select="."/></p>
  </xsl:template>
  
  <xsl:template match="itemizedlist">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: itemizedlist</xsl:text></xsl:message></xsl:if>
    <ul>
      <xsl:for-each select="listitem">
        <li><xsl:apply-templates select="."/></li>
      </xsl:for-each>
    </ul>
  </xsl:template>
  
  <xsl:template match="orderedlist">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: orderedlist</xsl:text></xsl:message></xsl:if>
    <ol>
      <xsl:for-each select="listitem">
        <li><xsl:value-of select="."/></li>
      </xsl:for-each>
    </ol>
  </xsl:template>
  
  <xsl:template match="emphasis">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: emphasis</xsl:text></xsl:message></xsl:if>
    <em><xsl:value-of select="."/></em>
  </xsl:template>
  
  <xsl:template match="superscript">
    <sup><xsl:value-of select="."/></sup>
  </xsl:template>
  
  <xsl:template match="subscript">
    <sub><xsl:value-of select="."/></sub>
  </xsl:template>
  
  <!-- note: EML is using docbook 4, citetitle is optional, unbounded. -->
  <xsl:template match="ulink">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: ulink</xsl:text></xsl:message></xsl:if>
    <xsl:element name="a">
      <xsl:attribute name="href">
        <xsl:value-of select="@url"/>
      </xsl:attribute>
      <xsl:choose>
        <xsl:when test="citetitle">
          <xsl:value-of select="."/>
        </xsl:when>
        <xsl:otherwise>
          <xsl:value-of select="."/>
        </xsl:otherwise>
      </xsl:choose>    
    </xsl:element>
  </xsl:template>
  
  <xsl:template match="literalLayout">
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: literalLayout</xsl:text></xsl:message></xsl:if>
    <pre><xsl:value-of  select="." xml:space="preserve"/></pre>
  </xsl:template>
  
  <!-- eml-view-2.0.0.xsl -->
  <xsl:template name="view">
    <xsl:param name="viewfirstColStyle"/>
    <xsl:param name="viewsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:param name="entitytype"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: view</xsl:text></xsl:message></xsl:if>
    <hr></hr>
    <h2>View</h2>
    <table class="{$tabledefaultStyle}">
      <xsl:choose>
        <xsl:when test="references!=''">
          <xsl:variable name="ref_id" select="references"/>
          <xsl:variable name="references" select="$ids[@id=$ref_id]" />
          <xsl:for-each select="$references">
            <xsl:call-template name="viewCommon">
             <xsl:with-param name="viewfirstColStyle" select="$viewfirstColStyle"/>
             <xsl:with-param name="viewsubHeaderStyle" select="$viewsubHeaderStyle"/>
             <xsl:with-param name="docid" select="$docid"/>
             <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
           <xsl:call-template name="viewCommon">
             <xsl:with-param name="viewfirstColStyle" select="$viewfirstColStyle"/>
             <xsl:with-param name="viewsubHeaderStyle" select="$viewsubHeaderStyle"/>
             <xsl:with-param name="docid" select="$docid"/>
             <xsl:with-param name="entityindex" select="$entityindex"/>
            </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </table>
  </xsl:template>

  <xsl:template name="viewCommon">
    <xsl:param name="viewfirstColStyle"/>
    <xsl:param name="viewsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: viewCommon</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="entityName">
      <xsl:call-template name="entityName">
        <xsl:with-param name="entityfirstColStyle" select="$viewfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="queryStatement">
      <tr>
        <td class="{$viewfirstColStyle}">Query Statement:</td>
        <td class="{$secondColStyle}"><xsl:value-of select="."/></td>
      </tr>
    </xsl:for-each>
    <xsl:for-each select="alternateIdentifier">
      <xsl:call-template name="entityalternateIdentifier">
        <xsl:with-param name="entityfirstColStyle" select="$viewfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="entityDescription">
      <xsl:call-template name="entityDescription">
        <xsl:with-param name="entityfirstColStyle" select="$viewfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:for-each select="additionalInfo">
      <xsl:call-template name="entityadditionalInfo">
        <xsl:with-param name="entityfirstColStyle" select="$viewfirstColStyle"/>
      </xsl:call-template>
    </xsl:for-each>
    <!-- call physical moduel without show distribution(we want see it later)-->
    <xsl:if test="physical">
      <tr>
        <td class="{$viewsubHeaderStyle}" colspan="2">Physical Structure Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="physical">
      <tr>
        <td colspan="2">
          <xsl:call-template name="physical">
            <xsl:with-param name="physicalfirstColStyle" select="$viewfirstColStyle"/>
            <xsl:with-param name="notshowdistribution">yes</xsl:with-param>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="coverage">
      <tr>
        <td class="{$viewsubHeaderStyle}" colspan="2">Coverage Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="coverage">
      <tr>
        <td colspan="2"><xsl:call-template name="coverage"></xsl:call-template></td>
      </tr>
    </xsl:for-each>
    <xsl:if test="method | methods">
      <tr>
        <td class="{$viewsubHeaderStyle}" colspan="2">Method Description:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="method | methods">
      <tr>
        <td colspan="2">
          <xsl:call-template name="method">
            <xsl:with-param name="methodfirstColStyle" select="$viewfirstColStyle"/>
            <xsl:with-param name="methodsubHeaderStyle" select="$viewsubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="constraint">
      <tr>
        <td class="{$viewsubHeaderStyle}" colspan="2">Constraint:</td>
      </tr>
    </xsl:if>
    <xsl:for-each select="constraint">
      <tr>
        <td colspan="2">
          <xsl:call-template name="constraint">
            <xsl:with-param name="constraintfirstColStyle" select="$viewfirstColStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
    <xsl:if test="$withAttributes='1'">
      <xsl:for-each select="attributeList">
        <xsl:call-template name="viewAttributeList">
          <xsl:with-param name="viewfirstColStyle" select="$viewfirstColStyle"/>
          <xsl:with-param name="viewsubHeaderStyle" select="$viewsubHeaderStyle"/>
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
    <xsl:if test="$withAttributes='1'">
      <!-- Here to display distribution info-->
      <xsl:for-each select="physical">
        <xsl:call-template name="viewShowDistribution">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
          <xsl:with-param name="physicalindex" select="position()"/>
          <xsl:with-param name="viewfirstColStyle" select="$viewfirstColStyle"/>
          <xsl:with-param name="viewsubHeaderStyle" select="$viewsubHeaderStyle"/>
        </xsl:call-template>
      </xsl:for-each>
    </xsl:if>
  </xsl:template>

  <xsl:template name="viewShowDistribution">
    <xsl:param name="viewfirstColStyle"/>
    <xsl:param name="viewsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="level">entitylevel</xsl:param>
    <xsl:param name="entitytype">view</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:param name="physicalindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: viewShowDistribution</xsl:text></xsl:message></xsl:if>
    <xsl:for-each select="distribution">
      <tr>
        <td colspan="2">
          <xsl:call-template name="distribution">
            <xsl:with-param name="docid" select="$docid"/>
            <xsl:with-param name="level" select="$level"/>
            <xsl:with-param name="entitytype" select="$entitytype"/>
            <xsl:with-param name="entityindex" select="$entityindex"/>
            <xsl:with-param name="physicalindex" select="$physicalindex"/>
            <xsl:with-param name="distributionindex" select="position()"/>
            <xsl:with-param name="disfirstColStyle" select="$viewfirstColStyle"/>
            <xsl:with-param name="dissubHeaderStyle" select="$viewsubHeaderStyle"/>
          </xsl:call-template>
        </td>
      </tr>
    </xsl:for-each>
  </xsl:template>

  <xsl:template name="viewAttributeList">
    <xsl:param name="viewfirstColStyle"/>
    <xsl:param name="viewsubHeaderStyle"/>
    <xsl:param name="docid"/>
    <xsl:param name="entitytype">view</xsl:param>
    <xsl:param name="entityindex"/>
    <xsl:if test="boolean(number($debugmessages))"><xsl:message><xsl:text>TEMPLATE: viewAttributeList</xsl:text></xsl:message></xsl:if>
    <tr>
      <td class="{$viewsubHeaderStyle}" colspan="2"><xsl:text>Attribute(s) Info:</xsl:text></td>
    </tr>
    <tr>
      <td colspan="2">
        <xsl:call-template name="attributelist">
          <xsl:with-param name="docid" select="$docid"/>
          <xsl:with-param name="entitytype" select="$entitytype"/>
          <xsl:with-param name="entityindex" select="$entityindex"/>
        </xsl:call-template>
      </td>
    </tr>
  </xsl:template>

</xsl:stylesheet>