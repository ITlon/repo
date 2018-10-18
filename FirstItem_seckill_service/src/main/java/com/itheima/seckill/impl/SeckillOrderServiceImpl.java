package com.itheima.seckill.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.TbSeckillGoodsMapper;
import com.itheima.mapper.TbSeckillOrderMapper;
import com.itheima.pojo.TbSeckillGoods;
import com.itheima.pojo.TbSeckillOrder;
import com.itheima.pojo.TbSeckillOrderExample;
import com.itheima.pojo.TbSeckillOrderExample.Criteria;
import com.itheima.seckill.service.SeckillOrderService;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import util.IdWorker;

import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private TbSeckillOrderMapper seckillOrderMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TbSeckillGoodsMapper seckillGoodsMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbSeckillOrder> findAll() {
        return seckillOrderMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.insert(seckillOrder);
    }


    /**
     * 修改
     */
    @Override
    public void update(TbSeckillOrder seckillOrder) {
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public TbSeckillOrder findOne(Long id) {
        return seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            seckillOrderMapper.deleteByPrimaryKey(id);
        }
    }


    @Override
    public PageResult findPage(TbSeckillOrder seckillOrder, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbSeckillOrderExample example = new TbSeckillOrderExample();
        Criteria criteria = example.createCriteria();

        if (seckillOrder != null) {
            if (seckillOrder.getUserId() != null && seckillOrder.getUserId().length() > 0) {
                criteria.andUserIdLike("%" + seckillOrder.getUserId() + "%");
            }
            if (seckillOrder.getSellerId() != null && seckillOrder.getSellerId().length() > 0) {
                criteria.andSellerIdLike("%" + seckillOrder.getSellerId() + "%");
            }
            if (seckillOrder.getStatus() != null && seckillOrder.getStatus().length() > 0) {
                criteria.andStatusLike("%" + seckillOrder.getStatus() + "%");
            }
            if (seckillOrder.getReceiverAddress() != null && seckillOrder.getReceiverAddress().length() > 0) {
                criteria.andReceiverAddressLike("%" + seckillOrder.getReceiverAddress() + "%");
            }
            if (seckillOrder.getReceiverMobile() != null && seckillOrder.getReceiverMobile().length() > 0) {
                criteria.andReceiverMobileLike("%" + seckillOrder.getReceiverMobile() + "%");
            }
            if (seckillOrder.getReceiver() != null && seckillOrder.getReceiver().length() > 0) {
                criteria.andReceiverLike("%" + seckillOrder.getReceiver() + "%");
            }
            if (seckillOrder.getTransactionId() != null && seckillOrder.getTransactionId().length() > 0) {
                criteria.andTransactionIdLike("%" + seckillOrder.getTransactionId() + "%");
            }

        }

        Page<TbSeckillOrder> page = (Page<TbSeckillOrder>) seckillOrderMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void submitOrder(Long seckillId, String userId) {
        TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillId);
        System.out.println("从缓存中读取秒杀商品");
        if (seckillGoods == null) {
            throw new RuntimeException("商品不存在");
        }
        if (seckillGoods.getStockCount() <= 0) {
            throw new RuntimeException("商品已抢购一空");
        }
        //减库存 再次存入缓存中
        seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
        redisTemplate.boundHashOps("seckillGoods").put(seckillId, seckillGoods);
        if (seckillGoods.getStockCount() == 0) {
            //库存没有 更新数据到数据库 删除缓存中的数据
            seckillGoodsMapper.updateByPrimaryKey(seckillGoods);
            System.out.println("将秒杀商品更新数据到数据库并删除缓存中的秒杀商品数据");
            redisTemplate.boundHashOps("seckillGoods").delete(seckillId);
        }
        //生成订单
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setMoney(seckillGoods.getCostPrice());//金额
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setCreateTime(new Date());//订单生成时间
        seckillOrder.setSeckillId(seckillId);//订单id
        seckillOrder.setSellerId(seckillGoods.getSellerId());//商家id
        seckillOrder.setStatus("0");//支付状态 未支付
        seckillOrder.setUserId(userId);//用户id
        //保存到缓存中
        redisTemplate.boundHashOps("seckillOrder").put(userId, seckillOrder);
        System.out.println("生成秒杀订单并保存到数据库");
    }

    @Override
    public TbSeckillOrder searchSeckillOrderFromRedis(String userId) {

        return (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
    }

    @Override
    public void saveSeckillOrderToDb(String userId, Long orderId, String transactionId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        System.out.println("从缓存中取出订单信息");
        if (seckillOrder == null) {
            throw new RuntimeException("订单不存在");
        }
        if (seckillOrder.getId().longValue() != orderId.longValue()) {
            throw new RuntimeException("订单不相符");
        }
        //更新信息
        seckillOrder.setPayTime(new Date());
        seckillOrder.setStatus("1");
        seckillOrder.setTransactionId(transactionId);
        System.out.println("将已经付款的秒杀订单保存到数据库并删除缓存中的订单数据");
        seckillOrderMapper.insert(seckillOrder);
        redisTemplate.boundHashOps("seckillOrder").delete(userId);
    }
    @Override
    public void deleteOrderFromRedis(String userId, Long orderId) {
        TbSeckillOrder seckillOrder = (TbSeckillOrder) redisTemplate.boundHashOps("seckillOrder").get(userId);
        if (seckillOrder != null && seckillOrder.getId().longValue() == orderId.longValue()) {
            System.out.println("删除缓存中的秒杀订单");
            redisTemplate.boundHashOps("seckillOrder").delete(userId);
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps("seckillGoods").get(seckillOrder.getSeckillId());
            if (seckillGoods != null) {
                System.out.println("更新秒杀商品库存再次存入缓存中");
                seckillGoods.setStockCount(seckillGoods.getStockCount() + 1);
                redisTemplate.boundHashOps("seckillGoods").put(seckillOrder.getSeckillId(), seckillGoods);
            }
        }
    }
}
