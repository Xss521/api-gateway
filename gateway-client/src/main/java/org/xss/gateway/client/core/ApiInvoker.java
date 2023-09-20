package org.xss.gateway.client.core;

import java.lang.annotation.*;

/**
 * @author MR.XSS
 * 2023/9/18 12:09
 * <h3>必须在服务方法上使用</h3>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiInvoker {
    String path();
}
