package com.dc.xml.post;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.config.HrSystem;
import com.dc.xml.XmlObject;
import com.dc.xml.XpathParser;
import org.dom4j.Document;

/**
 * 统一图形前端的后置处理器
 *
 * @author: Administrator
 * @date: 2020-11-30 22:14
 * @version: 1.0
 */
public class XmlPostSmrtlr implements XmlPost {


  @Override
  public void invoke(XmlObject xmlObject) {

    if (ObjectUtil.isNull(xmlObject)){
      return;
    }

    if (!(MapUtil.isNotEmpty(xmlObject.getSystem()) &&
            ObjectUtil.isNotNull(xmlObject.getSystem().get(Constants.CONSUMER_CHANNEL)))){
      return;
    }
    HrSystem consumer = xmlObject.getSystem().get(Constants.CONSUMER_CHANNEL);
    if (!Constants.SMRTLR.equals(consumer.getCode())){
      return;
    }

    if (!xmlObject.isRootElement()){
      return;
    }
    xmlObject.setTagName(Constants.NODE_SERVICE);
    //生成in端文件
    Document inDoc = CommonUtil.createDocument(xmlObject);
    //生成out端文件
    Document outDoc = CommonUtil.createDocument(xmlObject);

    //处理输入字段,生成拆组包文件
    xmlObject.dealChildTags(xmlObject.getChildTags().get(Constants.IN),inDoc,"",Constants.SMRTLR);
    String in = CommonUtil.generateFilePathInputIn(consumer.getCode(), xmlObject.getServiceCode());
    XpathParser.DOCUMENT_MAP.put(in,inDoc);
    //处理输出字段,生成拆文件组包
    xmlObject.dealChildTags(xmlObject.getChildTags().get(Constants.OUT),outDoc,"",Constants.SMRTLR);
    String out = CommonUtil.generateFilePathOutputIn(consumer.getCode(), xmlObject.getServiceCode());
    XpathParser.DOCUMENT_MAP.put(out,outDoc);

  }

}
