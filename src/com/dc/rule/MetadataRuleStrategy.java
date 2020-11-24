package com.dc.rule;

/**
 * metadata.xml规则s
 *
 * @author: Administrator
 * @date: 2020-11-23 16:15
 * @version: 1.0
 */
public class MetadataRuleStrategy implements RuleStrategy {

  /**
   * 前置校验
   * @param baseRule
   * @return
   */
  @Override
  public boolean preCheckRule(BaseRule baseRule) {
    return false;
  }

  @Override
  public boolean postCheckRule(BaseRule baseRule) {
    return false;
  }
}
