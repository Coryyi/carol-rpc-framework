package com.carol.annotation;

import java.lang.annotation.*;

@Documented //表明这个注解被包含在JavaDoc生成的文档中
@Retention(RetentionPolicy.RUNTIME) //定义了注解保留事件，及注解生命周期 这里是保留在运行时可以被JVM或其他使用反射机制的代码读取
@Target(ElementType.TYPE)//可以用于哪些Java元素，这里是可用于类、接口（包括注解类型）和枚举
public @interface SPI {
}
