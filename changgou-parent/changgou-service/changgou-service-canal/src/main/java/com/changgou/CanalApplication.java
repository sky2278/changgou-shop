package com.changgou;

import com.xpand.starter.canal.annotation.EnableCanalClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
@EnableCanalClient      //  开启canal客户端
@EnableFeignClients(basePackages = {"com.changgou.content.feign"})  //  开启feign
public class CanalApplication {

    public static void main(String[] args) {

        SpringApplication.run(CanalApplication.class, args);
    }
}