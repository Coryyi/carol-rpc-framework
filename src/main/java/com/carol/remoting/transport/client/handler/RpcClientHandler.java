package com.carol.remoting.transport.client.handler;

import com.carol.enums.SerializationTypeEnum;
import com.carol.factory.SingletonFactory;
import com.carol.remoting.constant.RpcConstant;
import com.carol.remoting.dto.RpcResponse;
import com.carol.remoting.message.RpcMessage;
import com.carol.remoting.transport.client.RpcClient;
import com.carol.remoting.transport.client.UnprocessedRequests;
import com.carol.remoting.transport.zip.CompressTypeEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * channel客户端处理器，继承于入站处理器
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcMessage> {
    private final UnprocessedRequests unprocessedRequests;
    private final RpcClient nettyRpcClient;

    public RpcClientHandler() {
        this.unprocessedRequests = SingletonFactory.getInstance(UnprocessedRequests.class);
        this.nettyRpcClient = SingletonFactory.getInstance(RpcClient.class);
    }

    /**
     * Read the message transmitted by the server
     */
    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcMessage msg) {
        try {
            log.info("client receive msg: [{}]", msg);
            byte messageType = msg.getMessageType();
            if (messageType == RpcConstant.HEARTBEAT_RESPONSE_TYPE) {
                log.info("heart [{}]", msg.getData());
            } else if (messageType == RpcConstant.RESPONSE_TYPE) {
                RpcResponse<Object> rpcResponse = (RpcResponse<Object>) msg.getData();
                unprocessedRequests.complete(rpcResponse);
            }

        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                log.info("write idle happen [{}]", ctx.channel().remoteAddress());
                Channel channel = nettyRpcClient.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RpcMessage rpcMessage = new RpcMessage();
                rpcMessage.setCodec(SerializationTypeEnum.PROTOSTUFF.getCode());
                rpcMessage.setCompressType(CompressTypeEnum.GZIP.getCode());
                rpcMessage.setMessageType(RpcConstant.HEARTBEAT_REQUEST_TYPE);
                rpcMessage.setData(RpcConstant.PING);
                channel.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * Called when an exception occurs in processing a client message
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("client catch exception：", cause);
        cause.printStackTrace();
        ctx.close();
    }
}
