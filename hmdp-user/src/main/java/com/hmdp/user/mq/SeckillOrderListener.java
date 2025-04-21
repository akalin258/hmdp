package com.hmdp.user.mq;

import com.hmdp.user.service.IVoucherOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SeckillOrderListener {

    @Autowired
    private IVoucherOrderService voucherOrderService;

    /*@RabbitListener(queues = "seckill.order.create.queue")
    public void handleSeckillOrder(Map<String, Object> msg, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            // 1. 解析消息
            Long userId = ((Number) msg.get("userId")).longValue();
            Long voucherId = ((Number) msg.get("voucherId")).longValue();
            Long orderId = ((Number) msg.get("orderId")).longValue();
            Long requestId = ((Number) msg.get("requestId")).longValue();

            // 2. 处理订单（调用 Service 层）
            voucherOrderService.createVoucherOrder(userId, voucherId, orderId, requestId);

            // 3. 手动确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理秒杀订单失败", e);
            try {
                // 4. 处理失败，拒绝消息（可根据异常类型决定是否重试）
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("消息NACK失败", ex);
            }
        }
    }*/
}
