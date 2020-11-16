package com.changgou.seckill.service;

import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:SeckillOrder业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SeckillOrderService {


    /** 支付失败 未支付 超时...
     * @param username
     */
    void deleteSecKillOrder(String username);

    /**
     * 支付成功将订单信息保存到数据库中
     *
     * @param username
     * @param out_trade_no
     * @param transaction_id
     * @param time_end
     */
    void updateSecKillOrder(String username, String out_trade_no, String transaction_id, String time_end);

    /**
     * 秒杀商品下单实现
     *
     * @param key      时间戳
     * @param id       秒杀商品id
     * @param username 用户名
     * @return
     */
    Boolean add(String key, Long id, String username);

    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /***
     * 新增SeckillOrder
     * @param seckillOrder
     */
    Boolean add(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();

    /**
     * 查询用户订单支付状态
     *
     * @param username 用户名
     * @return
     */
    SeckillStatus queryStatus(String username);


}
