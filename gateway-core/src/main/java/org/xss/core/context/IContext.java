package org.xss.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.function.Consumer;

/**
 * @author MR.XSS
 * @version 1.0
 * 2023/9/13 10:31
 */
public interface IContext {
    /**
     * 上下文生命周期，运行中状态
     */
    int RUNNING = 0;

    /**
     * 运行过程中发生错误，对其进行标记，告诉请求结束，需要返回客户端
     */
    int WRITTEN = 1;

    /**
     * 标记协会成功,防止并发情况下多次写回
     */
    int COMPLETED = 2;

    /**
     * 表示网关请求结束
     */
    int TERMINATED = -1;

    /**
     * 设置上下文状态运行中
     */
    void running();


    /**
     * 设置上下文标记为写回
     */
    void written();

    /**
     * 设置上下文标记写回成功
     */
    void completed();

    /**
     * 设置上下文请求结束
     */
    void terminated();


    /**
     * 判断网关状态
     */
    boolean isRunning();
    boolean isWritten();
    boolean isCompleted();
    boolean isTerminated();


    /**
     * 判断协议，获取协议类型
     */
    String getProtocol();


    /**
     * 获取请求对象
     */
    Object getRequest();


    /**
     * 获取返回对象
     */
    Object getResponse();

    /**
     * 设置返回对象
     */
    void setResponse(Object response);

    /**
     * 设置异常对象
     */
    void setThrowable(Throwable e);

    Throwable getThrowable();

    /**
     * 获取上下文参数
     * @param key
     * @return
     * @param <T>
     */
    <T> T getAttribute(String key);

    /**
     *
     * @param key
     * @param value
     * @return
     * @param <T>
     */
    <T> T putAttribute(String key, T value);

    /**
     * 获取netty上下文
     */
    ChannelHandlerContext getNettyCtx();

    //
    boolean isKeepAlive();

    /**
     * 释放资源
     */
    void releaseRequest();

    /**
     * 设置写回接收回调函数
     */
    void setCompletedCallBack(Consumer<IContext> consumer);

    /**
     * 执行回调函数
     */
    void invokeCompletedCallBack();

}
