package com.hmdp.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.admin.dto.AdminLoginFormDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Admin;
import com.hmdp.admin.mapper.AdminMapper;

import com.hmdp.admin.service.IAdminService;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {
    @Override
    public Result login(AdminLoginFormDTO loginForm, HttpSession session) {
        String username = loginForm.getUsername();
        String password = loginForm.getPassword();
        Admin admin = query().eq("username", username)
                .eq("password", password)
                .one();
        if(admin==null){
            return Result.fail("用户不存在");
        }
        session.setAttribute("admin",admin);
        return Result.ok("登录成功");
    }

    @Override
    public Result logout(HttpSession session) {
        session.removeAttribute("admin");
        return Result.ok();
    }
}
