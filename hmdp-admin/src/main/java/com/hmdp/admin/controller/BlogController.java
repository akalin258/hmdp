package com.hmdp.admin.controller;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.UserDTO;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Blog;

import com.hmdp.admin.service.IBlogService;
import com.hmdp.admin.service.IUserService;
import com.hmdp.admin.utils.SystemConstants;
import com.hmdp.admin.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/blog")
@Slf4j
public class BlogController {

    @Resource
    private IBlogService blogService;
    @Resource
    private IUserService userService;

    @PostMapping
    public Result saveBlog(@RequestBody Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 保存探店博文
        blogService.save(blog);
        // 返回id
        return Result.ok(blog.getId());
    }

    @PutMapping("/like/{id}")
    public Result likeBlog(@PathVariable("id") Long id) {
        // 修改点赞数量
        /*blogService.update()
                .setSql("liked = liked + 1").eq("id", id).update();
        return Result.ok();*/
        //给该blog点赞
        return blogService.likeBlog(id);
    }

    @GetMapping("/of/me")
    public Result queryMyBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", user.getId()).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    @GetMapping("/hot")
    //把下面这些东西放到service里面
    public Result queryHotBlog(@RequestParam(value = "current", defaultValue = "1") Integer current) {
        return blogService.queryHotBlog(current);
    }

    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id){
        return blogService.queryBlogById(id);
    }

    //获取部分点赞的用户
    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long blogId){
        return blogService.queryBlogLikes(blogId);
    }

    // BlogController  根据id查询博主的探店笔记
    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam("id") Long id) {
        // 根据用户查询
        Page<Blog> page = blogService.query()
                .eq("user_id", id).page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        return Result.ok(records);
    }

    /**
     * 分页查询博客列表（用于后台管理）
     * @param current 当前页
     * @param size 每页数量
     * @param title 标题关键词
     * @param shopId 店铺ID
     * @param orderBy 排序字段
     * @param order 排序方式（asc/desc）
     * @return 博客分页列表
     */
    @GetMapping("/list")
    public Result getBlogList(
            @RequestParam(value = "current", defaultValue = "1") Integer current,
            @RequestParam(value = "size", defaultValue = "10") Integer size,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "shopId", required = false) Long shopId,
            @RequestParam(value = "orderBy", required = false) String orderBy,
            @RequestParam(value = "order", required = false) String order
    ) {
        log.info("获取博客列表: current={}, size={}, title={}, shopId={}, orderBy={}, order={}", 
                current, size, title, shopId, orderBy, order);
        // 调用服务层方法，添加店铺ID参数
        Page<Blog> blogPage = blogService.queryBlogByPage(current, size, title, null, shopId, orderBy, order);
        return Result.ok(blogPage);
    }

    /**
     * 删除探店笔记
     * @param id 笔记ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    public Result deleteBlog(@PathVariable("id") Long id) {
        log.info("删除探店笔记: id={}", id);
        boolean success = blogService.removeBlog(id);
        if (success) {
            return Result.ok();
        } else {
            return Result.fail("删除失败，笔记不存在或已被删除");
        }
    }
}
