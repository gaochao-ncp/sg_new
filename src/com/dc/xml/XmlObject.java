package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.Constants;
import com.dc.config.HrConfig;
import com.dc.config.HrSystem;
import com.dc.excel.ExcelIndex;
import com.dc.excel.ExcelRow;
import com.dc.excel.ExcelSheet;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.*;

/**
 * Xml文件对象
 * @author: Administrator
 * @date: 2020-11-23 23:55
 * @version: 1.0
 */
public class XmlObject {

  private static final Log log = LogFactory.get(XmlObject.class);
  /**
   * 属性列表
   */
  private Map<String, String> attrs;

  /**
   * 子标签集合:包含输入输出字段。 key：in|out ==>Constants.IN Constants.OUT
   */
  private Map<String, List<XmlObject>> childTags = new HashMap<>();

  /**
   * 标签文本内容
   */
  private String content;

  /**
   * 是否根节点
   */
  private boolean rootElement = false;

  /**
   * 是否是注释节点.默认false
   * //todo List<XmlComment> 这种数据格式待考证
   */
  private boolean commentFlag = false;

  /**
   * 自定义注释内容，默认的话 是当前时间+服务码
   */
  private String comment;

  /**
   * 标签名
   */
  private String tagName;

  /**
   * 原来本身的路径也要保存，不能忘本！
   */
  private String path;

  /**
   * 绝对路径，Xpath操作的时候需要用到.root节点不需要
   */
  private String Xpath;

  /**
   * 服务编码，生成文件名
   */
  private String serviceCode;

  /**
   * 系统渠道信息:接入系统和消费系统信息
   */
  private Map<String,HrSystem> system;

  /**
   * 根据这个类型生成对应的Xpath路径。必须存在该值，不然程序报错停止
   * 应用位置为：
   * {@link XpathParser#ofTransfer(XmlObject)}
   */
  private XmlType xmlType;

  public XmlObject(){}

  /**
   * 构建XML对象
   *
   * @param tagName 标签名
   */
  public XmlObject(String tagName) {
    super();
    this.tagName = tagName;
  }

  /**
   * 构建XML对象
   * @param Xpath xpath对应的节点路径
   * @param tagName 标签名
   */
  public XmlObject(String tagName,String Xpath) {
    super();
    this.tagName = tagName;
    this.Xpath = Xpath;
  }

  /**
   * 构建XML对象
   * @param Xpath xpath对应的节点路径
   * @param tagName 标签名
   * @param path 原路径
   */
  public XmlObject(String tagName, String Xpath,String path,String serviceCode) {
    super();
    this.tagName = tagName;
    if (StrUtil.isNotBlank(Xpath)){
      Xpath = Xpath.replaceAll("/+","/");
    }
    this.Xpath = Xpath;
    if (StrUtil.isNotBlank(path)){
      path = path.replaceAll("/+","/");
    }
    this.path = path;
    this.serviceCode = serviceCode;
  }

  /**
   * 批量解析Excel表格信息
   * @param sheets
   * @return
   */
  public static XmlMetadata ofBatch(List<ExcelSheet> sheets){
    XmlMetadata metadata = new XmlMetadata();
    //添加注释
    metadata.addCommentNodeStart();
    for (ExcelSheet sheet : sheets) {
      XmlObject xmlObject = of(sheet);
      metadata.parseXmlObject(xmlObject);
    }
    //添加注释
    metadata.addCommentNodeEnd();
    return metadata;
  }

  /**
   * 批量解析Excel表格信息
   * @param sheets
   * @return
   */
  public static XmlMetadata ofBatch(ExcelSheet... sheets){
    List<ExcelSheet> params = new ArrayList<>();
    for (ExcelSheet sheet : sheets) {
      params.add(sheet);
    }
    return ofBatch(params);
  }

