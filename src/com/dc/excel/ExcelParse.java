package com.dc.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.WorkbookUtil;
import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.config.HrConfig;
import com.dc.config.HrSystem;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
   * 根据sheet页名称获取 {@link ExcelReader 对象}
   * @param absolutePath
   * @return
   */
  public ExcelReader getExcelReader(String absolutePath,String sheetName){
    return ExcelUtil.getReader(FileUtil.file(absolutePath),sheetName);
  }

  /**
   * 默认获取excel首个sheet页的ExcelReader对象
   * @param absolutePath
   * @return
   */
  public ExcelReader getExcelReader(String absolutePath){
    return ExcelUtil.getReader(FileUtil.file(absolutePath));
  }

  /**
   * 解析Excel的全部sheet数据
   * @param absolutePath 绝对路径
   * @return
   */
  public ExcelAll parseExcel(String absolutePath){
    ExcelAll excelAll = new ExcelAll();
    List<ExcelSheet> all = excelAll.getList();
    ExcelReader excelReader = getExcelReader(absolutePath);
    if (excelReader == null){
      return excelAll;
    }
    //获取所有索引页
    List<String> sheetNames = excelReader.getSheetNames();
    //修订记录不需要解析
    sheetNames.stream().skip(1).forEach(sheetName -> {
      all.add(parseExcel(absolutePath,sheetName));
    });
    return excelAll;
  }

  /**
   * 解析指定sheet页的数据
   * @param absolutePath
   * @param sheetName
   */
  public ExcelSheet parseExcel(String absolutePath, String sheetName){
    if (CommonUtil.verifySheetName(sheetName)){
      throw new RuntimeException(sheetName+" 无法解析");
    }
    //解析表格
    ExcelSheet sheet = parseExcelSheet(getExcelReader(absolutePath,sheetName));
    //解析公共部分
    sheet.setAppHead(parseExcelSheet(getExcelReader(absolutePath,Constants.SHEET_APP_HEAD)));
    sheet.setSysHead(parseExcelSheet(getExcelReader(absolutePath,Constants.SHEET_SYS_HEAD)));
    //解析索引页数据信息
    sheet.setIndex(parseIndex(getExcelReader(absolutePath,Constants.SHEET_INDEX),sheetName));
    //标志为已解析
    sheet.setParseFlag(true);
    return sheet;
  }

  /**
   * 解析表格
   * @param excelReader
   * @return
   */
  private ExcelSheet parseExcelSheet(ExcelReader excelReader){
    ExcelSheet sheet = new ExcelSheet();

    if (excelReader == null){
      log.warn("解析表格为空，请核实！");
      return sheet;
    }

    String sheetName = excelReader.getSheet().getSheetName();
    //常量对照表和索引页目前不解析
    if (Constants.SHEET_COMMON.equals(sheetName)
            || Constants.SHEET_INDEX.equals(sheetName)){
      return sheet;
    }

    sheet.setSheetName(sheetName);
    sheet.setWorkbook(excelReader.getWorkbook());

    HSSFWorkbook workbook = (HSSFWorkbook) excelReader.getWorkbook();

    int fontIndexAsInt = excelReader.getCell("H9").getCellStyle().getFontIndex();
    HSSFFont fontAt = workbook.getFontAt(fontIndexAsInt);
    System.out.println("fontAt.getStrikeout() = " + fontAt.getStrikeout());

    //处理其他的sheet页面,从G行开始读取,输出行默认是第7列开始
    //计算输入字段和输出字段的位置,非索引页的处理逻辑
    int rowCount = excelReader.getRowCount();
    int inStart = 0,inEnd=0,outStart = 0;
    for (int i = 0; i < rowCount; i++) {
      //读取7:G列数据
      String cellValue = (String) excelReader.readCellValue(6, i);
      if (Constants.IN_CN.equals(cellValue)){
        //不包括输入行
        inStart = i+1;
      }else if (Constants.OUT_CN.equals(cellValue)){
        outStart = i+1;
        inEnd = i-1;
      }
    }

    List<List<Object>> inRowValue = excelReader.read(inStart,inEnd);
    List<List<Object>> outRowValue = excelReader.read(outStart);

    if (CollUtil.isNotEmpty(inRowValue)){
      sheet.setInRows(parseExcelRow(inRowValue));
    }

    if (CollUtil.isNotEmpty(outRowValue)){
      sheet.setOutRows(parseExcelRow(outRowValue));
    }
    return sheet;
  }

  /**
   * 解析 索引 页的数据
   * @param excelReader
   * @return
   */
  public ExcelIndex parseIndex(ExcelReader excelReader,String sheetName){
    ExcelIndex index = new ExcelIndex();
    //索引页直接解析
    List<List<Object>> allRow = excelReader.read();
    //索引页第一行标题,截取0-11列的数据,因为存在很多脏数据
    for (List<Object> row : allRow) {
      List<String> rowValue = row.stream().map(o -> String.valueOf(o)).collect(Collectors.toList());
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
  private List<ExcelRow> parseExcelRow(List<List<Object>> rows){
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
  private ExcelRow doParseExcelRow(List<Object> row){
    ExcelRow result = new ExcelRow();
    //截取row中7:G列之后的数据
    List<String> rowValue = row.stream().skip(7).map(o -> String.valueOf(o)).collect(Collectors.toList());
    result.setTagName(rowValue.get(0));
    String type = rowValue.get(1).replaceAll("[^a-zA-Z]", "").toLowerCase();
    result.setType(type);
    if (double.class.getName().equals(type)){
      String[] value = parseDouble(rowValue.get(1));
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
   * 解析type为double时候的length和scale字段
   * @param value
   * @return
   */
  private String[] parseDouble(String value){
    String[] split = new String[2];
    Pattern p = Pattern.compile(Constants.REGEX);
    Matcher matcher = p.matcher(value);
    if (matcher.find()){
      split = matcher.group(0).split(",");
    }
    return split;
  }

  /**
   * 解析消费者系统英文名和中文名的映射关系
   * @param absolutePath
   * @return
   */
  public static Map<String, HrSystem> parseSystemMapping(String absolutePath){
    //Constants.SHEET_SYSTEM
    ExcelReader reader = ExcelUtil.getReader(new File(absolutePath), Constants.SHEET_SYSTEM);
    Map<String, HrSystem> map = new HashMap<>(99);
    if (reader != null){
      //读取所有的行信息
      List<List<Object>> rows = reader.read();
      //忽略前面两行无用
      List<List<Object>> collect = rows.stream().skip(2).collect(Collectors.toList());
      collect.stream().forEach(row -> {
        //只取前三行的数据
        List<String> system = row.stream().limit(3).map(o -> String.valueOf(o)).collect(Collectors.toList());
        HrSystem info = new HrSystem(system.get(0).trim(),system.get(1).trim(),Integer.valueOf(system.get(2).trim()));
        map.put(system.get(0),info);
      });
    }
    return map;
  }


}
