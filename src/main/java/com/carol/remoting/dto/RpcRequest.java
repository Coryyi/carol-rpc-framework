package com.carol.remoting.dto;

import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@ToString
public class RpcRequest implements Serializable {
    //jdk序列化UID
    private static final long serialVersionUID = 1905122041950251207L;
    //请求id
    private String requestId;
    //接口名称
    private String interfaceName;
    //方法名
    private String methodName;
    //参数
    private Object[] parameters;
    //参数类型
    private Class<?>[] paramTypes;
    private String version;
    private String group;
    public String getRpcServiceName() {
        return this.getInterfaceName() + this.getGroup() + this.getVersion();
    }
}
