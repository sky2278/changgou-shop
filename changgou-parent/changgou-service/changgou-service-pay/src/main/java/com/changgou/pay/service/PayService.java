package com.changgou.pay.service;

import java.util.Map;

public interface PayService {

    /**
     * 生成支付二维码
     * Map<String,String> parameters    封装参数map
     * @return
     */
    Map<String, String> createNative(Map<String,String> parameters);

    /**
     *  查询订单支付状态
     * @param outTradeNo
     * @return
     */
    Map<String, String> queryStatus(String outTradeNo);

    /**
     *  关闭订单
     * @param out_trade_no  订单id
     * @return
     */
    Map<String, String> cancelOrder(String out_trade_no);
}
