package com.carol.registry.zk;

import java.net.InetSocketAddress;

/**
 * 注册
 */

public interface ServiceRegistry {
    /**
     * 注册到注册中心
     * @param serviceName 服务名称
     * @param inetSocketAddress 服务ip端口
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);
}
