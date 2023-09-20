package org.xss.gateway.config.center.api;

import org.xss.common.config.Rule;

import java.util.List;

/**
 * @author MR.XSS
 * 2023/9/20 17:12
 */
public interface RulesChangeListener {
    void onRuleChange(List<Rule> rules);
}
