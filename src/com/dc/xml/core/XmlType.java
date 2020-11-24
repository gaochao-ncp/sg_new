package com.dc.xml.core;

/**
 * @author: Administrator
 * @date: 2020-11-24 11:14
 * @version: 1.0
 */
public enum XmlType {
  /**
   * 元数据
   */
  METADATA("metadata"),
  /**
   * 拆组包文件
   */
  SERVICE("service"),
  /**
   * 服务定义
   */
  SERVICE_DEFINITION("S"),
  /**
   * 服务识别
   */
  SERVICE_IDENTIFY("channels"),
  /**
   * 系统识别
   */
  SYSTEM_IDENTIFY("systems");

  /**
   * 根节点名称
   */
  private String rootName;

  XmlType(String rootName) {
    this.rootName = rootName;
  }

  public String getRootName() {
    return rootName;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }
}
