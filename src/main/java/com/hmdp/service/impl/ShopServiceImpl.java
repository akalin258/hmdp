package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
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
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private ExecutorService CACHE_REBUILD_EXECUTOR= Executors.newFixedThreadPool(10);
    @Override
    public Result queryShopById(Long id) {
        //用逻辑过期解决缓存击穿
        Shop shop = queryWithLogicalExpire(id);
        return Result.ok(shop);
    }
    //缓存空对象,解决缓存穿透
    public Shop handleShopCachePenetration(Long id){
        //1.先去redis查
        String key=CACHE_SHOP_KEY+id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.redis里有数据,直接返回
        //2.1这时返回的是真正有意义的数据
        if(!StrUtil.isBlank(shopJson)){
            Shop shop = JSONUtil.toBean(shopJson, Shop.class);
            return shop;
        }
        //2.2,判断一下是否是空对象""
        if(shopJson.equals("")){
            return null;
        }
        //3.redis里面没有,去数据库查
        Shop shop = getById(id);
        //4.数据库没有,返回错误
        if(shop==null){
            //采用缓存空对象来处理缓存穿透
            stringRedisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL,TimeUnit.MINUTES);
            return null;
        }
        //5.数据库有,那么写回redis,然后返回shop
        stringRedisTemplate.opsForValue().set(key,JSONUtil.toJsonStr(shop),CACHE_SHOP_TTL, TimeUnit.MINUTES);
        return shop;
    }
    //redis里的数据"永不过期",
    public Shop queryWithLogicalExpire(Long id){
        //1.先去redis查
        String key = CACHE_SHOP_KEY + id;
        String shopJson = stringRedisTemplate.opsForValue().get(key);
        //2.如果redis里没有值,
        if(StrUtil.isBlank(shopJson)){
            return null;
        }
        //3.获取redis数据,取出真正的过期时间
        RedisData redisData = JSONUtil.toBean(shopJson, RedisData.class);
        //Shop shop = redisData.getData();
        //这么写不行,报错,左边是shop,右边是Object,只能强制转换一下
        //左边更抽象,范围更大
        //例如,List list=new ArrayList<>()
        //Shop shop = (Shop)redisData.getData();序列化,反序列化时类型会出问题
        Shop shop = JSONUtil.toBean((JSONObject) redisData.getData(), Shop.class);
        LocalDateTime expireTime = redisData.getExpireTime();
        //4.判断是否过期
        if(expireTime.isAfter(LocalDateTime.now())){
            //4.1未过期
            return shop;
        }
        //4.2过期了
        //5.重建缓存
        String lockKey=LOCK_SHOP_KEY + id;
        boolean hasLock = tryLock(lockKey);
        //判断是否获取锁成功
        if(hasLock){
            //获取成功,准备新开一个线程去做缓存重建
            //这里采用线程池,因此上面还有加一个变量
            CACHE_REBUILD_EXECUTOR.submit(()->{
                try {
                    this.saveShop2Redis(id,20L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }finally {
                    unlock(lockKey);
                }
            });
        }
        //返回过期的商铺信息
        return shop;

    }

    //尝试获取互斥锁
    private boolean tryLock(String key){
        Boolean hasLock = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", LOCK_SHOP_TTL, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(hasLock);
    }

    //释放锁
    private void unlock(String key){
        stringRedisTemplate.delete(key);
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
        stringRedisTemplate.opsForValue().set(CACHE_SHOP_KEY+id,JSONUtil.toJsonStr(redisData));
    }
}
