package com.carol.registry.zk;

import com.carol.remoting.dto.RpcRequest;

import java.net.InetSocketAddress;

/**
 * 发现
 */
public interface ServiceDiscovery {
    /**
     * 服务发现
     * @param rpcRequest 服务名
     * @return 服务ip端口
     */
     InetSocketAddress discoveryService(RpcRequest rpcRequest);
}
