package com.dc.common;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.config.HrConfig;
import com.dc.excel.ExcelSheet;
import org.apache.poi.ss.usermodel.Sheet;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
   * 过滤如下sheet页的解析：无法解析，没有对应的方法支持
   * @param sheet
   * @return
   */
  public static boolean filterSheet(ExcelSheet sheet){
    return filteredSheet(sheet.getSheetName());
  }

  /**
   * 过滤如下sheet页的解析：无法解析，没有对应的方法支持
   * @param sheetName
   * @return true 不解析；false 能解析
   */
  public static boolean filteredSheet(String sheetName){
    if (HrConfig.getConfig().getFilteredSheets().contains(sheetName)){
      log.warn("针对当前sheet页：【"+sheetName+"】无法解析，会在后续进行解析。");
      return true;
    }
    return false;
  }

  /**
   * 忽略公共部分的sheet页解析
   * @param sheet
   * @return
   */
  public static boolean filteredCommonSheet(Sheet sheet){
    return filteredCommonSheet(sheet.getSheetName());
  }

  /**
   * 忽略公共部分的sheet页解析
   * @param sheetName
   * @return
   */
  public static boolean filteredCommonSheet(String sheetName){
    if (Constants.SHEET_COMMON.equals(sheetName)
            || Constants.SHEET_INDEX.equals(sheetName)
            || Constants.SHEET_SYS_HEAD.equals(sheetName)
            || Constants.SHEET_APP_HEAD.equals(sheetName)){
      return true;
    }
    return false;
  }

  /**
   * 获取默认注释。格式为 20201126 GD号 GD号中文名 服务码
   * @param last 结束语
   * @return
   */
  public static String getDefaultComment(String last){
    StringBuffer sb = new StringBuffer();
    sb.append(DateUtil.format(new Date(), DatePattern.PURE_DATE_PATTERN));
    String gdCode = HrConfig.getConfig().getGdCode();
    if (StrUtil.isNotBlank(gdCode)){
      sb.append("-"+gdCode);
    }
    String gdValue = HrConfig.getConfig().getGdName();
    if (StrUtil.isNotBlank(gdValue)){
      sb.append("-"+gdValue);
    }
    if (StrUtil.isNotBlank(last)){
      sb.append("-"+last);
    }
    return sb.toString();
  }

  /**
   * 按照规则生成文件名的绝对路径
   * <p>
   *   规则：
   *   1.拆包文件：
   *      1.1 in端：channel_[consumerCode]_service_[serviceCode].xml 输入字段
   *      1.2 out端：channel_[providerCode]_service_[serviceCode].xml 输出字段
   *   2.组包文件：
   *      2.1 in端：service_[serviceCode]_system_[consumerCode].xml 输出字段
   *      2.2 out端：service_[serviceCode]_system_[providerCode].xml 输入字段
   *
   *
   * </p>
   * @param providerCode
   * @param consumerCode 系统代码
   * @param serviceCode 服务码
   * @return
   */
  public static List<String> generateFilePath(String type, String consumerCode ,String providerCode, String serviceCode){
    List<String> filePath = new ArrayList<>();
    if (Constants.IN.equals(type)){
      //输入字段
      filePath.add(HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/channel_"+consumerCode+"_service_"+serviceCode+".xml");
      filePath.add(HrConfig.getConfig().LOCAL_URL.get(Constants.OUT)+"/service_"+serviceCode+"_system_"+providerCode+".xml");
    }else if (Constants.OUT.equals(type)){
      //输出字段
      filePath.add(HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/service_"+serviceCode+"_system_"+consumerCode+".xml");
      filePath.add(HrConfig.getConfig().LOCAL_URL.get(Constants.OUT)+"/channel_"+providerCode+"_service_"+serviceCode+".xml");
    }else if (Constants.ALL.equals(type)){
      //全部 生成服务识别文件
      filePath.add(HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/service_"+serviceCode+".xml");
      filePath.add(HrConfig.getConfig().LOCAL_URL.get(Constants.OUT)+"/service_"+serviceCode+".xml");
    }
    return filePath;
  }


  /**
   * 给数组里面的数据去除空格并转换为List
   * @param array
   */
  public static List<String> trimToArrayList(String[] array){
    List<String> newArray = new ArrayList<>(array.length);
    for (String s : array) {
      newArray.add(s.trim());
    }
    return newArray;
  }

}
