package com.dc.unpack;

import com.dc.common.CommonField;

import java.util.List;

/**
 * 拆组包子节点
 * @author: Administrator
 * @date: 2020-11-19 10:09
 * @version: 1.0
 */
public class UnpackingChild extends CommonField {

  private String metadataId;

  private String isStruct;

  private String mode;

  private String id;

  private String expression;

  private String encode;

  private String value;

  private List<UnpackingChild> childList;

  public String getMetadataId() {
    return metadataId;
  }

  public void setMetadataId(String metadataId) {
    this.metadataId = metadataId;
  }

  public String getIsStruct() {
    return isStruct;
  }

  public void setIsStruct(String isStruct) {
    this.isStruct = isStruct;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getExpression() {
    return expression;
  }

  public void setExpression(String expression) {
    this.expression = expression;
  }

  public String getEncode() {
    return encode;
  }

  public void setEncode(String encode) {
    this.encode = encode;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<UnpackingChild> getChildList() {
    return childList;
  }

  public void setChildList(List<UnpackingChild> childList) {
    this.childList = childList;
  }

  @Override
  public String toString() {
    return "UnpackingChild{" +
            "tagName='" + tagName + '\'' +
            ", chineseName='" + chineseName + '\'' +
            ", type='" + type + '\'' +
            ", length='" + length + '\'' +
            ", scale='" + scale + '\'' +
            '}';
  }
}
