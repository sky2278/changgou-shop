package com.changgou.content.feign;

import com.changgou.content.pojo.Content;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:
 * @Date 2019/6/18 13:58
 *****/
@FeignClient(name = "content")
@RequestMapping("/content")
public interface ContentFeign {

    /**
     *  canal 同步广告信息
     * @param categoryId
     * @return
     */
    @RequestMapping("/findContentListByCategoryId/{categoryId}")
    Result<List<Content>> findContentListByCategoryId(@PathVariable(value = "categoryId") Long categoryId);

}