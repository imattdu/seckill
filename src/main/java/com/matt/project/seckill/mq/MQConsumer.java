package com.matt.project.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.matt.project.seckill.dao.ItemStockDOMapper;
import com.matt.project.seckill.service.ItemService;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

/**
 * 功能：
 *
 * @author matt
 * @create 2020-12-24 15:23
 */
@Component
public class MQConsumer {

    private DefaultMQPushConsumer consumer;

    @Value("${mq.nameserver.addr}")
    private String nameAddr;

    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private ItemStockDOMapper itemStockDOMapper;

    @PostConstruct
    public void init() throws MQClientException {
        consumer = new DefaultMQPushConsumer("stock_consumer_group");
        consumer.setNamesrvAddr(nameAddr);
        consumer.subscribe(topicName, "*");

        // 如何处理消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {

                MessageExt messageExt = list.get(0);
                String msgJson = new String(messageExt.getBody());
                Map<String, Object> map = JSON.parseObject(msgJson, Map.class);


                Integer itemId = (Integer) map.get("itemId");
                Integer amount = (Integer) map.get("amount");

                int i = itemStockDOMapper.decreaseStock(itemId, amount);


                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        System.out.println("hello word");

    }


    public void receiveMessage() throws MQClientException {


    }


}
