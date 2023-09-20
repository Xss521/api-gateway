package org.xss.gateway.client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author MR.XSS
 * 2023/9/20 10:14
 */
@Data
@ConfigurationProperties(prefix = "api")
public class ApiProperties {
    private String registerAddr;

    private String env = "dev";
}
