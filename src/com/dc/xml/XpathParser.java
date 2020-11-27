package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.config.HrConfig;
import com.dc.config.HrSystem;
import com.dc.core.MetadataNode;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
   * @param Xpath 解析路径
   * @param tagName 标签名
   * @param path 原路径
   * @return XMLObject 新节点对象
   */
  public static XmlObject createNode(String tagName, String Xpath, String path) {
    XmlObject newNode = new XmlObject(tagName, Xpath, path);
    return newNode;
  }

  /**
   * 批量处理服务信息
   * 1.生成服务对应的document对象 2.输出到本地 。一体化操作。
   * @param metadata
   * @return
   */
  public static boolean ofTransferBatch(XmlMetadata metadata){
    //处理元数据
    if (!ObjectUtil.isNotEmpty(metadata)){
      log.warn("元数据metadata为空，处理失败！");
      return false;
    }

    Set<MetadataNode> metadataNodeList = metadata.getMetadataNodeList();
    if (ObjectUtil.isNotEmpty(metadataNodeList)){
      Document metadataDocument = createMetadataDocument(metadataNodeList);
      //输出到本地文件夹
      transfer(HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/metadata.xml", metadataDocument);
      transfer(HrConfig.getConfig().LOCAL_URL.get(Constants.OUT)+"/metadata.xml", metadataDocument);
    }

    //处理拆组包文件
    List<XmlObject> xmlObjectList = metadata.getXmlObjectList();
    if (CollUtil.isNotEmpty(xmlObjectList)){
      ofTransfer(xmlObjectList);
    }

    //处理系统识别和服务识别信息
    Map<String, List<String>> serviceCodeMapping = metadata.getServiceCodeMapping();
    if (MapUtil.isNotEmpty(serviceCodeMapping)){
      //todo

    }
    return true;
  }

  public static boolean ofTransfer(List<XmlObject> xmlObjectList){
    xmlObjectList.stream().forEach(xmlObject -> {
      ofTransfer(xmlObject,false);
    });
    return true;
  }

  /**
   * 直接将 XmlObject对象转换为文件输出
   * <p>
   *   这个方法无法生成metadata.xml方法,将 metadataFlag 设置为true可以生成
   * </p>
   * @param metadataFlag 是否处理metadata.xml true 处理；false 不处理。目前处理方法还没写好
   * @param xmlObject
   * @return
   */
  public static boolean ofTransfer(XmlObject xmlObject,boolean metadataFlag){
    Constants.XML_TYPE_LIST.stream().forEach(xmlType -> {
      //设置根节点名称
      xmlObject.setTagName(xmlType);
      //根据XmlType生成对应的Xpath
      xmlObject.setChildXpath(xmlType);
      //消费者系统
      HrSystem consumer = xmlObject.getSystem().get(Constants.CONSUMER_CHANNEL);
      //提供者系统
      HrSystem provider = xmlObject.getSystem().get(Constants.PROVIDER_SYSTEM);
      //根据XmlType决定对应的路径 从LOCAL_URL中获取
      String absolutePath = "";
      if (XmlType.METADATA.equals(xmlType)){
        //在 ofTransferBatch 方法中已经处理了，这里不再进行处理
        if (metadataFlag){
          //todo 生成metadata.xml文件
        }else {
          return;
        }
      }else if (XmlType.SERVICE.equals(xmlType)){
        //给 Service 节点设置属性
        xmlObject.setAttrs(Constants.NODE_PACKAGE_TYPE,Constants.NODE_PACKAGE_TYPE_VALUE);
        xmlObject.setAttrs(Constants.NODE_STORE_MODE,Constants.NODE_STORE_MODE_VALUE);

        Document inDoc = createDom4j(Constants.IN, xmlObject);
        //输入字段生成 in端的拆包和out端的组包文件
        List<String> inFilePath = CommonUtil.generateFilePath(Constants.IN,consumer.getCode(),provider.getCode(),xmlObject.getServiceCode());
        transfer(inFilePath,inDoc);

        Document outDoc = createDom4j(Constants.OUT, xmlObject);
        List<String> outFilePath = CommonUtil.generateFilePath(Constants.IN,consumer.getCode(),provider.getCode(),xmlObject.getServiceCode());
        transfer(outFilePath,outDoc);

        //最后将设置的属性清空
        xmlObject.clearAttrs();
      }else if (XmlType.SERVICE_DEFINITION.equals(xmlType)){
        Document allDoc = createDom4j(Constants.ALL, xmlObject);
        List<String> allFilePath = CommonUtil.generateFilePath(Constants.ALL,"","",xmlObject.getServiceCode());
        transfer(allFilePath,allDoc);
      }else {
        log.warn("没有找到对应的XmlType，无法生成对应的文件，请检查 Constants.XML_TYPE_LIST 是否配置正确");
        return;
      }
    });
    return true;
  }

  /**
   * 处理元数据节点
   * @param metadataNodes
   * @return
   */
  public static Document createMetadataDocument(Set<MetadataNode> metadataNodes){
    Document newDoc = DocumentHelper.createDocument();
    Element root = newDoc.addElement(XmlType.METADATA.getRootName());
    //获取节点名称
    metadataNodes.stream().forEach(metadataNode -> {

      if (metadataNode.isCommentFlag()){
        //创建注释节点
        root.addComment(metadataNode.getComment());
      }else {
        //创建子节点
        Element childElement = root.addElement(metadataNode.getNodeName());
        metadataNode.setAttr(childElement);
      }
    });
    return newDoc;
  }

  /**
   * 将XmlObject解析成对应的Document对象
   * @param childType 需要处理的子节点类型:Constants.IN 处理输入字段；Constants.OUT 处理输出字段
   * @param xmlObject
   * @return
   */
  public static Document createDom4j(String childType,XmlObject xmlObject){
    Document newDoc = DocumentHelper.createDocument();

    //检查是否是root节点
    if (!xmlObject.isRootElement()){
      log.warn("当前节点不为root节点，无法生成document对象！");
      return newDoc;
    }

    Element root = newDoc.addElement(xmlObject.getTagName());

    //根节点属性赋值
    attributeValue(root,xmlObject.getAttrs());

    //处理子标签
    Map<String, List<XmlObject>> childTags = xmlObject.getChildTags();
    List<XmlObject> in = childTags.get(Constants.IN);
    List<XmlObject> out = childTags.get(Constants.OUT);


    if (Constants.IN.equals(childType)){
      //处理输入字段
      dealChildTags(in,newDoc);
    }else if (Constants.OUT.equals(childType)){
      //处理输出字段
      dealChildTags(out,newDoc);
    }else {
      dealChildTags(in,newDoc);
      dealChildTags(out,newDoc);
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
        //创建 xpath对应的 节点
        Element parent = createByXPath(newDoc,childTag.getXpath());
        if (null != parent){
          //设置属性
          Element node = parent.addElement(childTag.getTagName());
          attributeValue(node, childTag.getAttrs());
          attributeContent(node, childTag.getContent());
        }
      });
    }
  }

  /**
   * 使用xpath表达式创建节点
   * @param doc
   * @param xpath
   * @return
   */
  public static Element createByXPath(Document doc, String xpath){

    if (StrUtil.isBlank(xpath)){
      log.warn("传入的Xpath为空，无法解析");
    }

    if (xpath.endsWith("/")){
      //去除最后面得/号，防止通过Xpath查询的时候报错
      xpath = xpath.substring(0,xpath.length()-1);
    }

    if (doc.selectSingleNode(xpath) != null) {
      log.warn("重复字段：" + doc.selectSingleNode(xpath).getPath() + " [已忽略]");
      return (Element) doc.selectSingleNode(xpath);

    }

    String path = xpath.substring(0, xpath.lastIndexOf("/"));
    Element e = (Element) doc.selectSingleNode(path);
    if (null == e) {
      e = createByXPath(doc, path);
      if (e.getParent().getName().toLowerCase().equals("array")) {
        e.addAttribute("metadataid", e.getName());
        e.addAttribute("type", "array");
        e.addAttribute("is_struct", "false");
      }
    }
    e = e.addElement(xpath.substring(xpath.lastIndexOf("/") + 1, xpath.length()));
    return e;
  }

  /**
   * 给节点属性赋值
   * @param node
   * @param attrs
   */
  public static void attributeValue(Element node, Map<String, String> attrs){
    if (MapUtil.isNotEmpty(attrs)) {
      for (Map.Entry<String, String> entry : attrs.entrySet()) {
        node.addAttribute(entry.getKey(), entry.getValue());
      }
    }else {
      log.info("节点："+node.getName()+"传入的属性为空");
    }
  }

  /**
   * 给节点设置文本值
   * @param node 节点
   * @param content 文本值
   */
  public static void attributeContent(Node node,String content){
    if (ObjectUtil.isNotEmpty(node) && StrUtil.isNotBlank(content)){
      node.setText(content);
    }
  }

  /**
   * 转换为多个文件
   * @param absolutePath
   * @param doc
   * @return
   */
  public static boolean transfer(List<String> absolutePath,Document doc){
    if (CollUtil.isNotEmpty(absolutePath)){
      absolutePath.stream().forEach(path -> {
        transfer(path,doc);
      });
    }
    return true;
  }

  /**
   * 转换为文件
   * @param doc 文档对象
   * @param absolutePath 输出文件的绝对路径
   * @return boolean true-转换成功, false-转换失败
   */
  public static boolean transfer(String absolutePath,Document doc) {
    // 后缀检测
    if (!absolutePath.endsWith(".xml")) {
      log.debug("输出文件:"+absolutePath+" 不是.xml文件");
      return false;
    }

    File outputFile = createFile(absolutePath);
    // 类型检测
    if (!outputFile.isFile()) {
      log.debug(outputFile.getAbsolutePath()+" 不是文件类型");
      return false;
    }

    OutputFormat of = OutputFormat.createPrettyPrint();
    of.setNewLineAfterDeclaration(false);
    of.setIndent(true);
    of.setIndentSize(4);
    of.setEncoding("UTF-8");
    of.setLineSeparator("\n");

    // 将格式化内容写入文件
    try {
      OutputStream os = new FileOutputStream(outputFile);
      XMLWriter writer = new XMLWriter(os, of);
      writer.write(doc);
      writer.flush();
      writer.close();
      log.info("生成文件：" + outputFile.getAbsolutePath());
      return true;
    } catch (FileNotFoundException e) {
      log.error(e);
    } catch (UnsupportedEncodingException e) {
      log.error(e);
    } catch (IOException e) {
      log.error(e);
    }
    return false;
  }

  /**
   * 创建文件, 已存在就删除
   * @param absolutePath 文件绝对路径
   * @return 创建成功返回true, 否则返回false
   */
  public static File createFile(String absolutePath) {
    File file = new File(absolutePath);
    try {
      createFileParentDir(file);
      if (file.exists() && file.delete()){
        log.debug(file.getName()+"已存在,进行删除操作");
      }
      file.createNewFile();
    } catch (IOException e) {
      log.error("创建文件时发生错误",e);
    }
    return file;
  }

  /**
   * 创建文件所在目录
   *
   * @param file 文件对象
   * @return 创建成功返回true, 否则返回false
   */
  public static boolean createFileParentDir(File file) {
    File dir = file.getParentFile();
    if (!dir.exists()){
      return dir.mkdirs();
    }
    return false;
  }

}
