package org.xss.core.context;

import io.netty.channel.ChannelHandlerContext;
import org.xss.common.config.Rule;
import org.xss.core.request.GatewayRequest;

/**
 * @author MR.XSS
 * @version 1.0
 * 2023/9/13 13:59
 */
public class GatewayContext extends BaseContext{

    public GatewayRequest request;

    public GatewayResponse response;

    public Rule rule;



    public GatewayContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        super(protocol, nettyCtx, keepAlive);
    }
}
