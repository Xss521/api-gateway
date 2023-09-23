package org.xss.core.netty.process;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.xss.common.enums.ResponseCode;
import org.xss.common.exception.BaseException;
import org.xss.common.exception.ConnectException;
import org.xss.common.exception.ResponseException;
import org.xss.core.ConfigLoader;
import org.xss.core.context.GatewayContext;
import org.xss.core.context.HttpRequestWrapper;
import org.xss.core.filter.FilterFactory;
import org.xss.core.filter.GatewayFilterChainFactory;
import org.xss.core.help.AsyncHttpHelper;
import org.xss.core.help.RequestHelper;
import org.xss.core.help.ResponseHelper;
import org.xss.core.response.GatewayResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

/**
 * @author MR.XSS
 * 2023/9/15 21:40
 */
@Slf4j
public class NettyCoreProcessor implements NettyProcessor {

    /**
     * 获取到过滤器工厂
     */
    private FilterFactory filterFactory = GatewayFilterChainFactory.getInstance();

    @Override
    public void process(HttpRequestWrapper wrapper) {
        FullHttpRequest request = wrapper.getRequest();
        ChannelHandlerContext context = wrapper.getContext();

        try {
            //进行类型转换，转换为Gateway上下文对象
            GatewayContext gatewayContext = RequestHelper.doContext(request, context);

            //执行过滤器规则
            filterFactory.buildFilterChain(gatewayContext).doFilter(gatewayContext);
        } catch (BaseException e) {
            log.error("process error {} {}", e.getCode().getCode(), e.getCode().getMessage());
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(e.getCode());
            doWriteAndRelease(context, request, httpResponse);
        } catch (Throwable throwable) {
            log.error("process unknown error", throwable);
            FullHttpResponse httpResponse = ResponseHelper.getHttpResponse(ResponseCode.INTERNAL_ERROR);
            doWriteAndRelease(context, request, httpResponse);
        }
    }

    /**
     * @author: MR.XSS
     * @Params: [context, request, httpResponse]
     * @return: void
     * @date 2023/9/19 13:24
     * @描述: 写回响应结果，然后释放内存
     */
    private void doWriteAndRelease(ChannelHandlerContext context, FullHttpRequest request, FullHttpResponse httpResponse) {
        context.writeAndFlush(httpResponse)
                .addListener(ChannelFutureListener.CLOSE); //释放资源后关闭Channel
        ReferenceCountUtil.release(request);
    }



}
