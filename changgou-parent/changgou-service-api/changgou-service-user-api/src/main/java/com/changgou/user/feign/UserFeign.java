package com.changgou.user.feign;

import com.changgou.user.pojo.User;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "user")
@RequestMapping("/user")
public interface UserFeign {

    /**
     * 添加会员积分
     * @param username
     * @param points
     */
    @RequestMapping("/increase/{username}/{points}")
    void increase(@PathVariable(value = "username") String username, @PathVariable(value = "points") Integer points);


    @GetMapping("/{id}")
    Result<User> findById(@PathVariable(value = "id") String id);

}
