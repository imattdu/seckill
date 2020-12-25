package com.matt.project.seckill.mq;

import com.alibaba.fastjson.JSON;
import com.matt.project.seckill.dao.StockLogDOMapper;
import com.matt.project.seckill.dataobject.StockLogDO;
import com.matt.project.seckill.error.BusinessException;
import com.matt.project.seckill.service.OrderService;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.*;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * 功能：生产者
 *
 * @author matt
 * @create 2020-12-24 15:23
 */

@Component
public class MQProducer {

    private DefaultMQProducer mqProducer;
    private TransactionMQProducer transactionMQProducer;
    @Value("${mq.nameserver.addr}")
    private String nameAddr;
    @Value("${mq.topicname}")
    private String topicName;

    @Autowired
    private MQConsumer mqConsumer;

    @Autowired
    private StockLogDOMapper stockLogDOMapper;

    @Autowired
    private OrderService orderService;

    @PostConstruct
    public void init() throws MQClientException {
        mqProducer = new DefaultMQProducer("producer_group");

        mqProducer.setNamesrvAddr(nameAddr);
        mqProducer.start();


        transactionMQProducer = new TransactionMQProducer("transaction_producer_group");

        transactionMQProducer.setNamesrvAddr(nameAddr);
        transactionMQProducer.setCreateTopicKey(topicName);
        transactionMQProducer.start();

        transactionMQProducer.setTransactionListener(new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(Message message, Object o) {

                Map<String,Object> argMap = (Map<String,Object>)o;

                Integer userId = (Integer) (argMap.get("userId"));
                Integer itemId = (Integer) (argMap.get("itemId"));
                Integer amount = (Integer) (argMap.get("amount"));
                Integer promoId = (Integer) (argMap.get("promoId"));
                String stockLogId = (String) (argMap.get("stockLogId"));


                try {
                    orderService.createOrder(userId,itemId,amount,promoId,stockLogId);
                } catch (BusinessException e) {
                    e.printStackTrace();

                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.insertSelective(stockLogDO);

                    return LocalTransactionState.ROLLBACK_MESSAGE;
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                    stockLogDO.setStatus(3);
                    stockLogDOMapper.updateByPrimaryKeySelective(stockLogDO);
                    return LocalTransactionState.ROLLBACK_MESSAGE;
                }

                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(MessageExt messageExt) {

                String msgJson = messageExt.getBody().toString();
                Map<String,Object> map = JSON.parseObject(msgJson, Map.class);

                String stockLogId = (String)map.get("stockLogId");

                StockLogDO stockLogDO = stockLogDOMapper.selectByPrimaryKey(stockLogId);
                if (stockLogDO.getStatus() == 2) {
                    return LocalTransactionState.COMMIT_MESSAGE;
                } else if (stockLogDO.getStatus() == 1) {
                    return LocalTransactionState.UNKNOW;
                }

                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
        });

    }


    public Boolean transactionalAsyncSendCreateOrder(Integer userId, Integer itemId, Integer amount,
                                                       Integer promoId,String stockLogId) throws MQClientException {


        Map<String,Object> map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("amount",amount);
        map.put("stockLogId",stockLogId);


        Map<String,Object> argMap = new HashMap<>();
        argMap.put("userId",userId);
        argMap.put("itemId",itemId);
        argMap.put("amount",amount);
        argMap.put("promoId",promoId);
        argMap.put("stockLogId",stockLogId);

        TransactionSendResult transactionSendResult = null;


        try {
            Message message = new Message(topicName, JSON.toJSON(map).toString()
                    .getBytes("UTF-8"));
            transactionSendResult =
                    transactionMQProducer.sendMessageInTransaction(message, argMap);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return false;
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        }
        if (transactionSendResult.getLocalTransactionState() == LocalTransactionState.ROLLBACK_MESSAGE){
            return false;
        } else if (transactionSendResult.getLocalTransactionState()
                == LocalTransactionState.COMMIT_MESSAGE) {

            return true;
        } else {
            return false;
        }

    }


    public Boolean sendDecreaseStock(Integer itemId,Integer amount) throws UnsupportedEncodingException {


        Map<String,Integer> map = new HashMap<>();
        map.put("itemId",itemId);
        map.put("amount",amount);

        Message message = new Message(topicName, JSON.toJSON(map).toString().getBytes("UTF-8"));
        try {
            mqProducer.send(message);
        } catch (MQClientException e) {
            e.printStackTrace();
            return false;
        } catch (RemotingException e) {
            e.printStackTrace();
            return false;
        } catch (MQBrokerException e) {
            e.printStackTrace();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


}
