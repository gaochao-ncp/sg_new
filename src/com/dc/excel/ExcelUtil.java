package com.dc.excel;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.Constants;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * excel解析工具类
 *
 * @author: Administrator
 * @date: 2020-11-26 22:12
 * @version: 1.0
 */
public class ExcelUtil {

  private static final Log log = LogFactory.get(ExcelUtil.class);

  /**
   * 简单的workbook对象的缓存
   */
  private static Map<String, Workbook> WB_CACHE = new ConcurrentHashMap<>();

  /**
   * 读取excel中的sheet页，根据名称获取
   * @param absoluteFilePath
   * @param sheetName
   * @return
   */
  public static Sheet readExcel(String absoluteFilePath,String sheetName){
    Workbook sheets = readExcel(absoluteFilePath);
    return sheets.getSheet(sheetName);
  }

  /**
   * 读取整个excel
   * @param absoluteFilePath 文件绝对路径
   * @return
   */
  public static Workbook readExcel(String absoluteFilePath){
    Workbook wb = null;
    if(StrUtil.isBlank(absoluteFilePath)){
      return null;
    }

    //先去缓存容器中查找
    Workbook wbCache = WB_CACHE.get(absoluteFilePath);
    if (ObjectUtil.isNotNull(wbCache)){
      return wbCache;
    }

    String suffix = absoluteFilePath.substring(absoluteFilePath.lastIndexOf("."));
    try (InputStream is = new FileInputStream(absoluteFilePath)){
      if(".xls".equals(suffix)){
        wb = new HSSFWorkbook(is);
      }else if(".xlsx".equals(suffix)){
        wb = new XSSFWorkbook(is);
      }
    } catch (FileNotFoundException e) {
      log.error(e);
    } catch (IOException e) {
      log.error(e);
    }
    //放入缓存容器中
    WB_CACHE.put(absoluteFilePath,wb);
    return wb;
  }


