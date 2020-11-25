package com.dc;

import com.dc.config.HrConfig;
import com.dc.excel.ExcelParse;
import com.dc.excel.ExcelSheet;
import com.dc.xml.XmlObject;
import com.dc.xml.XmlType;
import com.dc.xml.XpathParser;
import org.dom4j.Document;

/**
 * @author: Administrator
 * @date: 2020-11-18 11:13
 * @version: 1.0
 */
public class Test {

  static HrConfig config = HrConfig.getConfig();

  public static void main(String[] args) {

    //解析excel
    ExcelParse excelParse = new ExcelParse();

    ExcelSheet excelSheets = excelParse.parseExcel(System.getProperty("user.dir") + "/config/服务治理_字段映射_线上信贷系统_V2.0.2.xls",
            "1500300000605");

//    UnpackFactory metadataFactory = new MetadataUnpackFactory();
//    UnpackingRoot root = metadataFactory.parseAll(excelSheets);
//
//    root.getChildList().forEach(l -> {
//      System.out.println(l.toString());
//    });

    //解析 metadata.xml
//    XMLObject metadata = XMLObject.of("metadata", excelSheets.get(0),
//            excelSheets.get(1), excelSheets.get(2));

    XmlObject service = XmlObject.of(XmlType.SERVICE_DEFINITION, excelSheets);

    Document dom4j = XpathParser.createDom4j(service);

    XpathParser.transfer("E:/metadata.xml",dom4j);

    System.out.println(service);

    //XMLParser.transfer(metadata,new File("E:/metadata.xml"));
    //XMLParser.transfer(service,new File("E:/service.xml"));
  }
}
