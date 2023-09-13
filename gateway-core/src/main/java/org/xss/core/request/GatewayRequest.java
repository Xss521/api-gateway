package org.xss.core.request;

import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import lombok.Getter;
import org.asynchttpclient.cookie.Cookie;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.Request;
import org.asynchttpclient.RequestBuilder;
import org.xss.common.constants.BasicConst;
import org.xss.common.utils.TimeUtil;

import java.nio.charset.Charset;
import java.util.*;

/**
 * @author MR.XSS
 * @version 1.0
 * 2023/9/13 16:58
 */
public class GatewayRequest implements IGatewayRequest {

    /**
     * 服务唯一ID
     */
    @Getter
    private final String uniqueId;

    /**
     * 进入网关开始时间
     */
    @Getter
    private final long beginTime;


    /**
     * 字符集
     */
    private final Charset charset;

    /**
     * 客户端IP地址
     */
    @Getter
    private final String clientIp;

    /**
     * 服务端主机
     */
    @Getter
    private final String host;

    /**
     * 服务端请求路径
     */
    @Getter
    private final String path;

    /**
     * 统一资源标识符
     */
    @Getter
    private final String uri;

    /**
     * 请求方式
     */
    @Getter
    private final HttpMethod httpMethod;

    /**
     * 请求格式
     */
    @Getter
    private final String contentType;


    /**
     * 请求头
     */
    @Getter
    private final HttpHeaders httpHeaders;

    /**
     * 参数解析器
     */
    @Getter
    private final QueryStringDecoder queryStringDecoder;

    //fullHttpRequest
    @Getter
    private final FullHttpRequest fullHttpRequest;

    /**
     * 请求体
     */
    private String body;

    private Map<String, io.netty.handler.codec.http.cookie.Cookie> cookieMap;

    /**
     * post请求参数
     */
    private Map<String, List<String>> postParameters;

    /**
     * 可修改的Scheme默认为http协议
     */
    private String modifyScheme;

    /**
     * 可以修改的主机地址
     */
    private String modifyHost;

    /**
     * 可以修改的路径
     */
    private String modifyPath;

    /**
     * 构建下游请求时的HTTP构建器
     */
    private final RequestBuilder requestBuilder;

    public GatewayRequest(String uniqueId,
                          Charset charset,
                          String clientIp,
                          String host,
                          String path,
                          String uri,
                          HttpMethod httpMethod,
                          String contentType,
                          HttpHeaders httpHeaders,
                          QueryStringDecoder queryStringDecoder,
                          FullHttpRequest fullHttpRequest) {
        this.uniqueId = uniqueId;
        this.beginTime = TimeUtil.currentTimeMillis();
        this.charset = charset;
        this.clientIp = clientIp;
        this.host = host;
        this.uri = uri;
        this.httpMethod = httpMethod;
        this.contentType = contentType;
        this.httpHeaders = httpHeaders;
        this.queryStringDecoder = new QueryStringDecoder(uri, charset);
        this.path = queryStringDecoder.path();
        this.fullHttpRequest = fullHttpRequest;

        this.modifyHost = host;
        this.modifyPath = path;
        this.modifyScheme = BasicConst.HTTP_PREFIX_SEPARATOR;

        this.requestBuilder = new RequestBuilder();
        this.requestBuilder.setMethod(getHttpMethod().name());
        this.requestBuilder.setHeaders(getHttpHeaders());
        this.requestBuilder.setQueryParams(queryStringDecoder.parameters());

        ByteBuf contextBuffer = fullHttpRequest.content();

        if (Objects.nonNull(contextBuffer)) {
            this.requestBuilder.setBody(contextBuffer.nioBuffer());
        }
    }

    /**
     * 获取请求体
     */
    public String getBody() {
        if (StringUtils.isEmpty(body)) {
            body = fullHttpRequest.content().toString(charset);
        }
        return body;
    }

    /**
     * 获取cookie
     */
    public io.netty.handler.codec.http.cookie.Cookie getCookie(String name) {
        if (cookieMap == null) {
            cookieMap = new HashMap<>();
            String cookieStr = getHttpHeaders().get(HttpHeaderNames.COOKIE);
            Set<io.netty.handler.codec.http.cookie.Cookie> cookies = ServerCookieDecoder.STRICT.decode(cookieStr);

            for (io.netty.handler.codec.http.cookie.Cookie cookie : cookies) {
                cookieMap.put(name, cookie);
            }
        }
        return cookieMap.get(name);
    }

    /**
     * 获取指定名称的参数值
     */
    public List<String> getQueryParametersMultiple(String name) {
        return queryStringDecoder.parameters().get(name);
    }

    //获取post请求参数
    public List<String> getPostParametersMultiple(String name) {
        String body = getBody();
        if (isFormPost()) {
            if (postParameters == null) {
                QueryStringDecoder paramDecoder = new QueryStringDecoder(body, false);
                postParameters = paramDecoder.parameters();
            }
            if (postParameters == null || postParameters.isEmpty()) {
                return null;
            } else {
                return postParameters.get(name);
            }
        } else if (isJsonPost()) {
            return Lists.newArrayList(JsonPath.read(body, name).toString());
        }
        return null;
    }


    /**
     * 判断是否是表单数据
     */
    private boolean isFormPost() {
        return HttpMethod.POST.equals(httpMethod) &&
                (contentType.startsWith(HttpHeaderValues.FORM_DATA.toString()) ||
                        contentType.startsWith(HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString()));
    }

    /**
     * 判断是否是JSON数据
     */
    private boolean isJsonPost() {
        return HttpMethod.POST.equals(httpMethod) && contentType.startsWith(HttpHeaderValues.APPLICATION_JSON.toString());
    }

    @Override
    public void setModifyHost(String host) {
    }

    @Override
    public String getModifyHost() {
        return null;
    }

    @Override
    public void setModifyPath(String path) {

    }

    @Override
    public String getModifyPath() {
        return null;
    }

    @Override
    public void addHeader(CharSequence name, String value) {

    }

    @Override
    public void setHeader(CharSequence name, String value) {

    }

    @Override
    public void addQueryParam(String name, String value) {

    }

    @Override
    public void addFormParam(String name, String value) {

    }

    @Override
    public void addOrReplaceCookie(Cookie cookie) {

    }

    @Override
    public void setRequestTimeout(int requestTimeout) {

    }

    @Override
    public String getFinalUrl(int requestTimeout) {
        return null;
    }

    @Override
    public Request build() {
        return null;
    }
}
