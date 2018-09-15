package com.itheima.manager.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-09-15 15:48
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/login")
public class LoginController {
    @RequestMapping("/name")
    public Map loginName() {
        String loginName = SecurityContextHolder.getContext().getAuthentication().getName();
        Map map = new HashMap();
        map.put("loginName", loginName);
        return map;
    }
}
