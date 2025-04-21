package com.hmdp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest
public class UVTest {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Test
    public void testHyperLogLog(){
        //用户量不足,那就手动往redis里面添加1百万数据
        //一次加入1000个,慢慢加入
        String[] values=new String[1000];
        int j=0;
        for(int i=0;i<1000000;i++){
            j=i%1000;
            values[j]="user_"+i;
            if(j==999){
                stringRedisTemplate.opsForHyperLogLog().add("test_uv",values);
            }
        }
        //统计数量
        Long testUv = stringRedisTemplate.opsForHyperLogLog().size("test_uv");
        System.out.println(testUv);
    }
}
