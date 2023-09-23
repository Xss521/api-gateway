package org.xss.core.filter.router;

import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.xss.common.enums.ResponseCode;
import org.xss.common.exception.ConnectException;
import org.xss.common.exception.ResponseException;
import org.xss.core.ConfigLoader;
import org.xss.core.context.GatewayContext;
import org.xss.core.filter.Filter;
import org.xss.core.filter.FilterAspect;
import org.xss.core.help.AsyncHttpHelper;
import org.xss.core.help.ResponseHelper;
import org.xss.core.request.GatewayRequest;
import org.xss.core.response.GatewayResponse;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static org.xss.common.constants.FilterConst.*;

/**
 * @author: MR.XSS
 * @date 2023/9/22 17:49
 * @描述: 路由过滤器
 */
@Slf4j
@FilterAspect(id = ROUTER_FILTER_ID,
        name = ROUTER_FILTER_NAME,
        order = ROUTER_FILTER_ORDER)
public class RouterFilter implements Filter {
    @Override
    public void doFilter(GatewayContext gatewayContext) throws Exception{
        GatewayRequest gatewayRequest = gatewayContext.getRequest();
        Request request = gatewayRequest.build();
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
