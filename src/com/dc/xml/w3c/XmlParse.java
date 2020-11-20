package com.dc.xml.w3c;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.XmlUtil;
import com.dc.common.Constants;
import com.dc.unpack.UnpackingChild;
import com.dc.unpack.UnpackingRoot;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * xml文件解析
 *
 * @author: Administrator
 * @date: 2020-11-18 11:46
 * @version: 1.0
 */
public abstract class XmlParse {

  /**
   * 生成xml文件
   * @param doc 文档对象
   * @param absolutePath 绝对路径
   */
  public void createFile(Document doc,String absolutePath){
    XmlUtil.toFile(doc,absolutePath);
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
  public Document createUnpackingXmlDynamic(String rootElementName,UnpackingRoot unpacking){
    Document document = doCreateUnpackingXmlDynamic(rootElementName, unpacking);
    //不显示standalone="no"
    document.setXmlStandalone(true);
    return document;
  }

  /**
   * 动态生成xml文件的document对象
   * @param rootElementName
   * @param unpacking
   * @return
   */
  public abstract Document doCreateUnpackingXmlDynamic(String rootElementName,UnpackingRoot unpacking);

  /**
   * 给元素添加子节点
   * @param element
   * @param list
   */
  protected void appendChild(Element element, List<UnpackingChild> list){
    list.forEach(child -> {
      if (StrUtil.isBlank(child.getTagName())){
        return;
      }
      Element root = XmlUtil.appendChild(element,child.getTagName());
      if (StrUtil.isNotBlank(child.getType())){
        root.setAttribute(Constants.NODE_TYPE,child.getType());
      }

      if (StrUtil.isNotBlank(child.getLength())){
        root.setAttribute(Constants.NODE_LENGTH,child.getLength());
      }

      if (StrUtil.isNotBlank(child.getChineseName())){
        root.setAttribute(Constants.NODE_CHINESE_NAME,child.getChineseName());
      }

      if (StrUtil.isNotBlank(child.getMetadataId())){
        root.setAttribute(Constants.NODE_METADATA_ID,child.getMetadataId());
      }

      if (StrUtil.isNotBlank(child.getIsStruct())){
        root.setAttribute(Constants.NODE_IS_STRUCT,child.getIsStruct());
      }

      if (StrUtil.isNotBlank(child.getMode())){
        root.setAttribute(Constants.NODE_MODE,child.getMode());
      }

      if (StrUtil.isNotBlank(child.getExpression())){
        root.setAttribute(Constants.NODE_EXPRESSION,child.getExpression());
      }

      if (StrUtil.isNotBlank(child.getId())){
        root.setAttribute(Constants.NODE_ID,child.getId());
      }

      if (StrUtil.isNotBlank(child.getEncode())){
        root.setAttribute(Constants.NODE_ENCODE,child.getEncode());
      }

      if (StrUtil.isNotBlank(child.getValue())){
        root.setAttribute(Constants.NODE_VALUE,child.getValue());
      }

      if (StrUtil.isNotBlank(child.getScale())){
        root.setAttribute(Constants.NODE_SCALE,child.getScale());
      }

      if (CollUtil.isNotEmpty(child.getChildList())){
        appendChild(root,child.getChildList());
      }
    });
  }

  //todo 追加生成xml,重新生成xml:追加模式 1.从服务器上拉取xml文件并对比差异,将没有的进行追加 ;新生成模式:重新生成
  public abstract void appendXml(boolean flag);

  public static void main(String[] args) {
    UnpackingRoot root = new UnpackingRoot();
    root.setRootName("metadata");
    List<UnpackingChild> childList = new ArrayList<>();
    UnpackingChild child = new UnpackingChild();
    child.setChineseName("交易柜员号");
    child.setType("string");
    child.setLength("15");
    child.setTagName("AuthFlag");
    childList.add(child);

    UnpackingChild child1 = new UnpackingChild();
    child1.setChineseName("交易柜员号");
    child1.setType("string");
    child1.setLength("15");
    child1.setTagName("AuthFlag");
    childList.add(child1);
    root.setChildList(childList);




    XmlParse parse = new MetadataXmlParse();
    Document document = parse.createUnpackingXmlDynamic(root);
    //不显示standalone属性!!
    document.setXmlStandalone(true);
    String format = XmlUtil.format(document);
    System.out.println(format);


    Map<String, String> map =new HashMap<>();
    map.put("ServiceCode","");

    Document document1 = XmlUtil.beanToXml(root, "metadata");
    System.out.println(XmlUtil.format(document1));
    Document metadata = XmlUtil.createXml("metadata");

//    Element root = XmlUtil.getRootElement(metadata);
//    root.setAttribute("test","测试属性");
//
//    Element element = XmlUtil.appendChild(root,"ServiceCode");
//    element.setAttribute("type","String");
//    element.setAttribute("length","15");
//    element.setAttribute("chinese_name","服务代码");



    XmlUtil.toFile(document,"E:/metadata.xml");

  }
}
