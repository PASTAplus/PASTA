<?xml version="1.0"?>
<!--

 $Date: 2017-11-07 11:13:25 -0600 (Tue, 07 Nov 2017) $
 $Author: Mike Rugge $
 $Author: Duane Costa $
 $Revision: 1 $
 
 Copyright 2011,2012 the University of New Mexico.
 Used with permission of Florida Coastal Everglades LTER
 
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
<xsl:stylesheet xmlns:eml="eml://ecoinformatics.org/eml-2.1.0" version="1.0"
xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:template match="eml:eml">
<xsl:text>Dataset title:  </xsl:text>
<xsl:value-of select="dataset/title"/>
<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>Dataset ID:  </xsl:text>
<xsl:if test="dataset/alternateIdentifier">
<xsl:value-of select="dataset/alternateIdentifier"/>
<xsl:text>&#x0d;&#x0a;</xsl:text>      
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/researchType">
<xsl:text>Research type:   </xsl:text> 
<xsl:value-of select="additionalMetadata/metadata/additionalDataset/researchType"/>
<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/creator">Dataset Creator<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/creator">
<xsl:call-template name="contact_info"/>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/metadataProvider">Metadata Provider<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/metadataProvider">
<xsl:call-template name="contact_info"/>
</xsl:for-each>
<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/abstract">Dataset Abstract<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/abstract/para">
<xsl:text>&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/datasetPurpose">Dataset Purpose
<xsl:for-each select="additionalMetadata/metadata/additionalDataset/datasetPurpose/para">
<xsl:value-of select="."/>
</xsl:for-each>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/keywordSet">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Dataset Keywords<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/keywordSet/keyword">
<xsl:text>&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
<xsl:if test="dataset/intellectualRights">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Intellectual Rights<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/intellectualRights/para">
<xsl:text>&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
<xsl:if test="dataset/distribution">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Distribution<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:if test="dataset/distribution/online/url">
<xsl:text>&#9;</xsl:text>Online distribution:<xsl:text>  </xsl:text> <xsl:value-of select="dataset/distribution/online/url"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="dataset/distribution/offline">
<xsl:text>&#9;</xsl:text>Offline distribution <xsl:if test="dataset/distribution/offline/mediumName">
<xsl:text>&#9;</xsl:text>Medium name:<xsl:text>  </xsl:text> <xsl:value-of  select="dataset/distribution/offline/mediumName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/distribution/offline/mediumDensity">
<xsl:text>&#9;</xsl:text>Medium density:><xsl:text>  </xsl:text><xsl:value-of select="dataset/distribution/offline/mediumDensity"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/distribution/offline/mediumDensityUnits">
<xsl:text>&#9;</xsl:text>Medium density units:<xsl:text>  </xsl:text><xsl:value-of select="dataset/distribution/offline/mediumDensityUnits"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/distribution/offline/mediumVolume">
<xsl:text>&#9;</xsl:text>Medium volume:<xsl:text>  </xsl:text><xsl:value-of select="dataset/distribution/offline/mediumVolume" /><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/distribution/offline/mediumFormat">
<xsl:text>&#9;</xsl:text>Medium Format:<xsl:text>  </xsl:text><xsl:value-of select="dataset/distribution/offline/mediumFormat" /><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:if>

<xsl:if test="additionalMetadata/metadata/additionalDataset/addDistribution">
<xsl:if test="additionalMetadata/metadata/additionalDataset/addDistribution/submissionDate"> 
<xsl:text>&#9;</xsl:text>Data Submission Date:<xsl:text>  </xsl:text> <xsl:value-of select="additionalMetadata/metadata/additionalDataset/addDistribution/submissionDate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/addDistribution/distributionDate">
<xsl:text>&#9;</xsl:text>Data Distribution Date:<xsl:text>  </xsl:text> <xsl:value-of select="additionalMetadata/metadata/additionalDataset/addDistribution/distributionDate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/addDistribution/distributionType"> 
<xsl:text>&#9;</xsl:text>Distribution Type:<xsl:text>  </xsl:text> <xsl:value-of select="additionalMetadata/metadata/additionalDataset/addDistribution/distributionType"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:if>

</xsl:if>
<xsl:if test="dataset/coverage/geographicCoverage">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Geographic Coverage<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/methods/sampling/studyExtent/description">
<xsl:text>&#9;</xsl:text>Study Extent Description<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/methods/sampling/studyExtent/description/para">
<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>

