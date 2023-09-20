package org.xss.core;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.xss.common.config.DynamicConfigManager;
import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInstance;
import org.xss.common.utils.NetUtils;
import org.xss.common.utils.TimeUtil;
import org.xss.gateway.config.center.api.ConfigCenter;
import org.xss.gateway.register.center.api.RegisterCenter;
import org.xss.gateway.register.center.api.RegisterCenterListener;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static org.xss.common.constants.BasicConst.COLON_SEPARATOR;

/**
 * 核心配置启动类
 */
@Slf4j
public class BootStrap {
    public static void main(String[] args) {

        //加载网关静态配置
        Config config = ConfigLoader.getInstance().load(args);
        // System.out.println(config.getPort());

        //插件初始化
        //配置中心管理器初始化，连接配置中心，监听配置新增、修改、删除
        ServiceLoader<ConfigCenter> serviceLoader = ServiceLoader.load(ConfigCenter.class);
        final ConfigCenter configCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found ConfigCenter impl");
            return new RuntimeException("not found ConfigCenter impl");
        });
        configCenter.init(config.getRegisterAddr(), config.getEnv());
        configCenter.subscribeRuleChange(rules -> DynamicConfigManager.getInstance().putAllRule(rules));


        //启动容器
        Container container = new Container(config);
        container.start();


        // 连接注册中心、将注册中心实例加载到本地
        //构建网关服务定义和实例信息,并且对实例信息进行订阅和刷新
        final RegisterCenter registerCenter = registerAndSubscribe(config);


        // 服务优雅关机
        // 收到Kill信号时，调用虚拟机钩子函数，进行注销
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            registerCenter.deregister(
                    buildGatewayServiceDefinition(config),
                    buildGatewayServiceInstance(config));
            container.shutDown();
        }));


    }


    private static RegisterCenter registerAndSubscribe(Config config) {
        ServiceLoader<RegisterCenter> serviceLoader = ServiceLoader.load(RegisterCenter.class);
        final RegisterCenter registerCenter = serviceLoader.findFirst().orElseThrow(() -> {
            log.error("not found RegisterCenter impl");
            return new RuntimeException("not found RegisterCenter impl");
        });
        registerCenter.init(config.getRegisterAddr(), config.getEnv());

        //构造网关服务定义和服务实例
        ServiceDefinition serviceDefinition = buildGatewayServiceDefinition(config);
        ServiceInstance serviceInstance = buildGatewayServiceInstance(config);

        //注册
        registerCenter.register(serviceDefinition, serviceInstance);

        //订阅
        registerCenter.subscribeAllServices(new RegisterCenterListener() {
            @Override
            public void onChange(ServiceDefinition serviceDefinition, Set<ServiceInstance> serviceInstanceSet) {
                log.info("refresh service and instance: {} {}", serviceDefinition.getUniqueId(),
                        JSON.toJSON(serviceInstanceSet));
                DynamicConfigManager manager = DynamicConfigManager.getInstance();
                manager.addServiceInstance(serviceDefinition.getUniqueId(), serviceInstanceSet);
            }
        });
        return registerCenter;
    }

    private static ServiceInstance buildGatewayServiceInstance(Config config) {
        String localIp = NetUtils.getLocalIp();
        int port = config.getPort();
        ServiceInstance serviceInstance = new ServiceInstance();
        serviceInstance.setServiceInstanceId(localIp + COLON_SEPARATOR + port);
        serviceInstance.setIp(localIp);
        serviceInstance.setPort(port);
        serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
        return serviceInstance;
    }

    private static ServiceDefinition buildGatewayServiceDefinition(Config config) {
        ServiceDefinition serviceDefinition = new ServiceDefinition();
        serviceDefinition.setInvokerMap(Map.of());
        serviceDefinition.setUniqueId(config.getApplicationName());
        serviceDefinition.setServiceId(config.getApplicationName());
        serviceDefinition.setEnvType(config.getEnv());
        return serviceDefinition;
    }
}
