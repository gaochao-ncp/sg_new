package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;

import com.dc.common.Constants;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.*;
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
   * @param Xpath 解析路径
   * @param tagName 标签名
   * @param content 标签体
   * @param attrs   属性列表
   * @return XMLObject 新节点对象
   */
  public static XmlObject createNode(String tagName, String Xpath, String content, Map<String, String> attrs) {
    XmlObject newNode = new XmlObject(tagName, Xpath, content, attrs);
    return newNode;
  }

  /**
   * 将XmlObject解析成对应的Document对象
   * @param xmlObject
   * @return
   */
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

    dealChildTags(in,newDoc);
    dealChildTags(out,newDoc);

    //处理注释节点
    List<XmlContext> contexts = xmlObject.getContexts();
    attributeContent(newDoc,contexts);

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
    String path1 = xpath.substring(0, xpath.lastIndexOf("/"));
    Element e = (Element) doc.selectSingleNode(path1);
    if (null == e) {
      e = createByXPath(doc, path1);
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
   * 给节点设置属性值
   * @param doc
   * @param contexts
   */
  public static void attributeContent(Document doc,List<XmlContext> contexts){
    if (CollUtil.isNotEmpty(contexts)){
      contexts.stream().forEach(xmlContext -> {
        //查找开始节点信息
        Element startNode = (Element)doc.selectSingleNode(xmlContext.getStartContext());
        attributeContent(startNode, xmlContext.getStartContext());
        //查找结束节点信息
        Element endNode = (Element)doc.selectSingleNode(xmlContext.getEndXpath());
        attributeContent(endNode, xmlContext.getEndContext());
      });
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
      log.info("生成文件：" + outputFile.getName());
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

  public static void main(String[] args) {
    Document document = DocumentHelper.createDocument();

    //root
    Element element = document.addElement("metadata");

    String xpath = "/metadata/ServiceCode[@type='string' and @length='15' and @chinese_name='服务代码']/";
    Element element1 = createByXPath(document,xpath);


    transfer("E:/metadata.xml",document);


  }

}
