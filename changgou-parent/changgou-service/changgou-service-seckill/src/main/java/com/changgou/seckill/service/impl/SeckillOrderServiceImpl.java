package com.changgou.seckill.service.impl;

import com.changgou.seckill.dao.SeckillGoodsMapper;
import com.changgou.seckill.dao.SeckillOrderMapper;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.pojo.SeckillStatus;
import com.changgou.seckill.service.SeckillOrderService;
import com.changgou.seckill.threading.AsyncOrder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired(required = false)
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private AsyncOrder asyncOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired(required = false)
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 支付失败 未支付 超时...
     *
     * @param username 用户名
     */
    @Override
    public void deleteSecKillOrder(String username) {

        //  用户订单信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //  用户下单排队信息
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        if (seckillOrder != null && seckillStatus != null) {
            //  支付失败-支付超时-删除订单
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //  查询此用户为未成功购买的商品
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).get(seckillStatus.getGoodsId());
            if (seckillGoods != null) { //  不为空 回滚库存信息
                //  回滚商品
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);   //  库存回滚 1
                //  重新压入商品信息
                redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).put(seckillStatus.getGoodsId(), seckillGoods);
                //  回滚商品库存计量
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);


            } else {    //  说明刚刚此用户购买的是最后一件(因为如果是最后一件时redis已经删除改商品信息了)(此时redis中午此商品信息)
                SeckillGoods seckillGoods1 = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
                //  重新压入缓存
                redisTemplate.boundHashOps("SeckillGoods_" + seckillStatus.getTime()).put(seckillStatus.getGoodsId(), seckillGoods1);
                //  商品库存回滚 1
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillStatus.getGoodsId(), 1);
            }

            //  删除用户排队信息
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);
            //  删除用户重复下单信息
            redisTemplate.boundHashOps("UserQueueCount_" + username).delete(seckillStatus.getGoodsId());
        }

    }

    /**
     * 支付成功将订单信息保存到数据库中
     *
     * @param username       用户名
     * @param out_trade_no   订单号
     * @param transaction_id 流水号
     * @param time_end       支付时间
     */
    @Override
    public void updateSecKillOrder(String username, String out_trade_no, String transaction_id, String time_end) {

        try {
            //  获取redis中的订单信息
            SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
            //  封装新的数据并保存到mysql中
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            seckillOrder.setPayTime(sdf.parse(time_end));    //  支付时间
            seckillOrder.setTransactionId(transaction_id);      //  订单流水号
            seckillOrder.setStatus("1");
            //  保存到mysql中
            seckillOrderMapper.insertSelective(seckillOrder);
            //  删除此用户在redis中的缓存订单
            redisTemplate.boundHashOps("SeckillOrder").delete(username);
            //  删除此用户下单信息
            redisTemplate.boundHashOps("UserQueueCount_" + username).delete(seckillOrder.getSeckillId());
            //  删除此用户的排队信息
            redisTemplate.boundHashOps("UserQueueStatus").delete(username);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    /**
     * 查询用户订单支付状态
     *
     * @param username 用户名
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {

        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        return seckillStatus;
    }

    /**
     * 秒杀商品下单实现
     *
     * @param key      时间戳
     * @param id       秒杀商品id
     * @param username 用户名
     * @return
     */
    @Override
    public Boolean add(String key, Long id, String username) {

        //  购买商品计数信息
        //  防止重复排队    increment +1
        Long incrementCount = redisTemplate.boundHashOps("UserQueueCount_" + username).increment(id, 1);
        if (incrementCount > 1) {
            throw new RuntimeException("同一秒杀商品只能购买一次!");
        }
        //  封装用户下单信息并保存到redis的队列中     用户名  创建时间   秒杀状态1(排队中) 商品id   秒杀时间段
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, key);
        //  左推
        redisTemplate.boundListOps("secKillOrderQueue").leftPush(seckillStatus);
        //  将用户下单具体信息保存到redis中(供查询)
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
        //  异步生成订单信息
        Future<Boolean> future = asyncOrder.createOrder();
        try {
            Boolean aBoolean = future.get();
            return aBoolean;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Test
    public void testDemo01() throws Exception {

    }

    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder) {
        Example example = new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                criteria.andEqualTo("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                criteria.andEqualTo("seckillId", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                criteria.andEqualTo("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                criteria.andEqualTo("userId", seckillOrder.getUserId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                criteria.andEqualTo("createTime", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                criteria.andEqualTo("payTime", seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                criteria.andEqualTo("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                criteria.andEqualTo("receiverAddress", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                criteria.andEqualTo("receiverMobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                criteria.andEqualTo("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                criteria.andEqualTo("transactionId", seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public Boolean add(SeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
        return null;
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }


}
