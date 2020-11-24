package com.dc.excel;

/**
 * 索引信息
 * @author: Administrator
 * @date: 2020-11-24 11:50
 * @version: 1.0
 */
public class ExcelIndex {

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
}
