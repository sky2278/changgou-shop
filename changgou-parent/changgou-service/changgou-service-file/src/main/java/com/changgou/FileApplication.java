package com.changgou;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})       //排除自动装配数据库
@EnableEurekaClient
public class FileApplication {

    public static void main(String[] args) {

        SpringApplication.run(FileApplication.class, args);
    }
}
