package com.hmdp.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.hmdp.admin.mapper")
@SpringBootApplication
@EnableRabbit
public class HmdpAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(HmdpAdminApplication.class, args);
    }

}
