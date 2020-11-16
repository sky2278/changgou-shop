package com.changgou;

import com.changgou.util.FastDFSClient;
import org.apache.commons.io.IOUtils;
import org.csource.fastdfs.FileInfo;
import org.csource.fastdfs.ServerInfo;
import org.csource.fastdfs.StorageServer;
import org.junit.Test;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

public class FastDFSTest {

    //      文件下载
    @Test
    public void downloadFile() throws Exception {

        String group = "group1";
        String remoteFileName = "M00/00/00/wKjThF8ab7yACeeuAACWDfzXGYg643.jpg";
        byte[] bytes = FastDFSClient.download(group, remoteFileName);
        //  文件下载
        IOUtils.write(bytes, new FileOutputStream("d:\\2.jpg"));

    }

    //      文件删除
    @Test
    public void delFile() throws Exception {

        String group = "group1";
        String remoteFileName = "M00/00/00/wKjThF8ab7yACeeuAACWDfzXGYg643.jpg";
        FastDFSClient.delete(group, remoteFileName);

    }

    //      获取文件信息
    @Test
    public void getFileInfo() throws Exception {

        String group = "group1";
        String remoteFileName = "M00/00/00/wKjThF8ab7yACeeuAACWDfzXGYg643.jpg";
        FileInfo fileInfo = FastDFSClient.getFileInfo(group, remoteFileName);
        //  文件信息
        System.out.println("文件大小:" + fileInfo.getFileSize());
        System.out.println("文件创建时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(fileInfo.getCreateTimestamp()));
        System.out.println("文件所在服务器地址:" + fileInfo.getSourceIpAddr());

    }

    //      获取服务器存储信息
    @Test
    public void getStorageInfo() throws Exception {
        String group = "group1";
        StorageServer storageInfo = FastDFSClient.getStorageInfo(group);

        //  服务器地址
        String ip = storageInfo.getInetSocketAddress().getAddress().getHostAddress();
        //  服务器端口
        int port = storageInfo.getInetSocketAddress().getPort();
        //  服务器角标
        int index = storageInfo.getStorePathIndex();
        System.out.println("ip:" + ip + "---" + "port:" + port + "---" + "index:" + index);
    }

    //      获取集群下的服务器存储信息
    @Test
    public void getStorageInfoS() throws Exception {
        String group = "group1";
        String remoteFileName = "M00/00/00/wKjThF8agJGAHrO5AACWDfzXGYg023.jpg";
        ServerInfo[] storageInfoS = FastDFSClient.getStorageInfoS(group, remoteFileName);
        ServerInfo storageInfo = storageInfoS[0];
        String ip = storageInfo.getIpAddr();
        int port = storageInfo.getPort();
        System.out.println("ip:" + ip + "---" + "port:" + port);

    }
}
