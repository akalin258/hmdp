package com.hmdp.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;
import com.hmdp.admin.entity.Voucher;
import com.hmdp.admin.mapper.VoucherMapper;
import com.hmdp.admin.entity.SeckillVoucher;

import com.hmdp.admin.service.ISeckillVoucherService;
import com.hmdp.admin.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.admin.utils.RedisConstants.SECKILL_STOCK_KEY;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class VoucherServiceImpl extends ServiceImpl<VoucherMapper, Voucher> implements IVoucherService {

    @Resource
    private ISeckillVoucherService seckillVoucherService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryVoucherOfShop(Long shopId) {
        // 查询优惠券信息
        List<Voucher> vouchers = getBaseMapper().queryVoucherOfShop(shopId);
        // 返回结果
        return Result.ok(vouchers);
    }

    @Override
    @Transactional
    public void addSeckillVoucher(Voucher voucher) {
        // 保存优惠券
        save(voucher);
        // 保存秒杀信息
        SeckillVoucher seckillVoucher = new SeckillVoucher();
        seckillVoucher.setVoucherId(voucher.getId());
        seckillVoucher.setStock(voucher.getStock());
        seckillVoucher.setBeginTime(voucher.getBeginTime());
        seckillVoucher.setEndTime(voucher.getEndTime());
        seckillVoucherService.save(seckillVoucher);

        //后续下单优化,把一些判断写入到redis了,所以要把一些信息存到redis里
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY+voucher.getId(),voucher.getStock().toString());

    }

    @Override
    public Page<Voucher> queryVoucherByPage(Integer current, Integer size, String title, Integer type) {
        Page<Voucher> page = new Page<>(current, size);
        LambdaQueryWrapper<Voucher> queryWrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(title)){
            queryWrapper.like(Voucher::getTitle,title);
        }
        if(type!=null){
            queryWrapper.eq(Voucher::getType,type);
        }
        return page(page,queryWrapper);

    }
}
