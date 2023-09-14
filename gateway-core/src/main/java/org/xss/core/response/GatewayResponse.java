package org.xss.core.response;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.netty.handler.codec.http.*;
import lombok.Data;
import org.asynchttpclient.Response;
import org.xss.common.enums.ResponseCode;
import org.xss.common.utils.JSONUtil;

/**
 * @author MR.XSS
 * 2023/9/14 9:40
 * <h3>网关恢复消息对象</h3>
 */
@Data
public class GatewayResponse {
    /**
     * 响应头信息
     */
    private HttpHeaders responseHeads = new DefaultHttpHeaders();

    /**
     * 额外响应头信息
     */
    private HttpHeaders extraResponseHeaders = new DefaultHttpHeaders();

    /**
     * 响应内容
     */
    private String context;

    /**
     * 响应状态码
     */
    private HttpResponseStatus httpResponseStatus;

    /**
     * 异步响应返回对象
     */
    private Response futureResponse;

    public GatewayResponse() {
    }

    /**
     * 设置响应头信息
     */
    public void putHeader(CharSequence key, CharSequence value) {
        this.responseHeads.add(key, value);
    }

    public static GatewayResponse buildGatewayResponse(Response futureResponse) {
        GatewayResponse response = new GatewayResponse();
        response.setFutureResponse(futureResponse);
        response.setHttpResponseStatus(HttpResponseStatus.valueOf(futureResponse.getStatusCode()));
        return response;
    }

    /**
     * 返回json数据失败情况
     */
    public static GatewayResponse buildGatewayResponse(ResponseCode responseCode, Object... args) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, responseCode.getStatus().code());
        objectNode.put(JSONUtil.CODE, responseCode.getCode());
        objectNode.put(JSONUtil.MESSAGE, responseCode.getMessage());

        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(responseCode.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        response.setContext(JSONUtil.toJSONString(objectNode));

        return response;
    }

    /**
     * 返回JSON数据成功的情况
     */
    public static GatewayResponse buildGatewayResponse(Object... args) {
        ObjectNode objectNode = JSONUtil.createObjectNode();
        objectNode.put(JSONUtil.STATUS, ResponseCode.SUCCESS.getStatus().code());
        objectNode.put(JSONUtil.STATUS,ResponseCode.SUCCESS.getCode());
        objectNode.putPOJO(JSONUtil.DATA,args);

        GatewayResponse response = new GatewayResponse();
        response.setHttpResponseStatus(ResponseCode.SUCCESS.getStatus());
        response.putHeader(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_JSON + ";charset=utf-8");
        response.setContext(JSONUtil.toJSONString(objectNode));

        return response;
    }


}
