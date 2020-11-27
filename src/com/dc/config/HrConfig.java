package com.dc.config;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.CommonUtil;
import com.dc.common.Constants;
import com.dc.excel.ExcelParse;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * 读取配置文件
 * @author: Administrator
 * @date: 2020-11-17 10:20
 * @version: 1.0
 */
public class HrConfig {

  private static final Log log = LogFactory.get(HrConfig.class);

  /** 服务器信息 **/
  private String sshIp;
  private String sshUser;
  private String sshPwd;

  /** 服务器远程路径 **/
  private String remoteEsbHome;
  private String remoteInHome;
  private String remoteOutHome;
  private String remoteMetadataHome;
  private String remoteServiceIdentifyHome;
  private String remoteSystemIdentifyHome;

  /** 本地路径 **/
  private String localEsbHome;
  private String localInHome;
  private String localOutHome;
  private String localMetadataHome;
  private String localServiceIdentifyHome;
  private String localSystemIdentifyHome;

  private String filteredChannels;
  private String filteredServices;

  /** 无法解析sheet页 **/
  private List filteredSheets;

  private String gdCode;
  private String gdName;

  public Map<String, HrSystem> systemMappings = new HashMap<>();

  /**
   * 治理文档和服务码列表
   * key : 服务治理文档绝对路径
   * value : 服务编码列表
   */
  public Map<String, List<String>> excelServices = new HashMap<>();

  public Map<String, String> REMOTE_URL = new HashMap<>();

  public Map<String, String> LOCAL_URL = new HashMap<>();

  public HrConfig(){
    init();
  }

  public static HrConfig getConfig(){
    HrConfig config = new HrConfig();
    config.init();
    return config;
  }

  /**
   * 初始化配置文件中的字段值
   */
  private void init() {
    Properties prop = initProperties();
    if (null == prop){return;}

    this.sshIp = prop.getProperty("ssh.ip");
    this.sshUser = prop.getProperty("ssh.user");
    this.sshPwd = prop.getProperty("ssh.pwd");

    this.remoteEsbHome = prop.getProperty("remote.esb.home");
    this.remoteInHome = prop.getProperty("remote.in.home");
    this.remoteOutHome = prop.getProperty("remote.out.home");
    this.remoteMetadataHome = prop.getProperty("remote.metadata.home");
    this.remoteServiceIdentifyHome = prop.getProperty("remote.serviceIdentify.home");
    this.remoteSystemIdentifyHome = prop.getProperty("remote.systemIdentify.home");

    this.localEsbHome = prop.getProperty("local.esb.home");
    this.localInHome = prop.getProperty("local.in.home");
    this.localOutHome = prop.getProperty("local.out.home");
    this.localMetadataHome = prop.getProperty("local.metadata.home");
    this.localServiceIdentifyHome = prop.getProperty("local.serviceIdentify.home");
    this.localSystemIdentifyHome = prop.getProperty("local.systemIdentify.home");

    String localSystemMapping = prop.getProperty("local.excel.system.mappings");
    if (StrUtil.isNotBlank(localSystemMapping)){
      //将本地的常量表解析进来
     this.systemMappings = ExcelParse.parseSystemMapping(localSystemMapping);
    }else {
      String systemMappingStr = prop.getProperty("system.mappings");
      if (StrUtil.isNotBlank(systemMappingStr)){
        String[] systemMappingStrs = systemMappingStr.split("\\;");
        for (String mappingStr : systemMappingStrs) {
          String[] mapping = mappingStr.split("\\:");
          systemMappings.put(mapping[0],new HrSystem(mapping[0],mapping[1],null));
        }
      }
    }

    this.filteredChannels = prop.getProperty("filtered.channels");
    this.filteredServices = prop.getProperty("filtered.services");

    String filteredSheet = prop.getProperty("filtered.sheets");
    if (StrUtil.isNotBlank(filteredSheet)){
      this.filteredSheets = Arrays.asList(filteredSheet.split(","));
    }

    this.gdCode = prop.getProperty("gd.code");
    this.gdName = prop.getProperty("gd.name");

    String localExcelServicesStr = prop.getProperty("local.excel.services");
    if (StrUtil.isNotBlank(localExcelServicesStr)){
      String[] localExcelServicesStrs = localExcelServicesStr.split("\\;");
      for (String excelServicesStr : localExcelServicesStrs) {
        String[] excelService = excelServicesStr.split("\\|");
        //解析需要过滤的服务组成一个数组
        List<String> value = CommonUtil.trimToArrayList(excelService[1].split("\\,"));
        excelServices.put(excelService[0],value);
      }
    }

    /**初始化远程路径**/
    REMOTE_URL.put(Constants.IN,this.remoteEsbHome+this.remoteInHome+this.remoteMetadataHome);
    REMOTE_URL.put(Constants.OUT,this.getRemoteEsbHome()+this.getRemoteOutHome()+this.getRemoteMetadataHome());
    REMOTE_URL.put(Constants.IN_SERVICE_IDENTIFY,this.remoteEsbHome+this.remoteInHome+this.remoteServiceIdentifyHome);
    REMOTE_URL.put(Constants.OUT_SYSTEM_IDENTIFY,this.remoteEsbHome+this.remoteOutHome+this.remoteSystemIdentifyHome);

    /**初始化本地路径(已经加上了绝对路径了)**/
    LOCAL_URL.put(Constants.IN,System.getProperty("user.dir") + this.getLocalEsbHome()+this.getLocalInHome()+this.getLocalMetadataHome());
    LOCAL_URL.put(Constants.OUT,System.getProperty("user.dir") + this.getLocalEsbHome()+this.getLocalOutHome()+this.getLocalMetadataHome());
    LOCAL_URL.put(Constants.IN_SERVICE_IDENTIFY,System.getProperty("user.dir") +this.localEsbHome+this.localInHome+this.localServiceIdentifyHome);
    LOCAL_URL.put(Constants.OUT_SYSTEM_IDENTIFY,System.getProperty("user.dir") +this.localEsbHome+this.localOutHome+this.localSystemIdentifyHome);
  }

