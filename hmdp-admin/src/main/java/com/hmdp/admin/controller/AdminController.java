package com.hmdp.admin.controller;

import com.hmdp.admin.dto.AdminLoginFormDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.service.IAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @PostMapping("/login")
    public Result login(@RequestBody AdminLoginFormDTO loginForm, HttpSession session){
        return adminService.login(loginForm,session);
    }

    @PostMapping("/logout")
    public Result logout(HttpSession session){
        return adminService.logout(session);
    }
}
