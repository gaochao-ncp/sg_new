package com.dc.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.dc.common.Constants;
import com.dc.log.Log;

import java.util.ArrayList;
import java.util.List;
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
  public List<ExcelSheet> parseExcel(String absolutePath){
    List<ExcelSheet> all = new ArrayList<>();
    ExcelReader excelReader = getExcelReader(absolutePath);
    if (excelReader == null){
      return all;
    }
    //获取所有索引页
    List<String> sheetNames = excelReader.getSheetNames();
    //修订记录不需要解析
    sheetNames.stream().skip(1).forEach(sheetName -> {
      all.add(parseExcel(absolutePath,sheetName));
    });
    return all;
  }

  /**
   * 解析指定sheet页的数据
   * @param absolutePath
   * @param sheetName
   */
  public ExcelSheet parseExcel(String absolutePath, String sheetName){
    ExcelReader excelReader = getExcelReader(absolutePath,sheetName);
    return parseExcelSheet(excelReader);
  }

  private ExcelSheet parseExcelSheet(ExcelReader excelReader){
    ExcelSheet sheet = new ExcelSheet();

    if (excelReader == null){return sheet;}

    String sheetName = excelReader.getSheet().getSheetName();
    sheet.setSheetName(sheetName);
    sheet.setWorkbook(excelReader.getWorkbook());

    //常量对照表目前不解析
    if (Constants.SHEET_COMMON.equals(sheetName)){
      return sheet;
    }

    //对索引页进行特殊的处理
    boolean index = (Constants.SHEET_INDEX.equals(sheetName))?true:false;

    if (!index){
      //处理其他的sheet页面,从G行开始读取,输出行默认是第7列开始
      //计算输入字段和输出字段的位置,非索引页的处理逻辑
      int rowCount = excelReader.getRowCount();
      int inStart = 0,inEnd=0,outStart = 0;
      for (int i = 0; i < rowCount; i++) {
        //读取7:G列数据
        String  cellValue = (String) excelReader.readCellValue(6, i);
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
        sheet.setInRows(parseExcelRow(inRowValue, false));
      }

      if (CollUtil.isNotEmpty(outRowValue)){
        sheet.setOutRows(parseExcelRow(outRowValue, false));
      }
    }else {
      //索引页直接解析
      List<List<Object>> allRow = excelReader.read();
      sheet.setIndexRows(parseExcelRow(allRow,true));
    }

    //标志为已解析
    sheet.setParseFlag(true);
    return sheet ;

  }

  /**
   * 解析多行数据
   * @param rows 多行数据
   * @param index 是否是索引页,索引页需要改变截取原则
   * @return
   */
  private List<ExcelRow> parseExcelRow(List<List<Object>> rows,boolean index){
    List<ExcelRow> rowList = new ArrayList<>();
    rows.stream().forEach(row -> {
      rowList.add(doParseExcelRow(row,index));
    });
    return rowList;
  }

  /**
   * 解析单行数据
   * @param row
   * @param index 是否是索引页的数据
   * @return
   */
  private ExcelRow doParseExcelRow(List<Object> row,boolean index){
    ExcelRow result = new ExcelRow();
    List<String> rowValue = new ArrayList<>();
    if (index){
      //索引页忽略表头的数据,截取0-11列的数据,因为存在很多脏数据
      rowValue = row.stream().skip(1).map(o -> String.valueOf(o)).collect(Collectors.toList());
      result.setTradeCode(rowValue.get(0));
      //服务编码
      result.setServiceCode(rowValue.get(4));
      result.setConsumerName(rowValue.get(6));
      result.setProviderName(rowValue.get(7));
    }else {
      //截取row中7:G列之后的数据
      rowValue = row.stream().skip(7).map(o -> String.valueOf(o)).collect(Collectors.toList());
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
    }
    return result;
  }

  private String[] parseDouble(String value){
    String[] split = new String[2];
    Pattern p = Pattern.compile(Constants.REGEX);
    Matcher matcher = p.matcher(value);
    if (matcher.find()){
      split = matcher.group(0).split(",");
    }
    return split;
  }

  public void compareExcelWithMetadataXml(){

  }

  public static void main(String[] args) {



    System.out.println("Double(10,4)".replaceAll("[^a-zA-Z]","").toLowerCase());
    System.out.println("String(10)".replaceAll("\\D*",""));
    ExcelParse excelParse = new ExcelParse();
    List<ExcelSheet> excelSheets = excelParse.parseExcel(System.getProperty("user.dir") + "/config/服务治理_字段映射_线上信贷系统_V2.0.2.xls");

    excelSheets.forEach(sheet -> {
      System.out.println(sheet);
    });

    Log.info("testrizhi");

  }

}
