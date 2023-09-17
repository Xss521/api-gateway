package org.xss.core;

import lombok.extern.slf4j.Slf4j;
import org.xss.core.netty.NettyHttpClient;
import org.xss.core.netty.NettyHttpServer;
import org.xss.core.netty.process.NettyCoreProcessor;
import org.xss.core.netty.process.NettyProcessor;

/**
 * @author MR.XSS
 * 2023/9/16 16:26
 */
@Slf4j
public class Container implements LifeCycle {
    private final Config config;

    private NettyHttpServer nettyHttpServer;

    private NettyHttpClient nettyHttpClient;

    private NettyProcessor nettyProcessor;

    public Container(Config config) {
        this.config = config;
        init();
    }

    @Override
    public void init() {
        this.nettyProcessor = new NettyCoreProcessor();

        this.nettyHttpServer = new NettyHttpServer(config, nettyProcessor);

        this.nettyHttpClient = new NettyHttpClient(config, nettyHttpServer.getEventLoopGroupWorker());
    }

    @Override
    public void start() {
        nettyHttpServer.start();
        nettyHttpClient.start();
        log.info("api gateway start!");
    }

    @Override
    public void shutDown() {
        nettyHttpServer.shutDown();
        nettyHttpClient.shutDown();
    }
}
