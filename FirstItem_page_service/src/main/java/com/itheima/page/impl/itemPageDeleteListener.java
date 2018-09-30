package com.itheima.page.impl;

import com.itheima.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * @author JAVA
 * @create 2018-09-30 17:44
 */
@Component
public class itemPageDeleteListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();
            System.out.println("ItemDeleteListener 监听接收到消息..."+goodsIds);
            boolean flag = itemPageService.deleteItemHtml(goodsIds);
            System.out.println("网页删除结果："+flag);
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
