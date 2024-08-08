package com.carol.remoting.transport.server.handler;

import com.carol.exception.RpcException;
import com.carol.factory.SingletonFactory;
import com.carol.registry.provider.ServiceProvider;
import com.carol.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class RpcRequestHandler {
    private final ServiceProvider serviceProvider;
    public RpcRequestHandler(){
        this.serviceProvider = SingletonFactory.getInstance(ServiceProvider.class);

    }


    public Object handler(RpcRequest rpcRequest){
        String rpcServiceName = rpcRequest.getRpcServiceName();
        Object service = serviceProvider.getService(rpcServiceName);
        return invokeTargetMethod(rpcRequest,service);
    }

    private Object invokeTargetMethod(RpcRequest rpcRequest, Object service) {
        Object result;

        try {
            Method method = service.getClass().getMethod(rpcRequest.getMethodName());
            result = method.invoke(service,rpcRequest.getParameters());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RpcException(e.getMessage(),e);
        }
        return result;


    }
}
