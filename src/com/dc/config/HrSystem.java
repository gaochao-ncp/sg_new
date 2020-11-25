package com.dc.config;

/**
 * 华融消费之系统映射
 *
 * @author: Administrator
 * @date: 2020-11-25 15:18
 * @version: 1.0
 */
public class HrSystem {

  /** 英文代码 **/
  private String code;
  /** 中文名字 **/
  private String value;
  /** 分配的端口 **/
  private Integer port;

  public HrSystem(String code, String value, Integer port) {
    this.code = code;
    this.value = value;
    this.port = port;
  }

  public String getCode() {
    return code;
  }

  public String getValue() {
    return value;
  }

  public Integer getPort() {
    return port;
  }
}
