package personal.fields.business;

import VO.ServerInfo;
import com.alibaba.fastjson.JSON;
import infrastructure.cache.Cache;
import infrastructure.cache.JedisCache;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import personal.fields.protocol.C2CSendReq;
import personal.fields.protocol.NotifyMsgText;
import personal.fields.protocol.NotifyPendingPackText;
import personal.fields.util.MessageHelper;
import personal.fields.util.Seq;
import personal.fields.vo.NotifyPendingPack;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class C2CBussiness {

    private final static Logger logger = LoggerFactory.getLogger(C2CBussiness.class);

    private DefaultMQProducer mqProducer;

    private DefaultMQPushConsumer mqConsumer;

    @Value("${rocketmq.nameserver}")
    private String namesrv;

    @Value("${servername}")
    private String servername;

    @Value("${logicId}")
    private Integer lid;

    private final Cache cache = new JedisCache();

    public C2CBussiness() {

    }

    @PostConstruct
    public void init() {
        try {

            mqProducer = new DefaultMQProducer("producer_of_" + servername);
            mqProducer.setNamesrvAddr(namesrv);
//            mqProducer.setRetryTimesWhenSendFailed(3);
            mqConsumer = new DefaultMQPushConsumer("consumer_of_" + servername);
            mqConsumer.setNamesrvAddr(namesrv);
            mqConsumer.subscribe("C2CTopic_send", "TAG:c2csend");
            // 消费不需要有序
            mqConsumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> list, ConsumeConcurrentlyContext consumeConcurrentlyContext) {
                    // 默认每次消费一个消息，一个消息由一个线程消费
                    try {
                        for (MessageExt msg : list) {
                            logger.info("logic 收到 send 消息：" + new String(msg.getBody()));
                            C2CSendReq c2cReq = JSON.parseObject(new String(msg.getBody()), C2CSendReq.class);
                            // Todo 落库 ,落库后可以获得 msgId
                            NotifyMsgText notifyMsg = MessageHelper.buildS2CNotifyText(c2cReq, /*从数据库返回的msgId*/999, Seq.generate());
                            NotifyPendingPackText notifyPending = new NotifyPendingPackText(notifyMsg, System.currentTimeMillis());
                            String json = (String) cache.get(new Integer(c2cReq.getToId()).toString());
                            ServerInfo serverInfo = JSON.parseObject(json, ServerInfo.class);

                            logger.info("serverInfo:" + serverInfo);

                            String nettyServername = serverInfo.getName();
                            SendResult sendResult = mqProducer.send(new Message("C2CTopic_notify", "TAG:notify:" + nettyServername,
                                    JSON.toJSONString(notifyPending).getBytes(RemotingHelper.DEFAULT_CHARSET)));
                            // 消费成功，notify 消息成功被投递到了 MQ 中。logic 任务已经完成
                            if (sendResult.getSendStatus() == SendStatus.SEND_OK) {
                                logger.info("notify 消息成功投递到 MQ！" + notifyPending);
                                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                            }
                        }
                        // 无法投递，消息将被重新消费
                        logger.warn("notify 消息投递到 MQ 失败，send消息不会被消费！");
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return null;
                }
            });
            mqProducer.start();
            mqConsumer.start();
            logger.info("MQ 启动成功!  namesrv: " + namesrv);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("MQ 启动失败！");
        }
    }



}
