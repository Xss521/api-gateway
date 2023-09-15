package org.xss.core;

import lombok.Data;

/**
 * @author MR.XSS
 * 2023/9/15 14:38
 */
@Data
public class Config {
    private int port = 12306;

    private String applicationName = "API-GATEWAY";

    private String registerAddr = "localhost:8848";

    private String env = "dev";

    //Netty
    private int eventLoopGroupBossNum = 1;

    //机器线程数
    private int eventLoopGroupWorkNum = Runtime.getRuntime().availableProcessors();

    private int maxContextLength = 64 * 1024 * 1024;

    //默认单异步模式
    private boolean whenComplete = true;


}
