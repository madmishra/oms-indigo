package com.indigo.inventory;

import java.text.SimpleDateFormat;

import com.bridge.sterling.consts.XMLLiterals;
import com.bridge.sterling.framework.api.AbstractCustomApi;
import com.sterlingcommerce.tools.datavalidator.XmlUtils;
import com.yantra.yfc.core.YFCIterable;
import com.yantra.yfc.date.YTimestamp;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfc.dom.YFCElement;


/**
 * This custom class convert the RTAM message to
 * requested changes
 * 
 * 
 * @author BSG109
 *
 */
public class IndgDeltaInventoryExport extends AbstractCustomApi{

  
  private static final String INDG_DELTA_EXPORT_Q = "INDG_DELTA_EXPORT_Q";

  /**
   * This is the invoke point of the service
   * 
   */
  @Override
  public YFCDocument invoke(YFCDocument inXml) {
    YFCElement inEle = inXml.getDocumentElement();
    YFCIterable<YFCElement> yfsItr = inEle.getChildren(XMLLiterals.AVAILABILITY_CHANGE);
    for(YFCElement availabilityEle : yfsItr) {
      if(!XmlUtils.isVoid(availabilityEle.getAttribute(XMLLiterals.NODE))) {
        YTimestamp ts = availabilityEle.getYTimestampAttribute("OnhandAvailableDate");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        String onHandAvlDate = format.format(ts);
        onHandAvlDate = onHandAvlDate.substring(0,10)+"T"+onHandAvlDate.substring(11,23)+"Z";
        availabilityEle.setAttribute("OnhandAvailableDate", onHandAvlDate);
        String inputString = availabilityEle.toString();
        YFCDocument invExp = YFCDocument.getDocumentFor(inputString);
        invokeYantraService(getProperty(INDG_DELTA_EXPORT_Q), invExp);
      }
    }
    return inXml;
  } 

}