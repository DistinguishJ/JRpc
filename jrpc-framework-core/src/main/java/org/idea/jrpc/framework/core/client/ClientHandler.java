package org.idea.jrpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.idea.jrpc.framework.core.common.RpcInvocation;
import org.idea.jrpc.framework.core.common.RpcProtocol;

import static org.idea.jrpc.framework.core.common.cache.CommonClientCache.RESP_MAP;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    //如果想在连接成功后回调，需要重写channelActive(方法

    /**
     * 在接收到服务端发来的数据之后被回调
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcProtocol rpcProtocol = (RpcProtocol) msg;
        byte[] reqContent = rpcProtocol.getContent();
        String json = new String(reqContent, 0, reqContent.length);
        RpcInvocation rpcInvocation = JSON.parseObject(json, RpcInvocation.class);
        if(!RESP_MAP.containsKey(rpcInvocation.getUuid())) {
            throw new IllegalAccessException("server response is error!");
        }
        //Uuid怎么在发送前被注入的？  --->    JDKProxyFactory
        RESP_MAP.put(rpcInvocation.getUuid(), rpcInvocation);
        ReferenceCountUtil.release(msg);
    }





    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Channel channel = ctx.channel();
        if(channel.isActive()) {
            ctx.close();
        }
    }
}
