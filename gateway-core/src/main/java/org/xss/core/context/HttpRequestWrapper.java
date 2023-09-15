package org.xss.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.Data;

/**
 * @author MR.XSS
 * 2023/9/15 17:32
 * <h3>包装器</h3>
 */
@Data
public class HttpRequestWrapper {
    private FullHttpRequest request;
    private ChannelHandlerContext context;
}
