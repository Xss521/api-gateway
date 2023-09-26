package org.xss.gateway.client.core.autoconfigure;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.xss.gateway.client.core.ApiProperties;
import org.xss.gateway.client.support.springmvc.SpringMVCClientRegisterManager;

import javax.annotation.Resource;
import javax.servlet.Servlet;

/**
 * @author MR.XSS
 * 2023/9/20 16:56
 */
@Configuration
@EnableConfigurationProperties(ApiProperties.class)
@ConditionalOnProperty(prefix = "api",name = {"registerAddr"})
public class ApiClientAutoConfiguration {

    @Resource
    private ApiProperties apiProperties;

    @Bean
    @ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
    @ConditionalOnMissingBean(SpringMVCClientRegisterManager.class)
    public SpringMVCClientRegisterManager springMVCClientRegisterManager(){
        return new SpringMVCClientRegisterManager(apiProperties);
    }

}
