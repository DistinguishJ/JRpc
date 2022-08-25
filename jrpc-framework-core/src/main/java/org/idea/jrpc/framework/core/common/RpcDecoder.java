package org.idea.jrpc.framework.core.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

import static org.idea.jrpc.framework.core.common.constants.RpcConstants.MAGIC_NUMBER;
public class RpcDecoder extends ByteToMessageDecoder {

    //协议的开头部分的标准长度
    public final int BASE_LENGTH = 2+4;

    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object>out) throws Exception {
        if(byteBuf.readableBytes() >= BASE_LENGTH) {
            //防止收到一些提及过大的数据包 目前限制在1000大小，后期版本可配置
            if(byteBuf.readableBytes() > 1000) {
                byteBuf.skipBytes(byteBuf.readableBytes());
            }
            int beginReader;
            while(true) {
                beginReader = byteBuf.readerIndex();
                byteBuf.markReaderIndex();
                if(byteBuf.readShort() == MAGIC_NUMBER) {
                    break;
                }else {
                    ctx.close();    //不是魔数开头，说明是非法的客户端发来的数据包
                    return;
                }
            }

            int length = byteBuf.readInt();
            //说明剩余的数据包不是完整的，这里需要重置下读索引
            if(byteBuf.readableBytes() < length) {
                byteBuf.readerIndex(beginReader);
                return;
            }
            byte[] data = new byte[length];
            byteBuf.readBytes(data);
            RpcProtocol rpcProtocol = new RpcProtocol(data);
            out.add(rpcProtocol);
        }
    }
}
