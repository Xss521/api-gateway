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

    @Override
    public void process(HttpRequestWrapper wrapper) {
        FullHttpRequest request = wrapper.getRequest();
        ChannelHandlerContext context = wrapper.getContext();

        try {
            //进行类型转换，转换为Gateway上下文对象
            GatewayContext gatewayContext = RequestHelper.doContext(request, context);
            route(gatewayContext);
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

    private void doWriteAndRelease(ChannelHandlerContext context, FullHttpRequest request, FullHttpResponse httpResponse) {
        context.writeAndFlush(httpResponse)
                .addListener(ChannelFutureListener.CLOSE); //释放资源后关闭Channel

        ReferenceCountUtil.release(request);
    }

    /**
     * 路由请求转发
     */
    private void route(GatewayContext gatewayContext) {
        Request request = gatewayContext.getRequest().build();
        CompletableFuture<Response> future = AsyncHttpHelper.getInstance().executeRequest(request);

        //判断是否是单异步模式
        boolean complete = ConfigLoader.getConfig().isWhenComplete();
        if (complete) {
            future.whenComplete((response, throwable) -> complete(request, response, throwable, gatewayContext));
        } else {
            future.whenCompleteAsync((response, throwable) -> complete(request, response, throwable, gatewayContext));
        }
    }

    /**
     * 对请求进行处理
     */
    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          GatewayContext ctx) {
        //释放请求资源
        ctx.releaseRequest();
        try {
            //出现异常
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time is out {}", url);
                    ctx.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    ctx.setThrowable(new ConnectException(throwable, ctx.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                //返回响应结果
                ctx.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Exception e) {
            ctx.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("complete error ", e);
        } finally {
            ctx.written();
            ResponseHelper.writeResponse(ctx);
        }
    }
}