<xsl:if test="dataset/coverage/geographicCoverage">
<xsl:text>&#x0d;&#x0a;</xsl:text><xsl:text>&#9;</xsl:text>Bounding Coordinates<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/coverage/geographicCoverage">
<xsl:text>&#9;&#9;</xsl:text>Geographic description:<xsl:text>&#9;</xsl:text><xsl:value-of select="geographicDescription"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>West bounding coordinate:<xsl:text>&#9;</xsl:text><xsl:value-of select="boundingCoordinates/westBoundingCoordinate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>East bounding coordinate:<xsl:text>&#9;</xsl:text><xsl:value-of select="boundingCoordinates/eastBoundingCoordinate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>North bounding coordinate:<xsl:text>&#9;</xsl:text><xsl:value-of select="boundingCoordinates/northBoundingCoordinate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>South bounding coordinate:<xsl:text>&#9;</xsl:text><xsl:value-of select="boundingCoordinates/southBoundingCoordinate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/LTERsites">
<xsl:text>&#9;</xsl:text>FCE LTER Sites:<xsl:text> </xsl:text><xsl:text> </xsl:text><xsl:for-each select="additionalMetadata/metadata/additionalDataset/LTERsites/sitename">
<xsl:value-of select="."/>
</xsl:for-each>
</xsl:if>
<xsl:if test="dataset/methods/sampling/spatialSamplingUnits/coverage">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text><xsl:text>&#9;</xsl:text>All Sites<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/methods/sampling/spatialSamplingUnits/coverage">
<xsl:text>&#9;&#9;</xsl:text>Geographic Description:<xsl:value-of select="geographicDescription"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>Longitude:<xsl:value-of select="boundingCoordinates/eastBoundingCoordinate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>Latitude:<xsl:value-of select="boundingCoordinates/southBoundingCoordinate"/><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
<xsl:if test="dataset/coverage/temporalCoverage"><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Temporal Coverage<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:if test="dataset/coverage/temporalCoverage/rangeOfDates/beginDate">
<xsl:text>&#9;</xsl:text>Start Date:<xsl:text>  </xsl:text><xsl:value-of select="dataset/coverage/temporalCoverage/rangeOfDates/beginDate/calendarDate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/coverage/temporalCoverage/rangeOfDates/endDate">
<xsl:text>&#9;</xsl:text>End Date:<xsl:text>  </xsl:text><xsl:value-of select="dataset/coverage/temporalCoverage/rangeOfDates/endDate/calendarDate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:if>
<xsl:if test="dataset/maintenance/description/para">
<xsl:text>&#x0d;&#x0a;</xsl:text>Maintenance<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/maintenance/description">
<xsl:text>&#9;</xsl:text><xsl:value-of select="para"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<br/>
</xsl:if>
<xsl:if test="dataset/contact">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Dataset Contact<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/contact">
<xsl:if test="positionName='Information Manager'">
<xsl:call-template name="contact_info"/>
</xsl:if>
</xsl:for-each>
</xsl:if>

<xsl:if test="dataset/methods"><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Methods</xsl:if>
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:if test="dataset/methods/sampling/samplingDescription">
<xsl:text>&#9;</xsl:text>Sampling Description <xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/methods/sampling/samplingDescription/para">
<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>

<xsl:for-each select="dataset/methods/methodStep">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text>Method Step <xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>Description <xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="description/para">
<xsl:text>&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<xsl:if test="citation">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>Citation<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="citation">
<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;&#9;</xsl:text>
<xsl:for-each select="creator/individualName">
<xsl:choose>
<xsl:when test="position()= 1">
<xsl:value-of select="surName"/>
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text><![CDATA[ ]]></xsl:text>
</xsl:for-each>
</xsl:when>
<xsl:when test="position()!= 1 and position()!=last()">
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
<xsl:text> </xsl:text>
<xsl:value-of select="surName"/>
</xsl:when>
<xsl:when test="position()=last()">
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
<xsl:text> </xsl:text>
<xsl:value-of select="surName"/>
<xsl:text>. </xsl:text>
</xsl:when>
</xsl:choose>
</xsl:for-each>
<xsl:if test="pubDate">
<xsl:value-of select="pubDate"/>
<xsl:text>. </xsl:text>
</xsl:if>
<xsl:if test="title">
<xsl:value-of select="title"/>
<xsl:text>. </xsl:text>
</xsl:if>
<xsl:if test="manuscript">
<xsl:if test="manuscript/institution">
<xsl:value-of
select="manuscript/institution/organizationName"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="manuscript/totalPages">
<xsl:value-of select="manuscript/totalPages"/>
<xsl:text>. </xsl:text>
</xsl:if>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="article">
<xsl:if test="article/journal">
<xsl:value-of select="article/journal"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="article/volume">
<xsl:value-of select="article/volume"/>
</xsl:if>
<xsl:if test="article/issue">
<xsl:text>(</xsl:text>
<xsl:value-of select="article/issue"/>
<xsl:text>)</xsl:text>
</xsl:if>
<xsl:if test="article/pageRange">
<xsl:text>: </xsl:text>
<xsl:value-of select="article/pageRange"/>
<xsl:text>.</xsl:text>
</xsl:if>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="report">
<xsl:text>Report number </xsl:text>
<xsl:value-of select="report/reportNumber"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="report/publisher">
<xsl:value-of select="report/publisher/organizationName"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="report/publicationPlace">
<xsl:value-of select="report/publicationPlace"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="report/totalPages">
<xsl:value-of select="report/totalPages"/>
<xsl:text> pp.</xsl:text>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="chapter">
<xsl:if test="chapter/pageRange">
<xsl:text>p. </xsl:text>
<xsl:value-of select="chapter/pageRange"/>
</xsl:if>

