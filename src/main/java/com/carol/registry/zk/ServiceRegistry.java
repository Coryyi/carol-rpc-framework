package com.carol.registry.zk;

import com.carol.annotation.SPI;

import java.net.InetSocketAddress;

/**
 * 注册
 */
@SPI
public interface ServiceRegistry {
    /**
     * 注册到注册中心
     * @param serviceName 服务名称
     * @param inetSocketAddress 服务ip端口
     */
    void registerService(String serviceName, InetSocketAddress inetSocketAddress);
}
