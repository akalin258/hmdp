package com.hmdp.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.hmdp.user.mapper")
@SpringBootApplication
@EnableRabbit
public class HmdpUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpUserApplication.class, args);
    }

}
