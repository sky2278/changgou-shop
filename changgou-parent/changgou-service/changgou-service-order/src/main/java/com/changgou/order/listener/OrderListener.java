package com.changgou.order.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderListener {

    @Autowired
    private OrderService orderService;

    /**
     * 监听队列
     *
     * @param content
     */
    @RabbitListener(queues = {"${mq.pay.queue.order}"})
    public void orderListener(String content) {

        //  将数据转为map
        Map<String, String> map = JSON.parseObject(content, Map.class);

        //  此表示仅为通信表示,并非交易标识
        String return_code = map.get("return_code");
        if ("SUCCESS".equals(return_code)) {
            //  获取成功交易标识
            String out_trade_no = map.get("out_trade_no");          //  订单id
            String transaction_id = map.get("transaction_id");      //  获取交易订单流水号
            String result_code = map.get("result_code");            //  是否成功交易标识
            if ("SUCCESS".equals(result_code)) {
                //  交易成功 (更新订单信息并将订单信息从redis中删除订单信息)
                orderService.updateOrder(out_trade_no, transaction_id);
            } else {
                //  交易失败 (将订单信息从redis中删除)
                orderService.deleteOrder(out_trade_no);
            }
        }

    }

}
