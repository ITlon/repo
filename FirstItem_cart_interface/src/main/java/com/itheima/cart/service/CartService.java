package com.itheima.cart.service;

import com.itheima.pojoGroup.Cart;

import java.util.List;

/**
 * @author JAVA
 * @create 2018-10-03 17:06
 */
public interface CartService {

    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num );
}

