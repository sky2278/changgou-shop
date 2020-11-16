package com.changgou.seckill.task;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SecKillGoodsTask {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 将秒杀商品压入redis中
     */
    @Scheduled(cron = "0/50 * * * * ? ")
    public void insertSeckillGoodsToRedisByTimer() {
        System.out.println("定时任务执行了...");
        //  获取时间段
        List<Date> dateMenus = DateUtil.getDateMenus();
        for (Date dateMenu : dateMenus) {
            //  时间段
            String key_rule = DateUtil.data2str(dateMenu, DateUtil.PATTERN_YYYYMMDDHH); //格式 2020080918
            //  查询符合条件的秒杀商品
            Example example = new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();
            //  封装条件 审核状态为1 库存大于0 开始时间-结束时间
            criteria.andEqualTo("status", "1");  //  审核状态
            criteria.andGreaterThan("stockCount", 0);     //  库存大于0

            criteria.andGreaterThanOrEqualTo("startTime", dateMenu); //  开始时间大于或等次此时间段
            criteria.andLessThanOrEqualTo("endTime", DateUtil.addDateHour(dateMenu, 2));  //  结束时间小于或等于此时间段

            //  去重 已存在的数据不再将从数据库中查询也不存压入redis中
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + key_rule).keys();   //获取redis中已存在secKill商品的id
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id", keys);
            }

            //  返回符合条件的结果
            List<SeckillGoods> secKillGoods = seckillGoodsMapper.selectByExample(example);
            System.out.println("size:" + secKillGoods.size());
            if (secKillGoods != null && secKillGoods.size() > 0) {
                //  压入redis中
                for (SeckillGoods secKillGood : secKillGoods) {
                    //  保存至redis中                                   时间戳         商品id                  商品
                    redisTemplate.boundHashOps("SeckillGoods_" + key_rule).put(secKillGood.getId(), secKillGood);
                    //  保存商品的库存信息(防止超卖,利用redis的单线程即使更新订单信息)
                    //  防止超卖
                    redisTemplate.boundHashOps("SeckillGoodsCount").increment(secKillGood.getId(),secKillGood.getStockCount());
                }
            }
        }
    }

}
