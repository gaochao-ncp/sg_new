package com.dc.excel;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.CommonField;

/**
 * 存储sheet页的信息
 *
 * @author: Administrator
 * @date: 2020-11-19 11:32
 * @version: 1.0
 */
public class ExcelRow extends CommonField {

  private static final Log log = LogFactory.get(ExcelRow.class);

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

  /**
   * 检查数据是否合法，如果不合法，不进行添加
   * @return true 数据不合法，忽略；false 数据合法，添加
   */
  public boolean ignoreRow(){
    if (StrUtil.isBlank(super.tagName)){
      log.warn("Excel Row 节点 tagName 为空，请检查文档的数据正确性！自动忽略该节点");
      return true;
    }
    return false;

  }
}