  /**
   * 初始化配置文件
   * @return Properties
   */
  private Properties initProperties(){
    Properties props = new Properties();
    String configPath = System.getProperty("user.dir") + "/config/config.properties";
    try(InputStream is = new FileInputStream(configPath)) {
      props.load(new InputStreamReader(is,"UTF-8"));
    } catch (IOException e) {
      log.error("配置文件路径有误！");
    }
    return props;
  }

  public String getSshIp() {
    return sshIp;
  }

  public String getSshUser() {
    return sshUser;
  }

  public String getSshPwd() {
    return sshPwd;
  }

  public String getRemoteEsbHome() {
    return remoteEsbHome;
  }

  public String getRemoteInHome() {
    return remoteInHome;
  }

  public String getRemoteOutHome() {
    return remoteOutHome;
  }

  public String getRemoteMetadataHome() {
    return remoteMetadataHome;
  }

  public String getRemoteServiceIdentifyHome() {
    return remoteServiceIdentifyHome;
  }

  public String getLocalEsbHome() {
    return localEsbHome;
  }

  public String getLocalInHome() {
    return localInHome;
  }

  public String getLocalOutHome() {
    return localOutHome;
  }

  public String getLocalMetadataHome() {
    return localMetadataHome;
  }

  public String getLocalServiceIdentifyHome() {
    return localServiceIdentifyHome;
  }

  public String getFilteredChannels() {
    return filteredChannels;
  }

  public String getFilteredServices() {
    return filteredServices;
  }

  public String getGdCode() {
    return gdCode;
  }

  public String getGdName() {
    return gdName;
  }

  public String getRemoteSystemIdentifyHome() {
    return remoteSystemIdentifyHome;
  }

  public String getLocalSystemIdentifyHome() {
    return localSystemIdentifyHome;
  }

  public List getFilteredSheets() {
    if (CollUtil.isEmpty(filteredSheets)){
      this.filteredSheets = ListUtil.empty();
    }
    return filteredSheets;
  }
}
