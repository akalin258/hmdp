package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.admin.entity.User;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {

    /**
     * 更新店铺信息，同时更新缓存
     * @param shop 店铺信息
     * @return 更新结果
     */
    Result updateShop(Shop shop);

    /**
     * 分页查询店铺
     * @param current 当前页
     * @param size 每页数量
     * @param name 店铺名称
     * @param typeId 店铺类型ID
     * @return 店铺分页结果
     */
    Page<Shop> queryShopByPage(Integer current, Integer size, String name, Integer typeId);
    
    /**
     * 删除店铺缓存
     * @param id 店铺ID
     */
    Result deleteShop(Long id);

    Result saveShop(Shop shop);
}
