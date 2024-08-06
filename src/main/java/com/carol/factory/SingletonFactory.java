package com.carol.factory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {
    //容器用于保存单例对象
    private static final Map<String,Object> singletonMap = new ConcurrentHashMap<>();

    /**
     * 获取单例对象
     * @param clazz Class.class
     * @return 类的单例
     * @param <T> 类
     */
    public static <T> T getInstance(Class<T> clazz){
        if(clazz == null){
            throw new IllegalArgumentException();
        }
        String key = clazz.toString();
        if(singletonMap.containsKey(key)){
            //将Object对象转为clazz所表示的类型
            return clazz.cast(singletonMap.get(key));
        }else {
            //如果ConcurrentHashMap中不存在key就会执行后面的lambda表达式 执行表达式会创建对象并于key存入map
            return clazz.cast(singletonMap.computeIfAbsent(key,k->{
                try{
                    return clazz.getDeclaredConstructor().newInstance();
                } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                         NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }));
        }
    }
}
