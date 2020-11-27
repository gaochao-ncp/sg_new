package com.dc.excel;

import com.dc.common.CommonField;

/**
 * 存储sheet页的信息
 *
 * @author: Administrator
 * @date: 2020-11-19 11:32
 * @version: 1.0
 */
public class ExcelRow extends CommonField {

  /**
   * 存放路径
   */
  private String path;

  /**
   * 是否存在删除线
   */
  private boolean deleteFlag = false;

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public boolean isDeleteFlag() {
    return deleteFlag;
  }

  public void setDeleteFlag(boolean deleteFlag) {
    this.deleteFlag = deleteFlag;
  }
}
