package com.hmdp.user.service;

import com.hmdp.user.dto.Result;
import com.hmdp.user.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    Result queryShopById(Long id);

    Result updateShop(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
