package com.itheima.search.controller;

import com.itheima.search.service.SearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-09-22 20:02
 */
@RestController
@RequestMapping("/itemSearch")
public class SearchController {

   @Reference
    private SearchService searchService;

     @RequestMapping("/search")

     public Map search(@RequestBody Map searchMap ){

        return  searchService.search(searchMap);
    }

}
