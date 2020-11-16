package com.changgou.order;

import entity.FeignInterceptor;
import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableFeignClients(basePackages = {"com.changgou.goods.feign","com.changgou.user.feign"})
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.order.dao"})
public class OrderApplication {

    public static void main(String[] args) {

        SpringApplication.run(OrderApplication.class, args);

    }

    //  注入请求头信息(服务之间调用)
    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }

    //  生成订单信息订单使用
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1,1);
    }

}


