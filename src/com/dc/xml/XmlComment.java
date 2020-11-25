package com.dc.xml;

/**
 * 注释节点信息
 * //todo 这个是我实现注释功能的初步设想，实现待确定
 * @author: Administrator
 * @date: 2020-11-25 14:49
 * @version: 1.0
 */
public class XmlComment {
  /**
   * 开始节点信息
   */
  private String startXpath;

  /**
   * 开始注释
   */
  private String startComment;

  /**
   * 结束节点信息
   */
  private String endXpath;

  /**
   * 结束注释
   */
  private String endComment;

  public String getStartXpath() {
    return startXpath;
  }

  public void setStartXpath(String startXpath) {
    this.startXpath = startXpath;
  }

  public String getStartComment() {
    return startComment;
  }

  public void setStartComment(String startComment) {
    this.startComment = startComment;
  }

  public String getEndXpath() {
    return endXpath;
  }

  public void setEndXpath(String endXpath) {
    this.endXpath = endXpath;
  }

  public String getEndComment() {
    return endComment;
  }

  public void setEndComment(String endComment) {
    this.endComment = endComment;
  }
}
