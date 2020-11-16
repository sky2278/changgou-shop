package com.changgou.search.service;

import java.util.Map;

public interface SearchService {

    /**
     *  商品检索
     * @param searchMap
     * @return
     */
    Map<String, Object> search(Map<String, String> searchMap);


    //  将正常状态下的库存信息保存到索引库中
    void importDate();

}
