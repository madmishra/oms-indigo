package com.indigo.om.outbound.api;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.bridge.sterling.consts.XMLLiterals;
import com.bridge.sterling.framework.api.AbstractCustomApi;
import com.bridge.sterling.utils.XPathUtil;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;

/**
 * 
 * 
 * @author BSG168
 *
 */
public class IndgCancelMissingLines extends AbstractCustomApi{
	YFCDocument docInputChangeOrderAPI=null;
	
	private static final String EMPTY_STRING = "";
	
	private static final String PRIMELINE_STATUS = "9000";
	private static final String ACTION_STATUS = "CANCEL";

	/**
	   * This is the invoke point of the Service
	 * @throws  
	   * 
	   */
	  @Override
	public YFCDocument invoke(YFCDocument docInXml) {
		YFCDocument docGetOrderLineList = getOrderLineListFunc(docInXml);
		getPrimeLineNo(docInXml, docGetOrderLineList);
		
		return docInputChangeOrderAPI;
	}
	  /**
	   * this method forms input to getOrderLineList API
	   * @param inXml
	   * @return
	   */
	
	public YFCDocument docGetOrderLineListInput(YFCDocument docInXml) {
		YFCElement eleInXmlOrder = docInXml.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).getChildElement(XMLLiterals.ORDER);
		String sOrderNo = eleInXmlOrder.getAttribute(XMLLiterals.STERLING_ORDER_NO);
		String sEnterpriseCode = eleInXmlOrder.getAttribute(XMLLiterals.ENTERPRISE_CODE);
		String sDocumentType = eleInXmlOrder.getAttribute(XMLLiterals.DOCUMENT_TYPE);
		YFCElement eleOrderLine = eleInXmlOrder.getChildElement(XMLLiterals.ORDER_LINES).getChildElement(XMLLiterals.ORDER_LINE);
		String sShipnode = eleOrderLine.getAttribute(XMLLiterals.SHIPNODE);
	    YFCDocument docGetOrderLineList = YFCDocument.createDocument(XMLLiterals.ORDER_LINE);
	    docGetOrderLineList.getDocumentElement().setAttribute(XMLLiterals.SHIPNODE, sShipnode);
	    YFCElement eleOrder = docGetOrderLineList.getDocumentElement().createChild(XMLLiterals.ORDER);
	    eleOrder.setAttribute(XMLLiterals.ORDER_NO, sOrderNo);
	    eleOrder.setAttribute(XMLLiterals.ENTERPRISE_CODE, sEnterpriseCode);
	    eleOrder.setAttribute(XMLLiterals.DOCUMENT_TYPE, sDocumentType);
	    return docGetOrderLineList;
	  }
	/**
	 * this method forms template for getOrderLineList API
	 * @return
	 */
	
	public YFCDocument docGetOrderLineListTemplate() {
	    YFCDocument docGetOrderListTemp = YFCDocument.createDocument(XMLLiterals.ORDER_LINE_LIST);
	    YFCElement eleOrderLine = docGetOrderListTemp.getDocumentElement().createChild(XMLLiterals.ORDER_LINE);
	    eleOrderLine.setAttribute(XMLLiterals.PRIME_LINE_NO, EMPTY_STRING);
	    eleOrderLine.setAttribute(XMLLiterals.SHIPNODE, EMPTY_STRING);
	    eleOrderLine.setAttribute(XMLLiterals.SUB_LINE_NO, EMPTY_STRING);
	    eleOrderLine.setAttribute(XMLLiterals.STATUS, EMPTY_STRING);
	    eleOrderLine.setAttribute(XMLLiterals.ORDERED_QTY, EMPTY_STRING);
	    eleOrderLine.setAttribute(XMLLiterals.ORIGINAL_ORDERED_QTY, EMPTY_STRING);
	    YFCElement eleItemEle = eleOrderLine.createChild(XMLLiterals.ITEM);
	    eleItemEle.setAttribute(XMLLiterals.ITEM_ID, EMPTY_STRING);
	    YFCElement eleExtn = eleOrderLine.createChild(XMLLiterals.EXTN);
	    eleExtn.setAttribute(XMLLiterals.EXTN_LEGACY_OMS_CHILD_ORDERNO, EMPTY_STRING);
	    eleExtn.setAttribute(XMLLiterals.EXTN_SAP_ORDER_NO, EMPTY_STRING);
	    YFCElement eleOrder = eleOrderLine.createChild(XMLLiterals.ORDER);
	    eleOrder.setAttribute(XMLLiterals.MODIFYTS, EMPTY_STRING);
	    eleOrder.setAttribute(XMLLiterals.ORDER_NO, EMPTY_STRING);
	    eleOrder.setAttribute(XMLLiterals.ENTERPRISE_CODE, EMPTY_STRING);
	    eleOrder.setAttribute(XMLLiterals.DOCUMENT_TYPE, EMPTY_STRING);
	    YFCElement eleOrderStatus = eleOrderLine.createChild(XMLLiterals.ORDER_STATUSES);
	    YFCElement eleStatus = eleOrderStatus.createChild(XMLLiterals.ORDER_STATUS);
	    eleStatus.setAttribute(XMLLiterals.STATUS, EMPTY_STRING);
	    
	    return docGetOrderListTemp;
	  }
	/**
	 * this method invokes getOrderLineList API
	 * @param inXml
	 * @return
	 */
	
	public YFCDocument getOrderLineListFunc(YFCDocument docInXml){
	    return  invokeYantraApi(XMLLiterals.GET_ORDER_LINE_LIST, docGetOrderLineListInput(docInXml), docGetOrderLineListTemplate());
	 }
	
	/**
	 * this method fetches common primeline no present in input Xml and getOrderLineList api output
	 * @param inXml
	 * @param getOrderLineListDoc
	 */
	public void getPrimeLineNo(YFCDocument docInXml, YFCDocument docGetOrderLineList) {
	 List<String> inXmlLineList=new ArrayList<String>();
	 List<String> getOrderLinesList=new ArrayList<String>();
		YFCElement eleOrderLineList = docGetOrderLineList.getDocumentElement();
		YFCIterable<YFCElement> apiPrimeLineNo = eleOrderLineList.getChildren();
		for(YFCElement elePrimeLine1:apiPrimeLineNo) {
			String sPrimeLineNo= elePrimeLine1.getAttribute(XMLLiterals.PRIME_LINE_NO);
			inXmlLineList.add(sPrimeLineNo);
	
		}
		YFCElement eleSapLineList= docInXml.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).
				getChildElement(XMLLiterals.ORDER).getChildElement(XMLLiterals.ORDER_LINES);
		YFCIterable<YFCElement> inputLineListEle = eleSapLineList.getChildren();
		for(YFCElement primeLineEle2:inputLineListEle) {
			String sPrimeLineNo= primeLineEle2.getAttribute(XMLLiterals.PRIME_LINE_NO);
			getOrderLinesList.add(sPrimeLineNo);
		}
		removeCommonPrimeLineNo(inXmlLineList, getOrderLinesList, docGetOrderLineList, docInXml);
	}
	/**
	 * this method removes common primeline no
	 * @param inXmlLineList
	 * @param getOrderLinesList
	 * @param getOrderLineListDoc
	 * @param inXml
	 */
	
	public void removeCommonPrimeLineNo(List<String> inXmlLineList, List<String> getOrderLinesList, YFCDocument docGetOrderLineList,
			YFCDocument docInXml) {
		Collection<String> primeLineNoList=new ArrayList<String>();
		List<String> union=new ArrayList<>(getOrderLinesList);
		union.addAll(inXmlLineList);
		List<String> common=new ArrayList<>(getOrderLinesList);
		common.retainAll(inXmlLineList);
		union.removeAll(common);
		for(String i: union)
		{
			primeLineNoList.add(i);
			
		}
		cancelMissingPrimeLineNo(primeLineNoList, docGetOrderLineList, docInXml);
	}
	/**
	 * this method cancels the missing prime line no
	 * @param primeLineNoList
	 * @param getOrderLineListDoc
	 * @param inXml
	 */
	private void cancelMissingPrimeLineNo(Collection<String> primeLineNoList, YFCDocument docGetOrderLineList, YFCDocument docInXml) {
		for(String primeLineNoValue:primeLineNoList) {
			YFCElement eleGetOrderLineList = XPathUtil.getXPathElement(docGetOrderLineList, 
					"/OrderLineList/OrderLine[@PrimeLineNo = \""+primeLineNoValue+"\"]");
			if(!XmlUtils.isVoid(eleGetOrderLineList)) {
				String status = eleGetOrderLineList.getChildElement(XMLLiterals.ORDER_STATUSES).
						getChildElement(XMLLiterals.ORDER_STATUS).getAttribute(XMLLiterals.STATUS);
				if(!PRIMELINE_STATUS.equals(status)) {
					changeOrderInput(docGetOrderLineList,primeLineNoValue);
				}
			}
		}
		sendCancelledPrimeLineNoDoc(docInXml, docInputChangeOrderAPI,docGetOrderLineList);
	}
	
	/**
	 * this method forms input for changeOrder api
	 * @param getOrderLineListDoc
	 * @param primeLineNoValue
	 */
	private void changeOrderInput(YFCDocument docGetOrderLineList,String sPrimeLineNo) {
		String sOrderNumber = docGetOrderLineList.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
				getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.ORDER_NO);
		String sEnterpriseCode = docGetOrderLineList.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
				getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.ENTERPRISE_CODE);
		String sDocumentType = docGetOrderLineList.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
				getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.DOCUMENT_TYPE);
		String sSubLineNo = docGetOrderLineList.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE).
				getAttribute(XMLLiterals.SUB_LINE_NO);
		
		docInputChangeOrderAPI = YFCDocument.createDocument(XMLLiterals.ORDER);
		docInputChangeOrderAPI.getDocumentElement().setAttribute(XMLLiterals.ORDER_NO, sOrderNumber);
		docInputChangeOrderAPI.getDocumentElement().setAttribute(XMLLiterals.ENTERPRISE_CODE, sEnterpriseCode);
		docInputChangeOrderAPI.getDocumentElement().setAttribute(XMLLiterals.DOCUMENT_TYPE, sDocumentType);
		YFCElement eleOrderLines = docInputChangeOrderAPI.getDocumentElement().createChild(XMLLiterals.ORDER_LINES);
		YFCElement eleOrderLine = eleOrderLines.createChild(XMLLiterals.ORDER_LINE);
		eleOrderLine.setAttribute(XMLLiterals.PRIME_LINE_NO, sPrimeLineNo);
		eleOrderLine.setAttribute(XMLLiterals.SUB_LINE_NO, sSubLineNo);
		eleOrderLine.setAttribute(XMLLiterals.ACTION, ACTION_STATUS);
		invokeYantraApi(XMLLiterals.CHANGE_ORDER_API, docInputChangeOrderAPI);
	}
	
	/**
	 * this method forms document containing cancelled order line
	 * @param inXml
	 * @param inputDocForChangeOrderAPI
	 * @param getOrderLineListDoc
	 * @return
	 */
	private YFCDocument sendCancelledPrimeLineNoDoc(YFCDocument docInXml, YFCDocument docInputChangeOrderAPI,YFCDocument docGetOrderLineList) {
		YFCElement eleorderLine=docGetOrderLineList.getDocumentElement();
		String sModifyts = eleorderLine.getChildElement(XMLLiterals.ORDER_LINE).getChildElement(XMLLiterals.ORDER).
				getAttribute(XMLLiterals.MODIFYTS);
		cancellingLine(docGetOrderLineList,eleorderLine);
		String sChildOrderNo = docInXml.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).
				getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.RELEASE_NO);
		String sSapOrderNo=docInXml.getDocumentElement().getChildElement(XMLLiterals.MESSAGE_BODY).
				getChildElement(XMLLiterals.ORDER).getAttribute(XMLLiterals.SAP_ORDER_NO);
		docInputChangeOrderAPI.getDocumentElement().setAttribute(XMLLiterals.MODIFYTS, sModifyts);
		YFCElement eleExtn = docInputChangeOrderAPI.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINES).
				getChildElement(XMLLiterals.ORDER_LINE).createChild(XMLLiterals.EXTN);
		eleExtn.setAttribute(XMLLiterals.EXTN_LEGACY_OMS_CHILD_ORDERNO, sChildOrderNo);
		eleExtn.setAttribute(XMLLiterals.EXTN_SAP_ORDER_NO, sSapOrderNo);
		return docInputChangeOrderAPI;
	}
	/**
	 * this method cancel the orderLine 
	 * @param getOrderLineListDoc
	 * @param orderLineEle
	 */
	private void cancellingLine(YFCDocument docGetOrderLineList,YFCElement eleorderLine) {
	
		YFCIterable<YFCElement> getOrderLineEle = eleorderLine.getChildren();
	    for(YFCElement orderLine: getOrderLineEle) {
	    	String primeLineNo1 = orderLine.getAttribute(XMLLiterals.PRIME_LINE_NO);
	    	YFCIterable<YFCElement> changeOrderLineEle = docInputChangeOrderAPI.getDocumentElement().
	    			getChildElement(XMLLiterals.ORDER_LINES).getChildren();
	    	for(YFCElement changeOrderLine: changeOrderLineEle) {
	    		String primeLineNo2 = changeOrderLine.getAttribute(XMLLiterals.PRIME_LINE_NO);
	    		
	    		if(primeLineNo1.equals(primeLineNo2)) {
	    			String sCurrentQty = "0";
	    			String sOriginalQty = docGetOrderLineList.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINE)
	    					.getAttribute(XMLLiterals.ORIGINAL_ORDERED_QTY);
	    			docInputChangeOrderAPI.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINES).
	    			getChildElement(XMLLiterals.ORDER_LINE).setAttribute(XMLLiterals.CURRENT_QTY, sCurrentQty);
	    			docInputChangeOrderAPI.getDocumentElement().getChildElement(XMLLiterals.ORDER_LINES).
	    			getChildElement(XMLLiterals.ORDER_LINE).setAttribute(XMLLiterals.ORIGINAL_QTY, sOriginalQty);
	    		}
	    	}
	    }
	}
	
}
