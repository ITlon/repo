package com.itheima.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.*;
import com.itheima.pojo.*;
import com.itheima.pojo.TbGoodsExample.Criteria;
import com.itheima.pojoGroup.Goods;
import com.itheima.service.GoodsService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 服务实现层
 *
 * @author Administrator
 */
 
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private TbBrandMapper brandMapper;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbSellerMapper sellerMapper;
    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {
        //设置商品状态为未审核
        goods.getGoods().setAuditStatus("0");
        //插入商品
        goodsMapper.insert(goods.getGoods());
        //将插入的商品id设置到商品扩展表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        //插入扩展表信息
        goodsDescMapper.insert(goods.getGoodsDesc());
        saveItemList(goods);

    }
     private  void saveItemList(Goods goods){
         List<TbItem> items = goods.getItemList();
         if ("1".equals(goods.getGoods().getIsEnableSpec())) {
             for (TbItem item : items) {
                 //构建标题  SPU名称+ 规格选项值
                 String title = goods.getGoods().getGoodsName();
                 Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                 Set<String> keys = specMap.keySet();
                 for (String key : keys) {
                     title += " " + specMap.get(key);
                 }
                 //标题
                 item.setTitle(title);
                 setItemValues(item, goods);
                 itemMapper.insert(item);
             }
         } else {
             TbItem item = new TbItem();
             item.setTitle(goods.getGoods().getGoodsName());//标题
             item.setPrice(goods.getGoods().getPrice());//价格
             item.setNum(9999);//库存量
             item.setStatus("1");//状态
             item.setIsDefault("1");//默认
             item.setSpec("{}");//
             setItemValues(item, goods);
             itemMapper.insert(item);
         }
     }

    private void setItemValues(TbItem item, Goods goods) {
        //商品spu编号
        item.setGoodsId(goods.getGoods().getId());
        //商家编号
        item.setSellerId(goods.getGoods().getSellerId());
        //商品分类id
        item.setCategoryid(goods.getGoods().getCategory3Id());
        //创建日期
        item.setCreateTime(new Date());
        //修改日期
        item.setUpdateTime(new Date());
        //品牌名称
        TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
        //分类名称
        TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
        //商家名称
        TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {

        goodsMapper.updateByPrimaryKey(goods.getGoods());
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);
        TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(goodsDesc);
        TbItemExample example = new TbItemExample();
        example.createCriteria().andGoodsIdEqualTo(id);
        List<TbItem> items = itemMapper.selectByExample(example);
        goods.setItemList(items);
        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //逻辑删除
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //设置逻辑删除值
            goods.setIsDelete("1");
            //保存到数据库中
            goodsMapper.updateByPrimaryKey(goods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        Criteria criteria = example.createCriteria();
        criteria.andIsDeleteIsNull();//非逻辑删除状态
        if (goods != null) {
            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {
                criteria.andSellerIdEqualTo(goods.getSellerId());
                /*criteria.andSellerIdLike("%"+goods.getSellerId()+"%");*/
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {
            //根据id查询出对一个的goods
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);
            //修改状态
            goods.setAuditStatus(status);
            //重新更新到数据库
            goodsMapper.updateByPrimaryKey(goods);
        }
    }

}
