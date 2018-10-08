package com.itheima.pojoGroup;

import com.itheima.pojo.TbOrderItem;

import java.io.Serializable;
import java.util.List;

/**
 * @author JAVA
 * @create 2018-10-03 17:04
 */
@SuppressWarnings("all")
public class Cart implements Serializable{
    private String sellerId;//商家 ID
    private String sellerName;//商家名称
    private List<TbOrderItem> orderItemList;

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public List<TbOrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<TbOrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
