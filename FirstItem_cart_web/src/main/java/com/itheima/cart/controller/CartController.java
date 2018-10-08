package com.itheima.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.itheima.cart.service.CartService;
import com.itheima.pojoGroup.Cart;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Reference
    private CartService cartService;
    @Autowired
    private HttpServletRequest request;
    @Autowired
    private HttpServletResponse response;

    @RequestMapping("/findCartToCookie")
    public List<Cart> findCartToCookie(){
        String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
        if (cartListString==null||cartListString==""){
            //如果取出的购物车为空
            cartListString="[]";
        }
        List<Cart> cartList = JSON.parseArray(cartListString, Cart.class);
        return  cartList;
    }
    @RequestMapping("/addGoodsIdToCart")
    public Result addGoodsIdToCart(Integer num,Long ItemId){
        try {
            //cookie中取购物车集合
            List<Cart> cartList = findCartToCookie();
            //调用业务层
            cartList = cartService.addGoodsToCartList(cartList, ItemId, num);
            //转换成json
            String cartListString = JSON.toJSONString(cartList);
            //存入cookie中
            CookieUtil.setCookie(request,response,"cartList",cartListString,3600*24,"UTF-8");
            return new Result(true,"添加成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(true,"添加失败");
        }

    }
}