<xsl:if test="chapter/editor">
<xsl:text> in </xsl:text>
<xsl:for-each select="chapter/editor/individualName">
<xsl:choose>
<xsl:when test="position()= 1">
<xsl:value-of select="surName"/>
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
</xsl:when>
<xsl:when
test="position()!= 1 and position()!=last()">
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
<xsl:text> </xsl:text>
<xsl:value-of select="surName"/>
</xsl:when>
<xsl:when test="position()=last()">
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
<xsl:text> </xsl:text>
<xsl:value-of select="surName"/>
<xsl:text>. </xsl:text>
</xsl:when>
</xsl:choose>
</xsl:for-each>
<xsl:text>, (eds). </xsl:text>
</xsl:if>
<xsl:if test="chapter/bookTitle">
<xsl:value-of select="chapter/bookTitle"/>
<xsl:text>. </xsl:text>
</xsl:if>
<xsl:if test="chapter/edition">
<xsl:value-of select="chapter/edition"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="chapter/publisher">
<xsl:value-of select="chapter/publisher/organizationName"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="chapter/publicationPlace">
<xsl:value-of select="chapter/publicationPlace"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="chapter/totalPages">
<xsl:value-of select="chapter/totalPages"/>
<xsl:text> pp. </xsl:text>
</xsl:if>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="book">
<xsl:if test="book/edition">
<xsl:value-of select="book/edition"/>
<xsl:text>, </xsl:text>
</xsl:if>

<xsl:if test="book/publisher">
<xsl:value-of select="book/publisher/organizationName"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="book/publicationPlace">
<xsl:value-of select="book/publicationPlace"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="book/totalPages">
<xsl:value-of select="book/totalPages"/>
<xsl:text> pp. </xsl:text>
</xsl:if>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="thesis">
<xsl:if test="thesis/degree">
<xsl:value-of select="thesis/degree"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="thesis/institution">
<xsl:value-of select="thesis/institution/organizationName"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="thesis/totalPages">
<xsl:value-of select="thesis/totalPages"/>
<xsl:text> pp. </xsl:text>
</xsl:if>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings">
<xsl:if test="conferenceProceedings/pageRange">
<xsl:text>p. </xsl:text>
<xsl:value-of select="conferenceProceedings/pageRange"/>
</xsl:if>
<xsl:if test="conferenceProceedings/editor">
<xsl:text> in </xsl:text>
<xsl:for-each
select="conferenceProceedings/editor/individualName">
<xsl:choose>
<xsl:when test="position()= 1">
<xsl:value-of select="surName"/>
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
</xsl:when>
<xsl:when
test="position()!= 1 and position()!=last()">
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
<xsl:text> </xsl:text>
<xsl:value-of select="surName"/>
</xsl:when>
<xsl:when test="position()=last()">
<xsl:text>, </xsl:text>
<xsl:for-each select="givenName">
<xsl:value-of select="."/>
<xsl:text> </xsl:text>
</xsl:for-each>
<xsl:text> </xsl:text>
<xsl:value-of select="surName"/>
<xsl:text>. </xsl:text>
</xsl:when>
</xsl:choose>
</xsl:for-each>
<xsl:text> (eds). </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/bookTitle">
<xsl:value-of select="conferenceProceedings/bookTitle"/>
<xsl:text>. </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/edition">
<xsl:value-of select="conferenceProceedings/edition"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/publisher">
<xsl:value-of
select="conferenceProceedings/publisher/organizationName"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/publicationPlace">
<xsl:value-of
select="conferenceProceedings/publicationPlace"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/totalPages">
<xsl:value-of select="conferenceProceedings/totalPages"/>
<xsl:text> pp. </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/conferenceName">
<xsl:value-of
select="conferenceProceedings/conferenceName"/>
<xsl:text>,  </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/conferenceLocation">
<xsl:value-of
select="conferenceProceedings/conferenceLocation/city"/>
<xsl:text>, </xsl:text>
<xsl:value-of
select="conferenceProceedings/conferenceLocation/administrativeArea"/>
<xsl:text>, </xsl:text>
<xsl:value-of
select="conferenceProceedings/conferenceLocation/country"/>
<xsl:text>, </xsl:text>
</xsl:if>
<xsl:if test="conferenceProceedings/conferenceDate">
<xsl:value-of
select="conferenceProceedings/conferenceDate"/>
<xsl:text>.</xsl:text>
</xsl:if>
</xsl:if>
<xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>

