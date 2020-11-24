package com.dc.excel;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有的Excel文档
 *
 * @author: Administrator
 * @date: 2020-11-24 0:18
 * @version: 1.0
 */
public class ExcelAll {

  private List<ExcelSheet> list;

  public List<ExcelSheet> getList() {
    if (this.list == null){
      this.list = new ArrayList<>();
    }
    return list;
  }

  public void setList(List<ExcelSheet> list) {
    this.list = list;
  }
}
