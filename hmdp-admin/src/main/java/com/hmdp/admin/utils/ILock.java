package com.hmdp.admin.utils;

public interface ILock {
    //传入超时时间
    boolean tryLock(long timeoutSec);
    void unlock();
}
