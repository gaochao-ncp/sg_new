package com.dc.config;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.common.Constants;
import com.sun.org.slf4j.internal.Logger;
import com.sun.org.slf4j.internal.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

/**
 * config目录下的配置文件
 * @author: Administrator
 * @date: 2020-11-17 10:20
 * @version: 1.0
 */
public class SgConfig {

  private static final Log log = LogFactory.get(SgConfig.class);

  private String sshIp;
  private String sshUser;
  private String sshPwd;

  private String remoteEsbHome;
  private String remoteInHome;
  private String remoteOutHome;
  private String remoteMetadataHome;
  private String remoteServiceIdentifyHome;

  private String localEsbHome;
  private String localInHome;
  private String localOutHome;
  private String localMetadataHome;
  private String localServiceIdentifyHome;

  private Map<String, String> systemMappings = new HashMap<>();

  private String filteredChannels;

  private String filteredServices;

  private Map<String, List<String>> excelServices = new HashMap<>();

  public Map<String, String> remoteUrl = new HashMap<>();

  public Map<String, String> localUrl = new HashMap<>();

  public SgConfig(){
    init();
  }

  public static SgConfig getConfig(){
    SgConfig config = new SgConfig();
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

    this.localEsbHome = prop.getProperty("local.esb.home");
    this.localInHome = prop.getProperty("local.in.home");
    this.localOutHome = prop.getProperty("local.out.home");
    this.localMetadataHome = prop.getProperty("local.metadata.home");
    this.localServiceIdentifyHome = prop.getProperty("local.serviceIdentify.home");

    String systemMappingStr = prop.getProperty("system.mappings");
    if (StrUtil.isNotBlank(systemMappingStr)){
      String[] systemMappingStrs = systemMappingStr.split("\\;");
      for (String mappingStr : systemMappingStrs) {
        String[] mapping = mappingStr.split("\\:");
        systemMappings.put(mapping[0],mapping[1]);
      }
    }

    this.filteredChannels = prop.getProperty("filtered.channels");

    this.filteredServices = prop.getProperty("filtered.services");

    String localExcelServicesStr = prop.getProperty("local.excel.services");
    if (StrUtil.isNotBlank(localExcelServicesStr)){
      String[] localExcelServicesStrs = localExcelServicesStr.split("\\;");
      for (String excelServicesStr : localExcelServicesStrs) {
        String[] excelService = excelServicesStr.split("\\|");
        //解析需要过滤的服务组成一个数组
        List<String> value = Arrays.asList(excelService[1].split("\\,"));
        excelServices.put(excelService[0],value);
      }
    }

    //初始化远程路径
    remoteUrl.put(Constants.IN,this.remoteEsbHome+this.remoteInHome+this.remoteMetadataHome);
    remoteUrl.put(Constants.OUT,this.getRemoteEsbHome()+this.getRemoteOutHome()+this.getRemoteMetadataHome());

    //初始化本地路径
    localUrl.put(Constants.IN,this.getLocalEsbHome()+this.getLocalInHome()+this.getLocalMetadataHome());
    localUrl.put(Constants.OUT,this.getLocalEsbHome()+this.getLocalOutHome()+this.getLocalMetadataHome());

  }

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

  public Map<String, String> getSystemMappings() {
    return systemMappings;
  }

  public String getFilteredChannels() {
    return filteredChannels;
  }

  public String getFilteredServices() {
    return filteredServices;
  }

  public Map<String, List<String>> getExcelServices() {
    return excelServices;
  }

}
