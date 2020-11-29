package com.dc.excel;

import cn.hutool.core.util.StrUtil;
import com.dc.common.CommonUtil;

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
   * 是否解析标志:当无法解析的时候会返回一个为false的对象，如果此标志不为true的话，就不对该对象进行下一步的解析
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

  /**
   * 获取服务码
   * @param excelSheet
   * @return
   */
  public String getServiceCode(ExcelSheet excelSheet){
    //先从索引页中查找服务码
    String serviceCode = excelSheet.getIndex().getServiceCode();
    if (StrUtil.isEmpty(serviceCode)){
      //无法找到的时候取sheet名
      if (StrUtil.isNotBlank(excelSheet.getSheetName()) && CommonUtil.isNumeric(excelSheet.getSheetName())){
        serviceCode = excelSheet.getSheetName();
      }
    }
    return serviceCode;
  }
}