  /**
   * 解析单个Excel转换成对应需要转换的xml类型
   * @param sheet
   * @return
   */
  public static XmlObject of(ExcelSheet sheet){
    XmlObject result = new XmlObject();

    if (ObjectUtil.isNull(sheet)){
      return result;
    }

    String serviceCode = sheet.getServiceCode(sheet);
    result.setServiceCode(serviceCode);
    //处理输入字段
    result.setChildTags(sheet.getInRows(),Constants.IN);

    //处理输出字段
    result.setChildTags(sheet.getOutRows(),Constants.OUT);

    //解析公共部分
    ExcelSheet appHead = sheet.getAppHead();
    if (appHead.isParseFlag()){
      result.setChildTags(appHead.getOutRows(),Constants.OUT);
      result.setChildTags(appHead.getInRows(),Constants.IN);
    }

    ExcelSheet sysHead = sheet.getSysHead();
    if (sysHead.isParseFlag()){
      result.setChildTags(sysHead.getOutRows(),Constants.OUT);
      result.setChildTags(sysHead.getInRows(),Constants.IN);
    }

    ExcelSheet common = sheet.getCommon();
    if (common.isParseFlag()){
      result.setChildTags(common.getOutRows(),Constants.OUT);
      result.setChildTags(common.getInRows(),Constants.IN);
    }


    //解析索引部分:用于生成服务识别和系统识别文件
    ExcelIndex index = sheet.getIndex();
    result.setSystem(index);

    //根节点
    result.setRootElement(true);
    return result;
  }

  /**
   * 解析 索引页中的系统code和名称,系统渠道信息
   * @param index
   */
  private void setSystem(ExcelIndex index){
    if (ObjectUtil.isNotEmpty(index)){
      Set<Map.Entry<String, HrSystem>> entries = HrConfig.getConfig().systemMappings.entrySet();
      for (Map.Entry<String, HrSystem> entry : entries) {
        HrSystem system = entry.getValue();
        if (system.getValue().equals(index.getConsumerName())){
          getSystem().put(Constants.CONSUMER_CHANNEL,system);
        }
        if (system.getValue().equals(index.getProviderName())){
          getSystem().put(Constants.PROVIDER_SYSTEM,system);
        }
      }

      if (!ObjectUtil.isNotEmpty(getSystem().get(Constants.CONSUMER_CHANNEL))){
        log.warn("c端渠道：【"+index.getConsumerName()+"】无法找到对应的信息，请检查配置");
      }
      if (!ObjectUtil.isNotEmpty(getSystem().get(Constants.PROVIDER_SYSTEM))){
        log.warn("P端渠道：【"+index.getProviderName()+"】无法找到对应的信息，请检查配置");
      }
    }
  }

  /**
   * 处理子节点信息:
   *  1.生成节点的xpath信息；2.给节点设置属性。serviceCode必须设置
   * @param rows
   * @param nodeType in节点；out节点
   */
  private void setChildTags(List<ExcelRow> rows,String nodeType){
    if (CollUtil.isEmpty(rows)){
      return;
    }
    List<XmlObject> nodeLis = new ArrayList<>();
    rows.stream().forEach(row -> {
      //已删除节点不进行处理
      if (row.isDeleteFlag()){
        return;
      }
      //String xpath = getXpath(row.getPath(),nodeType);//此处不再生成Xpath节点信息，因为没办法针对不同类型的文件。xpath setChildXpath 方法进行赋值
      //创建新节点加入:
      XmlObject node;
      if (Constants.XML_ARRAY.equals(row.getType())){
        //type节点做特殊处理 此处Xpath的值为：xpath+"array"，因为赋值地方更改，所以将array赋值给path
        node = XpathParser.createNode(row.getTagName(), null, row.getPath()+"/array/",this.serviceCode);//
        node.setAttrs("type","array");
        node.setAttrs("is_struct","false");
      }else {
        //这里的this.serviceCode是父节点的serviceCode
        node = XpathParser.createNode(row.getTagName(), null, row.getPath(),this.serviceCode);
        node.setAttrs(row);
      }
      nodeLis.add(node);
    });

    if (CollUtil.isNotEmpty(nodeLis)){
      List<XmlObject> xmlObjects = getChildTags().get(nodeType);
      if (CollUtil.isEmpty(xmlObjects)){
        getChildTags().put(nodeType,nodeLis);
      }else {
        xmlObjects.addAll(nodeLis);
      }
    }
  }

  /**
   * 给输入输出子节点设置Xpath的值
   * @param xmlType
   */
  public void setChildXpath(XmlType xmlType) {
    Map<String, List<XmlObject>> childTags = getChildTags();
    if (MapUtil.isNotEmpty(childTags)){
      List<XmlObject> ins = childTags.get(Constants.IN);
      List<XmlObject> outs = childTags.get(Constants.OUT);
      if (CollUtil.isNotEmpty(ins)){
        ins.stream().forEach(in -> {
          String inXpath = in.getXpath(xmlType, Constants.IN);
          in.setXpath(inXpath);
        });
      }
      if (CollUtil.isNotEmpty(outs)){
        outs.stream().forEach(out -> {
          String outXpath = out.getXpath(xmlType, Constants.OUT);
          out.setXpath(outXpath);
        });
      }
    }
  }