<xsl:if test="protocol">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>Protocol <xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:for-each select="protocol">
<xsl:if test="title">
<xsl:text>&#9;&#9;&#9;</xsl:text>Protocol Title:<xsl:text>  </xsl:text><xsl:value-of select="title"/><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:text>&#9;&#9;&#9;</xsl:text>Protocol Creator(s)<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="creator">
<xsl:text></xsl:text><xsl:call-template name="contact_info_protocol"/><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<xsl:if test="pubDate">
<xsl:text>&#9;&#9;&#9;</xsl:text>Publication Date:<xsl:text>  </xsl:text><xsl:value-of select="pubDate"/><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="abstract">
<xsl:text>&#9;&#9;&#9;</xsl:text>Abstract<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="abstract/para">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
<xsl:if test="keywordSet">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;&#9;</xsl:text>Keywords<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="keywordSet/keyword">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
<xsl:if test="proceduralStep/description">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;&#9;</xsl:text>Procedural Steps<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="proceduralStep/description/para">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
</xsl:for-each>
<xsl:if test="instrumentation">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text><xsl:text>&#9;&#9;</xsl:text>Instrumentation<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="instrumentation">
<xsl:text>&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
</xsl:for-each>

<xsl:if test="dataset/methods/qualityControl/description/para">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;</xsl:text>Quality Control <xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/methods/qualityControl/description/para">
<xsl:text>&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>

