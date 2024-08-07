package com.carol.remoting.transport.server;

import com.carol.factory.SingletonFactory;
import com.carol.registry.config.RpcServiceConfig;
import com.carol.registry.provider.ServiceProvider;
import com.carol.registry.zk.provider.ZkServiceProviderImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc服务器，接收客户端发送的rpc请求，处理后返回处理结果
 */
@Slf4j
public class RpcServer {
    //端口
    public static final int SERVER_PORT = 9999;
    //rpc服务容器 服务provider
    private final ServiceProvider serviceProvider = SingletonFactory.getInstance(ZkServiceProviderImpl.class);    //

    /**
     * 发布服务
     * @param rpcServiceConfig 远程服务配置
     */
    public void registerService(RpcServiceConfig rpcServiceConfig){
        serviceProvider.publishService(rpcServiceConfig);
    }


    //处理器
    LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);

    public void start(){
        //JVM关闭时清理注册中心上的相关服务

        //事件循环组
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

         //启动器
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            ChannelFuture channelFuture = bootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            pipeline.addLast(LOGGING_HANDLER);
                        }
                    }).bind(SERVER_PORT).sync();
            channelFuture.channel().closeFuture().sync();

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            log.debug("server closed");
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }

    }

}
