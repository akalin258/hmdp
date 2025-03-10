package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.SeckillVoucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.SeckillVoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.ISeckillVoucherService;
import com.hmdp.service.IVoucherOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisIdWorker;
import com.hmdp.utils.UserHolder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder> implements IVoucherOrderService {

    @Autowired
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private SeckillVoucherMapper seckillVoucherMapper;

    @Autowired
    private RedisIdWorker redisIdWorker;

    @Autowired
    private VoucherOrderMapper voucherOrderMapper;

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    //直接抄
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Override
    public Result seckillVoucher(Long voucherId) {
        //1.查询优惠券
        SeckillVoucher seckillVoucher = seckillVoucherService.getById(voucherId);
        //2.判断时间
        if (seckillVoucher.getBeginTime().isAfter(LocalDateTime.now())) {
            return Result.fail("秒杀未开始");
        }
        if (seckillVoucher.getEndTime().isBefore(LocalDateTime.now())) {
            return Result.fail("秒杀已结束");
        }
        //3.判断库存
        //要<=0
        if (seckillVoucher.getStock() <= 0) {
            return Result.fail("库存不足");
        }
        Long userId = UserHolder.getUser().getId();
        /*synchronized (userId.toString().intern()){
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }*/
        //就是锁的key是"lock:order:"+userId
        RLock lock = redissonClient.getLock("lock:order:"+userId);
        boolean isLock = lock.tryLock();
        if(!isLock){
            return Result.fail("不允许重复下单");
        }
        try {
            IVoucherOrderService proxy = (IVoucherOrderService) AopContext.currentProxy();
            return proxy.createVoucherOrder(voucherId);
        }finally {
            lock.unlock();
        }
    }

    @Transactional
    public Result createVoucherOrder(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        //4.判断一人一单
        int orderNum = voucherOrderMapper.queryByUserAndVoucher(userId, voucherId);
        if (orderNum > 0) {
            return Result.fail("每位用户只能下单一次");
        }
        //5.扣减库存
        //这块是不是有问题
        int row = seckillVoucherMapper.reduceStock(voucherId);
        if (row != 1) {
            return Result.fail("下单失败");
        }
        //6.创建订单
        //填充voucherId,userId,订单的id
        VoucherOrder voucherOrder = new VoucherOrder();
        voucherOrder.setVoucherId(voucherId);
        long orderId = redisIdWorker.nextId("order");
        voucherOrder.setUserId(userId);
        voucherOrder.setId(orderId);
        save(voucherOrder);
        return Result.ok(orderId);
    }
    /*@Override
    public Result seckillVoucher(Long voucherId){
        //获取用户
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");
        // 1.执行lua脚本
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );
        int r = result.intValue();
        // 2.判断结果是否为0
        if (r != 0) {
            // 2.1.不为0 ，代表没有购买资格
            return Result.fail(r == 1 ? "库存不足" : "不能重复下单");
        }

        //3.引入mq
        //走到这,发现有资格下单
        //构造消息
        long requestId = redisIdWorker.nextId("req");
        Map<String, Object> message = new HashMap<>();
        message.put("requestId", requestId);
        message.put("userId", userId);
        message.put("voucherId", voucherId);
        message.put("orderId", orderId);
        message.put("timestamp", System.currentTimeMillis());
        Map<String,Object> mes=new HashMap<>();
        rabbitTemplate.convertAndSend("seckill.direct.exchange","seckill.order",message);
        //TODO
        //返回这块可能还有问题
        return Result.ok(orderId);
    }

    @Override
    public void createVoucherOrder(Long userId, Long voucherId, Long orderId, Long requestId) {
        // 1. 幂等性校验（Redis）
        String key = "seckill:req:" + requestId;
        Boolean isNewRequest = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 24, TimeUnit.HOURS);
        if (Boolean.FALSE.equals(isNewRequest)) {
            throw new RuntimeException("重复请求");
        }

        // 2. 最终库存校验（数据库）
        SeckillVoucher voucher = seckillVoucherService.getById(voucherId);
        if (voucher.getStock() < 1) {
            throw new RuntimeException("库存不足");
        }

        // 3. 一人一单校验（数据库）
        Long count = query().eq("user_id", userId).eq("voucher_id", voucherId).count();
        if (count > 0) {
            throw new RuntimeException("用户已下单");
        }
        // 4. 扣减库存
        boolean success = update()
                .setSql("stock = stock - 1")
                .eq("voucher_id", voucherId)
                .gt("stock", 0) // 乐观锁
                .update();
        if (!success) {
            throw new RuntimeException("扣减库存失败");
        }

        // 5. 创建订单
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        save(order);
    }*/


}
