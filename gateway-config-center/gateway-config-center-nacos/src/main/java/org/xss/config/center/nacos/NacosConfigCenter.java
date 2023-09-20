package org.xss.config.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import lombok.extern.slf4j.Slf4j;
import org.xss.common.config.Rule;
import org.xss.gateway.config.center.api.ConfigCenter;
import org.xss.gateway.config.center.api.RulesChangeListener;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author MR.XSS
 * 2023/9/20 17:56
 */
@Slf4j
public class NacosConfigCenter implements ConfigCenter {

    private static final String DATA_ID = "api-gateway";

    private String serverAddr;

    private String env;

    private ConfigService configService;


    @Override
    public void init(String serverAddr, String env) {
        this.serverAddr = serverAddr;
        this.env = env;
        try {
            this.configService = NacosFactory.createConfigService(serverAddr);
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeRuleChange(RulesChangeListener listener) {
        try {
            //初始化通知
            String config = configService.getConfig(DATA_ID, env, 5000);
            //config是json，key是{"rules": [{},{}]}
            log.info("config from nacos: {}", config);
            List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            listener.onRuleChange(rules);

            //监听变化
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                @Override
                public void receiveConfigInfo(String configInfo) {
                    log.info("config from nacos: {}", configInfo);
                    List<Rule> rules = JSON.parseObject(configInfo).getJSONArray("rules").toJavaList(Rule.class);
                    listener.onRuleChange(rules);
                }
            });

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }
}
