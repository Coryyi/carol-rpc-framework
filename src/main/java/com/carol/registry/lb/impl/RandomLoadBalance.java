package com.carol.registry.lb.impl;

import com.carol.remoting.dto.RpcRequest;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance extends AbstractLoadBalance{
    @Override
    protected String doSelect(List<String> serviceUrlList, RpcRequest rpcRequest) {
        //定义随即类
        Random random = new Random();
        return serviceUrlList.get(random.nextInt(serviceUrlList.size()));//随机返回
    }
}
