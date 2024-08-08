package com.carol.util;


import com.carol.config.CustomThreadPoolConfig;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.*;

/**
 * 线程池工具类
 */
@Slf4j
public class ThreadPoolFactoryUtil {

    /**
     * 通过threadNamePrefix来区分不同线程池
     * key为prefix对应业务
     * value为threadPool线程池
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil(){}

    public static void shutDownAllThreadPool() {
        log.info("call shutDownAllThreadPool method");
        // entrySet 是包含Map.Entry对象的set Map.entry 是一个键值对 表示的那个条目 Set<Map.Entry<K, V>> entrySet();
        THREAD_POOLS.entrySet().parallelStream().forEach(entry -> {
            ExecutorService executorService = entry.getValue();
            executorService.shutdown();
            log.info("shut down thread pool [{}] [{}]", entry.getKey(), executorService.isTerminated());
            try {
                executorService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                log.error("Thread pool never terminated");
                executorService.shutdownNow();
            }
        });
    }

    public static ExecutorService CustomThreadPoolConfig(String threadNamePrefix){
        CustomThreadPoolConfig customerThreadPoolConfig = new CustomThreadPoolConfig();
        return createCustomThreadPoolIfAbsent(threadNamePrefix,customerThreadPoolConfig);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix, CustomThreadPoolConfig customThreadPoolConfig) {
        return createCustomThreadPoolIfAbsent(customThreadPoolConfig, threadNamePrefix, false);
    }

    public static ExecutorService createCustomThreadPoolIfAbsent(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, boolean daemon) {
        ExecutorService threadPool = THREAD_POOLS.computeIfAbsent(threadNamePrefix, k -> createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon));//daemon后台线程
        // 如果 threadPool 被 shutdown 的话就重新创建一个
        if (threadPool.isShutdown() || threadPool.isTerminated()) {
            THREAD_POOLS.remove(threadNamePrefix);
            threadPool = createThreadPool(customThreadPoolConfig, threadNamePrefix, daemon);
            THREAD_POOLS.put(threadNamePrefix, threadPool);
        }
        return threadPool;
    }



    public static ExecutorService createThreadPool(CustomThreadPoolConfig customThreadPoolConfig, String threadNamePrefix, Boolean daemon) {
        ThreadFactory threadFactory = createThreadFactory(threadNamePrefix, daemon);
        return new ThreadPoolExecutor(customThreadPoolConfig.getCorePoolSize(), customThreadPoolConfig.getMaximumPoolSize(),
                customThreadPoolConfig.getKeepAliveTime(), customThreadPoolConfig.getUnit(), customThreadPoolConfig.getWorkQueue(),
                threadFactory);
    }

    public static ThreadFactory createThreadFactory(String threadNamePrefix, Boolean daemon) {
        //ThreadFactory 创建线程的工厂类，定义了线程的创建行为 例如线程名称、优先级、守护线程属性等
        //
        if (threadNamePrefix != null){
            if(daemon != null){
                //                                设置线程名称的格式
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d")//%d会替换为线程id
                        .setDaemon(daemon).build();//是否为守护线程
            }else {
                return new ThreadFactoryBuilder().setNameFormat(threadNamePrefix + "-%d").build();
            }
        }
        return Executors.defaultThreadFactory();
    }
}
