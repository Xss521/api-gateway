package org.xss.core.context;

import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import org.xss.common.config.Rule;
import org.xss.common.utils.AssertUtil;
import org.xss.core.request.GatewayRequest;
import org.xss.core.response.GatewayResponse;

/**
 * @author MR.XSS
 * @version 1.0
 * 2023/9/13 13:59
 */
public class GatewayContext extends BaseContext {

    public GatewayRequest request;

    public GatewayResponse response;

    public Rule rule;


    public GatewayContext(String protocol,
                          ChannelHandlerContext nettyCtx,
                          boolean keepAlive,
                          GatewayRequest request,
                          Rule rule) {

        super(protocol, nettyCtx, keepAlive);
        this.request = request;
        this.rule = rule;
    }

    /**
     * 构建者类，构建上下文对象
     */
    public static class Builder {
        private String protocol;
        private ChannelHandlerContext nettyCtx;

        private GatewayRequest request;
        private GatewayResponse response;
        private Rule rule;
        private boolean keepAlive;

        public Builder() {
        }

        public Builder setProtocol(String protocol) {
            this.protocol = protocol;
            return this;
        }

        public Builder setNettyCtx(ChannelHandlerContext nettyCtx) {
            this.nettyCtx = nettyCtx;
            return this;
        }

        public Builder setRequest(GatewayRequest request) {
            this.request = request;
            return this;
        }

        public Builder setResponse(GatewayResponse response) {
            this.response = response;
            return this;
        }

        public Builder setRule(Rule rule) {
            this.rule = rule;
            return this;
        }

        public Builder setKeepAlive(boolean keepAlive) {
            this.keepAlive = keepAlive;
            return this;
        }

        public GatewayContext build() {
            AssertUtil.notNull(protocol, "protocol can not null!");
            AssertUtil.notNull(nettyCtx, "nettyCtx can not null!");
            AssertUtil.notNull(request, "request can not null!");
            AssertUtil.notNull(rule, "rule can not null!");

            return new GatewayContext(protocol, nettyCtx, keepAlive, request, rule);
        }
    }

    /**
     * 获取必要上下文参数
     */
    public <T> T getRequireAttribute(String key) {
        T value = getAttribute(key);
        AssertUtil.notNull(value, "缺少必要参数！");
        return value;
    }

    /**
     * 获取指定key的上下文参数,没有的话返回默认值
     */
    public <T> T getRequireAttribute(String key, T defaultVal) {
        return (T) attributes.getOrDefault(key, defaultVal);
    }

    /**
     * 获取指定的过滤器对象
     */
    public Rule.FilterConfig getFilterConfig(String filterId) {
        return rule.getFilterConfig(filterId);
    }

    /**
     * 获取服务ID
     */
    public String getUniqueId() {
        return request.getUniqueId();
    }


    /**
     * @author ==> 许帅帅
     * @return: void
     * @date 2023/9/19 13:00
     *@功能描述: 重写覆盖父类，真正的释放资源，
     * 在Netty中，HttpServletRequest对象是一个长连接，需要手动关闭以释放资源。ReferenceCountUtil.release()方法可以用于释放对象占用的内存空间，避免内存泄漏
     */
    @Override
    public void releaseRequest() {
        if (requestReleased.compareAndSet(false, true)) {
            ReferenceCountUtil.release(request.getFullHttpRequest());
        }
    }

    @Override
    public GatewayRequest getRequest() {
        return request;
    }

    public void setRequest(GatewayRequest request) {
        this.request = request;
    }

    @Override
    public GatewayResponse getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = (GatewayResponse) response;
    }


    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }
}
