package org.xss.core;

/**
 * @author MR.XSS
 * 2023/9/15 16:18
 * <h3>网关生命周期方法</h3>
 */
public interface LifeCycle {
    /**
     * 初始化
     */
    void init();
    /**
     * 启动
     */
    void start();
    /**
     * 关闭
     */
    void shutDown();
}
