package com.hmdp.admin.service.impl;

import com.hmdp.admin.dto.Result;
import com.hmdp.admin.dto.SeckillMessage;
import com.hmdp.admin.entity.VoucherOrder;
import com.hmdp.admin.mapper.VoucherOrderMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.admin.service.ISeckillVoucherService;
import com.hmdp.admin.service.IVoucherOrderService;

import com.hmdp.admin.utils.RedisIdWorker;
import com.hmdp.admin.utils.UserHolder;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    //引入mq
    @Override
    public Result seckillVoucher(Long voucherId) {
        //获取用户id
        Long userId = UserHolder.getUser().getId();
        //生成全局唯一id，用作订单号
        long orderId = redisIdWorker.nextId("order");

        //执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(),
                userId.toString(),
                String.valueOf(orderId)
        );

        int code = result.intValue();
        if (code != 0) {
            return Result.fail(code == 1 ? "库存不足" : "不能重复下单");
        }

        // 发送包含 orderId 的消息
        rabbitTemplate.convertAndSend(
                "seckill.direct.exchange",
                "seckill.order",
                new SeckillMessage(voucherId, userId, orderId)
        );

        return Result.ok("抢购请求已接收");

    }

    @RabbitListener(queues = "seckill.order.create.queue")
    public void handleSeckillMessage(SeckillMessage message,
                                     Channel channel,
                                     @Header(AmqpHeaders.DELIVERY_TAG) long tag) throws IOException {
        try {
            createVoucherOrder(
                    message.getVoucherId(),
                    message.getUserId(),
                    message.getOrderId()
            );
            channel.basicAck(tag, false);
        } catch (DuplicateKeyException e) {
            log.info("订单 {} 已存在，直接确认消息", message.getOrderId());
            channel.basicAck(tag, false);
        } catch (Exception e) {
            log.error("订单处理失败，消息重新入队", e);
            channel.basicNack(tag, false, true);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void createVoucherOrder(Long voucherId, Long userId, Long orderId) {
        // 1. 双重幂等校验
        if (getById(orderId) != null) {
            log.info("订单[{}]已存在，跳过处理", orderId);
            return;
        }

        // 2. 扣减数据库库存
        boolean success = seckillVoucherService.update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0)
                .update();

        // 4. 创建订单
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);

        if (!save(order)) {
            throw new RuntimeException("订单创建失败");
        }
    }

}
