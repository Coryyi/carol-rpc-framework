package com.carol.util;

import com.carol.registry.zk.util.CuratorUtils;
import com.carol.remoting.transport.server.RpcServer;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

@Slf4j
public class CustomShutdownHook {
    private static final CustomShutdownHook customShutdownHook = new CustomShutdownHook();

    private CustomShutdownHook(){
    }

    public static CustomShutdownHook getCustomShutdownHook(){
        return customShutdownHook;
    }

    public void clearAllService(){
        //定义一个关闭时的钩子函数
        Runtime.getRuntime().addShutdownHook(new Thread(()->{
            try {
                InetSocketAddress inetSocketAddress = new InetSocketAddress(InetAddress.getLocalHost().getHostAddress(), RpcServer.SERVER_PORT);
                CuratorUtils.clearRegistry(CuratorUtils.getZkClient(), inetSocketAddress);
            } catch (UnknownHostException ignored) {
            }
            //ThreadPoolFactoryUtil.shutDownAllThreadPool();
        }));
    }


}
