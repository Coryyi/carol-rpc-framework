package com.carol.remoting.message;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcMessage {
    /**
     * rpc 消息类型
     */
    private byte messageType;
    /**
     * 序列化类型
     */
    private byte codec;
    /**
     *
     */
    private byte compressType;
    /**
     * 请求id
     */
    private int sequenceId;
    /**
     * 请求体
     */
    private Object data;
}
