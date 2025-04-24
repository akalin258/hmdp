package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.dto.VoucherDTO;
import com.hmdp.admin.entity.Voucher;

public interface IVoucherService extends IService<Voucher> {

    // 已有的方法
    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);

    Page<VoucherDTO> queryVoucherByPage(Integer current, Integer size, String title, Integer type, Long shopId);

    // 新增方法
    VoucherDTO getVoucherById(Long id);

    boolean updateVoucherInfo(VoucherDTO voucherDTO);

    boolean removeVoucher(Long id);
}