<xsl:stylesheet version="3.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                >
    <xsl:output omit-xml-declaration="no" indent="yes"/>

    <!-- Template to copy nodes and apply templates to attributes and child nodes -->
    <xsl:template match="node()">
        <xsl:copy>
            <!-- Apply templates to attributes first to normalize space, then to child nodes -->
            <xsl:apply-templates select="@*"/>
            <xsl:apply-templates select="node()"/>
        </xsl:copy>
    </xsl:template>

    <!-- Template for normalizing space in text nodes, with specific exclusions -->
    <xsl:template match="text()[not(ancestor::markdown or ancestor::literalLayout or ancestor::objectName or ancestor::attributeName or ancestor::para)]">
        <xsl:value-of select="normalize-space(replace(., '&#160;', ' '))"/>
    </xsl:template>

    <!-- Template to normalize space in attribute values -->
    <xsl:template match="@*">
        <!-- Create a new attribute with the same name but normalized value -->
        <xsl:attribute name="{name()}">
            <xsl:value-of select="normalize-space(.)"/>
        </xsl:attribute>
    </xsl:template>
</xsl:stylesheet>
