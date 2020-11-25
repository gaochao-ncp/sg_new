package com.dc.common;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.excel.ExcelSheet;

/**
 * 通用工具类
 *
 * @author: Administrator
 * @date: 2020-11-20 16:58
 * @version: 1.0
 */
public class CommonUtil {

  private static final Log log = LogFactory.get(CommonUtil.class);

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

  /**
   * 校验sheet页名称是否合法
   * @param sheetName
   * @return
   */
  public static boolean verifySheetName(String sheetName){
    if (Constants.SHEET_COMMON.equals(sheetName)
            || Constants.SHEET_INDEX.equals(sheetName)
            || Constants.SHEET_SYS_HEAD.equals(sheetName)
            || Constants.SHEET_APP_HEAD.equals(sheetName)){
      log.warn("非法解析sheetName：【"+sheetName+"】");
      return true;
    }
    return false;
  }

}
