package com.carol.remoting.transport.server.handler;

import com.carol.common.RpcResponseCodeEnum;
import com.carol.factory.SingletonFactory;
import com.carol.remoting.RpcConstants;
import com.carol.remoting.constant.RpcConstant;
import com.carol.remoting.dto.RpcRequest;
import com.carol.remoting.dto.RpcResponse;
import com.carol.remoting.message.RpcMessage;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    //单例
    private final RpcRequestHandler rpcServerHandler;

    public RpcServerHandler(){
        this.rpcServerHandler = SingletonFactory.getInstance(RpcRequestHandler.class);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try{
            //判断是否是Rpc消息
            if(msg instanceof RpcMessage){
                log.debug("RpcServerHandler收到请求消息:{}",msg);
                //Object转RpcMessage
                RpcMessage receiveMessage = (RpcMessage) msg;
                //获取消息类型
                byte messageType = receiveMessage.getMessageType();
                //创建新的RpcMessage 用于返回消息
                RpcMessage rpcMessage = new RpcMessage();
                //获取并设置序列化格式
                byte codec = receiveMessage.getCodec();
                rpcMessage.setCodec(codec);
                //压缩类型
                byte compressType = receiveMessage.getCompressType();
                rpcMessage.setCompressType(compressType);
                //如果收到的消息为星心跳包则返回PONG消息
                if(messageType == RpcConstant.HEARTBEAT_RESPONSE_TYPE){
                    rpcMessage.setMessageType(RpcConstant.HEARTBEAT_RESPONSE_TYPE);
                    rpcMessage.setData(RpcConstant.PONG);
                }else {
                    //不是心跳包则是rpc请求，从传入的RpcMessage获取rpc请求RpcRequest
                    RpcRequest rpcRequest = (RpcRequest) receiveMessage.getData();
                    //处理请求 获取请求返回的数据
                    Object result = rpcServerHandler.handler(rpcRequest);
                    //设置返回消息类型
                    rpcMessage.setData(result);
                    //判断上下文中的channel是否可用 isActive isWritable
                    if (ctx.channel().isActive()&&ctx.channel().isWritable()){
                        //创建Rpc返回对象，并将结果传入
                        RpcResponse<Object> rpcResponse = RpcResponse.success(result, rpcRequest.getRequestId());
                        rpcMessage.setData(rpcResponse);
                        log.debug("调用rpc成功，返回结果为:{}",rpcMessage);
                    }else {
                        RpcResponse<Object> rpcResponse = RpcResponse.fail(RpcResponseCodeEnum.FAIL);
                        rpcMessage.setData(rpcResponse);
                        log.debug("调用rpc失败，rpcMessage为:{}",rpcMessage);
                    }
                }
                //最后发送数据，添加监听器，ChannelFutureListener.CLOSE_ON_FAILURE 当发送失败时channel异常需要将该channel关闭
                ctx.writeAndFlush(rpcMessage).addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
            }
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                log.info("idle check happen, so close the connection");
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("server catch exception");
        cause.printStackTrace();
        ctx.close();
    }
}
