<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="OrderMessage">
		<Order>
			<xsl:attribute name="OrderNo">
				<xsl:value-of select="MessageBody/Order/@ParentLegacyOMSOrderNo"/>
			</xsl:attribute>
			<xsl:attribute name="EnterpriseCode">
				<xsl:value-of select="MessageBody/Order/@EnterpriseCode"/>
			</xsl:attribute>
			<xsl:attribute name="DocumentType">
				<xsl:value-of select="MessageBody/Order/@DocumentType"/>
			</xsl:attribute>
			<xsl:attribute name="OrderType">
				<xsl:value-of select="MessageBody/Order/@OrderType"/>
			</xsl:attribute>
			<OrderLines>
				<xsl:for-each select="MessageBody/Order/OrderLines/OrderLine">
					<OrderLine>
						<xsl:attribute name="Action">MODIFY</xsl:attribute>
						<xsl:attribute name="PrimeLineNo">
							<xsl:value-of select="@PrimeLineNo"/>
						</xsl:attribute>
						<xsl:attribute name="SubLineNo">1</xsl:attribute>
						<LinePriceInfo>
							<xsl:attribute name="RetailPrice">
								<xsl:value-of select="LinePriceInfo/@RetailPrice"/>
							</xsl:attribute>
						</LinePriceInfo>						
					</OrderLine>
				</xsl:for-each>				
			</OrderLines>
		</Order>
	</xsl:template>
</xsl:stylesheet>