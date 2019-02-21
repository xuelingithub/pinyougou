package com.pinyougou.search.service.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import com.alibaba.fastjson.JSONArray;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component  //注入bean..配置包扫描
public class ItemSearchListener  implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;
    @Override
    public void onMessage(Message message) {
        TextMessage textMessage=(TextMessage)message;
        try {
            String text = textMessage.getText();
            System.out.println("jiantingdao:"+text);
            List<TbItem> itemList = JSONArray.parseArray(text,TbItem.class);
            itemSearchService.importList(itemList);
            System.out.println("daorusuoyinku");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
