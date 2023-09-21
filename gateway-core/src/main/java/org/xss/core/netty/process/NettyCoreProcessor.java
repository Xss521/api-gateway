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

    /**
     * @author: MR.XSS
     * @Params: [gatewayContext]
     * @return: void
     * @date 2023/9/19 13:17
     * @描述: 使用AsyncHttpClient进行对Request请求进行处理，实现异步通讯，实现大吞吐量
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
     * @author: MR.XSS
     * @Params: [request, response, throwable, ctx]
     * @return: void
     * @date 2023/9/19 13:14
     * @描述: 完成对服务请求的处理，并且写回服务响应结果，若在途中发现异常，对异常进行处理
     */
    private void complete(Request request,
                          Response response,
                          Throwable throwable,
                          GatewayContext ctx) {
        //处理完成Request请求时，释放请求资源，避免造成内存泄漏
        ctx.releaseRequest();
        try {
            //出现异常，将异常设置到上席文对象中去
            if (Objects.nonNull(throwable)) {
                String url = request.getUrl();
                if (throwable instanceof TimeoutException) {
                    log.warn("complete time is out {}", url);
                    ctx.setThrowable(new ResponseException(ResponseCode.REQUEST_TIMEOUT));
                } else {
                    ctx.setThrowable(new ConnectException(throwable, ctx.getUniqueId(), url, ResponseCode.HTTP_RESPONSE_ERROR));
                }
            } else {
                //访问成功，将返回结果设置到上下文对象
                ctx.setResponse(GatewayResponse.buildGatewayResponse(response));
            }
        } catch (Exception e) {
            ctx.setThrowable(new ResponseException(ResponseCode.INTERNAL_ERROR));
            log.error("complete error ", e);
        } finally {
            //处理结束，标记为写回状态，并且将响应结果返回
            ctx.written();
            ResponseHelper.writeResponse(ctx);
        }
    }
}
