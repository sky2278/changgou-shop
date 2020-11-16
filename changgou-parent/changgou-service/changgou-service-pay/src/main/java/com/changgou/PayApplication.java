package com.changgou;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
@EnableEurekaClient
public class PayApplication {

    public static void main(String[] args) {

        SpringApplication.run(PayApplication.class, args);

    }

    @Autowired
    private Environment evn;

    //  普通订单队列-交换机
    //  创建队列队列
    @Bean   //  队列名称 是否持久化
    public Queue orderQueue() {
        return new Queue(evn.getProperty("mq.pay.queue.order"), true);
    }

    //  创建交换机
    @Bean   //  交换机名称 是否持久化 是否自动删除
    public Exchange orderExchange() {
        return new DirectExchange(evn.getProperty("mq.pay.exchange.order"), true, false);
    }

    //  将队列绑定到交换机
    @Bean
    public Binding bindingQueueToExchange(Queue orderQueue, Exchange orderExchange) {
        return BindingBuilder.bind(orderQueue).to(orderExchange).with("mq.pay.routing.key").noargs();
    }

    //  秒杀商品订单-交换机
    @Bean
    public Queue secKillQueue() {
        return new Queue(evn.getProperty("mq.pay.queue.seckillorder"), true);
    }

    @Bean
    public Exchange secKillExchange() {
        return new DirectExchange(evn.getProperty("mq.pay.exchange.seckillorder"), true, false);
    }

    @Bean
    public Binding queueBindToExchangeForSeckillOrder(Queue secKillQueue, Exchange secKillExchange) {
        return BindingBuilder.bind(secKillQueue).to(secKillExchange).with(evn.getProperty("mq.pay.routing.seckillkey")).noargs();
    }

}
