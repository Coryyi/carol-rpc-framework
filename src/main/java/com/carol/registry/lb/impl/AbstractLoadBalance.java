package com.carol.registry.lb.impl;

import com.carol.registry.lb.LoadBalance;
import com.carol.remoting.dto.RpcRequest;
import com.carol.util.CollectionUtil;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance {
    @Override
    public String selectServiceAddress(List<String> serviceUrlList, RpcRequest rpcRequest) {

        if(CollectionUtil.isEmpty(serviceUrlList)){
           //为空返回null
           return null;
        }
        if (serviceUrlList.size()==1){
            return serviceUrlList.get(0);
        }
        return doSelect(serviceUrlList,rpcRequest);
    }
    protected abstract String doSelect(List<String> serviceUrlList,RpcRequest rpcRequest);
}
