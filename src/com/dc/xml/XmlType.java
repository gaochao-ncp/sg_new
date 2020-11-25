package com.dc.xml;

/**
 * 生成的文件类型
 * @author: Administrator
 * @date: 2020-11-24 11:14
 * @version: 1.0
 */
public enum XmlType {
  /**
   * 元数据 metadata.xml 所有字段全部生成
   */
  METADATA("metadata"),
  /**
   * 拆组包文件 输入字段：channel:in=service:out || 输出字段 channel:out=service:in
   */
  SERVICE("service"),
  /**
   * 服务定义 输入 输出
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
