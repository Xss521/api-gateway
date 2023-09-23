package org.xss.core.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xss.common.config.Rule;
import org.xss.core.context.GatewayContext;
import org.xss.core.filter.router.RouterFilter;

import java.util.*;

/**
 * @author MR.XSS
 * 2023/9/21 17:15
 */
@Slf4j
public class GatewayFilterChainFactory implements FilterFactory {

    Map<String, Filter> processFilterIdMap = new LinkedHashMap<>();

    //单例模式获取过滤器工厂
    private static class SingletonInstance {
        private static final GatewayFilterChainFactory INSTANCE = new GatewayFilterChainFactory();
    }
    public static GatewayFilterChainFactory getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public GatewayFilterChainFactory() {
        ServiceLoader<Filter> serviceLoader = ServiceLoader.load(Filter.class);
        serviceLoader.stream().forEach(filterProvider -> {
            Filter filter = filterProvider.get();
            FilterAspect annotation = filter.getClass().getAnnotation(FilterAspect.class);
            log.info("load filter info: {},{},{},{}", annotation.getClass(), annotation.id(), annotation.name(), annotation.order());

            if (annotation != null) {
                String id = annotation.id();
                if (StringUtils.isEmpty(id)) {
                    id = annotation.name();
                }
                processFilterIdMap.put(id, filter);
            }
        });
    }


    @Override
    public GatewayFilterChain buildFilterChain(GatewayContext ctx) throws Exception {
        GatewayFilterChain gatewayFilterChain = new GatewayFilterChain();
        List<Filter> filters = new ArrayList<>();

        Rule rule = ctx.getRule();
        if (rule != null) {
            Set<Rule.FilterConfig> filterConfigs = rule.getFilterConfigs();
            Iterator<Rule.FilterConfig> iterator = filterConfigs.iterator();
            Rule.FilterConfig filterConfig;
            while (iterator.hasNext()) {
                filterConfig = iterator.next();
                if (filterConfig == null) continue;
                String filterId = filterConfig.getId();

                if (StringUtils.isNotEmpty(filterId) && getFilterInfo(filterId) != null) {
                    Filter filter = getFilterInfo(filterId);
                    filters.add(filter);
                }
            }
        }

        // 添加路由过滤器-
        filters.add(new RouterFilter());

        //根据order对过滤器进行排序
        filters.sort(Comparator.comparingInt(Filter::getOrder));

        //添加到链表中
        gatewayFilterChain.addFilterList(filters);

        return gatewayFilterChain;
    }

    @Override
    public Filter getFilterInfo(String filterId) throws Exception {
        return processFilterIdMap.get(filterId);
    }


}
