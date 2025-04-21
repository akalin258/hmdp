package com.hmdp.admin.service;

import com.hmdp.admin.dto.Result;
import com.hmdp.admin.entity.Follow;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface IFollowService extends IService<Follow> {

    Result isFollow(Long bloggerId);

    Result follow(Long bloggerId, Boolean isFollow);

    Result followCommons(Long bloggerId);
}
