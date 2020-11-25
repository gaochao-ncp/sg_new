package com.dc.xml.deprecated.impl;

import cn.hutool.core.util.StrUtil;
import com.dc.xml.deprecated.XMLObject;
import com.dc.xml.deprecated.XMLObjectFormatter;

import java.util.List;
import java.util.Map;

/**
 * 默认XML格式化工具
 * 自定义组装xml文件并输出
 * @author: Administrator
 * @date: 2020-11-21 20:09
 * @version: 1.0
 */
@Deprecated
public class DefaultXMLObjectFormatter implements XMLObjectFormatter {

  /**
   * 单个缩进位
   */
  private static final String RETRACT_VALUE = "  ";

  private final String systemLineSeparator = System.lineSeparator();

  /**
   * 当前节点层次
   */
  private int nodeLevel = 0;

  /**
   * 获取换行符
   *
   * @return String 换行符
   */
  public String getSystemLineSeparator() {
    return systemLineSeparator;
  }

  @Override
  public StringBuilder format(XMLObject xmlObject) {
    StringBuilder content = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
    content.append(systemLineSeparator);
    String currentNewLine = getSystemLineSeparator();
    format(xmlObject, content, currentNewLine);
    return content;
  }

  /**
   * 格式化指定节点
   *
   * @param xmlObject         节点对象
   * @param contentRepository 用于保存格式化内容的容器
   * @param lineSeparator     行分割符
   */
  private void format(XMLObject xmlObject, StringBuilder contentRepository, String lineSeparator) {
    // 获取缩进占位符, retract 与 nodeLevel 相关
    String retract = createRetract();

    // 构建标签头, 总是以空格结尾: "[retract] + <[tagName] "
    contentRepository.append(retract).append(createTagStart(xmlObject));

    // 构建标签属性 : "[attrName='value'] [attrName='value'] ... >"
    contentRepository.append(createAttrs(xmlObject));

    // 追加行结束符号 : [NEW_LINE]
    contentRepository.append(lineSeparator);

    // 构建标签体, [retract] + [content] + [NEW_LINE]
    String content = xmlObject.getContent();
    contentRepository.append(createContent(content));

    boolean hasChildren = xmlObject.hasEffectiveChildren();
    if (!hasChildren && StrUtil.isEmpty(content)) {
      // 在最后一个 ">" 前面插入 "/", 使标签闭合
      int idx = contentRepository.lastIndexOf(">");
      contentRepository.insert(idx, " /");
      // 结束程序
      return;
    }

    // 处理子标签
    Map<String, List<XMLObject>> childTags = xmlObject.getChildTags();
    for (Map.Entry<String, List<XMLObject>> me : childTags.entrySet()) {
      // 标签层级 : nodeLevel+1,控制缩进符,让xml格式更好看
      this.nodeLevel++;

      contentRepository.append(createRetract()).append(createTagStart(me.getKey())).append(lineSeparator);
      List<XMLObject> childrenOfEach = me.getValue();
      for (XMLObject child : childrenOfEach) {
        // 递归构建子标签
        format(child, contentRepository, lineSeparator);
      }
      contentRepository.append(createRetract()).append(createTagEnd(me.getKey())).append(lineSeparator);
      // 标签层级 : nodeLevel-1
      this.nodeLevel--;
    }

    // 构建标签尾 "[retract] + </[tagName]>"
    contentRepository.append(retract).append(createTagEnd(xmlObject)).append(lineSeparator);
  }

  /**
   * 创建标签体
   *
   * @param content 标签体的值
   * @return String XML文件中的标签体值
   */
  private String createContent(String content) {
    StringBuilder ctt = new StringBuilder();

    if (StrUtil.isNotBlank(content)) {
      ctt.append(createRetract());
      ctt.append(DefaultXMLObjectFormatter.RETRACT_VALUE);
      ctt.append(content).append(getSystemLineSeparator());
    }

    return ctt.toString();
  }

  /**
   * 创建标签结束字符串
   *
   * @param xmlObject 节点对象
   * @return String 标签结束字符串
   */
  private String createTagEnd(XMLObject xmlObject) {
    return "</" + xmlObject.getTagName() + ">";
  }

  /**
   * 创建结束标签
   * @param tagName
   * @return
   */
  private String createTagEnd(String tagName) {
    return "</" + tagName + ">";
  }

  /**
   * 创建属性字符串([attrName]=[attrValue] [attrName]=[attrValue] ... )
   *
   * @param xmlObject 节点对象
   * @return String 属性字符串
   */
  private String createAttrs(XMLObject xmlObject) {
    StringBuilder attrContent = new StringBuilder();
    // 遍历所有属性
    Map<String, String> attrs = xmlObject.getAttrs();
    if (attrs != null && attrs.size()>0) {
      for (Map.Entry<String, String> me : attrs.entrySet()) {
        String attrName = me.getKey().trim();
        if (StrUtil.isNotEmpty(attrName)) {
          String attrVal = me.getValue().trim();
          attrContent.append(" ").append(attrName).append("=").append("\"").append(attrVal).append("\"");
        }
      }
    }

    attrContent.append(">");
    return attrContent.toString();
  }

  /**
   * 创建标签开始字符串(&lt;[TagName] )
   *
   * @param xmlObject 节点对象
   * @return String 标签开始字符串
   */
  private String createTagStart(XMLObject xmlObject) {
    return "<" + xmlObject.getTagName();
  }

  /**
   * 创建单节点 例如 <BODY>
   * @param tagName
   * @return
   */
  private String createTagStart(String tagName){
    return "<" + tagName + ">";
  }

  /**
   * 创建缩进位字符串
   *
   * @return String 缩进位字符串
   */
  private String createRetract() {
    StringBuilder retract = new StringBuilder();
      for (int i = 0; i < this.nodeLevel; i++){
        retract.append(DefaultXMLObjectFormatter.RETRACT_VALUE);
      }
    return retract.toString();
  }
}
