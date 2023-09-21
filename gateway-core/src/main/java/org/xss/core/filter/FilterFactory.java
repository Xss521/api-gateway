package org.xss.core.filter;

import org.xss.core.context.GatewayContext;

/**
 * @author MR.XSS
 * 2023/9/21 17:01
 * 过滤器工厂接口，负责创建过滤器链条，获取过滤器
 */
public interface FilterFactory {

    /**
     * 构建过滤器链
     */
    GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception;

    /**
     * 根据过滤器ID，获取过滤器
     */
    <T> T getFilterInfo(String filterId) throws Exception;
}
