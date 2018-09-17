package com.itheima.pojoGroup;

import com.itheima.pojo.TbGoods;
import com.itheima.pojo.TbGoodsDesc;
import com.itheima.pojo.TbItem;

import java.io.Serializable;
import java.util.List;

/**
 * @author JAVA
 * @create 2018-09-16 18:47
 */
public class Goods implements Serializable {
    private TbGoods goods;
    private TbGoodsDesc goodsDesc;
    private List<TbItem> itemList;

    public TbGoods getGoods() {
        return goods;
    }

    public void setGoods(TbGoods goods) {
        this.goods = goods;
    }

    public TbGoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(TbGoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<TbItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<TbItem> itemList) {
        this.itemList = itemList;
    }
}
