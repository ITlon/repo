package com.itheima.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-09-15 22:39
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map loginName(){
        String name= SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName",name);
        return map;
    }
}
