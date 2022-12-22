<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <!--Don't write XML header-->
  <xsl:output method="text" encoding="UTF-8"/>

  <!--CSV dialect-->
  <xsl:param name="delim" select="','"/>
  <xsl:param name="quote" select="'&quot;'"/>
  <xsl:param name="newline" select="'&#xa;'"/>

  <xsl:template match="/">
    <xsl:apply-templates select="/resultset"/>
  </xsl:template>

  <!--Generate header-->
  <xsl:template match="/resultset">
    <!--The elements occur in the same order in each /resultset/document, so we pull
    the header names from the names of the first set of elements. -->
    <xsl:for-each select="document[1]/*">
      <!--<xsl:value-of select="name()"/>-->
      <xsl:call-template name="escapeCsv">
        <xsl:with-param name="value" select="name()"/>
      </xsl:call-template>
      <xsl:if test="not(position()=last())">
        <xsl:value-of select="$delim"/>
      </xsl:if>
    </xsl:for-each>
    <xsl:apply-templates/>
    <!--Final newline-->
    <xsl:value-of select="$newline"/>
  </xsl:template>

  <!--Newline for each document-->
  <xsl:template match="/resultset/document">
    <xsl:value-of select="$newline"/>
    <xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="/resultset/document/*">
    <!--<xsl:text>[</xsl:text>-->
    <!--<xsl:value-of select="name()"/>-->
    <!--<xsl:text>=</xsl:text>-->
    <!--<xsl:value-of select="."/>-->
    <xsl:choose>
      <xsl:when test="./*">
        <xsl:value-of select="$quote"/>
        <xsl:for-each select="./*">
          <xsl:call-template name="escapeQuotes">
            <xsl:with-param name="value" select="normalize-space(.)"/>
          </xsl:call-template>
          <xsl:if test="not(position()=last())">
            <xsl:value-of select="$newline"/>
          </xsl:if>
        </xsl:for-each>
        <xsl:value-of select="$quote"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="escapeCsv">
          <xsl:with-param name="value" select="."/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>

    <xsl:if test="following-sibling::*">
      <xsl:value-of select="$delim"/>
    </xsl:if>

    <xsl:apply-templates/>

  </xsl:template>

  <!--Don't write text nodes implicitly-->
  <xsl:template match="text()"/>

  <!--Escape a string value so that it can be safely inserted into CSV-->
  <xsl:template name="escapeCsv">
    <xsl:param name="value"/>
    <xsl:choose>
      <xsl:when test="contains($value,$delim)">
        <xsl:value-of select="$quote"/>
        <xsl:call-template name="escapeQuotes">
          <xsl:with-param name="value" select="$value"/>
        </xsl:call-template>
        <xsl:value-of select="$quote"/>
      </xsl:when>
      <xsl:when test="contains($value,$quote)">
        <xsl:value-of select="$quote"/>
        <xsl:call-template name="escapeQuotes">
          <xsl:with-param name="value" select="$value"/>
        </xsl:call-template>
        <xsl:value-of select="$quote"/>
      </xsl:when>
      <xsl:when test="contains($value,$newline)">
        <xsl:value-of select="$quote"/>
        <xsl:call-template name="escapeQuotes">
          <xsl:with-param name="value" select="$value"/>
        </xsl:call-template>
        <xsl:value-of select="$quote"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="escapeQuotes">
    <xsl:param name="value"/>
    <xsl:choose>
      <xsl:when test="contains($value,$quote)">
        <xsl:value-of select="substring-before($value,$quote)"/>
        <xsl:text>&quot;&quot;</xsl:text>
        <xsl:call-template name="escapeQuotes">
          <xsl:with-param name="value" select="substring-after($value,$quote)"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
