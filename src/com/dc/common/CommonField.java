package com.dc.common;

/**
 * 公共的一些属性
 *
 * @author: Administrator
 * @date: 2020-11-19 11:34
 * @version: 1.0
 */
public class CommonField {

  protected String tagName;

  protected String type;

  protected String length;

  protected String scale;

  protected String chineseName;

  public String getTagName() {
    return tagName;
  }

  public void setTagName(String tagName) {
    this.tagName = tagName;
  }

  public String getChineseName() {
    return chineseName;
  }

  public void setChineseName(String chineseName) {
    this.chineseName = chineseName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    this.length = length;
  }

  public String getScale() {
    return scale;
  }

  public void setScale(String scale) {
    this.scale = scale;
  }
}