  /**
   * 处理路径信息，将路径信息转换为Xpath所需要的格式
   * @param nodeType
   * @return
   */
  public String getXpath(String nodeType){
    return getXpath(this.xmlType,nodeType);
  }

  /**
   * 处理路径信息，将路径信息转换为Xpath所需要的格式
   * @param xmlType
   * @param nodeType 输入，输出字段决定服务定义文件中的xml报文节点组成。in->request;out->response
   * @return
   */
  public String getXpath(XmlType xmlType,String nodeType){
    String Xpath = "";
    if (XmlType.METADATA.equals(xmlType)){
      Xpath = "/metadata";
    }else if (XmlType.SERVICE.equals(xmlType)) {
      Xpath = "/"+this.path;
    }else if (XmlType.SYSTEM_IDENTIFY.equals(xmlType)){
      //系统识别不在这里处理
      return Xpath;
    }else if (XmlType.SERVICE_IDENTIFY.equals(xmlType)){
      //系统识别不在这里处理
      return Xpath;
    }else if (XmlType.SERVICE_DEFINITION.equals(xmlType)){
      // /S0200200000510/request/sdoroot  /S0200200000510/response/sdoroot
      Xpath = path.replace("service","/S"+this.serviceCode+"/"+nodeType+"/sdoroot");
      //当检测到 */array/* 时，需要在路径后加一个 sdo节点
      if (verifyArray(Xpath)){
        Xpath += "/sdo";
      }
    }
    return Xpath.replaceAll("/+","/");
  }

  /**
   * 根据/对路径进行切割
   * 如果检测路径倒数第二个为array时进行处理
   * 例如：service/APP_HEAD/array/AuthTellerInfo/->处理后变为 service/APP_HEAD/array/AuthTellerInfo/sdo
   * @param path
   * @return
   */
  public boolean verifyArray(String path){
    String[] split = path.split("\\/");
    if (Constants.XML_ARRAY.equals(split[split.length-2])){
      return true;
    }
    return false;
  }

  /**
   * 添加属性
   * @param key
   * @param value
   */
  public void setAttrs(String key ,String value){
    getAttrs().put(key,value);
  }

  /**
   * 清空对应的属性
   */
  public void clearAttrs(){
    Map<String, String> attrs = getAttrs();
    if (MapUtil.isNotEmpty(attrs)){
      attrs.clear();
    }
  }

  /**
   * 添加属性
   * @param row
   */
  private void setAttrs(ExcelRow row){
    if (StrUtil.isNotBlank(row.getType())){
      getAttrs().put(Constants.NODE_TYPE,row.getType());
    }

    if (StrUtil.isNotBlank(row.getLength())){
      getAttrs().put(Constants.NODE_LENGTH,row.getLength());
    }

    if (StrUtil.isNotBlank(row.getScale())){
      getAttrs().put(Constants.NODE_SCALE,row.getScale());
    }

    if (StrUtil.isNotBlank(row.getChineseName())){
      getAttrs().put(Constants.NODE_CHINESE_NAME,row.getChineseName());
    }

    if (StrUtil.isNotBlank(row.getMetadataId())){
      getAttrs().put(Constants.NODE_METADATA_ID,row.getMetadataId());
    }

    if (StrUtil.isNotBlank(row.getIsStruct())){
      getAttrs().put(Constants.NODE_IS_STRUCT,row.getIsStruct());
    }

    if (StrUtil.isNotBlank(row.getMode())){
      getAttrs().put(Constants.NODE_MODE,row.getMode());
    }

    if (StrUtil.isNotBlank(row.getExpression())){
      getAttrs().put(Constants.NODE_EXPRESSION,row.getExpression());
    }

    if (StrUtil.isNotBlank(row.getId())){
      getAttrs().put(Constants.NODE_ID,row.getId());
    }

    if (StrUtil.isNotBlank(row.getEncode())){
      getAttrs().put(Constants.NODE_ENCODE,row.getEncode());
    }

    if (StrUtil.isNotBlank(row.getValue())){
      getAttrs().put(Constants.NODE_VALUE,row.getValue());
    }
  }

