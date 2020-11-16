package com.changgou.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
@ControllerAdvice
public class IgnoreResourceConfig implements WebMvcConfigurer {
    /**
     * 静态资源放行
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //      路径注意!!!
        registry.addResourceHandler("/items/**").addResourceLocations("classpath:/templates/items/");
    }
}
