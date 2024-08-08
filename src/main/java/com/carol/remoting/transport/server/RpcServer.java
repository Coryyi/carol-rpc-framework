package com.carol.remoting.transport.server;

import com.carol.factory.SingletonFactory;
import com.carol.registry.config.RpcServiceConfig;
import com.carol.registry.provider.ServiceProvider;
import com.carol.registry.zk.provider.ZkServiceProviderImpl;
import com.carol.remoting.transport.codec.ProtocolFrameDecoder;
import com.carol.remoting.transport.codec.RpcMessageEncode;
import com.carol.util.CustomShutdownHook;
import com.carol.util.ThreadPoolFactoryUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

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
    @SneakyThrows//隐式抛出异常 自动抛出异常 无需使用try-catch处理
    public void start(){
        //JVM关闭时清理注册中心上的相关服务 并清除所有线程池
        CustomShutdownHook.getCustomShutdownHook().clearAllService();
        //获取当前ip地址
        String host = InetAddress.getLocalHost().getHostAddress();
        //事件循环组
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();

        DefaultEventLoopGroup serviceHandlerGroup = new DefaultEventLoopGroup(
                //获取cpu线程数*2
                Runtime.getRuntime().availableProcessors()*2,
                ThreadPoolFactoryUtil.createThreadFactory("service-handler-group",false)
        );//创建一个时间循环组（线程池）执行事件循环任务
         //启动器

        try{
            ServerBootstrap bootstrap = new ServerBootstrap();
            ChannelFuture channelFuture = bootstrap
                    .group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    //关闭nagle
                    .childOption(ChannelOption.TCP_NODELAY,true)
                    //开启TPC底层心跳机制
                    .childOption(ChannelOption.SO_KEEPALIVE,true)
                    //表示系统用于临时存放已完成三次握手的请求的队列的最大长度,如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            ChannelPipeline pipeline = nioSocketChannel.pipeline();
                            pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
                            pipeline.addLast(new RpcMessageEncode());
                            pipeline.addLast(new ProtocolFrameDecoder());
                            //pipeline.addLast(serviceHandlerGroup, new RpcServerHandler());
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
