package com.dc.excel;

import java.util.List;

/**
 * sheet页获取的信息
 *
 * @author: Administrator
 * @date: 2020-11-19 15:38
 * @version: 1.0
 */
public class ExcelSheet {

  private String sheetName;

  /**
   * 输入字段
   */
  private List<ExcelRow> inRows;

  /**
   * 输出字段信息
   */
  private List<ExcelRow> outRows;

  /**
   * 索引页的数据信息：只解析本sheet页对应的那一栏
   */
  private ExcelIndex index;

  /**
   * 公共部分 SYS_HEAD
   */
  private ExcelSheet sysHead;

  /**
   * 公共部分 BODY
   */
  private ExcelSheet appHead;

  /**
   * 公共部分 公共字段
   */
  private ExcelSheet common;

  /**
   * 是否解析标志
   */
  private boolean parseFlag = false;

  public String getSheetName() {
    return sheetName;
  }

  public void setSheetName(String sheetName) {
    this.sheetName = sheetName;
  }

  public List<ExcelRow> getInRows() {
    return inRows;
  }

  public void setInRows(List<ExcelRow> inRows) {
    this.inRows = inRows;
  }

  public List<ExcelRow> getOutRows() {
    return outRows;
  }

  public void setOutRows(List<ExcelRow> outRows) {
    this.outRows = outRows;
  }

  public boolean isParseFlag() {
    return parseFlag;
  }

  public void setParseFlag(boolean parseFlag) {
    this.parseFlag = parseFlag;
  }

  public ExcelIndex getIndex() {
    return index;
  }

  public void setIndex(ExcelIndex index) {
    this.index = index;
  }

  public ExcelSheet getSysHead() {
    return sysHead;
  }

  public void setSysHead(ExcelSheet sysHead) {
    this.sysHead = sysHead;
  }

  public ExcelSheet getAppHead() {
    return appHead;
  }

  public void setAppHead(ExcelSheet appHead) {
    this.appHead = appHead;
  }

  public ExcelSheet getCommon() {
    return common;
  }

  public void setCommon(ExcelSheet common) {
    this.common = common;
  }
}
