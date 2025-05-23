package com.hmdp.admin.service.impl;

import com.hmdp.admin.entity.BlogComments;
import com.hmdp.admin.mapper.BlogCommentsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.admin.service.IBlogCommentsService;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class BlogCommentsServiceImpl extends ServiceImpl<BlogCommentsMapper, BlogComments> implements IBlogCommentsService {

}
