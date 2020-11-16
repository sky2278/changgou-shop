package com.changgou.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.CategoryFeign;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.feign.SpuFeign;
import com.changgou.goods.pojo.Category;
import com.changgou.goods.pojo.Sku;
import com.changgou.goods.pojo.Spu;
import com.changgou.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired(required = false)
    private SpuFeign spuFeign;

    @Autowired(required = false)
    private CategoryFeign categoryFeign;

    @Value("${pagepath}")
    private String pagepath;

    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 生成静态页
     *
     * @param id
     */
    @Override
    public void createHtml(Long id) {

        try {
            //  模板名称
            String template = "item";

            //  数据
            Context context = new Context();
            Map<String, Object> map = getDateModel(id);
            context.setVariables(map);

            //  输出流对象
            File dir = new File(pagepath);
            if (!dir.exists()) {
                //  如果不存在则创建该文件夹
                dir.mkdirs();
            }
            File dest = new File(dir, id + ".html");
            PrintWriter writer = new PrintWriter(dest, "UTF-8");

            //              模板名称    数据  输出流
            templateEngine.process(template, context, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    //  获取数据
    private Map<String, Object> getDateModel(Long id) {
        Map<String, Object> map = new HashMap<>();

        //  商品基本信息
        Spu spu = spuFeign.findById(id);
        map.put("spu", spu);

        //  商品分类信息
        Category category1 = categoryFeign.findById(spu.getCategory1Id());
        Category category2 = categoryFeign.findById(spu.getCategory2Id());
        Category category3 = categoryFeign.findById(spu.getCategory3Id());
        map.put("category1", category1);
        map.put("category2", category2);
        map.put("category3", category3);

        //  商品库存信息
        List<Sku> skuList = skuFeign.findSkuBySpuId(id);
        map.put("skuList", skuList);

        //  小图片信息
        //http://img12.360buyimg.com/n7/jfs/t1/64811/3/1181/238169/5cf76eedE4cede79a/9610f77876500f92.jpg,http://img10.360buyimg.com/n7/jfs/t1/42968/36/6334/124909/5cff4240Ebf980183/d7cc572e2e8ee29b.jpg,http://img14.360buyimg.com/n7/jfs/t1/36863/32/10249/283616/5cd53747E5a3c8d3e/6efe62222097d1b1.jpg,http://img13.360buyimg.com/n7/jfs/t1/30233/14/616/141860/5c3ecb1aEd66ede58/ad109c89b72e5db2.jpg
        String[] images = spu.getImages().split(",");
        map.put("imageList", images);

        //  商品规格信息
        //{"电视音响效果":["小影院","环绕","立体声"],"电视屏幕尺寸":["20英寸","60英寸","50英寸"],"尺码":["165","170","175"]}
        String specItems = spu.getSpecItems();
        Map<String, String> specificationList = JSON.parseObject(specItems, Map.class);
        map.put("specificationList", specificationList);

        return map;
    }
}
