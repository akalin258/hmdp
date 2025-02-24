package com.hmdp;

import cn.hutool.core.util.StrUtil;
import com.hmdp.service.IShopService;
import com.hmdp.service.impl.ShopServiceImpl;
import com.hmdp.utils.RegexUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HmDianPingApplicationTests {

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
    public void testSaveShop(){
        shopService.saveShop2Redis(1L,10L);
    }
}
