package org.xss.core.request;

import org.asynchttpclient.Request;
import org.asynchttpclient.cookie.Cookie;

/**
 * @author MR.XSS
 * @version 1.0
 * 2023/9/13 16:35
 * <h3>提供可修改的Request接口<h1/>
 */
public interface IGatewayRequest {

    /**
     * 修改目标服务主机
     */
    void setModifyHost(String host);

    /**
     * 获取目标主机地址
     */
    String getModifyHost();

    /**
     * 设置目标服务路径
     */
    void setModifyPath(String path);

    /**
     * 获取目标服务路径
     */
    String getModifyPath();

    /**
     * 添加请求头
     */
    void addHeader(CharSequence name, String value);

    /**
     * 设置请求头
     */
    void setHeader(CharSequence name, String value);

    /**
     * 添加GET请求参数
     */
    void addQueryParam(String name, String value);

    /**
     * 添加表单请求
     */
    void addFormParam(String name, String value);

    /**
     * 添加或替换Cookie
     */
    void addOrReplaceCookie(Cookie cookie);

    /**
     * 设置超时时间
     */
    void setRequestTimeout(int requestTimeout);

    /**
     * 获取最终请求路径，包含请求参数
     */
    String getFinalUrl(int requestTimeout);

    /**
     * 构建请求
     */
    Request build();
}
