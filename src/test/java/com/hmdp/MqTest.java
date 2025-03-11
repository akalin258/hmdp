package com.hmdp;

import com.hmdp.dto.SeckillMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@Slf4j
public class MqTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testSendMes(){
        rabbitTemplate.convertAndSend("seckill.direct.exchange","seckill.order","test");
    }

    @Test
    public void testSendMap() throws InterruptedException {
        // 准备消息
        Map<String,Object> msg = new HashMap<>();
        msg.put("name", "柳岩");
        msg.put("age", 21);
        // 发送消息
        rabbitTemplate.convertAndSend("object.queue", msg);
    }

    @Test
    public void testSendDTO(){
        SeckillMessage seckillMessage = new SeckillMessage();
        seckillMessage.setVoucherId(1L);
        seckillMessage.setUserId(1L);
        rabbitTemplate.convertAndSend("object.queue", seckillMessage);

    }
}
