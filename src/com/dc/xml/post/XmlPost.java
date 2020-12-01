package com.dc.xml.post;

import com.dc.xml.XmlObject;

import java.util.List;

/**
 * xml处理的后置接口，应对一些特殊改变
 *
 * @author: Administrator
 * @date: 2020-11-30 22:10
 * @version: 1.0
 */
public interface XmlPost {

  /**
   * 后置处理xmlObject节点的方法
   * @param xmlObject
   */
  void invoke(XmlObject xmlObject);
}
