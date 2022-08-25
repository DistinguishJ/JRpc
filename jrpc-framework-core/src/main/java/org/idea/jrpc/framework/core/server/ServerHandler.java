package org.idea.jrpc.framework.core.server;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.idea.jrpc.framework.core.common.RpcInvocation;
import org.idea.jrpc.framework.core.common.RpcProtocol;

import java.lang.reflect.Method;

import static org.idea.jrpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 在接收到客户端发来的数据之后被回调
     * @param ctx
     * @param msg 数据读写的载体
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        String json = new String(rpcProtocol.getContent(), 0, rpcProtocol.getContentLength());
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        Object aimObject = PROVIDER_CLASS_MAP.get(rpcInvocation.getTargetServiceName());
        Method[] methods = aimObject.getClass().getDeclaredMethods();
        Object result = null;
        for(Method method : methods) {
            if(method.getName().equals(rpcInvocation.getTargetMethod())) {
                //通过反射找到目标对象，然后执行目标方法并返回对应值
                if(method.getReturnType().equals(Void.TYPE)) {
                    method.invoke(aimObject, rpcInvocation.getArgs());
                }else {
                    result = method.invoke(aimObject, rpcInvocation.getArgs());
                }
            }
        }

        rpcInvocation.setResponse(result);
        RpcProtocol resPrcProtocol = new RpcProtocol(JSON.toJSONString(rpcInvocation).getBytes());
        ctx.writeAndFlush(resPrcProtocol);      //写数据调用writeAndFlush()方法
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        Channel channel = ctx.channel();
        if(channel.isActive()) {
            ctx.close();
        }
    }
}
