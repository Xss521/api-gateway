package org.xss.gateway.client.core;


import org.xss.common.config.HttpServiceInvoker;
import org.xss.common.config.ServiceDefinition;
import org.xss.common.config.ServiceInvoker;
import org.xss.common.constants.BasicConst;


import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 注解扫描类
 */
public class ApiAnnotationScanner {

    private ApiAnnotationScanner() {
    }

    private static class SingletonHolder {
        static final ApiAnnotationScanner INSTANCE = new ApiAnnotationScanner();
    }

    public static ApiAnnotationScanner getInstance() {
        return SingletonHolder.INSTANCE;
    }


    /**
     * 构建HttpServiceInvoker对象
     */
    private HttpServiceInvoker createHttpServiceInvoker(String path) {
        HttpServiceInvoker httpServiceInvoker = new HttpServiceInvoker();
        httpServiceInvoker.setInvokerPath(path);
        return httpServiceInvoker;
    }


    /**
     * @author: MR.XSS
     * @Params: [bean, args]
     * @return: org.xss.common.config.ServiceDefinition
     * @date 2023/9/20 9:01
     * @描述: 描传入Bean对象上的注解，获取注解参数，最终返回一个ServiceDefinition对象
     */
    public ServiceDefinition scanner(Object bean, Object... args) {
        Class<?> aClass = bean.getClass();
        if (!aClass.isAnnotationPresent(ApiService.class)) {
            return null;
        }
        ApiService annotation = aClass.getAnnotation(ApiService.class);
        String serviceId = annotation.serviceId();
        ApiProtocol protocol = annotation.protocol();
        String patternPath = annotation.patternPath();
        String version = annotation.version();

        ServiceDefinition serviceDefinition = new ServiceDefinition();
        Map<String, ServiceInvoker> invokerMap = new HashMap<>();

        Method[] methods = aClass.getMethods();
        if (methods != null && methods.length > 0) {
            for (Method method : methods) {
                ApiInvoker apiInvoker = method.getAnnotation(ApiInvoker.class);
                if (apiInvoker == null) continue;
                String path = apiInvoker.path();

                HttpServiceInvoker httpServiceInvoker = createHttpServiceInvoker(path);
                invokerMap.put(path, httpServiceInvoker);
            }
            serviceDefinition.setUniqueId(serviceId + BasicConst.COLON_SEPARATOR + version);
            serviceDefinition.setServiceId(serviceId);
            serviceDefinition.setVersion(version);
            serviceDefinition.setProtocol(protocol.getCode());
            serviceDefinition.setPatternPath(patternPath);
            serviceDefinition.setEnable(true);
            serviceDefinition.setInvokerMap(invokerMap);
            return serviceDefinition;
        }
        return null;
    }

}
