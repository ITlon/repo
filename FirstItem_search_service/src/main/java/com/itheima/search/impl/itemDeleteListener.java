package com.itheima.search.impl;


import com.itheima.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

/**
 * @author JAVA
 * @create 2018-09-29 20:16
 */
@Component
public class itemDeleteListener implements MessageListener {
    @Autowired
    private SearchService searchService;

    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsId = (Long[]) objectMessage.getObject();
            System.out.println("监听获取到消息:" + goodsId);
            searchService.deleteByGoodsIds(Arrays.asList(goodsId));
            System.out.println("执行索引库删除");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
