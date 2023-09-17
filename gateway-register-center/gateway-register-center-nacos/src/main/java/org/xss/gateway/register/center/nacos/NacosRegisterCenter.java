package org.xss.gateway.register.center.nacos;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingMaintainFactory;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.Event;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ServiceInfo;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInstance;
import org.xss.common.constants.GatewayConst;
import org.xss.gateway.register.center.api.RegisterCenter;
import org.xss.gateway.register.center.api.RegisterCenterListener;


import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author MR.XSS
 * 2023/9/17 14:03
 */
@Slf4j
public class NacosRegisterCenter implements RegisterCenter {

    private String registerCenterAddr;

    private String env;

    //用于维护服务实例信息
    private NamingService namingService;

    //主要用于维护服务定义信息
    private NamingMaintainService namingMaintainService;

    private List<RegisterCenterListener> registerCenterListenerList;


    @Override
    public void init(String registerAddr, String env) {
        this.registerCenterAddr = registerAddr;
        this.env = env;

        try {
            this.namingMaintainService = NamingMaintainFactory.createMaintainService(registerCenterAddr);
            this.namingService = NamingFactory.createNamingService(registerCenterAddr);
        } catch (NacosException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public void register(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            //构建Nacos实例信息--》 将serviceInstance转化为NacosInstance
            Instance nacosInstance = new Instance();
            nacosInstance.setInstanceId(serviceInstance.getServiceInstanceId());
            nacosInstance.setIp(serviceInstance.getIp());
            nacosInstance.setPort(serviceInstance.getPort());
            nacosInstance.setMetadata(Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceInstance)));

            //注册实例信息
            namingService.registerInstance(serviceDefinition.getServiceId(), env, nacosInstance);
            //更新服务定义
            namingMaintainService.updateService(serviceDefinition.getServiceId(), env, 0, Map.of(GatewayConst.META_DATA_KEY, JSON.toJSONString(serviceDefinition)));

            log.info("register {} {}", serviceDefinition, serviceInstance);

        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deregister(ServiceDefinition serviceDefinition, ServiceInstance serviceInstance) {
        try {
            namingService.registerInstance(serviceDefinition.getServiceId(),
                    env, serviceInstance.getIp(), serviceInstance.getPort());
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void subscribeAllServices(RegisterCenterListener listener) {
        registerCenterListenerList.add(listener);
        doSubscribeAllServices();

        //可能有新服务加入，所以需要一个定时任务检查
        ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(1, new DefaultThreadFactory("doSubscribeAllServices"));
        //每十秒对服务列表进行刷新一次
        scheduledThreadPool.scheduleWithFixedDelay(()->doSubscribeAllServices(),10,10, TimeUnit.SECONDS);
    }

    private void doSubscribeAllServices() {
        try {
            //已经订阅的服务
            Set<String> subscribeService = namingService.getSubscribeServices()
                    .stream()
                    .map(ServiceInfo::getName)
                    .collect(Collectors.toSet());

            int pageNo = 1;
            int pageSize = 100;

            //Nacos事件监听器
            EventListener eventListener = new NacosRegisterListener();

            //分页从Nacos拿到服务列表
            List<String> serviceList = namingService.getServicesOfServer(pageNo, pageSize, env).getData();
            while (CollectionUtils.isNotEmpty(serviceList)) {
                log.info("service size is {}", serviceList.size());

                for (String service : serviceList) {
                    if (subscribeService.contains(service)) {
                        continue;
                    }
                    namingService.subscribe(service, eventListener);
                    log.info("subscribe {} []", service, env);
                }

                serviceList = namingService
                        .getServicesOfServer(++pageNo, pageSize, env).getData();
            }
        } catch (NacosException e) {
            throw new RuntimeException(e);
        }
    }

    public class NacosRegisterListener implements EventListener {

        @Override
        public void onEvent(Event event) {

        }
    }
}