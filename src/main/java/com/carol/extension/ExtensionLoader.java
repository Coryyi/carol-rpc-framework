package com.carol.extension;

import com.carol.annotation.SPI;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简易SPI加载类
 * @param <T>
 */
@Slf4j
public final class ExtensionLoader<T> {
    //扩展服务配置文件目录 存放的文件名称为SPI接口的全限定名 文件内容是实现接口的全限定名
    private static final String SERVICE_DIRECTORY = "META_INF/extensions/";
    //缓存每个SPI接口类型对应的ExtensionLoader对象 key为SPI接口类对象
    private static final Map<Class<?>,ExtensionLoader<?>> EXTENSION_LOADERS = new ConcurrentHashMap<>();
    //缓存每个扩展类的单例实例
    private static final  Map<Class<?>,Object> EXTENSION_INSTANCES = new ConcurrentHashMap<>();

    //实例加载的扩展接口类型
    private final Class<?> type;

    //缓存每个扩展名对应的实例
    private final Map<String,Holder<Object>> cachedInstances = new ConcurrentHashMap<>();

    //缓存加载的扩展类
    private final Holder<Map<String,Class<?>>> cachedClasses = new Holder<>();

    private ExtensionLoader(Class<?> type){
        this.type = type;
    }

    /**
     * 获取SPI对应的扩展加载器
     * @param type 扩展接口类对象
     * @return 获取泛型为S类型的ExtensionLoader
     * @param <S> 泛型参数S
     */
    public static <S> ExtensionLoader<S> getExtensionLoader(Class<S> type){
        if(type == null){
            throw new IllegalArgumentException("扩展接口类型不能为空");
        }
        //必须为接口类型 这里是某个接口的扩展实现
        if (!type.isInterface()){
            throw new IllegalArgumentException("传入的类型必须是可以实现的接口类型");
        }
        if(type.getAnnotation(SPI.class) == null){
            throw new IllegalArgumentException("必须添加@SPI注解");
        }
        //从缓存中回去扩展加载器，如果没有则创建
        ExtensionLoader<S> extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        if(extensionLoader == null){
            //创建

            EXTENSION_LOADERS.putIfAbsent(type,new ExtensionLoader<S>(type));
            extensionLoader = (ExtensionLoader<S>) EXTENSION_LOADERS.get(type);
        }
        return extensionLoader;
    }

    /**
     * 更具扩展名获取对应的扩展实例
     * @param name 实现类全限定名
     * @return
     */
    public T getExtension(String name){
        //校验name不为空
        if (StringUtil.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("传入Extension名称不能为空");
        }
        //从缓存的实例中获取对应名称的实例
        Holder<Object> holder = cachedInstances.get(name);
        if(holder == null){
            //实例为空 创建实例
            cachedInstances.putIfAbsent(name,new Holder<>());
            holder = cachedInstances.get(name);
        }
        Object instance = holder.get();//获取实例
        if(instance == null){
            //实例为空，创建实例 这里需要加锁，不能重复创建实例
            synchronized (holder){
                //再次检验是否为空 只能创建一次并且只能由一个线程来进行创建
                instance = holder.get();
                if(instance == null){
                    instance = createExtension(name);
                    holder.set(instance);
                }
            }
        }
        //到这一步instance已被赋值
        return (T)instance;
    }

    /**
     * 通过实现类全限定名创建Extension
     * @param name
     * @return
     */
    private T createExtension(String name){
        Class<?> clazz = getExtensionClasses().get(name);
        if(clazz == null){
            throw new RuntimeException("没有该名称对应的扩展类:"+name);
        }
        T instance = (T) EXTENSION_INSTANCES.get(clazz);
        if(instance == null){
            try{
                EXTENSION_INSTANCES.putIfAbsent(clazz,clazz.newInstance());
                instance = (T) EXTENSION_INSTANCES.get(clazz);
            } catch (InstantiationException | IllegalAccessException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    private Map<String,Class<?>> getExtensionClasses(){
        //所有扩展类 类型
        Map<String, Class<?>> classes = cachedClasses.get();
        //双检
        if(classes == null){
            synchronized (cachedClasses){
                classes = cachedClasses.get();
                if(classes == null){
                    classes = new HashMap<>();
                    loadDirectory(classes);
                    cachedClasses.set(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses){
        //                                                       获取全限定名
        String fileName = ExtensionLoader.SERVICE_DIRECTORY + type.getName();//构建文件路径
        try{
            Enumeration<URL> urls;//urls枚举对象
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();//获取一个classLoader
            urls = classLoader.getResources(fileName);
            if(urls != null){
                while (urls.hasMoreElements()){
                    URL url = urls.nextElement();
                    loadResource(extensionClasses,classLoader,url);
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL url) {
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = bufferedReader.readLine()) != null){
                //获取注解下标
                final int ci = line.indexOf("#");
                //截取下标
                if(ci>=0){
                    line = line.substring(0,ci);
                }
                line.trim();
                if(line.length()>0){
                    //找出等于符号
                    int ei = line.indexOf("=");
                    String name = line.substring(0,ei).trim();
                    String clazzName = line.substring(ei+1).trim();
                    if(name.length()>0 && clazzName.length()>0){
                        Class<?> clazz = classLoader.loadClass(clazzName);
                        extensionClasses.put(name,clazz);
                    }
                }
            }
        } catch (IOException e) {
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
