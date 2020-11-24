package com.dc.xml.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import com.dc.common.Constants;
import org.apache.poi.poifs.filesystem.DocumentNode;
import org.dom4j.*;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Xml解析器
 * @author: Administrator
 * @date: 2020-11-23 23:58
 * @version: 1.0
 */
public class XpathParser {

  private static final Log log = LogFactory.get(XpathParser.class);

  /**
   * 读取xml
   * @param f
   * @return
   */
  public static Document readXml(File f) {
    SAXReader reader = new SAXReader();
    Document doc = null;
    try {
      doc = reader.read(f);
    } catch (DocumentException e) {
      log.error(e);
    }
    return doc;
  }

  /**
   * 读取xml
   * @param absolutePath
   * @return
   */
  public static Document readXml(String absolutePath){
    return readXml(new File(absolutePath));
  }

  /**
   * 创建新标签
   *
   * @param tagName 标签名
   * @param content 标签体
   * @param attrs   属性列表
   * @return XMLObject 新节点对象
   */
  public static XmlObject createNode(String tagName, String path, String content, Map<String, String> attrs) {
    XmlObject newNode = new XmlObject(tagName, path, content, attrs);
    return newNode;
  }

  public static Document createDom4j(XmlObject xmlObject){
    Document newDoc = DocumentHelper.createDocument();

    //检查是否是root节点
    if (!xmlObject.isRootElement()){
      log.warn("当前节点不为root节点，无法生成document对象！");
      return newDoc;
    }

    //
    XmlType xmlType = xmlObject.getXmlType();
    Element root = newDoc.addElement(xmlObject.getTagName());
    //根节点属性赋值
    attributeValue(root,xmlObject.getAttrs());

    //处理子标签
    Map<String, List<XmlObject>> childTags = xmlObject.getChildTags();

    List<XmlObject> in = childTags.get(Constants.IN);
    List<XmlObject> out = childTags.get(Constants.OUT);

    //生成metadata.xml文件,所有节点全部处理
    if (XmlType.METADATA.equals(xmlType)){
      //appendMetaDataChild();

    }else if (XmlType.SERVICE_DEFINITION.equals(xmlType)){

    }




    return newDoc;
  }

  /**
   * 处理子节点
   * @param childTags
   * @param newDoc
   */
  public static void dealChildTags(List<XmlObject> childTags,Document newDoc){
    if (CollUtil.isNotEmpty(childTags)){
      childTags.stream().forEach(childTag -> {
        Element child = (Element) newDoc.selectSingleNode(childTag.getPath());
        if (null == child){

        }
      });
    }else {
      log.warn("子节点为空");
    }


  }

  /**
   * 添加子节点
   * @param node
   * @param childTags
   * @return
   */
  public static void appendMetaDataChild(Document doc,Node node, List<XmlObject> childTags){
    Element element = (Element) node;
    //获取节点名称
    String elementName = element.getName();
    childTags.stream().forEach(child -> {
      //TODO 这里需要用Xpath表达式来处理
      String path = child.getPath();

//      if (Constants.XML_METADATA.equals()){
//
//      }


      element.addComment(child.getPath());
      Element childElement = element.addElement(child.getTagName());
      attributeValue(childElement,child.getAttrs());

      //设置文本
      if (StrUtil.isNotBlank(child.getContent())){
        childElement.setText(child.getContent());
      }
    });

  }

  /**
   * 给节点属性赋值
   * @param node
   * @param attrs
   */
  public static void attributeValue(Node node, Map<String, String> attrs){
    if (MapUtil.isNotEmpty(attrs)) {
      Element element = (Element) node;
      for (Map.Entry<String, String> entry : attrs.entrySet()) {
        element.attributeValue(entry.getKey(), entry.getValue());
      }
    }else {
      log.info("传入的属性为空");
    }
  }



}
