package org.xss.core.filter.loadbalance;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xss.common.config.Rule;
import org.xss.common.config.ServiceInstance;
import org.xss.common.exception.NotFoundException;
import org.xss.core.context.GatewayContext;
import org.xss.core.filter.Filter;
import org.xss.core.filter.FilterAspect;
import org.xss.core.request.GatewayRequest;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static org.xss.common.constants.FilterConst.*;
import static org.xss.common.enums.ResponseCode.SERVICE_INSTANCE_NOT_FOUND;

/**
 * @author MR.XSS
 * 2023/9/22 8:16
 * 负载均衡过滤器实现
 */
@Slf4j
@FilterAspect(id = LOAD_BALANCE_FILTER_ID, name = LOAD_BALANCE_FILTER_NAME, order = LOAD_BALANCE_FILTER_ORDER)
public class LoadBalanceFilter implements Filter {
    @Override
    public void doFilter(GatewayContext ctx) throws Exception {
        String serviceId = ctx.getUniqueId();
        IGatewayLoadBalanceRule gatewayLoadBalanceRule = getLoadBalanceRule(ctx);
        ServiceInstance serviceInstance = gatewayLoadBalanceRule.choose(serviceId);
        System.out.println("IP为"+serviceInstance.getIp()+",端口号："+serviceInstance.getPort());
        GatewayRequest gatewayRequest = ctx.getRequest();

        if (serviceInstance != null && gatewayRequest != null) {
            String host = serviceInstance.getIp() + ":" + serviceInstance.getPort();
            gatewayRequest.setModifyHost(host);
        } else {
            log.warn("No instance available：{}", serviceId);
            throw new NotFoundException(SERVICE_INSTANCE_NOT_FOUND);
        }
    }

    /**
     * @author: MR.XSS
     * @Params: [ctx]
     * @return: org.xss.core.filter.loadbalance.IGatewayLoadBalanceRule
     * @date 2023/9/22 12:56
     * @描述: 获取负载均衡器
     */
    private IGatewayLoadBalanceRule getLoadBalanceRule(GatewayContext ctx) {
        IGatewayLoadBalanceRule loadBalanceRule = null;

        Rule configRule = ctx.getRule();

        if (configRule != null) {
            Set<Rule.FilterConfig> filterConfigs = configRule.getFilterConfigs();
            Iterator<Rule.FilterConfig> iterator = filterConfigs.iterator();
            Rule.FilterConfig filterConfig;

            while (iterator.hasNext()) {
                filterConfig = iterator.next();
                if (filterConfig == null) {
                    continue;
                }
                String filterId = filterConfig.getId();
                if (filterId.equals(LOAD_BALANCE_FILTER_ID)) {
                    String config = filterConfig.getConfig();
                    String strategy = LOAD_BALANCE_STRATEGY_RANDOM;
                    if (StringUtils.isNotEmpty(config)) {
                        Map<String, String> map = JSON.parseObject(config, Map.class);
                        strategy = map.get(LOAD_BALANCE_KEY);
                    }
                    switch (strategy) {
                        case LOAD_BALANCE_STRATEGY_RANDOM:
                            loadBalanceRule =RandomLoadBalanceRule.getInstance(ctx.getUniqueId());
                            break;
                        case LOAD_BALANCE_STRATEGY_ROUND_ROBIN:
                            loadBalanceRule = RoundRobinLoadBalance.getInstance(ctx.getUniqueId());
                            break;
                        default:
                            log.warn("No such loadBalance of the strategy: {}", strategy);
                            loadBalanceRule =RandomLoadBalanceRule.getInstance(ctx.getUniqueId());
                            break;
                    }
                }
            }
        }
        return loadBalanceRule;
    }
}














