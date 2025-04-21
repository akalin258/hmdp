package com.hmdp.admin.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import cn.hutool.core.util.StrUtil;

import com.hmdp.admin.dto.LoginFormDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.User;
import com.hmdp.admin.mapper.UserMapper;

import com.hmdp.admin.service.IUserService;
import com.hmdp.admin.utils.RegexUtils;
import com.hmdp.admin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.admin.utils.RedisConstants.*;
import static com.hmdp.admin.utils.SystemConstants.USER_NICK_NAME_PREFIX;


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


    @Override
    public Page<User> queryUserByPage(Integer current, Integer size, String nickName, String phone) {
        // 创建分页对象
        Page<User> page = new Page<>(current, size);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加昵称模糊查询条件
        if (StrUtil.isNotBlank(nickName)) {
            queryWrapper.like(User::getNickName, nickName);
        }
        
        // 添加手机号模糊查询条件
        if (StrUtil.isNotBlank(phone)) {
            queryWrapper.like(User::getPhone, phone);
        }
        
        // 按创建时间降序排序
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 执行查询
        return page(page, queryWrapper);
    }
}
