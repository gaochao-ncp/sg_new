package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.config.HrConfig;
import com.dc.config.HrSystem;
import com.dc.core.MetadataNode;
import com.dc.xml.post.XmlPost;
import com.dc.xml.post.XmlPostSmrtlr;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Xml解析器
 * @author: Administrator
 * @date: 2020-11-23 23:58
 * @version: 1.0
 */
public class XpathParser {

  private static final Log log = LogFactory.get(XpathParser.class);

  /**
   * 存放文件地址和文档的映射关系
   */
  public static final Map<String,Document> DOCUMENT_MAP = new HashMap<>();

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
  public static XmlObject createNode(String tagName, String Xpath, String path, String serviceCode) {
    XmlObject newNode = new XmlObject(tagName, Xpath, path, serviceCode);
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
      //放入文档容器
      DOCUMENT_MAP.put(HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/metadata.xml", metadataDocument);
      DOCUMENT_MAP.put(HrConfig.getConfig().LOCAL_URL.get(Constants.OUT)+"/metadata.xml", metadataDocument);
    }

    //处理拆组包文件
    List<XmlObject> xmlObjectList = metadata.getXmlObjectList();
    if (CollUtil.isNotEmpty(xmlObjectList)){
      ofTransferUnpack(xmlObjectList);
    }

    //处理系统识别和服务识别信息
    Map<HrSystem, List<String>> serviceCodeMapping = metadata.getServiceCodeMapping();
    if (MapUtil.isNotEmpty(serviceCodeMapping)){
      ofTransferUnpackIdentify(serviceCodeMapping);
    }

    //将文档容器内的文档对象进行输出
    transfer();
    return true;
  }

  /**
   * 批量处理：将所有拆组包XmlObject对象转换为文件输出
   * @param xmlObjectList
   * @return
   */
  public static boolean ofTransferUnpack(List<XmlObject> xmlObjectList){
    xmlObjectList.stream().forEach(xmlObject -> {
      ofTransferUnpack(xmlObject);
    });
    return true;
  }

