package com.dc.log;

import cn.hutool.log.LogFactory;

/**
 * 日志通用类
 *
 * @author: Administrator
 * @date: 2020-11-18 10:57
 * @version: 1.0
 */
public class Log {

  public static void info(Throwable var1){
    LogFactory.get().info(var1);
  }

  public static void info(String var1, Object... var2){
    LogFactory.get().info(var1,var2);
  }

  public static void info(Throwable var1, String var2, Object... var3){
    LogFactory.get().info(var1, var2, var3);
  }

  public static void info(String var1, Throwable var2, String var3, Object... var4){
    LogFactory.get().info(var1, var2, var3, var4);
  }
}
