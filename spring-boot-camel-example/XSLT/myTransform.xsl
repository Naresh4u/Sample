<?xml version="1.0" encoding="iso-8859-1"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:template match="Root">
		<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
			xmlns:blz="http://thomas-bayer.com/blz/">
			<soapenv:Header />
			<soapenv:Body>
				<blz:getBank><blz:blz><xsl:value-of select="//blz" /></blz:blz></blz:getBank>
			</soapenv:Body>
		</soapenv:Envelope>
	</xsl:template>
	<xsl:template match="@*|node()">
		<xsl:apply-templates />
	</xsl:template>
</xsl:stylesheet>