package com.dc.common;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public interface Constants {

	// 远程ESB主目录
	//public static final String remoteEsbHome = ConfigUtil.getProp("remote.esb.home");
	// 本地ESB主目录
	public static final String localEsbHome = System.getProperty("user.dir") + "/SmartESB/";
	public static final String inMetadataHome = "configs/in_conf/metadata/";
	public static final String outMetadataHome = "configs/out_conf/metadata/";
	// in端metadata.xml路径
//	public static final String inMetadataPath = inMetadataHome + "metadata.xml";
	// out端metadata.xml路径
//	public static final String outMetadataPath = outMetadataHome + "metadata.xml";
	//public static final String remoteInMetadataPath = remoteEsbHome + "configs/in_conf/metadata/metadata.xml";

	//public static final String remoteOutMetadataPath = remoteEsbHome + "configs/out_conf/metadata/metadata.xml";
	public static final String localInMetadataPath = localEsbHome + "configs/in_conf/metadata/metadata.xml";
	public static final String localOutMetadataPath = localEsbHome + "configs/out_conf/metadata/metadata.xml";
	public static final String localInMetadataHome = localEsbHome + inMetadataHome;
	public static final String localOutMetadataHome = localEsbHome + outMetadataHome;
	public static final String localInServiceIdentifyHome = new File(localInMetadataHome).getParent() + "/";
	public static final String localOutSystemIdentifyHome = new File(localOutMetadataHome).getParent() + "/";

	public static final String localTestPath = localEsbHome + "test/";

	/**
	 * 生成文件标志
	 */
	String XML_METADATA = "metadata";
	String XML_SERVICE = "service";
	String XML_SYSTEMS = "systems";
	String XML_Identify = "Identify";
	String XML_S = "S";

	/**
	 * 截取 12,3 double类型的数字
	 */
	String REGEX = "[0-9]*,[0-9]*";

	String IN_CN = "输入";

	String OUT_CN = "输出";

	String IN = "IN";

	String OUT = "OUT";

	String UAT_CLIENT = "UAT";

	String SIT_CLIENT = "SIT";

	String SHEET_RECORD = "修订记录";

	String SHEET_INDEX = "索引";

	String SHEET_COMMON = "常量对照表";

	String SHEET_SYS_HEAD = "SYS_HEAD";

	String SHEET_APP_HEAD = "APP_HEAD";

	/**
	 * xml中的节点属性字段
	 */
	String NODE_TYPE = "type";
	String NODE_LENGTH = "length";
	String NODE_SCALE = "scale";
	String NODE_CHINESE_NAME = "chinese_name";
	String NODE_METADATA_ID = "metadataid";
	String NODE_IS_STRUCT = "is_struct";
	String NODE_MODE = "mode";
	String NODE_EXPRESSION = "expression";
	String NODE_ID = "id";
	String NODE_ENCODE = "encode";
	String NODE_VALUE = "value";
	String NODE_PACKAGE_TYPE = "package_type";
	String NODE_PACKAGE_TYPE_VALUE = "package_type";
	String NODE_STORE_MODE = "xml";
	String NODE_STORE_MODE_VALUE = "UTF-8";

	/**
	 * xml中root节点名称
	 */
	String ROOT_METADATA = "metadata";

	String SYS_HEAD = "SYS_HEAD";
	String APP_HEAD = "APP_HEAD";

}
