package com.carol.extension;

public class Holder<T> {
    /**
     * 保证可见性 不会出现脏读
     * 禁止指令重排
     */
    private volatile T value;

    public T get(){
        return value;
    }
    public void set(T value){
        this.value = value;
    }
}
