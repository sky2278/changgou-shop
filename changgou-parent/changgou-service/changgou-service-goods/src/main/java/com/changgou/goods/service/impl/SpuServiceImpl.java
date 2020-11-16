package com.changgou.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.dao.BrandMapper;
import com.changgou.goods.dao.CategoryMapper;
import com.changgou.goods.dao.SkuMapper;
import com.changgou.goods.dao.SpuMapper;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

/****
 * @Author:传智播客
 * @Description:Spu业务层接口实现类
 * @Date 2019/6/14 0:16
 *****/
@Service
public class SpuServiceImpl implements SpuService {

    @Autowired(required = false)
    private SpuMapper spuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired(required = false)
    private SkuMapper skuMapper;

    @Autowired(required = false)
    private CategoryMapper categoryMapper;

    @Autowired(required = false)
    private BrandMapper brandMapper;


    /**
     * 商品的还原
     */
    @Override
    public void restore(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //  更新状态
        spu.setIsDelete("0");
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品的逻辑删除
     */
    @Override
    public void logicDelete(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //  逻辑删除
        spu.setIsDelete("1");
        //  审核状态
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品的批量下架
     */
    @Override
    public int soldOut(Long[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("0");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //  封装条件
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable", "1");
        criteria.andEqualTo("isDelete", "0");
        criteria.andEqualTo("status", "1");
        int i = spuMapper.updateByExampleSelective(spu, example);
        return i;
    }

    /**
     * 商品的批量上架
     */
    @Override
    public int putMany(Long[] ids) {
        /*
            update `tb_spu` set is_marketable='1' where id in (1287389208431955968,1287382081986498560)
            and is_delete = '0' and `status`='1' and `is_marketable`=`0`
         */

        Spu spu = new Spu();
        spu.setIsMarketable("1");
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //  封装条件
        criteria.andIn("id", Arrays.asList(ids));
        criteria.andEqualTo("isMarketable", "0");
        criteria.andEqualTo("isDelete", "0");
        criteria.andEqualTo("status", "1");
        //  影响行数    (批量上架商品数)
        int i = spuMapper.updateByExampleSelective(spu, example);
        return i;

    }

    /**
     * 商品的上架/下架
     */
    @Override
    public void isShow(Long id, String isMarketable) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable(isMarketable);
        spuMapper.updateByPrimaryKeySelective(spu);

        //  已上架
        if ("1".equals(spu.getIsMarketable())) {
            //  TODO    将商品信息保存到es中

            //  TODO    生成静态页面

        } else {
            //  下架
            //  TODO    将商品信息从es中删除

            //  TODO    删除此商品信息的静态页面信息【可选】
        }
    }

    /**
     * 商品的审核
     */
    @Override
    public void audit(Long id, String status) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 编辑商品数据
     */
    @Override
    public Goods findGoodsById(Long id) {
        Goods goods = new Goods();
        //  商品信息
        Spu spu = spuMapper.selectByPrimaryKey(id);
        goods.setSpu(spu);
        //  库存信息
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 商品更新
     */
    @Override
    public void updateGoods(Goods goods) {
        Spu spu = goods.getSpu();
        //  保存操作
        if (spu.getId() == null) {
            //  设置主键id    (分布式存储Id)
            long spuId = idWorker.nextId();
            spu.setId(spuId);
            //  未上架
            spu.setIsMarketable("0");
            //  待审核
            spu.setStatus("0");
            //  未删除
            spu.setIsDelete("0");
            spuMapper.insertSelective(spu);
        } else {
            //  更新操作
            spu.setStatus("0");
            spuMapper.updateByPrimaryKeySelective(spu);

            //  删除原有的库存信息
            Sku sku = new Sku();
            sku.setSpuId(spu.getId());
            skuMapper.delete(sku);
        }

        //  保存库存信息
        List<Sku> skuList = goods.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            for (Sku sku : skuList) {
                //  设置主键id    (分布式存储Id)
                long skuId = idWorker.nextId();
                sku.setId(skuId);
                //  设置spuId
                sku.setSpuId(spu.getId());
                //  商品库存名称  商品名称+商品副标题+规格
                String name = spu.getName() + " " + spu.getCaption();
                //  规格(拼凑规格参数信息)
                String spec = sku.getSpec();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                if (map != null) {
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        String value = entry.getValue();
                        name += " " + value;
                    }
                }
                //  保存名称
                sku.setName(name);
                //  创建日期
                sku.setCreateTime(new Date());
                //  更新日期
                sku.setUpdateTime(new Date());
                //  商品id
                sku.setSpuId(spu.getId());
                //  分类id
                sku.setCategoryId(spu.getCategory3Id());
                //  分类名称
                String categoryName = categoryMapper.selectByPrimaryKey(spu.getCategory3Id()).getName();
                sku.setCategoryName(categoryName);
                //  品牌名称
                String brandName = brandMapper.selectByPrimaryKey(spu.getBrandId()).getName();
                sku.setBrandName(brandName);
                //  库存状态
                sku.setStatus("1");
                skuMapper.insertSelective(sku);
            }
        }
    }

    /**
     * 商品保存
     */
    @Override
    public void save(Goods goods) {
        //  保存商品信息
        Spu spu = goods.getSpu();
        //  设置主键id    (分布式存储Id)
        long spuId = idWorker.nextId();
        spu.setId(spuId);
        //  未上架
        spu.setIsMarketable("0");
        //  待审核
        spu.setStatus("0");
        //  未删除
        spu.setIsDelete("0");
        spuMapper.insertSelective(spu);


        //  保存库存信息
        List<Sku> skuList = goods.getSkuList();
        if (skuList != null && skuList.size() > 0) {
            for (Sku sku : skuList) {
                //  设置主键id    (分布式存储Id)
                long skuId = idWorker.nextId();
                sku.setId(skuId);
                //  设置spuId
                sku.setSpuId(spuId);
                //  商品库存名称  商品名称+商品副标题+规格
                String name = spu.getName() + " " + spu.getCaption();
                //  规格(拼凑规格参数信息)
                String spec = sku.getSpec();
                Map<String, String> map = JSON.parseObject(spec, Map.class);
                if (map != null) {
                    Set<Map.Entry<String, String>> entries = map.entrySet();
                    for (Map.Entry<String, String> entry : entries) {
                        String value = entry.getValue();
                        name += "  " + value;
                    }
                }
                //  保存名称
                sku.setName(name);
                //System.out.println(sku.getName());
                //  创建日期
                sku.setCreateTime(new Date());
                //  更新日期
                sku.setUpdateTime(new Date());
                //  商品id
                sku.setSpuId(spu.getId());
                //  分类id
                sku.setCategoryId(spu.getCategory3Id());
                //  分类名称
                String categoryName = categoryMapper.selectByPrimaryKey(spu.getCategory3Id()).getName();
                sku.setCategoryName(categoryName);
                //  品牌名称
                String brandName = brandMapper.selectByPrimaryKey(spu.getBrandId()).getName();
                sku.setBrandName(brandName);
                //  库存状态
                sku.setStatus("1");
                skuMapper.insertSelective(sku);
            }
        }

    }


    /**
     * Spu条件+分页查询
     *
     * @param spu  查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size) {
        //分页
        PageHelper.startPage(page, size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size) {
        //静态分页
        PageHelper.startPage(page, size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     *
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu) {
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     *
     * @param spu
     * @return
     */
    public Example createExample(Spu spu) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (spu != null) {
            // 主键
            if (!StringUtils.isEmpty(spu.getId())) {
                criteria.andEqualTo("id", spu.getId());
            }
            // 货号
            if (!StringUtils.isEmpty(spu.getSn())) {
                criteria.andEqualTo("sn", spu.getSn());
            }
            // SPU名
            if (!StringUtils.isEmpty(spu.getName())) {
                criteria.andLike("name", "%" + spu.getName() + "%");
            }
            // 副标题
            if (!StringUtils.isEmpty(spu.getCaption())) {
                criteria.andEqualTo("caption", spu.getCaption());
            }
            // 品牌ID
            if (!StringUtils.isEmpty(spu.getBrandId())) {
                criteria.andEqualTo("brandId", spu.getBrandId());
            }
            // 一级分类
            if (!StringUtils.isEmpty(spu.getCategory1Id())) {
                criteria.andEqualTo("category1Id", spu.getCategory1Id());
            }
            // 二级分类
            if (!StringUtils.isEmpty(spu.getCategory2Id())) {
                criteria.andEqualTo("category2Id", spu.getCategory2Id());
            }
            // 三级分类
            if (!StringUtils.isEmpty(spu.getCategory3Id())) {
                criteria.andEqualTo("category3Id", spu.getCategory3Id());
            }
            // 模板ID
            if (!StringUtils.isEmpty(spu.getTemplateId())) {
                criteria.andEqualTo("templateId", spu.getTemplateId());
            }
            // 运费模板id
            if (!StringUtils.isEmpty(spu.getFreightId())) {
                criteria.andEqualTo("freightId", spu.getFreightId());
            }
            // 图片
            if (!StringUtils.isEmpty(spu.getImage())) {
                criteria.andEqualTo("image", spu.getImage());
            }
            // 图片列表
            if (!StringUtils.isEmpty(spu.getImages())) {
                criteria.andEqualTo("images", spu.getImages());
            }
            // 售后服务
            if (!StringUtils.isEmpty(spu.getSaleService())) {
                criteria.andEqualTo("saleService", spu.getSaleService());
            }
            // 介绍
            if (!StringUtils.isEmpty(spu.getIntroduction())) {
                criteria.andEqualTo("introduction", spu.getIntroduction());
            }
            // 规格列表
            if (!StringUtils.isEmpty(spu.getSpecItems())) {
                criteria.andEqualTo("specItems", spu.getSpecItems());
            }
            // 参数列表
            if (!StringUtils.isEmpty(spu.getParaItems())) {
                criteria.andEqualTo("paraItems", spu.getParaItems());
            }
            // 销量
            if (!StringUtils.isEmpty(spu.getSaleNum())) {
                criteria.andEqualTo("saleNum", spu.getSaleNum());
            }
            // 评论数
            if (!StringUtils.isEmpty(spu.getCommentNum())) {
                criteria.andEqualTo("commentNum", spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if (!StringUtils.isEmpty(spu.getIsMarketable())) {
                criteria.andEqualTo("isMarketable", spu.getIsMarketable());
            }
            // 是否启用规格
            if (!StringUtils.isEmpty(spu.getIsEnableSpec())) {
                criteria.andEqualTo("isEnableSpec", spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if (!StringUtils.isEmpty(spu.getIsDelete())) {
                criteria.andEqualTo("isDelete", spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if (!StringUtils.isEmpty(spu.getStatus())) {
                criteria.andEqualTo("status", spu.getStatus());
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
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     *
     * @param spu
     */
    @Override
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     *
     * @param spu
     */
    @Override
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     *
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
