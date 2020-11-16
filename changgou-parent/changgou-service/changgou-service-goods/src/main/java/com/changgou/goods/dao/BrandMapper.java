package com.changgou.goods.dao;

import com.changgou.goods.pojo.Brand;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:Brand的Dao
 * @Date 2019/6/14 0:12
 *****/
public interface BrandMapper extends Mapper<Brand> {

    /**
     *  通过分类获取品牌列表
     * @param categoryId
     * @return
     */
    @Select("SELECT tb.* FROM tb_brand tb ,tb_category_brand tcb WHERE  tcb.category_id = #{categoryId} AND tb.id=tcb.brand_id")
    List<Brand> findBrandsByCategoryId(Integer categoryId);
}
