package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

//      文件上传工具类
public class FastDFSClient {

    //      初始化FastDFS配置参数
    static {
        String path = "fdfs_client.conf";
        String config_name = new ClassPathResource(path).getPath();
        try {
            ClientGlobal.init(config_name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //      文件上传
    public static String[] uploadFile(FastDFSFile fastDFSFile) {

        try {
            //文件内容
            byte[] content = fastDFSFile.getContent();
            //文件后缀名
            String ext = fastDFSFile.getExt();
            //文件其他信息
            String author = fastDFSFile.getAuthor();
            NameValuePair[] meta_list = new NameValuePair[1];
            meta_list[0] = new NameValuePair(fastDFSFile.getAuthor());
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            //3、创建存储服务器客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //  文件上传
            String[] uploadResult = storageClient.upload_file(content, ext, meta_list);
            return uploadResult;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //      获取tracker服务器地址
    public static String getTrackerUrl() {
        try {
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            //  ip
            String ip = trackerServer.getInetSocketAddress().getAddress().getHostName();
            //  port
            int port = ClientGlobal.getG_tracker_http_port();
            String url = "http://" + ip + ":" + port;
            return url;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //      文件下载
    public static byte[] download(String groupName, String remoteFileName) {
        try {
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            //3.创建存储服务器客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //  文件下载
            byte[] bytes = storageClient.download_file(groupName, remoteFileName);
            return bytes;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //      文件删除
    public static void delete(String groupName, String remoteFileName) {
        try {
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            //3.创建存储服务器客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //  文件删除
            storageClient.delete_file(groupName, remoteFileName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //      获取文件信息
    public static FileInfo getFileInfo(String groupName, String remoteFileName) {
        try {
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();
            //3.创建存储服务器客户端
            StorageClient storageClient = new StorageClient(trackerServer, null);
            //  获取文件信息
            FileInfo file_info = storageClient.get_file_info(groupName, remoteFileName);
            return file_info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //      获取服务器存储信息
    public static StorageServer getStorageInfo(String groupName) {
        try {
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();

            //3.创建存储服务器客户端
            StorageServer storeStorage = trackerClient.getStoreStorage(trackerServer, groupName);
            return storeStorage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //      获取集群下的服务器存储信息
    public static ServerInfo[] getStorageInfoS(String groupName, String remoteFileName){
        try {
            //1.创建跟踪服务器客户端
            TrackerClient trackerClient = new TrackerClient();
            //2.获取跟踪服务器
            TrackerServer trackerServer = trackerClient.getConnection();

            //3.获取集群下的服务器存储信息
            ServerInfo[] fetchStorage = trackerClient.getFetchStorages(trackerServer, groupName, remoteFileName);
            return fetchStorage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
