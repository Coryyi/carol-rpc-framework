package com.carol.remoting.transport.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存放和提供channel channel用于客户端服务端之间通信
 */
@Slf4j
public class ChannelProvider {
    //存放channel容器 这里的channel是netty包下的channel
    private final Map<String, Channel> channelMap;

    /**
     * 初始化容器
     */
    public ChannelProvider(){
        channelMap = new ConcurrentHashMap<>();
    }

    /**
     * 通过地址端口获取对应的channel
     * @param inetSocketAddress 地址端口
     * @return channel
     */
    public Channel get(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();

        if(channelMap.containsKey(key)){
            Channel channel = channelMap.get(key);
            //channel处于active
            if(channel != null && channel.isActive()){
                return channel;
            }else {
                channelMap.remove(key);
            }
        }
        return null;
    }
    public void set(InetSocketAddress inetSocketAddress,Channel channel){
        String key = inetSocketAddress.toString();
        channelMap.put(key,channel);
        log.info("channel放入ChannelProvider中成功，当前容器中Channel数量{}",channelMap.size());
    }
    public void remove(InetSocketAddress inetSocketAddress){
        String key = inetSocketAddress.toString();
        channelMap.remove(key);
        log.info("channel移除成功，当前容器中Channel数量{}",channelMap.size());

    }
}
