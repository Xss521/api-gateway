package org.xss.core.filter.loadbalance;

import lombok.extern.slf4j.Slf4j;
import org.xss.common.config.DynamicConfigManager;
import org.xss.common.config.ServiceInstance;
import org.xss.common.exception.NotFoundException;
import org.xss.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.xss.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * @author MR.XSS
 * 2023/9/22 9:55
 * 轮询访问实例信息
 */
@Slf4j
public class RoundRobinLoadBalance implements IGatewayLoadBalanceRule {

    final  AtomicInteger position = new AtomicInteger();

    private String serviceId;

    private Set<ServiceInstance> serviceInstanceSet;

    public RoundRobinLoadBalance(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        return choose(ctx.getUniqueId());
    }

    private static ConcurrentHashMap<String,RoundRobinLoadBalance> map = new ConcurrentHashMap<>();

    public static RoundRobinLoadBalance getInstance(String serviceId){
        RoundRobinLoadBalance roundRobinLoadBalance = map.get(serviceId);
        if (roundRobinLoadBalance == null){
            roundRobinLoadBalance = new RoundRobinLoadBalance(serviceId);
            map.put(serviceId,roundRobinLoadBalance);
        }
        return roundRobinLoadBalance;
    }

    //轮询访问实例
    @Override
    public ServiceInstance choose(String serviceId) {
        /* 构造方法中的该方法加载时间较长，此时主动进行拉取 */
        serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId);

        if (serviceInstanceSet.isEmpty()) {
            log.warn("No such instance {}", serviceId);
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }

        List<ServiceInstance> instances = new ArrayList<>(serviceInstanceSet);
        int pos = Math.abs(this.position.incrementAndGet());

        return instances.get(pos % instances.size());
    }
}
