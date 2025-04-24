package com.hmdp.admin.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Shop;
import com.hmdp.admin.entity.User;
import com.hmdp.admin.mapper.ShopMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hmdp.admin.service.IShopService;
import com.hmdp.admin.utils.RedisConstants;
import com.hmdp.admin.utils.RedisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.admin.utils.RedisConstants.*;
import static com.hmdp.admin.utils.SystemConstants.DEFAULT_PAGE_SIZE;
import static java.lang.Thread.sleep;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {


    @Override
    public Page<Shop> queryShopByPage(Integer current, Integer size, String name, Integer typeId) {
        Page<Shop> page = new Page<>(current, size);
        LambdaQueryWrapper<Shop> queryWrapper = new LambdaQueryWrapper<>();
        if(typeId!=null){
            queryWrapper.eq(Shop::getTypeId, typeId);
        }
        if(StrUtil.isNotBlank(name)){
            queryWrapper.like(Shop::getName,name);
        }
        queryWrapper.orderByDesc(Shop::getCreateTime);
        return page(page,queryWrapper);

    }

    @Override
    public Result deleteShop(Long id) {
        log.info("删除店铺: id={}", id);
        // 删除店铺，同时需要清除缓存
        boolean isSuccess = removeById(id);
        if (isSuccess) {
            // 删除Redis缓存
            stringRedisTemplate.delete(CACHE_SHOP_KEY+id);
            return Result.ok();
        }
        return Result.fail("删除失败，店铺不存在");
    }


    @Override
    public Result saveShop(Shop shop) {
        log.info("新增店铺: {}", shop);
        // 写入数据库
        save(shop);
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+shop.getId().toString(), JSONUtil.toJsonStr(shop));
        // 返回店铺id
        return Result.ok(shop.getId());
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @Override
    //记得打上事务
    @Transactional
    public Result updateShop(Shop shop) {
        //为了确保缓存与数据库的一致性,采用先更新数据库,再删缓存
        //0.校验一下shopId
        Long id = shop.getId();
        if(id==null){
            return Result.fail("店铺id不能为空");
        }
        //1.更新数据库
        updateById(shop);
        //2.删除缓存
        //这里从shop取id,前面得判空一下
        String key= RedisConstants.CACHE_SHOP_KEY+id;
        stringRedisTemplate.delete(key);
        return Result.ok();
    }



    //缓存预热,等会在单元测试里面直接调用
    //expireSeconds传进来一个有效期
    public void saveShop2Redis(Long id,Long expireSeconds) throws InterruptedException {
        //1.查询店铺
        Shop shop = getById(id);
        //tip:为了模拟逻辑过期之后缓存重建过程,这块睡一会
        sleep(200);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.写入redis
        stringRedisTemplate.opsForValue().set(RedisConstants.CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
    }
}
