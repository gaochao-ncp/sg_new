package com.dc.xml.deprecated;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.dc.common.CommonUtil;
import com.dc.excel.ExcelRow;
import com.dc.excel.ExcelSheet;
import com.dc.log.Log;
import com.dc.xml.XmlType;

import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;

/**
 * XML文件对象
 *
 * @author Huang.Yong
 */
@Deprecated
public class XMLObject implements Serializable {

    private static final long serialVersionUID = 7702755997734263716L;

    /**
     * 属性列表
     */
    private Map<String, String> attrs = new LinkedHashMap<>();

    /**
     * 子标签集合, Key:tagName, Value:List&lt;XMLObject&gt;
     */
    private Map<String, List<XMLObject>> childTags = new LinkedHashMap<>();

    /**
     * 标签文本内容
     */
    private String content;

    /**
     * 是否根节点
     */
    private boolean rootElement = false;

    /**
     * //todo 是否注释节点.先保留 后续进行优化注释行
     */
    private boolean noteElement = false;

    /**
     * 父级节点
     */
    private XMLObject parent;

    /**
     * 标签名
     */
    private String tagName;

    /**
     * 构建XML对象
     *
     * @param tagName 标签名
     */
    public XMLObject(String tagName) {
        super();
        this.tagName = tagName;
    }

    /**
     * 构建XML对象
     *
     * @param tagName 标签名
     * @param content 标签体
     * @param attrs   标签属性表
     */
    public XMLObject(String tagName, String content, Map<String, String> attrs) {
        super();
        this.tagName = tagName;
        this.content = content;
        this.attrs = attrs;
    }

    /**
     * //todo:
     * 将Excel读取的对象{@link com.dc.excel.ExcelSheet}转换为XMLObject root数据
     * @param sheets
     * @param tagName {@link com.dc.common.Constants#}
     * @return XMLObject实体, 目标对象为null时总是返回null
     */
    public static XMLObject of(String tagName,ExcelSheet... sheets) {
        XMLObject root = new XMLObject(tagName);
        List<ExcelRow> rowList = new ArrayList<>();
        if (XmlType.METADATA.getRootName().equals(tagName)){
            for (ExcelSheet sheet : sheets) {
                if (!CommonUtil.filterSheet(sheet)){
                    continue;
                }
                rowList.addAll(sheet.getInRows());
                rowList.addAll(sheet.getOutRows());
            }
            ofMetadataXml(rowList,root);
        //}else if (Constants.XML_SERVICE.equals(tagName)){
            //生成拆组包文件

       // }else if (Constants.XML_SYSTEMS.equals(tagName)){

        }else {
            //自定义名称，例如<S0200200000510>
            for (ExcelSheet sheet : sheets) {
                List<ExcelRow> outRows = sheet.getOutRows();
                outRows.forEach(row -> {
                    XMLObject child = root.parsePath(root,row.getPath());
                    //child.addAttr(row);
                });
            }
        }
        root.setRootElement(true);
        return root;
    }


    /**
     * 将excel解析的数据转换成xmlObject对象
     * @param rows
     * @param xmlObject
     */
    private static void ofMetadataXml(List<ExcelRow> rows,XMLObject xmlObject) {
        List<XMLObject> childTags = new ArrayList<>();
        rows.stream().forEach(row -> {
            XMLObject child = new XMLObject(row.getTagName());
            //child.addAttr(row);
            childTags.add(child);
        });

        if (CollUtil.isNotEmpty(childTags)){
            Map<String,List<XMLObject>> map = new LinkedHashMap<>(1);
            map.put("metadata",childTags);
            xmlObject.setChildTags(map);
        }
    }



    /**
     * 添加标签属性
     *
     * @param attrName  属性名
     * @param attrValue 属性值
     */
    public void addAttr(String attrName, String attrValue) {
        getAttrs().put(attrName, attrValue);
    }

