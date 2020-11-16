package com.changgou.goods.dao;

import com.changgou.goods.pojo.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:传智播客
 * @Description:Sku的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface SkuMapper extends Mapper<Sku> {

    /**
     * 扣减库存信息   (根据用户名从redis中查询扣减)
     *
     * @param skuId 商品id
     * @param num   购买数量
     * @return
     */
    @Update("UPDATE tb_sku SET num = num -#{num} WHERE id = #{skuId} AND num > #{num}")
    int decrease(@Param("skuId") Long skuId, @Param("num") Integer num);
}
