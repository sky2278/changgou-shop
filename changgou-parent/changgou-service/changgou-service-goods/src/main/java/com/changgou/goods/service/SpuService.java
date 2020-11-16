package com.changgou.goods.service;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:Spu业务层接口
 * @Date 2019/6/14 0:16
 *****/
public interface SpuService {


    /**
     * 商品的还原
     */
    void restore(Long id);

    /**
     * 商品的逻辑删除
     */
    void logicDelete(Long id);


    /**
     * 商品的批量下架
     */
    int soldOut(Long[] ids);

    /**
     * 商品的批量上架
     */
    int putMany(Long[] ids);

    /**
     * 商品的上架/下架
     */
    void isShow(Long id, String isMarketable);


    /**
     * 商品的审核
     */
    void audit(Long id, String status);

    /**
     * 编辑商品数据
     */
    Goods findGoodsById(Long id);

    /**
     * 商品更新
     */
    void updateGoods(Goods goods);

    /**
     * 商品保存
     */
    void save(Goods goods);


    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     *
     * @param id
     * @return
     */
    Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}