<xsl:if test="dataset/dataTable"><xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Data Table<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:if test="dataset/dataTable/entityName">
<xsl:text>&#9;</xsl:text>Entity Name:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/entityName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/dataTable/entityDescription">
<xsl:text>&#9;</xsl:text>Entity Description:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/entityDescription"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/dataTable/physical">
<xsl:if test="dataset/dataTable/physical/objectName">
<xsl:text>&#9;</xsl:text>Object Name:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/physical/objectName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:if>
<xsl:if test="dataset/dataTable/physical/dataFormat">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Data Format<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:if test="dataset/dataTable/physical/dataFormat/textFormat/numHeaderLines">
<xsl:text>&#9;</xsl:text>Number of Header Lines:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/physical/dataFormat/textFormat/numHeaderLines"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/dataTable/physical/dataFormat/textFormat/attributeOrientation">
<xsl:text>&#9;</xsl:text>Attribute Orientation:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/physical/dataFormat/textFormat/attributeOrientation"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/dataTable/physical/dataFormat/textFormat/simpleDelimited/fieldDelimiter">
<xsl:text>&#9;</xsl:text>Field Delimiter:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/physical/dataFormat/textFormat/simpleDelimited/fieldDelimiter"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="dataset/dataTable/numberOfRecords">
<xsl:text>&#9;</xsl:text>Number of Records:<xsl:text>  </xsl:text><xsl:value-of select="dataset/dataTable/numberOfRecords"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:if>
<xsl:if test="dataset/dataTable/attributeList">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Attributes<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="dataset/dataTable/attributeList/attribute">
<xsl:text>&#9;</xsl:text>Attribute Name:<xsl:text>  </xsl:text><xsl:value-of select="attributeName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text>Attribute Label:<xsl:text>  </xsl:text><xsl:value-of select="attributeLabel"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text>Attribute Definition:<xsl:text>  </xsl:text><xsl:value-of select="attributeDefinition"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text>Storage Type:<xsl:text>  </xsl:text><xsl:value-of select="storageType"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text>Measurement Scale: <xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:if test="measurementScale/nominal">
<xsl:if test="measurementScale/nominal/nonNumericDomain">
<xsl:if  test="measurementScale/nominal/nonNumericDomain/textDomain">
<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="measurementScale/nominal/nonNumericDomain/textDomain/definition"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="measurementScale/nominal/nonNumericDomain/enumeratedDomain">
<xsl:if test="measurementScale/nominal/nonNumericDomain/enumeratedDomain/codeDefinition">
<xsl:for-each select="measurementScale/nominal/nonNumericDomain/enumeratedDomain/codeDefinition">
<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="code"/>= <xsl:value-of select="definition"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:for-each>
</xsl:if>
</xsl:if>
</xsl:if>
</xsl:if>
<xsl:if test="measurementScale/ordinal">
<xsl:if test="measurementScale/ordinal/nonNumericDomain">
<xsl:if test="measurementScale/ordinal/nonNumericDomain/textDomain">
<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="measurementScale/ordinal/nonNumericDomain/textDomain/definition"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/ordinal/nonNumericDomain/enumeratedDomain">
<xsl:if test="measurementScale/ordinal/nonNumericDomain/enumeratedDomain/codeDefinition">
<xsl:for-each select="measurementScale/ordinal/nonNumericDomain/enumeratedDomain/codeDefinition">
<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="code"/>= <xsl:value-of select="definition"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:for-each>
</xsl:if>
</xsl:if>
</xsl:if>
</xsl:if>
<xsl:if test="measurementScale/ratio">
<xsl:if test="measurementScale/ratio/unit/customUnit"><xsl:text>&#9;&#9;</xsl:text>Units:<xsl:text>&#9;&#9;</xsl:text><xsl:value-of select="measurementScale/ratio/unit/customUnit"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/ratio/unit/standardUnit"><xsl:text>&#9;&#9;</xsl:text>Units:<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/ratio/unit/standardUnit"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/ratio/precision"><xsl:text>&#9;&#9;</xsl:text>Precision:<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/ratio/precision"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/ratio/numericDomain/numberType"><xsl:text>&#9;&#9;</xsl:text>Number Type:<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/ratio/numericDomain/numberType"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
</xsl:if>
<xsl:if test="measurementScale/interval">
<xsl:if test="measurementScale/interval/unit/customUnit"><xsl:text>&#9;&#9;</xsl:text>Units<xsl:text>  </xsl:text> <xsl:value-of  select="measurementScale/interval/unit/customUnit" /><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/interval/unit/standardUnit" ><xsl:text>&#9;&#9;</xsl:text>Units<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/interval/unit/standardUnit"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/interval/precision"><xsl:text>&#9;&#9;</xsl:text>Precision:<xsl:value-of select="measurementScale/interval/precision"/><br/></xsl:if>
<xsl:if test="measurementScale/interval/numericDomain/numberType"><xsl:text>&#9;&#9;</xsl:text>Number Type:<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/interval/numericDomain/numberType"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
</xsl:if>
<xsl:if test="measurementScale/datetime">
<xsl:if test="measurementScale/datetime/formatString"><xsl:text>&#9;&#9;</xsl:text>Format:<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/datetime/formatString"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/datetime/dateTimePrecision"><xsl:text>&#9;&#9;</xsl:text>Precision:<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/datetime/dateTimePrecision"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
<xsl:if test="measurementScale/datetime/dateTimeDomain/bounds/minimum"><xsl:text>&#9;&#9;</xsl:text>Minimum Value<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/datetime/dateTimeDomain/bounds/minimum"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="measurementScale/datetime/dateTimeDomain/bounds/maximum"><xsl:text>&#9;&#9;</xsl:text>Maximum Value<xsl:text>  </xsl:text><xsl:value-of select="measurementScale/datetime/dateTimeDomain/bounds/maximum"/><xsl:text>&#x0d;&#x0a;</xsl:text></xsl:if>
</xsl:if>

