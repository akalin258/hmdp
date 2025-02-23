package com.hmdp;

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
}
