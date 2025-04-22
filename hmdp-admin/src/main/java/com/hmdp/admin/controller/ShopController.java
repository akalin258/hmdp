package com.hmdp.admin.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;

import com.hmdp.admin.entity.User;
import com.hmdp.admin.service.IShopService;
import com.hmdp.admin.service.IShopTypeService;
import com.hmdp.admin.utils.SystemConstants;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/shop")
public class ShopController {

    @Resource
    public IShopService shopService;



    @GetMapping("/list")
    public Result getUserList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "typeId", required = false) Integer typeId
    ) {
        //分页查询，核心sql
        //select ... from tb_user limit #{current} offset #{size}
        Page<Shop> shopPage = shopService.queryShopByPage(current, size, name, typeId);
        return Result.ok(shopPage);
    }

    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        // 写入数据库
        shopService.save(shop);
        // 返回店铺id
        return Result.ok(shop.getId());
    }
}
