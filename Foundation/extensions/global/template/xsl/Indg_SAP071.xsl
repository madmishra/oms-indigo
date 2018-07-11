<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
	<xsl:template match="OrderLineList">
		<OrderMessage>
			<xsl:attribute name="MessageTypeId">SAP071</xsl:attribute>
			<xsl:attribute name="Modifyts">
				<xsl:value-of select="OrderLine/@Modifyts"/>
			</xsl:attribute>
			<MessageBody>
				<Order>
					<xsl:attribute name="SterlingOrderNo">
						<xsl:value-of select="OrderLine/DerivedFromOrder/@OrderNo"/>
					</xsl:attribute>
					<xsl:attribute name="ReturnOrderNumber">
						<xsl:value-of select="OrderLine/Order/@OrderNo"/>
					</xsl:attribute>
					<xsl:attribute name="DocumentType">
						<xsl:value-of select="OrderLine/Order/@DocumentType"/>
					</xsl:attribute>
					<xsl:attribute name="OrderType">
						<xsl:value-of select="OrderLine/Order/@OrderType"/>
					</xsl:attribute>
					<xsl:attribute name="EnterpriseCode">
						<xsl:value-of select="OrderLine/Order/@EnterpriseCode"/>
					</xsl:attribute>
					<xsl:if test="OrderLine/References/Reference/@Name = 'HUValue'">
						<xsl:attribute name="HUValue">
							<xsl:value-of select="OrderLine/References/Reference/@Value"/>
						</xsl:attribute>
					</xsl:if>
					<OrderLines>
						<xsl:for-each select="OrderLine">
							<OrderLine>
								<xsl:attribute name="Qty">
									<xsl:value-of select="@OrderedQty"/>
								</xsl:attribute>
								<xsl:attribute name="SAPOrderNo">
									<xsl:value-of select="DerivedFromOrderLine/@CustomerLinePONo"/>
								</xsl:attribute>
								<xsl:attribute name="PrimeLineNo">
									<xsl:value-of select="@PrimeLineNo"/>
								</xsl:attribute>
								<xsl:attribute name="ReturnedNodeId">
									<xsl:value-of select="@ShipNode"/>
								</xsl:attribute>
								<xsl:for-each select="References/Reference">
									<xsl:if test="@Name = 'Disposition'">
										<xsl:attribute name="Disposition">
											<xsl:value-of select="@Value"/>
										</xsl:attribute>
									</xsl:if>
								</xsl:for-each>
								<Item>
									<xsl:attribute name="ItemID">
										<xsl:value-of select="Item/@ItemID"/>
									</xsl:attribute>
								</Item>
							</OrderLine>
						</xsl:for-each>	
					</OrderLines>
				</Order>
			</MessageBody>
		</OrderMessage>
	</xsl:template>
</xsl:stylesheet>