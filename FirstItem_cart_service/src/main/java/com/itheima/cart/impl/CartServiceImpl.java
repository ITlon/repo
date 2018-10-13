package com.itheima.cart.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.cart.service.CartService;
import com.itheima.mapper.TbItemMapper;
import com.itheima.pojo.TbItem;
import com.itheima.pojo.TbOrderItem;
import com.itheima.pojoGroup.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

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

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {
        //1.根据商品 SKU ID 查询 SKU 商品信息
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态不合法");
        }
        //2.获取商家 ID
        String sellerId = item.getSellerId();
        //3.根据商家 ID 判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);
        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {
            //4.1 新建购物车对象
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
       List<TbOrderItem> orderItemList = new ArrayList();
            TbOrderItem orderItem = createOrderItem(item, num);
           
            orderItemList.add(orderItem);
            cart.setOrderItemList(orderItemList);
            //4.2 将购物车对象添加到购物车列表
            cartList.add(cart);
        } else {
            //5.如果购物车列表中存在该商家的购物车
            // 查询购物车明细列表中是否存在该商品
            List<TbOrderItem> orderItemList = cart.getOrderItemList();
            TbOrderItem orderItem = searchOrderItemByItemId(orderItemList, itemId);
            if (orderItem == null) {
                //5.1. 如果没有，新增购物车明细
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果有，在原购物车更该数量
                orderItem.setNum(orderItem.getNum() + num);
                //更该金额
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));
                //如果数量操作后小于等于 0，则移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细
                }
                //如果移除后 cart 的明细数量为 0，则将 cart 移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    @Override
    public void saveCartToRedis(String username,List<Cart> cartList) {
        redisTemplate.boundHashOps("cartList").put(username,cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartListR, List<Cart> cartListC) {
        for (Cart cart : cartListC) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                cartListR = addGoodsToCartList(cartListR, orderItem.getItemId(), orderItem.getNum());
            }
        }

        return cartListR;
    }

    @Override
    public List<Cart> findCartByRedis(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList==null){
            cartList= new ArrayList<>();
        }
        return cartList;
    }

    //通过商家id在购物车列表中查找该商家的购物车
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
			if(cart.getSellerId().equals(sellerId)){
                return cart;
            }
        }
        return null;
    }

    //通过商品id在购物车明细列表中查找该商品
    private TbOrderItem searchOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        for (TbOrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    //构建购物车明细列表
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        if (num<=0){
            throw new RuntimeException("数量非法");
        }
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setNum(num);
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setPicPath(item.getImage());
        orderItem.setItemId(item.getId());
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTotalFee(new BigDecimal(item.getPrice().doubleValue() * num));
        return orderItem;
    }
}