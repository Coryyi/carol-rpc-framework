package com.carol.registry.provider;

import com.carol.registry.config.RpcServiceConfig;

public interface ServiceProvider {
    /**
     * 添加服务
     */
    void addService(RpcServiceConfig rpcServiceConfig);

    Object getService(String rpcServiceName);

    void publishService(RpcServiceConfig rpcServiceConfig);

}
