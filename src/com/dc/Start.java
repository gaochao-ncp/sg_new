package com.dc;

import com.dc.common.Constants;
import com.dc.config.SgConfig;
import com.dc.sftp.SshUtil;
import com.dc.xml.w3c.MetadataXmlParse;

/**
 * 工具启动类
 *
 * @author: Administrator
 * @date: 2020-11-18 11:14
 * @version: 1.0
 */
public class Start {

  public static void main(String[] args) {
    // 1.下载in/out端metadata.xml
    SshUtil.download();

    // 2.解析in和out端metadata.xml
    MetadataXmlParse metadataXmlParse = new MetadataXmlParse();
    metadataXmlParse.parseMetadataXml(SgConfig.getConfig().localUrl.get(Constants.IN),SgConfig.getConfig().localUrl.get(Constants.OUT));

    // 3.将excel中sys_head、app_head、local_head、business_service的字段与metadata.xml中的进行比较


    // 4.生成对应服务的拆组包文件、服务定义文件和服务识别、系统识别文件

    // 生成新增字段的文件metadata_new.xml
    // 生成异常字段的文件metadata_abnormal.xml

    // 生成服务识别文件
    // 生成系统识别文件
  }
}
