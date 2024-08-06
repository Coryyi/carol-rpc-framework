package com.carol.remoting;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 相关常量
 */
public class RpcConstants {

    public static final byte[] MAGIC_NUMBER = {(byte)'c',(byte) 'a',(byte) 'r',(byte)'o'};
    //默认字符编码
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    //协议版本
    public static final byte VERSION = 1;
    //
}
