package com.hmdp.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.dto.VoucherDTO;
import com.hmdp.admin.entity.Shop;
import com.hmdp.admin.entity.Voucher;
import com.hmdp.admin.mapper.VoucherMapper;
import com.hmdp.admin.entity.SeckillVoucher;

import com.hmdp.admin.service.ISeckillVoucherService;
import com.hmdp.admin.service.IShopService;
import com.hmdp.admin.service.IVoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static com.hmdp.admin.utils.RedisConstants.SECKILL_ORDER_KEY;
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

    @Resource
    private IShopService shopService;
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

        // 将库存信息存入Redis
        stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucher.getStock().toString());

    }

    @Override
    public Page<VoucherDTO> queryVoucherByPage(Integer current, Integer size, String title, Integer type,Long shopId) {
        // 1. 查询优惠券基本信息
        Page<Voucher> page = new Page<>(current, size);
        LambdaQueryWrapper<Voucher> queryWrapper = new LambdaQueryWrapper<>();
        if(StrUtil.isNotBlank(title)){
            queryWrapper.like(Voucher::getTitle, title);
        }
        if(type != null){
            queryWrapper.eq(Voucher::getType, type);
        }
        if(shopId != null){
            queryWrapper.eq(Voucher::getShopId, shopId);
        }
        page(page, queryWrapper);

        // 2. 创建结果Page对象
        Page<VoucherDTO> dtoPage = new Page<>(current, size, page.getTotal());

        // 3. 如果无数据，直接返回
        if(page.getRecords().isEmpty()){
            return dtoPage;
        }

        // 4. 收集所有优惠券ID和店铺ID
        List<Long> voucherIds = new ArrayList<>();
        List<Long> shopIds = new ArrayList<>();
        for(Voucher voucher : page.getRecords()){
            voucherIds.add(voucher.getId());
            if(voucher.getShopId() != null){
                shopIds.add(voucher.getShopId());
            }
        }

        // 5. 批量查询店铺信息
        Map<Long, String> shopNameMap = new HashMap<>();
        if(!shopIds.isEmpty()){
            List<Shop> shops = shopService.listByIds(shopIds);
            for(Shop shop : shops){
                shopNameMap.put(shop.getId(), shop.getName());
            }
        }

        // 6. 批量查询秒杀券信息
        Map<Long, SeckillVoucher> seckillMap = new HashMap<>();
        if(!voucherIds.isEmpty()){
            LambdaQueryWrapper<SeckillVoucher> seckillQueryWrapper = new LambdaQueryWrapper<>();
            seckillQueryWrapper.in(SeckillVoucher::getVoucherId, voucherIds);
            List<SeckillVoucher> seckillVouchers = seckillVoucherService.list(seckillQueryWrapper);
            for(SeckillVoucher seckill : seckillVouchers){
                seckillMap.put(seckill.getVoucherId(), seckill);
            }
        }

        // 7. 组装DTO数据
        List<VoucherDTO> dtoList = new ArrayList<>(page.getRecords().size());
        for(Voucher voucher : page.getRecords()){
            VoucherDTO dto = new VoucherDTO();
            BeanUtil.copyProperties(voucher, dto);

            // 设置店铺名称
            if(voucher.getShopId() != null){
                dto.setShopName(shopNameMap.get(voucher.getShopId()));
            }

            // 设置秒杀信息
            if(voucher.getType() == 1){
                SeckillVoucher seckillVoucher = seckillMap.get(voucher.getId());
                if(seckillVoucher != null){
                    dto.setStock(seckillVoucher.getStock());
                    dto.setBeginTime(seckillVoucher.getBeginTime());
                    dto.setEndTime(seckillVoucher.getEndTime());
                }
            }

            dtoList.add(dto);
        }

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    @Override
    public VoucherDTO getVoucherById(Long id) {
        // 1. 查询优惠券基本信息
        Voucher voucher = getById(id);
        if (voucher == null) {
            return null;
        }

        // 2. 创建并填充DTO
        VoucherDTO voucherDTO = new VoucherDTO();
        BeanUtil.copyProperties(voucher, voucherDTO);

        // 3. 获取店铺名称
        if (voucher.getShopId() != null) {
            Shop shop = shopService.getById(voucher.getShopId());
            if (shop != null) {
                voucherDTO.setShopName(shop.getName());
            }
        }

        // 4. 如果是秒杀券，获取秒杀信息
        if (voucher.getType() == 1) {
            SeckillVoucher seckillVoucher = seckillVoucherService.getById(id);
            if (seckillVoucher != null) {
                voucherDTO.setStock(seckillVoucher.getStock());
                voucherDTO.setBeginTime(seckillVoucher.getBeginTime());
                voucherDTO.setEndTime(seckillVoucher.getEndTime());
            }
        }

        return voucherDTO;
    }

    @Override
    @Transactional
    public boolean updateVoucherInfo(VoucherDTO voucherDTO) {
        // 1. 更新优惠券基本信息
        Voucher voucher = new Voucher();
        BeanUtil.copyProperties(voucherDTO, voucher);
        boolean success = updateById(voucher);

        // 2. 如果是秒杀券，更新秒杀信息
        if (success && voucher.getType() == 1) {
            SeckillVoucher seckillVoucher = new SeckillVoucher();
            seckillVoucher.setVoucherId(voucher.getId());
            seckillVoucher.setStock(voucherDTO.getStock());
            seckillVoucher.setBeginTime(voucherDTO.getBeginTime());
            seckillVoucher.setEndTime(voucherDTO.getEndTime());
            seckillVoucherService.updateById(seckillVoucher);

            // 3. 更新Redis中的库存信息
            stringRedisTemplate.opsForValue().set(SECKILL_STOCK_KEY + voucher.getId(), voucherDTO.getStock().toString());
        }

        return success;
    }

    @Override
    @Transactional
    public boolean removeVoucher(Long id) {
        // 1. 先查询优惠券
        Voucher voucher = getById(id);
        if (voucher == null) {
            return false;
        }

        // 2. 删除优惠券
        boolean success = removeById(id);

        // 3. 如果是秒杀券，还需要删除秒杀券信息
        if (success && voucher.getType() == 1) {
            seckillVoucherService.removeById(id);

            // 4. 如果Redis中存在该优惠券库存信息，一并删除
            stringRedisTemplate.delete(SECKILL_STOCK_KEY + id);
            stringRedisTemplate.delete(SECKILL_ORDER_KEY + id);
        }

        return success;
    }
}
