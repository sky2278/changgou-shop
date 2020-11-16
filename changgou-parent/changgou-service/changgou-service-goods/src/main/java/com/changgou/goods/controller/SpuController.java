package com.changgou.goods.controller;

import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:传智播客
 * @Description:
 * @Date 2019/6/14 0:18
 *****/

@RestController
@RequestMapping("/spu")
@CrossOrigin
public class SpuController {

    @Autowired
    private SpuService spuService;


    /**
     * 商品的还原
     */
    @PutMapping("/restore/{id}")
    public Result restore(@PathVariable(value = "id") Long id) {
        Spu spu = spuService.findById(id);
        if ("0".equals(spu.getIsDelete())) {
            return new Result(false, StatusCode.ERROR, "此商品未删除无须还原!");
        }
        spuService.restore(id);
        return new Result(true, StatusCode.OK, "还原商品信息成功!");
    }


    /**
     * 商品的逻辑删除
     */
    @DeleteMapping("/logic/delete/{id}")
    public Result logicDelete(@PathVariable(value = "id") Long id) {
        Spu spu = spuService.findById(id);
        //  检查是否下架  (须先下架才能删除)
        if ("1".equals(spu.getIsMarketable())) {
            return new Result(false, StatusCode.ERROR, "须先下架才能逻辑删除商品信息!");
        }
        spuService.logicDelete(id);
        return new Result(true, StatusCode.OK, "逻辑删除商品信息成功!");
    }


    /**
     * 商品的批量下架
     */
    @PutMapping("/sold/out")
    public Result soldOut(@RequestBody(required = false) Long[] ids) {
        if (ids == null) {
            return new Result(false, StatusCode.ERROR, "请至少选择一个商品!");
        }
        int count = spuService.soldOut(ids);
        return new Result(true, StatusCode.OK, "已成功下架" + count + "件商品!");
    }

    /**
     * 商品的批量上架
     */
    @PutMapping("/put/many")
    public Result putMany(@RequestBody(required = false) Long[] ids) {
        if (ids == null) {
            return new Result(false, StatusCode.ERROR, "请至少选择一个商品!");
        }
        int count = spuService.putMany(ids);
        return new Result(true, StatusCode.OK, "已成功上架" + count + "件商品!");
    }


    /**
     * 商品的上架/下架
     */
    @GetMapping("/isShow/{id}/{isMarketable}")
    public Result isShow(@PathVariable(value = "id") Long id, @PathVariable(value = "isMarketable") String isMarketable) {
        spuService.isShow(id, isMarketable);
        return new Result(true, StatusCode.OK, "商品的上架/下架操作成功!");
    }

    /**
     * 商品的审核
     */
    @GetMapping("/audit/{id}/{status}")
    public Result audit(@PathVariable(value = "id") Long id, @PathVariable(value = "status") String status) {
        spuService.audit(id, status);
        return new Result(true, StatusCode.OK, "商品的审计操作成功!");
    }


    /**
     * 编辑商品数据
     */
    @GetMapping("/findGoodsById/{id}")
    public Result<Goods> findGoodsById(@PathVariable(value = "id") Long id) {
        Goods goods = spuService.findGoodsById(id);
        return new Result<>(true, StatusCode.OK, "编辑商品数据!", goods);
    }


    /**
     * 商品更新
     */
    @PostMapping("/update")
    public Result update(@RequestBody Goods goods) {
        spuService.updateGoods(goods);
        return new Result(true, StatusCode.OK, "商品更新成功!");
    }

    /**
     * 商品保存
     */
    @PostMapping("/save")
    public Result save(@RequestBody Goods goods) {
        spuService.save(goods);
        return new Result(true, StatusCode.OK, "商品保存成功!");
    }


    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@RequestBody(required = false) Spu spu, @PathVariable int page, @PathVariable int size) {
        //调用SpuService实现分页条件查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(spu, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * Spu分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageInfo> findPage(@PathVariable int page, @PathVariable int size) {
        //调用SpuService实现分页查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(page, size);
        return new Result<PageInfo>(true, StatusCode.OK, "查询成功", pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param spu
     * @return
     */
    @PostMapping(value = "/search")
    public Result<List<Spu>> findList(@RequestBody(required = false) Spu spu) {
        //调用SpuService实现条件查询Spu
        List<Spu> list = spuService.findList(spu);
        return new Result<List<Spu>>(true, StatusCode.OK, "查询成功", list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable Long id) {
        Spu spu = spuService.findById(id);
        if (!"1".equals(spu.getIsDelete())) {
            return new Result(false, StatusCode.ERROR, "须先逻辑删除才能物理删除商品信息!");
        }
        //调用SpuService实现根据主键删除
        spuService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 修改Spu数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Spu spu, @PathVariable Long id) {
        //设置主键值
        spu.setId(id);
        //调用SpuService实现修改Spu
        spuService.update(spu);
        return new Result(true, StatusCode.OK, "修改成功");
    }

    /***
     * 新增Spu数据
     * @param spu
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Spu spu) {
        //调用SpuService实现添加Spu
        spuService.add(spu);
        return new Result(true, StatusCode.OK, "添加成功");
    }

    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Spu findById(@PathVariable(value = "id") Long id) {
        //调用SpuService实现根据主键查询Spu
        Spu spu = spuService.findById(id);
        return spu;
    }

    /***
     * 查询Spu全部数据
     * @return
     */
    @GetMapping
    public Result<List<Spu>> findAll() {
        //调用SpuService实现查询所有Spu
        List<Spu> list = spuService.findAll();
        return new Result<List<Spu>>(true, StatusCode.OK, "查询成功", list);
    }
}
