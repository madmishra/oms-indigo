package com.indigo.om.outbound.api;

import com.bridge.sterling.consts.ExceptionLiterals;
import com.bridge.sterling.framework.api.AbstractCustomApi;
import com.bridge.sterling.utils.ExceptionUtil;
import com.yantra.yfc.dom.YFCDocument;
import com.yantra.yfs.japi.YFSException;

public class IndgDelay extends AbstractCustomApi {

  @Override
  public YFCDocument invoke(YFCDocument inXml) throws YFSException {
    try {
      Thread.sleep(10000);
    } catch (Exception exp) {
      throw ExceptionUtil.getYFSException(ExceptionLiterals.ERRORCODE_SYNC_EXP, exp);
    }
    return inXml;
  }

}