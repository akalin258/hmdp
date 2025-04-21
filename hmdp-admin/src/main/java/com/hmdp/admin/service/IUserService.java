package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.admin.dto.LoginFormDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Page<User> queryUserByPage(Integer current, Integer size, String nickName, String phone);
}
