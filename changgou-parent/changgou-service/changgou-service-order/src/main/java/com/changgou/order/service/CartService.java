package com.changgou.order.service;

import com.changgou.order.pojo.OrderItem;

import java.util.List;

public interface CartService {
    /**
     *  添加商品至购物车(保存至redis)
     * @param id    sku id  (商品id)
     * @param num   购买商品数量
     * @param username  用户姓名(唯一标识)
     */
    void add(Long id, int num, String username);

    /**
     *  查询购物车列表信息
     * @param username
     * @return
     */
    List<OrderItem> findOrdersByUsername(String username);
}
