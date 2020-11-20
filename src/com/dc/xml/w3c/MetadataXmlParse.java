package com.dc.xml.w3c;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.Constants;
import com.dc.config.SgConfig;
import com.dc.unpack.UnpackingRoot;
import com.dc.unpack.metadata.MetadataNode;
import org.w3c.dom.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * metadata.xml文件解析和生成器
 * @author: Administrator
 * @date: 2020-11-20 15:50
 * @version: 1.0
 */
public class MetadataXmlParse extends XmlParse {

  private static final Log log = LogFactory.get(MetadataXmlParse.class);

  @Override
  public Document doCreateUnpackingXmlDynamic(String rootElementName, UnpackingRoot unpacking) {
    if (null == unpacking || CollUtil.isEmpty(unpacking.getChildList())){
      log.warn("MetadataXmlParse 解析子节点内容为空,请排查原因!");
      return null;
    }

    if (StrUtil.isBlank(rootElementName)){
      // 固定值为 metadata
      rootElementName = unpacking.getRootName();
    }
    Document unpackingDoc = XmlUtil.createXml(rootElementName);
    Element root = XmlUtil.getRootElement(unpackingDoc);
    appendChild(root, unpacking.getChildList());
    return unpackingDoc;
  }

  @Override
  public void appendXml(boolean flag) {

  }

  /**
   * 对比in端metadata和out端metadata文件的不同
   * @param inPath in端路径
   * @param outPath out端路径
   */
  public void parseMetadataXml(String inPath,String outPath){

    if (inPath.contains(SgConfig.getConfig().getLocalInHome())){
      log.info("in端路径:"+inPath);
    }

    if (outPath.contains(SgConfig.getConfig().getLocalOutHome())){
      log.info("out端路径:"+outPath);
    }

    Map<String, MetadataNode> inMetadataMap = parseMetadataXml(inPath);
    Map<String, MetadataNode> outMetadataMap = parseMetadataXml(outPath);

    //保存in端和out端的所有子节点
    Set<String> inNodeName = new HashSet<>();
    Set<String> outNodeName = new HashSet<>();
    inMetadataMap.forEach((k1,v1)->{
      inNodeName.add(k1);
    });

    outMetadataMap.forEach((k2,v2)->{
      outNodeName.add(k2);
    });

    MetadataNode metadataNode = new MetadataNode();
    //进行对比:1.对比都存在的节点信息;2.对比不存在的节点信息
    Set<String> common = inNodeName.stream().filter(in -> outNodeName.contains(in)).collect(Collectors.toSet());
    Set<String> uncommon = inNodeName.stream().filter(in -> !outNodeName.contains(in)).collect(Collectors.toSet());

    if (CollUtil.isNotEmpty(common)){
      common.stream().forEach(nodeName -> {
        inMetadataMap.get(nodeName).parseNode(nodeName,outMetadataMap.get(nodeName));
      });
      //TODO 生成对比metadata-common.xml

    }

    if (CollUtil.isNotEmpty(common)){
      if (inMetadataMap.size()>outMetadataMap.size()){
        log.warn("out端metadata.xml缺少如下字段:"+ Arrays.toString(uncommon.toArray()));
      }else {
        log.warn("in端metadata.xml缺少如下字段:"+Arrays.toString(uncommon.toArray()));
      }
    }

    //todo 将excel中sys_head、app_head、local_head、business_service的字段与metadata.xml中的进行比较

  }

  /**
   * 解析对应路径的xml文档,返回键值对的关系
   * @param path
   * @return
   */
  public Map<String,MetadataNode> parseMetadataXml(String path){
    Map<String,MetadataNode> mapping = new HashMap<>(1024);
    //"/config/metadata.xml"  转换成绝对路径
    Document document = XmlUtil.readXML(System.getProperty("user.dir")+path);
    //保存服务器上的metadata.xml文件,新生成metadata.xml文件的时候追加
    Element rootElement = XmlUtil.getRootElement(document);
    NodeList childNodes = rootElement.getChildNodes();
    for (int i = 0; i < childNodes.getLength(); i++) {
      Node node = childNodes.item(i);
      String nodeName = node.getNodeName();
      //过滤无用节点
      if (!("#text".equals(nodeName) || "#comment".equals(nodeName))){
        MetadataNode value = new MetadataNode();
        NamedNodeMap attributes = node.getAttributes();
        Node type = attributes.getNamedItem(Constants.NODE_TYPE);
        if (null != type){
          value.setType(type.getNodeValue());
        }

        Node length = attributes.getNamedItem(Constants.NODE_LENGTH);
        if (null != length){
          value.setLength(length.getNodeValue());
        }

        Node chineseName = attributes.getNamedItem(Constants.NODE_CHINESE_NAME);
        if (null != chineseName){
          value.setChineseName(chineseName.getNodeValue());
        }

        Node scale = attributes.getNamedItem(Constants.NODE_SCALE);
        if (null != scale){
          value.setScale(scale.getNodeValue());
        }
        mapping.put(nodeName,value);
      }
    }
    return mapping;
  }

  /**
   * TODO 获得服务器上的metadata.xml文件信息
   * @return
   */
  public Document getMetadataXmlServer(){
    return XmlUtil.createXml();
  }
}