  /**
   * 处理子节点生成文档对象信息
   * @param nodeType in->request;out->response
   * @param childTags
   * @param newDoc
   */
  public void dealChildTags(List<XmlObject> childTags, Document newDoc, String nodeType){
    if (CollUtil.isNotEmpty(childTags)){
      //获取当前xmlType类型
      XmlType xmlType = getXmlType();
      childTags.stream().forEach(childTag -> {
        //创建子节点的xpath对应的父节点
        String xpath = childTag.getXpath(xmlType,nodeType);
        Element parent = createByXPath(newDoc,xpath);
        if (ObjectUtil.isNotNull(parent)){
          //设置属性
          Element node = parent.addElement(childTag.getTagName());
          node.addAttribute(Constants.NODE_METADATA_ID,childTag.getTagName());
          String chinese_name = childTag.getAttrs().get(Constants.NODE_CHINESE_NAME);
          if (StrUtil.isNotBlank(chinese_name)){
            node.addAttribute(Constants.NODE_CHINESE_NAME,chinese_name);
          }
          childTag.attributeContent(node);
        }
      });
    }
  }

  /**
   * 给Element节点设置文本值
   * @param node
   */
  public void attributeContent(Node node){
    attributeContent(node,getContent());
  }

  /**
   * 给Element节点设置文本值
   * @param node 节点
   * @param content 文本值
   */
  public void attributeContent(Node node, String content){
    if (ObjectUtil.isNotEmpty(node) && StrUtil.isNotBlank(content)){
      node.setText(content);
    }
  }

  /**
   * 给Element节点属性赋值
   * @param node 元素节点
   */
  public void attributeValue(Element node){
    Map<String, String> attrs = getAttrs();
    if (MapUtil.isNotEmpty(attrs)) {
      for (Map.Entry<String, String> entry : attrs.entrySet()) {
        node.addAttribute(entry.getKey(), entry.getValue());
      }
    }else {
      log.info("节点【"+node.getName()+"】传入的属性为空");
    }
  }

  /**
   * 使用xpath表达式创建xml中的父节点
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
      if (Constants.XML_ARRAY.equals(e.getParent().getName().toLowerCase())) {
        e.addAttribute("metadataid", e.getName());
        e.addAttribute("type", "array");
        e.addAttribute("is_struct", "false");
      }
    }
    e = e.addElement(xpath.substring(xpath.lastIndexOf("/") + 1, xpath.length()));
    return e;
  }

  public Map<String, String> getAttrs() {
    if (this.attrs == null){
      this.attrs = new LinkedHashMap<>();
    }
    return attrs;
  }

  public void setAttrs(Map<String, String> attrs) {
    this.attrs = attrs;
  }

  public Map<String, List<XmlObject>> getChildTags() {
    if (this.childTags == null){
      this.childTags = new HashMap<>();
    }
    return childTags;
  }

  public void setChildTags(Map<String, List<XmlObject>> childTags) {
    this.childTags = childTags;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public boolean isRootElement() {
    return rootElement;
  }

  public void setRootElement(boolean rootElement) {
    this.rootElement = rootElement;
  }

  public boolean isCommentFlag() {
    return commentFlag;
  }

  public void setCommentFlag(boolean commentFlag) {
    this.commentFlag = commentFlag;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public String getTagName() {
    return tagName;
  }

  /**
   * 设置节点名称
   * @param tagName
   */
  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  /**
   * 根据自身生成的文件类型来确定根节点名称
   */
  public void setTagName(XmlType xmlType){
    String tagName = xmlType.getRootName();
    if (XmlType.SERVICE_DEFINITION.equals(xmlType)){
      tagName += getServiceCode();
    }
    setTagName(tagName);
  }

  public String getXpath() {
    return Xpath;
  }

  public void setXpath(String xpath) {
    this.Xpath = xpath;
  }

  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public Map<String, HrSystem> getSystem() {
    if (MapUtil.isEmpty(system)){
      this.system = new HashMap<>(2);
    }
    return system;
  }

  public void setSystem(Map<String, HrSystem> system) {
    this.system = system;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public XmlType getXmlType() {
    return xmlType;
  }

  public void setXmlType(XmlType xmlType) {
    this.xmlType = xmlType;
  }
}
