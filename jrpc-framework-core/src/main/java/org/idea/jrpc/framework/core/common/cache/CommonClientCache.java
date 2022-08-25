package org.idea.jrpc.framework.core.common.cache;

import org.idea.jrpc.framework.core.common.RpcInvocation;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class CommonClientCache {
    public static BlockingQueue<RpcInvocation> SEND_QUEUE = new ArrayBlockingQueue<>(100);
    //键为RpcInvocation的UUid，值为属于类对象的Object，每次请求的UUid都不同用于区分，可以将请求和响应进行关联匹配，方便我们在客户端接收数据的时候进行识别
    public static Map<String, Object> RESP_MAP = new ConcurrentHashMap<>();
}
