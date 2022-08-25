package org.idea.jrpc.framework.core.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.idea.jrpc.framework.core.common.RpcDecoder;
import org.idea.jrpc.framework.core.common.RpcEncoder;
import org.idea.jrpc.framework.core.common.config.ServerConfig;

import static org.idea.jrpc.framework.core.common.cache.CommonServerCache.PROVIDER_CLASS_MAP;
@Slf4j
@Data
public class Server {
    private ServerConfig serverConfig;
    private static EventLoopGroup bossGroup = null;
    private static EventLoopGroup workerGroup = null;

    public void registyService(Object serviceBean) {
        if(serviceBean.getClass().getInterfaces().length == 0) {
            throw new RuntimeException("service must had interfaces!");
        }
        Class[] classes = serviceBean.getClass().getInterfaces();   //获得对象所有的实现接口
//        System.out.println(classes[0]);
        if(classes.length > 1) {
            throw new RuntimeException("service must only had one interface!");
        }
        Class interfaceClass = classes[0];
        System.out.println(interfaceClass.getName());
        PROVIDER_CLASS_MAP.put(interfaceClass.getName(), serviceBean);
    }

    public void startApplication() throws InterruptedException {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup);
        bootstrap.channel(NioServerSocketChannel.class);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);  //启用Nagle算法
        bootstrap.option(ChannelOption.SO_BACKLOG, 1024);   //用于临时存放已完成三次握手的请求的队列的最大长度
        bootstrap.option(ChannelOption.SO_SNDBUF, 16*1024) //
                .option(ChannelOption.SO_RCVBUF, 16*1024)
                .option(ChannelOption.SO_KEEPALIVE, true);  //是否启用心跳包保活机制
        bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch) throws Exception {
                System.out.println("初始化服务器过程！");
                System.out.println("ok");
                //ch.pipeline()返回的是和这条连接相关的逻辑处理链，采用链责任链模式
                ch.pipeline().addLast(new RpcEncoder());
                ch.pipeline().addLast(new RpcDecoder());
                ch.pipeline().addLast(new ServerHandler());
            }
        });
        bootstrap.bind(serverConfig.getPort()).sync();
    }

    public static void main(String[] args) throws InterruptedException {
        Server server = new Server();
        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setPort(9090);
        server.setServerConfig(serverConfig);
        server.registyService(new DataServiceImpl());
        server.startApplication();

    }

}
