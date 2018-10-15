package com.itheima.pay.service;

import com.itheima.pojo.TbPayLog;

import java.util.Map;

/**
 * @author JAVA
 * @create 2018-10-14 12:11
 */
public interface WeixinPayService {
    /**
     * 生成微信支付地址
     * @param out_trade_no 订单号
     * @param total_fee 金额
     * @return
     */
    public Map createNative(String out_trade_no, String total_fee);

    /**
     * 查询支付状态
     * @param out_trade_no 订单号
     * @return
     */
    public Map queryPayStatus(String out_trade_no);

    /**
     * 微信关闭订单
     * @param out_trade_no
     * @return
     */
    public Map closePay(String out_trade_no);
}
