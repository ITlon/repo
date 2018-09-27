package com.itheima.search.service;

import java.util.List;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-09-22 19:22
 */
public interface SearchService {

    public Map search(Map searchMap);

    public void importList(List list);

    public void deleteByGoodsIds(List goodsIdList);
}
