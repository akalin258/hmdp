package com.hmdp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Shop;
import com.hmdp.mapper.ShopMapper;
import com.hmdp.service.IShopService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryShopById(Long id) {
        //1.先去redis查
        String key=CACHE_SHOP_KEY+id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.redis里有数据,直接返回
        //2.1这时返回的是真正有意义的数据
        if(!StrUtil.isBlank(shopJson)){
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return Result.ok(shop);
        }
        //2.2,判断一下是否是空对象""
        if(shopJson.equals("")){
            return Result.fail("店铺信息不存在");
        }
        //3.redis里面没有,去数据库查
        Shop shop = getById(id);
        //4.数据库没有,返回错误
        if(shop==null){
            //采用缓存空对象来处理缓存穿透
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return Result.fail("店铺不存在");
        }
        //5.数据库有,那么写回redis,然后返回shop
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return Result.ok(shop);
    }

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
        String key=CACHE_SHOP_KEY+id;
        stringRedisTemplate.delete(key);
        return Result.ok();
    }

    //缓存预热,等会在单元测试里面直接调用
    //expireSeconds传进来一个有效期
    public void saveShop2Redis(Long id,Long expireSeconds){
        //1.查询店铺
        Shop shop = getById(id);
        //2.封装逻辑过期时间
        RedisData redisData = new RedisData();
        redisData.setData(shop);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(expireSeconds));
        //3.写入redis
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
    }
}
