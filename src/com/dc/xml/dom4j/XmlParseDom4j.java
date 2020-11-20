package com.dc.xml.dom4j;

import com.dc.unpack.UnpackingChild;
import com.dc.unpack.UnpackingRoot;
import org.dom4j.Document;

import java.util.List;

/**
 * @author: Administrator
 * @date: 2020-11-20 21:16
 * @version: 1.0
 */
public abstract class XmlParseDom4j {

  /**
   * 生成xml文件
   * @param doc 文档对象
   * @param absolutePath 绝对路径
   */
  public void createFile(Document doc, String absolutePath){


  }

  /**
   * 动态生成拆组包文件
   * @param unpacking
   * @return
   */
  public Document createUnpackingXmlDynamic(UnpackingRoot unpacking) {
    return createUnpackingXmlDynamic("",unpacking);
  }

  /**
   * 动态生成拆组包文件,每一种配置文件有单独的生成策略
   * @param rootElementName
   * @param unpacking
   * @return
   */
  public Document createUnpackingXmlDynamic(String rootElementName, UnpackingRoot unpacking){
    Document document = doCreateUnpackingXmlDynamic(rootElementName, unpacking);
    return document;
  }

  /**
   * 动态生成xml文件的document对象
   * @param rootElementName
   * @param unpacking
   * @return
   */
  public abstract Document doCreateUnpackingXmlDynamic(String rootElementName, UnpackingRoot unpacking);

  /**
   * 给元素添加子节点
   * @param element
   * @param list
   */
  protected void appendChild(org.dom4j.Element element, List<UnpackingChild> list){

  }

}
