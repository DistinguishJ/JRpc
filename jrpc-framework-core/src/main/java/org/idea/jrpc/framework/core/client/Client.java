package org.idea.jrpc.framework.core.client;

import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Data;
import org.idea.jrpc.framework.core.common.RpcDecoder;
import org.idea.jrpc.framework.core.common.RpcEncoder;
import org.idea.jrpc.framework.core.common.RpcInvocation;
import org.idea.jrpc.framework.core.common.RpcProtocol;
import org.idea.jrpc.framework.core.common.config.ClientConfig;
import org.idea.jrpc.framework.core.proxy.jdk.JDKProxyFactory;
import org.idea.jrpc.framework.interfaces.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.idea.jrpc.framework.core.common.cache.CommonClientCache.SEND_QUEUE;


@Data
public class Client {
    private Logger logger = LoggerFactory.getLogger(Client.class);

    private ClientConfig clientConfig;

    public RpcReference startClientApplication() throws InterruptedException {
        EventLoopGroup clientgroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(clientgroup);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            protected void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ClientHandler());
            }
        });
        //异步连接服务端，调用connect发起异步连接，sync同步方法等待连接成功
        ChannelFuture channelFuture = bootstrap.connect(clientConfig.getServerAddr(), clientConfig.getPort()).sync();
        logger.info("=============服务启动=============");
        this.startClient(channelFuture);
        //注入了一个代理工厂
        //客户端首先通过一个代理工厂获取被调用对象的代理对象，然后通过代理对象将数据放入发送队列，最后会有一个异步线程
        //将发送队列内部的数据一个个地发送给到服务端，并且等待服务端响应的数据结果。
        RpcReference rpcReference = new RpcReference(new JDKProxyFactory());
        return rpcReference;
    }


    /**
     * 开启发送线程，专门从事将数据包发送给服务端，起到一个解耦的功能
     * @param channelFuture
     */
    private void startClient(ChannelFuture channelFuture) {
        Thread asyncSendJob = new Thread(new AsyncSendJob(channelFuture));
        asyncSendJob.start();
    }

    /**
     * 异步发送信息任务
     */
    class AsyncSendJob implements Runnable {
        private ChannelFuture channelFuture;

        public AsyncSendJob(ChannelFuture channelFuture) {
            this.channelFuture = channelFuture;
        }

        @Override
        public void run() {
            while(true) {
                try {
                    //阻塞模式
                    RpcInvocation data = SEND_QUEUE.take();
                    //将RpcInvocation封装到一个RpcProtocol对象，然后发送给服务端
                    String json = JSON.toJSONString(data);
                    RpcProtocol rpcProtocol = new RpcProtocol(json.getBytes());
                    //Netty的通道负责发送数据给服务端
                    channelFuture.channel().writeAndFlush(rpcProtocol);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) throws Throwable{
        Client client = new Client();
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPort(9090);
        clientConfig.setServerAddr("localhost");
        client.setClientConfig(clientConfig);
        RpcReference rpcReference = client.startClientApplication();
        DataService dataService = rpcReference.get(DataService.class);
        for (int i = 0; i < 100; i++) {
            String result = dataService.sendData("test");
            System.out.println(result);
        }
    }
}
