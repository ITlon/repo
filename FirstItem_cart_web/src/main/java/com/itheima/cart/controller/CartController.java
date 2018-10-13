package com.itheima.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.itheima.cart.service.CartService;
import com.itheima.pojo.TbAddress;
import com.itheima.pojoGroup.Cart;
import com.itheima.user.service.AddressService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import util.CookieUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author JAVA
 * @create 2018-10-03 22:14
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference(timeout=6000)
    private CartService cartService;
    @Reference(timeout=6000)
    private AddressService addressService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartList")
    public List<Cart> findCartList(){
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString==null||cartListString==""){
            //如果取出的购物车为空
            cartListString="[]";
        }
        List<Cart> cartList_cookie = JSON.parseArray(cartListString, Cart.class);
        //获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (username.equals("anonymousUser")){
            //未登录 从cookie中取购物车
             return   cartList_cookie;
        }else {
            //已登录
            List<Cart> cartList_redis = cartService.findCartByRedis(username);
             if (cartList_cookie.size()>0){
                 //合并cookie和redis中的购物车
                 cartList_redis=cartService.mergeCartList(cartList_redis,cartList_cookie);
                 //再次存入redis
                 cartService.saveCartToRedis(username,cartList_redis);
                 //清空cookie中的购物车
                 CookieUtil.deleteCookie(request,response,"cartList");
             }

            return cartList_redis;
        }

    }

    @RequestMapping("/addGoodsToCart")
    public Result addGoodsToCart(Integer num,Long itemId){
        //容许跨域请求
        response.setHeader("Access-Control-Allow-Origin", "http://localhost:9094");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        try {
            //取购物车列表
            List<Cart> cartList = findCartList();
            //获取当前用户名
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            //判断用户是否是登录的用户
            //调用业务层
            cartList = cartService.addGoodsToCartList(cartList, itemId, num);
            if (username.equals("anonymousUser")){
                //未登录 保存到cookie中
                //转换成json
                String cartListString = JSON.toJSONString(cartList);
                //存入cookie中
                CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"UTF-8");
            }else {
                //已登录
                cartService.saveCartToRedis(username,cartList);
            }
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加失败");
        }
    }
    @RequestMapping("/findAddressList")
    public List<TbAddress> findAddressList(){
        String username= SecurityContextHolder.getContext().getAuthentication().getName();
        List<TbAddress> addressList= addressService.findAddressList(username);
       return addressList;
    }

}
