package com.hmdp;

import cn.hutool.core.util.StrUtil;
import com.hmdp.user.service.impl.ShopServiceImpl;
import com.hmdp.user.utils.RegexUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
class HmdpUserApplicationTests {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //注意,这里注入的是实现类
    @Autowired
    private ShopServiceImpl shopService;
    @Test
    public void testPhone(){
        System.out.println(RegexUtils.isPhoneInvalid("111"));
        System.out.println(RegexUtils.isPhoneInvalid("13841121283"));
        System.out.println(RegexUtils.isPhoneInvalid("12345678910"));
    }
    @Test
    public void testIsBlank(){
        System.out.println(StrUtil.isBlank(""));
        System.out.println(StrUtil.isBlank(null));
        System.out.println(StrUtil.isBlank("111"));
    }
    @Test
    public void testIsNULL(){
        String shopJson="";
        System.out.println(shopJson=="");

    }
    @Test
    //店铺缓存预热
    public void testSaveShop() throws InterruptedException {
        shopService.saveShop2Redis(1L,10L);
    }
    @Test
    public void testRedisApi(){
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent("test setnx", "111");
        System.out.println(flag);
        //setIfAbsent,如果key不存在,添加k-v,返回true
        //第一次试一个不存在的返回true
        //再运行这个函数,返回false
        /*String s = new String();
        s.length()*/
    }


}
