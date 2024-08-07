package com.carol.remoting.transport.codec;

import com.carol.compress.Compress;
import com.carol.enums.SerializationTypeEnum;
import com.carol.exception.RpcException;
import com.carol.extension.ExtensionLoader;
import com.carol.remoting.constant.RpcConstant;
import com.carol.remoting.dto.RpcRequest;
import com.carol.remoting.dto.RpcResponse;
import com.carol.remoting.message.RpcMessage;
import com.carol.remoting.transport.zip.CompressTypeEnum;
import com.carol.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtocolFrameDecoder extends LengthFieldBasedFrameDecoder {


    /**
     *
     * @param maxFrameLength 最大帧长度
     * @param lengthFieldOffset 偏移量 消息帧中长度字段的起始位置（0开始）
     * @param lengthFieldLength 长度字段的长度  长度为4字节 8*1024*1024 8M长小于4字节
     * @param lengthAdjustment 长度调整值某些协议中，长度字段并不直接指向消息内容的开始，如果长度字段后没有其他字段那么此处填0
     * @param initialBytesToStrip
     */
    public ProtocolFrameDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }

    // 魔数4B RPC请求版本1B 此前为HEAD1 长度字段4B（长度从下标5开始） 前面有9字节 将读取到的长度-9  消息类型1B 压缩算法1B 自增id4B
    public ProtocolFrameDecoder(){
        this(RpcConstant.MAX_FRAME_LENGTH,5,4,-9,0);
    }

    /**
     * 反序列化
     * @param buf 消息BytrBuf
     * @return 反序列化后的对象
     */
    private Object decodeFrame(ByteBuf buf){
        // 按顺序读取序列化的消息内容
        // 检查魔数
        checkMagicNum(buf);
        //检查版本
        checkVersion(buf);
        //获取消息体长度
        int fullLength = buf.readInt();
        //消息类型
        byte messageType = buf.readByte();
        //编码方式
        byte codecType = buf.readByte();
        //压缩算法
        byte compressType = buf.readByte();
        //id
        int id = buf.readInt();

        //解析为RpcMessage
        RpcMessage rpcMessage = new RpcMessage().builder()
                .messageType(messageType)
                .codec(codecType)
                .compressType(compressType)
                .sequenceId(id).build();
        // 心跳包
        if(messageType == RpcConstant.HEARTBEAT_REQUEST_TYPE){
            rpcMessage.setData(RpcConstant.PING);
            return rpcMessage;
        }
        if(messageType == RpcConstant.HEARTBEAT_RESPONSE_TYPE){
            rpcMessage.setData(RpcConstant.PONG);
            return rpcMessage;
        }
        //消息体中数据长度
        int bodyLength = fullLength - RpcConstant.HEAD_LENGTH;
        if (bodyLength>0){
            //消息体
            byte[] bodyBytes = new byte[bodyLength];
            buf.readBytes(bodyBytes);
            //解压
            String compressName = CompressTypeEnum.getName(compressType);
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressName);
            bodyBytes = compress.decompress(bodyBytes);
            //反序列化
            String serializerName = SerializationTypeEnum.getName(codecType);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializerName);
            if(messageType == RpcConstant.REQUEST_TYPE){
                RpcRequest rpcRequest = serializer.deserialize(bodyBytes, RpcRequest.class);
                rpcMessage.setData(rpcRequest);
            }else{
                RpcResponse rpcResponse = serializer.deserialize(bodyBytes, RpcResponse.class);
                rpcMessage.setData(rpcResponse);
            }
        }
        return rpcMessage;
    }

    private void checkVersion(ByteBuf buf) {
        byte varsion = buf.readByte();
        if(varsion != RpcConstant.VERSION){
            throw new RpcException("Rpc版本错误");
        }
    }

    private void checkMagicNum(ByteBuf buf) {
        byte[] bytes = new byte[4];
        buf.readBytes(bytes);
        if(bytes != RpcConstant.MAGIC_NUM){
            throw new RpcException("魔数格式错误");
        }
    }


}
