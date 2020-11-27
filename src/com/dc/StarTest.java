package com.dc;

import com.dc.common.Constants;
import com.dc.config.HrConfig;
import com.dc.excel.ExcelParse;
import com.dc.excel.ExcelSheet;
import com.dc.rule.BaseRule;
import com.dc.rule.RuleStrategy;
import com.dc.rule.impl.MetadataRuleStrategy;
import com.dc.sftp.SshUtil;
import com.dc.xml.XmlMetadata;
import com.dc.xml.XmlObject;
import com.dc.xml.XmlType;
import com.dc.xml.XpathParser;
import org.dom4j.Document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author: Administrator
 * @date: 2020-11-18 11:13
 * @version: 1.0
 */
public class StarTest {

  /**
   * 配置文件信息
   */
  public static final HrConfig config = HrConfig.getConfig();

  public static void main(String[] args) {

    /** 三.解析Excel数据 **/
    // 3.解析配置local.excel.services配置的sheet页。
    //  3.1 新建ExcelParse解析器
    ExcelParse excelParse = new ExcelParse();

    //  3.2 遍历配置的需要解析的sheet信息
    Iterator<Map.Entry<String, List<String>>> iterator = config.excelServices.entrySet().iterator();

    //  3.2 保存解析得到的sheet页信息
    List<ExcelSheet> excelSheets = new ArrayList<>();
    while (iterator.hasNext()){
      Map.Entry<String, List<String>> next = iterator.next();
      //文档路径
      String wordPath = next.getKey();
      //需要解析的服务码集合：sheet页名字
      List<String> sheetNames = next.getValue();
      excelSheets.addAll(excelParse.parseExcel(wordPath, sheetNames));
    }

    /** 四.执行后置规则 **/
    // 4.将excel中sys_head、app_head、local_head、business_service的字段与metadata.xml中的进行比较


    /** 五.将Excel数据解析成对应的Xml对象 **/
    XmlMetadata metadata = XmlObject.ofBatch(excelSheets);

    /** 六.生成对应的xml文件 **/
    XpathParser.ofTransferBatch(metadata);
    // 5.生成对应服务的拆组包文件、服务定义文件和服务识别、系统识别文件


    // 生成新增字段的文件metadata_new.xml
    // 生成异常字段的文件metadata_abnormal.xml

    // 生成服务识别文件
    // 生成系统识别文件
  }
}
