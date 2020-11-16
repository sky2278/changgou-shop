package com.changgou;

import entity.IdWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.changgou.seckill.dao"})
@EnableScheduling       //  开启定时任务
@EnableAsync        //  开启异步执行
public class SecKillApplication {

    public static void main(String[] args) {

        SpringApplication.run(SecKillApplication.class, args);

    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(1, 1);
    }

}
