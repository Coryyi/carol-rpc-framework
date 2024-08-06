package com.carol.registry.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 保存配置文件中的参数
 */
@AllArgsConstructor
@Getter
public enum RpcConfigEnum {
    RPC_CONFIG_PATH("rpc.properties"),
    ZK_ADDRESS("rpc.zookeeper.address");
    private final String propertyValue;
}
