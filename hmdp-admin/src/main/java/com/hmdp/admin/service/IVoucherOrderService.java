package com.hmdp.admin.service;

import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    Result seckillVoucher(Long voucherId);

    //未引入mq时的方法
    //Result createVoucherOrder(Long voucherId);



    //加一个创建订单
    // 创建订单（异步消息消费入口）
    // createVoucherOrder(Long userId, Long voucherId, Long orderId, Long requestId);
}
