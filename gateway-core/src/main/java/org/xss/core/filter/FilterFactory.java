package org.xss.core.filter;

import org.xss.core.context.GatewayContext;

/**
 *@author: MR.XSS
 *@date 2023/9/22 15:06
 *@描述: 过滤器工厂接口
 * 1、负责创建过滤器链、按照过滤器链顺序执行过滤器
 * 2、根据ID获取过滤器
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
