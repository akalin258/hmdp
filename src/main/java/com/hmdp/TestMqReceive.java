package com.hmdp;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class TestMqReceive {
    /*@RabbitListener(queues = "seckill.order.create.queue")
    public void receiveMessage(String message) {
        log.info("收到秒杀订单消息: {}", message);
    }*/

    @RabbitListener(queues = "seckill.order.create.queue")
    public void receiveMessage(String message, Channel channel,@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) throws IOException {
        log.info("收到秒杀订单消息: {}", message);
        // 业务处理逻辑...
        //确认
        channel.basicAck(deliveryTag,false);
    }
}
