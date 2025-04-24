package com.hmdp.admin.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Blog;
import com.hmdp.admin.entity.User;
import com.hmdp.admin.entity.BlogComments;
import com.hmdp.admin.mapper.BlogMapper;
import com.hmdp.admin.service.IBlogService;
import com.hmdp.admin.service.IUserService;
import com.hmdp.admin.service.IBlogCommentsService;
import com.hmdp.admin.utils.SystemConstants;
import com.hmdp.admin.utils.UserHolder;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.hmdp.admin.utils.RedisConstants.BLOG_LIKED_KEY;


/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {

    @Autowired
    private IUserService userService;
    
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private BlogMapper blogMapper;
    
    @Autowired
    private IBlogCommentsService blogCommentsService;

    @Override
    public Result queryHotBlog(Integer current) {
        // 根据用户查询
        Page<Blog> page = query()
                .orderByDesc("liked")
                .page(new Page<>(current, SystemConstants.MAX_PAGE_SIZE));
        // 获取当前页数据
        List<Blog> records = page.getRecords();
        // 查询用户
        records.forEach(blog ->{
            Long userId = blog.getUserId();
            Long blogId = blog.getId();
            User user = userService.getById(userId);
            blog.setName(user.getNickName());
            blog.setIcon(user.getIcon());
            Boolean isCurUserLiked = stringRedisTemplate.opsForSet().isMember(BLOG_LIKED_KEY + blogId, userId.toString());
            blog.setIsLike(isCurUserLiked);
        });
        return Result.ok(records);
    }

    @Override
    public Result queryBlogById(Long id) {
        Blog blog = getById(id);
        if(blog==null){
            return Result.fail("blog不存在");
        }
        //blog上面还要有用户信息
        //博主name,头像小图标
        Long userId = blog.getUserId();
        User user = userService.getById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
        Boolean isCurUserLiked = stringRedisTemplate.opsForSet().isMember(BLOG_LIKED_KEY + id, userId.toString());
        blog.setIsLike(isCurUserLiked);
        return Result.ok(blog);
    }

    @Override
    public Result likeBlog(Long id) {
        //id:blogId
        //1.获取userId
        Long userId = UserHolder.getUser().getId();
        //2.去redis里查,是否已经点过赞了
        //key:前缀+blogId,val:许多userId
        String key=BLOG_LIKED_KEY + id;
        //stringRedisTemplate一般传入String类型
        Boolean isLiked = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if(!BooleanUtil.isTrue(isLiked)){
            //没点过赞
            int row = blogMapper.addLike(id);
            if(row>0){
                //存入redis里面
                stringRedisTemplate.opsForSet().add(key,userId.toString());
            }
        }else{
            int row=blogMapper.removeLike(id);
            if(row>0){
                stringRedisTemplate.opsForSet().remove(key,userId.toString());
            }
        }
        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long blogId) {
        List<String> userIds = stringRedisTemplate.opsForSet()
                .randomMembers(BLOG_LIKED_KEY + blogId, 5);
        // 如果 userIds 为空，直接返回空列表
        if (userIds == null || userIds.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 将 userIds 转换为 List<Long>

        List<Long> userIdList = userIds.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());

        // 根据 userIds 查询用户信息（假设你有一个 userMapper 可以查询用户信息）

        List<User> users = userService.listByIds(userIdList);

        // 返回用户信息
        return Result.ok(users);
    }

    @Override
    public Page<Blog> queryBlogByPage(Integer current, Integer size, String title, String status, Long shopId, String orderBy, String order) {
        // 创建分页对象
        Page<Blog> page = new Page<>(current, size);
        
        // 构建查询条件
        LambdaQueryWrapper<Blog> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加标题模糊查询条件
        if (StrUtil.isNotBlank(title)) {
            queryWrapper.like(Blog::getTitle, title);
        }
        
        // 添加店铺ID筛选条件
        if (shopId != null) {
            queryWrapper.eq(Blog::getShopId, shopId);
        }
        
        // 处理排序
        if (StrUtil.isNotBlank(orderBy)) {
            // 判断排序字段是否合法
            boolean isAsc = "asc".equalsIgnoreCase(order);
            
            // 根据不同的排序字段构建不同的排序条件
            switch (orderBy) {
                case "liked":
                    queryWrapper.orderBy(true, isAsc, Blog::getLiked);
                    break;
                case "comments":
                    queryWrapper.orderBy(true, isAsc, Blog::getComments);
                    break;
                case "createTime":
                    queryWrapper.orderBy(true, isAsc, Blog::getCreateTime);
                    break;
                default:
                    // 默认按创建时间降序
                    queryWrapper.orderByDesc(Blog::getCreateTime);
            }
        } else {
            // 默认按创建时间降序
            queryWrapper.orderByDesc(Blog::getCreateTime);
        }
        
        // 执行查询
        return page(page, queryWrapper);
    }

    @Override
    public boolean removeBlog(Long id) {
        // 1. 查询博客是否存在
        Blog blog = getById(id);
        if (blog == null) {
            return false;
        }
        
        // 2. 删除博客评论
        blogCommentsService.remove(new LambdaQueryWrapper<BlogComments>()
                .eq(BlogComments::getBlogId, id));
        
        // 3. 删除博客
        boolean success = removeById(id);
        
        // 4. 如果删除成功，还需要处理相关的点赞数据
        if (success) {
            // 4.1 删除Redis中的点赞数据
            String likeKey = BLOG_LIKED_KEY + id;
            stringRedisTemplate.delete(likeKey);
        }
        
        return success;
    }
}
