package com.itheima.content.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.content.service.ContentService;
import com.itheima.pojo.TbContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author JAVA
 * @create 2018-09-21 15:55
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/content")
public class contentController {
    @Reference
    private ContentService contentService;
    @RequestMapping("/findByCategoryId")
    public List<TbContent>findByCategoryId(Long categoryId){
      return contentService.findByCategoryId(categoryId);
    }
}
