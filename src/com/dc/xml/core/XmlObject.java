package com.dc.xml.core;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.excel.ExcelRow;
import com.dc.excel.ExcelSheet;
import com.dc.xml.XMLObject;

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
   * 子标签集合:包含输入输出字段
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
   * //todo 是否注释节点.先保留 后续进行优化注释行
   */
  private boolean noteElement = false;

  /**
   * 标签名
   */
  private String tagName;

  /**
   * 绝对路径，Xpath操作的时候需要用到.root节点不需要
   */
  private String path;

  /**
   * 服务编码，生成文件名
   */
  private String serviceCode;

  /**
   * 系统英文代码，生成文件名字
   */
  private String systemEng;

  /**
   * xml文件类型
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
   *
   * @param tagName 标签名
   */
  public XmlObject(String tagName,String path) {
    super();
    this.tagName = tagName;
    this.path = path;
  }

  /**
   * 构建XML对象
   *
   * @param tagName 标签名
   * @param content 标签体
   * @param attrs   标签属性表
   */
  public XmlObject(String tagName, String path,String content, Map<String, String> attrs) {
    super();
    this.tagName = tagName;
    this.path = path;
    this.content = content;
    this.attrs = attrs;
  }

  /**
   * 将Excel转换成对应需要转换的xml类型
   * @param xmlType 生成的根节点的类型
   * @param sheets
   * @return
   */
  public static XmlObject of(XmlType xmlType,ExcelSheet... sheets){
    XmlObject result = new XmlObject();

    //传入多个sheet页但是类型不为metadata的时候报错
    if (sheets.length > 1 && !XmlType.METADATA.equals(xmlType)){
      log.warn("生成类型请修改为metadata");
      return null;
    }

    //后续处理要用到这个属性，所以前置赋值
    result.setXmlType(xmlType);
    //当传入多个
    for (ExcelSheet sheet : sheets){
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
    }


    //给service节点添加属性
    if (XmlType.SERVICE.equals(xmlType)){
      result.setAttrs(Constants.NODE_PACKAGE_TYPE,Constants.NODE_PACKAGE_TYPE_VALUE);
      result.setAttrs(Constants.NODE_STORE_MODE,Constants.NODE_STORE_MODE_VALUE);
    }

    //根节点
    result.setRootElement(true);
    //设置根节点名称
    result.setTagName();
    return result;
  }

  /**
   * 解析SYS_HEAD APP_HEAD 公共部分
   * @param common
   */
  private void setChildTags(ExcelSheet common){


  }

  /**
   * 设置子节点，并且给子节点属性赋值
   * @param rows
   * @param nodeType in节点；out节点
   */
  private void setChildTags(List<ExcelRow> rows,String nodeType){
    List<XmlObject> list = CollUtil.newArrayList();

    rows.stream().forEach(row -> {
      String rootPath = getRootPath(row.getPath(),nodeType);
      //创建新节点加入
      XmlObject node;
      if ("array".equals(row.getType())){
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

  private String getRootPath(String path,String nodeType){
    String rootPath = "";
    if (XmlType.METADATA.equals(this.xmlType)){
      rootPath = "/metadata";
    }else if (XmlType.SERVICE.equals(this.xmlType)) {
      rootPath = "/"+path;
    }else if (XmlType.SYSTEM_IDENTIFY.equals(this.xmlType)){
      //TODO 后续做
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

  public boolean verifyArray(String path){
    String[] split = path.split("\\/");

    if ("array".equals(split[split.length-2])){
      return true;
    }
    return false;
  }

  private void setAttrs(String key ,String value){
    getAttrs().put(key,value);
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

  public boolean isNoteElement() {
    return noteElement;
  }

  public void setNoteElement(boolean noteElement) {
    this.noteElement = noteElement;
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
  public void setTagName(){
    XmlType xmlType = getXmlType();
    String tagName = xmlType.getRootName();
    if (XmlType.SERVICE_DEFINITION.equals(xmlType)){
      tagName += getServiceCode();
    }
    setTagName(tagName);
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public String getSystemEng() {
    return systemEng;
  }

  public void setSystemEng(String systemEng) {
    this.systemEng = systemEng;
  }

  public XmlType getXmlType() {
    return xmlType;
  }

  public void setXmlType(XmlType xmlType) {
    this.xmlType = xmlType;
  }
}
