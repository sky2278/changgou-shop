package com.changgou.listener;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.changgou.content.feign.ContentFeign;
import com.changgou.content.pojo.Content;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

@CanalEventListener
public class CanalContentDataListener {

    @Autowired(required = false)
    private ContentFeign contentFeign;

    @Autowired
    private StringRedisTemplate redisTemplate;


    /*
        监控实例名称(canal实例名称)
        监控数据库名称
        监控的表名
        监控的事件
     */
    @ListenPoint(destination = "example", schema = {"changgou_content"}, table = {"tb_content"},
            eventType = {CanalEntry.EventType.INSERT, CanalEntry.EventType.UPDATE})
    public void onEventContent(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {

        //  根据变化的数据获取categoryId
        List<CanalEntry.Column> columnList = rowData.getAfterColumnsList();
        String categoryIdStr = getCategoryId(columnList, "category_id");

        //  通过feign获取最新数据
        long categoryId = Long.parseLong(categoryIdStr);
        Result<List<Content>> result = contentFeign.findContentListByCategoryId(categoryId);
        List<Content> list = result.getData();

        //  同步至redis中
        redisTemplate.boundValueOps("content_" + categoryId).set(JSON.toJSONString(list));


    }

    //  获取categoryID
    private String getCategoryId(List<CanalEntry.Column> columnList, String columnName) {
        for (CanalEntry.Column column : columnList) {
            String name = column.getName();
            if (columnName.equals(name)) {
                //  获取id
                String value = column.getValue();
                return value;
            }
        }
        return null;
    }


}
