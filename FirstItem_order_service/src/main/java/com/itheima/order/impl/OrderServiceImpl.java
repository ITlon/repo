package com.itheima.order.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.itheima.mapper.TbOrderItemMapper;
import com.itheima.mapper.TbOrderMapper;
import com.itheima.mapper.TbPayLogMapper;
import com.itheima.order.service.OrderService;
import com.itheima.pojo.TbOrder;
import com.itheima.pojo.TbOrderExample;
import com.itheima.pojo.TbOrderExample.Criteria;
import com.itheima.pojo.TbOrderItem;
import com.itheima.pojo.TbPayLog;
import com.itheima.pojoGroup.Cart;
import entity.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import util.IdWorker;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class OrderServiceImpl implements OrderService {

	@Autowired
	private TbOrderMapper orderMapper;

	@Autowired
	private RedisTemplate redisTemplate;

	@Autowired
	private IdWorker idWorker;

	@Autowired
	private TbOrderItemMapper orderItemMapper;
	@Autowired
	private TbPayLogMapper payLogMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbOrder> findAll() {
		return orderMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbOrder> page=   (Page<TbOrder>) orderMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbOrder order) {
		
		//1.从redis中提取购物车列表
		List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(order.getUserId());
		//2.循环购物车列表添加订单
		double total_money=0;
		List orderIdList= new ArrayList();
		for (Cart cart : cartList) {
			//生成唯一id
			long orderId = idWorker.nextId();
			TbOrder tbOrder = new TbOrder();//创建订单对象
			tbOrder.setOrderId(orderId);//订单id
			tbOrder.setUserId(order.getUserId());//用户id
			tbOrder.setSellerId(cart.getSellerId());//商家id
			tbOrder.setStatus("1");//支付状态  为付款
			tbOrder.setSourceType(order.getSourceType());//订单来源
			tbOrder.setUpdateTime(new Date());//更新时间
			tbOrder.setCreateTime(new Date());//订单生成时间
			tbOrder.setReceiverAreaName(order.getReceiverAreaName());//地址
			tbOrder.setReceiver(order.getReceiver());//收货人
			tbOrder.setReceiverMobile(order.getReceiverMobile());//电话
			List<TbOrderItem> orderItemList = cart.getOrderItemList();
			double money=0;
			for (TbOrderItem orderItem : orderItemList) {
				orderItem.setId(idWorker.nextId());
				orderItem.setOrderId(orderId); //订单id
				orderItem.setSellerId(cart.getSellerId());//商家id
				money+=orderItem.getTotalFee().doubleValue();//每个购物车的金额
				orderItemMapper.insert(orderItem);
			}
            tbOrder.setPayment(new BigDecimal(money));
			orderMapper.insert(tbOrder);
			total_money+=money;//总金额
			orderIdList.add(orderId);
		}
		//微信支付方式->生成订单日志
		if (order.getPaymentType().equals("1")){
			TbPayLog payLog = new TbPayLog();
			payLog.setCreateTime(new Date());//生成日志的时间
			//所有订单的订单号
			payLog.setOrderList(orderIdList.toString().replace("[","").
					replace("]","").replace(" ",""));
			payLog.setTotalFee((long)(total_money*100));//总金额
			payLog.setUserId(order.getUserId());//当前用户
			payLog.setTradeState("0");//支付状态 未支付
			payLog.setPayType("1");//支付类型 微信
			payLog.setOutTradeNo(idWorker.nextId()+"");//支付订单号
			//插入数据库
			payLogMapper.insert(payLog);
			//根据用户名存入redis
			redisTemplate.boundHashOps("payLog").put(order.getUserId(),payLog);
		}

		//3.清空redis中的购物车

		redisTemplate.boundHashOps("cartList").delete(order.getUserId());
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbOrder order){
		orderMapper.updateByPrimaryKey(order);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbOrder findOne(Long id){
		return orderMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			orderMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbOrder order, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbOrderExample example=new TbOrderExample();
		Criteria criteria = example.createCriteria();
		
		if(order!=null){			
						if(order.getPaymentType()!=null && order.getPaymentType().length()>0){
				criteria.andPaymentTypeLike("%"+order.getPaymentType()+"%");
			}
			if(order.getPostFee()!=null && order.getPostFee().length()>0){
				criteria.andPostFeeLike("%"+order.getPostFee()+"%");
			}
			if(order.getStatus()!=null && order.getStatus().length()>0){
				criteria.andStatusLike("%"+order.getStatus()+"%");
			}
			if(order.getShippingName()!=null && order.getShippingName().length()>0){
				criteria.andShippingNameLike("%"+order.getShippingName()+"%");
			}
			if(order.getShippingCode()!=null && order.getShippingCode().length()>0){
				criteria.andShippingCodeLike("%"+order.getShippingCode()+"%");
			}
			if(order.getUserId()!=null && order.getUserId().length()>0){
				criteria.andUserIdLike("%"+order.getUserId()+"%");
			}
			if(order.getBuyerMessage()!=null && order.getBuyerMessage().length()>0){
				criteria.andBuyerMessageLike("%"+order.getBuyerMessage()+"%");
			}
			if(order.getBuyerNick()!=null && order.getBuyerNick().length()>0){
				criteria.andBuyerNickLike("%"+order.getBuyerNick()+"%");
			}
			if(order.getBuyerRate()!=null && order.getBuyerRate().length()>0){
				criteria.andBuyerRateLike("%"+order.getBuyerRate()+"%");
			}
			if(order.getReceiverAreaName()!=null && order.getReceiverAreaName().length()>0){
				criteria.andReceiverAreaNameLike("%"+order.getReceiverAreaName()+"%");
			}
			if(order.getReceiverMobile()!=null && order.getReceiverMobile().length()>0){
				criteria.andReceiverMobileLike("%"+order.getReceiverMobile()+"%");
			}
			if(order.getReceiverZipCode()!=null && order.getReceiverZipCode().length()>0){
				criteria.andReceiverZipCodeLike("%"+order.getReceiverZipCode()+"%");
			}
			if(order.getReceiver()!=null && order.getReceiver().length()>0){
				criteria.andReceiverLike("%"+order.getReceiver()+"%");
			}
			if(order.getInvoiceType()!=null && order.getInvoiceType().length()>0){
				criteria.andInvoiceTypeLike("%"+order.getInvoiceType()+"%");
			}
			if(order.getSourceType()!=null && order.getSourceType().length()>0){
				criteria.andSourceTypeLike("%"+order.getSourceType()+"%");
			}
			if(order.getSellerId()!=null && order.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+order.getSellerId()+"%");
			}
	
		}
		
		Page<TbOrder> page= (Page<TbOrder>)orderMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	@Override
	public void updateOrderStatus(String out_trade_no, String transaction_id) {
		//修改日志状态
		TbPayLog payLog = payLogMapper.selectByPrimaryKey(out_trade_no);
		payLog.setTradeState("1");//支付状态 已支付
		payLog.setTransactionId(transaction_id);//微信交易流水号
		payLog.setPayTime(new Date());//交易完成时间
		payLogMapper.updateByPrimaryKey(payLog);
		//更新订单状态
		String orderIds = payLog.getOrderList();
		String[] orderIdList = orderIds.split(",");
		for (String orderId : orderIdList) {
			TbOrder order = orderMapper.selectByPrimaryKey(Long.valueOf(orderId));
			if (order!=null){
				order.setStatus("2");
				orderMapper.updateByPrimaryKey(order);
			}
		}
		//删除redis中的订单日志记录
		redisTemplate.boundHashOps("payLog").delete(payLog.getUserId());
	}
	@Override
	public TbPayLog searchPayLogFromRedis(String userId) {
		return (TbPayLog) redisTemplate.boundHashOps("payLog").get(userId);
	}

}
