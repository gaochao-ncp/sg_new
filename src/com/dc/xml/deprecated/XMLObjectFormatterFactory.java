package com.dc.xml.deprecated;


import com.dc.xml.deprecated.impl.DefaultXMLObjectFormatter;

/**
 * {@link XMLObject}格式化工厂
 *
 * @author
 */
@Deprecated
public class XMLObjectFormatterFactory {

    /**
     * 创建格式化工具
     *
     * @return XMLObjectFormatter 格式化工具
     */
    public static XMLObjectFormatter createFormatter() {
        return new DefaultXMLObjectFormatter();
    }

}
