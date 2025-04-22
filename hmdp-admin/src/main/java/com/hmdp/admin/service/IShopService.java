package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.admin.entity.User;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {



    Page<Shop> queryShopByPage(Integer current, Integer size, String name, Integer typeId);
}
