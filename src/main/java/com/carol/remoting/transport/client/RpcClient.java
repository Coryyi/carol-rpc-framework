package com.carol.remoting.transport.client;

import com.carol.factory.SingletonFactory;
import com.carol.registry.zk.ServiceDiscovery;
import com.carol.registry.zk.ZkServiceDiscoveryImpl;
import com.carol.remoting.dto.RpcRequest;
import com.carol.remoting.dto.RpcResponse;
import com.carol.remoting.transport.RpcRequestTransport;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.CompleteFuture;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
public class RpcClient implements RpcRequestTransport {
    private static final String IP = "127.0.0.1";
    //服务发现
    private final ServiceDiscovery serviceDiscovery;
    //未处理的rpc响应集合
    private final UnprocessedRequests unprocessedRequests;
    //获取channel 存放Channel的ChannelProvider Channel用于在服务端和客户端之间传输数据
    private final ChannelProvider channelProvider;
    private final Bootstrap bootstrap;
    private final EventLoopGroup eventLoopGroup;


    /**
     * 初始化客户端
     */
    public RpcClient(){
        //服务发现
        this.serviceDiscovery = SingletonFactory.getInstance(ServiceDiscovery.class);
        //未处理响应集合 单例
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        //Channel容器
        this.channelProvider = SingletonFactory.getInstance(ChannelProvider.class);


        //处理器
        LoggingHandler LOGGING_HANDLER = new LoggingHandler(LogLevel.DEBUG);

        //初始化相关资源
        eventLoopGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bootstrap
                .group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)//5秒连接超时
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(LOGGING_HANDLER);
                        //设置写静默事件
                        pipeline.addLast(new IdleStateHandler(0,5,0, TimeUnit.SECONDS));

                    }
                });
    }

    public Channel doConnect(InetSocketAddress inetSocketAddress) throws ExecutionException, InterruptedException {
        //监听连接完成
        CompletableFuture<Channel> completableFuture = new CompletableFuture<>();
        //异步操作
        bootstrap.connect(inetSocketAddress).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if(future.isSuccess()){
                    //连接成功
                    log.info("客户端连接服务器成功{}",inetSocketAddress);
                    completableFuture.complete(future.channel());//通知连接完成
                }
            }
        });
        return completableFuture.get();//等待结果
    }


    /**
     * 发送消息
     * @param rpcRequest 消息体
     * @return
     */
    @Override
    public Object sendRpcRequest(RpcRequest rpcRequest) {
        //创建一个CompletableFuture等待远程返回结果
        CompletableFuture<RpcResponse<Object>> completableFuture = new CompletableFuture<>();
        //创建rpc请求

        return null;
    }
}
