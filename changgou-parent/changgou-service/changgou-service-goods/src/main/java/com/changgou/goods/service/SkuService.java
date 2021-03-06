package com.changgou.goods.service;

import com.changgou.goods.pojo.Sku;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:Sku业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SkuService {

    /***
     *  将正常状态下的库存信息保存到索引库中
     * @param status
     * @return
     */
    List<Sku> findSkusByStatus(String status);

    /***
     * Sku多条件分页查询
     * @param sku
     * @param page
     * @param size
     * @return
     */
    PageInfo<Sku> findPage(Sku sku, int page, int size);

    /***
     * Sku分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Sku> findPage(int page, int size);

    /***
     * Sku多条件搜索方法
     * @param sku
     * @return
     */
    List<Sku> findList(Sku sku);

    /***
     * 删除Sku
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Sku数据
     * @param sku
     */
    void update(Sku sku);

    /***
     * 新增Sku
     * @param sku
     */
    void add(Sku sku);

    /**
     * 根据ID查询Sku
     * @param id
     * @return
     */
     Sku findById(Long id);

    /***
     * 查询所有Sku
     * @return
     */
    List<Sku> findAll();

    /**
     *  根据 spuId查询List<sku>
     * @param spuId
     * @return
     */
    List<Sku> findSkuBySpuId(Long spuId);

    /**
     * 扣减库存信息   (根据用户名从redis中查询扣减)
     * @param username
     * @return
     */
    void decrease(String username);
}
