package com.dc.xml;

/**
 * 注释节点信息
 *
 * @author: Administrator
 * @date: 2020-11-25 14:49
 * @version: 1.0
 */
public class XmlContext {
  /**
   * 开始节点信息
   */
  private String startXpath;

  /**
   * 开始注释
   */
  private String startContext;

  /**
   * 结束节点信息
   */
  private String endXpath;

  /**
   * 结束注释
   */
  private String endContext;

  public String getStartXpath() {
    return startXpath;
  }

  public void setStartXpath(String startXpath) {
    this.startXpath = startXpath;
  }

  public String getStartContext() {
    return startContext;
  }

  public void setStartContext(String startContext) {
    this.startContext = startContext;
  }

  public String getEndXpath() {
    return endXpath;
  }

  public void setEndXpath(String endXpath) {
    this.endXpath = endXpath;
  }

  public String getEndContext() {
    return endContext;
  }

  public void setEndContext(String endContext) {
    this.endContext = endContext;
  }
}
