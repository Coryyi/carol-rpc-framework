package com.carol.remoting.transport.codec;

import com.carol.compress.Compress;
import com.carol.enums.SerializationTypeEnum;
import com.carol.extension.ExtensionLoader;
import com.carol.remoting.message.RpcMessage;
import com.carol.remoting.transport.zip.CompressTypeEnum;
import com.carol.serializer.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import java.util.concurrent.atomic.AtomicInteger;

import static com.carol.remoting.constant.RpcConstant.*;

public class RpcMessageEncode extends MessageToByteEncoder<RpcMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger();
    @Override
    protected void encode(ChannelHandlerContext ctx, RpcMessage msg, ByteBuf out) throws Exception {
        //魔数
        out.writeBytes(MAGIC_NUM);
        //rpc请求版本
        out.writeByte(VERSION);
        //长度字段 暂时留出空位
        //writerIndex()获取当前ByteBuf的写索引位置，写索引指向下一个写入的下标
        out.writerIndex(out.writerIndex() + 4);//这里是获取下标+4后将此时的下标设置给写指针
        //内部调用了this.writerIndex = index

        //消息类型
        byte messageType = msg.getMessageType();
        out.writeByte(messageType);

        //编解码方式
        out.writeByte(msg.getCodec());

        //压缩算法
        out.writeByte(CompressTypeEnum.GZIP.getCode());

        //自增id
        out.writeInt(ATOMIC_INTEGER.getAndIncrement());

        //构建消息体
        byte[] bytes = null;

        int fullLength = HEAD_LENGTH;//长度16
        //判断非心跳包
        if (messageType != HEARTBEAT_REQUEST_TYPE && messageType != HEARTBEAT_RESPONSE_TYPE){
            //序列化对象
            String serializationType = SerializationTypeEnum.getName(messageType);
            Serializer serializer = ExtensionLoader.getExtensionLoader(Serializer.class).getExtension(serializationType);
            bytes = serializer.serialize(msg.getData());
            //压缩消息
            String compressType = CompressTypeEnum.getName(msg.getCompressType());
            Compress compress = ExtensionLoader.getExtensionLoader(Compress.class).getExtension(compressType);
            bytes = compress.compress(bytes);
            fullLength += bytes.length;
        }
        if(bytes != null){
            out.writeBytes(bytes);
        }
        int writerIndex = out.writerIndex();
    }
}
