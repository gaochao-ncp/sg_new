package com.dc.unpack.metadata;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.dc.common.Constants;
import com.dc.excel.ExcelRow;
import com.dc.excel.ExcelSheet;
import com.dc.unpack.UnpackFactory;
import com.dc.unpack.UnpackingChild;
import com.dc.unpack.UnpackingRoot;

import java.util.ArrayList;
import java.util.List;

/**
 * metadata组装工厂
 *
 * @author: Administrator
 * @date: 2020-11-20 15:06
 * @version: 1.0
 */
public class MetadataUnpackFactory implements UnpackFactory {

  @Override
  public List<UnpackingRoot> parseExcel(List<ExcelSheet> sheets) {
    List<UnpackingRoot> roots = new ArrayList<>();
    if (CollUtil.isNotEmpty(sheets)){
      sheets.stream().forEach(sheet -> {
        roots.add(parseExcel(sheet));
      });
    }
    return roots;
  }

  @Override
  public UnpackingRoot parseExcel(ExcelSheet sheet) {
    UnpackingRoot root = new UnpackingRoot();

    List<UnpackingChild> childList = new ArrayList<>();
    List<ExcelRow> excelRowList = addAll(sheet);

    excelRowList.stream().forEach(row -> {
      childList.add(parseRow(row));
    });

    root.setRootName("metadata");
    root.setChildList(childList);
    return root;
  }

  /**
   * 将inRows和OutRows合并到一起
   * @param sheets
   * @return
   */
  private List<ExcelRow> addAll(List<ExcelSheet> sheets){
    List<ExcelRow> excelRowList = new ArrayList<>();
    if (CollUtil.isNotEmpty(sheets)){
      sheets.stream().forEach(sheet -> {
        if (filterSheet(sheet)){
          excelRowList.addAll(addAll(sheet));
        }
      });
    }
    return excelRowList;
  }

  /**
   * 将inRows和OutRows合并到一起
   * @param sheet
   * @return
   */
  private List<ExcelRow> addAll(ExcelSheet sheet){
    List<ExcelRow> excelRowList = new ArrayList<>();

    if (!filterSheet(sheet)){
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

  @Override
  public UnpackingChild parseRow(ExcelRow row) {
    UnpackingChild child = new UnpackingChild();
    //属性copy
    BeanUtil.copyProperties(row,child);
    return child;
  }

  @Override
  public UnpackingRoot parseAll(List<ExcelSheet> sheets){
    UnpackingRoot root = new UnpackingRoot();
    List<UnpackingChild> childList = new ArrayList<>();
    if (CollUtil.isNotEmpty(sheets)){
      List<ExcelRow> excelRowList = addAll(sheets);
      excelRowList.stream().forEach(row -> {
        childList.add(parseRow(row));
      });
    }
    root.setRootName("metadata");
    root.setChildList(childList);
    return root;
  }

  /**
   * 对不需要的sheet进行过滤
   * @param sheet
   * @return
   */
  private boolean filterSheet(ExcelSheet sheet){
    if (Constants.SHEET_COMMON.equals(sheet.getSheetName())
            || Constants.SHEET_INDEX.equals(sheet.getSheetName())){
      return false;
    }
    return true;
  }

}
