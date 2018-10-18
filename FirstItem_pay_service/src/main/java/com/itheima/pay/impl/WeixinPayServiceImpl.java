package com.itheima.pay.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.itheima.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import util.HttpClient;

import java.util.HashMap;
import java.util.Map;

/**
 * @author JAVA
 * @create 2018-10-14 12:14
 */
@Service
public class WeixinPayServiceImpl implements WeixinPayService {
    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${notifyurl}")
    private String notifyurl;
    @Value("${partnerkey}")
    private String partnerkey;
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        try {Map param = new HashMap();
            param.put("appid", appid);//公众账号ID
            param.put("mch_id", partner);//商户号
            param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
            param.put("body", "品优购");//商品描述
            param.put("out_trade_no", out_trade_no);//商户订单号
            param.put("total_fee", total_fee);//标价金额
            param.put("spbill_create_ip", "127.0.0.1");//终端IP
            param.put("notify_url", notifyurl);//通知地址
            param.put("trade_type", "NATIVE");//交易类型
            //根据秘钥和参数集合生成发送到微信端的xml文件
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);//是否是 https 协议
            client.setXmlParam(signedXml);//发送的 xml 数据
            client.post();//执行 post 请求
            String resultXml = client.getContent(); //获取结果
            Map resultMap = WXPayUtil.xmlToMap(resultXml);
            System.out.println("生成支付二维码返回的结果:"+resultMap);
            Map map = new HashMap();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("out_trade_no", out_trade_no);//订单号
            map.put("total_fee", total_fee);//金额
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Map queryPayStatus(String out_trade_no) {
        try {//设置参数
            Map param = new HashMap();
            param.put("appid", appid);
            param.put("mch_id", partner);
            param.put("out_trade_no", out_trade_no);
            param.put("nonce_str", WXPayUtil.generateNonceStr());
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            client.setHttps(true);
            client.setXmlParam(signedXml);
            client.post();
            String resultXml = client.getContent();
            Map resultMap = WXPayUtil.xmlToMap(resultXml);
            System.out.println("查询返回结果:"+resultMap);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
    @Override
    public Map closePay(String out_trade_no){
        try {Map param = new HashMap();
            param.put("appid",appid);
            param.put("mch_id",partner);
            param.put("out_trade_no",out_trade_no);
            param.put("nonce_str",WXPayUtil.generateNonceStr());
            String signedXml = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            client.setHttps(true);
            client.setXmlParam(signedXml);
            client.post();
            String resultXml = client.getContent();
            Map resultMap = WXPayUtil.xmlToMap(resultXml);
            System.out.println("关闭订单后返回的结果:"+resultMap);
            return  resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            return  null;
        }
    }

}