  /**
   * 获取cell中的值并返回String类型
   *
   * @param cell
   * @return String类型的cell值
   */
  public static String getCellValue(Cell cell) {
    String cellValue = "";
    if (null != cell) {
      // 以下是判断数据的类型
      switch (cell.getCellType()) {
        // 数字
        case NUMERIC:
            // 判断是否为日期类型
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
              Date date = cell.getDateCellValue();
              cellValue = DateUtil.format(date, DatePattern.PURE_DATE_PATTERN);
            } else {
              // 有些数字过大，直接输出使用的是科学计数法： 2.67458622E8 要进行处理
              DecimalFormat df = new DecimalFormat("####.####");
              cellValue = df.format(cell.getNumericCellValue());
            }
          break;
        // 字符串
        case STRING:
          cellValue = cell.getStringCellValue();
          break;
        // Boolean
        case BOOLEAN:
          cellValue = cell.getBooleanCellValue() + "";
          break;
        // 公式
        case FORMULA:
          try {
            // 如果公式结果为字符串
            cellValue = String.valueOf(cell.getStringCellValue());
          } catch (IllegalStateException e) {
            // 判断是否为日期类型
            if (HSSFDateUtil.isCellDateFormatted(cell)) {
              Date date = cell.getDateCellValue();
              cellValue = DateUtil.format(date, DatePattern.PURE_DATE_PATTERN);
            } else {
              FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper()
                      .createFormulaEvaluator();
              evaluator.evaluateFormulaCell(cell);
              // 有些数字过大，直接输出使用的是科学计数法： 2.67458622E8 要进行处理
              DecimalFormat df = new DecimalFormat("####.####");
              cellValue = df.format(cell.getNumericCellValue());
            }
          }
//              //直接获取公式
//              cellValue = cell.getCellFormula() + "";
          break;
        // 空值
        case BLANK:
          cellValue = "";
          break;
        // 故障
        case ERROR:
          cellValue = "非法字符";
          break;
        default:
          cellValue = "未知类型";
          break;
      }
    }
    return cellValue;
  }

  /**
   * 判断单元格是否标注删除线
   * @param sheet
   * @param rowNum 行号
   * @param cellNum 列号
   * @return
   */
  public static boolean verifyDelete(Sheet sheet,int rowNum,int cellNum){
    Cell cell = sheet.getRow(rowNum).getCell(cellNum);
    return verifyDelete(cell);
  }

  /**
   * 判断单元格是否标注删除线
   * @param cell
   * @return
   */
  public static boolean verifyDelete(Cell cell){
    int fontIndexAsInt = cell.getCellStyle().getFontIndexAsInt();
    Sheet sheet = cell.getSheet();
    Font fontAt = sheet.getWorkbook().getFontAt(fontIndexAsInt);
    if (fontAt.getStrikeout()){
      log.info("sheet页【"+sheet.getSheetName()+"】中第"+(cell.getRowIndex()+1)+"行，第"+(cell.getColumnIndex()+1)+"列的字段【"+cell.getStringCellValue()+"】添加了删除线");
    }
    return fontAt.getStrikeout();
  }

  /**
   * 读取输入字段
   * @param sheet
   * @return
   */
  public static List<List<String>> readIn(Sheet sheet){
    return readIn(read(sheet, 7));
  }

  /**
   * 读取输出字段
   * @param rows 从第七行开始截取的数据
   * @return
   */
  public static List<List<String>> readIn(List<List<String>> rows){
    List<List<String>> in = new ArrayList<>();
    if (CollUtil.isNotEmpty(rows)){
      for (int i = 0; i < rows.size(); i++) {
        List<String> list = rows.get(i);
        if (list.contains(Constants.OUT_CN)){
          break;
        }
        //输入节点
        in.add(list);
      }
    }
    return in;
  }

  /**
   * 读取输入字段
   * @param sheet
   * @return
   */
  public static List<ExcelRow> readInRow(Sheet sheet){
    return readInRow(readRow(sheet, 7));
  }

  /**
   * 读取输出字段
   * @param rows 从第七行开始截取的数据
   * @return
   */
  public static List<ExcelRow> readInRow(List<Row> rows){
    List<ExcelRow> in = new ArrayList<>();
    if (CollUtil.isNotEmpty(rows)){
      for (int i = 0; i < rows.size(); i++) {
        Row row = rows.get(i);
        List<String> list = readRow(row);
        if (list.contains(Constants.OUT_CN)){
          break;
        }
        //输入节点
        in.add(readExcelRow(row));
      }
    }
    return in;
  }

  /**
   * 读取输出字段
   * @param sheet
   * @return
   */
  public static List<List<String>> readOut(Sheet sheet){
    return readOut(read(sheet,7));
  }

  /**
   * 读取输出字段
   * @param rows
   * @return
   */
  public static List<List<String>> readOut(List<List<String>> rows){
    List<List<String>> out = new ArrayList<>();
    if (CollUtil.isNotEmpty(rows)) {
      boolean outFlag = false;
      for (int i = 0; i < rows.size(); i++) {
        List<String> list = rows.get(i);
        if (outFlag) {
          //输出节点
          out.add(list);
        }
        if (list.contains(Constants.OUT_CN)) {
          outFlag =true;
          continue;
        }
      }
    }
    return out;
  }

  /**
   * 读取输出字段
   * @param sheet
   * @return
   */
  public static List<ExcelRow> readOutRow(Sheet sheet){
    return readOutRow(readRow(sheet,7));
  }

  /**
   * 读取输出字段
   * @param rows
   * @return
   */
  public static List<ExcelRow> readOutRow(List<Row> rows){
    List<ExcelRow> out = new ArrayList<>();
    if (CollUtil.isNotEmpty(rows)) {
      boolean outFlag = false;
      for (int i = 0; i < rows.size(); i++) {
        Row row = rows.get(i);
        List<String> list = readRow(row);
        if (outFlag) {
          //输出节点
          out.add(readExcelRow(row));
        }
        if (list.contains(Constants.OUT_CN)) {
          outFlag =true;
          continue;
        }
      }
    }
    return out;
  }

  /**
   * 解析单行数据
   * @param row
   * @return
   */
  public static List<String> readRow(Row row){
    //只读到M列的数据 M->13列
    List<String> cellValue = new ArrayList<>();
    for (int i = 0; i <= Constants.M; i++) {
      Cell cell = row.getCell(i);
      cellValue.add(getCellValue(cell));
    }
    return cellValue;
  }

  /**
   * 读取工作簿中指定的Sheet全部行
   * @param sheet
   * @return
   */
  public static List<List<String>> read(Sheet sheet){
    return read(sheet,0,Integer.MAX_VALUE);
  }

  public static List<List<String>> read(Sheet sheet,int startRowIndex){
    return read(sheet,startRowIndex,Integer.MAX_VALUE);
  }

  /**
   * 读取工作簿中指定的Sheet
   *
   * @param startRowIndex 起始行（包含，从0开始计数）
   * @param endRowIndex   结束行（包含，从0开始计数）
   * @return 行的集合，一行使用List表示
   */
  public static List<List<String>> read(Sheet sheet,int startRowIndex, int endRowIndex) {
    List<List<String>> resultList = new ArrayList<>();

    // 读取起始行（包含）
    startRowIndex = Math.max(startRowIndex, sheet.getFirstRowNum());
    // 读取结束行（包含）
    endRowIndex = Math.min(endRowIndex, sheet.getLastRowNum());
    boolean isFirstLine = true;
    List rowList;
    for (int i = startRowIndex; i <= endRowIndex; i++) {
      rowList = readRow(sheet.getRow(i));
      if (CollUtil.isNotEmpty(rowList)) {
        if (null == rowList) {
          rowList = new ArrayList<>(0);
        }
        if (isFirstLine) {
          isFirstLine = false;
        }
        resultList.add(rowList);
      }
    }
    return resultList;
  }

  /**
   * 读取工作簿中指定的Sheet全部行
   * @param sheet
   * @return
   */
  public static List<Row> readRow(Sheet sheet){
    return readRow(sheet,0,Integer.MAX_VALUE);
  }

  public static List<Row> readRow(Sheet sheet,int startRowIndex){
    return readRow(sheet,startRowIndex,Integer.MAX_VALUE);
  }

  /**
   * 读取工作簿中指定的Sheet
   *
   * @param startRowIndex 起始行（包含，从0开始计数）
   * @param endRowIndex   结束行（包含，从0开始计数）
   * @return 行的集合，一行使用List表示
   */
  public static List<Row> readRow(Sheet sheet,int startRowIndex, int endRowIndex) {
    List<Row> resultList = new ArrayList<>();
    // 读取起始行（包含）
    startRowIndex = Math.max(startRowIndex, sheet.getFirstRowNum());
    // 读取结束行（包含）
    endRowIndex = Math.min(endRowIndex, sheet.getLastRowNum());
    boolean isFirstLine = true;
    Row row;
    for (int i = startRowIndex; i <= endRowIndex; i++) {
      row = sheet.getRow(i);
      if (ObjectUtil.isNotEmpty(row)) {
        if (isFirstLine) {
          isFirstLine = false;
        }
        resultList.add(row);
      }
    }
    return resultList;
  }

  /**
   * 解析单行数据
   * @param row
   * @return
   */
  public static ExcelRow readExcelRow(Row row){
    //只读到G列->列的数据 G->7列;M->13列
    ExcelRow excelRow = new ExcelRow();
    excelRow.setTagName(getCellValue(row.getCell(7)));
    String type = getCellValue(row.getCell(8)).replaceAll("[^a-zA-Z]", "").toLowerCase();
    excelRow.setType(type);
    if (double.class.getName().equals(type)){
      String[] value = ExcelUtil.parseDouble(getCellValue(row.getCell(8)));
      excelRow.setLength(value[0]);
      excelRow.setScale(value[1]);
    }else {
      excelRow.setLength(getCellValue(row.getCell(8)).replaceAll("\\D*",""));
    }
    excelRow.setChineseName(getCellValue(row.getCell(9)));
    excelRow.setPath(getCellValue(row.getCell(10)));
    excelRow.setDeleteFlag(verifyDelete(row.getCell(7)));
    return excelRow;
  }


  /**
   * 解析type为double时候的length和scale字段
   * @param value
   * @return
   */
  public static String[] parseDouble(String value){
    String[] split = new String[2];
    Pattern p = Pattern.compile(Constants.REGEX);
    Matcher matcher = p.matcher(value);
    if (matcher.find()){
      split = matcher.group(0).split(",");
    }
    return split;
  }

}

