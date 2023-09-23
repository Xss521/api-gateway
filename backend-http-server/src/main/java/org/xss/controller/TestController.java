package org.xss.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xss.gateway.client.core.ApiInvoker;
import org.xss.gateway.client.core.ApiProperties;
import org.xss.gateway.client.core.ApiProtocol;
import org.xss.gateway.client.core.ApiService;

import javax.annotation.Resource;

/**
 * @author MR.XSS
 * 2023/9/16 17:08
 */
@RestController
@ApiService(serviceId = "backend-http-server",protocol = ApiProtocol.HTTP,patternPath = "/http-server/**")
public class TestController {

    @Resource
    private ApiProperties apiProperties;

    @ApiInvoker(path = "/http-server/ping")
    @GetMapping("/http-server/ping")
    public String ping() {
        System.out.println(apiProperties);
        System.out.println("我被访问了");
        return "pong";
    }
}
