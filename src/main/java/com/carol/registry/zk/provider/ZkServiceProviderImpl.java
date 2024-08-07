package com.carol.registry.zk.provider;

import com.carol.exception.RpcErrorMessageEnum;
import com.carol.exception.RpcException;
import com.carol.extension.ExtensionLoader;
import com.carol.registry.config.RpcServiceConfig;
import com.carol.registry.config.ServiceRegistryEnum;
import com.carol.registry.provider.ServiceProvider;
import com.carol.registry.zk.ServiceRegistry;
import com.carol.remoting.transport.server.RpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
@Slf4j
public class ZkServiceProviderImpl implements ServiceProvider {
    /*
    这里的服务是指提供Rpc远程调用的服务！！！
    可以通过rpc框架来远程调用这些服务，和调用本地方法一样
     */
    /**
     * 服务容器
     */
    private final Map<String,Object> serviceMap;

    /**
     * 已注册的服务
     */
    private final Set<String> registeredService;
    /**
     * 注册中心
     */
    private final ServiceRegistry serviceRegistry;

    public ZkServiceProviderImpl(){
        this.serviceMap = new ConcurrentHashMap<>();
        this.registeredService = ConcurrentHashMap.newKeySet();
        this.serviceRegistry = ExtensionLoader.getExtensionLoader(ServiceRegistry.class).getExtension(ServiceRegistryEnum.ZK.getName());
    }
    @Override
    public void addService(RpcServiceConfig rpcServiceConfig) {
        String rpcServiceName = rpcServiceConfig.getRpcServiceName();
        if(registeredService.contains(rpcServiceName)){
            return;
        }
        registeredService.add(rpcServiceName);
        serviceMap.put(rpcServiceName,rpcServiceConfig.getService());
        log.debug("添加服务：{} 服务接口为：{}",rpcServiceName,rpcServiceConfig.getService().getClass().getInterfaces());
    }

    /**
     * 获取rpc服务
     * @param rpcServiceName
     * @return
     */
    @Override
    public Object getService(String rpcServiceName) {
        Object service = serviceMap.get(rpcServiceName);
        if(service == null){
            throw new RpcException(RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND.getMessage());
        }
        return service;
    }

    /**
     * 发布rpc服务
     * @param rpcServiceConfig
     */
    @Override
    public void publishService(RpcServiceConfig rpcServiceConfig) {
        try {
            String host = InetAddress.getLocalHost().getHostAddress();
            this.addService(rpcServiceConfig);//天界服务到注册列表
            serviceRegistry.registerService(rpcServiceConfig.getServiceName(), new InetSocketAddress(host, RpcServer.SERVER_PORT));

        } catch (UnknownHostException e) {
            log.error("发布服务异常",e);
            throw new RuntimeException(e);
        }
    }
}
