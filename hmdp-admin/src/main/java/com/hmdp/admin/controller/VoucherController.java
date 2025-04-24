package com.hmdp.admin.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.dto.VoucherDTO;
import com.hmdp.admin.entity.SeckillVoucher;
import com.hmdp.admin.entity.Shop;
import com.hmdp.admin.entity.Voucher;

import com.hmdp.admin.service.IVoucherService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/voucher")
public class VoucherController {

    @Resource
    private IVoucherService voucherService;

    /**
     * 新增普通券
     */
    @PostMapping
    public Result addVoucher(@RequestBody Voucher voucher) {
        voucherService.save(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 新增秒杀券
     */
    @PostMapping("/seckill")
    public Result addSeckillVoucher(@RequestBody Voucher voucher) {
        voucherService.addSeckillVoucher(voucher);
        return Result.ok(voucher.getId());
    }

    /**
     * 查询店铺的优惠券列表
     */
    @GetMapping("/list/{shopId}")
    public Result queryVoucherOfShop(@PathVariable("shopId") Long shopId) {
        return voucherService.queryVoucherOfShop(shopId);
    }

    /**
     * 分页查询优惠券列表
     */
    @GetMapping("/list")
    public Result getVoucherList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "type", required = false) Integer type,
            @RequestParam(value = "shopId", required = false) Long shopId  // 新增参数
    ) {
        Page<VoucherDTO> voucherPage = voucherService.queryVoucherByPage(current, size, title, type, shopId);
        return Result.ok(voucherPage);
    }

    /**
     * 获取优惠券详情
     */
    @GetMapping("/{id}")
    public Result getVoucherById(@PathVariable("id") Long id) {
        VoucherDTO voucherDTO = voucherService.getVoucherById(id);
        if (voucherDTO == null) {
            return Result.fail("优惠券不存在");
        }
        return Result.ok(voucherDTO);
    }

    /**
     * 更新优惠券
     */
    @PutMapping
    public Result updateVoucher(@RequestBody VoucherDTO voucherDTO) {
        boolean success = voucherService.updateVoucherInfo(voucherDTO);
        if (success) {
            return Result.ok();
        }
        return Result.fail("更新失败");
    }

    /**
     * 删除优惠券
     */
    @DeleteMapping("/{id}")
    public Result deleteVoucher(@PathVariable("id") Long id) {
        boolean success = voucherService.removeVoucher(id);
        if (success) {
            return Result.ok();
        }
        return Result.fail("删除失败");
    }
}
