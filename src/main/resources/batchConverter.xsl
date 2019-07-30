<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:TEI="http://www.tei-c.org/ns/1.0"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs"
    version="2.0">
    <xsl:output method="xml"/>
    <xsl:template match="@full"/>
    <xsl:template match="@instant"/>
    <xsl:template match="@status"/>
    <xsl:template match="@default"/>
    <xsl:template match="@defective"/>
    <xsl:template match="@part|@org|@sample|@anchored|@mode"/>
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    <xsl:template match="//TEI:listBibl[not(@type='preprints')]/TEI:biblStruct">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
            <TEI:ptr>
                <xsl:attribute name="target">
                    <xsl:value-of select="@corresp"/>
                </xsl:attribute>
            </TEI:ptr>
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>