package org.xss.core.filter;

import java.lang.annotation.*;

/**
 * @author MR.XSS
 * 2023/9/21 16:14
 * 过滤器注解类
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterAspect {
    /**
     * 过滤器ID
     */
    String id();

    /**
     * 过滤器名称
     */
    String name() default "";

    /**
     * 过滤器排序
     */
    int order() default 0;

}

