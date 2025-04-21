package com.hmdp.admin.mapper;

import com.hmdp.admin.entity.SeckillVoucher;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 秒杀优惠券表，与优惠券是一对一关系 Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2022-01-04
 */
public interface SeckillVoucherMapper extends BaseMapper<SeckillVoucher> {
    @Update("update tb_seckill_voucher set stock=stock-1 where voucher_id=#{x} and stock>0")
    int reduceStock(@Param("x") Long voucherId);
}
