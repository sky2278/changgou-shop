package com.changgou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.goods.feign.SkuFeign;
import com.changgou.goods.pojo.Sku;
import com.changgou.search.dao.SkuInfoMapper;
import com.changgou.search.pojo.SkuInfo;
import com.changgou.search.service.SearchService;
import entity.Result;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SearchServiceImpl implements SearchService {

    @Autowired(required = false)
    private SkuInfoMapper skuInfoMapper;

    @Autowired(required = false)
    private SkuFeign skuFeign;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    /**
     * 商品检索
     *
     * @param searchMap
     * @return
     */
    @Override
    public Map<String, Object> search(Map<String, String> searchMap) {
        //  封装检索条件
        NativeSearchQueryBuilder nativeSearchQueryBuilder = basicQueryBuilder(searchMap);
        //   查询商品列表
        Map<String, Object> resultMap = searchSpuPage(nativeSearchQueryBuilder);

        //  =================================================================

        //   查询商品分类列表
        //List<String> categoryList = searchCategoryList(nativeSearchQueryBuilder);
        //resultMap.put("categoryList", categoryList);

        //   查询商品品牌列表
        //List<String> brandList = searchBrandList(nativeSearchQueryBuilder);
        //resultMap.put("brandList", brandList);

        //  查询商品规格列表
        //Map<String, Set<String>> specList = searchSpecList(nativeSearchQueryBuilder);
        //resultMap.put("specList", specList);

        //  封装所有分组查询列表数据
        Map<String, Object> searchGroupListMap = searchGroupList(nativeSearchQueryBuilder);

        //  合并
        resultMap.putAll(searchGroupListMap);

        return resultMap;
    }

    //  封装所有分组查询列表数据
    private Map<String, Object> searchGroupList(NativeSearchQueryBuilder builder) {
        //  条件封装
        builder.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(40000));
        builder.addAggregation(AggregationBuilders.terms("brandList").field("brandName"));
        builder.addAggregation(AggregationBuilders.terms("categoryList").field("categoryName"));

        NativeSearchQuery build = builder.build();
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class);
        Aggregations aggregations = page.getAggregations();

        //  规格分组
        ArrayList<String> specLists = getGroupList(aggregations, "skuSpec");
        Map<String, Set<String>> specList = putAll(specLists);
        //  商品分组
        ArrayList<String> brandList = getGroupList(aggregations, "brandList");
        //  商品分类分组
        ArrayList<String> categoryList = getGroupList(aggregations, "categoryList");

        //  封装
        Map<String, Object> map = new HashMap<>();
        map.put("categoryList", categoryList);
        map.put("brandList", brandList);
        map.put("specList", specList);

        return map;
    }

    //  获取分组数据信息
    private ArrayList<String> getGroupList(Aggregations aggregations, String groupName) {
        StringTerms stringTerms = aggregations.get(groupName);
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        ArrayList<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }

    //  查询商品规格列表
    private Map<String, Set<String>> searchSpecList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        NativeSearchQuery build = nativeSearchQueryBuilder.build();                                 //  设置大小参数
        build.addAggregation(AggregationBuilders.terms("skuSpec").field("spec.keyword").size(40000));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class);
        Aggregations aggregations = page.getAggregations();
        //  断点
        StringTerms stringTerms = aggregations.get("skuSpec");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        ArrayList<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        Map<String, Set<String>> map = putAll(list);
        return map;
    }

    //  去重规格信息
    private Map<String, Set<String>> putAll(ArrayList<String> list) {
        // {"电视音响效果":"小影院","电视屏幕尺寸":"20英寸","尺码":"165"} - map
        // {"电视音响效果":"立体声","电视屏幕尺寸":"20英寸","尺码":"170"}
        // {"手机屏幕尺寸":"5寸","网络":"联通2G","颜色":"红","测试":"测试","机身内存":"16G","存储":"16G","像素":"300万像素"}
        // {"手机屏幕尺寸":"5寸","网络":"联通2G","颜色":"白","测试":"测试","机身内存":"16G","存储":"16G","像素":"300万像素"}
        if (list != null && list.size() > 0) {
            HashMap<String, Set<String>> map = new HashMap<>();
            for (String spec : list) {
                Map<String, String> specMap = JSON.parseObject(spec, Map.class);
                Set<Map.Entry<String, String>> entries = specMap.entrySet();
                for (Map.Entry<String, String> entry : entries) {
                    String key = entry.getKey();
                    String value = entry.getValue();
                    //  封装规格信息到set集合中
                    Set<String> set = map.get(key);
                    if (set == null) {
                        set = new HashSet<>();
                    }
                    set.add(value);     //  电视音响效果
                    map.put(key, set);
                }
            }
            return map;
        }
        return null;
    }

    //   查询商品品牌列表
    private List<String> searchBrandList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        NativeSearchQuery build = nativeSearchQueryBuilder.build();
        //
        build.addAggregation(AggregationBuilders.terms("brandList").field("brandName"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class);

        //  具体断点分析(deBug)
        Aggregations aggregations = page.getAggregations();
        StringTerms stringTerms = aggregations.get("brandList");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        ArrayList<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }

    //  查询商品分类列表
    private List<String> searchCategoryList(NativeSearchQueryBuilder nativeSearchQueryBuilder) {
        //  聚合查询
        NativeSearchQuery build = nativeSearchQueryBuilder.build();
        //  terms("categoryList")   别名
        //  field("categoryName")   es索引库中对应的字段
        build.addAggregation(AggregationBuilders.terms("categoryList").field("categoryName"));
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class);
        //  聚合查询结果集
        Aggregations aggregations = page.getAggregations();
        //  具体断点分析---
        StringTerms stringTerms = aggregations.get("categoryList");
        List<StringTerms.Bucket> buckets = stringTerms.getBuckets();
        ArrayList<String> list = new ArrayList<>();
        for (StringTerms.Bucket bucket : buckets) {
            list.add(bucket.getKeyAsString());
        }
        return list;
    }

    //  查询商品列表方法关键字并高亮显示
    private Map<String, Object> searchSpuPage(NativeSearchQueryBuilder nativeSearchQueryBuilder) {

        HighlightBuilder.Field highlightFields = new HighlightBuilder.Field("name");    // 对名称字段包含的关键字高亮显示
        //  开始标签
        highlightFields.preTags("<font color='red'>");
        //  结束标签
        highlightFields.postTags("</font>");

        nativeSearchQueryBuilder.withHighlightFields(highlightFields);

        //  获取高亮的结果集
        SearchResultMapper searchResultMapper = new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> clazz, Pageable pageable) {
                SearchHits hits = response.getHits();
                ArrayList<T> list = new ArrayList<>();
                for (SearchHit hit : hits) {
                    String json = hit.getSourceAsString();    //普通结果集
                    SkuInfo skuInfo = JSON.parseObject(json, SkuInfo.class);    //没有高亮的结果集
                    HighlightField highlightField = hit.getHighlightFields().get("name");   //  高亮结果集
                    if (highlightField != null) {
                        Text[] fragments = highlightField.getFragments();
                        String highLightName = fragments[0].toString(); //高亮的字段名称
                        //  将高亮字段名称替换
                        skuInfo.setName(highLightName);
                    }
                    list.add((T) skuInfo);
                }
                //  替换结果集
                return new AggregatedPageImpl<>(list, pageable, hits.getTotalHits());
            }
        };

        NativeSearchQuery build = nativeSearchQueryBuilder.build();
        //  封装高亮结果集
        AggregatedPage<SkuInfo> page = elasticsearchTemplate.queryForPage(build, SkuInfo.class, searchResultMapper);

        //  封装各种结果集
        HashMap<String, Object> resultMap = new HashMap<>();
        //  结果集
        List<SkuInfo> skuInfoList = page.getContent();
        //  总条数
        long totalElements = page.getTotalElements();
        //  总页数
        int totalPages = page.getTotalPages();
        //  当前页
        int pageNum = page.getPageable().getPageNumber();
        //  每页大小
        int pageSize = page.getPageable().getPageSize();

        //  封装所有结果集
        resultMap.put("skuInfoList", skuInfoList);
        resultMap.put("totalElements", totalElements);
        resultMap.put("totalPages", totalPages);

        resultMap.put("pageNum", pageNum + 1);  //从0开始
        resultMap.put("pageSize", pageSize);
        return resultMap;
    }

    //  封装检索条件
    private NativeSearchQueryBuilder basicQueryBuilder(Map<String, String> searchMap) {
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        BoolQueryBuilder bool = new BoolQueryBuilder();
        //  封装关键字查询条件
        if (searchMap != null) {
            String keywords = searchMap.get("keywords");
            if (!StringUtils.isEmpty(keywords)) {
                queryBuilder.withQuery(QueryBuilders.matchQuery("name", keywords));
            }

            //  分类信息
            String categoryName = searchMap.get("category");
            if (!StringUtils.isEmpty(categoryName)) {
                bool.must(QueryBuilders.matchQuery("categoryName", categoryName));
            }
            //  品牌信息
            String brandName = searchMap.get("brand");
            if (!StringUtils.isEmpty(brandName)) {
                bool.must(QueryBuilders.matchQuery("brandName", brandName));
            }

            //  规格信息         http://url?spec_内存=32G&spec_颜色=红
            //  获取所有key
            Set<String> keySet = searchMap.keySet();
            for (String key : keySet) {
                if (key.startsWith("spec_")) {
                    bool.must(QueryBuilders.matchQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                }
            }

            //  价格过滤
            String price = searchMap.get("price");
            if (!StringUtils.isEmpty(price)) {
                String[] prices = price.split("-");
                //  大于等于
                bool.must(QueryBuilders.rangeQuery("price").gte(prices[0]));
                if (prices.length == 2) {
                    //  小于等于
                    bool.must(QueryBuilders.rangeQuery("price").lte(prices[1]));
                }
            }

            //  搜索排序
            String field = searchMap.get("sortField");
            String sortRule = searchMap.get("sortRule");
            if (!StringUtils.isEmpty(field) && !StringUtils.isEmpty(sortRule)) {
                queryBuilder.withSort(SortBuilders.fieldSort(field).order(SortOrder.valueOf(sortRule)));
            }


            //  添加过滤条件
            queryBuilder.withFilter(bool);

            //  结果分页
            String pageNum = searchMap.get("pageNum");
            if (StringUtils.isEmpty(pageNum)) {
                pageNum = "1";
            }
            //  当前页 从0开始
            int page = Integer.parseInt(pageNum);
            int size = 40;
            Pageable pageable = PageRequest.of(page - 1, size);
            queryBuilder.withPageable(pageable);

        }
        return queryBuilder;
    }


    //  将正常状态下的库存信息保存到索引库中
    @Override
    public void importDate() {
        Result<List<Sku>> skusByStatus = skuFeign.findSkusByStatus("1");
        List<Sku> skuList = skusByStatus.getData();
        if (skuList != null && skuList.size() > 0) {
            //  转为json
            String text = JSON.toJSONString(skuList);
            //  转为skuInfo
            List<SkuInfo> skuInfoS = JSON.parseArray(text, SkuInfo.class);
            for (SkuInfo skuInfo : skuInfoS) {
                //  取出规格信息
                String spec = skuInfo.getSpec();
                Map<String, Object> map = JSON.parseObject(spec, Map.class);
                //  封装规格参数
                skuInfo.setSpecMap(map);
            }
            //  存入es
            skuInfoMapper.saveAll(skuInfoS);
        }

    }


}
