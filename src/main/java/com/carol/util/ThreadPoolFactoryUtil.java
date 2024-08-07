package com.carol.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * 线程池工具类
 */
public class ThreadPoolFactoryUtil {

    /**
     * 通过threadNamePrefix来区分不同线程池
     * key为prefix对应业务
     * value为threadPool线程池
     */
    private static final Map<String, ExecutorService> THREAD_POOLS = new ConcurrentHashMap<>();

    private ThreadPoolFactoryUtil(){}

    /*public static ExecutorService createCustomThreadPoolIfAbsent(String threadNamePrefix){

    }*/
}
