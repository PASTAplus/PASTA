<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output omit-xml-declaration="no" indent="yes"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*/text()[not(ancestor::markup or ancestor::literalLayout or ancestor::objectName or ancestor::attributeName)]">
        <xsl:value-of select="normalize-space(replace(., '&#160;', ' '))"/>
    </xsl:template>
</xsl:stylesheet>
