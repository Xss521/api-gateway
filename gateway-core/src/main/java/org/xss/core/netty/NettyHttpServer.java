package org.xss.core.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
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
                .option(ChannelOption.SO_BACKLOG, 1024)			//	sync + accept = backlog
                .option(ChannelOption.SO_REUSEADDR, true)   	//	tcp端口重绑定
                .option(ChannelOption.SO_KEEPALIVE, false)  	//  如果在两小时内没有数据通信的时候，TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.TCP_NODELAY, true)   //	该参数的左右就是禁用Nagle算法，使用小数据传输时合并
                .childOption(ChannelOption.SO_SNDBUF, 65535)	//	设置发送数据缓冲区大小
                .childOption(ChannelOption.SO_RCVBUF, 65535)	//	设置接收数据缓冲区大小
                .localAddress(new InetSocketAddress(config.getPort()))
                .childHandler(new ChannelInitializer<Channel>() {
                    @Override
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(
                                //http消息编解码器，但是只能处理请求中的URI中的数据，无法处理请求体中的内容
                                new HttpServerCodec(),
                                //可以对POST请求中的请求体内容进行处理
                                new HttpObjectAggregator(config.getMaxContextLength()),
                                //自定义Handler
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
