package com.changgou.order.service.impl;

import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.order.pojo.OrderItem;
import com.changgou.order.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 添加商品至购物车(保存至redis)
     *
     * @param id       sku id  (商品id)
     * @param num      购买商品数量
     * @param username 用户姓名(唯一标识)
     */
    @Override
    public void add(Long id, int num, String username) {

        //  将商品信息保存到购物车
        OrderItem orderItem = addGoodsToCart(id, num);

        //  保存到redis中(hash)     检查是否重复(重复num++)

        Boolean flag = redisTemplate.boundHashOps("cart_" + username).hasKey(id);
        if (flag) {
            //  key存在 (同一商品 数量增加)
            //  先取出orderItem    再num++ 再重新保存至redis中
            OrderItem oldOrderItem = (OrderItem) redisTemplate.boundHashOps("cart_" + username).get(id);
            //  购买数量++
            oldOrderItem.setNum(oldOrderItem.getNum() + num);
            redisTemplate.boundHashOps("cart_" + username).put(id, oldOrderItem);

        } else {        //  key不存在 (直接保存至redis)
            redisTemplate.boundHashOps("cart_" + username).put(id, orderItem);
        }

    }

    /**
     * 查询购物车列表信息
     *
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> findOrdersByUsername(String username) {
        List<OrderItem> orderItemList = redisTemplate.boundHashOps("cart_" + username).values();
        return orderItemList;
    }

    //  将商品信息保存到购物车
    private OrderItem addGoodsToCart(Long id, int num) {
        //  根据skuId查询商品具体信息
        Sku sku = skuFeign.findById(id).getData();
        //  根据spuID查询商品其他信息
        Spu spu = spuFeign.findById(sku.getSpuId());
        //  封装信息()
        OrderItem orderItem = new OrderItem();

        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());    //  spuID
        orderItem.setSkuId(id);             //  skuID
        orderItem.setName(sku.getName());   //  商品名称
        orderItem.setPrice(sku.getPrice()); //  商品价格
        orderItem.setNum(num);              //  商品数量
        orderItem.setMoney(sku.getPrice() * num);   //  总金额
        orderItem.setPayMoney(orderItem.getMoney() - 0);  //  实付金额
        orderItem.setImage(sku.getImage());     //  图片地址
        orderItem.setPostFee(10);       //  运费
        orderItem.setIsReturn("0");    //   退货状态(0未退货)
        return orderItem;
    }
}
