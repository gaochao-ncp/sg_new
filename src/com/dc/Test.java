package com.dc;

import cn.hutool.core.util.XmlUtil;
import com.dc.config.SgConfig;
import com.dc.excel.ExcelParse;
import com.dc.excel.ExcelSheet;
import com.dc.unpack.UnpackFactory;
import com.dc.unpack.UnpackingRoot;
import com.dc.unpack.metadata.MetadataUnpackFactory;
import com.dc.xml.w3c.MetadataXmlParse;
import com.dc.xml.w3c.XmlParse;
import org.w3c.dom.Document;

import java.util.List;

/**
 * @author: Administrator
 * @date: 2020-11-18 11:13
 * @version: 1.0
 */
public class Test {

  static SgConfig config = SgConfig.getConfig();

  public static void main(String[] args) {

    //解析excel
    ExcelParse excelParse = new ExcelParse();

    List<ExcelSheet> excelSheets = excelParse.parseExcel(System.getProperty("user.dir") + "/config/服务治理_字段映射_线上信贷系统_V2.0.2.xls");

    UnpackFactory metadataFactory = new MetadataUnpackFactory();
    UnpackingRoot root = metadataFactory.parseAll(excelSheets);

    root.getChildList().forEach(l -> {
      System.out.println(l.toString());
    });

    //解析 metadata.xml
    XmlParse parse = new MetadataXmlParse();
    Document doc = parse.createUnpackingXmlDynamic(root);
    XmlUtil.toFile(doc,"E:\\metadata.xml");
  }
}
