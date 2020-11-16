package com.changgou.pay.controller;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.PayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/weixin/pay")
public class PayController {

    @Autowired
    private PayService payService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private Environment env;

    /**
     http://localhost:18091/seckill/goods/list?key=2020081218
     http://localhost:18091/seckill/goods/one?key=2020081218&id=1131815260454522880
     http://localhost:18091//seckill/order/add?key=2020081218&id=1131815260454522880
     http://localhost:18091///seckill/order/query/status?username=zhangsan
     http://localhost:18090/weixin/pay/create/native?out_trade_no=1131815260454522880&total_fee=100&username=zhangsan&exchange=mq.pay.exchange.seckillorder&routingKey=mq.pay.routing.seckillkey
     http://localhost:18090/weixin/pay/cancel/order/?out_trade_no=1131815260454522880
     */



    /**
     * 关闭订单
     *
     * @param out_trade_no 订单id
     * @return
     */
    @RequestMapping("/cancel/order")
    public Result cancelOrder(String out_trade_no) {

        Map<String, String> map = payService.cancelOrder(out_trade_no);
        System.out.println("cancelOrder-Message:" + map);
        String return_code = map.get("return_code");    //  通信标识
        if ("SUCCESS".equals(return_code)) {
            String result_code = map.get("result_code");
            if ("SUCCESS".equals(result_code)) {
                return new Result(true, StatusCode.OK, "关闭订单成功!");
            }
        }
        return new Result(false, StatusCode.ERROR, "关闭订单失败!");
    }

    /**
     * 将微信返回的数据发到mq中
     *
     * @param request
     * @return
     * @throws IOException
     */
    @RequestMapping("/notify/url")
    public String notifyUrl(HttpServletRequest request) throws Exception {
        System.out.println("回调函数执行了...");

        //  获取微信返回的数据
        ServletInputStream is = request.getInputStream();
        //  创建一个字节数组输出流对象
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int len = 0;
        //  写入输出流中
        while ((len = is.read(bytes)) != -1) {
            os.write(bytes, 0, len);
        }
        os.flush();
        os.close();
        is.close();
        
        //  将输出流中的数据转为map
        String xmlStr = new String(os.toByteArray(), "UTF-8");
        Map<String, String> map = WXPayUtil.xmlToMap(xmlStr);

        System.out.println("Controller:" + map);

        //  TO DO    将微信的通知结果数据发送到RabbitMQ中
        //  路由名称
        //String routingKey = env.getProperty("mq.pay.routing.key");
        //  交换机名称
        //String exchange = env.getProperty("mq.pay.exchange.order");

        //  秒杀订单
        //  ===================================================================
        String attach = map.get("attach");   //  取出微信返回的数据包(包含交换机-路由名称)
        Map<String, String> attachMap = JSON.parseObject(attach, Map.class);
        //  配置文件中获取
        String exchange = env.getProperty(attachMap.get("exchange"));
        String routingKey = env.getProperty(attachMap.get("routingKey"));
        System.out.println("交换机:" + exchange + "---路由:" + routingKey);
        System.out.println();
        //  ===================================================================
        //  须先将map转为json串
        rabbitTemplate.convertAndSend(exchange, routingKey, JSON.toJSONString(map));
        //{transaction_id=4200000714202008086695991895, nonce_str=e40829ee3cb549788cdb806d1c2ffd0f, bank_type=OTHERS, openid=oNpSGwfVCBpAMv15cfmsDV5oFwJc, sign=D3A98EFBDF1C1E346BBCADF82D3D09C5, fee_type=CNY, mch_id=1473426802, cash_fee=1, out_trade_no=xyz00012, appid=wx8397f8696b538317, total_fee=1, trade_type=NATIVE, result_code=SUCCESS, time_end=20200808201321, is_subscribe=N, return_code=SUCCESS}

        return "success";
    }

    /**
     * 查询订单支付状态
     *
     * @param outTradeNo
     * @return
     */
    @RequestMapping("/query/status")
    public Result queryStatus(String outTradeNo) {
        Map<String, String> map = payService.queryStatus(outTradeNo);
        return new Result(true, StatusCode.OK, "查询订单支付状态成功!", map);
    }


    /**
     * 生成支付二维码
     *
     * @return
     */
    @RequestMapping("/create/native")
    public Result createNative(@RequestParam Map<String, String> parameters) {
        //  普通订单
        //Map<String, String> map = payService.createNative(map);

        //  普通订单+秒杀订单
        Map<String, String> map = payService.createNative(parameters);
        return new Result(true, StatusCode.OK, "生成支付链接成功!", map);
    }

}
