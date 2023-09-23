package org.xss.core.filter.loadbalance;

import org.xss.common.config.ServiceInstance;
import org.xss.core.context.GatewayContext;

/**
 * @author MR.XSS
 * 2023/9/22 8:24
 * 负载均衡顶级接口
 */
public interface IGatewayLoadBalanceRule {
    /**
     * 上下文获取实例信息
     */
    ServiceInstance choose(GatewayContext ctx);

    /**
     * 服务ID获取实例信息
     */
    ServiceInstance choose(String serviceId);
}