  /**
   * 直接将拆组包XmlObject对象转换为文件输出
   * <p>
   *   这个方法无法生成metadata.xml方法,将 metadataFlag 设置为true可以生成
   * </p>
   * @param xmlObject
   * @return
   */
  public static boolean ofTransferUnpack(XmlObject xmlObject){

    //跳过注释节点
    if (xmlObject.isCommentFlag()){
      return true;
    }

    Constants.XML_TYPE_LIST.stream().forEach(xmlType -> {
      //设置根节点名称
      xmlObject.setTagName(xmlType);
      //根据XmlType生成对应的Xpath
      xmlObject.setXmlType(xmlType);

      //根据XmlType决定对应的路径 从LOCAL_URL中获取
      if (XmlType.SERVICE.equals(xmlType)){
        //给 Service 节点设置属性
        xmlObject.setAttrs(Constants.NODE_PACKAGE_TYPE,Constants.NODE_PACKAGE_TYPE_VALUE);
        xmlObject.setAttrs(Constants.NODE_STORE_MODE,Constants.NODE_STORE_MODE_VALUE);

        //输入字段生成 in端的拆包和out端的组包文件
        Document inDoc = createUnpackDocument(Constants.IN, xmlObject);
        //输出字段生成 out端的拆包和in端的组包
        Document outDoc = createUnpackDocument(Constants.OUT, xmlObject);

        //最后将设置的属性清空
        xmlObject.clearAttrs();
      }else if (XmlType.SERVICE_DEFINITION.equals(xmlType)){
        Document allDoc = createUnpackDocument(Constants.ALL, xmlObject);
      }else {
        log.warn("没有找到对应的XmlType，无法生成对应的文件，请检查 Constants.XML_TYPE_LIST 是否配置正确");
        return;
      }
    });

    try {
      //拓展接口,实现一些特定的需求
      XmlPost xmlPost = new XmlPostSmrtlr();
      xmlPost.invoke(xmlObject);
    }catch (Exception e){
      e.printStackTrace();
    }


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
   * 将XmlObject解析成对应的拆组包Document对象,并且生成xml文件
   * @param childType
   * @param xmlObject
   * @return
   */
  public static Document createUnpackDocument(String childType,XmlObject xmlObject){
    Document newDoc = CommonUtil.createDocument(xmlObject);
    //检查是否是root节点
    if (!xmlObject.isRootElement()){
      log.warn("当前节点不为root节点，无法生成document对象！");
      return newDoc;
    }
    //处理子标签
    Map<String, List<XmlObject>> childTags = xmlObject.getChildTags();
    List<XmlObject> in = childTags.get(Constants.IN);
    List<XmlObject> out = childTags.get(Constants.OUT);

    XmlType xmlType = xmlObject.getXmlType();
    if (XmlType.SERVICE.equals(xmlType) && Constants.IN.equals(childType)){
      //处理输入字段,生成拆组包文件
      xmlObject.dealChildTags(in,newDoc,"");
    }else if (XmlType.SERVICE.equals(xmlType) && Constants.OUT.equals(childType)){
      //处理输出字段,生成拆文件组包
      xmlObject.dealChildTags(out,newDoc,"");
    }else if (XmlType.SERVICE_DEFINITION.equals(xmlType) && Constants.ALL.equals(childType)){
      //生成服务定义文件
      xmlObject.dealChildTags(in,newDoc,"request");
      xmlObject.dealChildTags(out,newDoc,"response");
    }

    //消费者系统
    HrSystem consumer = xmlObject.getSystem().get(Constants.CONSUMER_CHANNEL);
    //提供者系统
    HrSystem provider = xmlObject.getSystem().get(Constants.PROVIDER_SYSTEM);
    //获取文件路径
    List<String> filePath = CommonUtil.generateFilePath(childType,
            ObjectUtil.isNotNull(consumer)?consumer.getCode():"404",
            ObjectUtil.isNotNull(provider)?provider.getCode():"404",
            xmlObject.getServiceCode());
    //将拆组包文件放入到容器中
    if (CollUtil.isNotEmpty(filePath)){
      filePath.stream().forEach(path -> {
        DOCUMENT_MAP.put(path,newDoc);
      });
    }

    //统一图形前端需要做特殊处理
    if ("SMRTLR".equals(consumer.getCode())){

    }
    return newDoc;
  }

  /**
   * 生成服务识别和系统识别的xml文件
   * @param serviceCodeMapping
   * @return
   */
  public static boolean ofTransferUnpackIdentify(Map<HrSystem, List<String>> serviceCodeMapping){
    serviceCodeMapping.entrySet().forEach(entry -> {
      //系统信息
      HrSystem hrSystem = entry.getKey();
      //服务码列表
      List<String> value = entry.getValue();

      Document systemIdentifyDocument = createIdentifyDocument(XmlType.SYSTEM_IDENTIFY,hrSystem, value);
      DOCUMENT_MAP.put(HrConfig.getConfig().LOCAL_URL.get(Constants.OUT_SYSTEM_IDENTIFY)+"/"+hrSystem.getCode()+"Out.xml",systemIdentifyDocument);

      Document serviceIdentifyDocument = createIdentifyDocument(XmlType.SERVICE_IDENTIFY,hrSystem, value);
      DOCUMENT_MAP.put(HrConfig.getConfig().LOCAL_URL.get(Constants.IN_SERVICE_IDENTIFY)+"/"+hrSystem.getCode()+"In.xml",serviceIdentifyDocument);
    });
    return true;
  }

  /**
   * 生成系统识别和服务识别xml文件
   * @param xmlType 生成文件类型：只能是 {@link XmlType#SERVICE_IDENTIFY} 和 {@link XmlType#SYSTEM_IDENTIFY}
   * @param hrSystem 系统信息
   * @param serviceCodes 服务码列表
   * @return
   */
  public static Document createIdentifyDocument(XmlType xmlType,HrSystem hrSystem,List<String> serviceCodes){
    Document newDoc = DocumentHelper.createDocument();
    String rootName = "";
    String XpathSuffix = "";
    String nodeName = "";
    if (XmlType.SERVICE_IDENTIFY.equals(xmlType) || XmlType.SYSTEM_IDENTIFY.equals(xmlType)){
      rootName = XmlType.SERVICE_IDENTIFY.equals(xmlType)?Constants.NODE_CHANNELS:Constants.NODE_SYSTEMS;
      XpathSuffix = XmlType.SERVICE_IDENTIFY.equals(xmlType)?"/case/text()":"/service/text()";
      nodeName = XmlType.SERVICE_IDENTIFY.equals(xmlType)?Constants.NODE_CHANNEL:Constants.NODE_SYSTEM;
    }else {
      log.warn("传入的XmlType不符合规范，无法生成识别文件！");
      return newDoc;
    }

    Element root = newDoc.addElement(rootName);
    root.addComment(hrSystem.getValue());

    XmlObject xmlObject = new XmlObject();
    //生成
    String Xpath = xmlObject.getXpath(xmlType, hrSystem.getCode());
    //创建父节点
    Element parent = XmlObject.createByXPath(newDoc, Xpath);
    //开始设置文本service子节点
    if (ObjectUtil.isNotNull(parent)){
      //获取所有service文本节点
      List<Node> serviceNodes = newDoc.selectNodes(CommonUtil.standardXpath(Xpath + XpathSuffix));
      if (CollUtil.isNotEmpty(serviceCodes)){
        List<String> serviceNodesText = serviceNodes.stream().map(o -> o.getText()).collect(Collectors.toList());
        //过滤已经存在的服务后新增节点
        serviceCodes = serviceCodes.stream().filter(code -> !serviceNodesText.contains(code)).collect(Collectors.toList());
      }
      createIdentifyDocument(parent,nodeName,serviceCodes);
    }
    return newDoc;
  }

  /**
   * 生成系统识别文件
   * @param node
   * @param nodeName 节点名称
   * @param serviceCodes
   */
  public static void createIdentifyDocument(Element node,String nodeName,List<String> serviceCodes){
    if (CollUtil.isNotEmpty(serviceCodes)){
      serviceCodes.stream().forEach(serviceCode -> {
        Element element = node.addElement(nodeName);
        element.setText(serviceCode);
      });
    }
  }


  /**
   * 将缓存容器中的对象转换为文件
   * @return
   */
  public static boolean transfer(){
    if (MapUtil.isNotEmpty(DOCUMENT_MAP)){
      for (Map.Entry<String, Document> entry : DOCUMENT_MAP.entrySet()) {
        transfer(entry.getKey(),entry.getValue());
      }
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
    //非空检测
    if (ObjectUtil.isNull(doc)){
      log.info("传入文件对象为空");
      return false;
    }

    // 后缀检测
    if (!absolutePath.endsWith(".xml")) {
      log.info("输出文件:"+absolutePath+" 不是.xml文件");
      return false;
    }

    File outputFile = createFile(absolutePath);
    // 类型检测
    if (!outputFile.isFile()) {
      log.info(outputFile.getAbsolutePath()+" 不是文件类型");
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
      log.info("生成文件【{}】" ,outputFile.getAbsolutePath());
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
        log.info(file.getName()+"已存在,进行删除操作");
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
