package com.dc.common;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.config.HrConfig;
import com.dc.excel.ExcelSheet;
import com.dc.xml.XmlObject;
import com.dc.xml.post.XmlPost;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Sheet;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            || Constants.SHEET_APP_HEAD.equals(sheetName)
            || Constants.SHEET_COMMON_FILED.equals(sheetName)){
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
   * 生成in端拆包文件。输入字段
   * @param consumerCode
   * @param serviceCode
   * @return
   */
  public static String generateFilePathInputIn(String consumerCode , String serviceCode){
    return HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/channel_"+consumerCode+"_service_"+serviceCode+".xml";
  }

  /**
   * 生成in端组包文件。输出字段
   * @param consumerCode
   * @param serviceCode
   * @return
   */
  public static String generateFilePathOutputIn(String consumerCode , String serviceCode){
    return HrConfig.getConfig().LOCAL_URL.get(Constants.IN)+"/service_"+serviceCode+"_system_"+consumerCode+".xml";
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

  /**
   * 判断字符是否是数字
   * @param str
   * @return
   */
  public static boolean isNumeric(String str){
    Pattern pattern = Pattern.compile(Constants.NUMERIC_REGEX);
    Matcher isNum = pattern.matcher(str);
    if (!isNum.matches()) {
      return false;
    }
    return true;
  }

  /**
   * 截取Xpath匹配表达式中的属性列表，以map形式返回
   * 例如:
   * <p>
   *   String input = "/systems/system[@id='TEST' and @name='高潮']/service[@type='String' and @length='15']";
   * </p>
   * 目前
   * @param Xpath 需要匹配的Xpath路径
   * @return
   */
  public static Map<String, String> matcher(String Xpath){
    Map<String, String> attrs = new LinkedHashMap<>();
    String str=matcher(Constants.ATTRS_REGX,Xpath);
    if (StrUtil.isEmpty(str)){
      return attrs;
    }
    //通过and分隔，属性列表
    String[] ands = str.split("and");
    for (String and : ands) {
      //去除空格
      and = and.trim();
      //匹配@开头的任意字母
      String key_regx = "(?<=@)[^=]+";
      String key = matcher(key_regx, and);
      //匹配'开头的不包含'的属性
      String value_regx = "(?<==')[^']*";
      String value = matcher(value_regx, and);
      //只有节点和属性名不为空的才放入列表中
      if (StrUtil.isNotEmpty(key) && StrUtil.isNotEmpty(value)){
        //将反转的属性置为正常
        if (Constants.NODE_EXPRESSION_VALUE_REVERSE.equals(value)){
          value = Constants.NODE_EXPRESSION_VALUE;
        }
        attrs.put(key, value);
      }
    }
    log.info("解析Xpath表达式中的属性为【{}】",attrs.toString());
    return attrs;
  }

  /**
   * 通过正则表达式匹配字符串
   * @param regx
   * @param input
   * @return
   */
  public static String matcher(String regx,String input){
    Pattern p = Pattern.compile(regx);
    Matcher m = p.matcher(input);
    String result = "";
    while (m.find()){
      //取最后一个属性
      result = m.group();
    }
    return result;
  }

  /**
   * 返回标准化的Xpath表达式
   * @param xpath
   * @return
   */
  public static String standardXpath(String xpath){
    if (StrUtil.isBlank(xpath)){
      log.warn("传入的Xpath为空，无法解析");
    }

    if (!xpath.startsWith("/")){
      //log.warn("路径【"+xpath+"】开头没有【/】,自动补全/");
      xpath += "/";
    }

    if (xpath.endsWith("/")){
      //去除最后面得/号，防止通过Xpath查询的时候报错
      //log.warn("路径【"+xpath+"】结尾存在【/】,自动忽略/");
      xpath = xpath.substring(0,xpath.length()-1);
    }
    return xpath;
  }

  /**
   * 使用正则去除字符串中的 [@id='test' and @type='String'] 属性字段
   * @param input
   * @return
   */
  public static String deleteAttr(String input){
    return input.replaceAll(Constants.ATTRS_REGX,"");
  }

  /**
   * 创建文档对象
   * @param xmlObject
   * @return
   */
  public static Document createDocument(XmlObject xmlObject){
    Document newDoc = DocumentHelper.createDocument();
    Element root = newDoc.addElement(xmlObject.getTagName());
    //根节点属性赋值
    xmlObject.attributeValue(root);
    return newDoc;
  }

  /**
   * 获取所有接口的实现类
   * @return
   */
  public static List<Class> getAllInterfaceAchieveClass(Class clazz){
    ArrayList<Class> list = new ArrayList<>();
    //判断是否是接口
    if (clazz.isInterface()) {
      try {
        ArrayList<Class> allClass = getAllClassByPath(clazz.getPackage().getName());
        /**
         * 循环判断路径下的所有类是否实现了指定的接口
         * 并且排除接口类自己
         */
        for (int i = 0; i < allClass.size(); i++) {

          //排除抽象类
          if(Modifier.isAbstract(allClass.get(i).getModifiers())){
            continue;
          }
          //判断是不是同一个接口
          if (clazz.isAssignableFrom(allClass.get(i))) {
            if (!clazz.equals(allClass.get(i))) {
              list.add(allClass.get(i));
            }
          }
        }
      } catch (Exception e) {
        System.out.println("出现异常");
      }
    }
    return list;
  }

  /**
   * 从指定路径下获取所有类
   * @return
   */
  public static ArrayList<Class> getAllClassByPath(String packagename){
    ArrayList<Class> list = new ArrayList<>();
    ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    String path = packagename.replace('.', '/');
    try {
      ArrayList<File> fileList = new ArrayList<>();
      Enumeration<URL> enumeration = classLoader.getResources(path);
      while (enumeration.hasMoreElements()) {
        URL url = enumeration.nextElement();
        fileList.add(new File(url.getFile()));
      }
      for (int i = 0; i < fileList.size(); i++) {
        list.addAll(findClass(fileList.get(i),packagename));
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return list;
  }

  /**
   * 如果file是文件夹，则递归调用findClass方法，或者文件夹下的类
   * 如果file本身是类文件，则加入list中进行保存，并返回
   * @param file
   * @param packagename
   * @return
   */
  private static ArrayList<Class> findClass(File file,String packagename) {
    ArrayList<Class> list = new ArrayList<>();
    if (!file.exists()) {
      return list;
    }
    File[] files = file.listFiles();
    for (File file2 : files) {
      if (file2.isDirectory()) {
        assert !file2.getName().contains(".");//添加断言用于判断
        ArrayList<Class> arrayList = findClass(file2, packagename+"."+file2.getName());
        list.addAll(arrayList);
      }else if(file2.getName().endsWith(".class")){
        try {
          //保存的类文件不需要后缀.class
          list.add(Class.forName(packagename + '.' + file2.getName().substring(0,
                  file2.getName().length()-6)));
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
      }
    }
    return list;
  }

  /**
   * 处理路径
   * @param path
   * @return
   */
  public static String regexArray(String path) {
    Pattern compile = Pattern.compile(Constants.REGEX_ARRAY);
    Matcher matcher = compile.matcher(path);
    while (matcher.find()){
      //匹配的字符串
      String group = matcher.group();
      //去除首位的 / ，保证分割后只有两个
      String clear = group.substring(group.indexOf("/")+1,group.lastIndexOf("/"));
      String tagName = clear.split("/")[1];
      path = path.replace(group,"/array/"+tagName+"[@bemiddle='true']/struct[@metadataid='"+tagName+"' and @type='array' and @is_struct='false']");
    }
    return path;
  }

  public static void main(String[] args) {

    String s = "struct[@metadataid='AuthTellerInfo' and @type='array' and @is_struct='false']";
    Map<String, String> matcher = matcher(s);
    System.out.println(matcher);
  }

}
