package com.hmdp;

import cn.hutool.core.util.StrUtil;
import com.hmdp.utils.RegexUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class HmDianPingApplicationTests {
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
}
