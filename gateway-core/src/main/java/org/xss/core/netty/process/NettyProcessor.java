package org.xss.core.netty.process;

import org.xss.core.context.HttpRequestWrapper;

/**
 * @author MR.XSS
 * 2023/9/15 17:36
 */
public interface NettyProcessor {
    void process(HttpRequestWrapper wrapper);
}
