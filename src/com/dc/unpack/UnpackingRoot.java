package com.dc.unpack;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * 拆组包root节点
 * @author: Administrator
 * @date: 2020-11-19 10:05
 * @version: 1.0
 */
public class UnpackingRoot {

  private String rootName;

  private List<UnpackingChild> childList;

  public String getRootName() {
    return rootName;
  }

  public void setRootName(String rootName) {
    this.rootName = rootName;
  }

  public List<UnpackingChild> getChildList() {
    return childList;
  }

  public void setChildList(List<UnpackingChild> childList) {
    this.childList = childList;
  }
}
