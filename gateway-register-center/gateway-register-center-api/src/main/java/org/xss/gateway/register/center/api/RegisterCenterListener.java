package org.xss.gateway.register.center.api;

import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInstance;

import java.util.Set;

/**
 * @author MR.XSS
 * 2023/9/17 12:59
 * 注册中心监听器，监听服务发生改变时的方法
 */
public interface RegisterCenterListener {

    void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet);
}
