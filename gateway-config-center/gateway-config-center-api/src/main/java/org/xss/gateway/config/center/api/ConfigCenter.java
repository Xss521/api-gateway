package org.xss.gateway.config.center.api;

/**
 * @author MR.XSS
 * 2023/9/20 17:11
 */
public interface ConfigCenter {
    void init(String serverAddr, String env);

    void subscribeRuleChange(RulesChangeListener rulesChangeListener);

}
