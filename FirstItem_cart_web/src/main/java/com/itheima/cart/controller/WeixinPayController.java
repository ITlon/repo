package com.itheima.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.itheima.order.service.OrderService;
import com.itheima.pay.service.WeixinPayService;
import com.itheima.pojo.TbPayLog;
import entity.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-10-14 12:53
 */
@RestController
@RequestMapping("/pay")
public class WeixinPayController {
    @Reference(timeout = 3000)
    private WeixinPayService weixinPayService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/createNative")
    public Map createNative() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        TbPayLog payLog = orderService.searchPayLogFromRedis(username);
        if (payLog==null){
            return new HashMap();
        }else {
            return weixinPayService.createNative(payLog.getOutTradeNo(), payLog.getTotalFee()+"");
        }
    }

    @RequestMapping("/query")
    public Result queryPayStatus(String out_trade_no) {
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
                orderService.updateOrderStatus(out_trade_no,map.get("transaction_id")+"");
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
                //关闭微信支付
                weixinPayService.closePay(out_trade_no);
                break;
            }
        }
        return result;
    }
}
