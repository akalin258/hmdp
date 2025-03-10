package com.hmdp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class MqTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMes(){
        rabbitTemplate.convertAndSend("seckill.direct.exchange","seckill.order","test");
    }
}
