package com.hmdp.admin.controller;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;

import com.hmdp.admin.entity.User;
import com.hmdp.admin.service.IShopService;
import com.hmdp.admin.service.IShopTypeService;
import com.hmdp.admin.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

import static com.hmdp.admin.utils.RedisConstants.CACHE_SHOP_KEY;

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
@Slf4j
public class ShopController {

    @Autowired
    private IShopService shopService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 分页查询店铺列表
     * @param current 当前页
     * @param size 每页数量
     * @param name 店铺名称
     * @param typeId 店铺类型ID
     * @return 店铺分页结果
     */
    @GetMapping("/list")
    public Result getUserList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "typeId", required = false) Integer typeId
    ) {
        log.info("分页查询店铺: current={}, size={}, name={}, typeId={}", current, size, name, typeId);
        Page<Shop> shopPage = shopService.queryShopByPage(current, size, name, typeId);
        return Result.ok(shopPage);
    }

    /**
     * 根据ID查询店铺详情
     * @param id 店铺ID
     * @return 店铺详情
     */
    @GetMapping("/{id}")
    public Result getShopById(@PathVariable("id") Long id) {
        log.info("查询店铺详情: id={}", id);
        Shop shop = shopService.getById(id);
        if (shop == null) {
            return Result.fail("店铺不存在");
        }
        return Result.ok(shop);
    }

    /**
     * 新增店铺
     * @param shop 店铺信息
     * @return 新增结果
     */
    @PostMapping
    public Result saveShop(@RequestBody Shop shop) {
        return shopService.saveShop(shop);
    }
    
    /**
     * 更新店铺信息
     * @param shop 店铺信息
     * @return 更新结果
     */
    @PutMapping
    public Result updateShop(@RequestBody Shop shop) {
        return shopService.updateShop(shop);
    }
    
    /**
     * 删除店铺
     * @param id 店铺ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteShop(@PathVariable("id") Long id) {
        return shopService.deleteShop(id);
    }
}