<xsl:text>&#9;</xsl:text>Missing Value Code: <xsl:value-of select="missingValueCode/code"/><xsl:if test="missingValueCode/codeExplanation"><xsl:text>  </xsl:text>(<xsl:value-of select="missingValueCode/codeExplanation"/>)</xsl:if>
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
</xsl:if>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/addDistribution/submissionDate">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Dataset Submission Date <xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text><xsl:value-of select="additionalMetadata/metadata/additionalDataset/addDistribution/submissionDate"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/datasetSubmissionNotes/notes">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Data Submission Notes<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text><xsl:value-of select="additionalMetadata/metadata/additionalDataset/datasetSubmissionNotes/notes" /><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/datasetAnomolies/anomolies">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Dataset Anomalies<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text><xsl:value-of select="additionalMetadata/metadata/additionalDataset/datasetAnomolies/anomolies"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="additionalMetadata/metadata/additionalDataset/datasetInfoManagementNotes/notes">
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>Information Management Notes<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text><xsl:value-of select="additionalMetadata/metadata/additionalDataset/datasetInfoManagementNotes/notes"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:template>


<xsl:template name="contact_info">
<xsl:if test="individualName">
<xsl:text>&#9;</xsl:text>Name:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="individualName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="positionName">
<xsl:text>&#9;</xsl:text>Position:<xsl:text>&#9;</xsl:text><xsl:apply-templates select="positionName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="organizationName">
<xsl:text>&#9;</xsl:text>Organization:<xsl:text>&#9;</xsl:text><xsl:apply-templates select="organizationName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="address">
<xsl:text>&#9;</xsl:text><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text>Address:<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:apply-templates select="address"/><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;</xsl:text><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="phone[@phonetype='voice']">
<xsl:text>&#9;</xsl:text>Phone:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="phone[@phonetype='voice']"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="phone[@phonetype='fax']">
<xsl:text>&#9;</xsl:text>Fax:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="phone[@phonetype='fax']"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="electronicMailAddress">
<xsl:text>&#9;</xsl:text>Email:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="electronicMailAddress"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="onlineUrl">
<xsl:text>&#9;</xsl:text>URL:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="onlineUrl"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template name="contact_info_protocol">
<xsl:if test="individualName">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Name:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="individualName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="positionName">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Position:<xsl:text>&#9;</xsl:text><xsl:apply-templates select="positionName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="organizationName">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Organization:<xsl:text>&#9;</xsl:text><xsl:apply-templates select="organizationName"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="address">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text><xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Address:<xsl:text>&#x0d;&#x0a;</xsl:text>
<xsl:for-each select="address/deliveryPoint">
<xsl:text>&#9;&#9;&#9;&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<xsl:text>&#9;&#9;&#9;&#9;&#9;&#9;</xsl:text><xsl:value-of select="address/city"/>,<xsl:text> </xsl:text><xsl:value-of select="address/administrativeArea"/><xsl:text> </xsl:text><xsl:value-of select="address/postalCode"/><xsl:text>  </xsl:text><xsl:value-of select="address/country"/>
<xsl:text>&#x0d;&#x0a;&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="phone[@phonetype='voice']">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Phone:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="phone[@phonetype='voice']"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="phone[@phonetype='fax']">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Fax:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="phone[@phonetype='fax']"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="electronicMailAddress">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>Email:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="electronicMailAddress"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
<xsl:if test="onlineUrl">
<xsl:text>&#9;&#9;&#9;&#9;</xsl:text>URL:<xsl:text>&#9;</xsl:text><xsl:text>&#9;</xsl:text><xsl:apply-templates select="onlineUrl"/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:if>
</xsl:template>

<xsl:template match="phone[@phonetype='voice']">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="phone[@phonetype='fax']">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="address">
<xsl:for-each select="deliveryPoint">
<xsl:text>&#9;&#9;&#9;</xsl:text><xsl:value-of select="."/><xsl:text>&#x0d;&#x0a;</xsl:text>
</xsl:for-each>
<xsl:text>&#9;&#9;&#9;</xsl:text><xsl:value-of select="city"/>,<xsl:text> </xsl:text><xsl:value-of select="administrativeArea"/><xsl:text> </xsl:text><xsl:value-of select="postalCode"/><xsl:text>  </xsl:text><xsl:value-of select="country"/>
</xsl:template>

<xsl:template match="individualName">
<xsl:if test="salutation"><xsl:value-of select="salutation"/><xsl:text> </xsl:text></xsl:if>
<xsl:value-of select="givenName"/><xsl:text> </xsl:text><xsl:value-of select="surName"/> </xsl:template>

<xsl:template match="organizationName">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="positionName">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="electronicMailAddress">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="onlineUrl">
<xsl:value-of select="."/>
</xsl:template>

<xsl:template match="role">
<xsl:value-of select="."/>
</xsl:template>
</xsl:stylesheet>
