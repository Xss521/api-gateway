package org.xss.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xss.common.utils.RemotingUtil;
import org.xss.core.Config;
import org.xss.core.LifeCycle;
import org.xss.core.netty.process.NettyProcessor;

import java.net.InetSocketAddress;

/**
 * @author MR.XSS
 * 2023/9/15 16:32
 */
@Slf4j
public class NettyHttpServer implements LifeCycle {
    private final Config config;

    //Netty相关配置
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup eventLoopGroupBoss;
    @Getter
    private EventLoopGroup eventLoopGroupWorker;

    private final NettyProcessor nettyProcessor;


    public NettyHttpServer(Config config, NettyProcessor nettyProcessor) {
        this.config = config;
        this.nettyProcessor = nettyProcessor;
        init();
    }

    @Override
    public void init() {
        if (useEpoll()) {
            this.serverBootstrap = new ServerBootstrap();
            this.eventLoopGroupBoss = new EpollEventLoopGroup(config.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopGroupWorker = new EpollEventLoopGroup(config.getEventLoopGroupWorkNum(), new DefaultThreadFactory("netty-work-nio"));
        } else {
            this.serverBootstrap = new ServerBootstrap();
            this.eventLoopGroupBoss = new NioEventLoopGroup(config.getEventLoopGroupBossNum(), new DefaultThreadFactory("netty-boss-nio"));
            this.eventLoopGroupWorker = new NioEventLoopGroup(config.getEventLoopGroupWorkNum(), new DefaultThreadFactory("netty-work-nio"));
        }
    }


    @Override
    public void start() {
        this.serverBootstrap
                .group(eventLoopGroupBoss, eventLoopGroupWorker)
                .channel(useEpoll() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(
                                new HttpServerCodec(),
                                new HttpObjectAggregator(config.getMaxContextLength()),
                                new NettyServerConnectManagerHandler(),
                                new NettyHttpServerHandler(nettyProcessor)
                        );
                    }
                });
        try {
            this.serverBootstrap.bind().sync();
            log.info("server startup on port {}", config.getPort());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean useEpoll() {
        return RemotingUtil.isLinuxPlatform() && Epoll.isAvailable();
    }

    @Override
    public void shutDown() {
        if (eventLoopGroupBoss != null){
            eventLoopGroupBoss.shutdownGracefully();
        }
        if (eventLoopGroupWorker != null){
            eventLoopGroupWorker.shutdownGracefully();
        }
    }
}
