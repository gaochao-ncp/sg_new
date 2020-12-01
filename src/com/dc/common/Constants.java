package com.dc.common;

import com.dc.xml.XmlType;

import java.util.Arrays;
import java.util.List;

/**
 * 常量类
 * @author: Administrator
 * @date: 2020-11-17 10:20
 * @version: 1.0
 */
public interface Constants {

	/**
	 * 要生成的文件类型:拆组包文件和服务定义文件
	 */
	List<XmlType> XML_TYPE_LIST = Arrays.asList(new XmlType[]{XmlType.SERVICE,XmlType.SERVICE_DEFINITION});

	String IN_CN = "输入";

	String OUT_CN = "输出";

	/**
	 * 含义：输入字段；in端
	 */
	String IN = "IN";

	/**
	 * 含义：输出字段；out端
	 */
	String OUT = "OUT";

	/**
	 * 全部字段
	 */
	String ALL = "ALL";

	/**
	 * in端服务识别路径
	 */
	String IN_SERVICE_IDENTIFY = "in_service_identify";

	/**
	 * out端系统识别路径
	 */
	String OUT_SYSTEM_IDENTIFY = "out_system_identify";

	String UAT_CLIENT = "UAT";

	String SIT_CLIENT = "SIT";

	String SHEET_RECORD = "修订记录";

	String SHEET_INDEX = "索引";

	String SHEET_COMMON = "常量对照表";

	String SHEET_DATA = "数据字典";

	String SHEET_SYS_HEAD = "SYS_HEAD";

	String SHEET_APP_HEAD = "APP_HEAD";

	String SHEET_COMMON_FILED = "公共字段";

	String SHEET_SYSTEM = "消费者接入端口分配";

	/**
	 * xml中的属性和节点常量
	 */
	String NODE_TYPE = "type";
	String NODE_LENGTH = "length";
	String NODE_SCALE = "scale";
	String NODE_CHINESE_NAME = "chinese_name";
	String NODE_METADATA_ID = "metadataid";
	String NODE_IS_STRUCT = "is_struct";
	String NODE_MODE = "mode";
	String NODE_EXPRESSION = "expression";
	String NODE_EXPRESSION_VALUE = "/service/SYS_HEAD/ServiceCode+/service/SYS_HEAD/ServiceScene";
	/** 属性反转，为了匹配正则表达式 **/
	String NODE_EXPRESSION_VALUE_REVERSE = "\\service\\SYS_HEAD\\ServiceCode+\\service\\SYS_HEAD\\ServiceScene";
	String NODE_ID = "id";
	String NODE_ENCODE = "encode";
	String NODE_VALUE = "value";
	String NODE_PACKAGE_TYPE = "package_type";
	String NODE_PACKAGE_TYPE_VALUE = "package_type";
	String NODE_STORE_MODE = "xml";
	String NODE_STORE_MODE_VALUE = "UTF-8";
	String NODE_SYSTEMS = "systems";
	String NODE_SYSTEM = "system";
	String NODE_ARRAY = "array";
	String NODE_SERVICE = "service";
	String NODE_CHANNELS = "channels";
	String NODE_CHANNEL = "channel";
	String NODE_SWITCH = "switch";
	String NODE_BEMIDDLE = "bemiddle";


	/**
	 * 消费渠道和服务系统标识
	 */
	String CONSUMER_CHANNEL = "consumer_channel";
	String PROVIDER_SYSTEM = "provider_system";


	int M = 13;
	int G = 7;
	int H = 8;

	String BOOLEAN_FALSE = "false";

	/**
	 * 正则表达式
	 */
	/**
	 * 截取 [@*']中间的字符
	 */
	String ATTRS_REGX = "\\[@[^\\/]*']";

	/**
	 * 截取 12,3 double类型的数字
	 */
	String REGEX = "[0-9]*,[0-9]*";

	/**
	 * 切割类似于 /array/xxx/。将array节点后面的一个节点一起匹配下来
	 */
	String REGEX_ARRAY = "\\/array\\/[a-zA-Z]+\\/";

	/**
	 * 是否是数字
	 */
	String NUMERIC_REGEX = "[0-9]*";

  String SMRTLR = "SMRTLR";
}
