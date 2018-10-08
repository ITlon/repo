package com.itheima.cart.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.cart.service.CartService;
import com.itheima.mapper.TbItemMapper;
import com.itheima.pojo.TbItem;
import com.itheima.pojo.TbOrderItem;
import com.itheima.pojoGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JAVA
 * @create 2018-10-03 17:09
 */

@Service
public class CartServiceImpl implements CartService {
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }
        //2.获取商家 ID
        String sellerId = item.getSellerId();
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        if (cart == null) {
            //4.如果购物车列表中不存在该商家的购物车
            //4.1 新建购物车对象
            cart = new Cart();
            //4.2 将新建的购物车对象添加到购物车列表
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            List orderItemList = new ArrayList();
            orderItemList.add(createOrderItem(item, num));
            cart.setOrderItemList(orderItemList);
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            TbOrderItem orderItem = searchOrderByItemId(cart.getOrderItemList(), itemId);
            //5.1. 如果没有，新增购物车明细
            if (orderItem == null) {
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);//更该数量
                //更该金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);
                }
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return null;
    }

    //根据商家id查询购物车
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    //根据skuId在购物车明细列表中
    private TbOrderItem searchOrderByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }
}
