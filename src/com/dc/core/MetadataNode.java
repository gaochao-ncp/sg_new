package com.dc.core;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.Constants;
import org.dom4j.Element;

import java.util.Map;

/**
 * metadata中的节点信息
 * @author: Administrator
 * @date: 2020-11-17 22:57
 * @version: 1.0
 */
public class MetadataNode {

  private static final Log log = LogFactory.get(MetadataNode.class);

  private String nodeName;
  private String type;
  private String length;
  private String chineseName;
  /** 精度:当type=double的时候存在 **/
  private String scale;
  private String comment;
  private boolean commentFlag = false;

  /**
   * 对比两个节点的值.默认传入out端的节点,因为要输出信息
   * @param nodeName
   * @param outNode
   */
  public void parseNode(String nodeName,MetadataNode outNode) {
    if (ObjectUtil.isNull(outNode)){
      return;
    }
    StringBuffer sb = new StringBuffer();
    sb.append(parseNodeValue(Constants.NODE_CHINESE_NAME,outNode.getChineseName(),this.chineseName));
    sb.append(parseNodeValue(Constants.NODE_TYPE,outNode.getType(),this.type));
    sb.append(parseNodeValue(Constants.NODE_LENGTH,outNode.getLength(),this.length));
    sb.append(parseNodeValue(Constants.NODE_SCALE,outNode.getLength(),this.scale));
    if (StrUtil.isNotBlank(sb.toString())){
      log.warn("["+nodeName+"]对比结果为:"+sb.toString());
    }
  }

  public String parseNodeValue(String node,String v1,String v2){
    if (StrUtil.isBlank(v1) && StrUtil.isBlank(v2)){
      return "";
    }
    if (!v1.equals(v2)){
      return "节点"+node+"值不同[out端:"+v1+",in端:"+v2+"] ";
    }
    return "";
  }

  public void setAttr(Element node){
    if (StrUtil.isNotBlank(type)){
      node.addAttribute(Constants.NODE_TYPE,type);
    }
    if (StrUtil.isNotBlank(length)){
      node.addAttribute(Constants.NODE_LENGTH,length);
    }
    if (StrUtil.isNotBlank(scale)){
      node.addAttribute(Constants.NODE_SCALE,scale);
    }
    if (StrUtil.isNotBlank(chineseName)){
      node.addAttribute(Constants.NODE_CHINESE_NAME,chineseName);
    }
  }

  public void setAttr(Map<String, String> attrs){
    setType(attrs.get(Constants.NODE_TYPE));
    setLength(attrs.get(Constants.NODE_LENGTH));
    setScale(attrs.get(Constants.NODE_SCALE));
    setChineseName(attrs.get(Constants.NODE_CHINESE_NAME));
  }

  public String getNodeName() {
    return nodeName;
  }

  public void setNodeName(String nodeName) {
    this.nodeName = nodeName;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getLength() {
    return length;
  }

  public void setLength(String length) {
    this.length = length;
  }

  public String getChineseName() {
    return chineseName;
  }

  public void setChineseName(String chineseName) {
    this.chineseName = chineseName;
  }

  public String getScale() {
    return scale;
  }

  public void setScale(String scale) {
    this.scale = scale;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public boolean isCommentFlag() {
    return commentFlag;
  }

  public void setCommentFlag(boolean commentFlag) {
    this.commentFlag = commentFlag;
  }
}
