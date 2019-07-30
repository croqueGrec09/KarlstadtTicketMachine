<?xml version="1.0" encoding="UTF-8"?>
<!-- 
    This is the XSLT stylesheet for markup output of the TEI encoded letters. The 
    stylesheet is executed upon runtime; the DOMDocument will be processed by the 
    templates to generate HTML output. Depending upon the mode, Bootstrap-conform or 
    "plain" HTML tables will be generated.
    
    For the notes, there is a separate stylesheet.
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
    <xsl:key name="reference" match="//TEI:person|//TEI:place|//TEI:bibl|//TEI:biblStruct|//TEI:term" use="concat('#',@xml:id)"/>
    <xsl:template match="/">
        <xsl:choose>
            <xsl:when test="$mode = 'KtM'">
                <div class="col-md-12 well" id="content">
                    <xsl:apply-templates select="//TEI:opener"/>
                    <xsl:for-each select="//TEI:div">
                        <div class="row section">
                            <div class="col-md-10">
                                <xsl:attribute name="data-n">
                                    <xsl:value-of select="@n"/>
                                </xsl:attribute>
                                <xsl:attribute name="data-cat">
                                    <xsl:value-of select="@corresp"/>
                                </xsl:attribute>
                                <xsl:if test="@resp">
                                    <em>
                                        <xsl:text>[Von der Hand von </xsl:text>
                                        <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:forename"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:surname"/>
                                        <xsl:text>]</xsl:text>
                                    </em>
                                </xsl:if>
                                <xsl:apply-templates select="TEI:p"/>
                                <xsl:apply-templates select="TEI:lg"/>
                            </div>
                            <div class="col-md-2">
                                <xsl:text>Kategorie: </xsl:text>
                                <xsl:value-of select="key('reference',@corresp)"/>
                            </div>
                        </div>
                    </xsl:for-each>
                    <section>
                        <xsl:apply-templates select="//TEI:closer"/>
                    </section>
                    <xsl:for-each select="//TEI:postscript">
                        <section>
                            <xsl:attribute name="data-n">
                                <xsl:value-of select="count(//TEI:div[@corresp])+position()"/>
                            </xsl:attribute>
                            <xsl:attribute name="data-cat">
                                <xsl:value-of select="@corresp"/>
                            </xsl:attribute>
                            <xsl:apply-templates select="TEI:p"/>
                            <xsl:apply-templates select="TEI:lg"/>
                        </section>
                    </xsl:for-each>
                    <xsl:if test="//TEI:back">
                        <section>
                            <xsl:apply-templates select="//TEI:back"/>
                        </section>
                    </xsl:if>
                </div>
            </xsl:when>
            <xsl:when test="$mode = 'PoD'">
                
                    <tr>
                        <th colspan="6">
                            <xsl:apply-templates select="//TEI:opener" />
                        </th>
                    </tr>
                    <xsl:for-each select="//TEI:div">
                        <tr class="section">
                            <td colspan="6">
                                <xsl:attribute name="data-n">
                                    <xsl:value-of select="@n"/>
                                </xsl:attribute>
                                <xsl:attribute name="data-cat">
                                    <xsl:value-of select="@corresp"/>
                                </xsl:attribute>
                                <xsl:if test="@resp">
                                    <em>
                                        <xsl:text>[Von der Hand von </xsl:text>
                                        <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:forename"/>
                                        <xsl:text> </xsl:text>
                                        <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:surname"/>
                                        <xsl:text>]</xsl:text>
                                    </em>
                                </xsl:if>
                                <xsl:apply-templates select="TEI:p"/>
                                <xsl:apply-templates select="TEI:lg"/>
                            </td>
                            <td class="navLink loadCategory">
                                <!-- catref -->
                                <xsl:attribute name="data-catRef">
                                    <xsl:value-of select="substring(@corresp,2)"/>
                                </xsl:attribute>
                                <xsl:text>Kategorie: </xsl:text>
                                <xsl:value-of select="key('reference',@corresp)"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <tr>
                        <td colspan="6">
                            <xsl:apply-templates select="//TEI:closer"/>
                        </td>
                    </tr>
                    <xsl:for-each select="//TEI:postscript">
                        <tr>
                            <td colspan="6">
                                <xsl:attribute name="data-n">
                                    <xsl:value-of select="count(//TEI:div[@corresp])+position()"/>
                                </xsl:attribute>
                                <xsl:attribute name="data-cat">
                                    <xsl:value-of select="@corresp"/>
                                </xsl:attribute>
                                <xsl:apply-templates select="TEI:p"/>
                                <xsl:apply-templates select="TEI:lg"/>
                            </td>
                        </tr>
                    </xsl:for-each>
                    <tr>
                        <td colspan="6">
                            <xsl:apply-templates select="//TEI:back"/>
                        </td>
                    </tr>
                
            </xsl:when>
        </xsl:choose>
    </xsl:template>
    <xsl:template match="TEI:back">
        <section>
            <xsl:if test="@resp">
                <em>
                    <xsl:text>[Von der Hand von </xsl:text>
                    <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:forename"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:surname"/>
                    <xsl:text>]</xsl:text>
                </em>
            </xsl:if>
            <xsl:apply-templates select="TEI:p"/>
        </section>
    </xsl:template>
    <xsl:template match="TEI:opener">
        <xsl:if test="@resp">
            <em>
                <xsl:text>[Von der Hand von </xsl:text>
                <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:forename"/>
                <xsl:text> </xsl:text>
                <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:surname"/>
                <xsl:text>]</xsl:text>
            </em>
        </xsl:if>
        <h4>
            <xsl:apply-templates/>
        </h4>
    </xsl:template>
    <xsl:template match="TEI:p">
        <xsl:apply-templates/>
    </xsl:template>
    <!-- text part templates -->
    <xsl:template match="TEI:add[@place='above']">
        <span class="inserted">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>
    <xsl:template match="TEI:app">
        <a href="#" data-toggle="tooltip">
            <xsl:choose>
                <xsl:when test="TEI:rdg[position()>1]">
                    <xsl:attribute name="title">
                        <xsl:for-each select="TEI:rdg[position()>1]">
                            <xsl:value-of select="@wit"/>
                            <xsl:text>: </xsl:text>
                            <xsl:value-of select="."/>
                        </xsl:for-each>
                    </xsl:attribute>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:attribute name="title">
                        <xsl:text>nur in </xsl:text>
                        <xsl:value-of select="TEI:rdg[1]/@wit"/>
                        <xsl:text> vorhanden</xsl:text>
                    </xsl:attribute>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:value-of select="TEI:rdg[1]"/>
        </a>
    </xsl:template>
    <xsl:template match="TEI:emph[@rend='underlined']">
        <span class="underlined">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>
    <xsl:template match="TEI:choice">
        <xsl:value-of select="TEI:orig|TEI:sic"/><!-- is hardcoded UFN -->
    </xsl:template>
    <xsl:template match="TEI:cit">
        <span class="citation">
            <xsl:apply-templates />
        </span>
    </xsl:template>
    <xsl:template match="TEI:date">
        <xsl:apply-templates/>
    </xsl:template>
    <xsl:template match="TEI:dateline">
        <p>
            <xsl:attribute name="class">
                <xsl:value-of select="@rend"/>
            </xsl:attribute>
            <xsl:if test="@resp">
                <em>
                    <xsl:text>[Von der Hand von </xsl:text>
                    <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:forename"/>
                    <xsl:text> </xsl:text>
                    <xsl:value-of select="key('reference',@resp)/TEI:persName/TEI:surname"/>
                    <xsl:text>]</xsl:text>
                </em>
            </xsl:if>
            <xsl:apply-templates/>
        </p>
    </xsl:template>
    <xsl:template match="TEI:del">
        <span class="deleted">
            <xsl:value-of select="."/>
        </span>
    </xsl:template>
    <xsl:template match="TEI:gap/TEI:desc|TEI:unclear">
        <em>
            <xsl:text>[ - - </xsl:text>
            <xsl:value-of select="."/>
            <xsl:text> - - ]</xsl:text>
        </em>
    </xsl:template>
    <xsl:template match="TEI:lg">
        <xsl:choose>
            <xsl:when test="$mode = 'KtM'">
                <xsl:choose>
                    <xsl:when test="@type">
                        <div class="row">
                            <div class="col-md-4 signatureBox">
                                <xsl:for-each select="TEI:l[@rend='left']">
                                    <xsl:apply-templates/><br/>
                                </xsl:for-each>
                            </div>
                            <div class="col-md-4 empty">
                                    
                            </div>
                            <div class="col-md-4 signatureBox">
                                <xsl:for-each select="TEI:l[@rend='right']">
                                    <xsl:apply-templates/><br/>
                                </xsl:for-each>
                            </div>
                        </div>
                    </xsl:when>
                    <xsl:otherwise>
                        <div class="row">
                            <div class="col-md-3">
                                
                            </div>
                            <div class="col-md-6">
                                <xsl:for-each select="TEI:l">
                                    <xsl:apply-templates/>
                                    <br/>
                                </xsl:for-each>
                            </div>
                            <div class="col-md-3">
                                
                            </div>
                        </div>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:when test="$mode = 'PoD'">
                <xsl:choose>
                    <xsl:when test="@type">
                        <tr>
                            <td class="signatureBox">
                                <xsl:for-each select="TEI:l[@rend='left']">
                                    <xsl:apply-templates/><br/>
                                </xsl:for-each>
                            </td>
                            <td class="empty" colspan="6">
                                
                            </td>
                            <td class="signatureBox">
                                <xsl:for-each select="TEI:l[@rend='right']">
                                    <xsl:apply-templates/><br/>
                                </xsl:for-each>
                            </td>
                        </tr>
                    </xsl:when>
                    <xsl:otherwise>
                        <tr>
                            <td>
                                
                            </td>
                            <td colspan="4">
                                <xsl:for-each select="TEI:l">
                                    <xsl:apply-templates/>
                                    <br/>
                                </xsl:for-each>
                            </td>
                            <td>
                                
                            </td>
                        </tr>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
        </xsl:choose>
    </xsl:template>
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
    <xsl:template match="TEI:quote">
        <em>
            <xsl:value-of select="."/>
        </em>
        <a href="#" data-toggle="tooltip">
            <xsl:attribute name="title">
                <xsl:value-of select="key('reference',@corresp|../@corresp)//persName/surname"/>
                <xsl:text>, </xsl:text>
                <xsl:value-of select="key('reference',@corresp|../@corresp)//persName/forename"/>
                <xsl:if test="key('reference',@corresp|../@corresp)//title">
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="key('reference',@corresp|../@corresp)//title"/>
                    <xsl:text>)</xsl:text>
                </xsl:if>
            </xsl:attribute>
            [vgl.]
        </a>
    </xsl:template>
    <xsl:template match="TEI:ref">
        <a>
            <xsl:attribute name="href">
                <xsl:value-of select="@target"/>
            </xsl:attribute>
            <xsl:value-of select="."/>
            <sup>
                <xsl:value-of select="substring-after(@target,'-')"/>
            </sup>
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
    <xsl:template match="TEI:salute">
        <p>
            <xsl:attribute name="class">
                <xsl:value-of select="@rend"/>
            </xsl:attribute>
            <xsl:apply-templates/>
        </p>
    </xsl:template>
    <xsl:template match="TEI:signed">
        <em>
            <xsl:attribute name="class">
                <xsl:value-of select="@rend"/>
            </xsl:attribute>
            <xsl:value-of select="."/>
        </em>
    </xsl:template>
    <xsl:template match="TEI:title">
        <h4>
            <xsl:value-of select="."/>
        </h4>
    </xsl:template>
</xsl:stylesheet>