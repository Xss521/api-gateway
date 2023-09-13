package org.xss.core.context;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author MR.XSS
 * @version 1.0
 * 2023/9/13 13:30
 */
public class BaseContext implements IContext {

    //协议类型
    protected final String protocol;

    //多线程条件下考虑使用volatile关键字
    protected volatile int status = IContext.RUNNING;

    //Netty上下文
    protected final ChannelHandlerContext nettyCtx;

    //上下文参数
    protected Map<String, Object> attributes = new HashMap<>();

    //请求过程中异常
    protected Throwable throwable;

    //是否保持长连接
    protected final boolean keepAlive;

    //存放回调函数集合
    protected List<Consumer<IContext>> completedCallBacks;

    //定义是否已经释放资源
    protected final AtomicBoolean requestReleased = new AtomicBoolean(false);

    public BaseContext(String protocol, ChannelHandlerContext nettyCtx, boolean keepAlive) {
        this.protocol = protocol;
        this.nettyCtx = nettyCtx;
        this.keepAlive = keepAlive;
    }

    @Override
    public void running() {
        status = IContext.RUNNING;
    }

    @Override
    public void written() {
        status = IContext.WRITTEN;
    }

    @Override
    public void completed() {
        status = IContext.COMPLETED;
    }

    @Override
    public void terminated() {
        status = IContext.TERMINATED;
    }

    @Override
    public boolean isRunning() {
        return status == IContext.RUNNING;
    }

    @Override
    public boolean isWritten() {
        return status == IContext.WRITTEN;
    }

    @Override
    public boolean isCompleted() {
        return status == IContext.COMPLETED;
    }

    @Override
    public boolean isTerminated() {
        return status == IContext.TERMINATED;
    }

    @Override
    public String getProtocol() {
        return this.protocol;
    }

    @Override
    public Object getRequest() {
        return null;
    }

    @Override
    public Object getResponse() {
        return null;
    }

    @Override
    public Object setResponse(Object response) {
        return null;
    }

    @Override
    public void setThrowable(Throwable e) {
        this.throwable = e;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public ChannelHandlerContext getContext() {
        return this.nettyCtx;
    }

    @Override
    public boolean isKeepAlive() {
        return this.keepAlive;
    }

    @Override
    public boolean releaseRequest() {
        return this.requestReleased.get();
    }

    @Override
    public void setCompletedCallBack(Consumer<IContext> consumer) {
        if (completedCallBacks == null) {
            completedCallBacks = new ArrayList<>();
        }
        completedCallBacks.add(consumer);
    }

    @Override
    public void invokeCompletedCallBack(Consumer<IContext> consumer) {
            if (completedCallBacks != null){
                completedCallBacks.forEach(call -> call.accept(this));
            }
    }
}
