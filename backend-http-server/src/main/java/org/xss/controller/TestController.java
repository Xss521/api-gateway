package org.xss.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author MR.XSS
 * 2023/9/16 17:08
 */
@RestController
public class TestController {

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
