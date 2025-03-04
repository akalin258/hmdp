package com.hmdp;

import com.hmdp.entity.Shop;
import com.hmdp.service.IShopService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hmdp.utils.RedisConstants.SHOP_GEO_KEY;

@SpringBootTest
public class GeoTest {
    @Autowired
    private IShopService shopService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    //简单测试一下geo
    //看看k-v在redis里面长什么样
    public void testRedisGeo(){
        stringRedisTemplate.opsForGeo().add("test:geo",new Point(114,40),"dalian");
        System.out.println("添加成功");
    }

    @Test
    public void testZSet(){
        stringRedisTemplate.opsForZSet().add("test:zset","lisi",18);
    }

    @Test
    public void loadGeoShopData(){
        List<Shop> list = shopService.list();
        Map<Long, List<Shop>> map = list.stream()
                .collect(Collectors.groupingBy(Shop::getTypeId));
        for(Map.Entry<Long,List<Shop>> entry:map.entrySet()){
            Long typeId = entry.getKey();
            List<Shop> shops = entry.getValue();
            String key=SHOP_GEO_KEY+typeId;
            for(Shop shop:shops){
                stringRedisTemplate.opsForGeo()
                        .add(key,new Point(shop.getX(),shop.getY()),shop.getId().toString());
            }
        }
    }
}
