package com.hmdp.user.controller;


import com.hmdp.user.dto.Result;
import com.hmdp.user.service.IFollowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Autowired
    private IFollowService followService;
    @GetMapping("/or/not/{id}")
    public Result isFollow(@PathVariable("id") Long bloggerId){
        return followService.isFollow(bloggerId);
    }

    @PutMapping("/{id}/{isFollow}")
    public Result follow(@PathVariable("id") Long bloggerId,@PathVariable("isFollow") Boolean isFollow){
        return followService.follow(bloggerId,isFollow);
    }

    @GetMapping("/common/{id}")
    public Result followCommons(@PathVariable("id") Long bloggerId){
        return followService.followCommons(bloggerId);
    }
}
