package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import com.dc.common.Constants;
import com.dc.config.HrSystem;
import com.dc.core.MetadataNode;

import java.util.*;

/**
 * 元数据信息
 *
 * @author: Administrator
 * @date: 2020-11-25 22:39
 * @version: 1.0
 */
public class XmlMetadata {

  /**
   * 元数据metadata集合。因为不重复，所以选择Set
   */
  private Set<MetadataNode> metadataNodeList ;

  /**
   * 对象容器，支持一次解析多个服务
   */
  private List<XmlObject> xmlObjectList ;

  /**
   * 系统和服务码映射：key：系统代码 ；value：服务码列表
   */
  private Map<String,List<String>> serviceCodeMapping ;

  public void parseXmlObject(XmlObject xmlObject){
    //解析sheet页
    getXmlObjectList().add(xmlObject);
    //解析元数据
    parseMetadata(xmlObject.getChildTags().get(Constants.IN));
    parseMetadata(xmlObject.getChildTags().get(Constants.OUT));
    //解析系统和服务码
    parseSystemInfo(xmlObject.getSystem(),xmlObject.getServiceCode());
  }

  /**
   * 解析系统和服务码
   * @param system
   * @param serviceCode
   */
  private void parseSystemInfo(List<HrSystem> system,String serviceCode){
    if (CollUtil.isNotEmpty(system)){
      system.stream().forEach(s -> {
        List<String> codeList = getServiceCodeMapping().get(s.getCode());
        if (CollUtil.isEmpty(codeList)){
          getServiceCodeMapping().put(s.getCode(), ListUtil.toList(serviceCode));
        }
      });
    }
  }

  /**
   * 处理元数据
   * @param childTags
   */
  private void parseMetadata(List<XmlObject> childTags){
    if (CollUtil.isNotEmpty(childTags)){
      childTags.stream().forEach(childTag -> {
        MetadataNode node = new MetadataNode();
        node.setNodeName(childTag.getTagName());
        Map<String, String> attrs = childTag.getAttrs();
        if (MapUtil.isNotEmpty(attrs)){
          node.setAttr(attrs);
        }
        getMetadataNodeList().add(node);
      });
    }
  }

  public Set<MetadataNode> getMetadataNodeList() {
    if (CollUtil.isEmpty(metadataNodeList)){
      this.metadataNodeList = new HashSet<>();
    }
    return metadataNodeList;
  }

  public void setMetadataNodeList(Set<MetadataNode> metadataNodeList) {
    this.metadataNodeList = metadataNodeList;
  }

  public List<XmlObject> getXmlObjectList() {
    if (CollUtil.isEmpty(xmlObjectList)){
      this.xmlObjectList = new ArrayList<>();
    }
    return xmlObjectList;
  }

  public void setXmlObjectList(List<XmlObject> xmlObjectList) {
    this.xmlObjectList = xmlObjectList;
  }

  public Map<String, List<String>> getServiceCodeMapping() {
    if (MapUtil.isEmpty(serviceCodeMapping)){
      this.serviceCodeMapping = new LinkedHashMap<>();
    }
    return serviceCodeMapping;
  }

  public void setServiceCodeMapping(Map<String, List<String>> serviceCodeMapping) {
    this.serviceCodeMapping = serviceCodeMapping;
  }
}
