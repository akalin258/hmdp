package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验电话号
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //2.生成6位数的code
        String code = RandomUtil.randomNumbers(6);
        String key=LOGIN_CODE_KEY+phone;
        stringRedisTemplate.opsForValue().set(key,code,LOGIN_CODE_TTL,TimeUnit.MINUTES);
        //3.在idea控制台输出,模拟一下发送短信的功能
        log.debug("发送短信验证码成功，验证码：{}", code);
        return Result.ok();
        //return Result.ok();
    }

    @Override
    //tip:集登录注册为一体的
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //0.取出phone和code
        String phone = loginForm.getPhone();
        String code = loginForm.getCode();
        //1.校验电话号
        if(RegexUtils.isPhoneInvalid(phone)){
            return Result.fail("手机号格式错误");
        }
        //2.校验验证码
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        if(cacheCode==null || !code.equals(cacheCode)){
            return Result.fail("验证码错误");
        }
        //3.查询这个用户是否存在,没有的话创建
        User user = query().eq("phone", phone).one();
        if(user==null){
            user=createUserWithPhone(phone);
        }
        //4.将用户信息存入redis
        String token = UUID.randomUUID().toString(true);
        String key=LOGIN_USER_KEY+token;
        Map<String,String> userMap=new HashMap<>();
        userMap.put("id",user.getId().toString());
        userMap.put("nickName",user.getNickName());
        // 修正为
        userMap.put("icon", user.getIcon());
        stringRedisTemplate.opsForHash().putAll(key,userMap);
        //5.设置一下过期时间
        stringRedisTemplate.expire(key,LOGIN_USER_TTL, TimeUnit.MINUTES);
        //6.返回一下token
        return Result.ok(token);

    }

    @Override
    public Result sign() {
        // 1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        // 5.写入Redis SETBIT key offset 1
        stringRedisTemplate.opsForValue().setBit(key, dayOfMonth - 1, true);
        return Result.ok();
    }

    @Override
    public Result signCount() {
        // 1.获取当前登录用户
        Long userId = UserHolder.getUser().getId();
        // 2.获取日期
        LocalDateTime now = LocalDateTime.now();
        // 3.拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern(":yyyyMM"));
        String key = USER_SIGN_KEY + userId + keySuffix;
        // 4.获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();

        //5.从今天开始向前统计
        //和游戏中一样,从之前到今天连续签到的日期数
        int cnt=0;
        for(int i=dayOfMonth;i>=1;i--){
            Boolean isSign = stringRedisTemplate.opsForValue().getBit(key, i - 1);
            if(BooleanUtil.isTrue(isSign)){
                cnt++;
            }else{
                break;
            }
        }
        return Result.ok(cnt);
    }

    private User createUserWithPhone(String phone) {
        // 1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        // 2.保存用户
        save(user);
        return user;
    }
}
