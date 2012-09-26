<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
		xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="html" version="4.0"
	      encoding="UTF-8" indent="yes"
	      omit-xml-declaration="yes"/>



  <!-- Gets the value of a specified Java tag -->
  <xsl:template name="get-java-tag-value">
    <xsl:param name="tag"/>
    <xsl:variable name="tag-value">
      <xsl:value-of select="comment/attribute[@name=$tag]/description"/>
    </xsl:variable>      
    <xsl:choose>
      <xsl:when test="$tag-value = ''">
	<font color="red">[Insert
	<xsl:value-of select="$tag"/>
	into Javadoc]</font>
      </xsl:when>
      <xsl:otherwise>
	<xsl:copy-of select="$tag-value"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Gets the HTTP verb of a provided method element -->
  <xsl:template name="get-http-verb">
    <xsl:value-of select="annotation[@type='GET'  or 
			  @type='POST' or 
			  @type='PUT'  or 
			  @type='DELETE']/@type"/>
  </xsl:template>
  
  <!-- Gets the value of the @Path annotation of a provided method element -->
  <xsl:template name="get-relative-url">
    <xsl:variable name="path">
      <xsl:value-of select="annotation[@type='Path']/elementValuePair/@value" 
		    disable-output-escaping="yes"/>
    </xsl:variable>
    <xsl:value-of select="translate($path, '&quot;', '')"/>
  </xsl:template>

  <!-- Makes a "Verb : URL" pair from a provided method element -->
  <xsl:template name="make-verb-relative-url-pair">
    <xsl:variable name="verb">
      <xsl:call-template name="get-http-verb"/>
    </xsl:variable>
    <xsl:variable name="path">
      <xsl:call-template name="get-relative-url"/>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$path = ''">
	<xsl:value-of select="$verb"/>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="concat($verb, ' : ', $path)"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- Get the description of a provided method element -->
  <xsl:template name="get-description">
    <xsl:value-of select="comment/description" 
		  disable-output-escaping="yes"/>
  </xsl:template>  

  <!-- Get the brief description of a provided method element -->
  <xsl:template name="get-brief-description">
    <xsl:variable name="description">
      <xsl:call-template name="get-description"/>
    </xsl:variable>
    <xsl:value-of select="concat(substring-before($description, '.'), '.')" 
		  disable-output-escaping="yes"/>
  </xsl:template>  




  <!-- Making the web service API document -->  
  <xsl:template match="/java-to-xml/jelclass">

    <html>

      <!-- Saving web service name -->
      <xsl:variable name="web-service-name">
	<xsl:call-template name="get-java-tag-value">
	  <xsl:with-param name="tag" select="'@webservicename'"/>
	</xsl:call-template>
      </xsl:variable>
      
      <head>
	<title><xsl:value-of select="$web-service-name"/> - PASTA</title>
      </head>

      <body>

	<!-- Making title web service name section -->
	<h1>
	  <font color="#0101DF"><xsl:copy-of select="$web-service-name"/>
	  web service API</font>
	</h1>
	
	<hr/>

	<!-- Making the Base URL section -->
	<h3>Base URL - 
	<xsl:call-template name="get-java-tag-value">
	  <xsl:with-param name="tag" select="'@baseurl'"/>
	</xsl:call-template>
	</h3>
	
	<hr/>

	<!-- Making the introduction section -->
	<xsl:call-template name="get-description"/>

	<hr/><br/>

	<!-- Saving all method elements that are annotated with HTTP verbs -->
	<xsl:variable name="web-service-methods"
		      select="methods/method[annotation[@type='GET' or 
			                                @type='POST' or 
							@type='PUT'or 
							@type='DELETE']]"/>

	<!-- making the Request Summary section -->
	<table border="1" cellpadding="3" cellspacing="0" width="100%">

	  <th colspan="2" align="left" bgcolor="#E0ECF8">
	    <h3>Request Summary</h3>
	  </th>

	  <tr>
	    <td><b>HTTP Verb : Relative URL</b></td>
	    <td><b>Brief description</b></td>
	  </tr>

	  <!-- Iterating thru each web service method -->
	  <xsl:for-each select="$web-service-methods">

	    <tr>
	      <!-- Making the Verb and relative URL entry -->
	      <td>
		<xsl:variable name="verb-url-pair">
		  <xsl:call-template name="make-verb-relative-url-pair"/>
		</xsl:variable>
		<a>
		  <xsl:attribute name="href">
		    <xsl:value-of select="concat('#', $verb-url-pair)"/>
		  </xsl:attribute>
		  <xsl:value-of select="$verb-url-pair"/>
		</a>
	      </td>
	      <!-- Making the brief description entry -->
	      <td>
		<xsl:call-template name="get-brief-description"/>
	      </td>
	    </tr>
	  </xsl:for-each>
	</table>
	<br/><hr/><br/>

	<!-- Making the Request Detail section -->
	<table border="1" cellpadding="3" cellspacing="0" width="100%">
	  <th colspan="2" align="left" bgcolor="#E0ECF8">
	    <h3>Request Detail</h3>
	  </th>
	</table>

	<!-- Iterating thru each web service method element -->
	<xsl:for-each select="$web-service-methods">
	  <br/>

	  <!-- Making and saving the Verb:URL pair -->
	  <xsl:variable name="verb-url-pair">
	    <xsl:call-template name="make-verb-relative-url-pair"/>
	  </xsl:variable>

	  <!-- Making an anchor for this request with the Verb:URL pair -->
	  <a>
	    <xsl:attribute name="name">
	      <xsl:value-of select="$verb-url-pair"/>
	    </xsl:attribute>
	  </a>

	  <!-- Making the description of this request -->
	  <table>
	    <th colspan="2" align="left">
	      <b><xsl:value-of select="$verb-url-pair"/></b>
	    </th>
	    <tr>
	      <td width="5%"></td>
	      <td>
		<br/>
		<xsl:call-template name="get-description"/>
	      </td>
	    </tr>
	  </table>
	  <hr/>
	</xsl:for-each>

	<br/><br/>
      </body>
    </html>

  </xsl:template>
</xsl:stylesheet>