package org.xss.gateway.client.support;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInstance;
import org.xss.gateway.client.core.ApiProperties;
import org.xss.gateway.register.center.api.RegisterCenter;

import java.util.ServiceLoader;

/**
 * @author MR.XSS
 * 2023/9/20 10:06
 */
@Slf4j
public abstract class AbstractClientRegisterManager {
    @Getter
    private ApiProperties apiProperties;

    private RegisterCenter registerCenter;

    /**
     * @author: MR.XSS
     * @Params: [apiProperties]
     * @date 2023/9/20 10:48
     * @描述: 初始化并且加载注册中心实现类，为注册中心赋值
     */
    protected AbstractClientRegisterManager(ApiProperties apiProperties) {
        this.apiProperties = apiProperties;
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        this.registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("can not find RegisterCenter Impl");
            return new RuntimeException("can not find RegisterCenter Impl");
        });
        registerCenter.init(apiProperties.getRegisterAddr(), apiProperties.getEnv());
    }

    protected void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        registerCenter.register(serviceDefinition, serviceInstance);
    }
}
