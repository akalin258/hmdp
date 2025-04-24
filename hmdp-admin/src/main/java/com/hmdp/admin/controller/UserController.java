package com.hmdp.admin.controller;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.LoginFormDTO;
import com.hmdp.admin.dto.UserDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.User;
import com.hmdp.admin.entity.UserInfo;

import com.hmdp.admin.service.IUserInfoService;
import com.hmdp.admin.service.IUserService;
import com.hmdp.admin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Map;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private IUserService userService;

    /**
     * 获取用户列表
     * @param current 当前页
     * @param size 每页数量
     * @param nickName 用户昵称
     * @param phone 手机号
     * @return 分页用户列表
     */
    @GetMapping("/list")
    public Result getUserList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "nickName", required = false) String nickName,
            @RequestParam(value = "phone", required = false) String phone
    ) {
        //分页查询，核心sql
        //select ... from tb_user limit #{current} offset #{size} ....
        Page<User> userPage = userService.queryUserByPage(current, size, nickName, phone);
        return Result.ok(userPage);
    }

    /**
     * 获取用户详情
     * @param id 用户ID
     * @return 用户详情
     */
    @GetMapping("/{id}")
    public Result getUserDetail(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.ok(user);
    }

    /**
     * 更新用户状态
     * @param params 包含id和status的参数
     * @return 操作结果
     */
    /*@PutMapping("/status")
    public Result updateUserStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Boolean status = (Boolean) params.get("status");

        boolean success = userService.updateUserStatus(id, status);
        if (!success) {
            return Result.fail("更新用户状态失败");
        }
        return Result.ok("操作成功");
    }*/

    /**
     * 重置用户密码
     * @param id 用户ID
     * @return 操作结果
     */
    @PutMapping("/reset-password/{id}")
    public Result resetUserPassword(@PathVariable("id") Long id) {
        boolean success = userService.resetUserPassword(id);
        if (!success) {
            return Result.fail("重置密码失败");
        }
        return Result.ok("密码重置成功");
    }



}
