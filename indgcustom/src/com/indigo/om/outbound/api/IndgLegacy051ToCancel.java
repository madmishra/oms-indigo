package com.indigo.om.outbound.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.bridge.sterling.consts.XMLLiterals;
import com.bridge.sterling.framework.api.AbstractCustomApi;
import com.bridge.sterling.utils.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;
import com.yantra.yfc.dom.YFCNode;

/**
 * 
 * @author BGS168
 *
 * Custom API to consume LegacyOMS051 message and cancel the given lines 
 * The lines will be grouped by ReasonCode and send it to ChangeOrder API.
 * The lines will be grouped by ShipNode to sends SAP051 message to SAP.
 * 
 */
public class IndgLegacy051ToCancel extends AbstractCustomApi{
	 Map<String,List<YFCElement>> orderLineMapGroupByReasonCode = new HashMap<>();
	 Map<String,List<YFCElement>> orderLineMapGroupByShipNode = new HashMap<>();
	 private static final String SUBLINE_VALUE = "1";
	 private static final String ACTION_VALUE = "CANCEL";
	 private static final String EMPTY_STRING = "";
	 private static final String CALL_SAP051_SERVICE = "Indg_SAP051_OnLegacy051";
	 private String isFullOrderCancelled = "";
	 private static final String CANCELLED = "Cancelled";
	 private static final String NO = "N";
	 private static final String YES = "Y";
	 private static final String REASON_CODE = "03";
	 private String orderNo = "";
	 private String documentType = "";
	 private String enterpriseCode = "";
	 private String orderType = "";
	 
	 /**
	  * This method is the invoke point of the service.
	  * 
	  */
	 
	@Override
	public YFCDocument invoke(YFCDocument inXml) {
		orderNo = inXml.getDocumentElement().getAttribute(XMLLiterals.ORDER_NO);
		orderType = inXml.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).getChildElement(XMLLiterals.ORDER).
				getAttribute(XMLLiterals.ORDER_TYPE);
		String inputDocString = inXml.toString();
	    YFCDocument docLegacy051Input = YFCDocument.getDocumentFor(inputDocString);
	    
	    getOrderLinesGroupByReasonCode(docLegacy051Input);
	    docCancelOrderLines(docLegacy051Input, inXml);
	    
