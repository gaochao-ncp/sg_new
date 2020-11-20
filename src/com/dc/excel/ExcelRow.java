package com.dc.excel;

import com.dc.common.CommonField;

/**
 * 存储sheet页的信息
 *
 * @author: Administrator
 * @date: 2020-11-19 11:32
 * @version: 1.0
 */
public class ExcelRow extends CommonField {

  /**
   * 服务消费者
   */
  private String consumerName;

  /**
   * 服务提供者
   */
  private String providerName;

  /**
   * 交易代码
   */
  private String tradeCode;

  /**
   * 服务编码
   */
  private String serviceCode;

  /**
   * 存放路径
   */
  private String path;


  public String getConsumerName() {
    return consumerName;
  }

  public void setConsumerName(String consumerName) {
    this.consumerName = consumerName;
  }

  public String getProviderName() {
    return providerName;
  }

  public void setProviderName(String providerName) {
    this.providerName = providerName;
  }

  public String getTradeCode() {
    return tradeCode;
  }

  public void setTradeCode(String tradeCode) {
    this.tradeCode = tradeCode;
  }

  public String getServiceCode() {
    return serviceCode;
  }

  public void setServiceCode(String serviceCode) {
    this.serviceCode = serviceCode;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }
}
