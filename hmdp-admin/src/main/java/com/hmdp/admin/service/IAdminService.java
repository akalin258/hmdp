package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.admin.dto.AdminLoginFormDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Admin;

import javax.servlet.http.HttpSession;

public interface IAdminService extends IService<Admin> {
    Result login(AdminLoginFormDTO loginForm, HttpSession session);
    Result logout(HttpSession session);
}
