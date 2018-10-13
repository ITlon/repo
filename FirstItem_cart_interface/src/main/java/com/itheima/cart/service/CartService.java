package com.itheima.cart.service;

import com.itheima.pojoGroup.Cart;

import java.util.List;

/**
 * @author JAVA
 * @create 2018-10-03 17:06
 */
public interface CartService {
    /**
     * 添加商品到购物车列表
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );

    /**
     * 保存购物车到redis中
     * @param username
     */
    public void saveCartToRedis(String username,List<Cart> cartList);

    /**
     * 合并购物车
     * @param cartList_redis
     * @param cartList_cookie
     * @return
     */
    public List<Cart> mergeCartList(List<Cart>cartList_redis,List<Cart>cartList_cookie);


    public List<Cart> findCartByRedis(String username);
}