	    YFCDocument docSAP051Input = YFCDocument.getDocumentFor(inputDocString);
		getOrderLinesGroupByShipNode(docSAP051Input);
		docSAP051GetAttributes();
		return inXml;
	}
	
	/**
	 * This method takes the input file and group the OrderLines
	 * based on ReasonCode.
	 * 
	 * @param docLegacy051Input
	 */
	
	private void getOrderLinesGroupByReasonCode(YFCDocument docLegacy051Input){
	    YFCElement orderLinesEle = docLegacy051Input.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY)
	    		.getChildElement(XMLLiterals.ORDER).getChildElement(XMLLiterals.ORDER_LINES);
	    YFCIterable<YFCElement> yfsItrator = orderLinesEle.getChildren(XMLLiterals.ORDER_LINE);
	    for(YFCElement orderLine: yfsItrator) {
	      List<YFCElement> orderLineList;
	      String cancellationReasonCode = orderLine.getAttribute(XMLLiterals.CANCELLATION_REASON_CODE);
	      if(XmlUtils.isVoid(orderLineMapGroupByReasonCode.get(cancellationReasonCode))) {
	        orderLineList = new ArrayList<>();	
	        orderLineList.add(orderLine);
	        orderLineMapGroupByReasonCode.put(cancellationReasonCode,orderLineList);
	      }
	      else {
	        orderLineList = orderLineMapGroupByReasonCode.get(cancellationReasonCode);
	        orderLineList.add(orderLine);
	        orderLineMapGroupByReasonCode.put(cancellationReasonCode,orderLineList);
	      }
	      YFCNode parent = orderLine.getParentNode();
	      parent.removeChild(orderLine);
	    }
	  }
	
	/**
	 * This method forms the input for ChangeOrder API (lines grouped by ReasonCode).
	 * and returns the input message back to service from where
	 * LegacyOMS052 message will be formed.
	 * 
	 * @param docLegacy051Input
	 * @param inXml
	 * @return
	 */
	
	private YFCDocument docCancelOrderLines(YFCDocument docLegacy051Input, YFCDocument inXml){
		for (Entry<String, List<YFCElement>> entry : orderLineMapGroupByReasonCode.entrySet()) {
			YFCDocument docChangeOrderInputLines = YFCDocument.createDocument(XMLLiterals.ORDER_LINES);
		      List<YFCElement> orderLineList = orderLineMapGroupByReasonCode.get(entry.getKey());
		      YFCElement orderLinesEle = docChangeOrderInputLines.getDocumentElement();
		      
		      docGetAttributesForCancel(orderLineList, orderLinesEle, docChangeOrderInputLines, docLegacy051Input, inXml);
		      
		}
		return inXml;
	}
	
	/**
	 * This code fetches all the necessary attributes for ChangeOrder Input doc
	 * 
	 * @param orderLineList
	 * @param orderLinesEle
	 * @param docChangeOrderInputLines
	 * @param docLegacy051Input
	 * @param inXml
	 */
	
	private void docGetAttributesForCancel(List<YFCElement> orderLineList, YFCElement orderLinesEle, 
			YFCDocument docChangeOrderInputLines, YFCDocument docLegacy051Input, YFCDocument inXml) {
		for(YFCElement lineEle : orderLineList) {
	        orderLinesEle.importNode(lineEle);
	      } 
		YFCDocument docChangeOrderApiInput = YFCDocument.createDocument(XMLLiterals.ORDER);
	      enterpriseCode = docLegacy051Input.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).
	    		  getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.ENTERPRISE_CODE);
	      documentType = docLegacy051Input.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).
	    		  getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.DOCUMENT_TYPE);
	      
	      docSetAttributesForCancel(docChangeOrderApiInput, docChangeOrderInputLines, inXml);
	}
	
	/**
	 * This code appends the attributes to for the input for
	 * changeOrder API
	 * 
	 * @param docChangeOrderApiInput
	 * @param orderNo
	 * @param enterpriseCode
	 * @param documentType
	 * @param docChangeOrderInputLines
	 * @param inXml
	 */
	
	private void docSetAttributesForCancel(YFCDocument docChangeOrderApiInput, YFCDocument docChangeOrderInputLines,
			YFCDocument inXml) {
		  String reasonCode = docChangeOrderInputLines.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
	    		  getAttribute(XMLLiterals.CANCELLATION_REASON_CODE);
	      String reasonText = docChangeOrderInputLines.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
	    		  getAttribute(XMLLiterals.CANCELLATION_TEXT);
		  docChangeOrderApiInput.getDocumentElement().setAttribute(XMLLiterals.MODIFICATION_REASON_CODE, reasonCode);
	      docChangeOrderApiInput.getDocumentElement().setAttribute(XMLLiterals.MODIFICATION_REASON_TEXT, reasonText);
	      docChangeOrderApiInput.getDocumentElement().setAttribute(XMLLiterals.ORDER_NO, orderNo);
	      docChangeOrderApiInput.getDocumentElement().setAttribute(XMLLiterals.ENTERPRISE_CODE, enterpriseCode);
	      docChangeOrderApiInput.getDocumentElement().setAttribute(XMLLiterals.DOCUMENT_TYPE, documentType);
	      YFCElement orderLinesElement = docChangeOrderApiInput.getDocumentElement().createChild(XMLLiterals.ORDER_LINES);
	      YFCElement inputEle = docChangeOrderInputLines.getDocumentElement();
		  
	      docInputChangeOrderApi(docChangeOrderApiInput, inXml, inputEle, orderLinesElement);
	}
	
	/**
	 * This code appends the attributes for ChangeOrder API and
	 * calls the API.
	 * 
	 * @param docChangeOrderApiInput
	 * @param inXml
	 * @param inputEle
	 * @param orderLinesElement
	 */
	
	private void docInputChangeOrderApi(YFCDocument docChangeOrderApiInput, YFCDocument inXml, YFCElement inputEle, 
			YFCElement orderLinesElement) {
		 YFCIterable<YFCElement> yfsItrator = inputEle.getChildren(XMLLiterals.ORDER_LINE);
		 for(YFCElement orderLine : yfsItrator) {
			String primeLineNo = orderLine.getAttribute(XMLLiterals.PRIME_LINE_NO);
			YFCElement orderLineEle = orderLinesElement.createChild(XMLLiterals.ORDER_LINE);
			orderLineEle.setAttribute(XMLLiterals.PRIME_LINE_NO, primeLineNo);
			orderLineEle.setAttribute(XMLLiterals.SUB_LINE_NO, SUBLINE_VALUE);
			orderLineEle.setAttribute(XMLLiterals.ACTION, ACTION_VALUE);
		}
	    YFCDocument changeOrderOutput = invokeYantraApi(XMLLiterals.CHANGE_ORDER_API, docChangeOrderApiInput,
	    		getChangeOrderTemplateDoc());    
	    String modifyTS = changeOrderOutput.getDocumentElement().getAttribute(XMLLiterals.MODIFYTS);
	    inXml.getDocumentElement().setAttribute(XMLLiterals.MODIFYTS, modifyTS);
	}
	
	/**
	 * This method will group the OrderLines based on the ShipNode.
	 * 
	 * @param inXml
	 */
	
	public void getOrderLinesGroupByShipNode(YFCDocument docSAP051Input) {
		YFCElement orderLinesEle = docSAP051Input.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY)
	    		.getChildElement(XMLLiterals.ORDER).getChildElement(XMLLiterals.ORDER_LINES);
	    YFCIterable<YFCElement> yfsItrator = orderLinesEle.getChildren(XMLLiterals.ORDER_LINE);
	    for(YFCElement orderLine: yfsItrator) {
	      List<YFCElement> orderLineList;
	      String shipNodeValue = orderLine.getAttribute(XMLLiterals.SHIPNODE);
	      if(XmlUtils.isVoid(orderLineMapGroupByShipNode.get(shipNodeValue))) {
	        orderLineList = new ArrayList<>();	
	        orderLineList.add(orderLine);
	        orderLineMapGroupByShipNode.put(shipNodeValue,orderLineList);
	      }
	      else {
	        orderLineList = orderLineMapGroupByShipNode.get(shipNodeValue);
	        orderLineList.add(orderLine);
	        orderLineMapGroupByShipNode.put(shipNodeValue,orderLineList);
	      }
	      YFCNode parent = orderLine.getParentNode();
	      parent.removeChild(orderLine);
	    }
	}
	
	/**
	 * This message now forms the LegacyOMS051 message grouped by ship nodes
	 * 
	 * @param inXml
	 */
	
	private void docSAP051GetAttributes() {
		for (Entry<String, List<YFCElement>> entry : orderLineMapGroupByShipNode.entrySet()) {
			YFCDocument groupByShipNodeDoc = YFCDocument.createDocument(XMLLiterals.ORDER);
			YFCElement orderLinesEle = groupByShipNodeDoc.getDocumentElement().createChild(XMLLiterals.ORDER_LINES);
		    List<YFCElement> orderLineList = orderLineMapGroupByShipNode.get(entry.getKey());
		    for(YFCElement lineEle : orderLineList) {
		      orderLinesEle.importNode(lineEle);
		    }
		    YFCDocument getOrderLineListDoc = getOrderLineListFunc(groupByShipNodeDoc);
		    docSAP051Input(groupByShipNodeDoc, getOrderLineListDoc);
		}
	}
	
	/**
	 * This code appends original quantity and ordered quantity in
	 * SAP051 message
	 * 
	 * @param groupByShipNodeDoc
	 * @param getOrderLineListDoc
	 */
	
	private void docSAP051Input(YFCDocument groupByShipNodeDoc, YFCDocument getOrderLineListDoc) {
		YFCElement rootEle = groupByShipNodeDoc.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINES);
	    YFCIterable<YFCElement> yfsItrator = rootEle.getChildren(XMLLiterals.ORDER_LINE);
	    for(YFCElement orderLine : yfsItrator) {
	    	String primeLineNo = orderLine.getAttribute(XMLLiterals.PRIME_LINE_NO);
	    	YFCElement orderLineEle = XPathUtil.getXPathElement(getOrderLineListDoc, "/OrderLineList/OrderLine[@PrimeLineNo = \""+
	    	primeLineNo+"\"]");
	    	String currentQty = orderLineEle.getAttribute(XMLLiterals.ORDERED_QTY);
			String originalQty = orderLineEle.getAttribute(XMLLiterals.ORIGINAL_ORDERED_QTY);
			orderLine.setAttribute(XMLLiterals.ORDERED_QTY, currentQty);
			orderLine.setAttribute(XMLLiterals.ORIGINAL_ORDERED_QTY, originalQty);
			orderLine.setAttribute(XMLLiterals.CANCELLATION_REASON_CODE, REASON_CODE);
	    }
	    sendShipNodeDocToService(groupByShipNodeDoc, getOrderLineListDoc);
	}
	
	/**
	 * This code checks if the full order is cancelled or not
	 * and appends the value in SAP051 doc.
	 * 
	 * @param groupByShipNodeDoc
	 * @param getOrderLineListDoc
	 */
	
	private void sendShipNodeDocToService(YFCDocument groupByShipNodeDoc, YFCDocument getOrderLineListDoc) {
	    String sapOrderNo = getOrderLineListDoc.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
	    		getChildElement(XMLLiterals.EXTN).getAttribute(XMLLiterals.EXTN_SAP_ORDER_NO);
	    String modifyTs = getOrderLineListDoc.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
	    		getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.MODIFYTS);
	    groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.EXTN_SAP_ORDER_NO, sapOrderNo);
	    groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.MODIFYTS, modifyTs);
	    groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.STERLING_ORDER_NO, orderNo);
	    YFCElement getOrderLineListOutputEle=getOrderLineListDoc.getDocumentElement();
		YFCIterable<YFCElement> inputOrderLineEle = getOrderLineListOutputEle.getChildren(XMLLiterals.ORDER_LINE);
		for(YFCElement orderElement : inputOrderLineEle) {
			String orderLineStatus=orderElement.getAttribute(XMLLiterals.STATUS);
			if(!orderLineStatus.equals(CANCELLED))
			{ isFullOrderCancelled = NO;
				break; }
			else
			{ isFullOrderCancelled = YES; }
		}	
		if(isFullOrderCancelled.equals(NO)) {
			groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.IS_FULL_ORDER_CANCELLED, isFullOrderCancelled);
		}
		else
			groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.IS_FULL_ORDER_CANCELLED, isFullOrderCancelled);
		docAddOrderLevelAttr(groupByShipNodeDoc);
	}
	
	/**
	 * This code adds the necessary attributes to the SAP051 doc
	 * 
	 * @param groupByShipNodeDoc
	 */
	
	private void docAddOrderLevelAttr(YFCDocument groupByShipNodeDoc) {
		groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.DOCUMENT_TYPE, documentType);
		groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.ENTERPRISE_CODE, enterpriseCode);
		groupByShipNodeDoc.getDocumentElement().setAttribute(XMLLiterals.ORDER_TYPE, orderType);
		
		callUserUpdateQueue(groupByShipNodeDoc);
	}
	
	/**
	 * This code calls the service 
	 * @param doc
	 */
	
	private void callUserUpdateQueue(YFCDocument doc) {
	     invokeYantraService(CALL_SAP051_SERVICE, doc);
	}
	
	/**
	 * This code forms the input for getOrderLineList API doc.
	 * 
	 * @param groupByShipNodeDoc
	 * @return
	 */
	
	public YFCDocument getOrderLineListInDoc(YFCDocument groupByShipNodeDoc) {
		YFCElement inEle = groupByShipNodeDoc.getDocumentElement();
		String shipnode = inEle.getChildElement(XMLLiterals.ORDER_LINES).getChildElement(XMLLiterals.ORDER_LINE).
				getAttribute(XMLLiterals.SHIPNODE);
	    YFCDocument getOrderDoc = YFCDocument.createDocument(XMLLiterals.ORDER_LINE);
	    getOrderDoc.getDocumentElement().setAttribute(XMLLiterals.SHIPNODE, shipnode);
	    YFCElement orderEle = getOrderDoc.getDocumentElement().createChild(XMLLiterals.ORDER);
	    orderEle.setAttribute(XMLLiterals.ORDER_NO, orderNo);
	    orderEle.setAttribute(XMLLiterals.ENTERPRISE_CODE, enterpriseCode);
	    orderEle.setAttribute(XMLLiterals.DOCUMENT_TYPE, documentType);
	    return getOrderDoc;
	  }
	
	/**
	 * This code forms the template for getOrderLineList API
	 * 
	 * @return
	 */
	
	public YFCDocument getOrderLineListTemplateDoc() {
	    YFCDocument getOrderListTemp = YFCDocument.createDocument(XMLLiterals.ORDER_LINE_LIST);
	    YFCElement orderLineEle = getOrderListTemp.getDocumentElement().createChild(XMLLiterals.ORDER_LINE);
	    orderLineEle.setAttribute(XMLLiterals.PRIME_LINE_NO, EMPTY_STRING);
	    orderLineEle.setAttribute(XMLLiterals.SHIPNODE, EMPTY_STRING);
	    orderLineEle.setAttribute(XMLLiterals.SUB_LINE_NO, EMPTY_STRING);
	    orderLineEle.setAttribute(XMLLiterals.STATUS, EMPTY_STRING);
	    orderLineEle.setAttribute(XMLLiterals.ORDERED_QTY, EMPTY_STRING);
	    orderLineEle.setAttribute(XMLLiterals.ORIGINAL_ORDERED_QTY, EMPTY_STRING);
	    YFCElement itemEle = orderLineEle.createChild(XMLLiterals.ITEM);
	    itemEle.setAttribute(XMLLiterals.ITEM_ID, EMPTY_STRING);
	    YFCElement extnEle = orderLineEle.createChild(XMLLiterals.EXTN);
	    extnEle.setAttribute(XMLLiterals.EXTN_LEGACY_OMS_CHILD_ORDERNO, EMPTY_STRING);
	    extnEle.setAttribute(XMLLiterals.EXTN_SAP_ORDER_NO, EMPTY_STRING);
	    YFCElement orderEle = orderLineEle.createChild(XMLLiterals.ORDER);
	    orderEle.setAttribute(XMLLiterals.MODIFYTS, EMPTY_STRING);
	    orderEle.setAttribute(XMLLiterals.ORDER_NO, EMPTY_STRING);
	    orderEle.setAttribute(XMLLiterals.ENTERPRISE_CODE, EMPTY_STRING);
	    orderEle.setAttribute(XMLLiterals.DOCUMENT_TYPE, EMPTY_STRING);
	    YFCElement orderStatusEle = orderLineEle.createChild(XMLLiterals.ORDER_STATUSES);
	    YFCElement statusEle = orderStatusEle.createChild(XMLLiterals.ORDER_STATUS);
	    statusEle.setAttribute(XMLLiterals.STATUS, EMPTY_STRING);
	    
	    return getOrderListTemp;
	  }
	
	/**
	 * This code call the getOrderLineList API
	 * 
	 * @param groupByShipNodeDoc
	 * @return
	 */
	
	public YFCDocument getOrderLineListFunc(YFCDocument groupByShipNodeDoc){
	    return  invokeYantraApi(XMLLiterals.GET_ORDER_LINE_LIST, getOrderLineListInDoc(groupByShipNodeDoc), 
	    		getOrderLineListTemplateDoc());
	 }
	
	/**
	 * This code forms the template for changeOrde API
	 * 
	 * @return
	 */
	
	public YFCDocument getChangeOrderTemplateDoc() {
		 YFCDocument getChangeOrderTemp = YFCDocument.createDocument(XMLLiterals.ORDER);
		 getChangeOrderTemp.getDocumentElement().setAttribute(XMLLiterals.MODIFYTS, EMPTY_STRING);
		 return getChangeOrderTemp;
	}
}

