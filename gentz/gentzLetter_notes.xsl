<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    This is the XSLT stylesheet for markup output of the TEI encoded annotation apparatus. 
    Like the main content stylesheet, this one, too, is executed upon runtime; the 
    DOMDocument will be processed by the templates to generate HTML output. Depending upon 
    the mode, Bootstrap-conform or "plain" HTML tables will be generated.
    
    To see the templates for the main letter body, consider the main template.
-->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:TEI="http://www.tei-c.org/ns/1.0" version="1.0" 
    exclude-result-prefixes="TEI">
    <xsl:output method="html"/>
    <xsl:param name="mode" select="'KtM'"/>
    <!-- 
        PoD means Power of Data, means that the app calls are done from within the game
        other mode is KtM, which means Karlstadt ticket-machine, that is, if the app is used stand-alone for demonstration or debug 
        purposes
    -->
    <xsl:key name="reference" match="//TEI:person|//TEI:place|//TEI:bibl|//TEI:biblStruct|//TEI:titleStmt/TEI:editor" use="concat('#',@xml:id)"/>
    <xsl:template match="/">
        <ol>
            <xsl:for-each select="//TEI:note">
                <li>
                    <xsl:attribute name="id">
                        <xsl:value-of select="@xml:id"/>
                    </xsl:attribute>
                    <xsl:choose>
                        <xsl:when test="@resp">
                            <em>
                                <xsl:text>Anmerkung von </xsl:text>
                                <xsl:value-of select="key('reference',@resp)"/>
                                <xsl:text>: </xsl:text>
                            </em>
                        </xsl:when>
                        <xsl:otherwise>
                            <em>Anmerkung aus Gentz digital: </em>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:apply-templates/>
                </li>
            </xsl:for-each>
        </ol>
    </xsl:template>
    <!-- text part templates -->
    <xsl:template match="TEI:ptr">
        <a>
            <xsl:attribute name="class">
                <xsl:value-of select="$mode"/>
            </xsl:attribute>
            <xsl:attribute name="href">
                <xsl:choose>
                    <xsl:when test="key('reference',@target)">
                        <xsl:text>/</xsl:text>
                        <xsl:value-of select="$mode"/>
                        <xsl:text>/fetchWork/</xsl:text>
                        <xsl:value-of select="key('reference',@target)/@corresp"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="."/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </a>
    </xsl:template>
    <xsl:template match="TEI:rs">
        <a>
            <xsl:attribute name="class">
                <xsl:value-of select="$mode"/>
            </xsl:attribute>
            <xsl:attribute name="href">
                <xsl:text>/</xsl:text>
                <xsl:value-of select="$mode"/>
                <xsl:choose>
                    <xsl:when test="@type = 'citation'">
                        <xsl:choose>
                            <xsl:when test="contains(@ref,'c')">
                                <xsl:text>/letter/</xsl:text>
                                <xsl:value-of select="key('reference',@ref)/@corresp"/>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:text>/fetchWork/</xsl:text>
                                <xsl:value-of select="key('reference',@ref)/@corresp"/>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:when test="@type = 'person'">
                        <xsl:text>/fetchPerson/</xsl:text>
                        <xsl:value-of select="key('reference',@ref)/@xml:id"/>
                    </xsl:when>
                    <xsl:when test="@type = 'place'">
                        <xsl:text>/fetchPlace/</xsl:text>
                        <xsl:value-of select="key('reference',@ref)/@xml:id"/>
                    </xsl:when>
                </xsl:choose>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </a>
    </xsl:template>
</xsl:stylesheet>