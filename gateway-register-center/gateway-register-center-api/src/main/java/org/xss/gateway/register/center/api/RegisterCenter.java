package org.xss.gateway.register.center.api;

import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInstance;

/**
 * @author MR.XSS
 * 2023/9/17 12:46
 * 注册中心
 */
public interface RegisterCenter {
    //初始化
    void init(String registerAddr, String env);

    //注册
    void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    //撤销注册
    void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance);

    //订阅所有服务变更
    void subscribeAllServices(RegisterCenterListener listener);

}
