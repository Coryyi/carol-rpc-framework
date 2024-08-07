package com.carol.registry.config;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class RpcServiceConfig {
    /**
     * rpc 版本
     */
    private String version = "";
    /**
     * 当接口有多个发实现类时，通过group来区分
     */
    private String group = "";
    /**
     * 目标服务
     */
    private Object service;
    public String getRpcServiceName(){
        return this.getServiceName() + this.group + this.getVersion();
    }

    public String getServiceName(){
        //返回该类的规范名称 全限定名
        return this.service.getClass().getInterfaces()[0].getCanonicalName();
    }
}
