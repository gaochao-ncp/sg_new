package com.dc.rule;

/**
 * 规则参数基类
 * @author: Administrator
 * @date: 2020-11-20 17:11
 * @version: 1.0
 */
public class BaseRule {

  /**
   * in端路径
   */
  private String inPath;
  /**
   * out端路径
   */
  private String outPath;

  public String getInPath() {
    return inPath;
  }

  public void setInPath(String inPath) {
    this.inPath = inPath;
  }

  public String getOutPath() {
    return outPath;
  }

  public void setOutPath(String outPath) {
    this.outPath = outPath;
  }
}
