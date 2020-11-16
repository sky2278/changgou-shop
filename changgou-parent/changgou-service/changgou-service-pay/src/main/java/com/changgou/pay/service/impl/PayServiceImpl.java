package com.changgou.pay.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.pay.service.PayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    //  微信公众账号或开放平台APP的唯一标识
    @Value("${weixin.appid}")
    private String appid;

    //  财付通平台的商户账号
    @Value("${weixin.partner}")
    private String partner;

    //  财付通平台的商户密钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;

    //  回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;


    /**
     * 生成支付二维码
     * <p>
     * Map<String,String> parameters    参数map
     * String outTradeNo, String total_fee
     *
     * @return
     */
    @Override
    public Map<String, String> createNative(Map<String, String> parameters) {

        //  微信支付接口url
        String url = "https://api.mch.weixin.qq.com/pay/unifiedorder";

        try {
            //  封装微信支付接口需要的参数
            HashMap<String, String> data = new HashMap<>();
            //公众账号ID
            data.put("appid", appid);
            //商户号	mch_id
            data.put("mch_id", partner);
            //随机字符串	nonce_str	是	String(32)
            data.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名	sign
            // TO DO
            //商品描述	body
            data.put("body", "畅购商城-购买商品");
            //商户订单号	out_trade_no
            data.put("out_trade_no", parameters.get("out_trade_no"));
            //标价金额	total_fee
            data.put("total_fee", parameters.get("total_fee"));
            //终端IP
            data.put("spbill_create_ip", "127.0.0.1");
            //通知地址	notify_url	是
            data.put("notify_url", notifyurl);
            //交易类型	trade_type	是	String(16)	JSAPI
            data.put("trade_type", "NATIVE");

            //  封装数据包(attach 提供唯一标识 普通订单 秒杀订单)
            //(交换机 路由 username )
            HashMap<String, String> attachMap = new HashMap<>();
            attachMap.put("username", parameters.get("username"));       //  用户名
            attachMap.put("exchange", parameters.get("exchange"));       //  交换机
            attachMap.put("routingKey", parameters.get("routingKey"));       //  路由
            data.put("attach", JSON.toJSONString(attachMap));   //  转为json字符串

            //  将参数集合map转为xml格式 (并指定签名key)
            String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);

            //  httpClient发送请求(微信支付接口)
            HttpClient httpClient = new HttpClient(url);
            //  请求数据
            httpClient.setXmlParam(signedXml);
            //  http请求
            httpClient.setHttps(true);
            //  请求方式
            httpClient.post();
            //  获取返回的结果数据(xml格式)
            String resultXML = httpClient.getContent();
            //  将请求结果数据转为map
            Map<String, String> map = WXPayUtil.xmlToMap(resultXML);
            map.put("outTradeNo", parameters.get("out_trade_no"));   //  订单号
            map.put("total_fee", parameters.get("total_fee"));     //  订单金额
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单支付状态
     *
     * @param outTradeNo
     * @return
     */
    @Override
    public Map<String, String> queryStatus(String outTradeNo) {

        try {
            //  查询订单状态调用地址
            String url = "https://api.mch.weixin.qq.com/pay/orderquery";

            HashMap<String, String> data = new HashMap<>();
            //  封装查询订单状态信息参数

            //公众账号ID	appid	是	String(32)	wxd678efh567hg6787
            data.put("appid", appid);

            //商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
            data.put("mch_id", partner);

            //商户订单号	out_trade_no	String(32)	20150806125346
            data.put("out_trade_no", outTradeNo);

            //随机字符串	nonce_str	是	String(32)
            data.put("nonce_str", WXPayUtil.generateNonceStr());

            //签名	sign	是	String(32)
            // TO DO
            //  将参数集合map转为xml格式并指定签名
            String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);

            //  发送请求 查询订单支付状态
            HttpClient httpClient = new HttpClient(url);
            //  指定参数
            httpClient.setXmlParam(signedXml);
            //  https方式
            httpClient.setHttps(true);
            //  指定请求方式
            httpClient.post();


            //  获取返回结果(xml格式)
            String resultXMl = httpClient.getContent();

            //  将xml格式的结果集参数转为map
            Map<String, String> map = WXPayUtil.xmlToMap(resultXMl);
            //  返回map
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 关闭订单
     *
     * @param   out_trade_no 订单id
     * @return
     */
    @Override
    public Map<String, String> cancelOrder(String out_trade_no) {

        try {
            //  关闭订单链接地址
            String url = "https://api.mch.weixin.qq.com/pay/closeorder";
            HashMap<String, String> data = new HashMap<>();
            //公众账号ID	appid	是	String(32)
            data.put("appid", appid);
            //商户号	mch_id	是	String(32)
            data.put("mch_id", partner);
            //商户订单号	out_trade_no	是	String(32)
            data.put("out_trade_no", out_trade_no);
            //随机字符串	nonce_str	是	String(32)
            data.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名	sign	是	String(32)
            String signedXml = WXPayUtil.generateSignedXml(data, partnerkey);
            //  发送http请求方式
            HttpClient httpClient = new HttpClient(url);
            //  参数
            httpClient.setXmlParam(signedXml);
            //  https请求
            httpClient.setHttps(true);
            //  请求方式
            httpClient.post();

            //  获取返回结果
            String resultXml = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(resultXml);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
