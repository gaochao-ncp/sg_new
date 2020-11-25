package com.dc.xml;

import cn.hutool.core.collection.CollUtil;
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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
  private Map<String, List<XmlObject>> childTags = new LinkedHashMap<>();

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
  private boolean isComment = false;

  /**
   * 自定义注释内容，默认的话 是当前时间+服务码
   */
  private String comment;

  /**
   * 标签名
   */
  private String tagName;

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
  private List<HrSystem> system;

  /**
   * xml文件类型:这个属性进行废弃
   */
  @Deprecated
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
   * @param content 标签体
   * @param attrs   标签属性表
   */
  public XmlObject(String tagName, String Xpath,String content, Map<String, String> attrs) {
    super();
    this.tagName = tagName;
    this.Xpath = Xpath;
    this.content = content;
    this.attrs = attrs;
  }

  /**
   * 批量解析
   * @param sheets
   * @return
   */
  public static XmlMetadata ofBatch(ExcelSheet... sheets){
    XmlMetadata metadata = new XmlMetadata();
    for (ExcelSheet sheet : sheets) {
      XmlObject xmlObject = of(sheet);
      metadata.parseXmlObject(xmlObject);
    }
    return metadata;
  }

  /**
   * 解析单个Excel转换成对应需要转换的xml类型
   * @param sheet
   * @return
   */
  private static XmlObject of(ExcelSheet sheet){
    XmlObject result = new XmlObject();

    result.setServiceCode(sheet.getIndex().getServiceCode());
    //处理输入字段
    result.setChildTags(sheet.getInRows(),Constants.IN);

    //处理输出字段
    result.setChildTags(sheet.getOutRows(),Constants.OUT);

    //解析公共部分
    ExcelSheet appHead = sheet.getAppHead();
    result.setChildTags(appHead.getOutRows(),Constants.OUT);
    result.setChildTags(appHead.getInRows(),Constants.IN);
    ExcelSheet sysHead = sheet.getSysHead();
    result.setChildTags(sysHead.getOutRows(),Constants.OUT);
    result.setChildTags(sysHead.getInRows(),Constants.IN);

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
      HrSystem consumer = HrConfig.getConfig().systemMappings.get(index.getConsumerName());
      HrSystem provider = HrConfig.getConfig().systemMappings.get(index.getProviderName());
      List<HrSystem> system = new ArrayList<>(2);
      if (ObjectUtil.isNotEmpty(consumer)){
        system.add(consumer);
      }else {
        log.warn("c端渠道：【"+index.getConsumerName()+"】无法找到对应的信息，请检查配置");
      }
      if (ObjectUtil.isNotEmpty(provider)){
        system.add(provider);
      }else {
        log.warn("P端渠道：【"+index.getProviderName()+"】无法找到对应的信息，请检查配置");
      }
      setSystem(system);
    }
  }

  /**
   * 处理子节点信息。并且给子节点属性赋值
   * @param rows
   * @param nodeType in节点；out节点
   */
  private void setChildTags(List<ExcelRow> rows,String nodeType){
    List<XmlObject> list = CollUtil.newArrayList();

    rows.stream().forEach(row -> {
      String rootPath = getRootPath(row.getPath(),nodeType);
      //创建新节点加入
      XmlObject node;
      if (Constants.XML_ARRAY.equals(row.getType())){
        //type节点做特殊处理
        node = XpathParser.createNode(row.getTagName(), rootPath+"array", null, null);
        node.setAttrs("type","array");
        node.setAttrs("is_struct","false");
      }else {
        node = XpathParser.createNode(row.getTagName(), rootPath, null, null);
        node.setAttrs(row);
      }
      list.add(node);
    });
    getChildTags().put(nodeType,list);
  }

  /**
   * 处理路径信息，将路径信息转换为Xpath所需要的格式
   * @param path
   * @param nodeType
   * @return
   */
  private String getRootPath(String path,String nodeType){
    String rootPath = "";
    if (XmlType.METADATA.equals(this.xmlType)){
      rootPath = "/metadata";
    }else if (XmlType.SERVICE.equals(this.xmlType)) {
      rootPath = "/"+path;
    }else if (XmlType.SYSTEM_IDENTIFY.equals(this.xmlType)){

    }else if (XmlType.SERVICE_IDENTIFY.equals(this.xmlType)){

    }else if (XmlType.SERVICE_DEFINITION.equals(this.xmlType)){
      // /S0200200000510/request/sdoroot  /S0200200000510/response/sdoroot
      rootPath = path.replace("service","/S"+this.serviceCode+"/"+(Constants.IN.equals(nodeType)?"request":"response")+"/sdoroot");
      //当检测到 */array/* 时，需要在路径后加一个 sdo节点
      if (verifyArray(rootPath)){
        rootPath += "sdo";
      }
    }
    return rootPath;
  }

  /**
   *
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
      this.childTags = new LinkedHashMap<>();
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

  public boolean isComment() {
    return isComment;
  }

  public void setComment(boolean comment) {
    isComment = comment;
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
    Xpath = xpath;
  }

  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public List<HrSystem> getSystem() {
    return system;
  }

  public void setSystem(List<HrSystem> system) {
    this.system = system;
  }

}
