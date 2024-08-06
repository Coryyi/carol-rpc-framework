package com.carol.remoting.transport.client;

import com.carol.remoting.dto.RpcResponse;
import io.netty.util.concurrent.CompleteFuture;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理未处理的RPC响应
 */
public class UnprocessedRequests {
    //CompletableFuture是一个异步编程类，表示一个异步操作的最终结果，无需阻塞等待结果
    //完成一个CompletableFuture调用 该类对象的 future.complete("Result")方法即可
    //完成后获取结果调用该类对象的 future.get() 可能会阻塞

    // 创建未响应容器 key为请求ID value为CompletableFuture对象
    private static final Map<String, CompletableFuture<RpcResponse<Object>>> UNPROCESSED_RESPONSE_FUTURES = new ConcurrentHashMap<>();

    public void complete(RpcResponse<Object> rpcResponse){
        CompletableFuture<RpcResponse<Object>> future = UNPROCESSED_RESPONSE_FUTURES.remove(rpcResponse.getRequestId());
        if (future != null){
            future.complete(rpcResponse);
        }else {
            throw new IllegalStateException();
        }
    }
}
