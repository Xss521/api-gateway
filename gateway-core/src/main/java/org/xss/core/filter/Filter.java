package org.xss.core.filter;

import org.xss.core.context.GatewayContext;

/**
 * @author MR.XSS
 * 2023/9/21 16:13
 * 过滤器的顶级接口
 */
public interface Filter {
    void doFilter(GatewayContext ctx) throws Exception;

    /**
     * 过滤器默认执行顺序，如果没有设置，则在第一个被执行
     */
    default int getOrder(){
        FilterAspect annotation = this.getClass().getAnnotation(FilterAspect.class);
        if (annotation != null){
            return annotation.order();
        }
        return Integer.MAX_VALUE;
    }
}
