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
import java.util.concurrent.ThreadLocalRandom;

import static org.xss.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * @author MR.XSS
 * 2023/9/22 8:36
 * 随机的负载均衡算法，随机选取一个服务实例进行执行
 */
@Slf4j
public class RandomLoadBalanceRule implements IGatewayLoadBalanceRule {
    private final String serviceId;

    private Set<ServiceInstance> serviceInstanceSet;

    public RandomLoadBalanceRule(String serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public ServiceInstance choose(GatewayContext ctx) {
        String serviceId = ctx.getUniqueId();
        return choose(serviceId);
    }

    private static ConcurrentHashMap<String,RandomLoadBalanceRule> map = new ConcurrentHashMap<>();

    public static RandomLoadBalanceRule getInstance(String serviceId){
        RandomLoadBalanceRule randomLoadBalanceRule = map.get(serviceId);
        if (randomLoadBalanceRule == null){
            randomLoadBalanceRule = new RandomLoadBalanceRule(serviceId);
            map.put(serviceId,randomLoadBalanceRule);
        }
        return randomLoadBalanceRule;
    }

    @Override
    public ServiceInstance choose(String serviceId) {
        serviceInstanceSet = DynamicConfigManager.getInstance().getServiceInstanceByUniqueId(serviceId);

        if (serviceInstanceSet.isEmpty()) {
            log.warn("No such instance {}", serviceId);
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }

        List<ServiceInstance> instances = new ArrayList<>(serviceInstanceSet);

        //随机得到一个
        int index = ThreadLocalRandom.current().nextInt(instances.size());
        ServiceInstance serviceInstance = instances.get(index);
        return serviceInstance;
    }
}
