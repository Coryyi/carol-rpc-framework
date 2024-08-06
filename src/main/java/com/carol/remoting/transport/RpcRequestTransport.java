package com.carol.remoting.transport;

import com.carol.annotation.SPI;
import com.carol.remoting.dto.RpcRequest;

@SPI
public interface RpcRequestTransport {

    /**
     * 发送rpc请求到服务端 并获取返回结果
     * @param rpcRequest 消息体
     * @return 服务端消息
     */
    Object sendRpcRequest(RpcRequest rpcRequest);
}
