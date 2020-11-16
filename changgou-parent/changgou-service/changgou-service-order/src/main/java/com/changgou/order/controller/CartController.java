package com.changgou.order.controller;

import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    /**
     * 添加商品至购物车(保存至redis)
     *
     * @param id  sku id  (商品id)
     * @param num 购买商品数量
     *            //@param username  用户姓名(唯一标识)
     */
    @RequestMapping("/add")
    public Result add(Long id, int num) {

        //String username = "zhangsan";
        //  获取token中的username
        String username = TokenDecode.getUserInfo().get("username");
        cartService.add(id, num, username);
        return new Result(true, StatusCode.OK, "添加购物车成功!");
    }

    /**
     *  查询购物车列表信息
     * @return
     */
    @RequestMapping("/list")
    public Result<List<OrderItem>> findOrdersByUsername() {
        //  获取token中的username
        String username = TokenDecode.getUserInfo().get("username");
        List<OrderItem> list = cartService.findOrdersByUsername(username);
        return new Result<>(true, StatusCode.OK, "查询购物车列表信息成功!", list);
    }

}
