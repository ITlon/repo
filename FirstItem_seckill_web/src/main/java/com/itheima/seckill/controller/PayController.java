package com.itheima.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.pay.service.WeixinPayService;
import com.itheima.pojo.TbSeckillOrder;
import com.itheima.seckill.service.SeckillOrderService;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-10-15 19:07
 */
@SuppressWarnings("all")
@RestController
@RequestMapping("/pay")
public class PayController {
    @Reference
    private WeixinPayService weixinPayService;

    private SeckillOrderService seckillOrderService;

    @RequestMapping("/createNative")
    public Map createNative() {
        //获取当前用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbSeckillOrder seckillOrder = seckillOrderService.searchSeckillOrderFromRedis(username);
        if (seckillOrder == null) {
            return new HashMap();
        } else {
           long fen = (long) (seckillOrder.getMoney().doubleValue() * 100);
           return weixinPayService.createNative(seckillOrder.getId() + "", fen + "");
        }
    }
    @RequestMapping("/query")
    public Map queryStatus(String out_trade_no){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Result result = null;
        int seconds=0;
        while (true) {
            Map map = weixinPayService.queryPayStatus(out_trade_no);
            if (map == null) {//出错
                result = new Result(false, "支付出错");
                break;
            }
            if (map.get("trade_state").equals("SUCCESS")){
                result=new Result(true,"支付成功");
                seckillOrderService.saveSeckillOrderToDb(username,Long.valueOf(out_trade_no),map.get("transaction_id")+"");
                break;
            }
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            seconds++;
            if (seconds>=100){
                result=new Result(false,"二维码超时");
                Map closePay = weixinPayService.closePay(out_trade_no);
                if ("SUCCESS".equals(closePay.get("return_code"))&&closePay!=null){
                    if ("ORDERPAID".equals(closePay.get("err_code"))){
                        result=new Result(true, "支付成功");
                        seckillOrderService.saveSeckillOrderToDb(username,Long.valueOf(out_trade_no),map.get("transaction_id")+"");
                    }
                }else {
                    //删除缓存中的订单
                    seckillOrderService.deleteOrderFromRedis(username,Long.valueOf(out_trade_no));
                }
                break;
            }
        }
        return null;
    }
}
