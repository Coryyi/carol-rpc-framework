package com.carol.remoting.transport.codec;

import com.carol.remoting.constant.RpcConstant;
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

    public ProtocolFrameDecoder(){
        this(RpcConstant.MAX_FRAME_LENGTH,12,4,0,0);
    }


}
