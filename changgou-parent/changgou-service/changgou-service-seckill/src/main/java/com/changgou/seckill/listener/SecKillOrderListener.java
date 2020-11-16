package com.changgou.seckill.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.service.SeckillOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SecKillOrderListener {

    @Autowired
    private SeckillOrderService seckillOrderService;

    @RabbitListener(queues = {"${mq.pay.queue.seckillorder}"})
    public void secKillOrderListener(String content) {

        //  将返回结果转为map
        Map<String, String> map = JSON.parseObject(content, Map.class);
        //System.out.println("listener:" + map);

        //    通信标识
        String return_code = map.get("return_code");
        //  获取用户名
        if ("SUCCESS".equals(return_code)) {
            String username = (String) JSON.parseObject(map.get("attach"), Map.class).get("username");
            String result_code = map.get("result_code");
            if ("SUCCESS".equals(result_code)) {    //  更新订单信息
                String transaction_id = map.get("transaction_id");  //  支付流水号
                String out_trade_no = map.get("out_trade_no");      //  订单号
                String time_end = map.get("time_end");              //  支付时间
                //  支付成功将订单信息持久化到数据库中
                seckillOrderService.updateSecKillOrder(username, out_trade_no, transaction_id, time_end);

            } else {      //  (未支付,超时..删除订单)
                seckillOrderService.deleteSecKillOrder(username);
            }
        }


    }

}
