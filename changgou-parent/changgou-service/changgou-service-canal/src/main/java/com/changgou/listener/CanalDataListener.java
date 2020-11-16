package com.changgou.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.DeleteListenPoint;
import com.xpand.starter.canal.annotation.InsertListenPoint;
import com.xpand.starter.canal.annotation.UpdateListenPoint;

import java.util.List;

@CanalEventListener
public class CanalDataListener {


    /**
     * 监听新增事件
     *
     * @param entryType 数据库的操作事件
     * @param rowData   监控的行数据
     */
    @InsertListenPoint
    public void onEventInsert(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        //  获取新增后的数据
        List<CanalEntry.Column> columnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : columnsList) {
            String name = column.getName();
            String value = column.getValue();
            System.out.println("列名:" + name + "列值:" + value);
        }
    }

    /**
     * 监听更新事件
     *
     * @param entryType 数据库的操作事件
     * @param rowData   监控的行数据
     */
    @UpdateListenPoint
    public void onEventUpdate(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        System.out.println("==============前===================");
        //  获取更新前的数据
        List<CanalEntry.Column> columnsListBefore = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : columnsListBefore) {
            String name = column.getName();
            String value = column.getValue();
            //System.out.println("列名:" + name + "列值:" + value);
            if ("category_id".equals(name)) {
                System.out.println("列名 :" + name + "列值:" + value);
            }
        }

        System.out.println();
        System.out.println("==============后===================");
        System.out.println();

        //  获取更新后的数据
        List<CanalEntry.Column> columnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : columnsList) {
            String name = column.getName();
            String value = column.getValue();
            //System.out.println("列名:" + name + "列值:" + value);
            if ("category_id".equals(name)) {
                System.out.println("列名:" + name + "列值:" + value);
            }
        }
    }

    /**
     * 监听删除事件
     *
     * @param entryType 数据库的操作事件
     * @param rowData   监控的行数据
     */
    @DeleteListenPoint
    public void onEventDelete(CanalEntry.EntryType entryType, CanalEntry.RowData rowData) {
        //  获取删除后的数据
        List<CanalEntry.Column> columnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : columnsList) {
            String name = column.getName();
            String value = column.getValue();
            System.out.println("列名:" + name + "列值:" + value);
        }

    }


}
