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


    /**
     * @author: MR.XSS
     * @Params: [serverAddr, env]
     * @return: void
     * @date 2023/9/21 9:12
     * @描述: 初始化注册中心配置
     */
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

    /**
     *@author: MR.XSS
     *@Params: [listener]
     *@return: void
     *@date 2023/9/21 9:12
     *@描述: 订阅Nacos配置，将Nacos配置中心配置加载到本地，并且监听Nacos服务变化情况，若Nacso配置中心配置发生变化，覆盖掉原来配置，重新加载进入本地缓存
     */
    @Override
    public void subscribeRuleChange(RulesChangeListener listener) {
        try {
            //初始化通知
            String config = configService.getConfig(DATA_ID, env, 5000);
            //config是json，key是{"rules": [{},{}]}
            log.info("config from nacos: {}", config);
            List<Rule> rules = JSON.parseObject(config).getJSONArray("rules").toJavaList(Rule.class);
            listener.onRuleChange(rules);

            //Nacos的API，监听名字为api-gateway的配置的变化情况
            configService.addListener(DATA_ID, env, new Listener() {
                @Override
                public Executor getExecutor() {
                    return null;
                }

                //监听服务变化，具体交给启动类实现
                @Override
                public void receiveConfigInfo(String configInfo/* 配置中心Json字符串 */) {
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
