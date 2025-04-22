package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;
import com.hmdp.admin.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);

    Page<Voucher> queryVoucherByPage(Integer current, Integer size, String title, Integer type);
}
