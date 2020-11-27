package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import com.dc.common.CommonUtil;
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
   * 系统和服务码映射：key：系统代码 ；value：服务码列表。目的是用来生成服务识别和系统识别文件
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
   * 添加注释节点信息
   * @param last 结束语
   */
  public void addCommentNode(String last){
    String comment = CommonUtil.getDefaultComment(last);
    MetadataNode commentNode = new MetadataNode();
    commentNode.setCommentFlag(true);
    commentNode.setComment(comment);
    getMetadataNodeList().add(commentNode);
    XmlObject xmlObject = new XmlObject();
    xmlObject.setCommentFlag(true);
    xmlObject.setComment(comment);
    xmlObject.setRootElement(true);
    getXmlObjectList().add(xmlObject);
  }

  /**
   * 解析系统和服务码
   * @param system
   * @param serviceCode
   */
  private void parseSystemInfo(Map<String,HrSystem> system,String serviceCode){
    if (MapUtil.isNotEmpty(system)){
      parseSystemInfo(system.get(Constants.CONSUMER_CHANNEL),serviceCode);
      parseSystemInfo(system.get(Constants.PROVIDER_SYSTEM),serviceCode);
    }
  }

  private void parseSystemInfo(HrSystem system,String serviceCode){
    if (ObjectUtil.isNotNull(system)){
      List<String> codeList = getServiceCodeMapping().get(system.getCode());
      if (CollUtil.isEmpty(codeList)){
        List<String> list = new ArrayList<>();
        list.add(serviceCode);
        getServiceCodeMapping().put(system.getCode(), list);
      }else {
        codeList.add(serviceCode);
      }
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
