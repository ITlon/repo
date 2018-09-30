package com.itheima.search.impl;

import com.alibaba.fastjson.JSON;
import com.itheima.pojo.TbItem;
import com.itheima.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;


/**
 * @author JAVA
 * @create 2018-09-29 19:31
 */
@Component
public class ItemSearchListener implements MessageListener {
    @Autowired
    private SearchService searchService;


    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String messageText = textMessage.getText();
            List<TbItem> itemList = JSON.parseArray(messageText, TbItem.class);
            for (TbItem item : itemList) {
                Map specMap = JSON.parseObject(item.getSpec());
                item.setSpecMap(specMap);
            }
            searchService.importList(itemList);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
