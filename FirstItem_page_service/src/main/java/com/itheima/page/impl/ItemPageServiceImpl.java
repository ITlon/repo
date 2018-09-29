package com.itheima.page.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.mapper.TbGoodsDescMapper;
import com.itheima.mapper.TbGoodsMapper;
import com.itheima.mapper.TbItemCatMapper;
import com.itheima.mapper.TbItemMapper;
import com.itheima.page.service.ItemPageService;
import com.itheima.pojo.TbGoods;
import com.itheima.pojo.TbGoodsDesc;
import com.itheima.pojo.TbItem;
import com.itheima.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-09-28 16:02
 */
@Service
public class ItemPageServiceImpl implements ItemPageService {
    @Value("${pagedir}")
    private String pagedir;
    @Autowired
    private TbGoodsMapper goodsMapper;
    @Autowired
    private TbGoodsDescMapper goodsDescMapper;
    @Autowired
    private FreeMarkerConfig markerConfig;
    @Autowired
    private TbItemCatMapper itemCatMapper;
    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {
        try {
            //获得模板对象
            Configuration configuration = markerConfig.getConfiguration();
            Template template = configuration.getTemplate("item.ftl");
            //创建数据模型对象
            Map map = new HashMap();
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            TbItemExample example = new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andGoodsIdEqualTo(goodsId);
            criteria.andStatusEqualTo("1");
            example.setOrderByClause("is_default desc");
            List<TbItem> itemList = itemMapper.selectByExample(example);
            if(itemList.size()>0){
                map.put("itemList",itemList);
            }
            if (itemCat1!=null){
                map.put("itemCat1",itemCat1);
            }
             if (itemCat2!=null){
                 map.put("itemCat2",itemCat2);
             }
            if (itemCat3!=null){
                map.put("itemCat3",itemCat3);
            }
            if (goods!=null){
                map.put("goods",goods);
            }
            if (goodsDesc!=null){
                map.put("goodsDesc",goodsDesc);
            }
            //获取输出流生成文件
            Writer writer = new FileWriter(pagedir+goodsId+".html");
            template.process(map,writer);
            writer.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
