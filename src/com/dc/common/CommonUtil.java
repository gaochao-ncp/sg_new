package com.dc.common;

import com.dc.excel.ExcelSheet;

import java.util.List;

/**
 * 通用工具类
 *
 * @author: Administrator
 * @date: 2020-11-20 16:58
 * @version: 1.0
 */
public class CommonUtil {

  /**
   * 解析metadata.xml的时候对不需要的sheet进行过滤
   * @param sheet
   * @return
   */
  public static boolean filterSheet(ExcelSheet sheet){
    if (Constants.SHEET_COMMON.equals(sheet.getSheetName())
            || Constants.SHEET_INDEX.equals(sheet.getSheetName())){
      return false;
    }
    return true;
  }

}
