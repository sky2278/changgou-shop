package com.changgou.seckill.threading;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.Future;

@Component
public class AsyncOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    @Async
    public Future<Boolean> createOrder() {
        //  右取
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("secKillOrderQueue").rightPop();
        try {
            if (seckillStatus != null) {
                String username = seckillStatus.getUsername();  //  用户名
                String key = seckillStatus.getTime();       //  时间段
                Long id = seckillStatus.getGoodsId();       //  商品id
                //  从redis中取出商品
                SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + key).get(id);
                //  判断商品是否为空(为空说明已卖完)
                if (seckillGoods == null) {
                    throw new RuntimeException("该商品已售罄!");
                }
                //  生成订单信息(封装各项信息)并保存到redis中
                SeckillOrder seckillOrder = new SeckillOrder();
                seckillOrder.setId(this.idWorker.nextId());  //订单id
                seckillOrder.setSeckillId(id);          // 秒杀商品id
                seckillOrder.setMoney(seckillGoods.getCostPrice()); //  秒杀商品价格(默认抢1件)
                seckillOrder.setStatus("0");        //  支付状态
                seckillOrder.setUserId(username);
                //  保存订单信息
                redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);
                //  防止超卖    扣减库存
                Long stockCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1);
                seckillGoods.setStockCount(stockCount.intValue());
                if (stockCount == 0) {  //  说明最后一件商品一件卖完了
                    //  说明最后一件商品已卖完 (将该商品从redis中删除并修改mysql中的数据信息)
                    redisTemplate.boundHashOps("SeckillGoods_" + key).delete(id);
                    //  更新数据库中的信息 (防止下一次定时任务又写入redis中)
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                } else if (stockCount < 0) {    //  此时并发执行至此 但商品库存已卖完 则不能完成购买(将此用户的排队信息,计数信息删除)
                    //  删除此用户的计数信息
                    redisTemplate.boundHashOps("UserQueueCount_" + username).delete(seckillGoods.getId());
                    //  删除此用户的排队信息
                    redisTemplate.boundHashOps("UserQueueStatus").delete(username);
                    throw new RuntimeException("该商品已售罄!");
                } else {
                    //  未卖完 更新redis中的数据
                    redisTemplate.boundHashOps("SeckillGoods_" + key).put(id, seckillGoods);
                }
                //  下单完成 更新用户订单具体关系
                seckillStatus.setMoney(Float.valueOf(seckillOrder.getMoney()));     // 订单金额
                seckillStatus.setOrderId(seckillOrder.getId());                     // 订单id
                seckillStatus.setStatus(2);                                         // 秒杀状态(待支付)
                //  将用户下单具体信息保存到redis中(二次保存查询)
                redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
                //  下单完成
                return new AsyncResult<>(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new AsyncResult<>(false);
        }
        return null;
    }




}
