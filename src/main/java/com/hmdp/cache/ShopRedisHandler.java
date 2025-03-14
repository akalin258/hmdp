package com.hmdp.cache;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.benmanes.caffeine.cache.Cache;
import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

import static com.hmdp.utils.RedisConstants.CACHE_SHOP_KEY;

@Component
//实现了InitializingBean
public class ShopRedisHandler implements InitializingBean {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private IShopService shopService;




    // 修改此行，配置 ObjectMapper
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())  // 支持 LocalDateTime
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // 可选：禁用时间戳格式

    // 其他代码不变...
    @Resource
    private Cache<String, Object> shopCache;

    // 缓存预热

    @Override
    //spring容器初始化之后执行,就相当于缓存预热
    //把数据写入caffeine和redis(感觉可以用上逻辑过期)
    public void afterPropertiesSet() throws Exception {
        // 初始化缓存
        // 1.查询商品信息
        List<Shop> shopList = shopService.list();
        // 2.放入缓存
        for (Shop shop : shopList) {
            // 2.1.item序列化为JSON
            String json = MAPPER.writeValueAsString(shop);
            // 2.2 存入caffeind
            String key = CACHE_SHOP_KEY + shop.getId();
            shopCache.put(key, shop);
            // 2.2.存入redis
            redisTemplate.opsForValue().set(key, json);
        }

//        // 3.查询商品库存信息
//        List<ItemStock> stockList = stockService.list();
//        // 4.放入缓存
//        for (ItemStock stock : stockList) {
//            // 2.1.item序列化为JSON
//            String json = MAPPER.writeValueAsString(stock);
//            // 2.2.存入redis
//            redisTemplate.opsForValue().set("item:stock:id:" + stock.getId(), json);
//        }
    }

    //
    public void saveShop(Shop shop) {
        try {
            String json = MAPPER.writeValueAsString(shop);
            String key = CACHE_SHOP_KEY  + shop.getId();
            redisTemplate.opsForValue().set(key, json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteShopById(Long id) {
        String key = CACHE_SHOP_KEY  + id;
        redisTemplate.delete(key + id);
    }
}
