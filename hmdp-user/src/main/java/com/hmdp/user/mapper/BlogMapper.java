package com.hmdp.user.mapper;

import com.hmdp.user.entity.Blog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
public interface BlogMapper extends BaseMapper<Blog> {
    @Update("update tb_blog set liked=liked+1 where id=#{blogId}")
    int addLike(@Param("blogId") Long blogId);

    @Update("update tb_blog set liked=liked-1 where id=#{blogId}")
    int removeLike(@Param("blogId") Long blogId);
}
