package com.hmdp.user.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    @Bean
    public RedissonClient redissonClient(){
        //....
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://121.43.137.22:6379")
                .setPassword("root");
        return Redisson.create(config);
    }
}
