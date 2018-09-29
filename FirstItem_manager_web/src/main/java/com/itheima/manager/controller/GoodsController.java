package com.itheima.manager.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.page.service.ItemPageService;
import com.itheima.pojo.TbGoods;
import com.itheima.pojo.TbItem;
import com.itheima.pojoGroup.Goods;
import com.itheima.search.service.SearchService;
import com.itheima.service.GoodsService;
import entity.PageResult;
import entity.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/**
 * controller
 *
 * @author Administrator
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Reference
    private GoodsService goodsService;
    @Reference
    private SearchService searchService;
    @Reference
    private ItemPageService itemPageService;
    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findAll")
    public List<TbGoods> findAll() {
        return goodsService.findAll();
    }


    /**
     * 返回全部列表
     *
     * @return
     */
    @RequestMapping("/findPage")
    public PageResult findPage(int page, int rows) {
        return goodsService.findPage(page, rows);
    }

    /**
     * 修改
     *
     * @param goods
     * @return
     */
    @RequestMapping("/update")
    public Result update(@RequestBody Goods goods) {
        try {
            goodsService.update(goods);
            return new Result(true, "修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }

    /**
     * 获取实体
     *
     * @param id
     * @return
     */
    @RequestMapping("/findOne")
    public Goods findOne(Long id) {

        return goodsService.findOne(id);
    }

    /**
     * 批量删除
     *
     * @param ids
     * @return
     */
    @RequestMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            goodsService.delete(ids);
            searchService.deleteByGoodsIds(Arrays.asList(ids));
            return new Result(true, "删除成功");
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "删除失败");
        }
    }

    /**
     * 查询+分页
     *
     * @param
     * @param page
     * @param rows
     * @return
     */
    @RequestMapping("/search")
    public PageResult search(@RequestBody TbGoods goods, int page, int rows) {
        return goodsService.findPage(goods, page, rows);
    }

    @RequestMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status) {

        try {
            goodsService.updateStatus(ids, status);
            if (status.equals("1")) {

                List<TbItem> itemList = goodsService.findItemListByGoodsId(ids, status);

                if (itemList.size() > 0) {
                    searchService.importList(itemList);
                } else {
                    System.out.println("没有明细数据");
                }
                for (Long id : ids) {
                    itemPageService.genItemHtml(id);
                }
            }
            return new Result(true, "修改成功");

        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "修改失败");
        }
    }


}
