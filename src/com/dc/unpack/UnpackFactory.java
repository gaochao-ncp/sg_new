package com.dc.unpack;

import com.dc.excel.ExcelRow;
import com.dc.excel.ExcelSheet;

import java.util.List;

/**
 * 将excel数据解析成对应创建xml文件的数据
 * @author: Administrator
 * @date: 2020-11-20 15:01
 * @version: 1.0
 */
public interface UnpackFactory {

  /**
   * 解析多个sheet
   * @param sheets
   * @return
   */
  List<UnpackingRoot> parseExcel(List<ExcelSheet> sheets);

  /**
   * 解析单个sheet
   * @param sheet
   * @return
   */
  UnpackingRoot parseExcel(ExcelSheet sheet);

  /**
   * 将所有sheet页的字段聚合在一起,形成一个对象
   * @param sheets
   * @return
   */
  UnpackingRoot parseAll(List<ExcelSheet> sheets);

  /**
   * 解析单行row
   * @param row
   * @return
   */
  UnpackingChild parseRow(ExcelRow row);

}
