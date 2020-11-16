package io.seata.sample.controller;

import io.seata.sample.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jimin.jm@alibaba-inc.com
 * @date 2019/06/14
 */

@RestController
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     *
     * @param userId  用户ID
     * @param commodityCode 要购买的商品的id
     * @param count 购买的数量
     * @return
     */
    @GetMapping(value = "/create", produces = "application/json")
    public Boolean create(String userId, String commodityCode, Integer count) {

        orderService.create(userId, commodityCode, count);
        return true;
    }

}
