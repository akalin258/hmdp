package com.hmdp.admin.service.impl;


import com.hmdp.admin.entity.UserInfo;
import com.hmdp.admin.mapper.UserInfoMapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.admin.service.IUserInfoService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-24
 */
@Service
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfo> implements IUserInfoService {

}
