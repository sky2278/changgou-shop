package com.changgou.controller;

import com.changgou.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page")
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable(value = "id") Long id) {
        pageService.createHtml(id);
        return new Result(true, StatusCode.OK, "静态页面生成成功!");
    }

}
