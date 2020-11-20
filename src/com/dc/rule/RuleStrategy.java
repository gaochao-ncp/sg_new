package com.dc.rule;

/**
 * 解析校验规则
 * @author: Administrator
 * @date: 2020-11-20 17:04
 * @version: 1.0
 */
public interface RuleStrategy {

  /**
   * 前置校验规则
   * @param baseRule
   * @return
   */
  boolean preCheckRule(BaseRule baseRule);

  /**
   * 后置校验规则
   * @param baseRule
   * @return
   */
  boolean postCheckRule(BaseRule baseRule);
}
