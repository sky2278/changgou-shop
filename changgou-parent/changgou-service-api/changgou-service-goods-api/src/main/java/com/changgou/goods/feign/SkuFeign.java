package com.changgou.goods.feign;

import com.changgou.goods.pojo.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "goods")
@RequestMapping("/sku")
public interface SkuFeign {

    /**
     * 扣减库存信息   (根据用户名从redis中查询扣减)
     * @param username
     * @return
     */
    @RequestMapping("/decrease/{username}")
    Result decrease(@PathVariable(value = "username") String username);


    /**
     * 根据id查询sku   (添加购物车)
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Sku> findById(@PathVariable(value = "id") Long id);


    /**
     * 根据 spuId查询List<sku>
     *
     * @param spuId
     * @return
     */
    @GetMapping("/findSkuBySpuId/{spuId}")
    List<Sku> findSkuBySpuId(@PathVariable(value = "spuId") Long spuId);

    //  将正常状态下的库存信息保存到索引库中
    @GetMapping("/findSkusByStatus/{status}")
    Result<List<Sku>> findSkusByStatus(@PathVariable(value = "status") String status);

}
