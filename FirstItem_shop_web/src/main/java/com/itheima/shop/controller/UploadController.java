package com.itheima.shop.controller;

import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;

/**
 * @author JAVA
 * @create 2018-09-17 18:17
 */
@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}")
    private String file_service_url;
    @RequestMapping("/upload")
     public Result upload(MultipartFile file){
         //获取文件全名
        String originalFilename= file.getOriginalFilename();
         // 截取获得后缀名
        String extName= originalFilename .substring(originalFilename.lastIndexOf(".")+1);
         try {
             // /获取上传对象
             FastDFSClient client  =new FastDFSClient("classpath:config/fdfs_client.conf");
             String path = client.uploadFile(file.getBytes(), extName);
             String url=file_service_url+path;
             return new Result(true,url);
         } catch (Exception e) {
             e.printStackTrace();
             return new Result(false,"上传失败");
         }
     }
}
