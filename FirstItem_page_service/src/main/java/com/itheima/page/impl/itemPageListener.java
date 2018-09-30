package com.itheima.page.impl;

import com.itheima.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * @author JAVA
 * @create 2018-09-30 17:21
 */
@Component
public class itemPageListener implements MessageListener {
    @Autowired
    private ItemPageService itemPageService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String goodsId = textMessage.getText();
            System.out.println("接收到消息："+goodsId);
            itemPageService.genItemHtml(Long.parseLong(goodsId));
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