    /**
     * 添加子标签
     *
     * @param xmlObject 子标签对象
     */
    public void addChildTag(XMLObject xmlObject) {
        Map<String, List<XMLObject>> localSubTags = getChildTags();

        // 验证是否已存在当前标签
        String tagName = xmlObject.tagName;
        List<XMLObject> subTags = localSubTags.computeIfAbsent(tagName, k -> new ArrayList<>());

        // 添加标签
        subTags.add(xmlObject);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof XMLObject)) {
            return false;
        }
        XMLObject xmlObject = (XMLObject) o;
        return rootElement == xmlObject.rootElement &&
                Objects.equals(attrs, xmlObject.attrs) &&
                Objects.equals(childTags, xmlObject.childTags) &&
                Objects.equals(content, xmlObject.content) &&
                Objects.equals(parent, xmlObject.parent) &&
                tagName.equals(xmlObject.tagName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attrs, childTags, content, rootElement, parent, tagName);
    }

    /**
     * 获取指定属性, 如果属性不存在或没有值, 总是返回 null
     *
     * @param attrName 属性名
     * @return String 属性值
     */
    public String getAttr(String attrName) {
        String attrVal = getAttrs().get(attrName);
        return StrUtil.isNotEmpty(attrVal) ? attrVal : "";
    }

    /**
     * 获取指定子标签
     * @param tagName 子标签名
     * @param index   第几个 <i>tagName</i> 指定的子标签
     * @return XMLObject XML 节点对象
     */
    public XMLObject getChildTag(String tagName, int index) {
        List<XMLObject> subTags = getChildTags(tagName);
        if (index >= subTags.size() || index < 0) {
            return null;
        }
        return subTags.get(index);
    }

    /**
     * 获取子标签
     *
     * @param tagName 标签名
     * @return List&lt;XMLObject&gt; 当前标签包含的所有子标签, 总是返回合法的列表对象
     */
    public List<XMLObject> getChildTags(String tagName) {
        return getChildTags().computeIfAbsent(tagName, k -> new ArrayList<>());
    }

    /**
     * 是否包含指定属性名
     *
     * @param attrName 属性名
     * @return boolean true-包含指定属性, false-不包含指定属性
     */
    public boolean hasAttr(String attrName) {
        return (this.getAttrs().containsKey(attrName));
    }

    /**
     * 验证是否包含目标子标签
     *
     * @param subTag 目标子标签
     * @return boolean true-包含, false-不包含
     */
    public boolean hasChildTag(XMLObject subTag) {
        if (null == childTags || subTag == null) {
            return false;
        }

        String subTagName = subTag.getTagName();
        List<XMLObject> list = getChildTags().get(subTagName);
        return (null != list) && list.contains(subTag);
    }

    /**
     * 验证是否包含目标子标签节点
     * @param subTagName 子标签名字
     * @return
     */
    public boolean hasChildTag(XMLObject parent,String subTagName){
        List<XMLObject> list = parent.getChildTags(subTagName);
        return (null != list) && (list.size()>0);
    }

    /**
     * 编辑节点前结构验证, 当前节点不是root节点且父节点不是漂浮状态
     *
     * @param targetParent 目标父节点
     * @return true-验证通过, false-验证失败
     */
    private boolean validationInnerEdit(XMLObject targetParent) {
        if (StrUtil.isBlank(tagName)) {
            Log.info("当前节点未指定有效标签名称");
            return false;
        }

        if (this.isRootElement()) {
            Log.info("根节点不允许被插入到其他节点中");
            return false;
        }

        if (StrUtil.isBlank(targetParent.tagName)) {
            Log.info("目标节点未指定有效标签名称");
            return false;
        }

        if (targetParent.isFloating()) {
            Log.info("目标节点不稳定, 不允许插入其他节点");
            return false;
        }

        return true;
    }


    /**
     * 校验当前节点是否漂浮状态(只能代表当前节点, 而不能代表其上级节点是否也是漂浮状态)
     *
     * @return boolean true-漂浮状态, false-不是漂浮状态
     */
    private boolean isFloating() {
        // 不是根节点, 并且没有相应的父节点时
        return !this.isRootElement() && null == this.getParent();
    }

    /**
     * 追加到result中
     *
     * @param xmlObject    xml节点元素
     * @param childTagName 目标节点名称
     * @param result       用于保存节点列表
     */
    private void appendTag(XMLObject xmlObject, String childTagName, List<XMLObject> result) {
        if (StrUtil.equals(xmlObject.getTagName(), childTagName)) {
            result.add(xmlObject);
        }
    }

    /**
     * 获取所有子节点
     *
     * @param xmlObject    节点对象
     * @param childTagName 目标节点名称
     * @param result       用于保存节点列表
     */
    private void getAllChild(XMLObject xmlObject, String childTagName, List<XMLObject> result) {
        appendTag(xmlObject, childTagName, result);

        // 验证是否还有后代元素
        Map<String, List<XMLObject>> children = xmlObject.getChildTags();
        if (0 >= children.size()) {
            return;
        }

        // 遍历所有后代标签
        for (Entry<String, List<XMLObject>> child : children.entrySet()) {
            List<XMLObject> descendants = child.getValue();

            if (0 >= descendants.size()) {
                appendTag(xmlObject, childTagName, result);
                continue;
            }

            for (XMLObject descendant : descendants) {
                getAllChild(descendant, childTagName, result);
            }
        }
    }

    /**
     * 解析字段路径配置
     * @param path 字段路径
     * @return 目标XML节点
     */
    private XMLObject parsePath(XMLObject target,String path) {

        if (StrUtil.isBlank(path)){
            return this;
        }

        int tagIndex = 0;
        //根据分隔符 / 来进行分隔
        String[] paths = path.split("\\/");
        Log.info("解析路径: " + path);
        for (int i = 0; i < paths.length; i++) {
            String tagName = paths[i];
            if (hasChildTag(target,tagName)){
                target = target.getChildTag(tagName,0);
            }else {
                //不存在子节点，创建并放置于当前对象中
                XMLObject child = XMLParser.createNode(tagName, null, null);
                target.getChildTags().put(tagName,ListUtil.toLinkedList(child));
                if (i == paths.length-1) {
                    target = child;
                }else {
                    child.parsePath(child,path.replace(tagName+"/",""));
                }
            }
        }
        return target;
    }

    /**
     * 检查是否包含有效子标签
     * @return 包含有效子标签返回true, 否则返回false
     */
    public boolean hasEffectiveChildren() {
        Map<String, List<XMLObject>> childTags = getChildTags();
        if (null == childTags || 0 == childTags.size()){
            return false;
        }

        for (Entry<String, List<XMLObject>> me : childTags.entrySet()){
            if (CollUtil.isNotEmpty(me.getValue())){
                return true;
            }
        }
        return false;
    }

    /**
     * 获取标签属性列表
     *
     * @return Map&lt;String,String&gt; 标签属性集合, Key:属性名, Value:属性值
     */
    public Map<String, String> getAttrs() {
        if (null == this.attrs){
            this.attrs = new LinkedHashMap<>();
        }
        return this.attrs;
    }
    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isRootElement() {
        return rootElement;
    }

    public void setRootElement(boolean rootElement) {
        this.rootElement = rootElement;
    }

    public XMLObject getParent() {
        return parent;
    }

    public void setParent(XMLObject parent) {
        this.parent = parent;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public boolean isNoteElement() {
        return noteElement;
    }

    public void setNoteElement(boolean noteElement) {
        this.noteElement = noteElement;
    }

    /**
     * 获取所有子标签
     *
     * @return Map&lt;String,List&lt;XMLObject&gt;&gt; 子标签集合, Key:标签名,
     * Value:当前标签下所有与标签名关联的一级子标签
     */
    public Map<String, List<XMLObject>> getChildTags() {
        if (null == this.childTags) {
            this.childTags = new LinkedHashMap<>();
        }
        return this.childTags;
    }

    public void setChildTags(Map<String, List<XMLObject>> childTags) {
        this.childTags = childTags;
    }
}
