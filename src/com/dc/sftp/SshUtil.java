package com.dc.sftp;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.ssh.Sftp;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.dc.config.SgConfig;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * ssh操作工具类
 * @author GAOCHAO
 */
public class SshUtil {

	private static final Log log = LogFactory.get(SshUtil.class);

	private static SgConfig config = new SgConfig();

	public static Sftp client(){
		Sftp conn = new Sftp(config.getSshIp(), 22, config.getSshUser(), config.getSshPwd(), Charset.forName("UTF-8"));
		if (conn == null){
			throw new NullPointerException("初始化Sftp连接客户端失败,请检查配置!");
		}
		return conn;
	}

	/**
	 * 下载全部Metadata.xml
	 */
	public static void download(){
		if (config.localUrl != null && config.localUrl.size()>0){
			for (Map.Entry<String, String> entry : config.localUrl.entrySet()) {
				String local = entry.getValue();
				String remoteType = entry.getKey();
				download(local,remoteType);
			}
		}
	}

	public static void download(String remoteType){
		//自动获得路径
		String toLocal = config.localUrl.get(remoteType);
		download(toLocal,remoteType);
	}

	/**
	 * 从服务器上下载文件
	 * @param toLocal 本地下载路径
	 * @param remoteType 远程路径标识
	 */
	public static void download(String toLocal,String remoteType){
		String url = config.remoteUrl.get(remoteType);
		if (StrUtil.isBlank(url)){
			log.error("下载地址无法解析");
			return;
		}

		client().download(url, FileUtil.file(toLocal));
	}

}
