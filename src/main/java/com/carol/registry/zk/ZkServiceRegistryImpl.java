package com.carol.registry.zk;

import com.carol.registry.zk.util.CuratorUtils;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;

class ZkServiceRegistryImpl implements ServiceRegistry{
    @Override
    public void registerService(String serviceName, InetSocketAddress inetSocketAddress) {

        //服务路径
        String servicePath = CuratorUtils.ZK_REGISTER_ROOT_PATH + "/" +serviceName;
        //获取zk客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //注册
        CuratorUtils.createPersistentNode(zkClient,servicePath);
    }
}
