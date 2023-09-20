package org.xss.gateway.client.support.springmvc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInstance;
import org.xss.common.constants.BasicConst;
import org.xss.common.utils.NetUtils;
import org.xss.common.utils.TimeUtil;
import org.xss.gateway.client.core.ApiAnnotationScanner;
import org.xss.gateway.client.core.ApiProperties;
import org.xss.gateway.client.support.AbstractClientRegisterManager;

import javax.annotation.Resource;
import java.util.*;

import static org.xss.common.constants.GatewayConst.DEFAULT_WEIGHT;

/**
 * @author MR.XSS
 * 2023/9/20 15:05
 * 对接springmvc服务
 */
@Slf4j
public class SpringMVCClientRegisterManager extends AbstractClientRegisterManager implements ApplicationListener, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Resource
    private ServerProperties serverProperties;

    private Set<Object> set = new HashSet<>();


    /**
     * @param apiProperties
     * @author: MR.XSS
     * @Params: [apiProperties]
     * @date 2023/9/20 10:48
     * @描述: 初始化并且加载注册中心实现类，为注册中心赋值
     */
    public SpringMVCClientRegisterManager(ApiProperties apiProperties) {
        super(apiProperties);
    }


    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        if (applicationEvent instanceof ApplicationStartedEvent) {
            try {
                doRegisterSpringMvc();
            } catch (Exception e) {
                log.info("doRegisterSpringMvc error", e);
                throw new RuntimeException(e);
            }
            log.info("springmvc api started!");
        }
    }

    private void doRegisterSpringMvc() {
        Map<String, RequestMappingHandlerMapping> requestMappings =
                BeanFactoryUtils.beansOfTypeIncludingAncestors(applicationContext,
                        RequestMappingHandlerMapping.class,
                        true, false);

        for (RequestMappingHandlerMapping value : requestMappings.values()) {
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = value.getHandlerMethods();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> me : handlerMethods.entrySet()) {
                HandlerMethod handlerMethod = me.getValue();
                Class<?> aClass = handlerMethod.getBeanType();
                Object bean = applicationContext.getBean(aClass);
                if (set.contains(bean)) {
                    continue;
                }

                ServiceDefinition serviceDefinition = ApiAnnotationScanner.getInstance().scanner(bean);

                if (serviceDefinition == null) {
                    continue;
                }

                serviceDefinition.setEnvType(getApiProperties().getEnv());

                //处理服务实例
                ServiceInstance serviceInstance = new ServiceInstance();
                String localIp = NetUtils.getLocalIp();
                int port = serverProperties.getPort();
                String serviceInstanceId = localIp + BasicConst.COLON_SEPARATOR + port;
                String uniqueId = serviceDefinition.getUniqueId();
                String version = serviceDefinition.getVersion();

                serviceInstance.setServiceInstanceId(serviceInstanceId);
                serviceInstance.setUniqueId(uniqueId);
                serviceInstance.setIp(localIp);
                serviceInstance.setPort(port);
                serviceInstance.setRegisterTime(TimeUtil.currentTimeMillis());
                serviceInstance.setVersion(version);
                serviceInstance.setWeight(DEFAULT_WEIGHT);

                //注册服务
                register(serviceDefinition, serviceInstance);
            }
        }

    }
}
