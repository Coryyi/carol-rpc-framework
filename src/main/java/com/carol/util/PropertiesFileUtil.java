package com.carol.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

@Slf4j
public class PropertiesFileUtil {
    private PropertiesFileUtil(){
        //防止外部创建实例
    }

    public static Properties readPropertiesFile(String fileName){
        //获取当前线程的上下文类加载器，使用它来获取一个资源路径 "" 标识获取当前类路径的根目录
        URL url = Thread.currentThread().getContextClassLoader().getResource("");
        //储存资源路径
        String rpcConfigPath = "";
        if (url != null){
            rpcConfigPath = url.getPath() + fileName;
        }
        Properties properties = null;
        try (InputStreamReader inputStreamReader = new InputStreamReader(
                Files.newInputStream(Paths.get(rpcConfigPath)), StandardCharsets.UTF_8
        )) {
            properties = new Properties();
            //将流中的数据加载到对象中
            properties.load(inputStreamReader);

        } catch (IOException e) {
            log.error("读取配置文件失败{}",fileName);
        }

        return properties;
    }
}
