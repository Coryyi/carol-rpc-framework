package com.carol.registry.zk;

import com.carol.exception.RpcException;
import com.carol.registry.lb.LoadBalance;
import com.carol.registry.lb.impl.RandomLoadBalance;
import com.carol.registry.zk.util.CuratorUtils;
import com.carol.remoting.dto.RpcRequest;
import com.carol.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;

import java.net.InetSocketAddress;
import java.util.List;

@Slf4j
public class ZkServiceDiscoveryImpl implements ServiceDiscovery{
    private final LoadBalance loadBalance;

    public ZkServiceDiscoveryImpl(){
        //暂时只使用随机的方法
        this.loadBalance = new RandomLoadBalance();
    }
    @Override
    public InetSocketAddress discoveryService(RpcRequest rpcRequest) {
        //获取服务名称
        String rpcServiceName = rpcRequest.getRpcServiceName();
        // 获取zk客户端
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        //获取服务地址列表
        List<String> serviceUrlList = CuratorUtils.getChildrenNodes(zkClient, rpcServiceName);
        if(CollectionUtil.isEmpty(serviceUrlList)){
            throw new RpcException("获取服务器列表为空,服务名·为"+rpcServiceName);
        }
        //负载均衡
        String url = loadBalance.selectServiceAddress(serviceUrlList, rpcRequest);
        log.info("负载均衡获取服务url成功{}",url);
        String[] strings = url.split(":");
        if(strings.length<2)throw new RpcException("保存的服务地址格式错误"+url);
        String host = strings[0];
        int port = Integer.parseInt(strings[1]);
        return new InetSocketAddress(host,port);
    }
}
