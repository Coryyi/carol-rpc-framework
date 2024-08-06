package com.carol.registry.zk.util;

import com.carol.registry.config.RpcConfigEnum;
import com.carol.util.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * zookeeper client
 */
@Slf4j
public class CuratorUtils {
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";

    //存放服务名称以及服务url路径列表
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    //已注册列表
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "127.0.0.1:2181";

    private CuratorUtils(){

    }

    /**
     * 创建持久化节点
     * @param zkClient
     * @param path 节点路径
     */
    public static void createPersistentNode(CuratorFramework zkClient,String path){
        try{

            //路径已注册或已存在于zk中
            if(REGISTERED_PATH_SET.contains(path)||zkClient.checkExists().forPath(path) != null){
                log.info("此节点以及存在,节点为{}",path);
            }else {
                //创建节点
                /*
                create返回一个CreateBuilder实例允许配置创建节点的各种参数
                creatingParentsIfNeeded如果父节点不存在，自动创建所需的父节点
                withMode(CreateMode.PERSISTENT) withMode用于设置节点类型，CreateMode.PERSISTENT枚举值标识持久的节点，不会因为客户端会话结束而消失，需要显示删除

                 */
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path);
                log.info("创建节点成功. 节点为:[{}]", path);
            }
            //添加到已注册列表中
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取路径下的子节点
     * @param zkClient
     * @param rpcServiceName
     * @return
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient,String rpcServiceName){
        if(SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)){
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }

        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try{
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName,result);
            registerWatcher(rpcServiceName, zkClient);

        } catch (Exception e) {
            log.error("获取子节点失败，路径为...{}",servicePath);
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * 监听服务实例的上下线 维护服务名称到地址列表的映射关系
     *
     * @param rpcServiceName
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        // Curator提供的一个缓存机制，用于监听指定路径下子节点的增删改事件。组要传入zkClient（Curator客户端）servicePath（监听的路径
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        // 监听器
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            //当事件发生时，会获取servicePath路径下面的所有子节点，将子节点名称存储到map中（更新服务消息）
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        //注册监听器 将定义的监听器添加到PathChildrenCache监听器列表
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        //启动该开始监听路径下子节点的变化
        pathChildrenCache.start();
    }

    /**
     * 获取zk客户端
     * @return
     */
    public static CuratorFramework getZkClient(){
        //读取用户配置文件 更目录下面的rpc.properties
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ? properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : DEFAULT_ZOOKEEPER_ADDRESS;

        //已启动，直接返回zkClient
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){
            return zkClient;
        }
        //未启动 创建客户端
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME,MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                //zk的地址
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();

        try {
            if(!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)){
                //调用blockUntilConnected时，会阻塞当前线程直到于zk服务器建立连接，或者是超时
                throw new RuntimeException("连接zookeeper服务器超时");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return zkClient;
    }

    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress){
        // parallel并行执行forEach
        REGISTERED_PATH_SET.stream().parallel().forEach(path->{
            try {
                if(path.endsWith(inetSocketAddress.toString())){
                    zkClient.delete().forPath(path);

                }
            } catch (Exception e) {
                log.error("清除注册路径失败{}",path);
                throw new RuntimeException(e);
            }
            log.info("所有已注册服务已清除{}",REGISTERED_PATH_SET);
        });
    }
}
