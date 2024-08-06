package com.carol.registry.lb;

import com.carol.annotation.SPI;
import com.carol.remoting.dto.RpcRequest;

import java.util.List;

@SPI
public interface LoadBalance {
    /**
     * 负载均衡
     * @param serviceUrlList 服务url列表
     * @param rpcRequest rpc请求消息
     * @return 服务url
     */
    String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest);
}
