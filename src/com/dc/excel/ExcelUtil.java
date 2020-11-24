package com.dc.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.CommonUtil;
import com.dc.common.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Administrator
 * @date: 2020-11-23 16:17
 * @version: 1.0
 */
public class ExcelUtil {

  private static final Log log = LogFactory.get(ExcelUtil.class);

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

  /**
   * 将inRows和OutRows合并到一起
   * @param sheet
   * @return
   */
  public static List<ExcelRow> addAll(ExcelSheet sheet){
    List<ExcelRow> excelRowList = new ArrayList<>();

    if (!CommonUtil.filterSheet(sheet)){
      return excelRowList;
    }

    // 字段全部解析
    List<ExcelRow> inRows = sheet.getInRows();
    List<ExcelRow> outRows = sheet.getOutRows();

    if (CollUtil.isNotEmpty(inRows)){
      excelRowList.addAll(inRows);
    }

    if (CollUtil.isNotEmpty(outRows)){
      excelRowList.addAll(outRows);
    }
    return excelRowList;
  }

  /**
   * 将inRows和OutRows合并到一起
   * @param sheets
   * @return
   */
  public static List<ExcelRow> addAll(List<ExcelSheet> sheets){
    List<ExcelRow> excelRowList = new ArrayList<>();
    if (CollUtil.isNotEmpty(sheets)){
      sheets.stream().forEach(sheet -> {
        if (CommonUtil.filterSheet(sheet)){
          excelRowList.addAll(addAll(sheet));
        }
      });
    }
    return excelRowList;
  }
}
