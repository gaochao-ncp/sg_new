package com.dc.excel;

import org.apache.poi.ss.usermodel.Workbook;

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
   * 预留,防止以后用到
   */
  private Workbook workbook;

  /**
   * 输入字段
   */
  private List<ExcelRow> inRows;

  /**
   * 输出字段信息
   */
  private List<ExcelRow> outRows;

  /**
   * 索引页的数据信息
   */
  private List<ExcelRow> indexRows;

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

  public Workbook getWorkbook() {
    return workbook;
  }

  public void setWorkbook(Workbook workbook) {
    this.workbook = workbook;
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

  public List<ExcelRow> getIndexRows() {
    return indexRows;
  }

  public void setIndexRows(List<ExcelRow> indexRows) {
    this.indexRows = indexRows;
  }
}
