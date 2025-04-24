package com.hmdp.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Blog;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IBlogService extends IService<Blog> {

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);

    Result likeBlog(Long id);

    Result queryBlogLikes(Long blogId);
    
    /**
     * 分页查询博客
     * @param current 当前页
     * @param size 每页数量
     * @param title 标题关键词
     * @param status 状态
     * @param shopId 店铺ID
     * @param orderBy 排序字段
     * @param order 排序方式（asc/desc）
     * @return 博客分页结果
     */
    Page<Blog> queryBlogByPage(Integer current, Integer size, String title, String status, Long shopId, String orderBy, String order);
    
    /**
     * 删除探店笔记
     * @param id 笔记ID
     * @return 是否删除成功
     */
    boolean removeBlog(Long id);
}
