package com.itheima.task;

import com.itheima.mapper.TbSeckillGoodsMapper;
import com.itheima.pojo.TbSeckillGoods;
import com.itheima.pojo.TbSeckillGoodsExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author JAVA
 * @create 2018-10-17 18:12
 */
@Component
public class SeckillTask {
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    //每分钟执行一次
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillGoods(){
        System.out.println("执行调度任务:"+new Date());
        System.out.println("查询缓存中所有的秒杀商品id集合");
        List ids= new ArrayList(redisTemplate.boundHashOps("seckillGoods").keys());
        TbSeckillGoodsExample example =new TbSeckillGoodsExample();
        TbSeckillGoodsExample.Criteria criteria = example.createCriteria();
        if (ids.size()>0){
            criteria.andGoodsIdNotIn(ids); //排除已经存在缓存中的秒杀商品
        }
        criteria.andStatusEqualTo("1");//审核通过的商品
        criteria.andStartTimeLessThan(new Date());//开始时间小于当前时间
        criteria.andEndTimeGreaterThan(new Date());//结束时间大于当前时间
        criteria.andStockCountGreaterThan(0);//剩余库存量大于0
        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);
        //装入缓存中
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            redisTemplate.boundHashOps("seckillGoods").put(seckillGoods.getId(),seckillGoods);
        }
    }
    //每一秒执行一次删除过期商品
    @Scheduled(cron ="* * * * * ?")
    public void removeSeckillGoods(){
        //查询所有秒杀商品
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps("seckillGoods").values();
        //判断过期商品
        for (TbSeckillGoods seckillGoods : seckillGoodsList) {
            if (seckillGoods.getEndTime().getTime()<new Date().getTime()){
                //更新到数据库
                seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
                System.out.println("调度任务->删除过期商品:"+new Date()+":"+seckillGoods.getId());
                redisTemplate.boundHashOps("seckillGoods").delete(seckillGoods.getId());
            }
        }
    }
}
