package com.dc.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.config.HrSystem;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 将excel中的数据解析到实体类中
 *
 * @author: Administrator
 * @date: 2020-11-18 11:46
 * @version: 1.0
 */
public class ExcelParse {

  private static final Log log = LogFactory.get(ExcelParse.class);



  /**
   * 解析Excel的全部sheet数据
   * @param absolutePath 绝对路径
   * @return
   */
  public ExcelAll parseExcel(String absolutePath){
    ExcelAll excelAll = new ExcelAll();
    List<ExcelSheet> all = excelAll.getList();
    Workbook wb = ExcelUtil.readExcel(absolutePath);
    if (ObjectUtil.isNull(wb)){
      return excelAll;
    }

    //获取所有sheet页
    Iterator<Sheet> sheets = wb.sheetIterator();

    while (sheets.hasNext()){
      Sheet next = sheets.next();
      if (CommonUtil.filteredSheet(next.getSheetName())){
        continue;
      }
      if (CommonUtil.filteredCommonSheet(next.getSheetName())){
        continue;
      }
      ExcelSheet sheet = parseExcel(absolutePath, next.getSheetName());
      all.add(sheet);
    }
    return excelAll;
  }

  /**
   * 解析整个工作簿:不包括工作簿中的公共部分：见{@link CommonUtil#filteredCommonSheet(String)}
   * @param absolutePath
   * @param sheetNames
   * @return
   */
  public List<ExcelSheet> parseExcel(String absolutePath, List<String> sheetNames){
    List<ExcelSheet> sheets = new ArrayList<>();
    if (CollUtil.isNotEmpty(sheetNames)){
      sheetNames.stream().forEach(sheetName -> {
        if (Constants.ALL.equals(sheetName)){
          //配置信息中配置ALL的话表示该文档的所有sheet页都需要解析
          ExcelAll excelAll = parseExcel(absolutePath);
          sheets.addAll(excelAll.getList());
        }else {
          ExcelSheet sheet = parseExcel(absolutePath, sheetName);
          if (ObjectUtil.isNotNull(sheet)){
            sheets.add(sheet);
          }
        }
      });
    }
    return  sheets;
  }

  /**
   * 解析工作簿指定sheet页的数据。
   * @param absolutePath word文档的绝对路径，不然无法解析
   * @param sheetName 要解析的sheet页的名字
   */
  public ExcelSheet parseExcel(String absolutePath, String sheetName){
    if (CommonUtil.filteredCommonSheet(sheetName)){
      return null;
    }
    //解析表格
    ExcelSheet sheet = parseExcelSheet(ExcelUtil.readExcel(absolutePath,sheetName));
    //解析公共部分
    sheet.setAppHead(parseExcelSheet(ExcelUtil.readExcel(absolutePath,Constants.SHEET_APP_HEAD)));
    sheet.setSysHead(parseExcelSheet(ExcelUtil.readExcel(absolutePath,Constants.SHEET_SYS_HEAD)));
    sheet.setCommon(parseExcelSheet(ExcelUtil.readExcel(absolutePath,Constants.SHEET_COMMON_FILED)));
    //解析索引页数据信息
    sheet.setIndex(parseIndex(ExcelUtil.readExcel(absolutePath,Constants.SHEET_INDEX),sheetName));

    //标志为已解析
    sheet.setParseFlag(true);
    return sheet;
  }

  /**
   * 解析工作表
   * @param sheetBean
   * @return
   */
  private ExcelSheet parseExcelSheet(Sheet sheetBean){
    ExcelSheet sheet = new ExcelSheet();

    if (ObjectUtil.isNull(sheetBean)){
      log.warn("解析表格为空，请核实！");
      return sheet;
    }

    String sheetName = sheetBean.getSheetName();
    //常量对照表和索引页还有修订记录目前不支持解析
    if (CommonUtil.filteredSheet(sheetName)){
      return sheet;
    }

    sheet.setSheetName(sheetName);

    List<Row> read = ExcelUtil.readRow(sheetBean, 7);
    List<ExcelRow> inRowValue = ExcelUtil.readInRow(read);
    List<ExcelRow> outRowValue = ExcelUtil.readOutRow(read);

    if (CollUtil.isNotEmpty(inRowValue)){
      sheet.setInRows(inRowValue);
    }

    if (CollUtil.isNotEmpty(outRowValue)){
      sheet.setOutRows(outRowValue);
    }
    sheet.setParseFlag(true);
    return sheet;
  }

  /**
   * 解析索引页的数据:从索引页中找到对应的那条信息
   * @param sheet
   * @return
   */
  public ExcelIndex parseIndex(Sheet sheet,String sheetName){
    ExcelIndex index = new ExcelIndex();
    //索引页直接解析
    List<List<String>> allRow = ExcelUtil.read(sheet);
    //索引页第一行标题,截取0-11列的数据,因为存在很多脏数据
    for (List<String> rowValue : allRow) {
      //找到对应当前excel的那一列数据
      if (sheetName.equals(rowValue.get(4))){
        index.setTradeCode(rowValue.get(0));
        //服务编码
        index.setServiceCode(rowValue.get(4));
        index.setConsumerName(rowValue.get(6));
        index.setProviderName(rowValue.get(7));
      }
    }

    if (StrUtil.isBlank(index.getServiceCode())){
      log.warn("当前sheetName【"+sheetName+"】没有在索引页中搜索到对应的数据");
    }
    return index;
  }

  /**
   * 解析多行数据
   * @param rows 多行数据
   * @return
   */
  private List<ExcelRow> parseExcelRow(List<List<String>> rows){
    List<ExcelRow> rowList = new ArrayList<>();
    rows.stream().forEach(row -> {
      rowList.add(doParseExcelRow(row));
    });
    return rowList;
  }

  /**
   * 解析单行数据
   * @param row
   * @return
   */
  private ExcelRow doParseExcelRow(List<String> row){
    ExcelRow result = new ExcelRow();
    //截取row中7:G列之后的数据
    List<String> rowValue = row.stream().skip(7).collect(Collectors.toList());
    result.setTagName(rowValue.get(0));
    String type = rowValue.get(1).replaceAll("[^a-zA-Z]", "").toLowerCase();
    result.setType(type);
    if (double.class.getName().equals(type)){
      String[] value = ExcelUtil.parseDouble(rowValue.get(1));
      result.setLength(value[0]);
      result.setScale(value[1]);
    }else {
      result.setLength(rowValue.get(1).replaceAll("\\D*",""));
    }
    result.setChineseName(rowValue.get(2));
    result.setPath(rowValue.get(3));
    return result;
  }

  /**
   * 解析消费者系统英文名和中文名的映射关系
   * @param absolutePath
   * @return
   */
  public static Map<String, HrSystem> parseSystemMapping(String absolutePath){
    //Constants.SHEET_SYSTEM
    Sheet s = ExcelUtil.readExcel(absolutePath, Constants.SHEET_SYSTEM);
    Map<String, HrSystem> map = new HashMap<>(99);
    if (ObjectUtil.isNotNull(s)){
      //忽略前面两行无用的行信息
      List<List<String>> rows = ExcelUtil.read(s,2,s.getLastRowNum());
      rows.stream().forEach(row -> {
        //只取前三行的数据
        HrSystem info = new HrSystem(row.get(0).trim(),row.get(1).trim(),Integer.valueOf(row.get(2).trim()));
        map.put(row.get(0),info);
      });
    }
    return map;
  }



}
