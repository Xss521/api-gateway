package org.xss.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.xss.core.context.GatewayContext;

import java.util.ArrayList;
import java.util.List;

/**
 * @author MR.XSS
 * 2023/9/21 17:08
 * 过滤器链条类
 */
@Slf4j
public class GatewayFilterChain {
    private List<Filter> filters = null;

    public GatewayFilterChain addFilter(Filter filter) {
        this.filters.add(filter);
        return this;
    }

    public GatewayFilterChain addFilterList(List<Filter> filters) {
        this.filters.addAll(filters);
        return this;
    }

    public GatewayContext doFilter(GatewayContext gatewayContext) {
        if (filters.isEmpty()) {
            return gatewayContext;
        }

        for (Filter filter : filters) {
            try {
                filter.doFilter(gatewayContext);
            } catch (Exception e) {
                log.info("执行过滤器出现异常，异常信息 {}", e.getMessage());
                throw new RuntimeException(e);
            }
        }

        return gatewayContext;
    }
}
