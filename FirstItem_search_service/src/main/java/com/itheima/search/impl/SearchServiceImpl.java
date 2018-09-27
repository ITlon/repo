package com.itheima.search.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.itheima.pojo.TbItem;
import com.itheima.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-09-22 19:24
 */
@Service
public class SearchServiceImpl implements SearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map search(Map searchMap) {
        if (!"".equals(searchMap.get("keywords"))||searchMap.get("keywords")!=null){
            //关键字空格处理
            String keywords = (String) searchMap.get("keywords");
            searchMap.put("keywords",keywords.replace(" ",""));
        }
        Map map = new HashMap();
        //查询高亮列表
        map.putAll(searchHighLight(searchMap));
        //查询分类
        List<String> categoryList = searchGroup(searchMap);
        map.put("categoryList", categoryList);
        //根据分类查询品牌和规格
        String category = (String) searchMap.get("category");
        if (category != null) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            //没选默认查询第一个
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }
        return map;
    }
    //更新到索引库
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }
    //删除被删除的的商品的索引
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        Query query = new SimpleQuery();
        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    //高亮查询
    private Map searchHighLight(Map searchMap) {
        Map map = new HashMap();
        //设置高亮域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
        //高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        HighlightQuery query = new SimpleHighlightQuery();
        query.setHighlightOptions(highlightOptions);
        //1.1关键字查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //1.2按照商品分类过滤查询
        if (!"".equals(searchMap.get("category"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.3按照品牌分类过滤查询
        if (!"".equals(searchMap.get("brand"))) {
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);
        }
        //1.4按照规格分类过滤查询
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
            for (String key : specMap.keySet()) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_spec_" + key).is(specMap.get(key));
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.5按照价格分类过滤查询
        if (!"".equals(searchMap.get("price"))){
            String[] arr= ((String) searchMap.get("price")).split("-");
            if (!arr[0].equals("0")){
                //区间起点不等于0
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(arr[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
            if (!arr[1].equals("*")){
                //区间终点不等于*
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(arr[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }
        //1.6分页查询
        //当前页
        Integer pageNo = (Integer) searchMap.get("pageNo");
        //每页显示条数
        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageNo==null){
            //设置默认当前页
            pageNo=1;
        }
        if (pageSize==null){
            //设置默认每页显示条数
            pageSize=20;
        }
        //起始查询索引
        query.setOffset((pageNo-1)*pageSize);
        //每页显示条数
        query.setRows(pageSize);
        //1.7排序查询
        String sortValue = (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if (sortValue!=null&&!sortValue.equals("")){
            if (sortValue.equals("ASC")){
                Sort sort = new Sort(Sort.Direction.ASC,"item_"+sortField);
                query.addSort(sort);
            }
            if (sortValue.equals("DESC")){
                Sort sort = new Sort(Sort.Direction.DESC,"item_"+sortField);
                query.addSort(sort);
            }
        }

        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for (HighlightEntry<TbItem> highlightEntry : highlightPage.getHighlighted()) {
            TbItem item = highlightEntry.getEntity();
            if (highlightEntry.getHighlights().size() > 0 &&
                    highlightEntry.getHighlights().get(0).getSnipplets().size() > 0) {
                item.setTitle(highlightEntry.getHighlights().get(0).getSnipplets().get(0));
            }

        }
        map.put("rows", highlightPage.getContent());
        //总页数
        map.put("totalPage",highlightPage.getTotalPages());
        //总记录数
        map.put("total",highlightPage.getTotalElements());
        return map;
    }

    //分组查询
    private List searchGroup(Map searchMap) {
        //创建集合
        List list = new ArrayList();
        Query query = new SimpleQuery();
        //关键字查询条件
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分组查询域
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> groupPage = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = groupPage.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组结果集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
        //遍历集合
        for (GroupEntry<TbItem> entry : entryList) {
            //将分组结果的名称封装到返回值中
            list.add(entry.getGroupValue());
        }
        return list;
    }

    private Map searchBrandAndSpecList(String category) {
        //创建map集合
        Map map = new HashMap();
        //根据分类从缓存中查询
        Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (templateId != null) {
            //根据模板id查询品牌
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            //添加进map集合
            map.put("brandList", brandList);
            //根据模板id查询规格
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            //添加进map集合
            map.put("specList", specList);
        }
        return map;
    }

}
