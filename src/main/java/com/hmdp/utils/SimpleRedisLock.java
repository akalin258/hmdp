package com.hmdp.utils;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

public class SimpleRedisLock implements ILock{
    //下面的属性就用构造方法注入,毕竟这个类没交给spring管理,用不了某些注解
    private String name;//业务名,区分一下key,毕竟那么多业务不会只用一把锁
    private StringRedisTemplate stringRedisTemplate;//这肯定是必备的

    private static final String KEY_PREFIX="lock:";//用来拼业务前缀
    //下面这个用的是huTool的uuid
    private static final String ID_PREFIX= UUID.randomUUID().toString(true)+"-";

    public SimpleRedisLock(String name,StringRedisTemplate stringRedisTemplate){
        this.name=name;
        this.stringRedisTemplate=stringRedisTemplate;
    }
    @Override
    public boolean tryLock(long timeoutSec) {
        String threadId=ID_PREFIX+Thread.currentThread().getId();
        Boolean hasLock = stringRedisTemplate.opsForValue()
                .setIfAbsent(KEY_PREFIX + name, threadId, timeoutSec, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(hasLock);
    }

    @Override
    public void unlock() {
        String threadId=ID_PREFIX+Thread.currentThread().getId();
        String val=stringRedisTemplate.opsForValue().get(KEY_PREFIX+name);
        if(threadId.equals(val)){
            stringRedisTemplate.delete(KEY_PREFIX + name);
        }
    }
}
