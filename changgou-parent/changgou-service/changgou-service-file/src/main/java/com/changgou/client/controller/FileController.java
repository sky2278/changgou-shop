package com.changgou.client.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FastDFSClient;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileController {

    //      文件上传
    @PostMapping("/upload")
    public String upload(MultipartFile file) throws IOException {

        //  封装文件信息
        String name = file.getOriginalFilename();       //  文件名
        byte[] content = file.getBytes();              //   文件内容
        String ext = FilenameUtils.getExtension(name); //   获取文件扩展名 (切记)

        FastDFSFile fastDFSFile = new FastDFSFile(name, content, ext);

        //  文件上传
        //      /group1/M00/00/00......jpg
        String[] uploadFile = FastDFSClient.uploadFile(fastDFSFile);
        //  拼接
        String url = FastDFSClient.getTrackerUrl() + "/" + uploadFile[0] + "/" + uploadFile[1];
        return url;

    }




}
