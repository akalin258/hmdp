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
}
