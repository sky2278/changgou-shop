package com.changgou.client.controller;

import com.changgou.search.feign.SkuInfoFeign;
import com.changgou.search.pojo.SkuInfo;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/search")
public class SearchController {

    @Autowired(required = false)
    private SkuInfoFeign skuInfoFeign;

    @RequestMapping("/list")
    public String list(Model model, @RequestParam(required = false) Map<String, String> searchMap) {

        Map<String, Object> resultMap = skuInfoFeign.search(searchMap);

        //  回显搜索条件
        model.addAttribute("searchMap", searchMap);
        //  回显结果集信息
        model.addAttribute("resultMap", resultMap);

        //  组装url地址条件
        String url = getUrl(searchMap);
        model.addAttribute("url", url);

        //  封装分页信息参数
        //  总条数
        long pageNum = Long.parseLong(resultMap.get("totalElements").toString());
        //  当前页
        int currentPage = Integer.parseInt(resultMap.get("pageNum").toString());
        //  每页大小
        int pageSize = Integer.parseInt(resultMap.get("pageSize").toString());
        Page<SkuInfo> page = new Page<>(pageNum, currentPage, pageSize);
        model.addAttribute("page", page);

        return "search";
    }

    //  拼接地址条件
    private String getUrl(Map<String, String> searchMap) {

        String url = "/search/list";

        if (searchMap != null) {
            //https://search.jd.com/search   ?  keyword=%E6%89%8B%E6%9C%BA&suggest=1.his.0.0
            //              &  wq=%E6%89%8B%E6%9C%BA&ev=exbrand_%E5%8D%8E%E4%B8%BA%EF%BC%88HUAWEI%EF%BC%89%5E
            //  后面拼接条件
            url += "?";
            Set<Map.Entry<String, String>> entries = searchMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                String key = entry.getKey();
                String value = entry.getValue();
                //  如已拼接当前页则无需再拼接当前页
                if (key.equals("pageNum")) {
                    continue;
                }
                url += key + "=" + value + "&";
            }
            //  最后一个 切割 &
            url = url.substring(0, url.length() - 1);
            return url;
        }

        return null;
    }

}
