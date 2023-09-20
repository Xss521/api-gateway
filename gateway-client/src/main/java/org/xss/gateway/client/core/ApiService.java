package org.xss.gateway.client.core;

import java.lang.annotation.*;

/**
 * @author MR.XSS
 * 2023/9/18 12:03
 * <h3>服务定义</h3>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiService {
    String serviceId();

    String version() default "1.0.0";

    ApiProtocol protocol();

    String patternPath();
}
